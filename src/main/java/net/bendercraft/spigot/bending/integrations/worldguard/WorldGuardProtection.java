package net.bendercraft.spigot.bending.integrations.worldguard;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.SimpleFlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class WorldGuardProtection implements Listener {
    private StateFlag allBending;
    private StateFlag passivesBending;
    private StateFlag affinityBending;
    @Deprecated
    private StateFlag speBending;
    private StateFlag perksBending;

    private Map<BendingElement, StateFlag> elementFlags;
    private Map<BendingAffinity, StateFlag> affinityFlags;
    private Map<RegisteredAbility, StateFlag> abilityFlags;
    
    private List<StateFlag> all;

    private WorldGuardPlugin worldguard = null;
	private Bending plugin;
	private File folder;
	
	private Gson gson = new Gson();

    public WorldGuardProtection (Bending plugin) {
    	this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        //Get available plugins
        PluginManager pm = Bukkit.getServer().getPluginManager();

        Plugin worlguard = pm.getPlugin("WorldGuard");
        if ((worlguard != null) && (worlguard.isEnabled())) {
        	folder = new File(plugin.getDataFolder(), "flags");
        	folder.mkdirs();
            worldguard = (WorldGuardPlugin) worlguard;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
            generateFlags();
            addFlags();
            loadFlags();
            Bending.getInstance().getLogger().info("Add BENDING FLAGS to Worldguard");
        }
    }

	private void generateFlags() {
        allBending = new StateFlag("bending", true);
        passivesBending = new StateFlag("bending-passives",false);
        affinityBending = new StateFlag("bending-affinities", false);
        speBending = new StateFlag("bending-spe", false);
        perksBending = new StateFlag("bending-perks", false);

        elementFlags = new HashMap<BendingElement, StateFlag>();
        for (BendingElement element : BendingElement.values()) {
            if (element != BendingElement.NONE) {
                elementFlags.put(element, new StateFlag("bending-"+element.name().toLowerCase(), false));
            }
        }

        affinityFlags = new HashMap<BendingAffinity, StateFlag>();
        for (BendingAffinity affinity : BendingAffinity.values()) {
            if (affinity != BendingAffinity.NONE) {
                affinityFlags.put(affinity, new StateFlag("bending-"+affinity.name().toLowerCase(), false));
            }
        }

        abilityFlags = new HashMap<RegisteredAbility, StateFlag>();
        for (RegisteredAbility ability : AbilityManager.getManager().getRegisteredAbilities()) {
            abilityFlags.put(ability, new StateFlag("bending-"+ability.getName().toLowerCase(), false));
        }
        
        all = new LinkedList<>();
        all.add(allBending);
        all.add(passivesBending);
        all.add(affinityBending);
        all.add(speBending);
        all.add(perksBending);
        all.addAll(elementFlags.values());
        all.addAll(affinityFlags.values());
        all.addAll(abilityFlags.values());
    }

    private void addFlags() {
    	SimpleFlagRegistry registry = (SimpleFlagRegistry) worldguard.getFlagRegistry();
    	all.forEach(flag -> silentlyRegister(registry, flag));
    }
    
    private void silentlyRegister(SimpleFlagRegistry registry, StateFlag flag) {
    	// Because worlguard load its region before this code is executed, an "UnknownFlag" is created
    	// We therefore cannot user "register" method and we have to hook directly into store
		try {
			Field f = SimpleFlagRegistry.class.getDeclaredField("flags");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			ConcurrentMap<String, Flag<?>> flags = (ConcurrentMap<String, Flag<?>>) f.get(registry);
			flags.put(flag.getName().toLowerCase(), flag);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			Bending.getInstance().getLogger().log(Level.WARNING, "Flag "+flag.getName()+" failed to register", e1);
		}
    }
    
    public boolean isRegionProtectedFromPerks(Player player, Location location) {
        if (worldguard == null) {
            return false;
        }

        if (location == null) {
            return false;
        }

        com.sk89q.worldguard.LocalPlayer localPlayer = worldguard.wrapPlayer(player);
        RegionManager manager = worldguard.getRegionManager(location.getWorld());
        ApplicableRegionSet regions = manager.getApplicableRegions(location);

        State state = regions.queryState(localPlayer, perksBending);

        return state == StateFlag.State.DENY;
    }

    public boolean isRegionProtectedFromBending(Player player, RegisteredAbility ability, Location location) {
        if (worldguard == null) {
            return false;
        }

        if (ability == null || location == null) {
            return false;
        }

        com.sk89q.worldguard.LocalPlayer localPlayer = worldguard.wrapPlayer(player);
        RegionManager manager = worldguard.getRegionManager(location.getWorld());
        ApplicableRegionSet regions = manager.getApplicableRegions(location);

        StateFlag.State state = regions.queryState(localPlayer, abilityFlags.get(ability));
        if (state != null) {
            return state != StateFlag.State.ALLOW;
        }

        if (ability.getAffinity() != null && ability.getAffinity() != BendingAffinity.NONE) {
            state = regions.queryState(localPlayer, affinityFlags.get(ability.getAffinity()));
            if (state != null) {
                return state != StateFlag.State.ALLOW;
            }

            state = regions.queryState(localPlayer, affinityBending);
            if (state != null) {
                return state != StateFlag.State.ALLOW;
            }
            state = regions.queryState(localPlayer, speBending);
            if (state != null) {
                return state != StateFlag.State.ALLOW;
            }
        }

        state = regions.queryState(localPlayer, elementFlags.get(ability.getElement()));
        if (state != null) {
            return state != StateFlag.State.ALLOW;
        }

        if (ability.isPassive()) {
            state = regions.queryState(localPlayer, passivesBending);
            if (state != null) {
                return state != StateFlag.State.ALLOW;
            }
        }

        state = regions.queryState(localPlayer, allBending);

        return state == StateFlag.State.DENY;
    }

    public boolean isRegionProtectedFromExplosion(Player player, RegisteredAbility ability, Location loc) {
        if (this.worldguard == null) {
            return false;
        }
        if (player == null ||ability == null || loc == null) {
            return false;
        }
        if (isRegionProtectedFromBending(player, ability, loc)) {
            return true;
        }

        com.sk89q.worldguard.LocalPlayer localPlayer = worldguard.wrapPlayer(player);
        for (Location location : new Location[] { loc, player.getLocation() }) {
            if (!player.isOnline()) {
                return true;
            }
            RegionContainer container = worldguard.getRegionContainer();
            RegionQuery query = container.createQuery();
            if (!query.testState(location, localPlayer, DefaultFlag.OTHER_EXPLOSION)) {
                return true;
            }
        }

        return false;
    }

    public boolean isRegionProtectedFromBendingPassives(Player player, Location location) {
        if (worldguard == null) {
            return false;
        }

        if (location == null) {
            return false;
        }

        com.sk89q.worldguard.LocalPlayer localPlayer = worldguard.wrapPlayer(player);
        RegionManager manager = worldguard.getRegionManager(location.getWorld());
        ApplicableRegionSet regions = manager.getApplicableRegions(location);
        StateFlag.State state = regions.queryState(localPlayer, passivesBending);
        if (state != null) {
            return state != StateFlag.State.ALLOW;
        }

        state = regions.queryState(localPlayer, allBending);
        return state == StateFlag.State.DENY;
    }
    
    public void loadFlags() {
    	plugin.getServer().getWorlds().forEach(w -> loadFlags(w));
	}
    
    public void loadFlags(World world) {
    	FileReader fr = null;
    	try {
    		fr = new FileReader(getFile(world));
    		List<StoredFlags> flags = Arrays.asList(gson.fromJson(fr, StoredFlags[].class));
    		RegionManager rgm = worldguard.getRegionManager(world);
    		for(Entry<String, ProtectedRegion> entry : rgm.getRegions().entrySet()) {
    			for(StoredFlags storedFlag : flags) {
    				if(storedFlag.region.equals(entry.getKey())) {
    					try {
        					StateFlag flag = (StateFlag) worldguard.getFlagRegistry().get(storedFlag.flag);
        					entry.getValue().setFlag(flag, flag.unmarshal(storedFlag.value));
        				} catch (ClassCastException e1) {
        					plugin.getLogger().warning("Invalid stored flags '"+storedFlag.flag+"' for region '"+storedFlag.region+"' : "+e1.getMessage());
        				}
    				}
    			}
    		}
		} catch (JsonSyntaxException | JsonIOException e) {
			plugin.getLogger().log(Level.SEVERE, "Flags file problem", e);
		} catch (FileNotFoundException e) {
			// Quiet please !
		} finally {
			IOUtils.closeQuietly(fr);
		}
    }
    
    public void saveFlags(World world) {
    	List<StoredFlags> flags = new ArrayList<StoredFlags>();
    	RegionManager rgm = worldguard.getRegionManager(world);
    	for(Entry<String, ProtectedRegion> entry : rgm.getRegions().entrySet()) {
    		for(Entry<Flag<?>, Object> entryFlags : entry.getValue().getFlags().entrySet()) {
    			Optional<StateFlag> test = all.stream().filter(x -> x.getName().equals(entryFlags.getKey().getName())).findAny();
    			if(test.isPresent()) {
    				StoredFlags temp = new StoredFlags();
    				temp.flag = test.get().getName();
    				temp.region = entry.getKey();
    				temp.value = entryFlags.getValue();
    				flags.add(temp);
    			}
    		}
    	}
    	FileWriter fw = null;
    	try {
    		fw = new FileWriter(getFile(world));
			gson.toJson(flags, fw);
		} catch (JsonIOException | IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not save flags", e);
		} finally {
			if(fw != null) {
				IOUtils.closeQuietly(fw);
			}
		}
    }
    
    @EventHandler
    public void onWorldInit(WorldInitEvent e) {
    	loadFlags(e.getWorld());
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
    	loadFlags(e.getWorld());
    }
    
    @EventHandler
    public void onWorldSave(WorldSaveEvent e) {
    	saveFlags(e.getWorld());
    }
    
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent e) {
    	saveFlags(e.getWorld());
    }
    
    @EventHandler
    public void onServerCommand(PlayerCommandPreprocessEvent e) {
    	String[] args = e.getMessage().toLowerCase().trim().split(" ");
    	if(args.length > 1 
    			&& (args[0].equals("/rg") || args[0].equals("/region"))
    			&& args[1].equals("flag")) {
    		final World w = e.getPlayer().getWorld();
    		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					saveFlags(w);
				}
			}, 10);
    	}
    }
    
    private File getFile(World w) {
    	return new File(folder, w.getName()+".json");
    }
    
    private class StoredFlags {
    	public String region;
    	public String flag;
    	public Object value;
    }
}

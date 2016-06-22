package net.bendercraft.spigot.bending.integrations.worldguard;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class WorldGuardProtection {

    private StateFlag allBending;
    private StateFlag passivesBending;
    private StateFlag affinityBending;
    @Deprecated
    private StateFlag speBending;

    private Map<BendingElement, StateFlag> elementFlags;
    private Map<BendingAffinity, StateFlag> affinityFlags;
    private Map<RegisteredAbility, StateFlag> abilityFlags;

    private WorldGuardPlugin worldguard = null;

    public WorldGuardProtection (Bending plugin) {
        initialize();
    }

    private void initialize() {
        //Get available plugins
        PluginManager pm = Bukkit.getServer().getPluginManager();

        Plugin plugin = pm.getPlugin("WorldGuard");
        if ((plugin != null) && (plugin.isEnabled())) {
            worldguard = (WorldGuardPlugin) plugin;
            generateFlags();
            addFlags();
            Bending.getInstance().getLogger().info("Add BENDING FLAGS to Worldguard");
        }
    }

    private void generateFlags() {
        allBending = new StateFlag("bending", true);
        passivesBending = new StateFlag("bending-passives",false);
        affinityBending = new StateFlag("bending-affinities", false);
        speBending = new StateFlag("bending-spe", false);

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
    }

    private void addFlags() {
    	worldguard.getFlagRegistry().register(allBending);
        worldguard.getFlagRegistry().register(passivesBending);
        worldguard.getFlagRegistry().register(affinityBending);
        worldguard.getFlagRegistry().register(speBending);
        for (StateFlag flag : elementFlags.values()) {
            worldguard.getFlagRegistry().register(flag);
        }
        for (StateFlag flag : affinityFlags.values()) {
            worldguard.getFlagRegistry().register(flag);
        }
        for (StateFlag flag : abilityFlags.values()) {
            worldguard.getFlagRegistry().register(flag);
        }
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
}

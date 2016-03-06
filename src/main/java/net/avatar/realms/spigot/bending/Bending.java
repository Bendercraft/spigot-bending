package net.avatar.realms.spigot.bending;

import net.avatar.realms.spigot.bending.integrations.citizens.BendableTrait;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.earth.TremorSense;
import net.avatar.realms.spigot.bending.commands.BendingCommandExecutor;
import net.avatar.realms.spigot.bending.controller.BendingManager;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.db.DBUtils;
import net.avatar.realms.spigot.bending.db.IBendingDB;
import net.avatar.realms.spigot.bending.learning.BendingLearning;
import net.avatar.realms.spigot.bending.listeners.BendingBlockListener;
import net.avatar.realms.spigot.bending.listeners.BendingDenyItem;
import net.avatar.realms.spigot.bending.listeners.BendingEntityListener;
import net.avatar.realms.spigot.bending.listeners.BendingPlayerListener;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class Bending extends JavaPlugin {
	private static Bending instance;
	
	private BendingManager manager;
	private BendingEntityListener listener;
	private BendingPlayerListener bpListener;
	private BendingBlockListener blListener;
	private IBendingDB bendingDatabase;

	private BendingLearning learning;

	private BendingCommandExecutor commandExecutor;

	@Override
	public void onEnable() {
		instance = this;
		
		if(manager == null)
			manager = new BendingManager(this);
		if(listener == null)
			listener = new BendingEntityListener(this);
		if(bpListener == null)
			bpListener = new BendingPlayerListener(this);
		if(blListener == null)
			blListener = new BendingBlockListener(this);

		this.commandExecutor = new BendingCommandExecutor();

		Settings.applyConfiguration(getDataFolder());
		AbilityManager.getManager().registerAllAbilities();
		AbilityManager.getManager().applyConfiguration(getDataFolder());

		Messages.loadMessages();

		// Learning
		this.learning = new BendingLearning();
		this.learning.onEnable();

		bendingDatabase = DBUtils.choose(Settings.DATABASE);
		// Fatal error
		if (bendingDatabase == null) {
			throw new RuntimeException("Invalid database : " + Settings.DATABASE);
		}
		bendingDatabase.init(this);

		getServer().getPluginManager().registerEvents(this.listener, this);
		getServer().getPluginManager().registerEvents(this.bpListener, this);
		getServer().getPluginManager().registerEvents(this.blListener, this);
		if(Settings.DENY_ITEMS) {
			getServer().getPluginManager().registerEvents(new BendingDenyItem(), this);
		}

		getCommand("bending").setExecutor(this.commandExecutor);
		getCommand("bending").setTabCompleter(this.commandExecutor);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, this.manager, 0, 1);

		ProtectionManager.init();
		getLogger().info("Bending v" + getDescription().getVersion() + " has been loaded.");

		// Citizens
		if ((getServer().getPluginManager().getPlugin("Citizens") != null) && getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
			CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BendableTrait.class).withName("bendable"));
		}
		
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		
		PacketListener listener = new PacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Bending.getInstance().onPacketSending(event);
            }
        };
		manager.addPacketListener(listener);
	}
	
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		// Because we listen for "ENTITY_METADATA", packet we should have is "PacketPlayOutEntityMetadata"
		// it has one integer filed : entity id
		int entityID = packet.getIntegers().readSafely(0);
		
		boolean suppress = false;
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(TremorSense.NAME);
		if(instances != null && !instances.isEmpty()) {
			for(BendingAbility raw : instances.values()) {
				TremorSense ability = (TremorSense) raw;
				if(ability.getIds().contains(entityID) && event.getPlayer() != ability.getPlayer()) {
					suppress = true;
					break;
				}
			}
		}
		
		if(suppress) {
			//This packet also have a collection of datawatcher with only one on it
			List<WrappedWatchableObject> metadatas = packet.getWatchableCollectionModifier().readSafely(0);
			WrappedWatchableObject status = null;
			for (WrappedWatchableObject metadata : metadatas) {
				//See http://wiki.vg/Entities for explanation on why index 0
				try {
					if (metadata.getIndex() == 0) {
						status = metadata;
		                break;
		            }
				} catch(FieldAccessException e) {
					
				}
			}
			if(status != null) {
				byte mask = (byte) status.getValue(); //0x40 = Glowing effect mask
				mask &= ~0x40;//0x40 = Glowing effect mask
				status.setValue(mask);
			}
		}
	}

	@Override
	public void onDisable() {
		PluginTools.stopAllBending();
		AbilityManager.getManager().stopAllAbilities();
		getServer().getScheduler().cancelTasks(this);

		this.learning.onDisable();
	}

	public BendingManager getManager() {
		return manager;
	}

	public IBendingDB getBendingDatabase() {
		return bendingDatabase;
	}

	public BendingLearning getLearning() {
		return learning;
	}

	public void reloadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public static void callEvent(Event e) {
		Bukkit.getServer().getPluginManager().callEvent(e);
	}
	
	public static Bending getInstance() {
		return instance;
	}
}

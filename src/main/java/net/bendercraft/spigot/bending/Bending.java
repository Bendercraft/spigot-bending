package net.bendercraft.spigot.bending;

import net.bendercraft.spigot.bending.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketListener;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.commands.BendingCommandExecutor;
import net.bendercraft.spigot.bending.controller.BendingManager;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.db.DBUtils;
import net.bendercraft.spigot.bending.db.IBendingDB;
import net.bendercraft.spigot.bending.integrations.citizens.BendableTrait;
import net.bendercraft.spigot.bending.integrations.protocollib.BendingPacketAdapter;
import net.bendercraft.spigot.bending.learning.BendingLearning;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
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
		
		if(manager == null) {
			manager = new BendingManager(this);
		}
		if(listener == null) {
			listener = new BendingEntityListener(this);
		}
		if(bpListener == null) {
			bpListener = new BendingPlayerListener(this);
		}
		if(blListener == null) {
			blListener = new BendingBlockListener(this);
		}

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
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.listener, this);
		pm.registerEvents(this.bpListener, this);
		pm.registerEvents(this.blListener, this);

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
		
		PacketListener listener = new BendingPacketAdapter(this);
		manager.addPacketListener(listener);
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

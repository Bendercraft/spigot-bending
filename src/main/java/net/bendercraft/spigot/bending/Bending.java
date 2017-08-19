package net.bendercraft.spigot.bending;

import net.bendercraft.spigot.bending.listeners.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketListener;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.commands.BendingCommandExecutor;
import net.bendercraft.spigot.bending.controller.BendingManager;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.db.MySQLDB;
import net.bendercraft.spigot.bending.integrations.citizens.BendableTrait;
import net.bendercraft.spigot.bending.integrations.protocollib.BendingPacketAdapter;
import net.bendercraft.spigot.bending.learning.BendingLearning;
import net.bendercraft.spigot.bending.utils.BendingAbilityLogFormatter;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.RollingFileHandler;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class Bending extends JavaPlugin {
	private static Bending instance;
	
	private BendingManager manager;
	private BendingEntityListener listener;
	private BendingPlayerListener bpListener;
	private BendingBlockListener blListener;

	private BendingLearning learning;

	private BendingCommandExecutor commandExecutor;
	
	private Logger abilityLogger;

	@Override
	public void onEnable() {
		instance = this;
		
		abilityLogger = Logger.getLogger("Bending Ability");
		abilityLogger.setUseParentHandlers(false);
		try {
			File logs = new File(getDataFolder(), "logs");
			logs.mkdirs();
			RollingFileHandler handler = new RollingFileHandler(logs, "bending_%d.log");
			handler.setFormatter(new BendingAbilityLogFormatter());
			abilityLogger.addHandler(handler);
		} catch (SecurityException | IOException e) {
			getLogger().log(Level.SEVERE, "Ability logger error", e);
		}
		
		
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
		BendingPerk.collect();

		Messages.loadMessages();

		// Learning
		this.learning = new BendingLearning();
		this.learning.onEnable();
		
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
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new TempBlock.QueueRevert(), 20*1, 20*5);
		getServer().getScheduler().runTaskTimerAsynchronously(this, new MySQLDB.SaveTask(), 20*30, 20*30);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new MySQLDB.UpdateTask(), 20*45, 20*30);

		ProtectionManager.init(this);
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
		
		// Force save
		MySQLDB.SaveTask save = new MySQLDB.SaveTask();
		save.run();
	}

	public BendingManager getManager() {
		return manager;
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

	public Logger getAbilityLogger() {
		return abilityLogger;
	}
}

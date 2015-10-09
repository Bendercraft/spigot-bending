package net.avatar.realms.spigot.bending;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.citizens.UnbendableTrait;
import net.avatar.realms.spigot.bending.commands.BendingCommandExecutor;
import net.avatar.realms.spigot.bending.controller.BendingManager;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.db.DBUtils;
import net.avatar.realms.spigot.bending.db.IBendingDB;
import net.avatar.realms.spigot.bending.learning.BendingLearning;
import net.avatar.realms.spigot.bending.listeners.BendingBlockListener;
import net.avatar.realms.spigot.bending.listeners.BendingEntityListener;
import net.avatar.realms.spigot.bending.listeners.BendingPlayerListener;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class Bending extends JavaPlugin {
	public static long time_step = 1; // in ms
	public static Logger log;
	public static Bending plugin;
	public BendingManager manager;
	public BendingEntityListener listener;
	public BendingPlayerListener bpListener;
	public BendingBlockListener blListener;
	public static IBendingDB database;
	public Tools tools;

	public BendingLearning learning;

	private BendingCommandExecutor commandExecutor;

	@Override
	public void onEnable() {
		plugin = this;
		log = plugin.getLogger();
		
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

		database = DBUtils.choose(Settings.DATABASE);
		// Fatal error
		if (database == null) {
			throw new RuntimeException("Invalid database : " + Settings.DATABASE);
		}
		database.init(this);
		this.tools = new Tools();

		getServer().getPluginManager().registerEvents(this.listener, this);
		getServer().getPluginManager().registerEvents(this.bpListener, this);
		getServer().getPluginManager().registerEvents(this.blListener, this);

		getCommand("bending").setExecutor(this.commandExecutor);
		getCommand("bending").setTabCompleter(this.commandExecutor);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, this.manager, 0, 1);

		ProtectionManager.init();
		Bending.log.info("Bending v" + getDescription().getVersion() + " has been loaded.");

		// Citizens
		if ((getServer().getPluginManager().getPlugin("Citizens") != null) && getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
			CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(UnbendableTrait.class).withName("unbendable"));
		}
	}

	@Override
	public void onDisable() {
		PluginTools.stopAllBending();
		AbilityManager.getManager().stopAllAbilities();
		getServer().getScheduler().cancelTasks(plugin);

		this.learning.onDisable();
	}

	public void reloadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public static void callEvent(Event e) {
		Bukkit.getServer().getPluginManager().callEvent(e);
	}
}

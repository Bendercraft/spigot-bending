package net.avatar.realms.spigot.bending;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.citizens.UnbendableTrait;
import net.avatar.realms.spigot.bending.controller.BendingManager;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.controller.RevertChecker;
import net.avatar.realms.spigot.bending.controller.TempBackup;
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

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public class Bending extends JavaPlugin {
	public static long time_step = 1; // in ms
	public static Logger log = null;
	public static Bending plugin;
	public final BendingManager manager = new BendingManager(this);
	public final BendingEntityListener listener = new BendingEntityListener(this);
	public final BendingPlayerListener bpListener = new BendingPlayerListener(this);
	public final BendingBlockListener blListener = new BendingBlockListener(this);
	private final RevertChecker revertChecker = new RevertChecker(this);
	static Map<String, String> commands = new HashMap<String, String>();
	public final ConfigManager configuration = new ConfigManager();
	public static Language language;
	public static TempBackup backup;
	public static IBendingDB database;
	public TempBackup tBackup;
	public Tools tools;
	
	public BendingLearning learning;

	@Override
	public void onEnable () {
		plugin = this;
		log = plugin.getLogger();
		saveDefaultConfig();
		
		Messages.loadMessages();
		
		//Learning
		learning = new BendingLearning();
		learning.onEnable();
		
		AbilityManager.getManager().registerAllAbilities();
		AbilityManager.getManager().applyConfiguration(getDataFolder());
		
		
		configuration.load(new File(getDataFolder(), "config.yml"));
		language = new Language();
		language.load(new File(getDataFolder(), "language.yml"));
		backup = new TempBackup(getDataFolder());
		database = DBUtils.choose(ConfigManager.database);
		// Fatal error
		if (database == null) {
			throw new RuntimeException("Invalid database : " + ConfigManager.database);
		}
		database.init(this);
		this.tBackup = new TempBackup(getDataFolder());
		BendingPlayer.initializeCooldowns();
		this.tools = new Tools();
		
		getServer().getPluginManager().registerEvents(this.listener, this);
		getServer().getPluginManager().registerEvents(this.bpListener, this);
		getServer().getPluginManager().registerEvents(this.blListener, this);
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, this.manager, 0, 1);
		getServer().getScheduler().runTaskTimerAsynchronously(plugin, this.revertChecker, 0, 200);
		
		ProtectionManager.init();
		PluginTools.verbose("Bending v" + getDescription().getVersion() + " has been loaded.");
		registerCommands();
		
		//Citizens
		if ((getServer().getPluginManager().getPlugin("Citizens") != null) && getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
			 CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(UnbendableTrait.class).withName("unbendable"));
	    }
	}

	@Override
	public void onDisable () {
		PluginTools.stopAllBending();
		AbilityManager.getManager().stopAllAbilities();
		getServer().getScheduler().cancelTasks(plugin);
		
		learning.onDisable();
	}

	public void reloadConfiguration () {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	private void registerCommands () {
		commands.put("command.admin", "remove <player>");
		commands.put("admin.reload", "reload");
		commands.put("admin.permaremove", "permaremove <player>");
		commands.put("command.choose", "choose <element>");
		commands.put("admin.choose", "choose <player> <element>");
		commands.put("admin.add", "add <element>");
		commands.put("command.displayelement", "display <element>");
		commands.put("command.clear", "clear");
		commands.put("command.display", "display");
		commands.put("command.bind", "bind <ability>");
		commands.put("command.version", "version");
		commands.put("command.bindmode", "bindmode [item/slot]");
	}

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
		// Will have to change that to allow the console sender
		Player player = null;
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		if (cmd.getName().equalsIgnoreCase("bending")) {
			new BendingCommand(player, args, getDataFolder(), getServer());
		}
		return true;
	}

	public static void callEvent (Event e) {
		Bukkit.getServer().getPluginManager().callEvent(e);
	}
}

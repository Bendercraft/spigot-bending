package net.avatarrealms.minecraft.bending;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.controller.BendingManager;
import net.avatarrealms.minecraft.bending.controller.BendingPlayers;
import net.avatarrealms.minecraft.bending.controller.BendingPlayersSaver;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.RevertChecker;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Bending extends JavaPlugin {

	public static long time_step = 1; // in ms
	public static Logger log = Logger.getLogger("Bending");

	public static Bending plugin;

	public final BendingManager manager = new BendingManager(this);
	public final BendingListener listener = new BendingListener(this);
	private final RevertChecker revertChecker = new RevertChecker(this);
	private final BendingPlayersSaver saver = new BendingPlayersSaver();

	static Map<String, String> commands = new HashMap<String, String>();
	// public static ConcurrentHashMap<String, List<BendingType>> benders = new
	// ConcurrentHashMap<String, List<BendingType>>();

	// public BendingPlayers config = new BendingPlayers(getDataFolder(),
	// getResource("bendingPlayers.yml"));
	public static ConfigManager configManager = new ConfigManager();
	public static Language language = new Language();
	public BendingPlayers config;
	public Tools tools;

	public String[] waterbendingabilities;
	public String[] airbendingabilities;
	public String[] earthbendingabilities;
	public String[] firebendingabilities;
	public String[] chiblockingabilities;

	static int air = 0, earth = 0, water = 0, fire = 0, chi = 0;

	public void onDisable() {

		Tools.stopAllBending();
		// PlayerStorageWriter.finish();
		BendingPlayersSaver.save();

		getServer().getScheduler().cancelTasks(plugin);

	}

	public void onEnable() {
		plugin = this;
		configManager.load(new File(getDataFolder(), "config.yml"));
		language.load(new File(getDataFolder(), "language.yml"));

		config = new BendingPlayers(getDataFolder());
		BendingPlayer.initializeCooldowns();

		tools = new Tools(config);

		// for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
		// benders.put(player.getName(),
		// config.getBendingTypes(player.getName()));
		// BendingPlayer.getBendingPlayer(player);
		// }

		waterbendingabilities = Abilities.getWaterbendingAbilities();
		airbendingabilities = Abilities.getAirbendingAbilities();
		earthbendingabilities = Abilities.getEarthbendingAbilities();
		firebendingabilities = Abilities.getFirebendingAbilities();
		chiblockingabilities = Abilities.getChiBlockingAbilities();

		getServer().getPluginManager().registerEvents(listener, this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, manager, 0,
				1);

		getServer().getScheduler().runTaskTimerAsynchronously(plugin,
				revertChecker, 0, 200);
		getServer().getScheduler().runTaskTimerAsynchronously(plugin, saver, 0,
				20 * 60 * 3);

		Tools.printHooks();
		Tools.verbose("Bending v" + this.getDescription().getVersion()
				+ " has been loaded.");

		registerCommands();

	}

	public void reloadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();

	}

	private void registerCommands() {
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

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("bending")) {

			new BendingCommand(player, args, getDataFolder(), getServer());

		}

		return true;
	}
}

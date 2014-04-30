package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import tools.Abilities;
import tools.BendingPlayer;
import tools.BendingType;
import tools.ConfigManager;
import tools.Tools;

public class BendingCommand {

	private final String[] bindAliases = { "bind", "b" };
	private final String[] clearAliases = { "clear", "cl" };
	private final String[] chooseAliases = { "choose", "ch" };
	private final String[] addAliases = { "add", "a" };
	private final String[] removeAliases = { "remove", "r" };
	private final String[] permaremoveAliases = { "permaremove", "premove",
			"pr", "p" };
	private final String[] toggleAliases = { "toggle", "t" };
	private final String[] displayAliases = { "display", "disp", "dis", "d" };
	private final String[] reloadAliases = { "reload" };
	private final String[] helpAliases = { "help", "h", "?" };
	private final String[] importAliases = { "import" };
	private final String[] whoAliases = { "who", "wh", "w" };
	private final String[] languageAliases = { "language", "lang", "la", "l" };
	private final String[] bindModeAliases = { "bindmode", "bmode", "bindm",
			"bm" };
	private final String[] versionAliases = { "version", "ver", "v" };

	private final String[] airbendingAliases = { "air", "a", "airbender",
			"airbending", "airbend" };
	private final String[] earthbendingAliases = { "earth", "e", "earthbender",
			"earthbending", "earthbend" };
	private final String[] firebendingAliases = { "fire", "f", "firebender",
			"firebending", "firebend" };
	private final String[] waterbendingAliases = { "water", "w", "waterbender",
			"waterbending", "waterbend" };
	private final String[] chiblockingAliases = { "chi", "c", "chiblock",
			"chiblocker", "chiblocking" };

	private final String[] itemAliases = { "item", "ite", "it", "i" };
	private final String[] slotAliases = { "slot", "slo", "sl", "s" };

	private String[] waterbendingabilities = Abilities
			.getWaterbendingAbilities();
	private String[] airbendingabilities = Abilities.getAirbendingAbilities();
	private String[] earthbendingabilities = Abilities
			.getEarthbendingAbilities();
	private String[] firebendingabilities = Abilities.getFirebendingAbilities();
	private String[] chiblockingabilities = Abilities.getChiBlockingAbilities();

	private File dataFolder;
	// private Bending config;
	private Server server;
	private boolean verbose = true;
	private BendingPlayer bPlayer;

	static final String usage = "Usage";
	static final String the_server = "The server";
	static final String who_usage = "Displays a list of users online, along with their bending types.";
	static final String who_not_on_server = "is not on the server.";
	static final String who_player_usage = "Displays which bending types that player has.";
	static final String choose_usage = "Choose your <element> (Options: air, water, earth, fire, chiblocker).";
	static final String choose_player_usage = "Choose <element> for <player>.";
	static final String no_perms_air = "You do not have permission to be an airbender.";
	static final String no_perms_fire = "You do not have permission to be a firebender.";
	static final String no_perms_earth = "You do not have permission to be an earthbender.";
	static final String no_perms_water = "You do not have permission to be a waterbender.";
	static final String no_perms_chi = "You do not have permission to be a chiblocker.";
	static final String other_no_perms_air = "They do not have permission to be an airbender.";
	static final String other_no_perms_fire = "They do not have permission to be a firebender.";
	static final String other_no_perms_earth = "They do not have permission to be an earthbender.";
	static final String other_no_perms_water = "They do not have permission to be a waterbender.";
	static final String other_no_perms_chi = "They do not have permission to be a chiblocker.";
	static final String choosen_air = "You are now an airbender!";
	static final String choosen_fire = "You are now a firebender!";
	static final String choosen_earth = "You are now an earthbender!";
	static final String choosen_water = "You are now a waterbender!";
	static final String choosen_chi = "You are now a chiblocker!";
	static final String you_changed = "You have changed";
	static final String changed_you = "has changed your bending.";
	static final String import_usage = "Imports data from your bendingPlayers.yml file to your MySQL database.";
	static final String import_noSQL = "MySQL needs to be enabled to import bendingPlayers";
	static final String import_success = "Imported BendingPlayers to MySQL.";
	static final String no_execute_perms = "You do not have permission to execute that command.";
	static final String no_bind_perms = "You do not have permissions to bind";
	static final String no_use_perms = "You do not have permissions to use";
	static final String reload_usage = "Reloads Bending configuration, languages and players. Also restarts all bending.";
	static final String reload_success = "was reloaded.";
	static final String display_usage = "Displays all the abilities you have bound.";
	static final String display_element_usage = "Displays all available abilites for <element>";
	static final String slot = "Slot";
	static final String display_no_abilities = "You have no abilities bound.";
	static final String toggle_usage = "Toggles your bending on or off. Passives will still work.";
	static final String toggle_off = "You toggled your bending. You now can't use bending until you use that command again.";
	static final String toggle_on = "You toggled your bending back. You can now use them freely again!";
	static final String admin_toggle_off = "has toggled your bending. You now can't use bending until you use /bending toggle.";
	static final String admin_toggle_on = "has toggled your bending back. You can now use them freely again!";
	static final String admin_toggle = "You have toggled the bending of: ";
	static final String not_from_console = "This command cannot be used from the console.";
	static final String permaremove_message = "Permanently removes the bending of <player1> (optionally accepts a list of players) until someone with permissions chooses their bending.";
	static final String you_permaremove = "You have permanently removed the bending of: ";
	static final String permaremove_you = "has removed your bending permanently.";
	static final String remove_usage = "Removes all of <player>'s bending.";
	static final String remove_you = "has removed your bending.";
	static final String you_remove = "You have removed the bending of: ";
	static final String add_self = "Add <element>, allowing you to use it along with what you already can do.";
	static final String add_other = "Adds <element> to <player>.";
	static final String clear_all = "Clears all abilities you have bound.";
	static final String clear_slot = "Clears the ability bound to <slot#>";
	static final String clear_item = "Clears the ability bound to <item>";
	static final String cleared_message = "Your abilities have been cleared.";
	static final String slot_item_cleared = "has been cleared.";
	static final String bind_slot = "This binds <ability> to the item you are holding.";
	static final String bind_to_slot = "This binds <ability> to <slot#>.";
	static final String bind_item = "This binds <ability> to your current item.";
	static final String bind_to_item = "This binds <ability> to <item>.";
	static final String bind_mode_usage = "Displays your current bind mode, whether you bind abilities to slots or items.";
	static final String bind_mode_change_usage = "Changes your bind mode, allowing you to bind to items instead of slots, or vice versa.";
	static final String bind_mode_change = "You have changed your binding mode to bind to: ";
	static final String your_bind_mode = "You're currently binding to: ";
	static final String server_bind_mode = "The server default of bindmode is: ";
	static final String version_usage = "Displays information about the version and author of Bending.";
	static final String not_air = "You are not an airbender.";
	static final String not_earth = "You are not an earthbender.";
	static final String not_fire = "You are not a firebender.";
	static final String not_water = "You are not a waterbender.";
	static final String not_chi = "You are not a chiblocker.";
	static final String bound_to = "bound to";
	static final String bound_to_slot = "bound to slot";
	static final String help_list = "Use /bending help to see a list of commands.";
	static final String command_list = "Use /bending help <command> to see how to use that command.";
	static final String ability_list = "Use /bending help <ability> to get help with that ability.";
	static final String player = "player";
	static final String element = "element";
	static final String language_success = "You have successfully changed your language.";
	static final String language_not_supported = "That language is not supported.";
	static final String your_language = "Your language: ";
	static final String default_language = "Default (server) language: ";
	static final String supported_languages = "Supported languages: ";
	static final String language_usage = "Displays your language, the default language, and the supported languages.";
	static final String language_change_usage = "Changes your language to <language>. It must be a supported language.";
	static final String you_already_air = "You are already an airbender.";
	static final String you_already_earth = "You are already an earthbender.";
	static final String you_already_fire = "You are already a firebender.";
	static final String you_already_water = "You are already a waterbender.";
	static final String you_already_chi = "You are already a chiblocker.";
	static final String they_already_air = "is already an airbender.";
	static final String they_already_earth = "is already an earthbender.";
	static final String they_already_fire = "is already a firebender.";
	static final String they_already_water = "is already a waterbender.";
	static final String they_already_chi = "is already a chiblocker.";
	static final String add_air = "You are now also an airbender!";
	static final String add_earth = "You are now also an earthbender!";
	static final String add_fire = "You are now also a firebender!";
	static final String add_water = "You are now also a waterbender!";
	static final String add_chi = "You are now also a chiblocker!";
	static final String add_you_air = "has made you also an airbender!";
	static final String add_you_earth = "has made you also an earthbender!";
	static final String add_you_fire = "has made you also a firebender!";
	static final String add_you_water = "has made you also a waterbender!";
	static final String add_you_chi = "has made you also a chiblocker!";
	static final String you_add_air = "is now also an airbender!";
	static final String you_add_earth = "is now also an earthbender!";
	static final String you_add_fire = "is now also a firebender!";
	static final String you_add_water = "is now also a waterbender!";
	static final String you_add_chi = "is now also a chiblocker!";

	public BendingCommand(Player player, String[] args, File dataFolder,
			Server server) {

		this.dataFolder = dataFolder;
		// this.config = config;
		this.server = server;

		if (player != null)
			bPlayer = BendingPlayer.getBendingPlayer(player);

		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].toLowerCase();
		}

		if (args.length >= 1) {
			if (args[args.length - 1].equalsIgnoreCase("&")) {
				verbose = false;

				String[] temp = new String[args.length - 1];
				for (int i = 0; i < args.length - 1; i++) {
					temp[i] = args[i];
				}
				args = temp;
				// String arglist = "";
				// for (String arg : args)
				// arglist = arglist + arg + " ";
				// Tools.verbose(arglist);
			}

			String arg = args[0];

			// if (arg.equalsIgnoreCase("test")) {
			// try {
			// Tools.verbose("Sleeping for 5000ms");
			// Thread.sleep(5000);
			// Tools.verbose("Done sleeping.");
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }

			if (Arrays.asList(bindAliases).contains(arg)) {
				bind(player, args);
			} else if (Arrays.asList(clearAliases).contains(arg)) {
				clear(player, args);
			} else if (Arrays.asList(chooseAliases).contains(arg)) {
				choose(player, args);
			} else if (Arrays.asList(addAliases).contains(arg)) {
				add(player, args);
			} else if (Arrays.asList(removeAliases).contains(arg)) {
				remove(player, args);
			} else if (Arrays.asList(permaremoveAliases).contains(arg)) {
				permaremove(player, args);
			} else if (Arrays.asList(toggleAliases).contains(arg)) {
				toggle(player, args);
			} else if (Arrays.asList(displayAliases).contains(arg)) {
				display(player, args);
			} else if (Arrays.asList(reloadAliases).contains(arg)) {
				reload(player, args);
			} else if (Arrays.asList(helpAliases).contains(arg)) {
				help(player, args);
			} else if (Arrays.asList(importAliases).contains(arg)) {
				importBending(player, args);
			} else if (Arrays.asList(whoAliases).contains(arg)) {
				who(player, args);
			} else if (Arrays.asList(languageAliases).contains(arg)) {
				language(player, args);
			} else if (Arrays.asList(bindModeAliases).contains(arg)) {
				bindMode(player, args);
			} else if (Arrays.asList(versionAliases).contains(arg)) {
				version(player, args);
			} else {
				printHelpDialogue(player);
			}
		} else {

			printHelpDialogue(player);

		}

	}

	private void version(Player player, String[] args) {
		if (!hasHelpPermission(player, "bending.command.version")) {
			sendNoCommandPermissionMessage(player, "version");
			return;
		}

		sendMessage(player, "Bending v"
				+ Bending.plugin.getDescription().getVersion());
		sendMessage(player, "Author: orion304");
	}

	private void printVersionUsage(Player player) {
		if (!hasHelpPermission(player, "bending.command.version")) {
			sendNoCommandPermissionMessage(player, "version");
		}

		printUsageMessage(player, "/bending version", "General.version_usage");
	}

	private void bindMode(Player player, String[] args) {
		if (!hasHelpPermission(player, "bending.command.bindmode")) {
			sendNoCommandPermissionMessage(player, "bindmode");
			return;
		}

		boolean bindmode = ConfigManager.bendToItem;
		String mode;

		if (args.length > 2) {
			printBindModeUsage(player);
			return;
		} else if (args.length == 1) {
			if (player == null) {
				if (bindmode) {
					mode = "item";
				} else {
					mode = "slot";
				}
				sendMessage(player,
						Tools.getMessage(player, "General.server_bind_mode")
								+ "" + mode);
			} else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
				bindmode = bPlayer.getBendToItem();
				if (bindmode) {
					mode = "item";
				} else {
					mode = "slot";
				}
				sendMessage(player,
						Tools.getMessage(player, "General.your_bind_mode") + ""
								+ mode);
			}
			return;
		} else if (player == null) {
			printNotFromConsole();
			return;
		} else {
			bPlayer = BendingPlayer.getBendingPlayer(player);
			String choice = args[1];
			if (Arrays.asList(slotAliases).contains(choice)) {
				bPlayer.setBendToItem(false);
				sendMessage(player,
						Tools.getMessage(player, "General.bind_mode_change")
								+ "slot");
			} else if (Arrays.asList(itemAliases).contains(choice)) {
				bPlayer.setBendToItem(true);
				sendMessage(player,
						Tools.getMessage(player, "General.bind_mode_change")
								+ "item");
			} else {
				printBindModeUsage(player);
			}
			return;
		}
	}

	private void printBindModeUsage(Player player) {
		if (!hasHelpPermission(player, "bending.command.bindmode")) {
			sendNoCommandPermissionMessage(player, "language");
			return;
		}

		printUsageMessage(player, "/bending bindmode",
				"General.bind_mode_usage");

		if (player != null) {
			printUsageMessage(player, "/bending bindmode <slot/item>",
					"General.bind_mode_change_usage");
		}
	}

	private void language(Player player, String[] args) {
		if (!hasHelpPermission(player, "bending.command.language")) {
			sendNoCommandPermissionMessage(player, "language");
			return;
		}

		if (args.length > 2) {
			printLanguageUsage(player);
			return;
		} else if (args.length == 2) {
			String language = args[1];
			if (Tools.isLanguageSupported(language)) {
				// config.setLanguage(player, language);
				// PlayerStorageWriter.setLanguage(player, language);
				BendingPlayer.getBendingPlayer(player).setLanguage(language);
				Tools.sendMessage(player, "General.language_success");
			} else {
				Tools.sendMessage(player, "General.language_not_supported");
			}
		} else {
			sendMessage(player,
					Tools.getMessage(player, "General.your_language") + " "
							+ Tools.getLanguage(player));
			sendMessage(player,
					Tools.getMessage(player, "General.default_language") + " "
							+ Tools.getDefaultLanguage());
			sendMessage(player,
					Tools.getMessage(player, "General.supported_languages")
							+ " " + Tools.getSupportedLanguages());
		}
	}

	private void printLanguageUsage(Player player) {
		if (!hasHelpPermission(player, "bending.command.language")) {
			sendNoCommandPermissionMessage(player, "language");
			return;
		}
		printUsageMessage(player, "/bending language", "General.language_usage");
		printUsageMessage(player, "/bending language <language>",
				"General.language_change_usage");
	}

	private void printWhoUsage(Player player) {
		if (!hasHelpPermission(player, "bending.command.who")) {
			sendNoCommandPermissionMessage(player, "who");
			return;
		}
		printUsageMessage(player, "/bending who", "General.who_usage");
		printUsageMessage(player, "/bending who <player>",
				"General.who_player_usage");
	}

	private void who(Player player, String[] args) {
		if (!hasPermission(player, "bending.command.who"))
			return;

		if (args.length > 2) {
			printWhoUsage(player);
			return;
		}

		if (args.length == 1) {
			for (Player p : server.getOnlinePlayers()) {
				if (player != null) {
					if (!player.canSee(p))
						continue;
				}
				ChatColor color = ChatColor.WHITE;
				if (Tools.isBender(p.getName(), BendingType.Air))
					color = Tools.getColor(ConfigManager.getColor("Air"));
				if (Tools.isBender(p.getName(), BendingType.Water))
					color = Tools.getColor(ConfigManager.getColor("Water"));
				if (Tools.isBender(p.getName(), BendingType.Fire))
					color = Tools.getColor(ConfigManager.getColor("Fire"));
				if (Tools.isBender(p.getName(), BendingType.Earth))
					color = Tools.getColor(ConfigManager.getColor("Earth"));
				if (Tools.isBender(p.getName(), BendingType.ChiBlocker))
					color = Tools
							.getColor(ConfigManager.getColor("ChiBlocker"));
				sendMessage(player, color + p.getName());
			}
		} else if (args.length == 2) {
			Player p = server.getPlayer(args[1]);
			if (p == null) {
				sendMessage(
						player,
						args[1]
								+ " "
								+ Tools.getMessage(player,
										"General.who_not_on_server"));
			} else if (player != null) {
				if (!player.canSee(p)) {
					sendMessage(
							player,
							args[1]
									+ " "
									+ Tools.getMessage(player,
											"General.who_not_on_server"));
				} else {
					sendMessage(player, p.getDisplayName());
					if (!Tools.isBender(p.getName())) {
						sendMessage(player, "-No bending");
					} else {
						if (Tools.isBender(p.getName(), BendingType.Air))
							sendMessage(
									player,
									Tools.getColor(ConfigManager
											.getColor("Air")) + "-Airbending");
						if (Tools.isBender(p.getName(), BendingType.Water))
							sendMessage(
									player,
									Tools.getColor(ConfigManager
											.getColor("Water"))
											+ "-Waterbending");
						if (Tools.isBender(p.getName(), BendingType.Fire))
							sendMessage(
									player,
									Tools.getColor(ConfigManager
											.getColor("Fire")) + "-Firebending");
						if (Tools.isBender(p.getName(), BendingType.Earth))
							sendMessage(
									player,
									Tools.getColor(ConfigManager
											.getColor("Earth"))
											+ "-Earthbending");
						if (Tools.isBender(p.getName(), BendingType.ChiBlocker))
							sendMessage(
									player,
									Tools.getColor(ConfigManager
											.getColor("ChiBlocker"))
											+ "-Chiblocking");
					}
				}
			} else {
				sendMessage(player, p.getDisplayName());
				if (!Tools.isBender(p.getName())) {
					sendMessage(player, "-No bending");
				} else {
					if (Tools.isBender(p.getName(), BendingType.Air))
						sendMessage(player,
								Tools.getColor(ConfigManager.getColor("Air"))
										+ "-Airbending");
					if (Tools.isBender(p.getName(), BendingType.Water))
						sendMessage(player,
								Tools.getColor(ConfigManager.getColor("Water"))
										+ "-Waterbending");
					if (Tools.isBender(p.getName(), BendingType.Fire))
						sendMessage(player,
								Tools.getColor(ConfigManager.getColor("Fire"))
										+ "-Firebending");
					if (Tools.isBender(p.getName(), BendingType.Earth))
						sendMessage(player,
								Tools.getColor(ConfigManager.getColor("Earth"))
										+ "-Earthbending");
					if (Tools.isBender(p.getName(), BendingType.ChiBlocker))
						sendMessage(
								player,
								Tools.getColor(ConfigManager
										.getColor("ChiBlocker"))
										+ "-Chiblocking");
				}
			}
		} else {
			printWhoUsage(player);
		}
	}

	private void printUsageMessage(Player player, String command, String key) {
		ChatColor color = ChatColor.AQUA;
		String usage = Tools.getMessage(player, "General.usage");
		String description = Tools.getMessage(player, key);
		sendMessage(player, color + usage + ": " + command);
		sendMessage(player, color + "-" + description);
	}

	private void printChooseUsage(Player player) {
		if (!hasHelpPermission(player, "bending.admin.choose")
				&& !hasHelpPermission(player, "bending.admin.rechoose")
				&& !hasHelpPermission(player, "bending.command.choose")) {
			sendNoCommandPermissionMessage(player, "choose");
			return;
		}
		if (hasHelpPermission(player, "bending.command.choose")
				|| hasHelpPermission(player, "bending.admin.rechoose")) {
			printUsageMessage(player, "/bending choose <element>",
					"General.choose_usage");
		}
		if (hasHelpPermission(player, "bending.admin.choose")) {
			printUsageMessage(player, "/bending choose <player> <element>",
					"General.choose_player_usage");
		}
	}

	private void choose(Player player, String[] args) {
		if (args.length != 2 && args.length != 3) {
			printChooseUsage(player);

			if (!player.hasPermission("bending.command.choose")
					&& !player.hasPermission("bending.admin.rechoose")
					&& !player.hasPermission("bending.admin.choose")) {
				printNoPermissions(player);
				return;
			}
			return;
		}
		if (args.length == 2) {
			if (player == null) {
				printChooseUsage(player);
				return;
			}
			if (!player.hasPermission("bending.command.choose")
					&& !player.hasPermission("bending.admin.rechoose")
					&& !player.hasPermission("bending.admin.choose")) {
				printNoPermissions(player);
				return;
			}
			if (Tools.isBender(player.getName())
					&& !player.hasPermission("bending.admin.rechoose")) {
				printNoPermissions(player);
				return;
			}
			String choice = args[1].toLowerCase();
			if (Arrays.asList(airbendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.air")) {
					Tools.sendMessage(player, "General.no_perms_air");
					return;
				}
				Tools.sendMessage(player, "General.choosen_air");
				// config.removeBending(player);
				// config.setBending(player, "air");
				// PlayerStorageWriter.removeBending(player);
				// PlayerStorageWriter.setBending(player, BendingType.Air);
				BendingPlayer.getBendingPlayer(player).setBender(
						BendingType.Air);
				return;
			}
			if (Arrays.asList(firebendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.fire")) {
					Tools.sendMessage(player, "General.no_perms_fire");
					return;
				}
				Tools.sendMessage(player, "General.choosen_fire");
				// config.removeBending(player);
				// config.setBending(player, "fire");
				// PlayerStorageWriter.removeBending(player);
				// PlayerStorageWriter.setBending(player, BendingType.Fire);
				BendingPlayer.getBendingPlayer(player).setBender(
						BendingType.Fire);
				return;
			}
			if (Arrays.asList(earthbendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.earth")) {
					Tools.sendMessage(player, "General.no_perms_earth");
					return;
				}
				Tools.sendMessage(player, "General.choosen_earth");
				// config.removeBending(player);
				// config.setBending(player, "earth");
				// PlayerStorageWriter.removeBending(player);
				// PlayerStorageWriter.setBending(player, BendingType.Earth);
				BendingPlayer.getBendingPlayer(player).setBender(
						BendingType.Earth);
				return;
			}
			if (Arrays.asList(waterbendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.water")) {
					Tools.sendMessage(player, "General.no_perms_water");
					return;
				}
				Tools.sendMessage(player, "General.choosen_water");
				// config.removeBending(player);
				// config.setBending(player, "water");
				// PlayerStorageWriter.removeBending(player);
				// PlayerStorageWriter.setBending(player, BendingType.Water);
				BendingPlayer.getBendingPlayer(player).setBender(
						BendingType.Water);
				return;
			}
			if (Arrays.asList(chiblockingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.chiblocking")) {
					Tools.sendMessage(player, "General.no_perms_chiblocking");
					return;
				}
				Tools.sendMessage(player, "General.choosen_chi");
				// config.removeBending(player);
				// config.setBending(player, "chiblocker");
				// PlayerStorageWriter.removeBending(player);
				// PlayerStorageWriter.setBending(player,
				// BendingType.ChiBlocker);
				BendingPlayer.getBendingPlayer(player).setBender(
						BendingType.ChiBlocker);
				return;
			}
			printChooseUsage(player);
		} else if (args.length == 3) {
			if (!hasPermission(player, "bending.admin.choose"))
				return;
			String playername = args[1];
			Player targetplayer = server.getPlayer(playername);
			if (targetplayer == null) {
				printChooseUsage(player);
				return;
			}

			String senderName = Tools.getMessage(targetplayer,
					"General.the_server");
			if (player != null)
				senderName = player.getName();

			String choice = args[2].toLowerCase();
			if (Arrays.asList(airbendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.air")) {
					Tools.sendMessage(player, "General.other_no_perms_air");
					return;
				}
				sendMessage(player,
						Tools.getMessage(player, "General.you_changed") + " "
								+ targetplayer.getName() + "'s bending.");
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.changed_you"));
				Tools.sendMessage(targetplayer, "General.choosen_air");
				// config.removeBending(targetplayer);
				// config.setBending(targetplayer, "air");
				// PlayerStorageWriter.removeBending(targetplayer);
				// PlayerStorageWriter.setBending(targetplayer,
				// BendingType.Air);
				BendingPlayer.getBendingPlayer(targetplayer).setBender(
						BendingType.Air);
				return;
			}
			if (Arrays.asList(firebendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.fire")) {
					Tools.sendMessage(player, "General.other_no_perms_fire");
					return;
				}
				sendMessage(player,
						Tools.getMessage(player, "General.you_changed") + " "
								+ targetplayer.getName() + "'s bending.");
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.changed_you"));
				Tools.sendMessage(targetplayer, "General.choosen_fire");
				// config.removeBending(targetplayer);
				// config.setBending(targetplayer, "fire");
				// PlayerStorageWriter.removeBending(targetplayer);
				// PlayerStorageWriter.setBending(targetplayer,
				// BendingType.Fire);
				BendingPlayer.getBendingPlayer(targetplayer).setBender(
						BendingType.Fire);
				return;
			}
			if (Arrays.asList(earthbendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.earth")) {
					Tools.sendMessage(player, "General.other_no_perms_earth");
					return;
				}
				sendMessage(player,
						Tools.getMessage(player, "General.you_changed") + " "
								+ targetplayer.getName() + "'s bending.");
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.changed_you"));
				Tools.sendMessage(targetplayer, "General.choosen_earth");
				// config.removeBending(targetplayer);
				// config.setBending(targetplayer, "earth");
				// PlayerStorageWriter.removeBending(targetplayer);
				// PlayerStorageWriter.setBending(targetplayer,
				// BendingType.Earth);
				BendingPlayer.getBendingPlayer(targetplayer).setBender(
						BendingType.Earth);
				return;
			}
			if (Arrays.asList(waterbendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.water")) {
					Tools.sendMessage(player, "General.other_no_perms_water");
					return;
				}
				sendMessage(player,
						Tools.getMessage(player, "General.you_changed") + " "
								+ targetplayer.getName() + "'s bending.");
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.changed_you"));
				Tools.sendMessage(targetplayer, "General.choosen_water");
				// config.removeBending(targetplayer);
				// config.setBending(targetplayer, "water");
				// PlayerStorageWriter.removeBending(targetplayer);
				// PlayerStorageWriter.setBending(targetplayer,
				// BendingType.Water);
				BendingPlayer.getBendingPlayer(targetplayer).setBender(
						BendingType.Water);
				return;
			}
			if (Arrays.asList(chiblockingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.chiblocking")) {
					sendMessage(player, "General.other_no_perms_chi");
					return;
				}
				sendMessage(player,
						Tools.getMessage(player, "General.you_changed") + " "
								+ targetplayer.getName() + "'s bending.");
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.changed_you"));
				Tools.sendMessage(targetplayer, "General.choosen_chi");
				// config.removeBending(targetplayer);
				// config.setBending(targetplayer, "chiblocker");
				// PlayerStorageWriter.removeBending(targetplayer);
				// PlayerStorageWriter.setBending(targetplayer,
				// BendingType.ChiBlocker);
				BendingPlayer.getBendingPlayer(targetplayer).setBender(
						BendingType.ChiBlocker);
				return;
			}
			printChooseUsage(player);
		}
	}

	private void sendMessage(Player player, String message) {
		if (!verbose)
			return;
		if (player == null) {
			Bending.log.info(message);
		} else {
			player.sendMessage(message);
		}
	}

	private void printImportUsage(Player player) {
		if (!hasHelpPermission(player, "bending.admin.import")) {
			sendNoCommandPermissionMessage(player, "import");
			return;
		}
		printUsageMessage(player, "/bending import", "General.import_usage");

	}

	private void importBending(Player player, String[] args) {
		if (!hasPermission(player, "bending.admin.import"))
			return;

		if (StorageManager.useFlatFile) {
			sendMessage(
					player,
					ChatColor.AQUA
							+ Tools.getMessage(player, "General.import_noSQL"));
			return;
		}
		BendingPlayers temp = new BendingPlayers(dataFolder);
		Set<String> keys = temp.getKeys();

		for (String s : keys) {
			if (s.contains("<")) {
				// String[] getplayername = s.split("<");
				// String playername = getplayername[0];
				String[] getSetter = s.split("<");
				String Setter = getSetter[1];
				OfflinePlayer oPlayer = server.getOfflinePlayer(getSetter[0]);
				String binded = Setter.replace("Bind", "").replace(">", "");
				Tools.verbose(getSetter[0] + ": " + binded);
				String ability = temp.getKey(s);
				if ((binded.equalsIgnoreCase("0")
						|| binded.equalsIgnoreCase("1")
						|| binded.equalsIgnoreCase("2")
						|| binded.equalsIgnoreCase("3")
						|| binded.equalsIgnoreCase("4")
						|| binded.equalsIgnoreCase("5")
						|| binded.equalsIgnoreCase("6")
						|| binded.equalsIgnoreCase("7") || binded
							.equalsIgnoreCase("8"))) {
					int slot = Integer.parseInt(binded);
					// config.setAbility(playername, ability, slot);
					// PlayerStorageWriter.bindSlot(oPlayer, slot,
					// Abilities.getAbility(ability));
					BendingPlayer.getBendingPlayer(oPlayer).setAbility(slot,
							Abilities.getAbility(ability));
				} else if (binded.equalsIgnoreCase("Language")) {
					// PlayerStorageWriter.setLanguage(oPlayer, ability);
					BendingPlayer.getBendingPlayer(oPlayer)
							.setLanguage(ability);
				} else {
					// config.setAbility(playername, ability,
					// Material.matchMaterial(binded));
					// PlayerStorageWriter.bindItem(oPlayer,
					// Material.matchMaterial(binded),
					// Abilities.getAbility(ability));
					BendingPlayer.getBendingPlayer(oPlayer).setAbility(
							Material.matchMaterial(binded),
							Abilities.getAbility(binded));
				}
			} else {
				// String playerName = s;
				String bending = temp.getKey(s);
				OfflinePlayer oPlayer = server.getOfflinePlayer(s);
				if (bending.contains("a"))
					// config.addBending(playerName, BendingType.Air);
					// PlayerStorageWriter.addBending(oPlayer, BendingType.Air);
					BendingPlayer.getBendingPlayer(oPlayer).addBender(
							BendingType.Air);
				if (bending.contains("w"))
					// config.addBending(playerName, BendingType.Water);
					// PlayerStorageWriter.addBending(oPlayer,
					// BendingType.Water);
					BendingPlayer.getBendingPlayer(oPlayer).addBender(
							BendingType.Water);
				if (bending.contains("f"))
					// config.addBending(playerName, BendingType.Fire);
					// PlayerStorageWriter.addBending(oPlayer,
					// BendingType.Fire);
					BendingPlayer.getBendingPlayer(oPlayer).addBender(
							BendingType.Fire);
				if (bending.contains("e"))
					// config.addBending(playerName, BendingType.Earth);
					// PlayerStorageWriter.addBending(oPlayer,
					// BendingType.Earth);
					BendingPlayer.getBendingPlayer(oPlayer).addBender(
							BendingType.Earth);
				if (bending.contains("c"))
					// config.addBending(playerName, BendingType.ChiBlocker);
					// PlayerStorageWriter.addBending(oPlayer,
					// BendingType.ChiBlocker);
					BendingPlayer.getBendingPlayer(oPlayer).addBender(
							BendingType.ChiBlocker);

			}

		}
		temp = null;
		sendMessage(
				player,
				ChatColor.AQUA
						+ Tools.getMessage(player, "General.import_success"));

	}

	private void printNoPermissions(Player player) {
		sendMessage(
				player,
				ChatColor.RED
						+ Tools.getMessage(player, "General.no_execute_perms"));

	}

	private void help(Player player, String[] args) {
		// int pages = 0;
		// int page = 1;
		List<String> command = new ArrayList<String>();
		for (String s : Bending.commands.keySet()) {
			if (hasHelpPermission(player, "bending." + s)) {
				command.add(Bending.commands.get(s));
			}
		}
		if (args.length > 1) {
			helpCommand(player, args);
			Abilities ability = Abilities.getAbility(args[1]);
			if (Abilities.getAbility(args[1]) != null) {
				ChatColor cc = ChatColor.GOLD;
				String element = "Other";
				if (Abilities.isAirbending(Abilities.getAbility(args[1]))) {
					cc = Tools.getColor(ConfigManager.color.get("Air"));
					element = "Air";
				}
				if (Abilities.isFirebending(Abilities.getAbility(args[1]))) {
					cc = Tools.getColor(ConfigManager.color.get("Fire"));
					element = "Fire";
				}
				if (Abilities.isEarthbending(Abilities.getAbility(args[1]))) {
					cc = Tools.getColor(ConfigManager.color.get("Earth"));
					element = "Earth";
				}
				if (Abilities.isWaterbending(Abilities.getAbility(args[1]))) {
					cc = Tools.getColor(ConfigManager.color.get("Water"));
					element = "Water";
				}
				if (Abilities.isChiBlocking(Abilities.getAbility(args[1]))) {
					cc = Tools.getColor(ConfigManager.color.get("ChiBlocker"));
					element = "Chiblocker";
				}
				if (Tools.hasPermission(player, Abilities.getAbility(args[1]))) {

					sendMessage(
							player,
							("                                                "
									+ cc + Abilities.getAbility(args[1]).name()));

					switch (Abilities.getAbility(args[1])) {
					case AirBlast:
						Tools.sendMessage(player, cc, element + ".AirBlast");
						break;
					case AirBubble:
						Tools.sendMessage(player, cc, element + ".AirBubble");
						break;
					case AirShield:
						Tools.sendMessage(player, cc, element + ".AirShield");
						break;
					case AirSuction:
						Tools.sendMessage(player, cc, element + ".AirSuction");
						break;
					case AirSwipe:
						Tools.sendMessage(player, cc, element + ".AirSwipe");
						break;
					case AirBurst:
						Tools.sendMessage(player, cc, element + ".AirBurst");
						break;
					case Tornado:
						Tools.sendMessage(player, cc, element + ".Tornado");
						break;
					case AirScooter:
						Tools.sendMessage(player, cc, element + ".AirScooter");
						break;
					case Shockwave:
						Tools.sendMessage(player, cc, element + ".Shockwave");
						break;
					case Catapult:
						Tools.sendMessage(player, cc, element + ".Catapult");
						break;
					case RaiseEarth:
						Tools.sendMessage(player, cc, element + ".RaiseEarth");
						break;
					case EarthGrab:
						Tools.sendMessage(player, cc, element + ".EarthGrab");
						break;
					case EarthTunnel:
						Tools.sendMessage(player, cc, element + ".EarthTunnel");
						break;
					case EarthBlast:
						Tools.sendMessage(player, cc, element + ".EarthBlast");
						break;
					case Collapse:
						Tools.sendMessage(player, cc, element + ".Collapse");
						break;
					case Tremorsense:
						Tools.sendMessage(player, cc, element + ".Tremorsense");
						break;
					case Blaze:
						Tools.sendMessage(player, cc, element + ".Blaze");
						break;
					case HeatControl:
						Tools.sendMessage(player, cc, element + ".HeatControl");
						break;
					case FireBlast:
						Tools.sendMessage(player, cc, element + ".FireBlast");
						break;
					case FireJet:
						Tools.sendMessage(player, cc, element + ".FireJet");
						break;
					case FireBurst:
						Tools.sendMessage(player, cc, element + ".FireBurst");
						break;
					case Illumination:
						Tools.sendMessage(player, cc, element + ".Illumination");
						break;
					case Lightning:
						Tools.sendMessage(player, cc, element + ".Lightning");
						break;
					case FireShield:
						Tools.sendMessage(player, cc, element + ".FireShield");
						break;
					case WallOfFire:
						Tools.sendMessage(player, cc, element + ".WallOfFire");
						break;
					case Bloodbending:
						Tools.sendMessage(player, cc, element + ".Bloodbending");
						break;
					case WaterBubble:
						Tools.sendMessage(player, cc, element + ".WaterBubble");
						break;
					case PhaseChange:
						Tools.sendMessage(player, cc, element + ".PhaseChange");
						break;
					case HealingWaters:
						Tools.sendMessage(player, cc, element
								+ ".HealingWaters");
						break;
					case WaterManipulation:
						Tools.sendMessage(player, cc, element
								+ ".WaterManipulation");
						break;
					case WaterSpout:
						Tools.sendMessage(player, cc, element + ".WaterSpout");
						break;
					case OctopusForm:
						Tools.sendMessage(player, cc, element + ".OctopusForm");
						break;
					case Surge:
						Tools.sendMessage(player, cc, element + ".Surge");
						break;
					case Torrent:
						Tools.sendMessage(player, cc, element + ".Torrent");
						break;
					case AvatarState:
						Tools.sendMessage(player, cc, element + ".AvatarState");
						break;
					case EarthArmor:
						Tools.sendMessage(player, cc, element + ".EarthArmor");
						break;
					case RapidPunch:
						Tools.sendMessage(player, cc, element + ".RapidPunch");
						break;
					case HighJump:
						Tools.sendMessage(player, cc, element + ".HighJump");
						break;
					case IceSpike:
						Tools.sendMessage(player, cc, element + ".IceSpike");
						break;
					case AirSpout:
						Tools.sendMessage(player, cc, element + ".AirSpout");
						break;
					case Paralyze:
						Tools.sendMessage(player, cc, element + ".Paralyze");
						break;
					}
					return;
				} else {
					sendMessage(player,
							Tools.getMessage(player, "General.no_bind_perms")
									+ " " + cc + ability + ChatColor.WHITE
									+ ".");
				}
			}
			// for (String s : command) {
			//
			// if (args[1].equalsIgnoreCase(s.split(" ")[0])) {
			// sendMessage(
			// player,
			// ChatColor.AQUA
			// + "                                        /bending "
			// + (s.split(" ")[0]));
			// String msg = "";
			// if ((s.split(" ")[0]).equalsIgnoreCase("choose")
			// && hasHelpPermission(player,
			// "bending.command.choose")) {
			// msg =
			// "The command /bending choose <element> let you choose one of the four elements (air, fire, water, air) each having different abilities. ";
			// if (hasHelpPermission(player, "bending.admin.choose"))
			// msg = msg
			// +
			// "The command can also be used to set other people bending element like so /bending choose <player> <element>";
			// sendMessage(player, msg);
			// } else if ((s.split(" ")[0]).equalsIgnoreCase("bind")
			// && hasHelpPermission(player, "bending.command.bind")) {
			// String append = "a slot";
			// if (ConfigManager.bendToItem) {
			// append = "an item";
			// }
			// msg =
			// "The command /bending <bind> <ability> is used to bind an ability to "
			// + append + ".";
			// sendMessage(player, msg);
			// } else if ((s.split(" ")[0].toLowerCase())
			// .equalsIgnoreCase("help")) {
			//
			// } else if ((s.split(" ")[0].toLowerCase())
			// .equalsIgnoreCase("remove")
			// && hasHelpPermission(player, "bending.admin.remove")) {
			// sendMessage(
			// player,
			// "The command /bending remove <player> will remove the player bending. It can also be used to lift the permaremove of a player.");
			// } else if ((s.split(" ")[0]).equalsIgnoreCase("reload")
			// && hasHelpPermission(player, "bending.admin.reload")) {
			// player.sendMessage("The command /bending reload will allow you to reload the configuration, so you can make change while the server is running and don't have to restart/reload it.");
			// } else if ((s.split(" ")[0])
			// .equalsIgnoreCase("permaremove")
			// && hasHelpPermission(player,
			// "bending.admin.permaremove")) {
			// sendMessage(
			// player,
			// "The command /bending permaremove <player> will permanantly remove someone's bending and won't allow him to choose once again until you do /bending remove <player> or manually set his bending");
			// } else if ((s.split(" ")[0]).equalsIgnoreCase("add")
			// && hasHelpPermission(player, "bending.admin.add")) {
			// sendMessage(
			// player,
			// "The command /bending add <element> allow you to add elements to the one you aleredy have. It can be used to stack all for eleents at once.");
			// } else if ((s.split(" ")[0]).equalsIgnoreCase("display")
			// && hasHelpPermission(player,
			// "bending.command.display")) {
			// msg =
			// ("The command /bending display allows you to see a list of your binding so you can remember where you binded what.");
			// if (hasHelpPermission(player,
			// "bending.command.displayelement")) {
			// msg = msg
			// +
			// " The command /bending display <element> can allow you to see a list of abilities an bending element has.";
			// }
			// sendMessage(player, msg);
			// }
			//
			// return;
			// }
			// }

			// try {
			// page = Integer.parseInt(args[1]);
			// } catch (NumberFormatException e) {
			// page = -1;
			// }
		} else {
			printCommands(player);
		}
		// if (page != -1) {
		// pages = command.size() / 8;
		// if (command.size() % 8 != 0)
		// pages++;
		//
		// if (page > pages) {
		// if (pages > 1) {
		// sendMessage(player, ChatColor.RED + "There's only " + pages
		// + " pages of help");
		// } else {
		// sendMessage(player, ChatColor.RED + "There's only " + pages
		// + " page of help");
		// }
		// return;
		// }
		// sendMessage(player, ChatColor.AQUA
		// + "=======================Help===========================");
		// for (int i = 1; i <= 8; i++) {
		// if (command.size() > (i + (8 * page) - 9)) {
		// String comname = "/bending "
		// + command.get((i + (8 * page) - 9));
		// sendMessage(player, comname);
		// }
		// }
		// sendMessage(player, ChatColor.AQUA + "=====================Page "
		// + page + "/" + pages + "========================");
		// return;
		// }

	}

	private void helpCommand(Player player, String[] args) {
		ChatColor color = ChatColor.AQUA;

		String command = args[1];
		if (Arrays.asList(bindAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.bind")) {
				sendNoCommandPermissionMessage(player, "bind");
				return;
			}
			sendMessage(player, color + "Command: /bending bind");
			String aliases = "";
			for (String alias : bindAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printBindUsage(player);
		} else if (Arrays.asList(clearAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.clear")) {
				sendNoCommandPermissionMessage(player, "clear");
				return;
			}
			sendMessage(player, color + "Command: /bending clear");
			String aliases = "";
			for (String alias : clearAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printClearUsage(player);
		} else if (Arrays.asList(chooseAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.choose")
					&& !hasHelpPermission(player, "bending.admin.choose")
					&& !hasHelpPermission(player, "bending.admin.rechoose")) {
				sendNoCommandPermissionMessage(player, "choose");
				return;
			}
			sendMessage(player, color + "Command: /bending choose");
			String aliases = "";
			for (String alias : chooseAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printChooseUsage(player);
		} else if (Arrays.asList(addAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.add")) {
				sendNoCommandPermissionMessage(player, "add");
				return;
			}
			sendMessage(player, color + "Command: /bending add");
			String aliases = "";
			for (String alias : addAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printAddUsage(player);
		} else if (Arrays.asList(removeAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.remove")) {
				sendNoCommandPermissionMessage(player, "remove");
				return;
			}
			sendMessage(player, color + "Command: /bending remove");
			String aliases = "";
			for (String alias : removeAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printRemoveUsage(player);
		} else if (Arrays.asList(permaremoveAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.permaremove")) {
				sendNoCommandPermissionMessage(player, "permaremove");
				return;
			}
			sendMessage(player, color + "Command: /bending permaremove");
			String aliases = "";
			for (String alias : permaremoveAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printPermaremoveUsage(player);
		} else if (Arrays.asList(toggleAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.toggle")) {
				sendNoCommandPermissionMessage(player, "toggle");
				return;
			}
			sendMessage(player, color + "Command: /bending toggle");
			String aliases = "";
			for (String alias : toggleAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printToggleUsage(player);
		} else if (Arrays.asList(displayAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.display")) {
				sendNoCommandPermissionMessage(player, "display");
				return;
			}
			sendMessage(player, color + "Command: /bending display");
			String aliases = "";
			for (String alias : displayAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printDisplayUsage(player);
		} else if (Arrays.asList(reloadAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.reload")) {
				sendNoCommandPermissionMessage(player, "reload");
				return;
			}
			sendMessage(player, color + "Command: /bending reload");
			String aliases = "";
			for (String alias : reloadAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printReloadUsage(player);
		} else if (Arrays.asList(importAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.import")) {
				sendNoCommandPermissionMessage(player, "import");
				return;
			}
			sendMessage(player, color + "Command: /bending import");
			String aliases = "";
			for (String alias : importAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printImportUsage(player);
		} else if (Arrays.asList(whoAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.who")) {
				sendNoCommandPermissionMessage(player, "who");
				return;
			}
			sendMessage(player, color + "Command: /bending who");
			String aliases = "";
			for (String alias : whoAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printWhoUsage(player);
		} else if (Arrays.asList(languageAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.language")) {
				sendNoCommandPermissionMessage(player, "language");
				return;
			}
			sendMessage(player, color + "Command: /bending language");
			String aliases = "";
			for (String alias : languageAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printLanguageUsage(player);
		} else if (Arrays.asList(versionAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.version")) {
				sendNoCommandPermissionMessage(player, "version");
				return;
			}
			sendMessage(player, color + "Command: /bending version");
			String aliases = "";
			for (String alias : versionAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printVersionUsage(player);
		} else if (Arrays.asList(bindModeAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.bindmode")) {
				sendNoCommandPermissionMessage(player, "bindmode");
				return;
			}
			sendMessage(player, color + "Command: /bending bindmode");
			String aliases = "";
			for (String alias : bindModeAliases)
				aliases = aliases + alias + " ";
			sendMessage(player, color + "Aliases: " + aliases);
			printBindModeUsage(player);
		}
	}

	private void printReloadUsage(Player player) {
		if (!hasHelpPermission(player, "bending.admin.reload")) {
			sendNoCommandPermissionMessage(player, "reload");
			return;
		} else {
			printUsageMessage(player, "/bending reload", "General.reload_usage");
		}
	}

	private void reload(Player player, String[] args) {
		if (!hasPermission(player, "bending.admin.reload"))
			return;
		Bending.configManager.load(new File(dataFolder, "config.yml"));
		Bending.language.load(new File(dataFolder, "language.yml"));
		// config.initialize(dataFolder);
		server.getScheduler().runTaskAsynchronously(Bending.plugin,
				new BendingPlayersSaver());
		// String append = StorageManager.useMySQL ? " Database "
		// : "Players file ";
		Tools.stopAllBending();
		sendMessage(
				player,
				ChatColor.AQUA + "Bending "
						+ Tools.getMessage(player, "General.reload_success"));

	}

	private void printDisplayUsage(Player player) {
		if (!hasHelpPermission(player, "bending.command.display")) {
			sendNoCommandPermissionMessage(player, "display");
			return;
		}
		if (player != null)
			printUsageMessage(player, "/bending display",
					"General.display_usage");
		printUsageMessage(player, "/bending display <element>",
				"General.display_element_usage");
	}

	private void display(Player player, String[] args) {
		if (!hasPermission(player, "bending.command.display"))
			return;

		if (args.length > 2) {
			printDisplayUsage(player);
		}

		if (args.length == 1) {
			if (player == null) {
				printNotFromConsole();
				return;
			}

			boolean none = true;
			boolean item = bPlayer.getBendToItem();

			if (!item) {
				for (int i = 0; i <= 8; i++) {
					// Abilities a = config.getAbility(player, i);
					Abilities a = bPlayer.getAbility(i);
					if (a != null) {
						none = false;
						ChatColor color = ChatColor.WHITE;
						if (Abilities.isAirbending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Air"));
						} else if (Abilities.isChiBlocking(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("ChiBlocker"));
						} else if (Abilities.isEarthbending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Earth"));
						} else if (Abilities.isFirebending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Fire"));
						} else if (Abilities.isWaterbending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Water"));
						}
						String ability = a.name();
						sendMessage(player,
								Tools.getMessage(player, "General.slot") + " "
										+ (i + 1) + ": " + color + ability);
					}
				}
			} else {

				for (Material mat : Material.values()) {
					// Abilities a = config.getAbility(player, mat);
					Abilities a = bPlayer.getAbility(mat);
					if (a != null) {
						none = false;
						ChatColor color = ChatColor.WHITE;
						if (Abilities.isAirbending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Air"));
						} else if (Abilities.isChiBlocking(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("ChiBlocker"));
						} else if (Abilities.isEarthbending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Earth"));
						} else if (Abilities.isFirebending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Fire"));
						} else if (Abilities.isWaterbending(a)) {
							color = Tools.getColor(ConfigManager
									.getColor("Water"));
						}
						String ability = a.name();
						sendMessage(player, mat.name().replaceAll("_", " ")
								+ ": " + color + ability);
					}
				}
			}
			if (none)
				Tools.sendMessage(player, "General.display_no_abilities");
		}

		if (args.length == 2) {
			String[] abilitylist = null;
			String choice = args[1].toLowerCase();
			ChatColor color = ChatColor.WHITE;
			if (Arrays.asList(airbendingAliases).contains(choice)) {
				abilitylist = airbendingabilities;
				color = Tools.getColor(ConfigManager.getColor("Air"));
			} else if (Arrays.asList(waterbendingAliases).contains(choice)) {
				abilitylist = waterbendingabilities;
				color = Tools.getColor(ConfigManager.getColor("Water"));
			} else if (Arrays.asList(earthbendingAliases).contains(choice)) {
				abilitylist = earthbendingabilities;
				color = Tools.getColor(ConfigManager.getColor("Earth"));
			} else if (Arrays.asList(firebendingAliases).contains(choice)) {
				abilitylist = firebendingabilities;
				color = Tools.getColor(ConfigManager.getColor("Fire"));
			} else if (Arrays.asList(chiblockingAliases).contains(choice)) {
				abilitylist = chiblockingabilities;
				color = Tools.getColor(ConfigManager.getColor("ChiBlocker"));
			}

			if (abilitylist != null) {
				for (String ability : abilitylist) {
					if (Tools.hasPermission(player,
							Abilities.getAbility(ability))) {
						sendMessage(player, color + ability);
					}
				}
				return;
			} else {
				printDisplayUsage(player);
			}

		}

	}

	private void printToggleUsage(Player player) {
		if (!hasHelpPermission(player, "bending.command.toggle")) {
			sendNoCommandPermissionMessage(player, "toggle");
			return;
		}
		printUsageMessage(player, "/bending toggle", "General.toggle_usage");
	}

	private void toggle(Player player, String[] args) {
		if (args.length == 1) {
			if (!hasHelpPermission(player, "bending.command.toggle")
					&& !hasHelpPermission(player, "bending.admin.toggle")) {
				printNoPermissions(player);
				return;
			}
			if (player == null) {
				printNotFromConsole();
				return;
			}
			if (!Tools.toggledBending.contains(player)) {
				Tools.toggledBending.add(player);
				Tools.sendMessage(player, ChatColor.AQUA, "General.toggle_off");
			} else {
				Tools.toggledBending.remove(player);
				Tools.sendMessage(player, ChatColor.AQUA, "General.toggle_on");
			}
		} else {
			if (!hasPermission(player, "bending.admin.toggle"))
				return;
			String playerlist = "";
			for (int i = 1; i < args.length; i++) {
				String name = args[i];
				Player targetplayer = server.getPlayer(name);
				String senderName = Tools.getMessage(targetplayer,
						"General.the_server");
				if (player != null)
					senderName = player.getName();
				if (targetplayer != null) {
					if (!Tools.toggledBending.contains(targetplayer)) {
						Tools.toggledBending.add(targetplayer);
						sendMessage(targetplayer, senderName + " "
								+ "General.admin_toggle_off");
					} else {
						Tools.toggledBending.remove(targetplayer);
						sendMessage(targetplayer, senderName + " "
								+ "General.admin_toggle_on");
					}

					playerlist = playerlist + " " + targetplayer.getName();
				}
			}
			sendMessage(player,
					Tools.getMessage(player, "General.admin_toggle") + " "
							+ playerlist);
		}

	}

	private void printNotFromConsole() {
		Tools.sendMessage(null, "General.not_from_console");

	}

	private void printPermaremoveUsage(Player player) {
		if (!hasHelpPermission(player, "bending.admin.permaremove")) {
			sendNoCommandPermissionMessage(player, "permaremove");
			return;
		}
		printUsageMessage(player,
				"/bending permaremove <player1> [player2] [player3] ...",
				"General.permaremove_message");
	}

	private void permaremove(Player player, String[] args) {
		if (!hasPermission(player, "bending.admin.permaremove"))
			return;
		String playerlist = "";
		for (int i = 1; i < args.length; i++) {
			String playername = args[i];
			String senderName = "The server";
			if (player != null)
				senderName = player.getName();
			Player targetplayer = server.getPlayer(playername);
			if (targetplayer != null) {
				// config.permaRemoveBending(targetplayer);
				// PlayerStorageWriter.permaRemoveBending(targetplayer);
				BendingPlayer.getBendingPlayer(targetplayer)
						.permaremoveBender();
				targetplayer.sendMessage(senderName
						+ " "
						+ Tools.getMessage(targetplayer,
								"General.permaremove_you"));
				playerlist = playerlist + targetplayer.getName() + " ";
			}
		}
		sendMessage(player, Tools.getMessage(player, "General.you_permaremove")
				+ " " + playerlist);
	}

	private void printRemoveUsage(Player player) {
		if (!hasHelpPermission(player, "bending.admin.remove")) {
			sendNoCommandPermissionMessage(player, "remove");
			return;
		}
		printUsageMessage(player, "/bending remove <player>",
				"General.remove_usage");
	}

	private void remove(Player player, String[] args) {
		if (!hasPermission(player, "bending.admin.remove"))
			return;
		String playerlist = "";
		for (int i = 1; i < args.length; i++) {
			String playername = args[i];
			Player targetplayer = server.getPlayer(playername);
			String senderName = Tools.getMessage(targetplayer,
					"General.the_server");
			if (player != null)
				senderName = player.getName();
			if (targetplayer != null) {
				// config.removeBending(targetplayer);
				// PlayerStorageWriter.removeBending(targetplayer);
				BendingPlayer.getBendingPlayer(targetplayer).removeBender();
				targetplayer.sendMessage(senderName + " "
						+ Tools.getMessage(targetplayer, "General.remove_you"));
				playerlist = playerlist + targetplayer.getName() + " ";
			}
		}
		sendMessage(player, Tools.getMessage(player, "General.you_remove")
				+ " " + playerlist);

	}

	private void printAddUsage(Player player) {
		if (player != null)
			printUsageMessage(player, "/bending add <element>",
					"General.add_self");
		printUsageMessage(player, "/bending add <player> <element>",
				"General.add_other");
	}

	private void add(Player player, String[] args) {
		if (!hasPermission(player, "bending.admin.add"))
			return;
		if (args.length != 2 && args.length != 3) {
			printAddUsage(player);
			return;
		}
		if (args.length == 2) {
			// printAddUsage(player);
			String choice = args[1].toLowerCase();
			if (Arrays.asList(airbendingAliases).contains(choice)) {
				if (Tools.isBender(player.getName(), BendingType.Air)) {
					Tools.sendMessage(player, "General.you_already_air");
					return;
				}
				if (!hasHelpPermission(player, "bending.air")) {
					Tools.sendMessage(player, "General.no_perms_air");
					return;
				}
				Tools.sendMessage(player, "General.add_air");
				// config.addBending(player, "air");
				// PlayerStorageWriter.addBending(player, BendingType.Air);
				BendingPlayer.getBendingPlayer(player).addBender(
						BendingType.Air);
				return;
			}
			if (Arrays.asList(firebendingAliases).contains(choice)) {
				if (Tools.isBender(player.getName(), BendingType.Fire)) {
					Tools.sendMessage(player, "General.you_already_fire");
					return;
				}
				if (!hasHelpPermission(player, "bending.fire")) {
					Tools.sendMessage(player, "General.no_perms_fire");
					return;
				}
				Tools.sendMessage(player, "General.add_fire");
				// config.addBending(player, "fire");
				// PlayerStorageWriter.addBending(player, BendingType.Fire);
				BendingPlayer.getBendingPlayer(player).addBender(
						BendingType.Fire);
				return;
			}
			if (Arrays.asList(earthbendingAliases).contains(choice)) {
				if (Tools.isBender(player.getName(), BendingType.Earth)) {
					Tools.sendMessage(player, "General.you_already_earth");
					return;
				}
				if (!hasHelpPermission(player, "bending.earth")) {
					Tools.sendMessage(player, "General.no_perms_earth");
					return;
				}
				Tools.sendMessage(player, "General.add_earth");
				// config.addBending(player, "earth");
				// PlayerStorageWriter.addBending(player, BendingType.Earth);
				BendingPlayer.getBendingPlayer(player).addBender(
						BendingType.Earth);
				return;
			}
			if (Arrays.asList(waterbendingAliases).contains(choice)) {
				if (Tools.isBender(player.getName(), BendingType.Water)) {
					Tools.sendMessage(player, "General.you_already_water");
					return;
				}
				if (!hasHelpPermission(player, "bending.water")) {
					Tools.sendMessage(player, "General.no_perms_water");
					return;
				}
				Tools.sendMessage(player, "General.add_water");
				// config.addBending(player, "water");
				// PlayerStorageWriter.addBending(player, BendingType.Water);
				BendingPlayer.getBendingPlayer(player).addBender(
						BendingType.Water);
				return;
			}
			if (Arrays.asList(chiblockingAliases).contains(choice)) {
				if (Tools.isBender(player.getName(), BendingType.ChiBlocker)) {
					Tools.sendMessage(player, "General.you_already_chi");
					return;
				}
				if (!hasHelpPermission(player, "bending.chiblocking")) {
					Tools.sendMessage(player, "General.no_perms_chi");
					return;
				}
				Tools.sendMessage(player, "General.add_chi");
				// config.addBending(player, "chiblocker");
				// PlayerStorageWriter.addBending(player,
				// BendingType.ChiBlocker);
				BendingPlayer.getBendingPlayer(player).addBender(
						BendingType.ChiBlocker);
				return;
			}
			printAddUsage(player);
		} else if (args.length == 3) {
			String playername = args[1];
			Player targetplayer = server.getPlayer(playername);
			if (targetplayer == null) {
				printAddUsage(player);
				return;
			}

			String senderName = Tools.getMessage(targetplayer,
					"General.the_server");
			if (player != null)
				senderName = player.getName();

			String choice = args[2].toLowerCase();
			if (Arrays.asList(airbendingAliases).contains(choice)) {
				if (Tools.isBender(targetplayer.getName(), BendingType.Air)) {
					sendMessage(
							player,
							targetplayer.getName()
									+ " "
									+ Tools.getMessage(player,
											"General.they_already_air"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.air")) {
					Tools.sendMessage(player, "General.no_perms_air");
					return;
				}
				sendMessage(
						player,
						targetplayer.getName()
								+ " "
								+ Tools.getMessage(player,
										"General.you_add_air"));
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.add_you_air"));
				// config.addBending(targetplayer, "air");
				// PlayerStorageWriter.addBending(targetplayer,
				// BendingType.Air);
				BendingPlayer.getBendingPlayer(targetplayer).addBender(
						BendingType.Air);
				return;
			}
			if (Arrays.asList(firebendingAliases).contains(choice)) {
				if (Tools.isBender(targetplayer.getName(), BendingType.Fire)) {
					sendMessage(
							player,
							targetplayer.getName()
									+ " "
									+ Tools.getMessage(player,
											"General.they_already_fire"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.fire")) {
					Tools.sendMessage(player, "General.no_perms_fire");
					return;
				}
				sendMessage(
						player,
						targetplayer.getName()
								+ " "
								+ Tools.getMessage(player,
										"General.you_add_fire"));
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.add_you_fire"));
				// config.addBending(targetplayer, "fire");
				// PlayerStorageWriter.addBending(targetplayer,
				// BendingType.Fire);
				BendingPlayer.getBendingPlayer(targetplayer).addBender(
						BendingType.Fire);
				return;
			}
			if (Arrays.asList(earthbendingAliases).contains(choice)) {
				if (Tools.isBender(targetplayer.getName(), BendingType.Earth)) {
					sendMessage(
							player,
							targetplayer.getName()
									+ " "
									+ Tools.getMessage(player,
											"General.they_already_earth"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.earth")) {
					Tools.sendMessage(player, "General.no_perms_earth");
					return;
				}
				sendMessage(
						player,
						targetplayer.getName()
								+ " "
								+ Tools.getMessage(player,
										"General.you_add_earth"));
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.add_you_earth"));
				// config.addBending(targetplayer, "earth");
				// PlayerStorageWriter.addBending(targetplayer,
				// BendingType.Earth);
				BendingPlayer.getBendingPlayer(targetplayer).addBender(
						BendingType.Earth);
				return;
			}
			if (Arrays.asList(waterbendingAliases).contains(choice)) {
				if (Tools.isBender(targetplayer.getName(), BendingType.Water)) {
					sendMessage(
							player,
							targetplayer.getName()
									+ " "
									+ Tools.getMessage(player,
											"General.they_already_water"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.water")) {
					Tools.sendMessage(player, "General.no_perms_water");
				}
				sendMessage(
						player,
						targetplayer.getName()
								+ " "
								+ Tools.getMessage(player,
										"General.you_add_water"));
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.add_you_water"));
				// config.addBending(targetplayer, "water");
				// PlayerStorageWriter.addBending(targetplayer,
				// BendingType.Water);
				BendingPlayer.getBendingPlayer(targetplayer).addBender(
						BendingType.Water);
				return;
			}
			if (Arrays.asList(chiblockingAliases).contains(choice)) {
				if (Tools.isBender(targetplayer.getName(),
						BendingType.ChiBlocker)) {
					sendMessage(
							player,
							targetplayer.getName()
									+ " "
									+ Tools.getMessage(player,
											"General.they_already_chi"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.chiblocking")) {
					Tools.sendMessage(player, "General.no_perms_chi");
					return;
				}
				sendMessage(
						player,
						targetplayer.getName()
								+ " "
								+ Tools.getMessage(player,
										"General.you_add_chi"));
				sendMessage(
						targetplayer,
						senderName
								+ " "
								+ Tools.getMessage(targetplayer,
										"General.add_you_chi"));
				// config.addBending(targetplayer, "chiblocker");
				// PlayerStorageWriter.addBending(targetplayer,
				// BendingType.ChiBlocker);
				BendingPlayer.getBendingPlayer(targetplayer).addBender(
						BendingType.ChiBlocker);
				return;
			}
			printAddUsage(player);
		}

	}

	private void printClearUsage(Player player) {
		printUsageMessage(player, "/bending clear", "General.clear_all");

		printUsageMessage(player, "/bending clear <slot#>",
				"General.clear_slot");

		printUsageMessage(player, "/bending clear <item>", "General.clear_item");

	}

	private void clear(Player player, String[] args) {
		if (!hasPermission(player, "bending.command.clear"))
			return;
		if (player == null) {
			printNotFromConsole();
			return;
		}

		if (args.length != 1 && args.length != 2) {
			printClearUsage(player);
		}

		if (args.length == 1) {
			// if (!ConfigManager.bendToItem) {
			// for (int i = 0; i <= 8; i++) {
			// // config.removeAbility(player, i);
			// // PlayerStorageWriter.removeSlot(player, i);
			// BendingPlayer.getBendingPlayer(player).removeAbility(i);
			// }
			// } else {
			// for (Material mat : Material.values()) {
			// // config.removeAbility(player, mat.getId());
			// // PlayerStorageWriter.removeItem(player, mat);
			// BendingPlayer.getBendingPlayer(player).removeAbility(mat);
			// }
			// }
			BendingPlayer.getBendingPlayer(player).clearAbilities();
			Tools.sendMessage(player, "General.cleared_message");
		} else if (args.length == 2) {
			try {
				int slot = Integer.parseInt(args[1]);

				if (slot > 0 && slot < 10) {
					BendingPlayer.getBendingPlayer(player).removeAbility(
							slot - 1);
					sendMessage(
							player,
							Tools.getMessage(player, "General.slot")
									+ " "
									+ args[1]
									+ " "
									+ Tools.getMessage(player,
											"General.slot_item_cleared"));
					return;
				}
				printClearUsage(player);
				return;
			} catch (NumberFormatException e) {
				Material mat = Material.matchMaterial(args[1]);
				if (mat != null) {
					BendingPlayer.getBendingPlayer(player).removeAbility(
							Material.matchMaterial(args[1]));
					sendMessage(
							player,
							"Item "
									+ args[1]
									+ " "
									+ Tools.getMessage(player,
											"General.slot_item_cleared"));
					return;
				} else {
					printClearUsage(player);
					return;
				}
			}
			//
			// if (!ConfigManager.bendToItem) {
			// try {
			// int slot = Integer.parseInt(args[1]);
			//
			// if (slot > 0 && slot < 10) {
			// // config.removeAbility(player, slot - 1);
			// // PlayerStorageWriter.removeSlot(player, slot - 1);
			// BendingPlayer.getBendingPlayer(player).removeAbility(
			// slot - 1);
			// sendMessage(
			// player,
			// Tools.getMessage(player, "General.slot")
			// + " "
			// + args[1]
			// + " "
			// + Tools.getMessage(player,
			// "General.slot_item_cleared"));
			// return;
			// }
			// printClearUsage(player);
			// } catch (NumberFormatException e) {
			// printClearUsage(player);
			// return;
			// }
			// } else {
			// if (Material.matchMaterial(args[1]) != null) {
			// // config.removeAbility(player,
			// // Material.matchMaterial(args[1]));
			// // PlayerStorageWriter.removeItem(player,
			// // Material.matchMaterial(args[1]));
			// BendingPlayer.getBendingPlayer(player).removeAbility(
			// Material.matchMaterial(args[1]));
			// sendMessage(
			// player,
			// "Item "
			// + args[1]
			// + " "
			// + Tools.getMessage(player,
			// "General.slot_item_cleared"));
			// return;
			// }
			// printClearUsage(player);
			// }

		}

	}

	private void printBindUsage(Player player) {
		boolean item = bPlayer.getBendToItem();
		if (!item) {
			printUsageMessage(player, "/bending bind <ability>",
					"General.bind_slot");
		} else {
			printUsageMessage(player, "/bending bind <ability>",
					"General.bind_item");
		}
		printUsageMessage(player, "/bending bind <ability> <slot#>",
				"General.bind_to_slot");
		printUsageMessage(player, "/bending bind <ability> <item>",
				"General.bind_to_item");
	}

	private void bind(Player player, String[] args) {
		if (!hasPermission(player, "bending.command.bind"))
			return;
		if (player == null) {
			printNotFromConsole();
			return;
		}

		if (args.length != 2 && args.length != 3) {
			printBindUsage(player);
			return;
		}

		String a = args[1];
		Abilities ability = Abilities.getAbility(a);

		if (ability == null) {
			printBindUsage(player);
			return;
		}

		if (!Tools.hasPermission(player, ability)) {
			printNoPermissions(player);
			return;
		}

		int slot = player.getInventory().getHeldItemSlot();
		Material mat = player.getInventory().getItemInHand().getType();
		boolean item = bPlayer.getBendToItem();

		if (args.length == 3) {
			try {
				slot = Integer.parseInt(args[2]);

				if (slot <= 0 || slot >= 10) {
					printBindUsage(player);
					return;
				}
				slot--;
			} catch (NumberFormatException e) {
				mat = Material.matchMaterial(args[2]);
				if (mat == null) {
					printBindUsage(player);
					return;
				}
				item = true;
			}
		}

		// if (args.length == 3 && ConfigManager.bendToItem) {
		// mat = Material.matchMaterial(args[2]);
		// if (mat == null) {
		// printNoPermissions(player);
		// return;
		// }
		// } else if (args.length == 3) {
		// try {
		// slot = Integer.parseInt(args[2]);
		//
		// if (slot <= 0 || slot >= 10) {
		// printNoPermissions(player);
		// return;
		// }
		// slot--;
		// } catch (NumberFormatException e) {
		// printBindUsage(player);
		// return;
		// }
		// }

		ChatColor color = ChatColor.WHITE;
		ChatColor white = ChatColor.WHITE;

		if (Abilities.isWaterbending(ability)) {
			if (!Tools.isBender(player.getName(), BendingType.Water)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = Tools.getColor(ConfigManager.getColor("Water"));
				Tools.sendMessage(player, color2, "General.not_water");
				return;
			}
			color = Tools.getColor(ConfigManager.getColor("Water"));
			if (!item) {
				// config.setAbility(player, ability, slot);
				// PlayerStorageWriter.bindSlot(player, slot, ability);
				BendingPlayer.getBendingPlayer(player)
						.setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to slot " + (slot + 1));
			} else {
				// config.setAbility(player, ability, mat);
				// PlayerStorageWriter.bindItem(player, mat, ability);
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isAirbending(ability)) {
			if (!Tools.isBender(player.getName(), BendingType.Air)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = Tools.getColor(ConfigManager.getColor("Air"));
				Tools.sendMessage(player, color2, "General.not_air");
				return;
			}
			color = Tools.getColor(ConfigManager.getColor("Air"));
			if (!item) {
				// config.setAbility(player, ability, slot);
				// PlayerStorageWriter.bindSlot(player, slot, ability);
				BendingPlayer.getBendingPlayer(player)
						.setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to slot " + (slot + 1));
			} else {
				// config.setAbility(player, ability, mat);
				// PlayerStorageWriter.bindItem(player, mat, ability);
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isEarthbending(ability)) {
			if (!Tools.isBender(player.getName(), BendingType.Earth)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = Tools.getColor(ConfigManager.getColor("Earth"));
				Tools.sendMessage(player, color2, "General.not_earth");
				return;
			}
			color = Tools.getColor(ConfigManager.getColor("Earth"));
			if (!item) {
				// config.setAbility(player, ability, slot);
				// PlayerStorageWriter.bindSlot(player, slot, ability);
				BendingPlayer.getBendingPlayer(player)
						.setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to slot " + (slot + 1));
			} else {
				// config.setAbility(player, ability, mat);
				// PlayerStorageWriter.bindItem(player, mat, ability);
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isChiBlocking(ability)) {
			if (!Tools.isBender(player.getName(), BendingType.ChiBlocker)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = Tools.getColor(ConfigManager.getColor("ChiBlocker"));
				Tools.sendMessage(player, color2, "General.not_chi");
				return;
			}
			color = Tools.getColor(ConfigManager.getColor("ChiBlocker"));
			if (!item) {
				// config.setAbility(player, ability, slot);
				// PlayerStorageWriter.bindSlot(player, slot, ability);
				BendingPlayer.getBendingPlayer(player)
						.setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to slot " + (slot + 1));
			} else {
				// config.setAbility(player, ability, mat);
				// PlayerStorageWriter.bindItem(player, mat, ability);
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isFirebending(ability)) {
			if (!Tools.isBender(player.getName(), BendingType.Fire)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = Tools.getColor(ConfigManager.getColor("Fire"));
				Tools.sendMessage(player, color2, "General.not_fire");
				return;
			}
			color = Tools.getColor(ConfigManager.getColor("Fire"));
			if (!item) {
				// config.setAbility(player, ability, slot);
				// PlayerStorageWriter.bindSlot(player, slot, ability);
				BendingPlayer.getBendingPlayer(player)
						.setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to slot " + (slot + 1));
			} else {
				// config.setAbility(player, ability, mat);
				// PlayerStorageWriter.bindItem(player, mat, ability);
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (ability == Abilities.AvatarState) {
			if (!hasPermission(player, "bending.admin.avatarstate"))
				return;

			color = ChatColor.DARK_PURPLE;
			if (!item) {
				// config.setAbility(player, ability, slot);
				// PlayerStorageWriter.bindSlot(player, slot, ability);
				BendingPlayer.getBendingPlayer(player)
						.setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to slot " + (slot + 1));
			} else {
				// config.setAbility(player, ability, mat);
				// PlayerStorageWriter.bindItem(player, mat, ability);
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white
						+ " bound to " + mat.name().replaceAll("_", " "));
			}
			return;

		}

	}

	private boolean hasPermission(Player player, String permission) {
		if (player == null)
			return true;
		if (player.hasPermission(permission)) {
			return true;
		}
		printNoPermissions(player);
		return false;
	}

	private boolean hasHelpPermission(Player player, String permission) {
		if (player == null)
			return true;
		if (player.hasPermission(permission))
			return true;
		return false;
	}

	private void printHelpDialogue(Player player) {
		Tools.sendMessage(player, ChatColor.RED, "General.help_list");
		Tools.sendMessage(player, ChatColor.RED, "General.command_list");
		Tools.sendMessage(player, ChatColor.RED, "General.ability_list");
	}

	private void sendNoCommandPermissionMessage(Player player, String command) {
		sendMessage(player, Tools.getMessage(player, "General.no_use_perms")
				+ " /bending " + command + ".");
	}

	private void printCommands(Player player) {
		sendMessage(player, "Bending aliases: bending bend b mtla tla");
		String slot = Tools.getMessage(player, "General.slot") + "#";
		if (player != null)
			if (bPlayer.getBendToItem())
				slot = "item";
		// if (ConfigManager.bendToItem)
		// slot = "item";
		if (hasHelpPermission(player, "bending.command.bind"))
			sendMessage(player, "/bending bind <ability> [" + slot + "]");
		if (hasHelpPermission(player, "bending.command.clear"))
			sendMessage(player, "/bending clear [" + slot + "]");

		if (hasHelpPermission(player, "bending.admin.choose")) {
			sendMessage(player, "/bending choose [player] <element>");
		} else if (hasHelpPermission(player, "bending.command.choose")
				|| hasHelpPermission(player, "bending.admin.rechoose")) {
			sendMessage(player, "/bending choose <element>");
		}

		if (hasHelpPermission(player, "bending.admin.add"))
			sendMessage(player, "/bending add [player] <element>");
		if (hasHelpPermission(player, "bending.admin.remove"))
			sendMessage(player,
					"/bending remove <player1> [player2] [player3] ...");
		if (hasHelpPermission(player, "bending.admin.permaremove"))
			sendMessage(player,
					"/bending permaremove <player1> [player2] [player3] ...");

		if (hasHelpPermission(player, "bending.admin.toggle")) {
			sendMessage(player, "/bending toggle [player]");
		} else if (hasHelpPermission(player, "bending.command.toggle")) {
			sendMessage(player, "/bending toggle");
		}

		if (hasHelpPermission(player, "bending.command.display"))
			sendMessage(player, "/bending display [element]");
		if (hasHelpPermission(player, "bending.admin.reload"))
			sendMessage(player, "/bending reload");
		if (hasHelpPermission(player, "bending.admin.import"))
			sendMessage(player, "/bending import");
		if (hasHelpPermission(player, "bending.command.who"))
			sendMessage(player, "/bending who [player]");
		if (hasHelpPermission(player, "bending.command.language"))
			sendMessage(player, "/bending language [language]");
		if (hasHelpPermission(player, "bending.command.bindmode"))
			sendMessage(player, "/bending bindmode [slot/item]");
		if (hasHelpPermission(player, "bending.command.version"))
			sendMessage(player, "/bending version");

	}

}

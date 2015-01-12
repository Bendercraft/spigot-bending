package net.avatarrealms.minecraft.bending;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;
import net.avatarrealms.minecraft.bending.abilities.BendingSpecializationType;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.db.DBUtils;
import net.avatarrealms.minecraft.bending.db.IBendingDB;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Metrics;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;


public class BendingCommand {

	private final String[] bindAliases = {"bind", "b"};
	private final String[] clearAliases = {"clear", "cl"};
	private final String[] chooseAliases = {"choose", "ch"};
	private final String[] addAliases = {"add", "a"};
	private final String[] specializeAliases = {"specialize", "spe"};
	private final String[] removeAliases = {"remove", "r"};
	private final String[] toggleAliases = {"toggle", "t"};
	private final String[] displayAliases = {"display", "disp", "dis", "d"};
	private final String[] reloadAliases = {"reload"};
	private final String[] helpAliases = {"help", "h", "?"};
	private final String[] importAliases = {"import"};
	private final String[] whoAliases = {"who", "wh", "w"};
	private final String[] languageAliases = {"language", "lang", "la", "l"};
	private final String[] bindModeAliases = {"bindmode", "bmode", "bindm", "bm"};
	private final String[] versionAliases = {"version", "ver", "v"};
	private final String[] airbendingAliases = {"air", "a", "airbender", "airbending", "airbend"};
	private final String[] earthbendingAliases = {"earth", "e", "earthbender", "earthbending", "earthbend", "terre"};
	private final String[] firebendingAliases = {"fire", "f", "firebender", "firebending", "firebend", "feu"};
	private final String[] waterbendingAliases = {"water", "w", "waterbender", "waterbending", "waterbend", "eau"};
	private final String[] chiblockingAliases = {"chi", "c", "chiblock", "chiblocker", "chiblocking"};
	private final String[] saveAliases = {"save"};
	private final String[] getbackAliases = {"getback"};
	private final String[] joinAliases = {"joinghost", "jghost"};
	private final String[] leaveAliases = {"leaveghost", "lghost"};
	private final String[] itemAliases = {"item", "ite", "it", "i"};
	private final String[] slotAliases = {"slot", "slo", "sl", "s"};
	private final String[] metricsAlias = {"metrics"};
	private final String[] dbAlias = {"db"};
	private final File dataFolder;
	private final Server server;
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
	static final String import_usage = "Imports data from your benders.json file to your MySQL database.";
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

	public BendingCommand (final Player player, String[] args, final File dataFolder, final Server server) {
		this.dataFolder = dataFolder;
		// this.config = config;
		this.server = server;
		if (player != null) {
			this.bPlayer = BendingPlayer.getBendingPlayer(player);
		}
		for (int i = 0 ; i < args.length ; i++) {
			args[i] = args[i].toLowerCase();
		}
		if (args.length >= 1) {
			if (args[args.length - 1].equalsIgnoreCase("&")) {
				this.verbose = false;
				final String[] temp = new String[args.length - 1];
				for (int i = 0 ; i < (args.length - 1) ; i++) {
					temp[i] = args[i];
				}
				args = temp;
			}
			final String arg = args[0];
			if (Arrays.asList(this.bindAliases).contains(arg)) {
				bind(player, args);
			}
			else if (Arrays.asList(this.clearAliases).contains(arg)) {
				clear(player, args);
			}
			else if (Arrays.asList(this.chooseAliases).contains(arg)) {
				choose(player, args);
			}
			else if (Arrays.asList(this.addAliases).contains(arg)) {
				add(player, args);
			}
			else if (Arrays.asList(this.specializeAliases).contains(arg)) {
				specialize(player, args);
			}
			else if (Arrays.asList(this.removeAliases).contains(arg)) {
				remove(player, args);
			}
			else if (Arrays.asList(this.toggleAliases).contains(arg)) {
				toggle(player, args);
			}
			else if (Arrays.asList(this.displayAliases).contains(arg)) {
				display(player, args);
			}
			else if (Arrays.asList(this.reloadAliases).contains(arg)) {
				reload(player, args);
			}
			else if (Arrays.asList(this.helpAliases).contains(arg)) {
				help(player, args);
			}
			else if (Arrays.asList(this.whoAliases).contains(arg)) {
				who(player, args);
			}
			else if (Arrays.asList(this.languageAliases).contains(arg)) {
				language(player, args);
			}
			else if (Arrays.asList(this.bindModeAliases).contains(arg)) {
				bindMode(player, args);
			}
			else if (Arrays.asList(this.versionAliases).contains(arg)) {
				version(player, args);
			}
			else if (Arrays.asList(this.metricsAlias).contains(arg)) {
				metrics(player, args);
			}
			else if (Arrays.asList(this.dbAlias).contains(arg)) {
				db(player, args);
			}
			else if (Arrays.asList(this.saveAliases).contains(arg)) {
				save(player);
			}
			else if (Arrays.asList(this.getbackAliases).contains(arg)) {
				getback(player);
			}
			else if (Arrays.asList(this.joinAliases).contains(arg)) {
				Bending.plugin.ghostManager.addGhost(player);
			}
			else if (Arrays.asList(this.leaveAliases).contains(arg)) {
				Bending.plugin.ghostManager.removeGhost(player);
			}
			else {
				printHelpDialogue(player);
			}
		}
		else {
			printHelpDialogue(player);
		}
	}

	private void metrics (final Player player, final String[] args) {
		if (player.hasPermission("bending.admin")) {
			sendMessage(player, Metrics.ROOT.toMinecraftString("", 0));
		}
		else {
			player.sendMessage(ChatColor.RED + "You're not allowed to do that.");
		}
	}

	private void db (final Player player, final String[] args) {
		if (!player.hasPermission("bending.admin")) {
			player.sendMessage(ChatColor.RED + "You're not allowed to do that.");
			return;
		}
		if (args.length >= 2) {
			final String routingKey = args[1];
			if (routingKey.equals("convert")) {
				if (args.length == 4) {
					final IBendingDB src = DBUtils.choose(args[2]);
					final IBendingDB dest = DBUtils.choose(args[3]);
					if (src != null) {
						if (dest != null) {
							src.init(Bending.plugin);
							dest.init(Bending.plugin);
							DBUtils.convert(src, dest);
							sendMessage(player, "Success !");
							return;
						}
						else {
							sendMessage(player, "Unknown DB implementation : " + args[3]);
						}
					}
					else {
						sendMessage(player, "Unknown DB implmentation : " + args[2]);
					}
				}
				else {
					sendMessage(player, "Invalid args number");
				}
			}
			else {
				sendMessage(player, "Invalid routing key : " + routingKey);
			}
		}
		else {
			sendMessage(player, "Invalid args number");
		}
		sendMessage(player, "Must be used /db convert <flatfile|mongodb> <flatfile|mongodb>");
		sendMessage(player, "Where the first one is the source, and the second destination");
	}

	private void version (final Player player, final String[] args) {
		if (!hasHelpPermission(player, "bending.command.version")) {
			sendNoCommandPermissionMessage(player, "version");
			return;
		}
		sendMessage(player, "Bending v" + Bending.plugin.getDescription().getVersion());
		sendMessage(player, "Author: orion304; updated by : Koudja, Nokorikatsu");
	}

	private void printVersionUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.command.version")) {
			sendNoCommandPermissionMessage(player, "version");
		}
		printUsageMessage(player, "/bending version", "General.version_usage");
	}

	private void bindMode (final Player player, final String[] args) {
		if (!hasHelpPermission(player, "bending.command.bindmode")) {
			sendNoCommandPermissionMessage(player, "bindmode");
			return;
		}
		boolean bindmode = ConfigManager.bendToItem;
		String mode;
		if (args.length > 2) {
			printBindModeUsage(player);
			return;
		}
		else if (args.length == 1) {
			if (player == null) {
				if (bindmode) {
					mode = "item";
				}
				else {
					mode = "slot";
				}
				sendMessage(player, PluginTools.getMessage(player, "General.server_bind_mode") + "" + mode);
			}
			else {
				this.bPlayer = BendingPlayer.getBendingPlayer(player);
				bindmode = this.bPlayer.getBendToItem();
				if (bindmode) {
					mode = "item";
				}
				else {
					mode = "slot";
				}
				sendMessage(player, PluginTools.getMessage(player, "General.your_bind_mode") + "" + mode);
			}
			return;
		}
		else if (player == null) {
			printNotFromConsole();
			return;
		}
		else {
			this.bPlayer = BendingPlayer.getBendingPlayer(player);
			final String choice = args[1];
			if (Arrays.asList(this.slotAliases).contains(choice)) {
				this.bPlayer.setBendToItem(false);
				sendMessage(player, PluginTools.getMessage(player, "General.bind_mode_change") + "slot");
			}
			else if (Arrays.asList(this.itemAliases).contains(choice)) {
				this.bPlayer.setBendToItem(true);
				sendMessage(player, PluginTools.getMessage(player, "General.bind_mode_change") + "item");
			}
			else {
				printBindModeUsage(player);
			}
			return;
		}
	}

	private void printBindModeUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.command.bindmode")) {
			sendNoCommandPermissionMessage(player, "language");
			return;
		}
		printUsageMessage(player, "/bending bindmode", "General.bind_mode_usage");
		if (player != null) {
			printUsageMessage(player, "/bending bindmode <slot/item>", "General.bind_mode_change_usage");
		}
	}

	private void language (final Player player, final String[] args) {
		if (!hasHelpPermission(player, "bending.command.language")) {
			sendNoCommandPermissionMessage(player, "language");
			return;
		}
		if (args.length > 2) {
			printLanguageUsage(player);
			return;
		}
		else if (args.length == 2) {
			final String language = args[1];
			if (PluginTools.isLanguageSupported(language)) {
				BendingPlayer.getBendingPlayer(player).setLanguage(language);
				PluginTools.sendMessage(player, "General.language_success");
			}
			else {
				PluginTools.sendMessage(player, "General.language_not_supported");
			}
		}
		else {
			sendMessage(player, PluginTools.getMessage(player, "General.your_language") + " " + PluginTools.getLanguage(player));
			sendMessage(player, PluginTools.getMessage(player, "General.default_language") + " " + PluginTools.getDefaultLanguage());
			sendMessage(player,
					PluginTools.getMessage(player, "General.supported_languages") + " " + PluginTools.getSupportedLanguages());
		}
	}

	private void printLanguageUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.command.language")) {
			sendNoCommandPermissionMessage(player, "language");
			return;
		}
		printUsageMessage(player, "/bending language", "General.language_usage");
		printUsageMessage(player, "/bending language <language>", "General.language_change_usage");
	}

	private void printWhoUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.command.who")) {
			sendNoCommandPermissionMessage(player, "who");
			return;
		}
		printUsageMessage(player, "/bending who", "General.who_usage");
		printUsageMessage(player, "/bending who <player>", "General.who_player_usage");
	}

	private void who (final Player player, final String[] args) {
		if (!hasPermission(player, "bending.admin")) {
			player.sendMessage(ChatColor.RED+"You are not authorized to perform this command");
			return;
		}
		if (args.length > 2) {
			printWhoUsage(player);
			return;
		}
		if (args.length == 1) {
			for (final Player p : this.server.getOnlinePlayers()) {
				if (player != null) {
					if (!player.canSee(p)) {
						continue;
					}
				}
				ChatColor color = ChatColor.WHITE;
				if (EntityTools.isBender(p, BendingType.Air)) {
					color = PluginTools.getColor(ConfigManager.getColor("Air"));
				}
				if (EntityTools.isBender(p, BendingType.Water)) {
					color = PluginTools.getColor(ConfigManager.getColor("Water"));
				}
				if (EntityTools.isBender(p, BendingType.Fire)) {
					color = PluginTools.getColor(ConfigManager.getColor("Fire"));
				}
				if (EntityTools.isBender(p, BendingType.Earth)) {
					color = PluginTools.getColor(ConfigManager.getColor("Earth"));
				}
				if (EntityTools.isBender(p, BendingType.ChiBlocker)) {
					color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
				}
				sendMessage(player, color + p.getName());
			}
		} else if (args.length == 2) {
			final Player p = getOnlinePlayer(args[1]);
			if (p == null) {
				sendMessage(player, args[1] + " " + PluginTools.getMessage(player, "General.who_not_on_server"));
			}
			else if (player != null) {
				if (!player.canSee(p)) {
					sendMessage(player, args[1] + " " + PluginTools.getMessage(player, "General.who_not_on_server"));
				}
				else {
					sendMessage(player, p.getDisplayName());
					if (!EntityTools.isBender(p)) {
						sendMessage(player, "- No bending");
					}
					else {
						if (EntityTools.isBender(p, BendingType.Air)) {
							String elementMessage = PluginTools.getColor(ConfigManager.getColor("Air")) + "- Airbending";
							for(BendingSpecializationType spe : bPlayer.getSpecializations()) {
								if(spe.getElement() == BendingType.Air) {
									elementMessage = elementMessage + " ("+spe.name()+")";
								}
							}
							sendMessage(player, elementMessage);
							for (final Abilities ability : Abilities.getAirbendingAbilities()) {
								if (EntityTools.hasPermission(p, ability)) {
									sendMessage(player,
											PluginTools.getColor(ConfigManager.getColor("Air")) + "   -- " + ability.name());
								}
							}
						}
						if (EntityTools.isBender(p, BendingType.Water)) {
							String elementMessage = PluginTools.getColor(ConfigManager.getColor("Water")) + "- Waterbending";
							for(BendingSpecializationType spe : bPlayer.getSpecializations()) {
								if(spe.getElement() == BendingType.Water) {
									elementMessage = elementMessage + " ("+spe.name()+")";
								}
							}
							sendMessage(player, elementMessage);
							for (final Abilities ability : Abilities.getWaterbendingAbilities()) {
								if (EntityTools.hasPermission(p, ability)) {
									sendMessage(player,
											PluginTools.getColor(ConfigManager.getColor("Water")) + "   -- " + ability.name());
								}
							}
						}
						if (EntityTools.isBender(p, BendingType.Fire)) {
							String elementMessage = PluginTools.getColor(ConfigManager.getColor("Fire")) + "- Firebending";
							for(BendingSpecializationType spe : bPlayer.getSpecializations()) {
								if(spe.getElement() == BendingType.Fire) {
									elementMessage = elementMessage + " ("+spe.name()+")";
								}
							}
							sendMessage(player, elementMessage);
							for (final Abilities ability : Abilities.getFirebendingAbilities()) {
								if (EntityTools.hasPermission(p, ability)) {
									sendMessage(player,
											PluginTools.getColor(ConfigManager.getColor("Fire")) + "   -- " + ability.name());
								}
							}
						}
						if (EntityTools.isBender(p, BendingType.Earth)) {
							String elementMessage = PluginTools.getColor(ConfigManager.getColor("Earth")) + "- Earthbending";
							for(BendingSpecializationType spe : bPlayer.getSpecializations()) {
								if(spe.getElement() == BendingType.Earth) {
									elementMessage = elementMessage + " ("+spe.name()+")";
								}
							}
							sendMessage(player, elementMessage);
							for (final Abilities ability : Abilities.getEarthbendingAbilities()) {
								if (EntityTools.hasPermission(p, ability)) {
									sendMessage(player,
											PluginTools.getColor(ConfigManager.getColor("Earth")) + "   -- " + ability.name());
								}
							}
						}
						if (EntityTools.isBender(p, BendingType.ChiBlocker)) {
							String elementMessage = PluginTools.getColor(ConfigManager.getColor("ChiBlocker")) + "- Chiblocking";
							for(BendingSpecializationType spe : bPlayer.getSpecializations()) {
								if(spe.getElement() == BendingType.ChiBlocker) {
									elementMessage = elementMessage + " ("+spe.name()+")";
								}
							}
							sendMessage(player, elementMessage);
							for (final Abilities ability : Abilities.getChiBlockingAbilities()) {
								if (EntityTools.hasPermission(p, ability)) {
									sendMessage(player, PluginTools.getColor(ConfigManager.getColor("ChiBlocker")) + "   -- "
											+ ability.name());
								}
							}
						}
					}
				}
			} else {
				sendMessage(player, p.getDisplayName());
				if (!EntityTools.isBender(p)) {
					sendMessage(player, "-No bending");
				}
				else {
					if (EntityTools.isBender(p, BendingType.Air)) {
						sendMessage(player, PluginTools.getColor(ConfigManager.getColor("Air")) + "-Airbending");
					}
					if (EntityTools.isBender(p, BendingType.Water)) {
						sendMessage(player, PluginTools.getColor(ConfigManager.getColor("Water")) + "-Waterbending");
					}
					if (EntityTools.isBender(p, BendingType.Fire)) {
						sendMessage(player, PluginTools.getColor(ConfigManager.getColor("Fire")) + "-Firebending");
					}
					if (EntityTools.isBender(p, BendingType.Earth)) {
						sendMessage(player, PluginTools.getColor(ConfigManager.getColor("Earth")) + "-Earthbending");
					}
					if (EntityTools.isBender(p, BendingType.ChiBlocker)) {
						sendMessage(player, PluginTools.getColor(ConfigManager.getColor("ChiBlocker")) + "-Chiblocking");
					}
				}
			}
		}
		else {
			printWhoUsage(player);
		}
	}

	private void printUsageMessage (final Player player, final String command, final String key) {
		final ChatColor color = ChatColor.AQUA;
		final String usage = PluginTools.getMessage(player, "General.usage");
		final String description = PluginTools.getMessage(player, key);
		sendMessage(player, color + usage + ": " + command);
		sendMessage(player, color + "-" + description);
	}

	private void printChooseUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.admin.choose") && !hasHelpPermission(player, "bending.admin.rechoose")
				&& !hasHelpPermission(player, "bending.command.choose")) {
			sendNoCommandPermissionMessage(player, "choose");
			return;
		}
		if (hasHelpPermission(player, "bending.command.choose") || hasHelpPermission(player, "bending.admin.rechoose")) {
			printUsageMessage(player, "/bending choose <element>", "General.choose_usage");
		}
		if (hasHelpPermission(player, "bending.admin.choose")) {
			printUsageMessage(player, "/bending choose <player> <element>", "General.choose_player_usage");
		}
	}

	private void choose (final Player player, final String[] args) {
		if ((args.length != 2) && (args.length != 3)) {
			printChooseUsage(player);
			if (!player.hasPermission("bending.command.choose") && !player.hasPermission("bending.admin.rechoose")
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
			if (!player.hasPermission("bending.command.choose") && !player.hasPermission("bending.admin.rechoose")
					&& !player.hasPermission("bending.admin.choose")) {
				printNoPermissions(player);
				return;
			}
			if (EntityTools.isBender(player) && !player.hasPermission("bending.admin.rechoose")) {
				printNoPermissions(player);
				return;
			}
			final String choice = args[1].toLowerCase();
			if (Arrays.asList(this.airbendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.air")) {
					PluginTools.sendMessage(player, "General.no_perms_air");
					return;
				}
				PluginTools.sendMessage(player, "General.choosen_air");
				BendingPlayer.getBendingPlayer(player).setBender(BendingType.Air);
				return;
			}
			if (Arrays.asList(this.firebendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.fire")) {
					PluginTools.sendMessage(player, "General.no_perms_fire");
					return;
				}
				PluginTools.sendMessage(player, "General.choosen_fire");
				BendingPlayer.getBendingPlayer(player).setBender(BendingType.Fire);
				return;
			}
			if (Arrays.asList(this.earthbendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.earth")) {
					PluginTools.sendMessage(player, "General.no_perms_earth");
					return;
				}
				PluginTools.sendMessage(player, "General.choosen_earth");
				BendingPlayer.getBendingPlayer(player).setBender(BendingType.Earth);
				return;
			}
			if (Arrays.asList(this.waterbendingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.water")) {
					PluginTools.sendMessage(player, "General.no_perms_water");
					return;
				}
				PluginTools.sendMessage(player, "General.choosen_water");
				BendingPlayer.getBendingPlayer(player).setBender(BendingType.Water);
				return;
			}
			if (Arrays.asList(this.chiblockingAliases).contains(choice)) {
				if (!hasHelpPermission(player, "bending.chiblocking")) {
					PluginTools.sendMessage(player, "General.no_perms_chiblocking");
					return;
				}
				PluginTools.sendMessage(player, "General.choosen_chi");
				BendingPlayer.getBendingPlayer(player).setBender(BendingType.ChiBlocker);
				return;
			}
			printChooseUsage(player);
		}
		else if (args.length == 3) {
			if (!hasPermission(player, "bending.admin.choose")) {
				return;
			}
			final String playername = args[1];
			final Player targetplayer = getOnlinePlayer(playername);
			if (targetplayer == null) {
				printChooseUsage(player);
				return;
			}
			String senderName = PluginTools.getMessage(targetplayer, "General.the_server");
			if (player != null) {
				senderName = player.getName();
			}
			final String choice = args[2].toLowerCase();
			if (Arrays.asList(this.airbendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.air")) {
					PluginTools.sendMessage(player, "General.other_no_perms_air");
					return;
				}
				sendMessage(player, PluginTools.getMessage(player, "General.you_changed") + " " + targetplayer.getName()
						+ "'s bending.");
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.changed_you"));
				PluginTools.sendMessage(targetplayer, "General.choosen_air");
				BendingPlayer.getBendingPlayer(targetplayer).setBender(BendingType.Air);
				return;
			}
			if (Arrays.asList(this.firebendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.fire")) {
					PluginTools.sendMessage(player, "General.other_no_perms_fire");
					return;
				}
				sendMessage(player, PluginTools.getMessage(player, "General.you_changed") + " " + targetplayer.getName()
						+ "'s bending.");
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.changed_you"));
				PluginTools.sendMessage(targetplayer, "General.choosen_fire");
				BendingPlayer.getBendingPlayer(targetplayer).setBender(BendingType.Fire);
				return;
			}
			if (Arrays.asList(this.earthbendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.earth")) {
					PluginTools.sendMessage(player, "General.other_no_perms_earth");
					return;
				}
				sendMessage(player, PluginTools.getMessage(player, "General.you_changed") + " " + targetplayer.getName()
						+ "'s bending.");
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.changed_you"));
				PluginTools.sendMessage(targetplayer, "General.choosen_earth");
				BendingPlayer.getBendingPlayer(targetplayer).setBender(BendingType.Earth);
				return;
			}
			if (Arrays.asList(this.waterbendingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.water")) {
					PluginTools.sendMessage(player, "General.other_no_perms_water");
					return;
				}
				sendMessage(player, PluginTools.getMessage(player, "General.you_changed") + " " + targetplayer.getName()
						+ "'s bending.");
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.changed_you"));
				PluginTools.sendMessage(targetplayer, "General.choosen_water");
				BendingPlayer.getBendingPlayer(targetplayer).setBender(BendingType.Water);
				return;
			}
			if (Arrays.asList(this.chiblockingAliases).contains(choice)) {
				if (!hasHelpPermission(targetplayer, "bending.chiblocking")) {
					sendMessage(player, "General.other_no_perms_chi");
					return;
				}
				sendMessage(player, PluginTools.getMessage(player, "General.you_changed") + " " + targetplayer.getName()
						+ "'s bending.");
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.changed_you"));
				PluginTools.sendMessage(targetplayer, "General.choosen_chi");
				BendingPlayer.getBendingPlayer(targetplayer).setBender(BendingType.ChiBlocker);
				return;
			}
			printChooseUsage(player);
		}
	}

	private void sendMessage (final Player player, final String message) {
		if (!this.verbose) {
			return;
		}
		if (player == null) {
			Bending.log.info(message);
		}
		else {
			player.sendMessage(message);
		}
	}

	private void printImportUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.admin.import")) {
			sendNoCommandPermissionMessage(player, "import");
			return;
		}
		printUsageMessage(player, "/bending import", "General.import_usage");
	}

	private void printNoPermissions (final Player player) {
		sendMessage(player, ChatColor.RED + PluginTools.getMessage(player, "General.no_execute_perms"));
	}

	private void help (final Player player, final String[] args) {
		final List<String> command = new ArrayList<String>();
		for (final String s : Bending.commands.keySet()) {
			if (hasHelpPermission(player, "bending." + s)) {
				command.add(Bending.commands.get(s));
			}
		}
		if (args.length > 1) {
			helpCommand(player, args);
			final Abilities ability = Abilities.getAbility(args[1]);
			if (Abilities.getAbility(args[1]) != null) {
				ChatColor cc = ChatColor.GOLD;
				String element = "Other";
				if (Abilities.isAirbending(Abilities.getAbility(args[1]))) {
					cc = PluginTools.getColor(ConfigManager.color.get("Air"));
					element = "Air";
				}
				else if (Abilities.isFirebending(Abilities.getAbility(args[1]))) {
					cc = PluginTools.getColor(ConfigManager.color.get("Fire"));
					element = "Fire";
				}
				else if (Abilities.isEarthbending(Abilities.getAbility(args[1]))) {
					cc = PluginTools.getColor(ConfigManager.color.get("Earth"));
					element = "Earth";
				}
				else if (Abilities.isWaterbending(Abilities.getAbility(args[1]))) {
					cc = PluginTools.getColor(ConfigManager.color.get("Water"));
					element = "Water";
				}
				else if (Abilities.isChiBlocking(Abilities.getAbility(args[1]))) {
					cc = PluginTools.getColor(ConfigManager.color.get("ChiBlocker"));
					element = "Chiblocker";
				}
				if (EntityTools.hasPermission(player, ability)) {
					sendMessage(player, ("                                                " + cc + ability.name()));
					PluginTools.sendMessage(player, cc, element + "." + ability.name());
					return;
				}
				else {
					sendMessage(player, PluginTools.getMessage(player, "General.no_bind_perms") + " " + cc + ability + ChatColor.WHITE
							+ ".");
				}
			}
		}
		else {
			printCommands(player);
		}
	}

	private void helpCommand (final Player player, final String[] args) {
		final ChatColor color = ChatColor.AQUA;
		final String command = args[1];
		if (Arrays.asList(this.bindAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.bind")) {
				sendNoCommandPermissionMessage(player, "bind");
				return;
			}
			sendMessage(player, color + "Command: /bending bind");
			String aliases = "";
			for (final String alias : this.bindAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printBindUsage(player);
		}
		else if (Arrays.asList(this.clearAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.clear")) {
				sendNoCommandPermissionMessage(player, "clear");
				return;
			}
			sendMessage(player, color + "Command: /bending clear");
			String aliases = "";
			for (final String alias : this.clearAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printClearUsage(player);
		}
		else if (Arrays.asList(this.chooseAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.choose") && !hasHelpPermission(player, "bending.admin.choose")
					&& !hasHelpPermission(player, "bending.admin.rechoose")) {
				sendNoCommandPermissionMessage(player, "choose");
				return;
			}
			sendMessage(player, color + "Command: /bending choose");
			String aliases = "";
			for (final String alias : this.chooseAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printChooseUsage(player);
		}
		else if (Arrays.asList(this.addAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.add")) {
				sendNoCommandPermissionMessage(player, "add");
				return;
			}
			sendMessage(player, color + "Command: /bending add");
			String aliases = "";
			for (final String alias : this.addAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printAddUsage(player);
		}
		else if (Arrays.asList(this.specializeAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.admin.specialize")) {
				sendNoCommandPermissionMessage(player, "specialize");
				return;
			}
			sendMessage(player, color + "Command: /bending specialize");
			String aliases = "";
			for (final String alias : this.specializeAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printAddUsage(player);
		}
		else if (Arrays.asList(this.removeAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.remove")) {
				sendNoCommandPermissionMessage(player, "remove");
				return;
			}
			sendMessage(player, color + "Command: /bending remove");
			String aliases = "";
			for (final String alias : this.removeAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printRemoveUsage(player);
		}
		else if (Arrays.asList(this.toggleAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.toggle")) {
				sendNoCommandPermissionMessage(player, "toggle");
				return;
			}
			sendMessage(player, color + "Command: /bending toggle");
			String aliases = "";
			for (final String alias : this.toggleAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printToggleUsage(player);
		}
		else if (Arrays.asList(this.displayAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.display")) {
				sendNoCommandPermissionMessage(player, "display");
				return;
			}
			sendMessage(player, color + "Command: /bending display");
			String aliases = "";
			for (final String alias : this.displayAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printDisplayUsage(player);
		}
		else if (Arrays.asList(this.reloadAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.reload")) {
				sendNoCommandPermissionMessage(player, "reload");
				return;
			}
			sendMessage(player, color + "Command: /bending reload");
			String aliases = "";
			for (final String alias : this.reloadAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printReloadUsage(player);
		}
		else if (Arrays.asList(this.importAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.import")) {
				sendNoCommandPermissionMessage(player, "import");
				return;
			}
			sendMessage(player, color + "Command: /bending import");
			String aliases = "";
			for (final String alias : this.importAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printImportUsage(player);
		}
		else if (Arrays.asList(this.whoAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.who")) {
				sendNoCommandPermissionMessage(player, "who");
				return;
			}
			sendMessage(player, color + "Command: /bending who");
			String aliases = "";
			for (final String alias : this.whoAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printWhoUsage(player);
		}
		else if (Arrays.asList(this.languageAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.language")) {
				sendNoCommandPermissionMessage(player, "language");
				return;
			}
			sendMessage(player, color + "Command: /bending language");
			String aliases = "";
			for (final String alias : this.languageAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printLanguageUsage(player);
		}
		else if (Arrays.asList(this.versionAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.version")) {
				sendNoCommandPermissionMessage(player, "version");
				return;
			}
			sendMessage(player, color + "Command: /bending version");
			String aliases = "";
			for (final String alias : this.versionAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printVersionUsage(player);
		}
		else if (Arrays.asList(this.bindModeAliases).contains(command)) {
			if (!hasHelpPermission(player, "bending.command.bindmode")) {
				sendNoCommandPermissionMessage(player, "bindmode");
				return;
			}
			sendMessage(player, color + "Command: /bending bindmode");
			String aliases = "";
			for (final String alias : this.bindModeAliases) {
				aliases = aliases + alias + " ";
			}
			sendMessage(player, color + "Aliases: " + aliases);
			printBindModeUsage(player);
		}
	}

	private void printReloadUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.admin.reload")) {
			sendNoCommandPermissionMessage(player, "reload");
			return;
		}
		else {
			printUsageMessage(player, "/bending reload", "General.reload_usage");
		}
	}

	private void reload (final Player player, final String[] args) {
		if (!hasPermission(player, "bending.admin.reload")) {
			return;
		}
		Bending.configManager.load(new File(this.dataFolder, "config.yml"));
		Bending.language.load(new File(this.dataFolder, "language.yml"));
		PluginTools.stopAllBending();
		sendMessage(player, ChatColor.AQUA + "Bending " + PluginTools.getMessage(player, "General.reload_success"));
	}

	private void printDisplayUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.command.display")) {
			sendNoCommandPermissionMessage(player, "display");
			return;
		}
		if (player != null) {
			printUsageMessage(player, "/bending display", "General.display_usage");
		}
		printUsageMessage(player, "/bending display <element>", "General.display_element_usage");
	}

	private void display (final Player player, final String[] args) {
		if (args.length > 2) {
			printDisplayUsage(player);
		}
		if (args.length == 1) {
			if (player == null) {
				printNotFromConsole();
				return;
			}
			boolean none = true;
			final boolean item = this.bPlayer.getBendToItem();
			for (final BendingType type : this.bPlayer.getBendingTypes()) {
				final ChatColor color = PluginTools.getColor(ConfigManager.getColor(type.name()));
				String msg = "You're a " + type.name();
				if (type != BendingType.ChiBlocker) {
					msg += " bender.";
				}
				sendMessage(player, color + msg);
			}
			for (final BendingSpecializationType spe : this.bPlayer.getSpecializations()) {
				sendMessage(player, "You have specialization " + spe.name() + " for element " + spe.getElement().name());
			}
			if (!item) {
				for (int i = 0 ; i <= 8 ; i++) {
					// Abilities a = config.getAbility(player, i);
					final Abilities a = this.bPlayer.getAbility(i);
					if (a != null) {
						none = false;
						ChatColor color = ChatColor.WHITE;
						if (Abilities.isAirbending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Air"));
						}
						else if (Abilities.isChiBlocking(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
						}
						else if (Abilities.isEarthbending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Earth"));
						}
						else if (Abilities.isFirebending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Fire"));
						}
						else if (Abilities.isWaterbending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Water"));
						}
						final String ability = a.name();
						sendMessage(player, PluginTools.getMessage(player, "General.slot") + " " + (i + 1) + ": " + color + ability);
					}
				}
			}
			else {
				for (final Material mat : Material.values()) {
					// Abilities a = config.getAbility(player, mat);
					final Abilities a = this.bPlayer.getAbility(mat);
					if (a != null) {
						none = false;
						ChatColor color = ChatColor.WHITE;
						if (Abilities.isAirbending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Air"));
						}
						else if (Abilities.isChiBlocking(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
						}
						else if (Abilities.isEarthbending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Earth"));
						}
						else if (Abilities.isFirebending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Fire"));
						}
						else if (Abilities.isWaterbending(a)) {
							color = PluginTools.getColor(ConfigManager.getColor("Water"));
						}
						final String ability = a.name();
						sendMessage(player, mat.name().replaceAll("_", " ") + ": " + color + ability);
					}
				}
			}
			if (none) {
				PluginTools.sendMessage(player, "General.display_no_abilities");
			}
		}
		if (args.length == 2) {
			List<Abilities> abilitylist = null;
			final String choice = args[1].toLowerCase();
			ChatColor color = ChatColor.WHITE;
			if (Arrays.asList(this.airbendingAliases).contains(choice)) {
				abilitylist = Abilities.getAirbendingAbilities();
				color = PluginTools.getColor(ConfigManager.getColor("Air"));
			}
			else if (Arrays.asList(this.waterbendingAliases).contains(choice)) {
				abilitylist = Abilities.getWaterbendingAbilities();
				color = PluginTools.getColor(ConfigManager.getColor("Water"));
			}
			else if (Arrays.asList(this.earthbendingAliases).contains(choice)) {
				abilitylist = Abilities.getEarthbendingAbilities();
				color = PluginTools.getColor(ConfigManager.getColor("Earth"));
			}
			else if (Arrays.asList(this.firebendingAliases).contains(choice)) {
				abilitylist = Abilities.getFirebendingAbilities();
				color = PluginTools.getColor(ConfigManager.getColor("Fire"));
			}
			else if (Arrays.asList(this.chiblockingAliases).contains(choice)) {
				abilitylist = Abilities.getChiBlockingAbilities();
				color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
			}
			if (abilitylist != null) {
				for (final Abilities ability : abilitylist) {
					if (EntityTools.canBend(player, ability)) {
						sendMessage(player, color + ability.name());
					}
				}
				return;
			}
			else {
				printDisplayUsage(player);
			}
		}
	}

	private void printToggleUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.command.toggle")) {
			sendNoCommandPermissionMessage(player, "toggle");
			return;
		}
		printUsageMessage(player, "/bending toggle", "General.toggle_usage");
	}

	private boolean toggleSpe (final Player player, final String[] args) {
		if ((args.length == 2) && Arrays.asList(this.specializeAliases).contains(args[0])
				&& Arrays.asList(this.toggleAliases).contains(args[1])) {
			if (EntityTools.speToggledBenders.contains(player)) {
				EntityTools.speToggledBenders.remove(player);
				player.sendMessage("You toggled back your specialization");
			}
			else {
				EntityTools.speToggledBenders.add(player);
				player.sendMessage("You toggled your specialization");
			}
			return true;
		}
		return false;
	}

	private void toggle (final Player player, final String[] args) {
		if (args.length == 1) {
			if (!hasHelpPermission(player, "bending.command.toggle") && !hasHelpPermission(player, "bending.admin.toggle")) {
				printNoPermissions(player);
				return;
			}
			if (player == null) {
				printNotFromConsole();
				return;
			}
			if (!EntityTools.toggledBending.contains(player)) {
				EntityTools.toggledBending.add(player);
				PluginTools.sendMessage(player, ChatColor.AQUA, "General.toggle_off");
			}
			else {
				EntityTools.toggledBending.remove(player);
				PluginTools.sendMessage(player, ChatColor.AQUA, "General.toggle_on");
			}
		}
		else {
			if (!hasPermission(player, "bending.admin.toggle")) {
				return;
			}
			String playerlist = "";
			for (int i = 1 ; i < args.length ; i++) {
				final String name = args[i];
				final Player targetplayer = getOnlinePlayer(name);
				String senderName = PluginTools.getMessage(targetplayer, "General.the_server");
				if (player != null) {
					senderName = player.getName();
				}
				if (targetplayer != null) {
					if (!EntityTools.toggledBending.contains(targetplayer)) {
						EntityTools.toggledBending.add(targetplayer);
						sendMessage(targetplayer, senderName + " " + "General.admin_toggle_off");
					}
					else {
						EntityTools.toggledBending.remove(targetplayer);
						sendMessage(targetplayer, senderName + " " + "General.admin_toggle_on");
					}
					playerlist = playerlist + " " + targetplayer.getName();
				}
			}
			sendMessage(player, PluginTools.getMessage(player, "General.admin_toggle") + " " + playerlist);
		}
	}

	private void printNotFromConsole () {
		PluginTools.sendMessage(null, "General.not_from_console");
	}

	private void printRemoveUsage (final Player player) {
		if (!hasHelpPermission(player, "bending.admin.remove")) {
			sendNoCommandPermissionMessage(player, "remove");
			return;
		}
		printUsageMessage(player, "/bending remove <player>", "General.remove_usage");
	}

	private void remove (final Player player, final String[] args) {
		if (!hasPermission(player, "bending.admin.remove")) {
			return;
		}
		String playerlist = "";
		for (int i = 1 ; i < args.length ; i++) {
			final String playername = args[i];
			final Player targetplayer = getOnlinePlayer(playername);
			String senderName = PluginTools.getMessage(targetplayer, "General.the_server");
			if (player != null) {
				senderName = player.getName();
			}
			if (targetplayer != null) {
				BendingPlayer.getBendingPlayer(targetplayer).removeBender();
				targetplayer.sendMessage(senderName + " " + PluginTools.getMessage(targetplayer, "General.remove_you"));
				playerlist = playerlist + targetplayer.getName() + " ";
			}
		}
		sendMessage(player, PluginTools.getMessage(player, "General.you_remove") + " " + playerlist);
	}

	private void printAddUsage (final Player player) {
		if (player != null) {
			printUsageMessage(player, "/bending add <element>", "General.add_self");
		}
		printUsageMessage(player, "/bending add <player> <element>", "General.add_other");
	}

	private void printSpecializationUsage (final Player player) {
		printUsageMessage(player, "/bending spe", "General.spe_list");
		if (player != null) {
			printUsageMessage(player, "/bending spe set <specialization>", "General.spe_set_self");
			printUsageMessage(player, "/bending spe add <specialization>", "General.spe_add_self");
			printUsageMessage(player, "/bending spe remove <specialization>", "General.spe_remove_self");
			printUsageMessage(player, "/bending spe clear", "General.spe_clear_self");
		}
		else {
			printUsageMessage(player, "/bending spe set <specialization> <player>", "General.spe_set_other");
			printUsageMessage(player, "/bending spe add <specialization> <player>", "General.spe_add_other");
			printUsageMessage(player, "/bending spe remove <specialization> <player>", "General.spe_remove_self");
			printUsageMessage(player, "/bending spe clear <player>", "General.spe_clear_self");
		}
	}

	private void add (final Player player, final String[] args) {
		if (!hasPermission(player, "bending.admin.add")) {
			return;
		}
		if ((args.length != 2) && (args.length != 3)) {
			printAddUsage(player);
			return;
		}
		if (args.length == 2) {
			final String choice = args[1].toLowerCase();
			if (Arrays.asList(this.airbendingAliases).contains(choice)) {
				if (EntityTools.isBender(player, BendingType.Air)) {
					PluginTools.sendMessage(player, "General.you_already_air");
					return;
				}
				if (!hasHelpPermission(player, "bending.air")) {
					PluginTools.sendMessage(player, "General.no_perms_air");
					return;
				}
				PluginTools.sendMessage(player, "General.add_air");
				BendingPlayer.getBendingPlayer(player).addBender(BendingType.Air);
				return;
			}
			if (Arrays.asList(this.firebendingAliases).contains(choice)) {
				if (EntityTools.isBender(player, BendingType.Fire)) {
					PluginTools.sendMessage(player, "General.you_already_fire");
					return;
				}
				if (!hasHelpPermission(player, "bending.fire")) {
					PluginTools.sendMessage(player, "General.no_perms_fire");
					return;
				}
				PluginTools.sendMessage(player, "General.add_fire");
				BendingPlayer.getBendingPlayer(player).addBender(BendingType.Fire);
				return;
			}
			if (Arrays.asList(this.earthbendingAliases).contains(choice)) {
				if (EntityTools.isBender(player, BendingType.Earth)) {
					PluginTools.sendMessage(player, "General.you_already_earth");
					return;
				}
				if (!hasHelpPermission(player, "bending.earth")) {
					PluginTools.sendMessage(player, "General.no_perms_earth");
					return;
				}
				PluginTools.sendMessage(player, "General.add_earth");
				BendingPlayer.getBendingPlayer(player).addBender(BendingType.Earth);
				return;
			}
			if (Arrays.asList(this.waterbendingAliases).contains(choice)) {
				if (EntityTools.isBender(player, BendingType.Water)) {
					PluginTools.sendMessage(player, "General.you_already_water");
					return;
				}
				if (!hasHelpPermission(player, "bending.water")) {
					PluginTools.sendMessage(player, "General.no_perms_water");
					return;
				}
				PluginTools.sendMessage(player, "General.add_water");
				BendingPlayer.getBendingPlayer(player).addBender(BendingType.Water);
				return;
			}
			if (Arrays.asList(this.chiblockingAliases).contains(choice)) {
				if (EntityTools.isBender(player, BendingType.ChiBlocker)) {
					PluginTools.sendMessage(player, "General.you_already_chi");
					return;
				}
				if (!hasHelpPermission(player, "bending.chiblocking")) {
					PluginTools.sendMessage(player, "General.no_perms_chi");
					return;
				}
				PluginTools.sendMessage(player, "General.add_chi");
				BendingPlayer.getBendingPlayer(player).addBender(BendingType.ChiBlocker);
				return;
			}
			printAddUsage(player);
		}
		else if (args.length == 3) {
			final String playername = args[1];
			final Player targetplayer = getOnlinePlayer(playername);
			if (targetplayer == null) {
				printAddUsage(player);
				return;
			}
			String senderName = PluginTools.getMessage(targetplayer, "General.the_server");
			if (player != null) {
				senderName = player.getName();
			}
			final String choice = args[2].toLowerCase();
			if (Arrays.asList(this.airbendingAliases).contains(choice)) {
				if (EntityTools.isBender(targetplayer, BendingType.Air)) {
					sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.they_already_air"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.air")) {
					PluginTools.sendMessage(player, "General.no_perms_air");
					return;
				}
				sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.you_add_air"));
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.add_you_air"));
				BendingPlayer.getBendingPlayer(targetplayer).addBender(BendingType.Air);
				return;
			}
			if (Arrays.asList(this.firebendingAliases).contains(choice)) {
				if (EntityTools.isBender(targetplayer, BendingType.Fire)) {
					sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.they_already_fire"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.fire")) {
					PluginTools.sendMessage(player, "General.no_perms_fire");
					return;
				}
				sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.you_add_fire"));
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.add_you_fire"));
				BendingPlayer.getBendingPlayer(targetplayer).addBender(BendingType.Fire);
				return;
			}
			if (Arrays.asList(this.earthbendingAliases).contains(choice)) {
				if (EntityTools.isBender(targetplayer, BendingType.Earth)) {
					sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.they_already_earth"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.earth")) {
					PluginTools.sendMessage(player, "General.no_perms_earth");
					return;
				}
				sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.you_add_earth"));
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.add_you_earth"));
				BendingPlayer.getBendingPlayer(targetplayer).addBender(BendingType.Earth);
				return;
			}
			if (Arrays.asList(this.waterbendingAliases).contains(choice)) {
				if (EntityTools.isBender(targetplayer, BendingType.Water)) {
					sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.they_already_water"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.water")) {
					PluginTools.sendMessage(player, "General.no_perms_water");
				}
				sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.you_add_water"));
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.add_you_water"));
				BendingPlayer.getBendingPlayer(targetplayer).addBender(BendingType.Water);
				return;
			}
			if (Arrays.asList(this.chiblockingAliases).contains(choice)) {
				if (EntityTools.isBender(targetplayer, BendingType.ChiBlocker)) {
					sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.they_already_chi"));
					return;
				}
				if (!hasHelpPermission(targetplayer, "bending.chiblocking")) {
					PluginTools.sendMessage(player, "General.no_perms_chi");
					return;
				}
				sendMessage(player, targetplayer.getName() + " " + PluginTools.getMessage(player, "General.you_add_chi"));
				sendMessage(targetplayer, senderName + " " + PluginTools.getMessage(targetplayer, "General.add_you_chi"));
				BendingPlayer.getBendingPlayer(targetplayer).addBender(BendingType.ChiBlocker);
				return;
			}
			printAddUsage(player);
		}
	}

	private void specialize (final Player player, final String[] args) {
		boolean toggled = false;
		if (args.length == 2) {
			toggled = toggleSpe(player, args);
			if (toggled) {
				return;
			}
		}
		if (!hasPermission(player, "bending.admin.specialize")) {
			return;
		}
		// If no args, just list
		if (args.length == 1) {
			for (final BendingSpecializationType spe : BendingSpecializationType.values()) {
				final ChatColor color = PluginTools.getColor(ConfigManager.getColor(spe.getElement().name()));
				sendMessage(player, color + spe.name());
			}
			return;
		}
		final String subAction = args[1];
		if (subAction.equals("set")) {
			if (args.length < 3) {
				printSpecializationUsage(player);
			}
			final String choice = args[2].toLowerCase();
			final BendingSpecializationType spe = BendingSpecializationType.getType(choice);
			if (spe == null) {
				PluginTools.sendMessage(player, "General.bad_specialization");
				return;
			}
			BendingPlayer bPlayer = null;
			if (args.length == 4) {
				final String playername = args[3];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					// TODO unknown player
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			}
			else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			if (!bPlayer.isBender(spe.getElement())) {
				PluginTools.sendMessage(player, "General.bad_specialization_element");
				return;
			}
			bPlayer.setSpecialization(spe);
			return;
		}
		else if (subAction.equals("remove")) {
			if (args.length < 3) {
				printSpecializationUsage(player);
			}
			final String choice = args[2].toLowerCase();
			final BendingSpecializationType spe = BendingSpecializationType.getType(choice);
			if (spe == null) {
				PluginTools.sendMessage(player, "General.bad_specialization");
				return;
			}
			BendingPlayer bPlayer = null;
			if (args.length == 4) {
				final String playername = args[3];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					// TODO unknown player
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			}
			else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			bPlayer.removeSpecialization(spe);
			return;
		}
		else if (subAction.equals("add")) {
			if (args.length < 3) {
				printSpecializationUsage(player);
			}
			final String choice = args[2].toLowerCase();
			final BendingSpecializationType spe = BendingSpecializationType.getType(choice);
			if (spe == null) {
				PluginTools.sendMessage(player, "General.bad_specialization");
				return;
			}
			BendingPlayer bPlayer = null;
			if (args.length == 4) {
				final String playername = args[3];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					// TODO unknown player
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			}
			else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			if (!bPlayer.isBender(spe.getElement())) {
				PluginTools.sendMessage(player, "General.bad_specialization_element");
				return;
			}
			bPlayer.addSpecialization(spe);
			return;
		}
		else if (subAction.equals("clear")) {
			BendingPlayer bPlayer = null;
			if (args.length == 3) {
				final String playername = args[2];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					// TODO unknown player
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			}
			else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			bPlayer.clearSpecialization();
			return;
		}
		printSpecializationUsage(player);
	}

	private void printClearUsage (final Player player) {
		printUsageMessage(player, "/bending clear", "General.clear_all");
		printUsageMessage(player, "/bending clear <slot#>", "General.clear_slot");
		printUsageMessage(player, "/bending clear <item>", "General.clear_item");
	}

	private void clear (final Player player, final String[] args) {
		if (!hasPermission(player, "bending.command.clear")) {
			return;
		}
		if (player == null) {
			printNotFromConsole();
			return;
		}
		if ((args.length != 1) && (args.length != 2)) {
			printClearUsage(player);
		}
		if (args.length == 1) {
			BendingPlayer.getBendingPlayer(player).clearAbilities();
			PluginTools.sendMessage(player, "General.cleared_message");
		}
		else if (args.length == 2) {
			try {
				final int slot = Integer.parseInt(args[1]);
				if ((slot > 0) && (slot < 10)) {
					BendingPlayer.getBendingPlayer(player).removeAbility(slot - 1);
					sendMessage(
							player,
							PluginTools.getMessage(player, "General.slot") + " " + args[1] + " "
									+ PluginTools.getMessage(player, "General.slot_item_cleared"));
					return;
				}
				printClearUsage(player);
				return;
			}
			catch (final NumberFormatException e) {
				final Material mat = Material.matchMaterial(args[1]);
				if (mat != null) {
					BendingPlayer.getBendingPlayer(player).removeAbility(Material.matchMaterial(args[1]));
					sendMessage(player, "Item " + args[1] + " " + PluginTools.getMessage(player, "General.slot_item_cleared"));
					return;
				}
				else {
					printClearUsage(player);
					return;
				}
			}
		}
	}

	private void printBindUsage (final Player player) {
		final boolean item = this.bPlayer.getBendToItem();
		if (!item) {
			printUsageMessage(player, "/bending bind <ability>", "General.bind_slot");
		}
		else {
			printUsageMessage(player, "/bending bind <ability>", "General.bind_item");
		}
		printUsageMessage(player, "/bending bind <ability> <slot#>", "General.bind_to_slot");
		printUsageMessage(player, "/bending bind <ability> <item>", "General.bind_to_item");
	}

	private void bind (final Player player, final String[] args) {
		if (!hasPermission(player, "bending.command.bind")) {
			return;
		}
		if (player == null) {
			printNotFromConsole();
			return;
		}
		if ((args.length != 2) && (args.length != 3)) {
			printBindUsage(player);
			return;
		}
		final String a = args[1];
		final Abilities ability = Abilities.getAbility(a);
		if (ability == null) {
			printBindUsage(player);
			return;
		}
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (!EntityTools.hasPermission(player, ability)) {
			printNoPermissions(player);
			return;
		}
		if (ability.isSpecialization()) {
			if (!EntityTools.isSpecialized(player, ability.getSpecialization())) {
				printNoPermissions(player);
				return;
			}
		}
		int slot = player.getInventory().getHeldItemSlot();
		Material mat = player.getInventory().getItemInHand().getType();
		boolean item = bPlayer.getBendToItem();
		if (args.length == 3) {
			try {
				slot = Integer.parseInt(args[2]);
				if ((slot <= 0) || (slot >= 10)) {
					printBindUsage(player);
					return;
				}
				slot--;
			}
			catch (final NumberFormatException e) {
				mat = Material.matchMaterial(args[2]);
				if (mat == null) {
					printBindUsage(player);
					return;
				}
				item = true;
			}
		}
		ChatColor color = ChatColor.WHITE;
		final ChatColor white = ChatColor.WHITE;
		if (Abilities.isWaterbending(ability)) {
			if (!EntityTools.isBender(player, BendingType.Water)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = PluginTools.getColor(ConfigManager.getColor("Water"));
				PluginTools.sendMessage(player, color2, "General.not_water");
				return;
			}
			color = PluginTools.getColor(ConfigManager.getColor("Water"));
			if (!item) {
				BendingPlayer.getBendingPlayer(player).setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white + " bound to slot " + (slot + 1));
			}
			else {
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white + " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isAirbending(ability)) {
			if (!EntityTools.isBender(player, BendingType.Air)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = PluginTools.getColor(ConfigManager.getColor("Air"));
				PluginTools.sendMessage(player, color2, "General.not_air");
				return;
			}
			color = PluginTools.getColor(ConfigManager.getColor("Air"));
			if (!item) {
				BendingPlayer.getBendingPlayer(player).setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white + " bound to slot " + (slot + 1));
			}
			else {
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white + " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isEarthbending(ability)) {
			if (!EntityTools.isBender(player, BendingType.Earth)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = PluginTools.getColor(ConfigManager.getColor("Earth"));
				PluginTools.sendMessage(player, color2, "General.not_earth");
				return;
			}
			color = PluginTools.getColor(ConfigManager.getColor("Earth"));
			if (!item) {
				BendingPlayer.getBendingPlayer(player).setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white + " bound to slot " + (slot + 1));
			}
			else {
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white + " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isChiBlocking(ability)) {
			if (!EntityTools.isBender(player, BendingType.ChiBlocker)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
				PluginTools.sendMessage(player, color2, "General.not_chi");
				return;
			}
			color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
			if (!item) {
				BendingPlayer.getBendingPlayer(player).setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white + " bound to slot " + (slot + 1));
			}
			else {
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white + " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (Abilities.isFirebending(ability)) {
			if (!EntityTools.isBender(player, BendingType.Fire)) {
				ChatColor color2 = ChatColor.WHITE;
				color2 = PluginTools.getColor(ConfigManager.getColor("Fire"));
				PluginTools.sendMessage(player, color2, "General.not_fire");
				return;
			}
			color = PluginTools.getColor(ConfigManager.getColor("Fire"));
			if (!item) {
				BendingPlayer.getBendingPlayer(player).setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white + " bound to slot " + (slot + 1));
			}
			else {
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white + " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (ability == Abilities.AvatarState) {
			if (!hasPermission(player, "bending.admin.avatarstate")) {
				return;
			}
			color = ChatColor.DARK_PURPLE;
			if (!item) {
				BendingPlayer.getBendingPlayer(player).setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white + " bound to slot " + (slot + 1));
			}
			else {
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white + " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
		if (ability == Abilities.AstralProjection) {
			if (!hasPermission(player, "bending.admin.AstralProjection")) {
				return;
			}
			color = ChatColor.DARK_PURPLE;
			if (!item) {
				BendingPlayer.getBendingPlayer(player).setAbility(slot, ability);
				sendMessage(player, color + ability.name() + white + " bound to slot " + (slot + 1));
			}
			else {
				BendingPlayer.getBendingPlayer(player).setAbility(mat, ability);
				sendMessage(player, color + ability.name() + white + " bound to " + mat.name().replaceAll("_", " "));
			}
			return;
		}
	}

	private void save (final Player player) {
		if (player == null) {
			return;
		}
		if (player.hasPermission("bending.command.save")) {
			Bending.backup.setPlayer(player.getUniqueId(), BendingPlayer.getBendingPlayer(player));
			player.sendMessage(ChatColor.GREEN + "Bending properly saved.");
		}
		else {
			player.sendMessage(ChatColor.RED + "You're not allowed to do that.");
		}
	}

	private void getback (final Player player) {
		if (player == null) {
			return;
		}
		if (player.hasPermission("bending.command.getback")) {
			final BendingPlayer bPl = BendingPlayer.getBendingPlayer(player);
			final BendingPlayerData plData = Bending.backup.get(player.getUniqueId());
			bPl.removeBender();
			for (final BendingType data : plData.getBendings()) {
				Bending.log.info("Adding " + data.toString());
				bPl.addBender(data);
			}
			for (final BendingSpecializationType spe : plData.getSpecialization()) {
				Bending.log.info("Adding " + spe.toString());
				bPl.addSpecialization(spe);
			}
			for (final Entry<Integer, Abilities> data : plData.getSlotAbilities().entrySet()) {
				bPl.setAbility(data.getKey(), data.getValue());
			}
			player.sendMessage(ChatColor.GREEN + "Bending properly set back");
		}
		else {
			player.sendMessage(ChatColor.RED + "You're not allowed to do that.");
		}
	}

	private boolean hasPermission (final Player player, final String permission) {
		if (player == null) {
			return true;
		}
		if (player.hasPermission(permission)) {
			return true;
		}
		printNoPermissions(player);
		return false;
	}

	private boolean hasHelpPermission (final Player player, final String permission) {
		if (player == null) {
			return true;
		}
		if (player.hasPermission(permission)) {
			return true;
		}
		return false;
	}

	private void printHelpDialogue (final Player player) {
		PluginTools.sendMessage(player, ChatColor.RED, "General.help_list");
		PluginTools.sendMessage(player, ChatColor.RED, "General.command_list");
		PluginTools.sendMessage(player, ChatColor.RED, "General.ability_list");
	}

	private void sendNoCommandPermissionMessage (final Player player, final String command) {
		sendMessage(player, PluginTools.getMessage(player, "General.no_use_perms") + " /bending " + command + ".");
	}

	private void printCommands (final Player player) {
		sendMessage(player, "Bending aliases: bending bend b mtla tla");
		String slot = PluginTools.getMessage(player, "General.slot") + "#";
		if (player != null) {
			if (this.bPlayer.getBendToItem()) {
				slot = "item";
			}
		}
		if (hasHelpPermission(player, "bending.command.bind")) {
			sendMessage(player, "/bending bind <ability> [" + slot + "]");
		}
		if (hasHelpPermission(player, "bending.command.clear")) {
			sendMessage(player, "/bending clear [" + slot + "]");
		}
		if (hasHelpPermission(player, "bending.admin.choose")) {
			sendMessage(player, "/bending choose [player] <element>");
		}
		else if (hasHelpPermission(player, "bending.command.choose") || hasHelpPermission(player, "bending.admin.rechoose")) {
			sendMessage(player, "/bending choose <element>");
		}
		if (hasHelpPermission(player, "bending.admin.add")) {
			sendMessage(player, "/bending add [player] <element>");
		}
		if (hasHelpPermission(player, "bending.admin.remove")) {
			sendMessage(player, "/bending remove <player1> [player2] [player3] ...");
		}
		if (hasHelpPermission(player, "bending.admin.permaremove")) {
			sendMessage(player, "/bending permaremove <player1> [player2] [player3] ...");
		}
		if (hasHelpPermission(player, "bending.admin.toggle")) {
			sendMessage(player, "/bending toggle [player]");
		}
		else if (hasHelpPermission(player, "bending.command.toggle")) {
			sendMessage(player, "/bending toggle");
		}
		if (hasHelpPermission(player, "bending.command.display")) {
			sendMessage(player, "/bending display [element]");
		}
		if (hasHelpPermission(player, "bending.admin.reload")) {
			sendMessage(player, "/bending reload");
		}
		if (hasHelpPermission(player, "bending.admin.import")) {
			sendMessage(player, "/bending import");
		}
		if (hasHelpPermission(player, "bending.command.who")) {
			sendMessage(player, "/bending who [player]");
		}
		if (hasHelpPermission(player, "bending.command.language")) {
			sendMessage(player, "/bending language [language]");
		}
		if (hasHelpPermission(player, "bending.command.bindmode")) {
			sendMessage(player, "/bending bindmode [slot/item]");
		}
		if (hasHelpPermission(player, "bending.command.version")) {
			sendMessage(player, "/bending version");
		}
	}

	private Player getOnlinePlayer (final String name) {
		for (final Player p : this.server.getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}
}

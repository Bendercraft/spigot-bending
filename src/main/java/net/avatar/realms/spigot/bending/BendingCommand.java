package net.avatar.realms.spigot.bending;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

@Deprecated
public class BendingCommand {

	private static final String[] clearAliases = { "clear", "cl" };
	private static final String[] specializeAliases = { "specialize", "spe" };
	private static final String[] pathAliases = { "path", "p" };
	private static final String[] helpAliases = { "help", "h", "?" };
	private final Server server;
	private boolean verbose = true;
	private BendingPlayer bPlayer;

	public BendingCommand(final Player player, String[] args, final Server server) {
		this.server = server;
		if (player != null) {
			this.bPlayer = BendingPlayer.getBendingPlayer(player);
		}
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].toLowerCase();
		}
		if (args.length >= 1) {
			if (args[args.length - 1].equalsIgnoreCase("&")) {
				this.verbose = false;
				final String[] temp = new String[args.length - 1];
				for (int i = 0; i < (args.length - 1); i++) {
					temp[i] = args[i];
				}
				args = temp;
			}
			final String arg = args[0];
			if (Arrays.asList(clearAliases).contains(arg)) {
				clear(player, args);
			} else if (Arrays.asList(specializeAliases).contains(arg)) {
				specialize(player, args);
			} else if (Arrays.asList(pathAliases).contains(arg)) {
				path(player, args);
			} else if (Arrays.asList(helpAliases).contains(arg)) {
				help(player, args);
			} else {
				printHelpDialogue(player);
			}
		} else {
			printHelpDialogue(player);
		}
	}

	private void path(Player player, String[] args) {
		if (!hasPermission(player, "bending.admin.path")) {
			return;
		}
		// If no args, just list
		if (args.length == 1) {
			for (BendingPath path : this.bPlayer.getPath()) {
				final ChatColor color = PluginTools.getColor(Settings.getColorString(path.getElement().name()));
				sendMessage(player, color + "You are " + path.name() + " for element " + path.getElement().name() + ".");
			}
			return;
		}
		final String subAction = args[1];
		if (subAction.equals("set")) {
			if (args.length < 3) {
				printPathUsage(player);
			}
			final String choice = args[2].toLowerCase();
			final BendingPath path = BendingPath.getType(choice);
			if (path == null) {
				Messages.sendMessage(player, "general.bad_path");
				return;
			}
			BendingPlayer bPlayer = null;
			if (args.length == 4) {
				final String playername = args[3];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					player.sendMessage("Player " + playername + " is unknown.");
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			} else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			if (!bPlayer.isBender(path.getElement())) {
				Messages.sendMessage(player, "general.bad_path_element");
				return;
			}
			bPlayer.setPath(path);
			return;
		} else if (subAction.equals("list")) {
			for (final BendingPath path : BendingPath.values()) {
				final ChatColor color = PluginTools.getColor(Settings.getColorString(path.getElement().name()));
				sendMessage(player, color + path.name());
			}
			return;
		}
		printPathUsage(player);
	}

	private void printUsageMessage(final Player player, final String command, final String key) {
		final ChatColor color = ChatColor.AQUA;
		final String usage = Messages.getString("general.usage");
		final String description = Messages.getString(key);
		sendMessage(player, color + usage + ": " + command);
		sendMessage(player, color + "-" + description);
	}

	private void sendMessage(final Player player, final String message) {
		if (!this.verbose) {
			return;
		}
		if (player == null) {
			Bending.log.info(message);
		} else {
			player.sendMessage(message);
		}
	}

	private void printNoPermissions(final Player player) {
		sendMessage(player, ChatColor.RED + Messages.getString("general.no_execute_perms"));
	}

	private void help(final Player player, final String[] args) {

		if (args.length > 1) {
			helpCommand(player, args);
			final BendingAbilities ability = BendingAbilities.getAbility(args[1]);
			if (ability != null) {
				ChatColor cc = ChatColor.GOLD;
				cc = PluginTools.getColor(Settings.getColorString(ability.getElement().name()));
				if (EntityTools.hasPermission(player, ability)) {
					String msg = Messages.getAbilityDescription(ability);
					sendMessage(player, ("                                                " + cc + ability.name()));
					player.sendMessage(cc + msg);
					return;
				} else {
					sendMessage(player, Messages.getString("general.no_bind_perms") + " " + cc + ability + ChatColor.WHITE + ".");
				}
			}
		} else {
			printCommands(player);
		}
	}

	private void helpCommand(final Player player, final String[] args) {
		// final ChatColor color = ChatColor.AQUA;
		// final String command = args[1];
		// if (Arrays.asList(this.bindAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.bind")) {
		// sendNoCommandPermissionMessage(player, "bind");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending bind");
		// String aliases = "";
		// for (final String alias : this.bindAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printBindUsage(player);
		// }
		// else if (Arrays.asList(this.clearAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.clear")) {
		// sendNoCommandPermissionMessage(player, "clear");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending clear");
		// String aliases = "";
		// for (final String alias : this.clearAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printClearUsage(player);
		// }
		// else if (Arrays.asList(this.chooseAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.choose") &&
		// !hasHelpPermission(player, "bending.admin.choose")
		// && !hasHelpPermission(player, "bending.admin.rechoose")) {
		// sendNoCommandPermissionMessage(player, "choose");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending choose");
		// String aliases = "";
		// for (final String alias : this.chooseAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printChooseUsage(player);
		// }
		// else if (Arrays.asList(this.addAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.add")) {
		// sendNoCommandPermissionMessage(player, "add");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending add");
		// String aliases = "";
		// for (final String alias : this.addAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printAddUsage(player);
		// }
		// else if (Arrays.asList(this.specializeAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.admin.specialize")) {
		// sendNoCommandPermissionMessage(player, "specialize");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending specialize");
		// String aliases = "";
		// for (final String alias : this.specializeAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printAddUsage(player);
		// }
		// else if (Arrays.asList(this.removeAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.remove")) {
		// sendNoCommandPermissionMessage(player, "remove");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending remove");
		// String aliases = "";
		// for (final String alias : this.removeAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printRemoveUsage(player);
		// }
		// else if (Arrays.asList(this.toggleAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.toggle")) {
		// sendNoCommandPermissionMessage(player, "toggle");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending toggle");
		// String aliases = "";
		// for (final String alias : this.toggleAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printToggleUsage(player);
		// }
		// else if (Arrays.asList(this.displayAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.display")) {
		// sendNoCommandPermissionMessage(player, "display");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending display");
		// String aliases = "";
		// for (final String alias : this.displayAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printDisplayUsage(player);
		// }
		// else if (Arrays.asList(this.reloadAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.reload")) {
		// sendNoCommandPermissionMessage(player, "reload");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending reload");
		// String aliases = "";
		// for (final String alias : this.reloadAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printReloadUsage(player);
		// }
		// else if (Arrays.asList(this.importAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.import")) {
		// sendNoCommandPermissionMessage(player, "import");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending import");
		// String aliases = "";
		// for (final String alias : this.importAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printImportUsage(player);
		// }
		// else if (Arrays.asList(this.whoAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.who")) {
		// sendNoCommandPermissionMessage(player, "who");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending who");
		// String aliases = "";
		// for (final String alias : this.whoAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printWhoUsage(player);
		// }
		// else if (Arrays.asList(this.languageAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.language")) {
		// sendNoCommandPermissionMessage(player, "language");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending language");
		// String aliases = "";
		// for (final String alias : this.languageAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printLanguageUsage(player);
		// }
		// else if (Arrays.asList(this.versionAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.version")) {
		// sendNoCommandPermissionMessage(player, "version");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending version");
		// String aliases = "";
		// for (final String alias : this.versionAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printVersionUsage(player);
		// }
		// else if (Arrays.asList(this.bindModeAliases).contains(command)) {
		// if (!hasHelpPermission(player, "bending.command.bindmode")) {
		// sendNoCommandPermissionMessage(player, "bindmode");
		// return;
		// }
		// sendMessage(player, color + "Command: /bending bindmode");
		// String aliases = "";
		// for (final String alias : this.bindModeAliases) {
		// aliases = aliases + alias + " ";
		// }
		// sendMessage(player, color + "Aliases: " + aliases);
		// printBindModeUsage(player);
		// }
	}

	// private void printReloadUsage(final Player player) {
	// if (!hasHelpPermission(player, "bending.admin.reload")) {
	// sendNoCommandPermissionMessage(player, "reload");
	// return;
	// }
	// else {
	// printUsageMessage(player, "/bending reload", "General.reload_usage");
	// }
	// }

	private void printNotFromConsole() {
		Messages.sendMessage(null, "General.not_from_console");
	}

	private void printSpecializationUsage(final Player player) {
		printUsageMessage(player, "/bending spe", "General.spe_list");
		if (player != null) {
			printUsageMessage(player, "/bending spe set <specialization>", "General.spe_set_self");
			printUsageMessage(player, "/bending spe add <specialization>", "General.spe_add_self");
			printUsageMessage(player, "/bending spe remove <specialization>", "General.spe_remove_self");
			printUsageMessage(player, "/bending spe clear", "General.spe_clear_self");
		} else {
			printUsageMessage(player, "/bending spe set <specialization> <player>", "General.spe_set_other");
			printUsageMessage(player, "/bending spe add <specialization> <player>", "General.spe_add_other");
			printUsageMessage(player, "/bending spe remove <specialization> <player>", "General.spe_remove_self");
			printUsageMessage(player, "/bending spe clear <player>", "General.spe_clear_self");
		}
	}

	private void printPathUsage(final Player player) {
		printUsageMessage(player, "/bending path", "General.path_list");
		if (player != null) {
			printUsageMessage(player, "/bending path set <path>", "General.path_set_self");
		} else {
			printUsageMessage(player, "/bending path set <path> <player>", "General.path_set_other");
		}
	}

	private void specialize(final Player player, final String[] args) {

		if (!hasPermission(player, "bending.admin.specialize")) {
			return;
		}
		// If no args, just list
		if (args.length == 1) {
			for (final BendingAffinity spe : BendingAffinity.values()) {
				final ChatColor color = PluginTools.getColor(Settings.getColorString(spe.getElement().name()));
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
			final BendingAffinity spe = BendingAffinity.getType(choice);
			if (spe == null) {
				Messages.sendMessage(player, "general.bad_specialization");
				return;
			}
			BendingPlayer bPlayer = null;
			if (args.length == 4) {
				final String playername = args[3];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					player.sendMessage("Player " + playername + " is unknown.");
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			} else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			if (!bPlayer.isBender(spe.getElement())) {
				Messages.sendMessage(player, "general.bad_specialization_element");
				return;
			}
			bPlayer.setSpecialization(spe);
			return;
		} else if (subAction.equals("remove")) {
			if (args.length < 3) {
				printSpecializationUsage(player);
			}
			final String choice = args[2].toLowerCase();
			final BendingAffinity spe = BendingAffinity.getType(choice);
			if (spe == null) {
				Messages.sendMessage(player, "general.bad_specialization");
				return;
			}
			BendingPlayer bPlayer = null;
			if (args.length == 4) {
				final String playername = args[3];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					player.sendMessage("Player " + playername + " is unknown.");
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			} else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			bPlayer.removeSpecialization(spe);
			return;
		} else if (subAction.equals("add")) {
			if (args.length < 3) {
				printSpecializationUsage(player);
			}
			final String choice = args[2].toLowerCase();
			final BendingAffinity spe = BendingAffinity.getType(choice);
			if (spe == null) {
				Messages.sendMessage(player, "general.bad_specialization");
				return;
			}
			BendingPlayer bPlayer = null;
			if (args.length == 4) {
				final String playername = args[3];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					player.sendMessage("Player " + playername + " is unknown.");
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			} else {
				bPlayer = BendingPlayer.getBendingPlayer(player);
			}
			if (bPlayer == null) {
				// Wut !
				return;
			}
			if (!bPlayer.isBender(spe.getElement())) {
				Messages.sendMessage(player, "general.bad_specialization_element");
				return;
			}
			bPlayer.addSpecialization(spe);
			return;
		} else if (subAction.equals("clear")) {
			BendingPlayer bPlayer = null;
			if (args.length == 3) {
				final String playername = args[2];
				final Player targetplayer = getOnlinePlayer(playername);
				if (targetplayer == null) {
					player.sendMessage("Player " + playername + " is unknown.");
					return;
				}
				bPlayer = BendingPlayer.getBendingPlayer(targetplayer);
			} else {
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

	private void printClearUsage(final Player player) {
		printUsageMessage(player, "/bending clear", "General.clear_all");
		printUsageMessage(player, "/bending clear <slot#>", "General.clear_slot");
		printUsageMessage(player, "/bending clear <item>", "General.clear_item");
	}

	private void clear(final Player player, final String[] args) {
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
			Messages.sendMessage(player, "General.cleared_message");
		} else if (args.length == 2) {
			try {
				final int slot = Integer.parseInt(args[1]);
				if ((slot > 0) && (slot < 10)) {
					BendingPlayer.getBendingPlayer(player).removeAbility(slot - 1);
					sendMessage(player, Messages.getString("general.slot") + " " + args[1] + " " + Messages.getString("general.slot_item_cleared"));
					return;
				}
				printClearUsage(player);
				return;
			} catch (final NumberFormatException e) {

			}
		}
	}

	private boolean hasPermission(final Player player, final String permission) {
		if (player == null) {
			return true;
		}
		if (player.hasPermission(permission)) {
			return true;
		}
		printNoPermissions(player);
		return false;
	}

	private boolean hasHelpPermission(final Player player, final String permission) {
		if (player == null) {
			return true;
		}
		if (player.hasPermission(permission)) {
			return true;
		}
		return false;
	}

	private void printHelpDialogue(final Player player) {
		Messages.sendMessage(player, "General.help_list", ChatColor.RED);
		Messages.sendMessage(player, "General.command_list", ChatColor.RED);
		Messages.sendMessage(player, "General.ability_list", ChatColor.RED);
	}

	private void printCommands(final Player player) {
		sendMessage(player, "Bending aliases: bending bend b mtla tla");
		String slot = Messages.getString("general.slot") + "#";
		if (hasHelpPermission(player, "bending.command.bind")) {
			sendMessage(player, "/bending bind <ability> [" + slot + "]");
		}
		if (hasHelpPermission(player, "bending.command.clear")) {
			sendMessage(player, "/bending clear [" + slot + "]");
		}
		if (hasHelpPermission(player, "bending.admin.choose")) {
			sendMessage(player, "/bending choose [player] <element>");
		} else if (hasHelpPermission(player, "bending.command.choose") || hasHelpPermission(player, "bending.admin.rechoose")) {
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
		} else if (hasHelpPermission(player, "bending.command.toggle")) {
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

	private Player getOnlinePlayer(final String name) {
		for (final Player p : this.server.getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}
}

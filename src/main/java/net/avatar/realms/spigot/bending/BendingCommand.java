package net.avatar.realms.spigot.bending;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

@Deprecated
public class BendingCommand {

	private static final String[] helpAliases = { "help", "h", "?" };
	private boolean verbose = true;

	public BendingCommand(final Player player, String[] args, final Server server) {
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
			if (Arrays.asList(helpAliases).contains(arg)) {
				help(player, args);
			} else {
				printHelpDialogue(player);
			}
		} else {
			printHelpDialogue(player);
		}
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
}

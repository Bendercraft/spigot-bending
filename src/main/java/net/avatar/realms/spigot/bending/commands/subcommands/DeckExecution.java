package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.commands.BendingCommand;

public class DeckExecution extends BendingCommand {

	public DeckExecution() {
		this.basePermission = "bending.command.deck";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.deck")) {
			sender.sendMessage("/bending deck <deckname>");
			sender.sendMessage("/bending deck rename <deckname> <newname>");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

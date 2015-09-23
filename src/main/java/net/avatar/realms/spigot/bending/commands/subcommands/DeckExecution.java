package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
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

		if (args.isEmpty()) {
			printUsage(sender);
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

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		List<String> values = new LinkedList<String>();
		if (!(sender instanceof Player)) {
			return values;
		}

		if (args.size() == 1 || args.size() == 2) {
			Player player = (Player) sender;
			BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
			for (String value : bender.getDecksNames()) {
				values.add(value);
			}
		}

		return values;
	}
}

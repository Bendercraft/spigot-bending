package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;

public class RemoveExecution extends BendingCommand {

	public RemoveExecution() {
		super();
		this.command = "remove";
		this.aliases.add("r");
		this.aliases.add("rem");
		this.aliases.add("rm");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!sender.hasPermission("bending.command.remove")) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}

		if (args.isEmpty()) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			printUsage(sender);
			return true;
		}

		String playerName = args.get(0);
		Player player = getPlayer(playerName);

		if (player == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return true;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		bender.removeBender();

		player.sendMessage(Messages.OTHER_REMOVE_YOU);
		String msg = Messages.YOU_REMOVE_OTHER;
		msg = msg.replaceAll("{0}", playerName);
		sender.sendMessage(msg);
		return true;
	}

	@Override
	public void printUsage(CommandSender sender) {
		if (sender.hasPermission("bending.command.remove")) {
			sender.sendMessage(ChatColor.RED + "/bending remove <player>");
		}
		else {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
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
		this.basePermission = "bending.command.remove";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

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
		msg = msg.replaceAll("\\{0\\}", playerName);
		sender.sendMessage(msg);
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.remove")) {
			sender.sendMessage(ChatColor.RED + "/bending remove <player>");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		LinkedList<String> values = new LinkedList<String>();

		if (sender.hasPermission("bending.command.remove")) {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				values.add(player.getName());
			}
		}
		return values;
	}
}

package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;

public class AddExecution extends BendingCommand {

	public AddExecution() {
		super();
		this.command = "add";
		this.aliases.add("a");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!sender.hasPermission("bending.command.add")) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}

		if ((args.size() != 1) && (args.size() != 2)) {
			printUsage(sender);
			return true;
		}

		if (!(sender instanceof Player) && args.size() != 2) {
			sender.sendMessage(Messages.CONSOLE_SPECIFY_PLAYER);
			return true;
		}

		Player player;

		if (args.size() == 2) {
			if (!sender.hasPermission("bending.command.add.other")) {
				sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
				return true;
			}
			player = getPlayer(args.remove(0));
		} else {
			player = (Player) sender;
		}

		if (player == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return true;
		}

		final String choice = args.remove(0);
		BendingElement element = getElement(choice);
		if (element == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_ELEMENT);
			return true;
		}

		if (!player.hasPermission("bending." + element.name().toLowerCase())) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_ELEMENT_ABLE);
			return true;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (bender.isBender(element)) {
			sender.sendMessage(ChatColor.RED + Messages.ALREADY_ELEMENT);
			return true;
		}

		bender.addBender(element);
		String msg = Messages.YOU_ADDED;
		msg = msg.replaceAll("\\{0\\}", element.name());
		msg = msg.replaceAll("\\{1\\}", player.getName());
		sender.sendMessage(msg);
		msg = Messages.YOU_WERE_ADDED;
		msg = msg.replaceAll("\\{0\\}", element.name());
		player.sendMessage(msg);

		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.add")) {
			sender.sendMessage("/bending add [player] <element>");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		List<String> values = new LinkedList<String>();
		if (!sender.hasPermission("bending.command.add")) {
			return values;
		}

		for (BendingElement el : BendingElement.values()) {
			values.add(el.name());
		}

		if (args.size() == 2 || !sender.hasPermission("bending.command.add.other")) {
			return values;
		}

		for (Player online : Bukkit.getServer().getOnlinePlayers()) {
			values.add(online.getName());
		}

		return values;
	}

}

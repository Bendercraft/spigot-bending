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
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class ChooseExecution extends BendingCommand {

	public ChooseExecution() {
		super();
		this.command = "choose";
		this.aliases.add("ch");
		this.basePermission = "bending.command.choose";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if ((args.size() != 1) && (args.size() != 2)) {
			printUsage(sender);
			return true;
		}
		Player target;
		boolean other = false;
		if (args.size() == 1) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Messages.CONSOLE_SPECIFY_PLAYER);
				return true;
			}
			target = (Player) sender;
		} else {
			if (!sender.hasPermission("bending.command.choose.other")) {
				sender.sendMessage(ChatColor.RED + Messages.ERROR_CHOOSE_OTHER);
				return true;
			}
			target = getPlayer(args.remove(0));
			other = true;
		}

		if (target == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return true;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);
		if (bender.isBender()) {
			if (!other) {
				if (!sender.hasPermission("bending.command.rechoose")) {
					sender.sendMessage(ChatColor.RED + Messages.ERROR_CHANGE_ELEMENT);
					return true;
				}
			}
		}

		String choice = args.remove(0);
		BendingElement element = getElement(choice);

		if (element == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_ELEMENT);
			return true;
		}

		bender.setBender(element);

		ChatColor color = PluginTools.getColor(Settings.getColorString(element.name()));
		if (other) {
			String msg = Messages.YOU_CHANGE_OTHER;
			msg = msg.replaceAll("\\{0\\}", target.getName());
			msg = msg.replaceAll("\\{1\\}", element.name());
			sender.sendMessage(color + msg);
			msg = Messages.OTHER_CHANGE_YOU;
			msg = msg.replaceAll("\\{0\\}", element.name());
			target.sendMessage(color + msg);
		} else {
			String msg = Messages.SET_ELEMENT;
			msg = msg.replaceAll("\\{0\\}", element.name());
			target.sendMessage(color + msg);
		}

		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.admin.choose") || sender.hasPermission("bending.admin.rechoose")) {
			sender.sendMessage("/bending choose [player] <element>");
		}
		else if (sender.hasPermission("bending.command.choose")) {
			sender.sendMessage("/bending choose <element>");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		List<String> values = new LinkedList<String>();
		if (!sender.hasPermission("bending.command.choose")) {
			return values;
		}

		for (BendingElement el : BendingElement.values()) {
			values.add(el.name());
		}

		if (args.size() == 2 || !sender.hasPermission("bending.command.choose.other")) {
			return values;
		}

		for (Player online : Bukkit.getServer().getOnlinePlayers()) {
			values.add(online.getName());
		}

		return values;
	}
}

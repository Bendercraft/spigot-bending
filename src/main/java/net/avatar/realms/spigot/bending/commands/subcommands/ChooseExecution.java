package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.commands.BendingCommands;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class ChooseExecution extends BendingCommand {

	public ChooseExecution() {
		super();
		this.command = "choose";
		this.aliases.add("ch");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!sender.hasPermission("bending.command.choose")) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}

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
		}
		else {
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
		BendingType element = BendingType.None;
		if (Arrays.asList(BendingCommands.AIRBENDING_ALIASES).contains(choice)) {
			element = BendingType.Air;
		}
		else if (Arrays.asList(BendingCommands.EARTHBENDING_ALIASES).contains(choice)) {
			element = BendingType.Earth;
		}
		else if (Arrays.asList(BendingCommands.FIREBENDING_ALIASES).contains(choice)) {
			element = BendingType.Fire;
		}
		else if (Arrays.asList(BendingCommands.WATERBENDING_ALIASES).contains(choice)) {
			element = BendingType.Water;
		}
		else if (Arrays.asList(BendingCommands.CHIBLOCKING_ALIASES).contains(choice)) {
			element = BendingType.ChiBlocker;
		}

		if (element == BendingType.None) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_ELEMENT);
			return true;
		}

		bender.setBender(element);

		ChatColor color = PluginTools.getColor(Settings.getColorString(element.name()));
		if (other) {
			String msg = Messages.YOU_CHANGE_OTHER;
			msg = msg.replaceAll("{0}", target.getName());
			msg = msg.replaceAll("{1}", element.name());
			sender.sendMessage(color + msg);
			msg = Messages.OTHER_CHANGE_YOU;
			msg = msg.replaceAll("{0}", element.name());
			target.sendMessage(color + msg);
		}
		else {
			String msg = Messages.SET_ELEMENT;
			msg = msg.replaceAll("{0}", element.name());
			target.sendMessage(color + msg);
		}

		return true;
	}

	@Override
	public void printUsage(CommandSender sender) {
		if (sender.hasPermission("bending.admin.choose") || sender.hasPermission("bending.admin.rechoose")) {
			sender.sendMessage("/bending choose [player] <element>");
		}
		else if (sender.hasPermission("bending.command.choose")) {
			sender.sendMessage("/bending choose <element>");
		}
		else {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

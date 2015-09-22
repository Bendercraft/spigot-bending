package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class PathExecution extends BendingCommand {

	public PathExecution() {
		super();
		this.command = "path";
		this.aliases.add("p");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!args.isEmpty() && args.size() != 2 && args.size() != 3) {
			printUsage(sender);
			return true;
		}

		Player target = null;
		if (args.size() == 3) {
			target = getPlayer(args.remove(1));
		}
		else {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
				return true;
			}
			target = (Player) sender;
		}

		if (target == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return true;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);

		if (args.isEmpty()) {
			for (BendingPath path : bender.getPath()) {
				final ChatColor color = PluginTools.getColor(Settings.getColorString(path.getElement().name()));
				sender.sendMessage(color + "You are " + path.name() + " for element " + path.getElement().name() + ".");
			}
		}
		else {
			BendingPath path = BendingPath.getType(args.get(1));
			if (path == null) {
				sender.sendMessage(ChatColor.RED + Messages.INVALID_PATH);
				return true;
			}

			if (!bender.isBender(path.getElement())) {
				sender.sendMessage(ChatColor.RED + Messages.INVALID_PATH_ELEMENT);
				return true;
			}

			bender.setPath(path);
			String msg = Messages.PATH_SET;
			msg = msg.replaceAll("\\{0\\}", path.name());
			target.sendMessage(msg);

			msg = Messages.YOU_SET_PATH;
			msg.replaceAll("\\{0\\}", path.name());
			msg.replaceAll("\\{1\\}", target.getName());
			sender.sendMessage(msg);
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.path")) {
			sender.sendMessage("/bending path");
			if (sender.hasPermission("bending.command.path.admin")) {
				sender.sendMessage("/bending path set [player] <path>");
			}
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		// TODO Auto-generated method stub
		return null;
	}

}

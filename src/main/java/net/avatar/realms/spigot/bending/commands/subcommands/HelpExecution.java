package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.commands.IBendingCommand;

public class HelpExecution extends BendingCommand {

	private List<IBendingCommand> commands;

	public HelpExecution(List<IBendingCommand> commands) {
		super();
		this.commands = commands;
		this.command = "help";
		this.aliases.add("?");
		this.aliases.add("h");
		this.basePermission = "bending.command.help";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (args.isEmpty()) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
				return true;
			}
			for (IBendingCommand cmd : this.commands) {
				cmd.printUsage(sender, false);
			}
		}
		else {
			String subCommand = args.remove(0);
			for (IBendingCommand cmd : this.commands) {
				if (cmd.isCommand(subCommand)) {
					cmd.printUsage(sender);
					break;
				}
			}
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.help")) {
			sender.sendMessage("/bending help [command]");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		LinkedList<String> values = new LinkedList<String>();
		if (args.size() == 1) {
			for (IBendingCommand cmd : this.commands) {
				if (sender.hasPermission("bending.command." + cmd.getCommand())) {
					values.add(cmd.getCommand());
				}
			}
		}
		return values;
	}
}

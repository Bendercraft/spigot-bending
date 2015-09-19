package net.avatar.realms.spigot.bending.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.commands.subcommands.BindExecution;

public class BendingCommandExecutor implements CommandExecutor {

	private IBendingCommand bind;

	public BendingCommandExecutor () {
		this.bind = new BindExecution();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender == null) {
			return false;
		}
		if (args.length < 1) {
			return false;
		}

		List<String> argList = Arrays.asList(args);
		String subCommand = argList.remove(0);
		if (Arrays.asList(BendingCommands.BIND_ALIASES).contains(subCommand)) {
			return this.bind.execute(sender, argList);
		}
		return false;
	}
}

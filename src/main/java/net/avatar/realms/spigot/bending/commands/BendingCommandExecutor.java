package net.avatar.realms.spigot.bending.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.commands.subcommands.BindExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.ChooseExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.RemoveExecution;

public class BendingCommandExecutor implements CommandExecutor {

	private IBendingCommand bind;
	private IBendingCommand choose;
	private IBendingCommand remove;

	public BendingCommandExecutor () {
		this.bind = new BindExecution();
		this.choose = new ChooseExecution();
		this.remove = new RemoveExecution();
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
		else if (Arrays.asList(BendingCommands.CHOOSE_ALIASES).contains(subCommand)) {
			return this.choose.execute(sender, argList);
		}
		else if (Arrays.asList(BendingCommands.REMOVE_ALIASES).contains(subCommand)) {
			return this.remove.execute(sender, argList);
		}
		return false;
	}
}

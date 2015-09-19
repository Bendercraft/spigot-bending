package net.avatar.realms.spigot.bending.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.commands.subcommands.BindExecution;

public class BendingCommandExecutor implements CommandExecutor {

	private CommandExecutor bind;

	public BendingCommandExecutor() {
		bind = new BindExecution();
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return false;
	}
}

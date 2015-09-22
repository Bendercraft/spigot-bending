package net.avatar.realms.spigot.bending.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface IBendingCommand {

	boolean execute(CommandSender sender, List<String> args);

	void printUsage(CommandSender sender);

	boolean isCommand(String command);

	String getCommand();

	/**
	 * Get all possibles values for a given command and given arguments
	 * 
	 * @param sender
	 *            The sender of the command
	 * @param args
	 *            The args that the sender sent (without the first arg being the
	 *            subcommand)
	 * @return List<String> of possible values
	 */
	public List<String> autoComplete(CommandSender sender, List<String> args);

}

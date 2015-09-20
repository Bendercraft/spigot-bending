package net.avatar.realms.spigot.bending.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface IBendingCommand {

	boolean execute(CommandSender sender, List<String> args);

	void printUsage(CommandSender sender);

	boolean isCommand(String command);

	String getCommand();

}

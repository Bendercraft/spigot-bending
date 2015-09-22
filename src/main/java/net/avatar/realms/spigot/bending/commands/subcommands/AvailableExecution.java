package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.commands.BendingCommand;

public class AvailableExecution extends BendingCommand {

	public AvailableExecution() {
		super();
		this.command = "available";
		this.aliases.add("availables");
		this.aliases.add("avail");

	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		// TODO Auto-generated method stub

	}
}

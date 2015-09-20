package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.commands.IBendingCommand;
import net.avatar.realms.spigot.bending.learning.LearningCommand;

public class LearningExecution implements IBendingCommand {

	@Override
	public boolean execute (CommandSender sender, List<String> args) {
		new LearningCommand(Bending.plugin.learning, sender, args);
		return true;
	}

	@Override
	public void printUsage (CommandSender sender) {
		// TODO Auto-generated method stub
		
	}

}

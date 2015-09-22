package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.learning.LearningCommand;

public class LearningExecution extends BendingCommand {

	public LearningExecution() {
		super();
		this.command = "learning";
		this.aliases.add("learn");
		this.aliases.add("l");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		new LearningCommand(Bending.plugin.learning, sender, args);
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		// TODO Auto-generated method stub
		return null;
	}

}

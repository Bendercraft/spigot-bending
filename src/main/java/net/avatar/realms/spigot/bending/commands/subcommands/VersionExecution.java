package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.commands.BendingCommand;

public class VersionExecution extends BendingCommand {

	public VersionExecution() {
		super();
		this.command = "version";
		this.aliases.add("v");
		this.aliases.add("ver");
		this.basePermission = "bending.command.version";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		sender.sendMessage("Bending v" + Bending.getInstance().getDescription().getVersion());
		sender.sendMessage("Authors : Koudja & Noko");
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.version")) {
			sender.sendMessage("/bending version");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

}

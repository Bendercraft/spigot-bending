package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.utils.PluginTools;

public class ReloadExecution extends BendingCommand {

	public ReloadExecution() {
		super();
		this.command = "reload";
		this.basePermission = "bending.command.reload";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		PluginTools.stopAllBending();
		AbilityManager.getManager().stopAllAbilities();
		AbilityManager.getManager().applyConfiguration(Bending.getInstance().getDataFolder());
		sender.sendMessage(ChatColor.GREEN + Messages.RELOADED);
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (!sender.hasPermission("bending.command.reload")) {
			sender.sendMessage("/bending reload");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

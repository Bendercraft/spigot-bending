package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.utils.TemporaryBlock;

public class ReloadExecution extends BendingCommand {

	public ReloadExecution() {
		super();
		this.command = "reload";
		this.basePermission = "bending.command.reload";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		AbilityManager.getManager().stopAllAbilities();
		TemporaryBlock.removeAll();
		AbilityManager.getManager().applyConfiguration(Bending.plugin.getDataFolder());
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

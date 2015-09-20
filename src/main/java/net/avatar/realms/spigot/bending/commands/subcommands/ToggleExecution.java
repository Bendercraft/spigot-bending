package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.commands.IBendingCommand;
import net.avatar.realms.spigot.bending.utils.EntityTools;

public class ToggleExecution implements IBendingCommand {

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		if (!sender.hasPermission("bending.command.toggle")) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}

		Player player = (Player) sender;

		if (args.size() >= 1
				&& (args.get(0).equalsIgnoreCase("spe") || args.get(0).equalsIgnoreCase("aff") || args.get(0).equalsIgnoreCase("affinity"))) {
			if (EntityTools.speToggledBenders.contains(player.getUniqueId())) {
				EntityTools.speToggledBenders.remove(player.getUniqueId());
				player.sendMessage("You toggled back your specialization");
			}
			else {
				EntityTools.speToggledBenders.add(player.getUniqueId());
				player.sendMessage("You toggled your specialization");
			}
		}
		else {
			if (!EntityTools.toggledBending.contains(player.getUniqueId())) {
				EntityTools.toggledBending.add(player.getUniqueId());
				Messages.sendMessage(player, "general.toggle_off", ChatColor.AQUA);
			}
			else {
				EntityTools.toggledBending.remove(player.getUniqueId());
				Messages.sendMessage(player, "general.toggle_on", ChatColor.AQUA);
			}
		}

		return false;
	}

	@Override
	public void printUsage(CommandSender sender) {
		if (sender.hasPermission("bending.command.toggle")) {
			sender.sendMessage("/bending toggle (spe)");
		}
	}
}

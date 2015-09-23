package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.utils.EntityTools;

public class ToggleExecution extends BendingCommand {

	public ToggleExecution() {
		super();
		this.command = "toggle";
		this.aliases.add("t");
		this.basePermission = "bending.command.toggle";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		Player player = (Player) sender;

		if (args.size() >= 1 && isAffinityToggle(args.get(0))) {
			if (EntityTools.speToggledBenders.contains(player.getUniqueId())) {
				EntityTools.speToggledBenders.remove(player.getUniqueId());
				player.sendMessage("You toggled back your specialization");
			} else {
				EntityTools.speToggledBenders.add(player.getUniqueId());
				player.sendMessage("You toggled your specialization");
			}
		} else {
			if (!EntityTools.toggledBending.contains(player.getUniqueId())) {
				EntityTools.toggledBending.add(player.getUniqueId());
				Messages.sendMessage(player, "general.toggle_off", ChatColor.AQUA);
			} else {
				EntityTools.toggledBending.remove(player.getUniqueId());
				Messages.sendMessage(player, "general.toggle_on", ChatColor.AQUA);
			}
		}

		return false;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.toggle")) {
			sender.sendMessage("/bending toggle (aff)");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	private boolean isAffinityToggle(String arg) {
		if (arg.equalsIgnoreCase("spe") || arg.equalsIgnoreCase("aff") || arg.equalsIgnoreCase("affinity")) {
			return true;
		}
		return false;
	}
}

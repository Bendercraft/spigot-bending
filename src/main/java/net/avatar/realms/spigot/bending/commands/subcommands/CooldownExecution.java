package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class CooldownExecution extends BendingCommand {

	public CooldownExecution() {
		super();
		this.command = "cooldown";
		this.aliases.add("cd");
		this.aliases.add("cooldowns");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		if (!sender.hasPermission("bending.command.cooldown")) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}
		Player player = (Player) sender;

		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (bender == null) {
			Bending.plugin.getLogger().warning("Cooldown command was not able to find bending player for " + player.getName());
			sender.sendMessage(ChatColor.RED + Messages.YOU_NO_EXIST);
			return true;
		}

		Map<BendingAbilities, Long> cooldowns = bender.getCooldowns();
		player.sendMessage("-Cooldowns :");
		if ((cooldowns == null) || cooldowns.isEmpty()) {
			player.sendMessage("--- None");
		} else {
			for (BendingAbilities ab : cooldowns.keySet()) {
				ChatColor col = ChatColor.WHITE;
				int min = (int) ((cooldowns.get(ab) / 1000) / 60);
				int sec = (int) ((((cooldowns.get(ab) / 1000.0) / 60.0) - min) * 60);
				if (!ab.isEnergyAbility()) {
					col = PluginTools.getColor(Settings.getColorString(ab.getElement().name()));
				}
				player.sendMessage(col + "--- " + ab.name() + " ~ " + min + ":" + sec);
			}
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender) {
		if (sender.hasPermission("bending.command.cooldown")) {
			sender.sendMessage("/bending cooldown");
		} else {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

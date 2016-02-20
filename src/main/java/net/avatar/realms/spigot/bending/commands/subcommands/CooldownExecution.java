package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class CooldownExecution extends BendingCommand {

	public CooldownExecution() {
		super();
		this.command = "cooldown";
		this.aliases.add("cd");
		this.aliases.add("cooldowns");
		this.basePermission = "bending.command.cooldown";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		Player player = (Player) sender;

		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (bender == null) {
			Bending.getInstance().getLogger().warning("Cooldown command was not able to find bending player for " + player.getName());
			sender.sendMessage(ChatColor.RED + Messages.YOU_NO_EXIST);
			return true;
		}

		Map<String, Long> cooldowns = bender.getCooldowns();
		player.sendMessage("-Cooldowns :");
		if ((cooldowns == null) || cooldowns.isEmpty()) {
			player.sendMessage("--- None");
		} else {
			for (String ab : cooldowns.keySet()) {
				ChatColor col = ChatColor.WHITE;
				int min = (int) ((cooldowns.get(ab) / 1000) / 60);
				int sec = (int) ((((cooldowns.get(ab) / 1000.0) / 60.0) - min) * 60);
				RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ab);
				if (register.getElement() != BendingElement.Energy) {
					col = PluginTools.getColor(Settings.getColorString(register.getElement().name()));
				}
				player.sendMessage(col + "--- " + register.getName() + " ~ " + min + ":" + ((sec < 10) ? "0" + sec : sec));
			}
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.cooldown")) {
			sender.sendMessage("/bending cooldown");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

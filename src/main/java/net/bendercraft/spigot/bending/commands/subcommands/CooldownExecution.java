package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityCooldown;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.utils.PluginTools;

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
		
		if(args.isEmpty()) {
			Map<String, BendingAbilityCooldown> cooldowns = bender.getCooldowns();
			player.sendMessage("-Cooldowns :");
			if ((cooldowns == null) || cooldowns.isEmpty()) {
				player.sendMessage("--- None");
			} else {
				long now = System.currentTimeMillis();
				for (Entry<String, BendingAbilityCooldown> entry : cooldowns.entrySet()) {
					ChatColor col = ChatColor.WHITE;
					RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(entry.getKey());
					if (register.getElement() != BendingElement.ENERGY) {
						col = PluginTools.getColor(Settings.getColor(register.getElement()));
					}
					player.sendMessage(col + "--- " + register.getName() + " ~ " + entry.getValue().timeLeft(now));
				}
			}
			return true;
		} else if(args.size() == 1) {
			String choice = args.get(0);
			if(choice.equalsIgnoreCase("show")) {
				bender.setUsingScoreboard(true);
				bender.loadScoreboard();
				if(Settings.USE_SCOREBOARD) {
					player.sendMessage(ChatColor.GREEN+"Your cooldowns will be shown as a sidebar scoreboard.");
				} else {
					player.sendMessage(ChatColor.RED+"Bending has been configured to not use scoreboard at all, your cooldown will not be shown.");
				}
				return true;
			} else if(choice.equalsIgnoreCase("hide")) {
				bender.setUsingScoreboard(false);
				bender.unloadScoreboard();
				player.sendMessage(ChatColor.GREEN+"Your cooldowns are now hidden.");
				return true;
			} else if(choice.equalsIgnoreCase("toggle")) {
				if(bender.isUsingScoreboard()) {
					bender.setUsingScoreboard(false);
					bender.unloadScoreboard();
					player.sendMessage(ChatColor.GREEN+"Your cooldowns are now hidden.");
				} else {
					bender.setUsingScoreboard(true);
					bender.loadScoreboard();
					if(Settings.USE_SCOREBOARD) {
						player.sendMessage(ChatColor.GREEN+"Your cooldowns will be shown as a sidebar scoreboard.");
					} else {
						player.sendMessage(ChatColor.RED+"Bending has been configured to not use scoreboard at all, your cooldown will not be shown.");
					}
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.cooldown")) {
			sender.sendMessage("/bending cooldown [hide|show|toggle]");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

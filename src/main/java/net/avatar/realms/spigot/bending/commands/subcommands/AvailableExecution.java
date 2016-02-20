package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class AvailableExecution extends BendingCommand {

	public AvailableExecution() {
		super();
		this.command = "available";
		this.aliases.add("availables");
		this.aliases.add("avail");
		this.basePermission = "bending.command.available";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		Player player = (Player) sender;
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);

		sender.sendMessage("Available abilities : ");
		for (RegisteredAbility ability : AbilityManager.getManager().getRegisteredAbilities()) {
			if (player.hasPermission(ability.getPermission()) && bender.isBender(ability.getElement())
					&& (ability.getAffinity() == BendingAffinity.NONE || bender.hasAffinity(ability.getAffinity()))) {
				ChatColor color = PluginTools.getColor(Settings.getColorString(ability.getElement().name()));
				sender.sendMessage("--" + color + ability.getName());
			}
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		sender.sendMessage("/bending available");
	}
}

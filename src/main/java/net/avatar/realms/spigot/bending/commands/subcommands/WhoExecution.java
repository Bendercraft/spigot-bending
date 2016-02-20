package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class WhoExecution extends BendingCommand {

	public WhoExecution() {
		super();
		this.command = "who";
		this.aliases.add("w");
		this.aliases.add("wh");
		this.basePermission = "bending.command.who";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			// We do not allow console to use this command because this would
			// spam the logs
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		Player player = (Player) sender;

		if (args.isEmpty()) {
			sendList(player);
		} else {
			sendWho(player, args.get(0));
		}
		return true;
	}

	private void sendWho(Player player, String playerName) {
		Player p = getPlayer(playerName);
		if (p == null || !player.canSee(p)) {
			player.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return;
		}
		BendingPlayer bender = BendingPlayer.getBendingPlayer(p);
		if (bender == null || bender.getBendingTypes() == null || bender.getBendingTypes().isEmpty()) {
			player.sendMessage(playerName);
			player.sendMessage(Messages.NO_BENDING);
			return;
		}

		ChatColor color = ChatColor.WHITE;
		if (bender.getBendingTypes().size() > 1) {
			color = PluginTools.getColor(Settings.getColorString(BendingElement.Energy.name()));
		} else {
			color = PluginTools.getColor(Settings.getColorString(bender.getBendingTypes().get(0).name()));
		}
		player.sendMessage(color + playerName);
		for (BendingElement element : bender.getBendingTypes()) {
			color = PluginTools.getColor(Settings.getColorString(element.name()));
			String msg;
			if (element == BendingElement.Master) {
				msg = Messages.WHO_IS_CHI;
			} else {
				msg = Messages.WHO_IS_BENDING;
				msg = msg.replaceAll("\\{1\\}", element.name());
			}
			msg = msg.replaceAll("\\{0\\}", p.getName());
			msg = color + msg;
			player.sendMessage(msg);

			for (BendingAffinity aff : bender.getAffinities()) {
				if (aff.getElement() == element) {
					player.sendMessage(color + p.getName() + " has affinity : " + aff.name());
					break;
				}
			}
		}
	}

	private void sendList(Player player) {
		for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (player.canSee(p)) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
				ChatColor color;
				if (bPlayer.getBendingTypes() != null && !bPlayer.getBendingTypes().isEmpty()) {
					BendingElement el = bPlayer.getBendingTypes().get(0);
					color = PluginTools.getColor(Settings.getColorString(el.name()));
				} else {
					color = ChatColor.WHITE;
				}
				player.sendMessage(color + p.getName());
			}
		}
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.who")) {
			sender.sendMessage("/bending who <player>");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}
}

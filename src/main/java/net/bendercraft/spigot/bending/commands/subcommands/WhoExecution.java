package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.utils.PluginTools;

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
		Player target = getPlayer(playerName);
		if (target == null || !player.canSee(target)) {
			player.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return;
		}
		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);
		if (bender == null || bender.getBendingTypes() == null || bender.getBendingTypes().isEmpty()) {
			player.sendMessage(playerName);
			player.sendMessage(Messages.NO_BENDING);
			return;
		}

		ChatColor color;
		if (bender.getBendingTypes().size() > 1) {
			color = PluginTools.getColor(Settings.getColor(BendingElement.ENERGY));
		}
		else {
			color = PluginTools.getColor(Settings.getColor(bender.getBendingTypes().get(0)));
		}
		player.sendMessage(color + playerName);

		bender.getBendingTypes().stream().parallel().forEach((element) -> {
			ChatColor col = PluginTools.getColor(Settings.getColor(element));
			String msg;
			if (element == BendingElement.MASTER) {
				msg = Messages.WHO_IS_MASTER;
			} else {
				msg = Messages.WHO_IS_BENDING;
				msg = msg.replace("{1}", element.name());
				Optional<BendingPath> path = bender.getPath().stream().filter((p) -> p.getElement().equals(element)).findFirst();
				if (path.isPresent()) {
					msg = msg.replace("{2}",path.get().name());
				}
			}
			msg = msg.replace("{0}", target.getName());
			msg = col + msg;
			player.sendMessage(msg);

			bender.getAffinities().stream().filter((aff) -> aff.getElement() == element).forEach((aff) -> {
				player.sendMessage(col + target.getName() + " has affinity : " + aff.name());
			});
		});
	}

	private void sendList(Player player) {
		for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (player.canSee(p)) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
				ChatColor color;
				if (bPlayer.getBendingTypes() != null && !bPlayer.getBendingTypes().isEmpty()) {
					BendingElement el = bPlayer.getBendingTypes().get(0);
					color = PluginTools.getColor(Settings.getColor(el));
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

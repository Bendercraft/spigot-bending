package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.IBendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class BindExecution implements IBendingCommand {

	@Override
	public boolean execute (CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		Player player = (Player) sender;

		if (!player.hasPermission("bending.command.bind")) {
			player.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}
		
		if ((args.size() != 1) && (args.size() != 2)) {
			printUsage(player);
			return true;
		}
		
		final String a = args.get(0);
		final Abilities ability = Abilities.getAbility(a);
		if (ability == null) {
			player.sendMessage(ChatColor.RED + Messages.INVALID_ABILITY);
			return true;
		}
		
		if (!EntityTools.hasPermission(player, ability)) {
			player.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}

		int slot;
		if (args.size() == 2) {
			try {
				slot = Integer.parseInt(args.get(1));
				slot--;
			}
			catch (final NumberFormatException e) {
				printUsage(player);
				return true;
			}
		}
		else {
			slot = player.getInventory().getHeldItemSlot();
		}
		if ((slot < 0) || (slot > 8)) {
			printUsage(player);
			return true;
		}
		ChatColor color = ChatColor.WHITE;
		color = PluginTools.getColor(Settings.getColorString(ability.getElement().name()));
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (!bender.isBender(ability.getElement())) {
			player.sendMessage(color + Messages.NOT_HAVE_ELEMENT + ability.getElement().name());
			return true;
		}
		bender.setAbility(slot, ability);
		String boundMessage = Messages.ABILITY_BOUND;
		boundMessage.replaceAll("{0}", ability.name());
		boundMessage.replaceAll("{1}", "" + slot);
		player.sendMessage(color + boundMessage);
		return true;
	}
	
	@Override
	public void printUsage (CommandSender sender) {
		sender.sendMessage("/bending bind <ability> [slot]");
	}
}

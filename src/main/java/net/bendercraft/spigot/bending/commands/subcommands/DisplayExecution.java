package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.utils.PluginTools;

public class DisplayExecution extends BendingCommand {

	public DisplayExecution() {
		super();
		this.command = "display";
		this.aliases.add("d");
		this.basePermission = "bending.command.display";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		Player player = null;
		
		if (!args.isEmpty() && sender.hasPermission("bending.admin")) {
			player = Bukkit.getPlayer(args.get(0));
		} else if(sender instanceof Player) {
			player = (Player) sender;
		}
		
		if(player == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return true;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		sender.sendMessage("Player: "+ChatColor.GOLD+player.getName());
		sender.sendMessage("Elements:");
		for(BendingElement element : bender.getBendingTypes()) {
			StringBuilder sb = new StringBuilder();
			for(BendingAffinity affinity : bender.getAffinities()) {
				if(affinity.getElement() == element) {
					sb.append(affinity.name()+", ");
				}
			}
			sender.sendMessage(" - "+PluginTools.getColor(Settings.getColor(element))+element.name()+" with affinities ["+(sb.length()==0 ? "NONE" : sb.substring(0, sb.lastIndexOf(", ")))+"]");
		}
		Map<Integer, String> abilities = bender.getAbilities();
		sender.sendMessage("Active deck : " + bender.getCurrentDeck());
		StringBuilder sb = new StringBuilder();
		bender.getDecks().keySet().forEach(d -> sb.append(d+", "));
		sender.sendMessage("Decks : " + sb.substring(0, sb.lastIndexOf(", ")));
		if (abilities != null && !abilities.isEmpty()) {
			sender.sendMessage("Slots:");
			for (Entry<Integer, String> slot : abilities.entrySet()) {
				RegisteredAbility ab = AbilityManager.getManager().getRegisteredAbility(slot.getValue());
				if (ab != null) {
					ChatColor color = PluginTools.getColor(Settings.getColor(ab.getElement()));
					sender.sendMessage(" - " + color + (slot.getKey() + 1) + ChatColor.WHITE + " : " + color + ab.getName());
				}
			}
		} else {
			sender.sendMessage("Slots: " + Messages.NOTHING_BOUND);
		}

		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.display")) {
			sender.sendMessage("/bending display");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		return new LinkedList<String>();
	}
}

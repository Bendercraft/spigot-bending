package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
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

		Map<BendingElement, List<RegisteredAbility>> elements = new HashMap<BendingElement, List<RegisteredAbility>>();
		Map<BendingAffinity, List<RegisteredAbility>> affinities = new HashMap<BendingAffinity, List<RegisteredAbility>>();
		
		
		for (RegisteredAbility ability : AbilityManager.getManager().getRegisteredAbilities()) {
			if (player.hasPermission(ability.getPermission()) && bender.isBender(ability.getElement())
					&& (ability.getAffinity() == BendingAffinity.NONE || bender.hasAffinity(ability.getAffinity()))) {
				if(ability.getAffinity() == null || ability.getAffinity() == BendingAffinity.NONE) {
					if(!elements.containsKey(ability.getElement())) {
						elements.put(ability.getElement(), new LinkedList<RegisteredAbility>());
					}
					elements.get(ability.getElement()).add(ability);
				} else {
					if(!affinities.containsKey(ability.getAffinity())) {
						affinities.put(ability.getAffinity(), new LinkedList<RegisteredAbility>());
					}
					affinities.get(ability.getAffinity()).add(ability);
				}
			}
		}
		
		sender.sendMessage("Available abilities : ");
		for(Entry<BendingElement, List<RegisteredAbility>> entry : elements.entrySet()) {
			ChatColor color = PluginTools.getColor(Settings.getColor(entry.getKey()));
			sender.sendMessage(" - "+color+entry.getKey());
			for(RegisteredAbility ability : entry.getValue()) {
				sender.sendMessage("   - "+color+ability.getName());
			}
		}
		for(Entry<BendingAffinity, List<RegisteredAbility>> entry : affinities.entrySet()) {
			ChatColor color = PluginTools.getColor(Settings.getColor(entry.getKey().getElement()));
			sender.sendMessage(" - "+color+entry.getKey());
			for(RegisteredAbility ability : entry.getValue()) {
				sender.sendMessage("   - "+color+ability.getName());
			}
		}
		
		
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		sender.sendMessage("/bending available");
	}
}

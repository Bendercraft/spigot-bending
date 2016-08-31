package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.utils.PluginTools;

public class ForgetExecution extends BendingCommand {

	public ForgetExecution() {
		super();
		this.command = "forget";
		this.aliases.add("forgetting");
		this.aliases.add("f");
		this.basePermission = "bending.command.learning";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.isEmpty()) {
			printUsage(sender);
			return true;
		}
		String target = args.get(0);
		
		Player player = null;
		if (args.size() == 2 && sender.hasPermission("bending.admin")) {
			player = Bukkit.getPlayer(args.get(1));
		} else if(sender instanceof Player) {
			player = (Player) sender;
		}
		if(player == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return true;
		}
		
		
		if(target.equalsIgnoreCase("all")) {
			for (RegisteredAbility ability : AbilityManager.getManager().getRegisteredAbilities()) {
				Bending.getInstance().getLearning().removePermission(player, ability);
			}
			player.sendMessage(ChatColor.GREEN+"You forgot every abilities from all elements !");
			if(sender != player) {
				sender.sendMessage(ChatColor.GREEN+"You have removed all abilities to "+player.getName()+" !");
			}
		} else if(BendingElement.getType(target) != null) {
			BendingElement element = BendingElement.getType(target);
			for (RegisteredAbility ability : AbilityManager.getManager().getRegisteredAbilities()) {
				if(ability.getElement() == element) {
					Bending.getInstance().getLearning().removePermission(player, ability);
				}
			}
			player.sendMessage(ChatColor.GREEN+"You forgot every abilities from "+PluginTools.getColor(Settings.getColor(element))+element.name()+ChatColor.GREEN+" !");
			if(sender != player) {
				sender.sendMessage(ChatColor.GREEN+"You have removed every abilities from from "+PluginTools.getColor(Settings.getColor(element))+element.name()+ChatColor.GREEN+" to "+player.getName()+" !");
			}
		} else if(BendingAffinity.getType(target) != null) {
			BendingAffinity affinity = BendingAffinity.getType(target);
			for (RegisteredAbility ability : AbilityManager.getManager().getRegisteredAbilities()) {
				if(ability.getAffinity() == affinity) {
					Bending.getInstance().getLearning().removePermission(player, ability);
				}
			}
			player.sendMessage(ChatColor.GREEN+"You forgot every abilities from "+PluginTools.getColor(Settings.getColor(affinity.getElement()))+affinity.name()+ChatColor.GREEN+" !");
			if(sender != player) {
				sender.sendMessage(ChatColor.GREEN+"You have removed every abilities from from "+PluginTools.getColor(Settings.getColor(affinity.getElement()))+affinity.name()+ChatColor.GREEN+" to "+player.getName()+" !");
			}
		} else if(AbilityManager.getManager().getRegisteredAbility(target) != null) {
			RegisteredAbility ability = AbilityManager.getManager().getRegisteredAbility(target);
			Bending.getInstance().getLearning().removePermission(player, ability);
			player.sendMessage(ChatColor.GREEN+"You forgot "+PluginTools.getColor(Settings.getColor(ability.getElement()))+ability.getName()+ChatColor.GREEN+" !");
			if(sender != player) {
				sender.sendMessage(ChatColor.GREEN+"You have removed "+PluginTools.getColor(Settings.getColor(ability.getElement()))+ability.getName()+ChatColor.GREEN+" to "+player.getName()+" !");
			}
		} else {
			sender.sendMessage(ChatColor.RED+"Unknown element/affinity/ability '"+target+"'.");
		}

		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (!sender.hasPermission("bending.command.affinity")) {
			sender.sendMessage("/bending forget <all|ELEMENT|AFFINITY|ABILITY> [player]");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}


	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		List<String> values = new LinkedList<String>();
		if (args.isEmpty()) {
			return values;
		}

		values.add("all");
		Arrays.asList(BendingElement.values()).forEach(e -> values.add(e.name()));
		Arrays.asList(BendingAffinity.values()).forEach(aff -> values.add(aff.name()));
		AbilityManager.getManager().getRegisteredAbilities().forEach(a -> values.add(a.getName()));

		return values;
	}

}

package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.commands.BendingCommand;

public class PerkExecution extends BendingCommand {

	public PerkExecution() {
		super();
		this.command = "perk";
		this.aliases.add("p");
		this.basePermission = "bending.command.perk";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if(args.size() == 0) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
				return true;
			}
			Player player = (Player) sender;
			BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
			bender.refreshPerks();
			
			List<BendingPerk> perks = bender.getPerks();
			StringBuilder sb = new StringBuilder();
			
			perks.forEach(p -> sb.append(p.name+", "));
			
			sender.sendMessage("Player "+player.getName()+" has perk :");
			sender.sendMessage(sb.toString());
			return true;
		} else if(args.size() == 1) {
			String arg = args.get(0);
			if(arg.equals("reset")) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
					return true;
				}
				//TODO check perm when debug is over
				Player player = (Player) sender;
				BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
				bender.refreshPerks();
				bender.resetPerks();
				sender.sendMessage("You have lost all your perks.");
				return true;
			} else {
				if (!sender.hasPermission("bending.admin")) {
					sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
					return true;
				}
				Player player = getPlayer(arg);
				BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
				bender.refreshPerks();
				List<BendingPerk> perks = bender.getPerks();
				StringBuilder sb = new StringBuilder();
				
				perks.forEach(p -> sb.append(p.name+", "));
				
				sender.sendMessage("Player "+player.getName()+" has perk :");
				sender.sendMessage(sb.toString());
				return true;
			}
		} else if(args.size() == 2) {
			if (!sender.hasPermission("bending.admin")) {
				sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
				return true;
			}
			
			String arg = args.get(0);
			if(arg.equals("reset")) {
				Player player = getPlayer(args.get(1));
				BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
				bender.refreshPerks();
				bender.resetPerks();
				sender.sendMessage("Player "+player.getName()+" losts all its perks.");
				return true;
			}
		}
		
		printUsage(sender);
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.perk")) {
			sender.sendMessage("/bending perk");
		} else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		return new LinkedList<String>();
	}
}

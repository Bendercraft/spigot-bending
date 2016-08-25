package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.utils.TempBlock;

public class DebugExecution extends BendingCommand {

	public DebugExecution() {
		super();
		this.command = "debug";
		this.basePermission = "bending.command.debug";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		List<BendingAbility> runnings = AbilityManager.getManager().getRunnings();
		Collections.sort(runnings, new Comparator<BendingAbility>() {
			@Override
			public int compare(BendingAbility o1, BendingAbility o2) {
				return (int) (o1.getStartedTime() - o2.getStartedTime());
			}
		});

		long now = System.currentTimeMillis();
		sender.sendMessage("Total temp block : "+ChatColor.GOLD+TempBlock.count());
		sender.sendMessage("Total runnings abilities : "+ChatColor.GOLD+runnings.size());
		for(BendingAbility running : runnings) {
			sender.sendMessage(" - "+ChatColor.DARK_PURPLE+running.getName()+ChatColor.RESET+" from "+ChatColor.GREEN+running.getPlayer().getName()+ChatColor.RESET+" since "+ChatColor.RED+((now - running.getStartedTime())/1000)+"s");
		}
		
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.debug")) {
			sender.sendMessage("/bending debug");
		} else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		return null;
	}
}

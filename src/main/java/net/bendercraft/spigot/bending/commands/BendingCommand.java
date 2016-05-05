package net.bendercraft.spigot.bending.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.controller.Settings;

public abstract class BendingCommand implements IBendingCommand {

	protected String command;
	protected List<String> aliases;
	protected String basePermission;

	public BendingCommand() {
		this.aliases = new LinkedList<String>();
	}

	@Override
	public boolean isCommand(String cmd) {
		if (this.command.equalsIgnoreCase(cmd)) {
			return true;
		}

		for (String alias : this.aliases) {
			if (alias.equalsIgnoreCase(cmd)) {
				return true;
			}
		}

		return false;
	}

	protected Player getPlayer(String name) {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	protected BendingElement getElement(String name) {

		for (String alias : Settings.AIR_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.AIR;
			}
		}

		for (String alias : Settings.MASTER_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.MASTER;
			}
		}

		for (String alias : Settings.EARTH_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.EARTH;
			}
		}

		for (String alias : Settings.FIRE_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.FIRE;
			}
		}

		for (String alias : Settings.WATER_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.WATER;
			}
		}

		return null;
	}

	@Override
	public void printUsage(CommandSender sender) {
		printUsage(sender, true);
	}

	@Override
	public String getCommand() {
		return this.command;
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		return new LinkedList<String>();
	}

	@Override
	public final boolean hasBasePermission(CommandSender sender) {
		if (sender.hasPermission(this.basePermission)) {
			return true;
		}
		return false;
	}

}

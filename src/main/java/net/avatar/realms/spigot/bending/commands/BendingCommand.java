package net.avatar.realms.spigot.bending.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.Settings;

public abstract class BendingCommand implements IBendingCommand {

	protected String command;
	protected List<String> aliases;

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
				return BendingElement.Air;
			}
		}

		for (String alias : Settings.CHI_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.ChiBlocker;
			}
		}

		for (String alias : Settings.EARTH_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.Earth;
			}
		}

		for (String alias : Settings.FIRE_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.Fire;
			}
		}

		for (String alias : Settings.WATER_ALIASES) {
			if (alias.equalsIgnoreCase(name)) {
				return BendingElement.Water;
			}
		}

		return null;
	}

	@Override
	public String getCommand() {
		return this.command;
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		return new LinkedList<String>();
	}

}

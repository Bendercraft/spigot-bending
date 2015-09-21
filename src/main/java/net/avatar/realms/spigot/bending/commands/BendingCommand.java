package net.avatar.realms.spigot.bending.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingType;

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

	protected BendingType getElement(String name) {

		if (name.equalsIgnoreCase("chi") || name.equalsIgnoreCase("chiblocker") || name.equalsIgnoreCase("chiblocking")) {
			return BendingType.ChiBlocker;
		}
		if (name.equalsIgnoreCase("air") || name.equalsIgnoreCase("airbender") || name.equalsIgnoreCase("airbending")) {
			return BendingType.Air;
		}
		if (name.equalsIgnoreCase("earth") || name.equalsIgnoreCase("earthbender") || name.equalsIgnoreCase("earthbending")) {
			return BendingType.Earth;
		}
		if (name.equalsIgnoreCase("fire") || name.equalsIgnoreCase("firebender") || name.equalsIgnoreCase("firebending")) {
			return BendingType.Fire;
		}
		if (name.equalsIgnoreCase("water") || name.equalsIgnoreCase("waterbender") || name.equalsIgnoreCase("waterbending")) {
			return BendingType.Water;
		}

		return null;
	}

	@Override
	public String getCommand() {
		return this.command;
	}
}

package net.avatar.realms.spigot.bending.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

	@Override
	public String getCommand() {
		return this.command;
	}
}

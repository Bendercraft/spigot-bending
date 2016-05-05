package net.bendercraft.spigot.bending.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.bendercraft.spigot.bending.abilities.BendingPlayer;

public class BendingCooldownEvent extends Event {
	protected static final HandlerList handlers = new HandlerList();
	
	private BendingPlayer bender;
	private String ability;

	public BendingCooldownEvent(BendingPlayer bender, String ability) {
		this.ability = ability;
		this.bender = bender;
	}

	public String getAbility() {
		return ability;
	}

	public BendingPlayer getBender() {
		return this.bender;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

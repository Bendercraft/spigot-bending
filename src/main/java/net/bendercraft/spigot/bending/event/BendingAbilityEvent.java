package net.bendercraft.spigot.bending.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.bendercraft.spigot.bending.abilities.BendingAbility;

public class BendingAbilityEvent extends Event {
	protected static final HandlerList handlers = new HandlerList();
	
	private final BendingAbility ability;

	public BendingAbilityEvent(BendingAbility ability) {
		this.ability = ability;
	}

	public BendingAbility getAbility() {
		return ability;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

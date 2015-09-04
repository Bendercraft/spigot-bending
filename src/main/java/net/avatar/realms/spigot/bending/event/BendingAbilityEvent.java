package net.avatar.realms.spigot.bending.event;

import org.bukkit.event.HandlerList;

import net.avatar.realms.spigot.bending.abilities.Abilities;

public class BendingAbilityEvent extends BendingEvent{
	
	protected Abilities ability;
	
	public BendingAbilityEvent (Abilities ability) {
		super();
		this.ability = ability;
	}
	
	public Abilities getAbility () {
		return this.ability;
	}

	public static HandlerList getHandlerList () {
		return handlers;
	}
	
}

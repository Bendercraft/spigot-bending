package net.avatar.realms.spigot.bending.event;

import org.bukkit.event.HandlerList;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;

public class BendingAbilityEvent extends BendingEvent{
	
	protected BendingAbilities ability;
	
	public BendingAbilityEvent (BendingAbilities ability) {
		super();
		this.ability = ability;
	}
	
	public BendingAbilities getAbility () {
		return this.ability;
	}

	public static HandlerList getHandlerList () {
		return handlers;
	}
	
}

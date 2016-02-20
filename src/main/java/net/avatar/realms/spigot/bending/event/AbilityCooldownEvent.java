package net.avatar.realms.spigot.bending.event;

import org.bukkit.event.HandlerList;

import net.avatar.realms.spigot.bending.abilities.BendingPlayer;

public class AbilityCooldownEvent extends BendingEvent {

	private BendingPlayer bender;
	private String ability;

	public AbilityCooldownEvent(BendingPlayer bender, String ability) {
		this.ability = ability;
		this.bender = bender;
	}

	public String getAbility() {
		return ability;
	}

	public BendingPlayer getBender() {
		return this.bender;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

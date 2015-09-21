package net.avatar.realms.spigot.bending.event;

import org.bukkit.event.HandlerList;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;

public class AbilityCooldownEvent extends BendingAbilityEvent {

	private BendingPlayer bender;

	public AbilityCooldownEvent(BendingPlayer bender, BendingAbilities ability) {
		super(ability);
		this.bender = bender;
	}

	public BendingPlayer getBender() {
		return this.bender;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

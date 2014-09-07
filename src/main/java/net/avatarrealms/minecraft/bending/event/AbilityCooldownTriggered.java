package net.avatarrealms.minecraft.bending.event;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AbilityCooldownTriggered extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Abilities ability;
	private BendingPlayer bender;

	public AbilityCooldownTriggered(BendingPlayer bender, Abilities ability) {
		this.bender = bender;
		this.ability = ability;
	}
	
	public Abilities getAbility() {
		return ability;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public BendingPlayer getBender() {
		return bender;
	}

}

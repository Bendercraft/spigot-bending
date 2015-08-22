package net.avatar.realms.spigot.bending.event;


import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;

public class AbilityCooldownEvent extends BendingAbilityEvent {

	private BendingPlayer bender;

	public AbilityCooldownEvent(BendingPlayer bender, Abilities ability) {
		super(ability);
		this.bender = bender;
	}

	public BendingPlayer getBender() {
		return this.bender;
	}


}

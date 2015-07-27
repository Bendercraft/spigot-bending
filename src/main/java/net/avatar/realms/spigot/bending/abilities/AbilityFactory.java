package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.chi.PoisonnedDart;

public class AbilityFactory {
	
	public Ability buildAbility (Abilities abilityType, Player player) {
		switch (abilityType) {
			case PoisonnedDart : return new PoisonnedDart(player);
			default : return null;
		}
	}

}

package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.chi.HighJump;
import net.avatar.realms.spigot.bending.abilities.chi.PoisonnedDart;
import net.avatar.realms.spigot.bending.abilities.chi.SmokeBomb;

public class AbilityFactory {
	
	public static Ability buildAbility (Abilities abilityType, Player player) {
		switch (abilityType) {
			case PoisonnedDart : return new PoisonnedDart(player);
			case HighJump : return new HighJump(player);
			case SmokeBomb : return new SmokeBomb(player);
			default : return null;
		}
	}

}

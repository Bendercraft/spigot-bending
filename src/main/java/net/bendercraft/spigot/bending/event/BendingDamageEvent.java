package net.bendercraft.spigot.bending.event;

import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.google.common.base.Function;

import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;

public class BendingDamageEvent extends EntityDamageByEntityEvent {
	private final BendingAbility ability;

	@SuppressWarnings("deprecation")
	public BendingDamageEvent(BendingPlayer damager, Entity damagee, BendingAbility ability, Map<DamageModifier, Double> modifiers, Map<DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions) {
		super(damager.getPlayer(), damagee, DamageCause.CUSTOM, modifiers, modifierFunctions);
		this.ability = ability;
	}

	public BendingAbility getAbility() {
		return ability;
	}
}

package net.avatar.realms.spigot.bending.event;

import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.google.common.base.Function;

import net.avatar.realms.spigot.bending.abilities.BendingPlayer;

public class BendingDamageEvent extends EntityDamageByEntityEvent {
	public BendingDamageEvent(BendingPlayer damager, Entity damagee, Map<DamageModifier, Double> modifiers, Map<DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions) {
		super(damager.getPlayer(), damagee, DamageCause.CUSTOM, modifiers, modifierFunctions);
	}
}

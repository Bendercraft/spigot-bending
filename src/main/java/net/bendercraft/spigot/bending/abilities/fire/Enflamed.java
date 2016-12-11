package net.bendercraft.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

public class Enflamed {
	private static Map<Entity, Enflamed> instances = new HashMap<Entity, Enflamed>();

	private static final double DAMAGE = 1;

	private int secondsLeft;
	private Entity target;

	private long time;
	private BendingPlayer bender;
	private double damage;
	private BendingAbility ability;

	public static void enflame(Player source, Entity target, int seconds, BendingAbility ability) {
		if (target == source) {
			return;
		}
		
		BendingPlayer bender = BendingPlayer.getBendingPlayer(source);
		double damage = DAMAGE;
		if (instances.containsKey(target)) {
			if(bender.hasPerk(BendingPerk.FIRE_SCORCH)) {
				damage = DAMAGE * 1.4;
				if(bender.hasPerk(BendingPerk.FIRE_SCORCH_ENHANCE_1)) {
					damage = DAMAGE * 1.6;
				}
				if(bender.hasPerk(BendingPerk.FIRE_SCORCH_ENHANCE_2)) {
					damage = DAMAGE * 1.8;
				}
			}
		}
		instances.put(target, new Enflamed(bender, target, damage, seconds, ability));
	}
	
	private Enflamed(BendingPlayer bender, Entity target, double damage, int seconds, BendingAbility ability) {
		this.ability = ability;
		this.target = target;
		this.time = System.currentTimeMillis();
		this.secondsLeft = seconds;
		this.damage = damage;
		this.bender = bender;
		target.setFireTicks(secondsLeft*20);
	}

	public void addSeconds(int amount) {
		this.secondsLeft += amount;
	}

	public boolean progress() {
		long now = System.currentTimeMillis();
		if(target.isDead()) {
			return false;
		}
		if ((now - this.time) < 1000) {
			return true;
		}
		time = now;
		
		if(secondsLeft <= 0) {
			return false;
		}
		if (target.getFireTicks() == 0 && !bender.hasPerk(BendingPerk.FIRE_INNERFIRE)) {
			return false;
		}
		
		if (target instanceof Player && !canBurn((Player) target)) {
			return false;
		}

		affect(target);
		secondsLeft -= 1;
		
		
		return true;
	}
	
	public void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(ability, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		target.setFireTicks(this.secondsLeft * 20);
		DamageTools.damageEntity(bender, target, ability, damage, true, 0, 0.0F, true);
	}

	public static boolean isEnflamed(Entity entity) {
		return instances.containsKey(entity);
	}

	public static boolean canBurn(Player player) {
		if (HeatControl.NAME.equals(EntityTools.getBendingAbility(player)) || FireJet.checkTemporaryImmunity(player)) {
			player.setFireTicks(0);
			return false;
		}

		if ((player.getFireTicks() > 80) && EntityTools.canBendPassive(player, BendingElement.FIRE)) {
			player.setFireTicks(80);
		}

		return true;
	}

	public static void progressAll() {
		List<Enflamed> toRemove = new LinkedList<Enflamed>();
		for (Enflamed flame : instances.values()) {
			if (!flame.progress()) {
				toRemove.add(flame);
			}
		}

		for (Enflamed flame : toRemove) {
			flame.remove();
		}
	}

	public boolean remove() {
		instances.remove(this.target);
		return true;
	}
}

package net.bendercraft.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

public class Enflamed {
	private static Map<Entity, Enflamed> instances = new HashMap<Entity, Enflamed>();

	private static final double DAMAGE = 1;

	private int secondsLeft;
	private Entity target;

	private long time;
	private BendingPlayer bender;
	private double damage;

	public static void enflame(Player source, Entity target, int seconds) {
		if (target.getEntityId() == source.getEntityId()) {
			return;
		}
		if (ProtectionManager.isEntityProtected(target)) {
			return;
		}
		
		BendingPlayer bender = BendingPlayer.getBendingPlayer(source);
		if (BendingPlayer.getBendingPlayer(source).hasPath(BendingPath.LIFELESS)) {
			return;
		}
		
		if (bender.hasPath(BendingPath.NURTURE)) {
			if (instances.containsKey(target) && instances.get(target).bender == bender) {
				instances.get(target).addSeconds(seconds);
				return;
			}
		}
		instances.put(target, new Enflamed(bender, target, seconds));
	}
	
	private Enflamed(BendingPlayer bender, Entity target, int seconds) {
		this.target = target;
		this.time = System.currentTimeMillis();
		this.secondsLeft = seconds;
		this.damage = DAMAGE;
		this.bender = bender;
		if (this.bender.hasPath(BendingPath.NURTURE)) {
			damage *= 0.5;
		}

		if (this.bender.hasPath(BendingPath.NURTURE)) {
			if (instances.containsKey(this.target) && instances.get(this.target).bender == bender) {
				instances.get(this.target).addSeconds(seconds);
				return;
			}
		}
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
		if (this.target.getFireTicks() == 0 && !this.bender.hasPath(BendingPath.NURTURE)) {
			return false;
		}
		
		if (target instanceof Player && !canBurn((Player) this.target)) {
			return false;
		}

		this.target.setFireTicks(this.secondsLeft * 20);
		this.secondsLeft -= 1;
		
		DamageTools.damageEntity(bender, this.target, damage, true, 0, 0.0F, true);
		return true;
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

package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

public class Enflamed {
	private static Map<Entity, Enflamed> instances = new HashMap<Entity, Enflamed>();

	private static final double DAMAGE = 1;

	private int secondsLeft;
	private Player source;
	private Entity target;

	private long time;
	private BendingPlayer bender;

	public Enflamed(Player source, Entity entity, int seconds) {
		if (entity.getEntityId() == source.getEntityId()) {
			return;
		}
		this.target = entity;
		this.source = source;
		this.time = System.currentTimeMillis();
		this.secondsLeft = seconds;

		if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		this.bender = BendingPlayer.getBendingPlayer(source);
		if (this.bender.hasPath(BendingPath.Lifeless)) {
			return;
		}

		if (this.bender.hasPath(BendingPath.Nurture)) {
			if (instances.containsKey(this.target) && instances.get(this.target).bender == bender) {
				instances.get(this.target).addSeconds(seconds);
				return;
			}
		}
		entity.setFireTicks(secondsLeft*20);
		instances.put(entity, this);
	}

	public void addSeconds(int amount) {
		this.secondsLeft += amount;
	}

	public boolean progress() {
		long now = System.currentTimeMillis();
		if ((now - this.time) < 1000) {
			return true;
		}
		time = now;
		
		if(secondsLeft <= 0) {
			return false;
		}
		if (this.target.getFireTicks() == 0 && !this.bender.hasPath(BendingPath.Nurture)) {
			return false;
		}
		
		if (target instanceof Player && !canBurn((Player) this.target)) {
			return false;
		}

		this.target.setFireTicks(this.secondsLeft * 20);
		this.secondsLeft -= 1;
		
		EntityTools.damageEntity(this.source, this.target, DAMAGE);
		return true;
	}

	public static boolean isEnflamed(Entity entity) {
		return instances.containsKey(entity);
	}

	public static boolean canBurn(Player player) {
		if ((EntityTools.getBendingAbility(player) == BendingAbilities.HeatControl) || FireJet.checkTemporaryImmunity(player)) {
			player.setFireTicks(0);
			return false;
		}

		if ((player.getFireTicks() > 80) && EntityTools.canBendPassive(player, BendingElement.Fire)) {
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

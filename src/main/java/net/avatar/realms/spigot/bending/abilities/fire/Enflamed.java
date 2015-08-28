package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@BendingAbility(name="Enflamed", element=BendingType.Fire)
public class Enflamed implements IAbility {
	private static Map<Entity, Player> instances = new HashMap<Entity, Player>();
	private static Map<Entity, Long> times = new HashMap<Entity, Long>();

	private static final int DAMAGE = 1;
	private static final int max = 90;
	private static final long buffer = 30;
	
	private IAbility parent;

	public Enflamed(Entity entity, Player source, IAbility parent) {
		this.parent = parent;
		if (entity.getEntityId() == source.getEntityId())
			return;
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		instances.put(entity, source);
	}

	public static boolean isEnflamed(Entity entity) {
		// return false;
		if (instances.containsKey(entity)) {
			if (times.containsKey(entity)) {
				long time = times.get(entity);
				if (System.currentTimeMillis() < time + buffer) {
					return false;
				}
			}
			times.put(entity, System.currentTimeMillis());
			return true;
		} else {
			return false;
		}
	}

	public static void dealFlameDamage(Entity entity) {
		if (instances.containsKey(entity) && entity instanceof LivingEntity) {
			if (entity instanceof Player) {
				if (!Extinguish.canBurn((Player) entity)) {
					return;
				}
			}
			LivingEntity Lentity = (LivingEntity) entity;
			Player source = instances.get(entity);
			Lentity.damage(DAMAGE, source);
			if (entity.getFireTicks() > max)
				entity.setFireTicks(max);
		}
	}

	public static void handleFlames() {
		List<Entity> toRemove = new LinkedList<Entity>();
		for (Entity entity : instances.keySet()) {
			if (entity.getFireTicks() <= 0) {
				toRemove.add(entity);
			}
		}
		
		for(Entity entity : toRemove) {
			instances.remove(entity);
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

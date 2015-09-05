package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPathType;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@BendingAbility(name="Enflamed", element=BendingType.Fire)
public class Enflamed implements IAbility {
	private static Map<Entity, Enflamed> instances = new HashMap<Entity, Enflamed>();

	private static final double DAMAGE = 1;
	
	private int secondsLeft;
	private Player source;
	private Entity target;
	
	private long time;
	private BendingPlayer bender;
	
	private IAbility parent;

	public Enflamed(Entity entity, Player source, int seconds, IAbility parent) {
		this.parent = parent;
		this.target = entity;
		this.source = source;
		this.time = System.currentTimeMillis();
		if (entity.getEntityId() == source.getEntityId())
			return;
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		bender = BendingPlayer.getBendingPlayer(source);
		if(bender.hasPath(BendingPathType.Lifeless)) {
			return;
		}
		
		if(bender.hasPath(BendingPathType.Nurture)) {
			if(instances.containsKey(target)) {
				instances.get(target).addSeconds(seconds);
				return;
			}
		}
		
		instances.put(entity, this);
	}
	
	public void addSeconds(int amount) {
		secondsLeft += amount;
	}
	
	public boolean progress() {
		long now = System.currentTimeMillis();
		if(now - time < 1000) {
			return true;
		}
		
		if (!Extinguish.canBurn((Player) target)) {
			return false;
		}
		
		if (target.getFireTicks() == 0) {
			if(bender.hasPath(BendingPathType.Nurture)) {
				target.setFireTicks(secondsLeft*50);
			} else {
				return false;
			}
		}
		
		secondsLeft--;
		EntityTools.damageEntity(source, target, DAMAGE);
		
		return true;
	}
	
	public boolean remove() {
		instances.remove(target);
		return true;
	}
	

	public static boolean isEnflamed(Entity entity) {
		return instances.containsKey(entity);
	}

	public static void progressAll() {
		List<Enflamed> toRemove = new LinkedList<Enflamed>();
		for (Enflamed flame : instances.values()) {
			if (flame.progress()) {
				toRemove.add(flame);
			}
		}
		
		for(Enflamed flame : toRemove) {
			flame.remove();
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

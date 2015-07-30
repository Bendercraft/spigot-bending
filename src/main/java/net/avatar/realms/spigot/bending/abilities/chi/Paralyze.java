package net.avatar.realms.spigot.bending.abilities.chi;


import java.util.HashMap;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * 
 * This ability will be modified :
 * When you hit an entity, you deal a small amount of damage to it and it gets slown.
 * The more you hit it, the more it get slown.
 *
 */
@BendingAbility(name="Vital Point", element=BendingType.ChiBlocker)
public class Paralyze implements IAbility {
	private static Map<Entity, Long> entities = new HashMap<Entity, Long>();
	private static Map<Entity, Long> cooldowns = new HashMap<Entity, Long>();

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;
	
	@ConfigurationParameter("Duration")
	private static long DURATION = 2500;
	private IAbility parent;

	public Paralyze(Player sourcePlayer, Entity targetEntity, IAbility parent) {
		this.parent = parent;
		if (targetEntity != null && sourcePlayer != null) {
			if (EntityTools.isBender(sourcePlayer, BendingType.ChiBlocker)
				&& EntityTools.getBendingAbility(sourcePlayer) == Abilities.Paralyze
				&& EntityTools.canBend(sourcePlayer, Abilities.Paralyze)) {
			if (cooldowns.containsKey(targetEntity)) {
				if (System.currentTimeMillis() < cooldowns.get(targetEntity)
						+ COOLDOWN) {
					return;
				} else {
					cooldowns.remove(targetEntity);
				}
			}
			Location sourcLoc = sourcePlayer.getLocation();
			Location targLoc = targetEntity.getLocation();
			if (sourcLoc.getWorld() != targLoc.getWorld() || sourcLoc.distance(targLoc) > 2.5) {
				return;
			}
			paralyze(targetEntity);
			cooldowns.put(targetEntity, System.currentTimeMillis());
			}
		}	
	}

	private static void paralyze(Entity entity) {
		entities.put(entity, System.currentTimeMillis());
		if (entity instanceof Creature) {
			((Creature) entity).setTarget(null);
		}
	}

	public static boolean isParalyzed(Entity entity) {
		if (entity instanceof Player) {
			if (AvatarState.isAvatarState((Player) entity)){
				return false;
			}
			if (((Player)entity).isOp()){
				return false;
			}
		}
		if (entities.containsKey(entity)) {
			if (System.currentTimeMillis() < entities.get(entity) + DURATION) {
				return true;
			}
			entities.remove(entity);
		}
		return false;

	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

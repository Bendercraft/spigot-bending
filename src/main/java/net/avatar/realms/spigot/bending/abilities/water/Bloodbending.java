package net.avatar.realms.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@BendingAbility(name="Blood Bending", element=BendingType.Water, specialization=BendingSpecializationType.Bloodbend)
public class Bloodbending implements IAbility {

	private static Map<Player, Bloodbending> instances = new HashMap<Player, Bloodbending>();

	private Map<Entity, Location> targetEntities = new HashMap<Entity, Location>();

	@ConfigurationParameter("Throw-Factor")
	private static double FACTOR = 1.0;
	
	@ConfigurationParameter("Max-Duration")
	private static int MAX_DURATION = 10000;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN =  6000;
	
	@ConfigurationParameter("Range")
	public static int RANGE = 8;

	private Player player;
	private int range = RANGE;
	private IAbility parent;
	private Long time;

	public Bloodbending(Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return;
		}
		range = (int) PluginTools.waterbendingNightAugment(range,
				player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			for (LivingEntity entity : EntityTools
					.getLivingEntitiesAroundPoint(player.getLocation(), range)) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (entity instanceof Player) {
					if (ProtectionManager.isRegionProtectedFromBending(player,
							Abilities.Bloodbending, entity.getLocation())
							|| AvatarState.isAvatarState((Player) entity)
							|| entity.getEntityId() == player.getEntityId()
							|| EntityTools.canBend((Player) entity,
									Abilities.Bloodbending))
						continue;
				}
				EntityTools.damageEntity(player, entity, 0);
				targetEntities.put(entity, entity.getLocation().clone());

			}
		} else {
			if (BendingPlayer.getBendingPlayer(player).isOnCooldown(Abilities.Bloodbending)) {
				return;
			}
			Entity target = EntityTools.getTargettedEntity(player, range);
			if(ProtectionManager.isEntityProtectedByCitizens(target)) {
				return;
			}
			if (target == null) {
				return;
			}		
			if (!(target instanceof LivingEntity)
					|| ProtectionManager.isRegionProtectedFromBending(player,
							Abilities.Bloodbending, target.getLocation()))
				return;
			if (target instanceof Player) {
				if (EntityTools
						.canBend((Player) target, Abilities.Bloodbending)
						|| AvatarState.isAvatarState((Player) target)
						|| ((Player) target).isOp()) {
					return;
				}

			}
			EntityTools.damageEntity(player, target, 0);
			targetEntities.put(target, target.getLocation().clone());
		}
		this.time = System.currentTimeMillis();
		this.player = player;
		instances.put(player, this);
	}

	public static void launch(Player player) {
		if (instances.containsKey(player))
			instances.get(player).launch();
	}

	private void launch() {
		Location location = player.getLocation();
		for (Entity entity : targetEntities.keySet()) {
			double dx, dy, dz;
			Location target = entity.getLocation().clone();
			dx = target.getX() - location.getX();
			dy = target.getY() - location.getY();
			dz = target.getZ() - location.getZ();
			Vector vector = new Vector(dx, dy, dz);
			vector.normalize();
			entity.setVelocity(vector.multiply(FACTOR));
		}
		remove();
	}

	private boolean progress() {
		PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 60, 1);

		if (!player.isSneaking()
				|| EntityTools.getBendingAbility(player) != Abilities.Bloodbending
				|| !EntityTools.canBend(player, Abilities.Bloodbending)) {
			return false;
		}
		
		if (System.currentTimeMillis() - time > MAX_DURATION) {
			return false;
		}
		
		if (AvatarState.isAvatarState(player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (LivingEntity entity : EntityTools
					.getLivingEntitiesAroundPoint(player.getLocation(), range)) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(player,
						Abilities.Bloodbending, entity.getLocation())) {
					continue;
				}
				if (entity instanceof Player) {
					if (!EntityTools.canBeBloodbent((Player) entity))
						continue;
				}
				entities.add(entity);
				if (!targetEntities.containsKey(entity)) {
					EntityTools.damageEntity(player, entity, 0);
					targetEntities.put(entity, entity.getLocation().clone());
				}
				Location newlocation = entity.getLocation().clone();
				Location location = targetEntities.get(entity);
				double distance = location.distance(newlocation);
				double dx, dy, dz;
				dx = location.getX() - newlocation.getX();
				dy = location.getY() - newlocation.getY();
				dz = location.getZ() - newlocation.getZ();
				Vector vector = new Vector(dx, dy, dz);
				if (distance > .5) {
					entity.setVelocity(vector.normalize().multiply(.5));
				} else {
					entity.setVelocity(new Vector(0, 0, 0));
				}
				new TempPotionEffect((LivingEntity) entity, effect);
				entity.setFallDistance(0);
				if (entity instanceof Creature) {
					((Creature) entity).setTarget(null);
				}

			}
			List<Entity> toRemove = new LinkedList<Entity>();
			for (Entity entity : targetEntities.keySet()) {
				if (!entities.contains(entity)) {
					toRemove.add(entity);
				}
			}
			for (Entity entity : toRemove) {
				targetEntities.remove(entity);
			}
		} else {
			List<Entity> toRemove = new LinkedList<Entity>();
			for (Entry<Entity, Location> entry : targetEntities.entrySet()) {
				Entity entity = entry.getKey();
				if (entity instanceof Player) {
					if (!EntityTools.canBeBloodbent((Player) entity)) {
						toRemove.add(entity);
						continue;
					}
				}
				Location newlocation = entity.getLocation();
				Location location = EntityTools.getTargetedLocation(player,
						(int) entry.getValue().distance(player.getLocation()));
				double distance = location.distance(newlocation);
				double dx, dy, dz;
				dx = location.getX() - newlocation.getX();
				dy = location.getY() - newlocation.getY();
				dz = location.getZ() - newlocation.getZ();
				Vector vector = new Vector(dx, dy/3, dz);
				if (distance > .5) {
					entity.setVelocity(vector.normalize().multiply(.5));
				} else {
					entity.setVelocity(new Vector(0, 0, 0));
				}
				new TempPotionEffect((LivingEntity) entity, effect);
				entity.setFallDistance(0);
				if (entity instanceof Creature) {
					((Creature) entity).setTarget(null);
				}
			}
			for (Entity entity : toRemove) {
				targetEntities.remove(entity);
			}
		}
		return true;
	}

	public static void progressAll() {
		List<Bloodbending> toRemove = new LinkedList<Bloodbending>();
		for (Bloodbending bloodBend : instances.values()) {
			boolean keep = bloodBend.progress();
			if (!keep) {
				toRemove.add(bloodBend);
			}
		}

		for (Bloodbending bloodBend : toRemove) {
			bloodBend.remove();
		}
	}

	private void remove() {
		BendingPlayer.getBendingPlayer(player).cooldown(Abilities.Bloodbending, COOLDOWN);
		instances.remove(player);
	}

	public static boolean isBloodbended(Entity entity) {
		for (Bloodbending bloodBend : instances.values()) {
			if (bloodBend.getTargetEntities().containsKey(entity)) {
				return true;
			}
		}
		return false;
	}

	public static Location getBloodbendingLocation(Entity entity) {
		for (Bloodbending bloodBend : instances.values()) {
			if (bloodBend.getTargetEntities().containsKey(entity)) {
				return bloodBend.getTargetEntities().get(entity);
			}
		}
		return null;
	}

	public static void removeAll() {
		instances.clear();
	}

	public Map<Entity, Location> getTargetEntities() {
		return targetEntities;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

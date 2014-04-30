package waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import tools.Abilities;
import tools.AvatarState;
import tools.ConfigManager;
import tools.TempPotionEffect;
import tools.Tools;

public class Bloodbending {

	public static ConcurrentHashMap<Player, Bloodbending> instances = new ConcurrentHashMap<Player, Bloodbending>();

	ConcurrentHashMap<Entity, Location> targetentities = new ConcurrentHashMap<Entity, Location>();

	private static final double factor = ConfigManager.bloodbendingThrowFactor;

	private Player player;
	private int range = ConfigManager.bloodbendingRange;

	public Bloodbending(Player player) {
		if (instances.containsKey(player)) {
			remove(player);
			return;
		}
		range = (int) Tools.waterbendingNightAugment(range, player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			for (Entity entity : Tools.getEntitiesAroundPoint(
					player.getLocation(), range)) {
				if (entity instanceof LivingEntity) {
					if (entity instanceof Player) {
						if (Tools.isRegionProtectedFromBuild(player,
								Abilities.Bloodbending, entity.getLocation())
								|| AvatarState.isAvatarState((Player) entity)
								|| entity.getEntityId() == player.getEntityId()
								|| Tools.canBend((Player) entity,
										Abilities.Bloodbending))
							continue;
					}
					Tools.damageEntity(player, entity, 0);
					targetentities.put(entity, entity.getLocation().clone());
				}
			}
		} else {
			Entity target = Tools.getTargettedEntity(player, range);
			if (target == null)
				return;
			if (!(target instanceof LivingEntity)
					|| Tools.isRegionProtectedFromBuild(player,
							Abilities.Bloodbending, target.getLocation()))
				return;
			if (target instanceof Player) {
				if (Tools.canBend((Player) target, Abilities.Bloodbending)
						|| AvatarState.isAvatarState((Player) target))
					return;
			}
			Tools.damageEntity(player, target, 0);
			targetentities.put(target, target.getLocation().clone());
		}
		this.player = player;
		instances.put(player, this);
	}

	public static void launch(Player player) {
		if (instances.containsKey(player))
			instances.get(player).launch();
	}

	private void launch() {
		Location location = player.getLocation();
		for (Entity entity : targetentities.keySet()) {
			double dx, dy, dz;
			Location target = entity.getLocation().clone();
			dx = target.getX() - location.getX();
			dy = target.getY() - location.getY();
			dz = target.getZ() - location.getZ();
			Vector vector = new Vector(dx, dy, dz);
			vector.normalize();
			entity.setVelocity(vector.multiply(factor));
		}
		remove(player);
	}

	private void progress() {
		PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 60, 1);

		if (!player.isSneaking()
				|| Tools.getBendingAbility(player) != Abilities.Bloodbending
				|| !Tools.canBend(player, Abilities.Bloodbending)) {
			remove(player);
			return;
		}
		if (AvatarState.isAvatarState(player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (Entity entity : Tools.getEntitiesAroundPoint(
					player.getLocation(), range)) {
				if (Tools.isRegionProtectedFromBuild(player,
						Abilities.Bloodbending, entity.getLocation()))
					continue;
				if (entity instanceof Player) {
					if (!Tools.canBeBloodbent((Player) entity))
						continue;
				}
				entities.add(entity);
				if (!targetentities.containsKey(entity)
						&& entity instanceof LivingEntity) {
					Tools.damageEntity(player, entity, 0);
					targetentities.put(entity, entity.getLocation().clone());
				}
				if (entity instanceof LivingEntity) {
					Location newlocation = entity.getLocation().clone();
					Location location = targetentities.get(entity);
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
			}
			for (Entity entity : targetentities.keySet()) {
				if (!entities.contains(entity))
					targetentities.remove(entity);
			}
		} else {
			for (Entity entity : targetentities.keySet()) {
				if (entity instanceof Player) {
					if (!Tools.canBeBloodbent((Player) entity)) {
						targetentities.remove(entity);
						continue;
					}
				}
				Location newlocation = entity.getLocation();
				Location location = Tools.getTargetedLocation(
						player,
						(int) targetentities.get(entity).distance(
								player.getLocation()));
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
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static void remove(Player player) {
		if (instances.containsKey(player)) {
			instances.remove(player);
		}
	}

	public static boolean isBloodbended(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				// if (entity instanceof Player) {
				// if (!Tools.canBeBloodbent((Player) entity))
				// return false;
				// }
				return true;
			}
		}
		return false;
	}

	public static Location getBloodbendingLocation(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targetentities.containsKey(entity)) {
				return instances.get(player).targetentities.get(entity);
			}
		}
		return null;
	}

	public static String getDescription() {
		return "This ability was made illegal for a reason. With this ability selected, sneak while "
				+ "targetting something and you will bloodbend that target. Bloodbent targets cannot move, "
				+ "bend or attack. You are free to control their actions by looking elsewhere - they will "
				+ "be forced to move in that direction. Additionally, clicking while bloodbending will "
				+ "launch that target off in the direction you're looking. "
				+ "People who are capable of bloodbending are immune to your technique, and you are immune to theirs.";
	}

}

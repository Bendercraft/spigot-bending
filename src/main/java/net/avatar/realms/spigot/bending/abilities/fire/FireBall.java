package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

@BendingAbility(name="Fire Ball", element=BendingType.Fire)
public class FireBall implements IAbility {

	private static Map<Integer, FireBall> instances = new HashMap<Integer, FireBall>();

	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 2000;
	
	@ConfigurationParameter("Radius")
	private static double RADIUS = 1.5;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 30000;
	
	@ConfigurationParameter("Speed")
	public static double SPEED = 0.3;
	
	private static long interval = 25;

	private static int ID = Integer.MIN_VALUE;

	private int id;
	private double range = 20;
	private int maxdamage = 4;
	private double explosionradius = 6;
	private double innerradius = 3;
	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private long starttime;
	private long time;
	private long chargetime = CHARGE_TIME;
	private boolean charged = false;
	private boolean launched = false;
	private TNTPrimed explosion = null;
	private IAbility parent;

	public FireBall(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		time = System.currentTimeMillis();
		starttime = time;
		if (Tools.isDay(player.getWorld())) {
			chargetime = (long) (chargetime / Settings.DAY_FACTOR);
		}
		if (AvatarState.isAvatarState(player)) {
			chargetime = 0;
			maxdamage = AvatarState.getValue(maxdamage);
		}
		range = PluginTools.firebendingDayAugment(range, player.getWorld());
		if (!player.getEyeLocation().getBlock().isLiquid()) {
			id = ID;
			instances.put(id, this);
			if (ID == Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			ID++;
		}

	}

	private boolean progress() {
		if ((!EntityTools.canBend(player, Abilities.FireBlast) 
				|| EntityTools.getBendingAbility(player) != Abilities.FireBlast) && !launched) {
			return false;
		}

		if (System.currentTimeMillis() > starttime + chargetime) {
			charged = true;
		}

		if (!player.isSneaking() && !charged) {
			new FireBlast(player, this);
			return false;
		}

		if (!player.isSneaking() && !launched) {
			launched = true;
			location = player.getEyeLocation();
			origin = location.clone();
			direction = location.getDirection().normalize().multiply(RADIUS);
		}

		if (System.currentTimeMillis() > time + interval) {
			if (launched)
				if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.Blaze,
						location)) {
					return false;
				}

			time = System.currentTimeMillis();

			if (!launched && !charged)
				return true;
			if (!launched) {
				player.getWorld().playEffect(player.getEyeLocation(),
						Effect.MOBSPAWNER_FLAMES, 0, 3);
				return true ;
			}

			location = location.clone().add(direction);
			if (location.distance(origin) > range) {
				return false;
			}

			if (BlockTools.isSolid(location.getBlock())) {
				explode();
				return false;
			} else if (location.getBlock().isLiquid()) {
				return false;
			}

			return fireball();
		}
		return true;
	}

	private void dealDamage(Entity entity) {
		if (explosion == null)
			return;
		// if (Tools.isObstructed(explosion.getLocation(),
		// entity.getLocation())) {
		// return 0;
		// }
		double distance = entity.getLocation()
				.distance(explosion.getLocation());
		if (distance > explosionradius)
			return;
		if (distance < innerradius) {
			EntityTools.damageEntity(player, entity, maxdamage);
			return;
		}
		double slope = -(maxdamage * .5) / (explosionradius - innerradius);

		double damage = slope * (distance - innerradius) + maxdamage;
		EntityTools.damageEntity(player, entity, (int) damage);
	}

	private boolean fireball() {
		for (Block block : BlockTools.getBlocksAroundPoint(location, RADIUS)) {
			block.getWorld().playEffect(block.getLocation(),
					Effect.MOBSPAWNER_FLAMES, 0, 20);
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(location, 2 * RADIUS)) {
			if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			entity.setFireTicks(120);
			if (entity instanceof LivingEntity) {
				explode();
				return false;
			}
		}
		return true;
	}
	
	private void explode() {
		for (Entity entity : EntityTools.getEntitiesAroundPoint(location, 2 * RADIUS)) {
			if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			if (entity instanceof LivingEntity) {
				dealDamage(entity);
			}
		}
		ignite(location);
	}

	public static boolean isCharging(Player player) {
		for (int id : instances.keySet()) {
			FireBall ball = instances.get(id);
			if (ball.player == player && !ball.launched)
				return true;
		}
		return false;
	}

	private void ignite(Location location) {
		for (Block block : BlockTools.getBlocksAroundPoint(location,
				FireBlast.AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(player, block)) {
				block.setType(Material.FIRE);
				if (FireBlast.DISSIPATES) {
					FireStream.addIgnitedBlock(block, player, System.currentTimeMillis());
				}
			}
		}
	}

	public static void progressAll() {
		List<FireBall> toRemove = new LinkedList<FireBall>();
		for (FireBall fireball : instances.values()) {
			boolean keep = fireball.progress();
			if(!keep) {
				toRemove.add(fireball);
			}
		}
		for(FireBall fireball : toRemove) {
			fireball.remove();
		}
	}

	public void remove() {
		instances.remove(id);
	}

	public static void removeAll() {
		instances.clear();
	}

	public static void removeFireballsAroundPoint(Location location,
			double radius) {
		List<FireBall> toRemove = new LinkedList<FireBall>();
		for (FireBall fireball : instances.values()) {
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					toRemove.add(fireball);
			}
		}
		for(FireBall fireball : toRemove) {
			fireball.remove();
		}
	}

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		boolean broke = false;
		List<FireBall> toRemove = new LinkedList<FireBall>();
		for (FireBall fireball : instances.values()) {
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()
					&& !source.equals(fireball.player)) {
				if (location.distance(fireblastlocation) <= radius) {
					fireball.explode();
					toRemove.add(fireball);
					broke = true;
				}
			}
		}
		
		for(FireBall fireball : toRemove) {
			fireball.remove();
		}

		return broke;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

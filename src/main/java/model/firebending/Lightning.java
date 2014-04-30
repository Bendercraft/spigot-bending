package model.firebending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import model.Abilities;
import model.AvatarState;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dataAccess.ConfigManager;
import business.Tools;

public class Lightning {

	public static int defaultdistance = ConfigManager.lightningrange;
	private static long defaultwarmup = ConfigManager.lightningwarmup;
	private static double misschance = ConfigManager.lightningmisschance;
	private static double threshold = 0.1;
	private static double blockdistance = 4;

	private int maxdamage = 6;
	private double strikeradius = 4;

	private Player player;
	private long starttime;
	private boolean charged = false;
	private LightningStrike strike = null;
	public static ConcurrentHashMap<Player, Lightning> instances = new ConcurrentHashMap<Player, Lightning>();
	private static ConcurrentHashMap<Entity, Lightning> strikes = new ConcurrentHashMap<Entity, Lightning>();
	private ArrayList<Entity> hitentities = new ArrayList<Entity>();

	public Lightning(Player player) {
		if (instances.containsKey(player)) {
			return;
		}
		this.player = player;
		starttime = System.currentTimeMillis();
		instances.put(player, this);

	}

	public static Lightning getLightning(Entity entity) {
		if (strikes.containsKey(entity))
			return strikes.get(entity);
		return null;
	}

	private void strike() {
		Location targetlocation = getTargetLocation();
		if (AvatarState.isAvatarState(player))
			maxdamage = AvatarState.getValue(maxdamage);
		if (!Tools.isRegionProtectedFromBuild(player, Abilities.Lightning,
				targetlocation)) {
			strike = player.getWorld().strikeLightning(targetlocation);
			strikes.put(strike, this);
		}
		instances.remove(player);
	}

	private Location getTargetLocation() {
		int distance = (int) Tools.firebendingDayAugment(defaultdistance,
				player.getWorld());

		Location targetlocation;
		targetlocation = Tools.getTargetedLocation(player, distance);
		Entity target = Tools.getTargettedEntity(player, distance);
		if (target != null) {
			if (target instanceof LivingEntity
					&& player.getLocation().distance(targetlocation) > target
							.getLocation().distance(player.getLocation())) {
				targetlocation = target.getLocation();
				if (target.getVelocity().length() < threshold)
					misschance = 0;
			}
		} else {
			misschance = 0;
		}

		if (targetlocation.getBlock().getType() == Material.AIR)
			targetlocation.add(0, -1, 0);
		if (targetlocation.getBlock().getType() == Material.AIR)
			targetlocation.add(0, -1, 0);

		if (misschance != 0 && !AvatarState.isAvatarState(player)) {
			double A = Math.random() * Math.PI * misschance * misschance;
			double theta = Math.random() * Math.PI * 2;
			double r = Math.sqrt(A) / Math.PI;
			double x = r * Math.cos(theta);
			double z = r * Math.sin(theta);

			targetlocation = targetlocation.add(x, 0, z);
		}

		return targetlocation;
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
			return;
		}

		if (Tools.getBendingAbility(player) != Abilities.Lightning) {
			instances.remove(player);
			return;
		}

		int distance = (int) Tools.firebendingDayAugment(defaultdistance,
				player.getWorld());
		long warmup = (int) ((double) defaultwarmup / ConfigManager.dayFactor);
		if (AvatarState.isAvatarState(player))
			warmup = 0;
		if (System.currentTimeMillis() > starttime + warmup)
			charged = true;

		if (charged) {
			if (player.isSneaking()) {
				player.getWorld().playEffect(
						player.getEyeLocation(),
						Effect.SMOKE,
						Tools.getIntCardinalDirection(player.getEyeLocation()
								.getDirection()), distance);
			} else {
				strike();
			}
		} else {
			if (!player.isSneaking()) {
				instances.remove(player);
			}
		}
	}

	public void dealDamage(Entity entity) {
		if (strike == null) {
			// Tools.verbose("Null strike");
			return;
		}
		// if (Tools.isObstructed(strike.getLocation(), entity.getLocation())) {
		// Tools.verbose("Is Obstructed");
		// return 0;
		// }
		if (hitentities.contains(entity)) {
			// Tools.verbose("Already hit");
			return;
		}
		double distance = entity.getLocation().distance(strike.getLocation());
		if (distance > strikeradius)
			return;
		double damage = maxdamage - (distance / strikeradius) * .5;
		hitentities.add(entity);
		Tools.damageEntity(player, entity, (int) damage);
	}

	public static boolean isNearbyChannel(Location location) {
		boolean value = false;
		for (Player player : instances.keySet()) {
			if (!player.getWorld().equals(location.getWorld()))
				continue;
			if (player.getLocation().distance(location) <= blockdistance) {
				value = true;
				instances.get(player).starttime = 0;
			}
		}
		return value;
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static String getDescription() {
		return "Hold sneak while selecting this ability to charge up a lightning strike. Once "
				+ "charged, release sneak to discharge the lightning to the targetted location.";
	}

}

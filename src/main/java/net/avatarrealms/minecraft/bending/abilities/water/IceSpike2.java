package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempBlock;
import net.avatarrealms.minecraft.bending.model.TempPotionEffect;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class IceSpike2 {

	private static ConcurrentHashMap<Integer, IceSpike2> instances = new ConcurrentHashMap<Integer, IceSpike2>();

	private static double defaultrange = 20;
	private static int defaultdamage = 1;
	private static int defaultmod = 2;
	private static int ID = Integer.MIN_VALUE;
	static long slowCooldown = 5000;

	private static final long interval = 20;
	private static final byte data = 0;
	private static final double affectingradius = 2;
	private static final double deflectrange = 3;

	private Player player;
	private int id;
	private double range;
	private boolean plantbending = false;
	private Block sourceblock;
	private TempBlock source;
	private boolean prepared = false;
	private boolean settingup = false;
	private boolean progressing = false;
	private long time;

	private Location location;
	private Location firstdestination;
	private Location destination;

	public IceSpike2(Player player) {
		block(player);
		if (Tools.canPlantbend(player))
			plantbending = true;
		range = Tools.waterbendingNightAugment(defaultrange, player.getWorld());
		this.player = player;
		Block sourceblock = Tools.getWaterSourceBlock(player, range,
				plantbending);

		if (sourceblock == null) {
			new SpikeField(player);
		} else {
			prepare(sourceblock);
		}

	}

	private void prepare(Block block) {
		for (IceSpike2 ice : getInstances(player)) {
			if (ice.prepared) {
				ice.cancel();
			}
		}
		sourceblock = block;
		location = sourceblock.getLocation();
		prepared = true;
		createInstance();
	}

	private void createInstance() {
		id = ID++;
		instances.put(id, this);
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
	}

	private static ArrayList<IceSpike2> getInstances(Player player) {
		ArrayList<IceSpike2> list = new ArrayList<IceSpike2>();
		for (int id : instances.keySet()) {
			IceSpike2 ice = instances.get(id);
			if (ice.player.equals(player)) {
				list.add(ice);
			}
		}

		return list;
	}

	public static void activate(Player player) {
		redirect(player);
		boolean activate = false;

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.IceSpike))
			return;

		for (IceSpike2 ice : getInstances(player)) {
			if (ice.prepared) {
				ice.throwIce();
				activate = true;
			}
		}

		if (!activate) {
			IceSpike spike = new IceSpike(player);
			if (spike.id == 0) {
				waterBottle(player);
			}
		}
	}

	private static void waterBottle(Player player) {
		if (WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (Tools.isTransparentToEarthbending(player, block)
					&& Tools.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {

				LivingEntity target = (LivingEntity) Tools.getTargettedEntity(
						player, defaultrange);
				Location destination;
				if (target == null) {
					destination = Tools.getTargetedLocation(player,
							defaultrange, Tools.transparentEarthbending);
				} else {
					destination = Tools.getPointOnLine(player.getEyeLocation(),
							target.getEyeLocation(), defaultrange);
				}

				if (destination.distance(block.getLocation()) < 1)
					return;

				block.setType(Material.WATER);
				block.setData((byte) 0x0);
				IceSpike2 ice = new IceSpike2(player);
				ice.throwIce();

				if (ice.progressing) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}

			}
		}
	}

	private void throwIce() {
		if (!prepared)
			return;
		LivingEntity target = (LivingEntity) Tools.getTargettedEntity(player,
				range);
		if (target == null) {
			destination = Tools.getTargetedLocation(player, range,
					Tools.transparentEarthbending);
		} else {
			destination = target.getEyeLocation();
		}

		location = sourceblock.getLocation();
		if (destination.distance(location) < 1)
			return;
		firstdestination = location.clone();
		if (destination.getY() - location.getY() > 2) {
			firstdestination.setY(destination.getY() - 1);
		} else {
			firstdestination.add(0, 2, 0);
		}
		destination = Tools
				.getPointOnLine(firstdestination, destination, range);
		progressing = true;
		settingup = true;
		prepared = false;

		if (Tools.isPlant(sourceblock)) {
			new Plantbending(sourceblock);
			sourceblock.setType(Material.AIR);
		} else if (!Tools.adjacentToThreeOrMoreSources(sourceblock)) {
			sourceblock.setType(Material.AIR);
		}

		source = new TempBlock(sourceblock, Material.ICE, data);
	}

	public static void progressAll() {
		for (int id : instances.keySet()) {
			instances.get(id).progress();
		}
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()
				|| !Tools.canBend(player, Abilities.IceSpike)) {
			cancel();
			return;
		}

		if (!player.getWorld().equals(location.getWorld())) {
			cancel();
			return;
		}

		if (player.getEyeLocation().distance(location) >= range) {
			if (progressing) {
				cancel();
				returnWater();
			} else {
				cancel();
			}
			return;
		}

		if (Tools.getBendingAbility(player) != Abilities.IceSpike && prepared) {
			cancel();
			return;
		}

		if (System.currentTimeMillis() < time + interval)
			return;

		time = System.currentTimeMillis();

		if (progressing) {

			Vector direction;

			if (location.getBlockY() == firstdestination.getBlockY())
				settingup = false;

			if (location.distance(destination) <= 2) {
				cancel();
				returnWater();
				return;
			}

			if (settingup) {
				direction = Tools.getDirection(location, firstdestination)
						.normalize();
			} else {
				direction = Tools.getDirection(location, destination)
						.normalize();
			}

			location.add(direction);

			Block block = location.getBlock();

			if (block.equals(sourceblock))
				return;

			source.revertBlock();
			source = null;

			if (Tools.isTransparentToEarthbending(player, block)
					&& !block.isLiquid()) {
				Tools.breakBlock(block);
			} else if (!Tools.isWater(block)) {
				cancel();
				returnWater();
				return;
			}

			if (Tools.isRegionProtectedFromBuild(player, Abilities.IceSpike,
					location)) {
				cancel();
				returnWater();
				return;
			}

			for (Entity entity : Tools.getEntitiesAroundPoint(location,
					affectingradius)) {
				if (entity.getEntityId() != player.getEntityId()
						&& entity instanceof LivingEntity) {
					affect((LivingEntity) entity);
					progressing = false;
					returnWater();
				}
			}

			if (!progressing) {
				cancel();
				return;
			}

			sourceblock = block;
			source = new TempBlock(sourceblock, Material.ICE, data);

		} else if (prepared) {
			Tools.playFocusWaterEffect(sourceblock);
		}
	}

	private void affect(LivingEntity entity) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		int mod = (int) Tools.waterbendingNightAugment(defaultmod,
				player.getWorld());
		double damage = (int) Tools.waterbendingNightAugment(defaultdamage,
				player.getWorld());
		damage = bPlayer.getCriticalHit(BendingType.Water,damage);
		if (((entity instanceof Player) ||(entity instanceof Monster)) && (entity.getEntityId() != player.getEntityId())){
			
			if (bPlayer != null) {
				bPlayer.earnXP(BendingType.Water);
			}
		}
		if (entity instanceof Player) {
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW,
						70, mod);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(slowCooldown);
				entity.damage(damage, player);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70,
					mod);
			new TempPotionEffect(entity, effect);
			entity.damage(damage, player);
		}

	}

	private static void redirect(Player player) {

		for (int id : instances.keySet()) {
			IceSpike2 ice = instances.get(id);

			if (!ice.progressing)
				continue;

			if (!ice.location.getWorld().equals(player.getWorld()))
				continue;

			if (ice.player.equals(player)) {
				Location location;
				Entity target = Tools.getTargettedEntity(player, defaultrange);
				if (target == null) {
					location = Tools.getTargetedLocation(player, defaultrange);
				} else {
					location = ((LivingEntity) target).getEyeLocation();
				}
				location = Tools.getPointOnLine(ice.location, location,
						defaultrange * 2);
				ice.redirect(location, player);
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = ice.location;
			if (Tools.isRegionProtectedFromBuild(player, Abilities.IceSpike,
					mloc))
				continue;
			if (mloc.distance(location) <= defaultrange
					&& Tools.getDistanceFromLine(vector, location, ice.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				Location loc;
				Entity target = Tools.getTargettedEntity(player, defaultrange);
				if (target == null) {
					loc = Tools.getTargetedLocation(player, defaultrange);
				} else {
					loc = ((LivingEntity) target).getEyeLocation();
				}
				loc = Tools.getPointOnLine(ice.location, loc, defaultrange * 2);
				ice.redirect(loc, player);
			}

		}
	}

	private static void block(Player player) {
		for (int id : instances.keySet()) {
			IceSpike2 ice = instances.get(id);

			if (ice.player.equals(player))
				continue;

			if (!ice.location.getWorld().equals(player.getWorld()))
				continue;

			if (!ice.progressing)
				continue;

			if (Tools.isRegionProtectedFromBuild(player, Abilities.IceSpike,
					ice.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = ice.location;
			if (mloc.distance(location) <= defaultrange
					&& Tools.getDistanceFromLine(vector, location, ice.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				ice.cancel();
			}

		}
	}

	private void redirect(Location destination, Player player) {
		this.destination = destination;
		this.player = player;
	}

	private void cancel() {
		if (progressing) {
			if (source != null)
				source.revertBlock();
			progressing = false;
		}

		instances.remove(id);
	}

	private void returnWater() {
		new WaterReturn(player, sourceblock);
	}

	public static void removeAll() {
		for (int id : instances.keySet()) {
			instances.get(id).cancel();
		}

		instances.clear();
	}

	public static boolean isBending(Player player) {
		for (int id : instances.keySet()) {
			if (instances.get(id).player.equals(player))
				return true;
		}
		return false;
	}
}

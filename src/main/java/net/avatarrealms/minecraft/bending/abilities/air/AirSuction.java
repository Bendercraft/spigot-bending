package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.water.WaterSpout;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirSuction implements IAbility {

	private static Map<Integer, AirSuction> instances = new HashMap<Integer, AirSuction>();
	private static Map<Player, Location> origins = new HashMap<Player, Location>();
	static final long soonesttime = Tools.timeinterval;

	private static int ID = Integer.MIN_VALUE;
	private static final int maxticks = AirBlast.maxticks;
	static final double maxspeed = AirBlast.maxspeed;

	private static double speed = ConfigManager.airSuctionSpeed;
	private static double range = ConfigManager.airSuctionRange;
	private static double affectingradius = ConfigManager.airSuctionRadius;
	private static double pushfactor = ConfigManager.airSuctionPush;
	private static double originselectrange = 10;

	private Location location;
	private Location origin;
	private Vector direction;
	private Player player;
	private int id;
	private int ticks = 0;
	private IAbility parent;

	private double speedfactor;

	public AirSuction(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.AirSuction))
			return;

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		if (AirSpout.getPlayers().contains(player)
				|| WaterSpout.getPlayers().contains(player))
			return;
		// timers.put(player, System.currentTimeMillis());
		this.player = player;
		if (origins.containsKey(player)) {
			origin = origins.get(player);
			origins.remove(player);
		} else {
			origin = player.getEyeLocation();
		}
		location = EntityTools.getTargetedLocation(player, range, BlockTools.nonOpaque);
		direction = Tools.getDirection(location, origin).normalize();
		Entity entity = EntityTools.getTargettedEntity(player, range);
		if (entity != null) {
			direction = Tools.getDirection(entity.getLocation(), origin)
					.normalize();
			location = getLocation(origin, direction.clone().multiply(-1));
		}

		id = ID;
		instances.put(id, this);
		bPlayer.cooldown(Abilities.AirSuction);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	private Location getLocation(Location origin, Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if (!BlockTools.isTransparentToEarthbending(player, location.getBlock())
					|| ProtectionManager.isRegionProtectedFromBending(player,
							Abilities.AirSuction, location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}

	public static void setOrigin(Player player) {
		Location location = EntityTools.getTargetedLocation(player,
				originselectrange, BlockTools.nonOpaque);
		if (location.getBlock().isLiquid()
				|| BlockTools.isSolid(location.getBlock()))
			return;

		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirSuction,
				location))
			return;

		origins.put(player, location);
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirSuction,
				location)) {
			return false;
		}
		speedfactor = speed * (Bending.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			return false;
		}


		if ((location.distance(origin) > range)
				|| (location.distance(origin) <= 1)) {
			return false;
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
				affectingradius)) {
			if (entity.getFireTicks() > 0) {
				entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
				entity.setFireTicks(0);
			}	
			if (entity.getEntityId() != player.getEntityId()) {
				Vector velocity = entity.getVelocity();
				double max = maxspeed;
				double factor = pushfactor;
				if (AvatarState.isAvatarState(player)) {
					max = AvatarState.getValue(maxspeed);
					factor = AvatarState.getValue(factor);
				}

				Vector push = direction.clone();
				if (Math.abs(push.getY()) > max
						&& entity.getEntityId() != player.getEntityId()) {
					if (push.getY() < 0)
						push.setY(-max);
					else
						push.setY(max);
				}

				factor *= 1 - location.distance(origin) / (2 * range);

				double comp = velocity.dot(push.clone().normalize());
				if (comp > factor) {
					velocity.multiply(.5);
					velocity.add(push
							.clone()
							.normalize()
							.multiply(
									velocity.clone().dot(
											push.clone().normalize())));
				} else if (comp + factor * .5 > factor) {
					velocity.add(push.clone().multiply(factor - comp));
				} else {
					velocity.add(push.clone().multiply(factor * .5));
				}
				entity.setVelocity(velocity);
				entity.setFallDistance(0);
				if (entity.getEntityId() != player.getEntityId()
						&& entity instanceof Player) {
					new Flight((Player) entity, player);
				}		
			}
		}		
		advanceLocation();
		return true;
	}
	
	private void remove() {
		instances.remove(id);
	}

	private void advanceLocation() {
		location.getWorld().playEffect(location, Effect.SMOKE, 4, (int) AirBlast.defaultrange);
		location = location.add(direction.clone().multiply(speedfactor));
	}

	public static void progressAll() {
		List<AirSuction> toRemove = new LinkedList<AirSuction>();
		for(AirSuction suction : instances.values()) {
			boolean keep = suction.progress();
			if(!keep) {
				toRemove.add(suction);
			}
		}
		
		for(AirSuction suction : toRemove) {
			suction.remove();
		}
		
		List<Player> toRemoveOrigin = new LinkedList<Player>();
		for (Player player : origins.keySet()) {
			boolean keep = playOriginEffect(player);
			if(!keep) {
				toRemoveOrigin.add(player);
			}
		}
		for(Player player : toRemoveOrigin) {
			origins.remove(player);
		}
	}

	private static boolean playOriginEffect(Player player) {
		if (!origins.containsKey(player))
			return true;
		Location origin = origins.get(player);
		if (!origin.getWorld().equals(player.getWorld())) {
			return false;
		}

		if (EntityTools.getBendingAbility(player) != Abilities.AirSuction
				|| !EntityTools.canBend(player, Abilities.AirSuction)) {
			return false;
		}

		if (origin.distance(player.getEyeLocation()) > originselectrange) {
			return false;
		}

		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
				(int) originselectrange);
		return true;
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

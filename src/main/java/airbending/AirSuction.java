package airbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import main.Bending;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tools.Abilities;
import tools.AvatarState;
import tools.BendingPlayer;
import tools.ConfigManager;
import tools.Flight;
import tools.Tools;
import waterbending.WaterSpout;

public class AirSuction {

	public static ConcurrentHashMap<Integer, AirSuction> instances = new ConcurrentHashMap<Integer, AirSuction>();
	private static ConcurrentHashMap<Player, Location> origins = new ConcurrentHashMap<Player, Location>();
	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();
	static final long soonesttime = Tools.timeinterval;

	private static int ID = Integer.MIN_VALUE;
	private static final int maxticks = AirBlast.maxticks;
	static final double maxspeed = AirBlast.maxspeed;

	private static double speed = ConfigManager.airSuctionSpeed;
	private static double range = ConfigManager.airSuctionRange;
	private static double affectingradius = ConfigManager.airSuctionRadius;
	private static double pushfactor = ConfigManager.airSuctionPush;
	private static double originselectrange = 10;
	// private static long interval = AirBlast.interval;

	private Location location;
	private Location origin;
	private Vector direction;
	private Player player;
	private boolean otherorigin = false;
	private int id;
	private int ticks = 0;
	// private long time;

	private double speedfactor;

	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirSuction(Player player) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + soonesttime) {
		// return;
		// }
		// }

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
			otherorigin = true;
			origins.remove(player);
		} else {
			origin = player.getEyeLocation();
		}
		// if (origins.containsKey(player)) {
		// origin = origins.get(player);
		// otherorigin = true;
		// location = Tools.getTargetedLocation(player, range);
		// origins.remove(player);
		// Entity entity = Tools.getTargettedEntity(player, range);
		// if (entity != null) {
		// direction = Tools.getDirection(entity.getLocation(), origin)
		// .normalize();
		// location = origin.clone().add(
		// direction.clone().multiply(-range));
		// } else {
		// direction = Tools.getDirection(location, origin).normalize();
		// }
		//
		// } else {
		location = Tools.getTargetedLocation(player, range, Tools.nonOpaque);
		direction = Tools.getDirection(location, origin).normalize();
		Entity entity = Tools.getTargettedEntity(player, range);
		if (entity != null) {
			direction = Tools.getDirection(entity.getLocation(), origin)
					.normalize();
			location = getLocation(origin, direction.clone().multiply(-1));
			// location =
			// origin.clone().add(direction.clone().multiply(-range));
		}
		// }

		id = ID;
		instances.put(id, this);
		bPlayer.cooldown(Abilities.AirSuction);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
		// time = System.currentTimeMillis();
		// timers.put(player, System.currentTimeMillis());
	}

	private Location getLocation(Location origin, Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if (!Tools.isTransparentToEarthbending(player, location.getBlock())
					|| Tools.isRegionProtectedFromBuild(player,
							Abilities.AirSuction, location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}

	public static void setOrigin(Player player) {
		Location location = Tools.getTargetedLocation(player,
				originselectrange, Tools.nonOpaque);
		if (location.getBlock().isLiquid()
				|| Tools.isSolid(location.getBlock()))
			return;

		if (Tools.isRegionProtectedFromBuild(player, Abilities.AirSuction,
				location))
			return;

		if (origins.containsKey(player)) {
			origins.replace(player, location);
		} else {
			origins.put(player, location);
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(id);
			return false;
		}
		if (Tools.isRegionProtectedFromBuild(player, Abilities.AirSuction,
				location)) {
			instances.remove(id);
			return false;
		}
		speedfactor = speed * (Bending.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			instances.remove(id);
			return false;
		}
		// if (player.isSneaking()
		// && Tools.getBendingAbility(player) == Abilities.AirSuction) {
		// new AirSuction(player);
		// }

		if ((location.distance(origin) > range)
				|| (location.distance(origin) <= 1)) {
			instances.remove(id);
			return false;
		}

		for (Entity entity : Tools.getEntitiesAroundPoint(location,
				affectingradius)) {
			// if (affectedentities.contains(entity))
			// continue;
			// affectedentities.add(entity);
			if (entity.getEntityId() != player.getEntityId() || otherorigin) {
				// Vector velocity = entity.getVelocity();
				// double mag = Math.abs(velocity.getY());
				// double max = maxspeed;
				// if (AvatarState.isAvatarState(player)) {
				// max = AvatarState.getValue(maxspeed);
				// velocity = velocity.clone().add(
				// direction.clone().multiply(
				// AvatarState.getValue(pushfactor)));
				// double newmag = Math.abs(velocity.getY());
				// if (newmag > mag) {
				// if (mag > max) {
				// velocity = velocity.clone().multiply(mag / newmag);
				// } else if (newmag > max) {
				// velocity = velocity.clone().multiply(max / newmag);
				// }
				// }
				// } else {
				// velocity = velocity.clone().add(
				// direction.clone().multiply(pushfactor));
				// double newmag = Math.abs(velocity.getY());
				// if (newmag > mag) {
				// if (mag > max) {
				// velocity = velocity.clone().multiply(mag / newmag);
				// } else if (newmag > max) {
				// velocity = velocity.clone().multiply(max / newmag);
				// }
				// }
				// }
				// if (entity instanceof Player)
				// velocity.multiply(2);
				// entity.setVelocity(velocity);
				// entity.setFallDistance(0);
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
				if (entity.getFireTicks() > 0)
					entity.getWorld().playEffect(entity.getLocation(),
							Effect.EXTINGUISH, 0);
				entity.setFireTicks(0);

			}
		}

		advanceLocation();

		return true;
	}

	private void advanceLocation() {
		location.getWorld().playEffect(location, Effect.SMOKE, 4,
				(int) AirBlast.defaultrange);
		location = location.add(direction.clone().multiply(speedfactor));
	}

	public static void progressAll() {
		for (int id : instances.keySet())
			instances.get(id).progress();
		for (Player player : origins.keySet()) {
			playOriginEffect(player);
		}
	}

	private static void playOriginEffect(Player player) {
		if (!origins.containsKey(player))
			return;
		Location origin = origins.get(player);
		if (!origin.getWorld().equals(player.getWorld())) {
			origins.remove(player);
			return;
		}

		if (Tools.getBendingAbility(player) != Abilities.AirSuction
				|| !Tools.canBend(player, Abilities.AirSuction)) {
			origins.remove(player);
			return;
		}

		if (origin.distance(player.getEyeLocation()) > originselectrange) {
			origins.remove(player);
			return;
		}

		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
				(int) originselectrange);
	}

	public static String getDescription() {
		return "To use, simply left-click in a direction. "
				+ "A gust of wind will originate as far as it can in that direction"
				+ " and flow towards you, sucking anything in its path harmlessly with it."
				+ " Skilled benders can use this technique to pull items from precarious locations. "
				+ "Additionally, tapping sneak will change the origin of your next "
				+ "AirSuction to your targeted location.";
	}

}

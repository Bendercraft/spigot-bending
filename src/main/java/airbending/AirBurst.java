package airbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tools.Abilities;
import tools.AvatarState;
import tools.BendingPlayer;
import tools.Tools;

public class AirBurst {

	private static ConcurrentHashMap<Player, AirBurst> instances = new ConcurrentHashMap<Player, AirBurst>();

	private static double threshold = 10;
	private static double pushfactor = 1.5;
	private static double deltheta = 10;
	private static double delphi = 10;

	private Player player;
	private long starttime;
	private long chargetime = 1750;
	private boolean charged = false;

	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirBurst(Player player) {
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.AirBurst))
			return;

		if (instances.containsKey(player))
			return;
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);
	}

	public AirBurst() {

	}

	public static void coneBurst(Player player) {
		if (instances.containsKey(player))
			instances.get(player).coneBurst();
	}

	private void coneBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			double angle = Math.toRadians(30);
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					if (direction.angle(vector) <= angle) {
						// Tools.verbose(direction.angle(vector));
						// Tools.verbose(direction);
						new AirBlast(location, direction.normalize(), player,
								pushfactor, this);
					}
				}
			}
		}
		// Tools.verbose("--" + AirBlast.instances.size() + "--");
		instances.remove(player);
	}

	private void sphereBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					new AirBlast(location, direction.normalize(), player,
							pushfactor, this);
				}
			}
		}
		// Tools.verbose("--" + AirBlast.instances.size() + "--");
		instances.remove(player);
	}

	public static void fallBurst(Player player) {
		if (!Tools.canBend(player, Abilities.AirBurst)
				|| Tools.getBendingAbility(player) != Abilities.AirBurst
				|| instances.containsKey(player)
				|| player.getFallDistance() < threshold) {
			return;
		}
		Location location = player.getLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 75; theta < 105; theta += deltheta) {
			double dphi = delphi / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				new AirBlast(location, direction.normalize(), player,
						pushfactor, new AirBurst());
			}
		}
	}

	void addAffectedEntity(Entity entity) {
		affectedentities.add(entity);
	}

	boolean isAffectedEntity(Entity entity) {
		return affectedentities.contains(entity);
	}

	private void progress() {
		if (!Tools.canBend(player, Abilities.AirBurst)
				|| Tools.getBendingAbility(player) != Abilities.AirBurst) {
			instances.remove(player);
			return;
		}
		if (System.currentTimeMillis() > starttime + chargetime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				sphereBurst();
			} else {
				instances.remove(player);
			}
		} else if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			location.getWorld().playEffect(
					location,
					Effect.SMOKE,
					Tools.getIntCardinalDirection(player.getEyeLocation()
							.getDirection()), 3);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet())
			instances.get(player).progress();
	}

	public static String getDescription() {
		return "AirBurst is one of the most powerful abilities in the airbender's arsenal. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of air in front of you, or click to release the burst in a sphere around you. "
				+ "Additionally, having this ability selected when you land on the ground from a "
				+ "large enough fall will create a burst of air around you.";
	}

	public static void removeAll() {
		instances.clear();

	}
}

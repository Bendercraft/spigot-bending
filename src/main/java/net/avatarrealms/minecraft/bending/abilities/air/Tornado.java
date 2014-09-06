package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Tornado implements IAbility {

	private static Map<Integer, Tornado> instances = new HashMap<Integer, Tornado>();

	private static double maxradius = ConfigManager.tornadoRadius;
	private static double maxheight = ConfigManager.tornadoHeight;
	private static double range = ConfigManager.tornadoRange;
	private static int numberOfStreams = (int) (.3 * (double) maxheight);
	private static double NPCpushfactor = ConfigManager.tornadoMobPush;
	private static double PCpushfactor = ConfigManager.tornadoPlayerPush;
	private static double speedfactor = 1;

	private double height = 2;
	private double radius = height / maxheight * maxradius;

	private Map<Integer, Integer> angles = new HashMap<Integer, Integer>();
	private Location origin;
	private Player player;
	private IAbility parent;

	public Tornado(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		
		origin = EntityTools.getTargetBlock(player, range).getLocation();
		origin.setY(origin.getY() - 1. / 10. * height);

		int angle = 0;
		for (int i = 0; i <= maxheight; i += (int) maxheight / numberOfStreams) {
			angles.put(i, angle);
			angle += 90;
			if (angle == 360)
				angle = 0;
		}

		new Flight(player);
		player.setAllowFlight(true);
		instances.put(player.getEntityId(), this);
	}
	
	private void remove() {
		instances.remove(player.getEntityId());
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (!EntityTools.canBend(player, Abilities.Tornado)
				|| player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		if ((EntityTools.getBendingAbility(player) != Abilities.Tornado)
				|| (!player.isSneaking())) {
			return false;
		}
		if (Tools
				.isRegionProtectedFromBuild(player, Abilities.AirBlast, origin)) {
			return false;
		}
		rotateTornado();
		return true;
	}

	private void rotateTornado() {
		origin = EntityTools.getTargetBlock(player, range).getLocation();

		double timefactor = height / maxheight;
		radius = timefactor * maxradius;

		if (origin.getBlock().getType() != Material.AIR) {
			origin.setY(origin.getY() - 1. / 10. * height);

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(origin, height)) {
				if (Tools.isRegionProtectedFromBuild(player,
						Abilities.AirBlast, entity.getLocation())) {
					continue;
				}
					
				double y = entity.getLocation().getY();
				double factor;
				if (y > origin.getY() && y < origin.getY() + height) {
					factor = (y - origin.getY()) / height;
					Location testloc = new Location(origin.getWorld(),
							origin.getX(), y, origin.getZ());
					if (testloc.distance(entity.getLocation()) < radius
							* factor) {
						double x, z, vx, vz, mag;
						double angle = 100;
						double vy = 0.7 * NPCpushfactor;
						angle = Math.toRadians(angle);

						x = entity.getLocation().getX() - origin.getX();
						z = entity.getLocation().getZ() - origin.getZ();

						mag = Math.sqrt(x * x + z * z);

						vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
						vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

						if (entity instanceof Player) {
							vy = 0.05 * PCpushfactor;
						}

						if (entity.getEntityId() == player.getEntityId()) {
							Vector direction = player.getEyeLocation()
									.getDirection().clone().normalize();
							vx = direction.getX();
							vz = direction.getZ();
							Location playerloc = player.getLocation();
							double py = playerloc.getY();
							double oy = origin.getY();
							double dy = py - oy;
							if (dy >= height * .95) {
								vy = 0;
							} else if (dy >= height * .85) {
								vy = 6.0 * (.95 - dy / height);
							} else {
								vy = .6;
							}
						}

						Vector velocity = entity.getVelocity();
						velocity.setX(vx);
						velocity.setZ(vz);
						velocity.setY(vy);
						velocity.multiply(timefactor);
						entity.setVelocity(velocity);
						entity.setFallDistance(0);

						if (entity instanceof Player) {
							new Flight((Player) entity);
						}
					}
				}
			}

			
			Map<Integer, Integer> toAdd = new HashMap<Integer, Integer>();
			for (Entry<Integer, Integer> entry : angles.entrySet()) {
				int i = entry.getKey();
				double x, y, z;
				double angle = (double) entry.getValue();
				angle = Math.toRadians(angle);
				double factor;

				y = origin.getY() + timefactor * (double) i;
				factor = (double) i / height;

				x = origin.getX() + timefactor * factor * radius
						* Math.cos(angle);
				z = origin.getZ() + timefactor * factor * radius
						* Math.sin(angle);

				Location effect = new Location(origin.getWorld(), x, y, z);
				if (!Tools.isRegionProtectedFromBuild(player,
						Abilities.AirBlast, effect))
					origin.getWorld().playEffect(effect, Effect.SMOKE, 4,
							(int) AirBlast.defaultrange);

				toAdd.put(i, angles.get(i) + 25 * (int) speedfactor);
			}
			angles.putAll(toAdd);
		}

		if (height < maxheight) {
			height += 1;
		}

		if (height > maxheight) {
			height = maxheight;
		}

	}

	public static void progressAll() {
		List<Tornado> toRemove = new LinkedList<Tornado>();
		for(Tornado tornado : instances.values()) {
			boolean keep = tornado.progress();
			if(!keep) {
				toRemove.add(tornado);
			}
		}
		
		for(Tornado tornado : toRemove) {
			tornado.remove();
		}
	}

	public static String getDescription() {
		return "To use, simply sneak (default: shift). "
				+ "This will create a swirling vortex at the targeted location. "
				+ "Any creature or object caught in the vortex will be launched up "
				+ "and out in some random direction. If another player gets caught "
				+ "in the vortex, the launching effect is minimal. Tornado can "
				+ "also be used to transport the user. If the user gets caught in his/her "
				+ "own tornado, his movements are much more manageable. Provided the user doesn't "
				+ "fall out of the vortex, it will take him to a maximum height and move him in "
				+ "the general direction he's looking. Skilled airbenders can scale anything "
				+ "with this ability.";
	}

	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for (Tornado tornado : instances.values()) {
			players.add(tornado.player);
		}
		return players;
	}

	public static void removeAll() {
		instances.clear();
	}
	
	@Override
	public IAbility getParent() {
		return parent;
	}

}

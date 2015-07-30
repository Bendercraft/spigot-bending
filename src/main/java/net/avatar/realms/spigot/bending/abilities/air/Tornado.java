package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@BendingAbility(name="Tornado", element=BendingType.Air, specialization=BendingSpecializationType.Tornado)
public class Tornado implements IAbility {
	private static Map<Integer, Tornado> instances = new HashMap<Integer, Tornado>();
	private static Map<UUID, Long> affecteds = new HashMap<UUID, Long>();
	
	@ConfigurationParameter("Fall-Imunity")
	private static long FALL_IMMUNITY = 5000;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;
	
	@ConfigurationParameter("Radius")
	private static double RADIUS = 10;
	
	@ConfigurationParameter("Height")
	private static double HEIGHT = 25;
	
	@ConfigurationParameter("Range")
	private static double RANGE = 25;
	
	@ConfigurationParameter("Mob-Push-Factor")
	private static double NPC_PUSH = 1.0;
	
	@ConfigurationParameter("Player-Push-Factor")
	private static double PC_PUSH = 1.0;
	
	private static int numberOfStreams = (int) (.3 * (double) HEIGHT);
	
	private static double speedfactor = 1;
	private static double MAX_AFFECTEDS = 5000;//ms

	private double height = 2;
	private double radius = height / HEIGHT * RADIUS;

	private Map<Integer, Integer> angles = new HashMap<Integer, Integer>();
	private Location origin;
	private Player player;
	private IAbility parent;
	
	

	public Tornado(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		
		if (!EntityTools.canBend(player, Abilities.Tornado)) {
			return;
		}
		
		origin = EntityTools.getTargetBlock(player, RANGE).getLocation();
		origin.setY(origin.getY() - 1. / 10. * height);

		int angle = 0;
		for (int i = 0; i <= HEIGHT; i += (int) HEIGHT / numberOfStreams) {
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
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirBlast, origin)) {
			return false;
		}
		rotateTornado();
		return true;
	}

	private void rotateTornado() {
		origin = EntityTools.getTargetBlock(player, RANGE).getLocation();

		double timefactor = height / HEIGHT;
		radius = timefactor * RADIUS;

		if (origin.getBlock().getType() != Material.AIR) {
			origin.setY(origin.getY() - 1. / 10. * height);

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(origin, height)) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(player,
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
						double vy = NPC_PUSH;
						angle = Math.toRadians(angle);

						x = entity.getLocation().getX() - origin.getX();
						z = entity.getLocation().getZ() - origin.getZ();

						mag = Math.sqrt(x * x + z * z);

						vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
						vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

						if (entity instanceof Player) {
							double dy = y - origin.getY();
							if (dy >= height * .95) {
								vy = 0;
							} else if (dy >= height * .85) {
								vy = 6.0 * (.95 - dy / height);
							} else {
								vy = PC_PUSH;
							}
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
						else {
							entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20*10, 1));
						}

						Vector velocity = entity.getVelocity();
						velocity.setX(vx);
						velocity.setZ(vz);
						velocity.setY(vy);
						velocity.multiply(timefactor*0.75);
						entity.setVelocity(velocity);
						entity.setFallDistance((float) (entity.getFallDistance()/3.0));

						if (entity instanceof Player) {
							new Flight((Player) entity);
						}
					}
				}
				affecteds.put(entity.getUniqueId(), System.currentTimeMillis());
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
				if (!ProtectionManager.isRegionProtectedFromBending(player,
						Abilities.AirBlast, effect))
					origin.getWorld().playEffect(effect, Effect.SMOKE, 4,
							(int) AirBlast.DEFAULT_RANGE);

				toAdd.put(i, angles.get(i) + 25 * (int) speedfactor);
			}
			angles.putAll(toAdd);
		}

		if (height < HEIGHT) {
			height += 1;
		}

		if (height > HEIGHT) {
			height = HEIGHT;
		}

	}
	
	public static boolean isAffected(LivingEntity entity) {
		if(affecteds.containsKey(entity.getUniqueId())) {
			if(System.currentTimeMillis() - affecteds.get(entity.getUniqueId()) > MAX_AFFECTEDS) {
				affecteds.remove(entity.getUniqueId());
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static boolean preventFall(LivingEntity entity) {
		if(isAffected(entity)) {
			long old = affecteds.get(entity.getUniqueId());
			long diff = System.currentTimeMillis() - old;
			
			affecteds.remove(entity.getUniqueId());
			if(diff < FALL_IMMUNITY) {
				return true;
			}
		}
		return false;
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

	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for (Tornado tornado : instances.values()) {
			players.add(tornado.player);
		}
		return players;
	}

	public static void removeAll() {
		instances.clear();
		affecteds.clear();
	}
	
	@Override
	public IAbility getParent() {
		return parent;
	}

}

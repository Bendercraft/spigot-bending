package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.abilities.fire.Illumination;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirSwipe {
	private static Map<Integer, AirSwipe> instances = new HashMap<Integer, AirSwipe>();

	private static int ID = Integer.MIN_VALUE;
	private static List<Material> breakables = new ArrayList<Material>();
	static {
		breakables.add(Material.SAPLING);
		breakables.add(Material.DEAD_BUSH);
		breakables.add(Material.LONG_GRASS);
		breakables.add(Material.YELLOW_FLOWER);
		breakables.add(Material.RED_ROSE);
		breakables.add(Material.BROWN_MUSHROOM);
		breakables.add(Material.RED_MUSHROOM);
		breakables.add(Material.CROPS);
		breakables.add(Material.CACTUS);
		breakables.add(Material.SUGAR_CANE);
		breakables.add(Material.VINE);
	};

	private static int defaultdamage = ConfigManager.airSwipeDamage;
	private static double affectingradius = ConfigManager.airSwipeRadius;
	private static double defaultpushfactor = ConfigManager.airSwipePush;
	private static double range = ConfigManager.airSwipeRange;
	private static int arc = ConfigManager.airSwipeArc;
	private static int stepsize = 4;
	private static double speed = ConfigManager.airSwipeSpeed;
	private static byte full = AirBlast.full;
	private static long maxchargetime = 3000;
	private static double maxfactor = 3;

	private double speedfactor;

	private Location origin;
	private Player player;
	private boolean charging = false;
	private long time;
	private int damage = defaultdamage;
	private double pushfactor = defaultpushfactor;
	private int id;
	private Map<Vector, Location> elements = new HashMap<Vector, Location>();
	private List<Entity> affectedentities = new ArrayList<Entity>();

	public AirSwipe(Player player) {
		this(player, false);
	}

	public AirSwipe(Player player, boolean charging) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.AirSwipe))
			return;

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		this.player = player;
		this.charging = charging;
		origin = player.getEyeLocation();
		time = System.currentTimeMillis();

		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		id = ID++;
		Bukkit.getLogger().info("create "+id);

		instances.put(id, this);

		bPlayer.cooldown(Abilities.AirSwipe);

		if (!charging)
			launch();

		// timers.put(player, System.currentTimeMillis());
	}

	private void launch() {
		Bukkit.getLogger().info("launch");
		origin = player.getEyeLocation();
		for (int i = -arc; i <= arc; i += stepsize) {
			double angle = Math.toRadians((double) i);
			Vector direction = player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			elements.put(direction, origin);
		}
		
	}
	
	private void remove() {
		Bukkit.getLogger().info("removed "+id);
		instances.remove(id);
	}
	
	public static void removeAll() {
		instances.clear();
	}

	public boolean progress() {
		Bukkit.getLogger().info("progress "+instances.size());
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		speedfactor = speed * (Bending.time_step / 1000.);
		if (!charging) {
			if (elements.isEmpty()) {
				return false;
			}
			return advanceSwipe();
		} else {
			if (EntityTools.getBendingAbility(player) != Abilities.AirSwipe
					|| !EntityTools.canBend(player, Abilities.AirSwipe)) {
				return false;
			}

			if (!player.isSneaking()) {
				double factor = 1;
				if (System.currentTimeMillis() >= time + maxchargetime) {
					factor = maxfactor;
					Logger.getLogger("Bending").info(player.getName() + " has launched his aiswipe");
				} else {
					factor = maxfactor
							* (double) (System.currentTimeMillis() - time)
							/ (double) maxchargetime;
				}
				charging = false;				
				launch();
				if (factor < 1)
					factor = 1;
				damage *= factor;
				pushfactor *= factor;
				return true;
			} else if (System.currentTimeMillis() >= time + maxchargetime) {
				player.getWorld().playEffect(
						player.getEyeLocation(),
						Effect.SMOKE,
						Tools.getIntCardinalDirection(player.getEyeLocation()
								.getDirection()), 3);
			}
		}
		return true;
	}

	private boolean advanceSwipe() {
		Bukkit.getLogger().info("advanceSwipe");
		affectedentities.clear();
		
		Map<Vector, Location> toAdd = new HashMap<Vector, Location>();
		List<Vector> toRemove = new LinkedList<Vector>();
		for(Entry<Vector, Location> entry : elements.entrySet()) {
			Vector direction = entry.getKey();
			Location location = entry.getValue();
			if (direction != null && location != null) {
				location = location.clone().add(
						direction.clone().multiply(speedfactor));
				toAdd.put(direction, location);

				if (location.distance(origin) > range
						|| Tools.isRegionProtectedFromBuild(player,
								Abilities.AirSwipe, location)) {
					toRemove.add(direction);
				} else {
					PluginTools.removeSpouts(location, player);

					double radius = FireBlast.affectingradius;
					Player source = player;
					if (EarthBlast.annihilateBlasts(location, radius, source)
							|| WaterManipulation.annihilateBlasts(location,
									radius, source)
							|| FireBlast.annihilateBlasts(location, radius,
									source)) {
						toRemove.add(direction);
						damage = 0;
						continue;
					}

					Block block = location.getBlock();
					for (Block testblock : BlockTools.getBlocksAroundPoint(location,
							affectingradius)) {
						if (testblock.getType() == Material.FIRE) {
							testblock.setType(Material.AIR);
						}
						if (isBlockBreakable(testblock)) {
							BlockTools.breakBlock(testblock);
						}
					}

					if (block.getType() != Material.AIR) {
						if (isBlockBreakable(block)) {
							BlockTools.breakBlock(block);
						} else {
							toRemove.add(direction);
						}
						if (block.getType() == Material.LAVA
								|| block.getType() == Material.STATIONARY_LAVA) {
							if (block.getData() == full) {
								block.setType(Material.OBSIDIAN);
							} else {
								block.setType(Material.COBBLESTONE);
							}
						}
					} else {
						location.getWorld().playEffect(location, Effect.SMOKE,
								4, (int) AirBlast.defaultrange);
						
						//TODO CRASH TEST start here
						//Check affected people
						PluginTools.removeSpouts(location, player);
						BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
						for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
								affectingradius)) {
							if (Tools.isRegionProtectedFromBuild(player, Abilities.AirSwipe,
									entity.getLocation()))
								continue;
							if (entity.getEntityId() != player.getEntityId()) {
								if (AvatarState.isAvatarState(player)) {
									entity.setVelocity(direction.multiply(AvatarState
											.getValue(pushfactor)));
								} else {
									entity.setVelocity(direction.multiply(pushfactor));
								}

								if (entity instanceof LivingEntity
										&& !affectedentities.contains(entity)) {
									if (damage != 0)
										EntityTools.damageEntity(player, entity, bPlayer.getCriticalHit(BendingType.Air,damage));
									affectedentities.add(entity);
									
									if (((entity instanceof Player) ||(entity instanceof Monster)) && (entity.getEntityId() != player.getEntityId())) {			
										if (bPlayer != null) {
											bPlayer.earnXP(BendingType.Air);
										}
									}
								}

								if (entity instanceof Player) {
									new Flight((Player) entity, player);
								}

								if (elements.containsKey(direction)) {
									toRemove.add(direction);
								}
							}
						}
						//CRASH TEST end here
					}
				}
			}
		}
		
		elements.putAll(toAdd);
		for(Vector direction : toRemove) {
			elements.remove(direction);
		}

		if (elements.isEmpty()) {
			return false;
		}
		Bukkit.getLogger().info("advanceSwipe elements "+elements.size());
		Bukkit.getLogger().info("advanceSwipe toAdd "+toAdd.size());
		Bukkit.getLogger().info("advanceSwipe toRemove "+toRemove.size());
		return true;
	}

	private boolean isBlockBreakable(Block block) {
		if (breakables.contains(block.getType())
				&& !Illumination.blocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static void progressAll() {
		List<AirSwipe> toRemove = new LinkedList<AirSwipe>();
		for(AirSwipe swipe : instances.values()) {
			boolean keep = swipe.progress();
			if(!keep) {
				toRemove.add(swipe);
			}
		}
		
		for(AirSwipe swipe : toRemove) {
			swipe.remove();
		}
	}

	public static String getDescription() {
		return "To use, simply left-click in a direction. "
				+ "An arc of air will flow from you towards that direction, "
				+ "cutting and pushing back anything in its path. "
				+ "Its damage is minimal, but it still sends the message. "
				+ "This ability will extinguish fires, cool lava, and cut things like grass, "
				+ "mushrooms and flowers. Additionally, you can charge it by holding sneak. "
				+ "Charging before attacking will increase damage and knockback, up to a maximum.";
	}

	public static void charge(Player player) {
		new AirSwipe(player, true);
	}

}

package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.abilities.fire.Illumination;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirSwipe implements IAbility {
	private static Map<Integer, AirSwipe> instances = new HashMap<Integer, AirSwipe>();

	private static int ID = Integer.MIN_VALUE;
	private static List<Material> breakables = new ArrayList<Material>();
	static {
		breakables.add(Material.SAPLING);
		breakables.add(Material.DEAD_BUSH);
		breakables.add(Material.LONG_GRASS);
		breakables.add(Material.DOUBLE_PLANT);
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
	private IAbility parent;

	public AirSwipe(Player player, IAbility parent) {
		this(player, false, parent);
	}

	public AirSwipe(Player player, boolean charging, IAbility parent) {
		this.parent = parent;
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

		instances.put(id, this);

		bPlayer.cooldown(Abilities.AirSwipe);

		if (!charging)
			launch();

		// timers.put(player, System.currentTimeMillis());
	}

	private void launch() {
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
		instances.remove(id);
	}
	
	public static void removeAll() {
		instances.clear();
	}

	public boolean progress() {
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
				} else {
					factor = maxfactor
							* (double) (System.currentTimeMillis() - time)
							/ (double) maxchargetime;
				}
				charging = false;				
				launch();
				if (factor < 1) {
					factor = 1;
				}				
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
		affectedentities.clear();
		
		//Basically, AirSwipe is  just a set of smoke effect on some location called "elements"
		Map<Vector, Location> toAdd = new HashMap<Vector, Location>();
		
		for(Entry<Vector, Location> entry : elements.entrySet()) {
			Vector direction = entry.getKey();
			Location location = entry.getValue();
			if (direction != null && location != null) {
				//For each elements, we calculate the next one and check afterwards if it is still in range
				Location newlocation = location.clone().add(
						direction.clone().multiply(speedfactor));
				if (newlocation.distance(origin) <= range
						&& !Tools.isRegionProtectedFromBuild(player,
								Abilities.AirSwipe, newlocation)) {
					//If new location is still valid, we add it
					if (!BlockTools.isSolid(newlocation.getBlock()) || BlockTools.isPlant(newlocation.getBlock())) {
						toAdd.put(direction, newlocation);
					}
					
				}
			}
		}
		elements.clear();
		elements.putAll(toAdd);
		List<Vector> toRemove = new LinkedList<Vector>();
		for(Entry<Vector, Location> entry : elements.entrySet()) {
			Vector direction = entry.getKey();
			Location location = entry.getValue();
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
			
				//Check affected people
				PluginTools.removeSpouts(location, player);
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location,
						affectingradius)) {
					if (Tools.isRegionProtectedFromBuild(player, Abilities.AirSwipe,
							entity.getLocation())) {
						continue;
					}
						
					if (entity.getEntityId() != player.getEntityId()) {
						if (AvatarState.isAvatarState(player)) {
							entity.setVelocity(direction.multiply(AvatarState
									.getValue(pushfactor)));
						} else {
							entity.setVelocity(direction.multiply(pushfactor));
						}
						
						if (!affectedentities.contains(entity)) {
							if (damage != 0)
								EntityTools.damageEntity(player, entity, damage);
							affectedentities.add(entity);
						}
						
						if (entity instanceof Player) {
							new Flight((Player) entity, player);
						}

						toRemove.add(direction);
					}
				}
			}
		}
		
		for(Vector direction : toRemove) {
			elements.remove(direction);
		}

		if (elements.isEmpty()) {
			return false;
		}
		return true;
	}

	private boolean isBlockBreakable(Block block) {
		if (breakables.contains(block.getType())
				&& !Illumination.isIlluminated(block)) {
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
		new AirSwipe(player, true, null);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.Illumination;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * State Preparing = Player is charging his Airswipe State Prepared = Player has
 * charged his Airswipe State Progressing = Player has thrown his Airswipe
 */
@BendingAbility(name = "Air Swipe", bind = BendingAbilities.AirSwipe, element = BendingElement.Air)
public class AirSwipe extends BendingActiveAbility {

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

	@ConfigurationParameter("Base-Damage")
	private static int DAMAGE = 5;

	@ConfigurationParameter("Affecting-Radius")
	private static double AFFECTING_RADIUS = 2.0;

	@ConfigurationParameter("Push-Factor")
	private static double PUSHFACTOR = 1.0;

	@ConfigurationParameter("Range")
	private static double RANGE = 16.0;

	@ConfigurationParameter("Arc")
	private static int ARC = 20;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

	@ConfigurationParameter("Speed")
	private static double SPEED = 25;

	@ConfigurationParameter("Max-Charge-Time")
	private static long MAX_CHARGE_TIME = 3000;

	private static int stepsize = 4;
	private static byte full = AirBlast.full;
	private static double maxfactor = 3;

	private double speedfactor;

	private Location origin;
	private int damage = DAMAGE;
	private double pushfactor = PUSHFACTOR;
	private int id;
	private Map<Vector, Location> elements = new HashMap<Vector, Location>();
	private List<Entity> affectedentities = new ArrayList<Entity>();

	private double range;
	private int arc;

	public AirSwipe(Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		this.id = ID++;
		this.speedfactor = SPEED * (Bending.time_step / 1000.);

		this.arc = ARC;
		this.range = RANGE;

		if (this.bender.hasPath(BendingPath.Mobile)) {
			this.arc *= 0.5;
			this.range *= 1.4;
		}
	}

	@Override
	public boolean sneak() {
		switch (this.state) {
		case None:
		case CannotStart:
			return true;
		case CanStart:
			setState(BendingAbilityState.Preparing);
			AbilityManager.getManager().addInstance(this);
			return false;
		case Preparing:
		case Prepared:
		case Progressing:
		case Ended:
		case Removed:
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean swing() {
		switch (this.state) {
		case None:
		case CannotStart:
			return true;
		case CanStart:
			setState(BendingAbilityState.Progressing);
			AbilityManager.getManager().addInstance(this);
		case Preparing:
		case Prepared:
			this.setState(BendingAbilityState.Progressing);
		case Progressing:
			this.origin = this.player.getEyeLocation();
			launch();
			return false;
		case Ended:
		case Removed:
			return false;
		default:
			return false;
		}
	}

	private void launch() {
		this.origin = this.player.getEyeLocation();
		for (int i = -this.arc; i <= this.arc; i += stepsize) {
			double angle = Math.toRadians(i);
			Vector direction = this.player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = (x * Math.cos(angle)) - (z * Math.sin(angle));
			vz = (x * Math.sin(angle)) + (z * Math.cos(angle));

			direction.setX(vx);
			direction.setZ(vz);

			this.elements.put(direction, this.origin);
		}
	}

	@Override
	public void remove() {
		this.bender.cooldown(BendingAbilities.AirSwipe, COOLDOWN);
		super.remove();
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if ((EntityTools.getBendingAbility(this.player) != BendingAbilities.AirSwipe)) {
			return false;
		}

		long now = System.currentTimeMillis();

		if (this.state.equals(BendingAbilityState.Preparing)) {
			if (this.player.isSneaking()) {
				if (now >= (this.startedTime + MAX_CHARGE_TIME)) {
					setState(BendingAbilityState.Prepared);
					this.damage *= maxfactor;
					this.pushfactor *= maxfactor;
					return true;
				}
			}
		}

		if (this.state.equals(BendingAbilityState.Prepared)) {
			this.player.getWorld().playEffect(this.player.getEyeLocation(), Effect.SMOKE, Tools.getIntCardinalDirection(this.player.getEyeLocation().getDirection()), 3);
		}

		if (this.state.equals(BendingAbilityState.Progressing)) {
			if (this.elements.isEmpty()) {
				return false;
			}
			return advanceSwipe();
		}

		if (!this.player.isSneaking()) {
			if (!this.state.equals(BendingAbilityState.Prepared)) {
				double factor = (maxfactor * (now - this.startedTime)) / MAX_CHARGE_TIME;
				if (factor < 1) {
					factor = 1;
				}
				this.damage *= factor;
				this.pushfactor *= factor;
			}

			launch();
			this.setState(BendingAbilityState.Progressing);
			return true;
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	private boolean advanceSwipe() {
		this.affectedentities.clear();

		// Basically, AirSwipe is just a set of smoke effect on some location
		// called "elements"
		Map<Vector, Location> toAdd = new HashMap<Vector, Location>();

		for (Entry<Vector, Location> entry : this.elements.entrySet()) {
			Vector direction = entry.getKey();
			Location location = entry.getValue();
			if ((direction != null) && (location != null)) {
				// For each elements, we calculate the next one and check
				// afterwards if it is still in range
				Location newlocation = location.clone().add(direction.clone().multiply(this.speedfactor));
				if ((newlocation.distance(this.origin) <= this.range) && !ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.AirSwipe, newlocation)) {
					// If new location is still valid, we add it
					if (!BlockTools.isSolid(newlocation.getBlock()) || BlockTools.isPlant(newlocation.getBlock())) {
						toAdd.put(direction, newlocation);
					}

				}
			}
		}
		this.elements.clear();
		this.elements.putAll(toAdd);
		List<Vector> toRemove = new LinkedList<Vector>();
		for (Entry<Vector, Location> entry : this.elements.entrySet()) {
			Vector direction = entry.getKey();
			Location location = entry.getValue();
			PluginTools.removeSpouts(location, this.player);

			double radius = FireBlast.AFFECTING_RADIUS;
			Player source = this.player;
			if (EarthBlast.annihilateBlasts(location, radius, source) || WaterManipulation.annihilateBlasts(location, radius, source) || FireBlast.annihilateBlasts(location, radius, source)) {
				toRemove.add(direction);
				this.damage = 0;
				continue;
			}

			Block block = location.getBlock();
			for (Block testblock : BlockTools.getBlocksAroundPoint(location, AFFECTING_RADIUS)) {
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
				if ((block.getType() == Material.LAVA) || ((block.getType() == Material.STATIONARY_LAVA) && !BlockTools.isTempBlock(block))) {
					if (block.getData() == full) {
						block.setType(Material.OBSIDIAN);
					} else {
						block.setType(Material.COBBLESTONE);
					}
				}
			} else {
				location.getWorld().playEffect(location, Effect.SMOKE, 4, (int) AirBlast.DEFAULT_RANGE);

				// Check affected people
				PluginTools.removeSpouts(location, this.player);
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, AFFECTING_RADIUS)) {
					if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
						continue;
					}
					if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.AirSwipe, entity.getLocation())) {
						continue;
					}

					if (entity.getEntityId() != this.player.getEntityId()) {
						if (AvatarState.isAvatarState(this.player)) {
							entity.setVelocity(direction.multiply(AvatarState.getValue(this.pushfactor)));
						} else {
							entity.setVelocity(direction.multiply(this.pushfactor));
						}

						if (!this.affectedentities.contains(entity)) {
							if (this.damage != 0) {
								EntityTools.damageEntity(this.player, entity, this.damage);
							}
							this.affectedentities.add(entity);
						}

						// if (entity instanceof Player) {
						// new Flight((Player) entity, this.player);
						// }

						toRemove.add(direction);
					}
				}
			}
		}

		for (Vector direction : toRemove) {
			this.elements.remove(direction);
		}

		if (this.elements.isEmpty()) {
			return false;
		}
		return true;
	}

	private boolean isBlockBreakable(Block block) {
		if (breakables.contains(block.getType()) && !Illumination.isIlluminated(block)) {
			return true;
		}
		return false;
	}

	@Override
	protected long getMaxMillis() {
		return 5 * 60 * 1000;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

}

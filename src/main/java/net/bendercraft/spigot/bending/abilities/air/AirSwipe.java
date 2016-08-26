package net.bendercraft.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

/**
 * State Preparing = Player is charging his Airswipe State Prepared = Player has
 * charged his Airswipe State Progressing = Player has thrown his Airswipe
 */
@ABendingAbility(name = AirSwipe.NAME, element = BendingElement.AIR)
public class AirSwipe extends BendingActiveAbility {
	public final static String NAME = "AirSwipe";
	
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

	@ConfigurationParameter ("Charge-Max-Factor")
	private static double MAX_FACTOR = 2.5;

	private static int stepsize = 4;


	private double speedfactor;

	private Location origin;
	private int damage = DAMAGE;
	private double pushfactor = PUSHFACTOR;
	private int id;
	private Map<Vector, Location> elements = new HashMap<Vector, Location>();
	private List<Entity> affectedEntities = new ArrayList<Entity>();

	private double range;
	private int arc;

	public AirSwipe(RegisteredAbility register, Player player) {
		super(register, player);

		this.id = ID++;
		this.speedfactor = SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);

		this.arc = ARC;
		this.range = RANGE;

		if (this.bender.hasPath(BendingPath.MOBILE)) {
			this.arc *= 0.5;
			this.range *= 1.4;
		}
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARED);
		} else if(getState() == BendingAbilityState.PREPARED) {
			this.setState(BendingAbilityState.PROGRESSING);
			this.origin = this.player.getEyeLocation();
			launch();
		}
		return false;
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
		this.bender.cooldown(NAME, COOLDOWN);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!getState().equals(BendingAbilityState.PROGRESSING) && (!bender.getAbility().equals(NAME))) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if (getState().equals(BendingAbilityState.PREPARING)) {
			if (this.player.isSneaking()) {
				Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
				player.getWorld().spawnParticle(Particle.SPELL, loc, 1, 0, 0, 0, 0);
				if (now >= (this.startedTime + MAX_CHARGE_TIME)) {
					setState(BendingAbilityState.PREPARED);
					this.damage *= MAX_FACTOR;
					this.pushfactor *= MAX_FACTOR;
					return;
				}
			}
		}

		if (getState().equals(BendingAbilityState.PREPARED)) {
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
		}

		if (getState().equals(BendingAbilityState.PROGRESSING)) {
			if (this.elements.isEmpty()) {
				remove();
				return;
			}
			advanceSwipe();
			return;
		}

		if (!this.player.isSneaking()) {
			if (!getState().equals(BendingAbilityState.PREPARED)) {
				double factor = (MAX_FACTOR * (now - this.startedTime)) / MAX_CHARGE_TIME;
				if (factor < 1) {
					factor = 1;
				}
				this.damage *= factor;
				this.pushfactor *= factor;
			}

			launch();
			this.setState(BendingAbilityState.PROGRESSING);
		}
	}

	@SuppressWarnings("deprecation")
	private boolean advanceSwipe() {
		this.affectedEntities.clear();

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
				if ((newlocation.distance(this.origin) <= this.range) && !ProtectionManager.isLocationProtectedFromBending(this.player, register, newlocation)) {
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
				if ((block.getType() == Material.LAVA) || ((block.getType() == Material.STATIONARY_LAVA) && !TempBlock.isTempBlock(block))) {
					if (block.getData() == BlockTools.FULL) {
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
					if(affect(entity, direction)) {
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
		if (breakables.contains(block.getType())) {
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

	@Override
	public void stop() {
		
	}
	
	private boolean affect(Entity entity, Vector direction) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}

		if (entity != player) {
			if (AvatarState.isAvatarState(this.player)) {
				entity.setVelocity(direction.multiply(AvatarState.getValue(this.pushfactor)));
			} else {
				entity.setVelocity(direction.multiply(this.pushfactor));
			}

			if (!this.affectedEntities.contains(entity)) {
				if (this.damage != 0) {
					DamageTools.damageEntity(bender, entity, this, this.damage);
				}
				this.affectedEntities.add(entity);
			}

			return true;
		}
		return false;
	}

}

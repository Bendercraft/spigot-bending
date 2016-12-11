package net.bendercraft.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

@ABendingAbility(name = WaterManipulation.NAME, element = BendingElement.WATER)
public class WaterManipulation extends BendingActiveAbility {
	public final static String NAME = "WaterManipulation";

	private static int ID = Integer.MIN_VALUE;

	@ConfigurationParameter("Range")
	private static double RANGE = 25;

	@ConfigurationParameter("Push")
	private static double PUSHFACTOR = 0.3;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 7;

	@ConfigurationParameter("Speed")
	private static double SPEED = 35;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 500;
	
	@ConfigurationParameter("Range-Deflect")
	private static double RANGE_DEFLECT = 3;
	
	@ConfigurationParameter("Radius")
	public static double AFFECTING_RADIUS = 2;
	
	private int id;
	private int damage;
	private double range;

	protected Block source = null; // Not nullable
	
	// If in progress, this track current blast location (not nullable in progress)
	protected Location location = null; // Not nullable if in PREPARED or after
	// If in progress, this track targets blast must reach
	protected Location firstDestination = null; // first target is to make blast up to bender's eye level
	protected Location targetDestination = null; // final target where bender's eye lie upon
	protected Vector firstDirection = null;
	protected Vector targetDirection = null;
	
	// If in progress, tracking current state of this blast
	protected boolean settingup = false; // true if blast is in progress and has not yet reached "firstDestination"
	private TempBlock current, trail; // Location is accurate, but this one is which block this blast truly use
	private boolean freeze = false;
	
	protected long time;
	protected long interval;
	protected double radius;
	protected double push;
	
	private WaterReturn waterReturn;
	private TempBlock drainedBlock = null; // Can be null if no drain or no bottle used

	private long cooldown;

	public WaterManipulation(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.id = ID;
		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;

		this.damage = DAMAGE;
		if(bender.hasPerk(BendingPerk.WATER_WATERMANIPULATION_DAMAGE)) {
			this.damage += 1;
		}
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.WATER_WATERMANIPULATION_RANGE_1)) {
			this.range += 2;
		}
		if(bender.hasPerk(BendingPerk.WATER_WATERMANIPULATION_RANGE_2)) {
			this.range += 2;
		}
		this.push = PUSHFACTOR;
		if(bender.hasPerk(BendingPerk.WATER_WATERMANIPULATION_PUSHBACK)) {
			this.push *= 1.5;
		}
		this.radius = AFFECTING_RADIUS;
		if(bender.hasPerk(BendingPerk.WATER_WATERMANIPULATION_HITBOX)) {
			this.radius *= 1.1;
		}
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.WATER_WATERMANIPULATION_COOLDOWN)) {
			this.cooldown -= 1000;
		}
		
		double speed = SPEED;
		if(bender.hasPerk(BendingPerk.WATER_WATERMANIPULATION_SPEED)) {
			speed *= 1.1;
		}
		
		this.interval = (long) (1000. / speed);
		this.time = System.currentTimeMillis();
	}

	@Override
	public boolean sneak() {
		// Select a source or create one (if bottle or drain)
		// If this blast is already in "PREPARED", it means bender wants to change source location
		if(!isState(BendingAbilityState.START) && !isState(BendingAbilityState.PREPARED)) {
			return true;
		}
		
		if(isState(BendingAbilityState.PREPARED)) {
			// Clean up a bit if previous source was a drain
			if(drainedBlock != null) {
				drainedBlock.revertBlock();
				drainedBlock = null;
			}
		}
		
		block(this.player); // TODO rework that function
		
		Block block = BlockTools.getWaterSourceBlock(player, range, EntityTools.canPlantbend(player));
		// If no block available, check if bender can drainbend !
		if (block == null && Drainbending.canDrainBend(player) && !bender.isOnCooldown(Drainbending.NAME)) {
			Location drainLocation = player.getEyeLocation();
			Vector vector = drainLocation.getDirection().clone().normalize();
			block = drainLocation.clone().add(vector.clone().multiply(2)).getBlock();
			if (Drainbending.canBeSource(block)) {
				drainedBlock = TempBlock.makeTemporary(this, block, Material.STATIONARY_WATER, false);
				bender.cooldown(Drainbending.NAME, Drainbending.COOLDOWN);
			} else {
				block = null;
			}
		}

		// Check for bottle too !
		if (block == null && WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			block = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
			if (BlockTools.isTransparentToEarthbending(player, block) 
					&& BlockTools.isTransparentToEarthbending(player, eyeloc.getBlock())
					&& WaterReturn.canBeSource(block)) {
				drainedBlock = TempBlock.makeTemporary(this, block, Material.STATIONARY_WATER, false);
				WaterReturn.emptyWaterBottle(player);
			} else {
				block = null;
			}
		}
		
		if (block != null) {
			source = block;
			location = block.getLocation();
			setState(BendingAbilityState.PREPARED);
			return false;
		}
		
		//No source ? Remove current instance !
		remove();
		return false;
	}

	@Override
	public boolean swing() {
		// In case swing is not needed (based on current state of this ability i.e. either to throw or redirect ongoing, 
		// then use it to redirect others blast
		if (isState(BendingAbilityState.START) || (isState(BendingAbilityState.PROGRESSING) && !player.isSneaking())) {
			redirectTargettedBlasts(player);
			return false;
		}

		// If PREPARED, then bender wants to throw his blast
		if (isState(BendingAbilityState.PREPARED)) {
			if (source == null || source.getWorld() != player.getWorld()) {
				return false;
			}
			targetDestination = getTargetLocation(player);
			
			firstDestination = source.getLocation().clone();
			if (targetDestination.getBlockY() - source.getY() <= 2) {
				firstDestination.setY(source.getY() + 2);
			} else {
				firstDestination.setY(targetDestination.getBlockY() - 1);
			}
			
			if (targetDestination.distance(location) <= 1) {
				targetDestination = null;
				return false;
			}
			firstDirection = Tools.getDirection(source.getLocation(), firstDestination).normalize();
			targetDirection = Tools.getDirection(firstDestination, targetDestination).normalize();
			
			settingup = true;
			
			if(TempBlock.isTempBlock(source)) {
				TempBlock.get(source).revertBlock();
			} else {
				source.setType(Material.AIR);
			}
			setState(BendingAbilityState.PROGRESSING);
			bender.cooldown(NAME, cooldown);
		} else if(isState(BendingAbilityState.PROGRESSING) && player.isSneaking()) {
			freeze = !freeze;
		}

		return false;
	}

	@Override
	public void progress() {
		// Water return is a gracious way of ending this blast if bender has an empty bottle
		// if it is set, then it means everything else is ready to be removed, so just compute this
		if(waterReturn != null) {
			if(!waterReturn.progress()) {
				remove();
			}
			return;
		}
		
		
		// Blast have a speed, make sure we do not compute each tick (one tick = one block further)
		long now = System.currentTimeMillis();
		if (now < (time + interval)) {
			// Not enough time has passed to progress, just waiting
			return;
		}
	
		time = System.currentTimeMillis();
		
		// Are we in PREPARED state ? In that case just pop visual effect to let benders know which source has been chosen
		if(isState(BendingAbilityState.PREPARED)) {
			// Slot change are allowed in PROGRESSING but not in PREPARED
			if(!NAME.equals(EntityTools.getBendingAbility(player))) {
				remove();
				return;
			}
			source.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range); //TODO use spawnParticle
			return;
		}

		// If this part of code is reached it means we are in PROGRESSING
		
		// Check if we are not too far away
		if (location.distance(targetDestination) <= 1 || location.distance(firstDestination) > range) {
			hit();
			return;
		}
		
		// Move location by one
		if(settingup) {
			if (location.distance(firstDestination) < 0.6) {
				settingup = false; // We got there !
			} else {
				location = location.add(firstDirection);
			}
		} else {
			location = location.add(targetDirection);
		}
		
		// Check if new block is OK to go through
		Block block = location.getBlock();
		if (BlockTools.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
			BlockTools.breakBlock(block); // DESTROY FLOWERSSSS
		} else if (block.getType() != Material.AIR && !BlockTools.isWater(block)) {
			hit();
			return;
		}
		
		// No matter if settingup or not, check collision with other abilities
		PluginTools.removeSpouts(location, player);
		if (EarthBlast.removeOneAroundPoint(location, player, radius)
				|| WaterManipulation.removeOneAroundPoint(location, player, radius) 
				|| FireBlast.removeOneAroundPoint(location, player, radius)) {
			hit();
			return;
		}
		
		// Check for any damage to entities
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, radius)) {
			if(affect(entity)) {
				hit();
				return;
			}
		}
		
		// Go for visual effect
		if(current == null || current.getBlock() != location.getBlock()) {
			if(current != null) {
				current.revertBlock();
			}
			if(!settingup) {
				if(trail != null) {
					trail.revertBlock();
				}
				if(current != null && !freeze) {
					trail = TempBlock.makeTemporary(this, current.getBlock(), Material.WATER, (byte) 2, false);
				}
			}
			if(freeze) {
				current = TempBlock.makeTemporary(this, location.getBlock(), Material.ICE, false);
			} else {
				current = TempBlock.makeTemporary(this, location.getBlock(), Material.WATER, false);
			}
		}
	}
	
	private boolean affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		if (entity == player) {
			return false;
		}
		Vector vector = player.getEyeLocation().getDirection();
		entity.setVelocity(vector.normalize().multiply(push));
		DamageTools.damageEntity(bender, entity, this, damage);
		return true;
	}
		
	private void hit() {
		if(freeze) {
			bender.water.ice();
		} else {
			bender.water.liquid();
		}
		waterReturn = new WaterReturn(player, location.getBlock(), this);
	}
	
	public Location getTargetLocation(Player player) {
		Entity target = EntityTools.getTargetedEntity(player, range);
		Location result = null;
		if (target == null || ProtectionManager.isEntityProtected(target)) {
			result = EntityTools.getTargetedLocation(player, range, BlockTools.getTransparentEarthBending());
		} else {
			result = ((LivingEntity) target).getLocation();
		}
		return result;
	}
	
	public void redirect(Location targetlocation) {
		if(!freeze) {
			if(isState(BendingAbilityState.PROGRESSING) && !settingup) {
				if (location.distance(player.getLocation()) <= range) {
					targetDirection = Tools.getDirection(location, targetlocation).normalize();
				}
				targetDestination = targetlocation;
			}
		}
	}
	
	@Override
	public void stop() {
		if (drainedBlock != null) {
			drainedBlock.revertBlock();
			drainedBlock = null;
		}
		if (waterReturn != null) {
			waterReturn.stop();
		}
		if(current != null) {
			current.revertBlock();
		}
		if(trail != null) {
			trail.revertBlock();
		}
	}
	

	@Override
	public Object getIdentifier() {
		return id;
	}

	private static void redirectTargettedBlasts(Player player) {
		RegisteredAbility registered = AbilityManager.getManager().getRegisteredAbility(NAME);
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (!manip.isState(BendingAbilityState.PROGRESSING) 
					|| manip.location == null
					|| !manip.location.getWorld().equals(player.getWorld()) 
					|| ProtectionManager.isLocationProtectedFromBending(player, registered, manip.location)) {
				continue;
			}

			if (manip.player.equals(player)) {
				manip.redirect(manip.getTargetLocation(player));
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distance(location) <= manip.range 
					&& Tools.getDistanceFromLine(vector, location, mloc) < RANGE_DEFLECT 
					&& mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				manip.redirect(manip.getTargetLocation(player));
			}

		}
	}

	private static void block(Player player) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		RegisteredAbility registered = AbilityManager.getManager().getRegisteredAbility(NAME);
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.player.equals(player) 
					|| manip.location == null
					|| !manip.location.getWorld().equals(player.getWorld()) 
					|| !manip.isState(BendingAbilityState.PROGRESSING)  
					|| ProtectionManager.isLocationProtectedFromBending(player, registered, manip.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distance(location) <= manip.range 
					&& Tools.getDistanceFromLine(vector, location, mloc) < RANGE_DEFLECT 
					&& mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				toBreak.add(manip);
			}
		}
		for (WaterManipulation manip : toBreak) {
			manip.remove();
		}
	}

	public static boolean removeOneAroundPoint(Location location, Player player, double radius) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.player != player 
					&& manip.location != null
					&& manip.location.getWorld().equals(location.getWorld()) 
					&& manip.location.distance(location) <= radius
					&& !manip.freeze) {
				manip.remove();
				return true;
			}
		}
		return false;
	}
	
	public static void removeAroundPoint(Location location, double radius) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.location != null 
					&& manip.location.getWorld().equals(location.getWorld()) 
					&& manip.location.distance(location) <= radius
					&& !manip.freeze) {
				manip.remove();
			}
		}
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.location != null && 
					manip.location.getWorld().equals(location.getWorld()) 
					&& !source.equals(manip.player) 
					&& manip.location.distance(location) <= radius) {
				toBreak.add(manip);
				broke = true;
			}
		}
		for (WaterManipulation manip : toBreak) {
			manip.remove();
		}
		return broke;
	}

	public static boolean isWaterManipulater(Player player) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.player.equals(player)) {
				return true;
			}
		}
		return false;
	}
}

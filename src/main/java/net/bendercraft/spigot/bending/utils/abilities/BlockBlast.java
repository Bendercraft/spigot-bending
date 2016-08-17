package net.bendercraft.spigot.bending.utils.abilities;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

public abstract class BlockBlast {
	protected BendingAbility parent;
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
	
	
	protected long time;
	protected double damage;
	protected long interval;
	protected double range;
	protected double radius;
	protected double push;
	
	public BlockBlast(BendingAbility parent, double damage, double range, double speed, double radius, double push) {
		this.parent = parent;
		this.damage = damage;
		this.interval = (long) (1000. / speed);
		this.range = range;
		this.radius = radius;
		this.push = push;
	}
	
	public void select(Block block) {
		source = block;
		location = source.getLocation();
	}
	
	public boolean shot() {
		if (source == null || source.getWorld() != parent.getPlayer().getWorld()) {
			return false;
		}
		
		firstDestination = source.getLocation().clone();
		firstDestination.setY(parent.getPlayer().getEyeLocation().getY()+1);
		targetDestination = getTargetLocation();
		// Not enough distance, just cancel this blast
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
		
		return true;
	}
	
	public boolean progress() {
		// Blast have a speed, make sure we do not compute each tick (one tick = one block further)
		long now = System.currentTimeMillis();
		if (now < (time + interval)) {
			// Not enough time has passed to progress, just waiting
			return true;
		}
	
		time = System.currentTimeMillis();
		
		// Are we in PREPARED state ? In that case just pop visual effect to let benders know which source has been taken
		if(parent.isState(BendingAbilityState.PREPARED)) {
			// Slot change are allowed in PROGRESSING but not in PREPARED
			if(!parent.getName().equals(EntityTools.getBendingAbility(parent.getPlayer()))) {
				return false;
			}
			source.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range); //TODO use spawnParticle
			return true;
		}

		// If this part of code is reached it means we are in PROGRESSING
		
		// Check if we are not too far away
		if (location.distance(targetDestination) <= 1 || location.distance(firstDestination) > range) {
			return false;
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
		if (BlockTools.isTransparentToEarthbending(parent.getPlayer(), block) && !block.isLiquid()) {
			BlockTools.breakBlock(block); // DESTROY FLOWERSSSS
		} else if (!allow(block)) {
			return false;
		}
		
		// No matter if settingup or not, apply damage + hindrance to other bending
		PluginTools.removeSpouts(location, parent.getPlayer());
		
		if (EarthBlast.removeOneAroundPoint(location, parent.getPlayer(), radius)
				|| WaterManipulation.removeOneAroundPoint(location, parent.getPlayer(), radius) 
				|| FireBlast.removeOneAroundPoint(location, parent.getPlayer(), radius)) {
			return false;
		}
		
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, radius)) {
			if (ProtectionManager.isEntityProtected(entity)) {
				continue;
			}
			if (entity != parent.getPlayer()) {
				Vector vector = parent.getPlayer().getEyeLocation().getDirection();
				entity.setVelocity(vector.normalize().multiply(push));

				DamageTools.damageEntity(parent.getBender(), entity, parent, damage);
				return false;
			}
		}
		
		// Go for visual effect
		effect();
		
		return true;
	}
	
	public void redirect(Location targetlocation) {
		if(parent.isState(BendingAbilityState.PROGRESSING) && !settingup) {
			if (location.distance(parent.getPlayer().getLocation()) <= range) {
				targetDirection = Tools.getDirection(location, targetlocation).normalize();
			}
			targetDestination = targetlocation;
		}
	}
	
	public Location getLocation() {
		if(location != null) {
			return location.clone();
		}
		return null;
	}
	
	/**
	 * Returns final destination for a blast, shpuld never return null value
	 * @param player
	 * @return
	 */
	public abstract Location getTargetLocation();
	/**
	 * Function used to display visual effect for blast
	 */
	public abstract void effect();
	
	/**
	 * Function that tell if a block is valid for advance.
	 * @param block
	 * @return
	 */
	public abstract boolean allow(Block block);
	
	public abstract void remove();
	
}

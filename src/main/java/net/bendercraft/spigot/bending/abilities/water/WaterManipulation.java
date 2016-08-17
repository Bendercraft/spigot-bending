package net.bendercraft.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.water.WaterBalance.Damage;
import net.bendercraft.spigot.bending.abilities.water.WaterBalance.State;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;
import net.bendercraft.spigot.bending.utils.abilities.BlockBlast;

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

	private BlockBlast blast;
	private WaterReturn waterReturn;
	
	private TempBlock drainedBlock = null; // Can be null if no drain or no bottle used

	public WaterManipulation(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.id = ID;
		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;

		this.damage = DAMAGE;
		this.range = RANGE;

		if (this.bender.hasPath(BendingPath.MARKSMAN)) {
			this.damage *= 0.8;
			this.range *= 1.4;
		}

		if (this.bender.hasPath(BendingPath.FLOWLESS)) {
			this.damage *= 1.15;
		}
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
		
		Block source = BlockTools.getWaterSourceBlock(player, range, EntityTools.canPlantbend(player));
		// If no block available, check if bender can drainbend !
		if (source == null && Drainbending.canDrainBend(player) && !bender.isOnCooldown(Drainbending.NAME)) {
			Location drainLocation = player.getEyeLocation();
			Vector vector = drainLocation.getDirection().clone().normalize();
			source = drainLocation.clone().add(vector.clone().multiply(2)).getBlock();
			if (Drainbending.canBeSource(source)) {
				drainedBlock = TempBlock.makeTemporary(source, Material.STATIONARY_WATER, false);
				bender.cooldown(Drainbending.NAME, Drainbending.COOLDOWN);
			} else {
				source = null;
			}
		}

		// Check for bottle too !
		if (source == null && WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			source = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
			if (BlockTools.isTransparentToEarthbending(player, source) 
					&& BlockTools.isTransparentToEarthbending(player, eyeloc.getBlock())
					&& WaterReturn.canBeSource(source)) {
				drainedBlock = TempBlock.makeTemporary(source, Material.STATIONARY_WATER, false);
				WaterReturn.emptyWaterBottle(player);
			} else {
				source = null;
			}
		}
		
		if (source != null) {
			if(source.getType() == Material.ICE) {
				blast = new IceBlockBlast(this, bender.water.damage(Damage.ICE, damage), range, SPEED, AFFECTING_RADIUS, PUSHFACTOR);
			} else {
				blast = new WaterBlockBlast(this, bender.water.damage(Damage.LIQUID, damage), range, SPEED, AFFECTING_RADIUS, PUSHFACTOR);
			}
			blast.select(source);
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
		if (isState(BendingAbilityState.START) || isState(BendingAbilityState.PROGRESSING)) {
			redirectTargettedBlasts(player);
			return false;
		}

		// If PREPARED, then bender wants to throw his blast
		if (isState(BendingAbilityState.PREPARED)) {
			if(blast.shot()) {
				setState(BendingAbilityState.PROGRESSING);
				
				bender.cooldown(NAME, COOLDOWN);
				if(blast instanceof IceBlockBlast) {
					bender.water.ice();
				} else {
					bender.water.liquid();
				}
			}
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
		
		if(!blast.progress()) {
			if(isState(BendingAbilityState.PROGRESSING) && blast.getLocation() != null) {
				if(bender.water.isState(State.STABLE) && blast instanceof WaterBlockBlast) {
					Frozen.freeze(player, blast.getLocation(), 3);
					remove();
				} else {
					waterReturn = new WaterReturn(player, blast.getLocation().getBlock(), this);
				}
			} else {
				remove();
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
		if(blast != null) {
			blast.remove();
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
					|| manip.blast == null
					|| !manip.blast.getLocation().getWorld().equals(player.getWorld()) 
					|| ProtectionManager.isLocationProtectedFromBending(player, registered, manip.blast.getLocation())) {
				continue;
			}

			if (manip.player.equals(player)) {
				manip.blast.redirect(manip.blast.getTargetLocation());
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.blast.getLocation();
			if (mloc.distance(location) <= manip.range 
					&& Tools.getDistanceFromLine(vector, location, mloc) < RANGE_DEFLECT 
					&& mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				manip.blast.redirect(manip.blast.getTargetLocation());
			}

		}
	}

	private static void block(Player player) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		RegisteredAbility registered = AbilityManager.getManager().getRegisteredAbility(NAME);
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.player.equals(player) 
					|| manip.blast == null
					|| !manip.blast.getLocation().getWorld().equals(player.getWorld()) 
					|| !manip.isState(BendingAbilityState.PROGRESSING)  
					|| ProtectionManager.isLocationProtectedFromBending(player, registered, manip.blast.getLocation())) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.blast.getLocation();
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
					&& manip.blast != null
					&& manip.blast.getLocation().getWorld().equals(location.getWorld()) 
					&& manip.blast.getLocation().distance(location) <= radius
					&& manip.blast instanceof WaterBlockBlast) {
				manip.remove();
				return true;
			}
		}
		return false;
	}
	
	public static void removeAroundPoint(Location location, double radius) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.blast != null 
					&& manip.blast.getLocation().getWorld().equals(location.getWorld()) 
					&& manip.blast.getLocation().distance(location) <= radius
					&& manip.blast instanceof WaterBlockBlast) {
				manip.remove();
			}
		}
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.blast != null && 
					manip.blast.getLocation().getWorld().equals(location.getWorld()) 
					&& !source.equals(manip.player) 
					&& manip.blast.getLocation().distance(location) <= radius) {
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
	
	
	private static class WaterBlockBlast extends BlockBlast {
		
		private TempBlock current, trail; // Location is accurate, but this one is which block this blast truly use

		public WaterBlockBlast(BendingAbility parent, double damage, double range, double speed, double radius, double push) {
			super(parent, damage, range, speed, radius, push);
		}

		@Override
		public Location getTargetLocation() {
			Entity target = null;
			if (!parent.getBender().hasPath(BendingPath.FLOWLESS)) {
				target = EntityTools.getTargetedEntity(parent.getPlayer(), range);
			}
			Location result = null;
			if (target == null || ProtectionManager.isEntityProtected(target)) {
				result = EntityTools.getTargetedLocation(parent.getPlayer(), range, BlockTools.getTransparentEarthBending());
			} else {
				result = ((LivingEntity) target).getLocation();
			}
			return result;
		}

		@Override
		public void effect() {
			if(current == null || current.getBlock() != location.getBlock()) {
				if(current != null) {
					current.revertBlock();
				}
				// Change trail if not settingup !
				if(!settingup) {
					if(trail != null) {
						trail.revertBlock();
					}
					if(current != null) {
						trail = TempBlock.makeTemporary(current.getBlock(), Material.WATER, (byte) 2, false);
					}
				}
				current = TempBlock.makeTemporary(location.getBlock(), Material.WATER, false);
			}
		}

		@Override
		public boolean allow(Block block) {
			return block.getType() == Material.AIR || BlockTools.isWater(block);
		}

		@Override
		public void remove() {
			if(current != null) {
				current.revertBlock();
			}
			if(trail != null) {
				trail.revertBlock();
			}
		}
		
	}
	
	public static class IceBlockBlast extends BlockBlast {
		
		private TempBlock current;

		public IceBlockBlast(BendingAbility parent, double damage, double range, double speed, double radius, double push) {
			super(parent, damage, range, speed, radius, push);
		}
		
		@Override
		public void redirect(Location targetlocation) {
			
		}

		@Override
		public Location getTargetLocation() {
			Entity target = EntityTools.getTargetedEntity(parent.getPlayer(), range);
			Location result = null;
			if (target == null || ProtectionManager.isEntityProtected(target)) {
				result = EntityTools.getTargetedLocation(parent.getPlayer(), range, BlockTools.getTransparentEarthBending());
			} else {
				result = ((LivingEntity) target).getEyeLocation();
			}
			return result;
		}

		@Override
		public void effect() {
			if(current == null || current.getBlock() != location.getBlock()) {
				if(current != null) {
					current.revertBlock();
				}
				current = TempBlock.makeTemporary(location.getBlock(), Material.ICE, false);
			}
		}

		@Override
		public boolean allow(Block block) {
			return block.getType() == Material.AIR || BlockTools.isWaterBased(block);
		}

		@Override
		public void remove() {
			if(current != null) {
				current.revertBlock();
			}
		}
		
	}
}

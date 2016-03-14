package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

@ABendingAbility(name = WaterManipulation.NAME, element = BendingElement.WATER)
public class WaterManipulation extends BendingActiveAbility {
	public final static String NAME = "WaterManipulation";
	
	private static Map<Block, Block> affectedblocks = new HashMap<Block, Block>();

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

	private static final double deflectrange = 3;

	private static Set<Byte> water = new HashSet<Byte>();

	private long interval;

	private int id;
	private Location location = null;
	private Block sourceblock = null;
	private TempBlock trail, trail2, drainedBlock;
	private boolean progressing = false;
	private Location firstdestination = null;
	private Location targetdestination = null;
	private Vector firstdirection = null;
	private Vector targetdirection = null;
	private boolean falling = false;
	private boolean settingup = false;
	private long time;
	private int damage;
	private double range;

	private WaterReturn waterReturn;

	public WaterManipulation(RegisteredAbility register, Player player) {
		super(register, player);
		if (water.isEmpty()) {
			water.add((byte) 0);
			water.add((byte) 8);
			water.add((byte) 9);
		}

		damage = DAMAGE;
		range = RANGE;

		if (bender.hasPath(BendingPath.MARKSMAN)) {
			damage *= 0.8;
			range *= 1.4;
		}

		if (bender.hasPath(BendingPath.FLOWLESS)) {
			damage *= 1.15;
		}

		this.id = ID;
		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;
		this.time = System.currentTimeMillis();
		
		interval = (long) (1000. / SPEED);
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.START && getState() != BendingAbilityState.PREPARED) {
			return true;
		}
		
		if(this.drainedBlock != null) {
			this.drainedBlock.revertBlock();
			this.drainedBlock = null;
		}
		
		setState(BendingAbilityState.PREPARING);
		Block block = BlockTools.getWaterSourceBlock(this.player, range, EntityTools.canPlantbend(this.player));

		block(this.player);
		

		// If no block available, check if bender can drainbend !
		if (block == null && Drainbending.canDrainBend(this.player) && !bender.isOnCooldown(Drainbending.NAME)) {
			Location drainLocation = this.player.getEyeLocation();
			Vector vector = drainLocation.getDirection().clone().normalize();
			block = drainLocation.clone().add(vector.clone().multiply(2)).getBlock();
			if (Drainbending.canBeSource(block)) {
				//this.drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, BlockTools.FULL);
				this.drainedBlock = TempBlock.makeTemporary(block, Material.STATIONARY_WATER);
			} else {
				block = null;
			}
			bender.cooldown(Drainbending.NAME, Drainbending.COOLDOWN/2);
		}

		// Check for bottle too !
		if (block == null && !bender.isOnCooldown(NAME) 
				&& getState() != BendingAbilityState.PREPARED && WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			block = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
			if (BlockTools.isTransparentToEarthbending(player, block) && BlockTools.isTransparentToEarthbending(player, eyeloc.getBlock())) {
				//this.drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, BlockTools.FULL);
				this.drainedBlock = TempBlock.makeTemporary(block, Material.STATIONARY_WATER);
				WaterReturn.emptyWaterBottle(player);
			} else {
				block = null;
			}
		}
		
		if (block != null) {
			this.sourceblock = block;
			this.location = this.sourceblock.getLocation();
			setState(BendingAbilityState.PREPARED);
			return false;
		}
		
		//No block ? Remove current instance !
		remove();
		return false;
	}
	
	@Override
	public void stop() {
		finalRemoveWater(this.sourceblock);
		if (this.drainedBlock != null) {
			this.drainedBlock.revertBlock();
			this.drainedBlock = null;
		}
		if (this.trail != null) {
			this.trail.revertBlock();
			this.trail = null;
		}
		if (this.trail2 != null) {
			this.trail2.revertBlock();
			this.trail2 = null;
		}
		if (waterReturn != null) {
			waterReturn.stop();
		}
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START || getState() == BendingAbilityState.PROGRESSING) {
			redirectTargettedBlasts(player);
			return false;
		}

		if (getState() == BendingAbilityState.PREPARED) {
			if (this.sourceblock == null) {
				return false;
			}
			if (this.sourceblock.getWorld() != this.player.getWorld()) {
				return false;
			}

			this.targetdestination = getTargetLocation(this.player);
			if (this.targetdestination.distance(this.location) <= 1) {
				this.progressing = false;
				this.targetdestination = null;
				remove();
			} else {
				this.progressing = true;
				this.settingup = true;
				this.firstdestination = getToEyeLevel();
				this.firstdirection = Tools.getDirection(this.sourceblock.getLocation(), this.firstdestination).normalize();
				this.targetdestination = Tools.getPointOnLine(this.firstdestination, this.targetdestination, range);
				this.targetdirection = Tools.getDirection(this.firstdestination, this.targetdestination).normalize();
				addWater(this.sourceblock);
				setState(BendingAbilityState.PROGRESSING);
			}
			BendingPlayer.getBendingPlayer(this.player).cooldown(NAME, COOLDOWN);
		}

		return false;
	}

	private Location getTargetLocation(Player player) {
		Entity target = null;
		if (!bender.hasPath(BendingPath.FLOWLESS)) {
			target = EntityTools.getTargetedEntity(player, range);
		}
		Location result = null;
		if ((target == null) || ProtectionManager.isEntityProtected(target)) {
			result = EntityTools.getTargetedLocation(player, range, BlockTools.getTransparentEarthBending());
		} else {
			result = ((LivingEntity) target).getEyeLocation();
		}
		return result;
	}

	private Location getToEyeLevel() {
		Location loc = this.sourceblock.getLocation().clone();
		double dy = this.targetdestination.getY() - this.sourceblock.getY();
		if (dy <= 2) {
			loc.setY(this.sourceblock.getY() + 2L);
		} else {
			loc.setY(this.targetdestination.getY() - 1L);
		}
		return loc;
	}

	private void redirect(Player player, Location targetlocation) {
		if (this.progressing && !this.settingup) {
			if (this.location.distance(player.getLocation()) <= range) {
				this.targetdirection = Tools.getDirection(this.location, targetlocation).normalize();
			}
			this.targetdestination = targetlocation;
		}
	}

	@Override
	public void progress() {
		if (waterReturn != null) {
			if(!waterReturn.progress()) {
				remove();
			}
			return;
		}
		if ((System.currentTimeMillis() - this.time) >= interval) {
			this.time = System.currentTimeMillis();

			if (!this.progressing && !this.falling && (!NAME.equals(EntityTools.getBendingAbility(player)))) {
				remove();
				return;
			}

			if (this.falling) {
				finalRemoveWater(this.sourceblock);
				waterReturn = new WaterReturn(this.player, this.sourceblock, this);
				return;

			} else {
				if (!this.progressing) {
					this.sourceblock.getWorld().playEffect(this.location, Effect.SMOKE, 4, (int) range);
					return;
				}

				if (this.sourceblock.getLocation().distance(this.firstdestination) < .5) {
					this.settingup = false;
				}

				Vector direction;
				if (this.settingup) {
					direction = this.firstdirection;
				} else {
					direction = this.targetdirection;
				}

				Block block = this.location.getBlock();
				PluginTools.removeSpouts(this.location, this.player);

				double radius = FireBlast.AFFECTING_RADIUS;
				Player source = this.player;
				if (EarthBlast.annihilateBlasts(this.location, radius, source) || WaterManipulation.shouldAnnihilateBlasts(this.location, radius, source, false) || FireBlast.annihilateBlasts(this.location, radius, source)) {
					waterReturn = new WaterReturn(this.player, this.sourceblock, this);
					return;
				}

				this.location = this.location.clone().add(direction);

				block = this.location.getBlock();
				if (block.getLocation().equals(this.sourceblock.getLocation())) {
					this.location = this.location.clone().add(direction);
					block = this.location.getBlock();
				}
				

				if (this.trail2 != null && this.trail2.getBlock().equals(block)) {
					this.trail2.revertBlock();
					this.trail2 = null;
				}

				if (this.trail != null && this.trail.getBlock().equals(block)) {
					this.trail.revertBlock();
					this.trail = null;
					if (this.trail2 != null) {
						this.trail2.revertBlock();
						this.trail2 = null;
					}
				}

				if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else if ((block.getType() != Material.AIR) && !BlockTools.isWater(block)) {
					waterReturn = new WaterReturn(this.player, this.sourceblock, this);
					return;
				}

				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, FireBlast.AFFECTING_RADIUS)) {
					if (ProtectionManager.isEntityProtected(entity)) {
						continue;
					}
					if (entity.getEntityId() != this.player.getEntityId()) {
						Vector vector = this.player.getEyeLocation().getDirection();
						entity.setVelocity(vector.normalize().multiply(PUSHFACTOR));
						if (AvatarState.isAvatarState(this.player)) {
							this.damage = AvatarState.getValue(this.damage);
						}

						EntityTools.damageEntity(bender, entity, PluginTools.waterbendingNightAugment(this.damage, this.player.getWorld()));
						this.progressing = false;
					}
				}

				if (!this.progressing) {
					waterReturn = new WaterReturn(this.player, this.sourceblock, this);
					return;
				}

				addWater(block);
				reduceWater(this.sourceblock);

				if (this.trail2 != null) {
					this.trail2.revertBlock();
					this.trail2 = null;
				}
				if (this.trail != null) {
					this.trail2 = this.trail;
					this.trail2.setType(Material.WATER, (byte) 2);
				}
				//this.trail = new TempBlock(this.sourceblock, Material.WATER, (byte) 1);
				this.trail = TempBlock.makeTemporary(sourceblock, Material.WATER, (byte) 1);
				this.sourceblock = block;

				if ((this.location.distance(this.targetdestination) <= 1) || (this.location.distance(this.firstdestination) > range)) {
					this.falling = true;
					this.progressing = false;
				}
			}
		}
	}

	private void reduceWater(Block block) {
		if (affectedblocks.containsKey(block)) {
			if (!BlockTools.adjacentToThreeOrMoreSources(block)) {
				block.setType(Material.AIR);
			}
			affectedblocks.remove(block);
		}
	}

	private void finalRemoveWater(Block block) {
		if (this.trail != null) {
			this.trail.revertBlock();
			this.trail = null;
		}
		if (this.trail2 != null) {
			this.trail2.revertBlock();
			this.trail = null;
		}
		if (affectedblocks.containsKey(block)) {
			if (!BlockTools.adjacentToThreeOrMoreSources(block)) {
				block.setType(Material.AIR);
			}
			affectedblocks.remove(block);
		}
	}

	@SuppressWarnings("deprecation")
	private static void addWater(Block block) {
		if (!affectedblocks.containsKey(block)) {
			affectedblocks.put(block, block);
		}
		if (PhaseChange.isFrozen(block)) {
			PhaseChange.thawThenRemove(block);
		}

		block.setType(Material.WATER);
		block.setData(BlockTools.FULL);
	}

	private static void redirectTargettedBlasts(Player player) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (!manip.progressing 
					|| !manip.location.getWorld().equals(player.getWorld()) 
					|| ProtectionManager.isLocationProtectedFromBending(player, NAME, manip.location)) {
				continue;
			}

			if (manip.player.equals(player)) {
				manip.redirect(player, manip.getTargetLocation(player));
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if ((mloc.distance(location) <= manip.range) && (Tools.getDistanceFromLine(vector, location, manip.location) < deflectrange) && (mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				manip.redirect(player, manip.getTargetLocation(player));
			}

		}
	}

	private static void block(Player player) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.player.equals(player) 
					|| !manip.location.getWorld().equals(player.getWorld()) 
					|| !manip.progressing 
					|| ProtectionManager.isLocationProtectedFromBending(player, NAME, manip.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if ((mloc.distance(location) <= manip.range) && (Tools.getDistanceFromLine(vector, location, manip.location) < deflectrange) && (mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				toBreak.add(manip);
			}

		}

		for (WaterManipulation manip : toBreak) {
			manip.remove();
		}
	}

	public static boolean canFlowFromTo(Block from, Block to) {
		if (affectedblocks.containsKey(to) 
				|| affectedblocks.containsKey(from) 
				|| WaterSpout.isWaterSpoutBlock(to) 
				|| WaterSpout.isWaterSpoutBlock(from) 
				|| WaterWall.isAffectedByWaterWall(to) 
				|| WaterWall.isAffectedByWaterWall(from) 
				|| WaterWall.isWaterWallPart(to) 
				|| WaterWall.isWaterWallPart(from) 
				|| Wave.isBlockWave(to) 
				|| Wave.isBlockWave(from) 
				|| TempBlock.isTempBlock(to) 
				|| TempBlock.isTempBlock(from) 
				|| BlockTools.adjacentToFrozenBlock(to) 
				|| BlockTools.adjacentToFrozenBlock(from)) {
			return false;
		}
		return true;
	}

	public static boolean canPhysicsChange(Block block) {
		if (affectedblocks.containsKey(block) 
				|| WaterSpout.isWaterSpoutBlock(block) 
				|| WaterWall.isAffectedByWaterWall(block) 
				|| WaterWall.isWaterWallPart(block) 
				|| Wave.isBlockWave(block) 
				|| TempBlock.isTempBlock(block) 
				|| TempBlock.isTouchingTempBlock(block)) {
			return false;
		}
		return true;
	}

	public static boolean canBubbleWater(Block block) {
		if (affectedblocks.containsKey(block) 
				|| WaterSpout.isWaterSpoutBlock(block) 
				|| WaterWall.isAffectedByWaterWall(block) 
				|| WaterWall.isWaterWallPart(block) 
				|| Wave.isBlockWave(block) 
				|| TempBlock.isTempBlock(block)) {
			return false;
		}
		return true;
	}

	public static void removeAroundPoint(Location location, double radius) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.location.getWorld().equals(location.getWorld()) 
					&& manip.location.distance(location) <= radius) {
				toBreak.add(manip);
			}
		}
		for (WaterManipulation manip : toBreak) {
			manip.remove();
		}
	}

	private static boolean shouldAnnihilateBlasts(Location location, double radius, Player source, boolean remove) {
		boolean broke = false;
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterManipulation manip = (WaterManipulation) ab;
			if (manip.location.getWorld().equals(location.getWorld()) 
					&& !source.equals(manip.player) 
					&& manip.location.distance(location) <= radius) {
				toBreak.add(manip);
				broke = true;
			}
		}
		if (remove) {
			for (WaterManipulation manip : toBreak) {
				manip.remove();
			}
		}
		return broke;
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		return shouldAnnihilateBlasts(location, radius, source, true);
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

	@Override
	public Object getIdentifier() {
		return id;
	}
}

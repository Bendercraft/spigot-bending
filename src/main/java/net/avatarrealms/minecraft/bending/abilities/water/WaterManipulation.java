package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.Information;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
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

public class WaterManipulation implements IAbility {
	private static Map<Integer, WaterManipulation> instances = new HashMap<Integer, WaterManipulation>();
	private static Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private static Map<Player, Integer> prepared = new HashMap<Player, Integer>();

	private static int ID = Integer.MIN_VALUE;

	private static final byte full = 0x0;
	// private static final byte half = 0x4;

	static double range = ConfigManager.waterManipulationRange;
	private static double pushfactor = ConfigManager.WaterManipulationPush;
	private static int defaultdamage = ConfigManager.waterManipulationDamage;
	private static double speed = ConfigManager.waterManipulationSpeed;
	private static final double deflectrange = 3;
	// private static double speed = 1.5;

	private static Set<Byte> water = new HashSet<Byte>();

	private static long interval = (long) (1000. / speed);

	Player player;
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
	// private boolean targetting = false;
	private final boolean displacing = false;
	private long time;
	private int damage = defaultdamage;
	private int displrange;
	
	private IAbility parent;

	public WaterManipulation(Player player, IAbility parent) {
		this.parent = parent;
		if (water.isEmpty()) {
			water.add((byte) 0);
			water.add((byte) 8);
			water.add((byte) 9);
		}
		this.player = player;
		if (prepare()) {
			id = ID;
			instances.put(id, this);
			prepared.put(player, id);
			if (ID == Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			ID++;
			time = System.currentTimeMillis();
		}
	}

	private boolean prepare() {
		Block block = BlockTools.getWaterSourceBlock(player, range,
				EntityTools.canPlantbend(player));
		cancelPrevious();
		block(player);
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if(bPlayer == null) {
			return false;
		}
		
		//If no block available, check if bender can drainbend !
		if(Drainbending.canDrainBend(player) && !bPlayer.isOnCooldown(Abilities.Drainbending)) {
			Location location = player.getEyeLocation();
			Vector vector = location.getDirection().clone().normalize();
			block = location.clone().add(vector.clone().multiply(2)).getBlock();
			if(Drainbending.canBeSource(block)) {
				drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0x0);
				sourceblock = block;
				focusBlock();
				bPlayer.cooldown(Abilities.Drainbending);
				return true;
			}
		}
		
		return false;
	}

	private void cancelPrevious() {
		if (prepared.containsKey(player)) {
			if (instances.containsKey(prepared.get(player))) {
				WaterManipulation old = instances.get(prepared.get(player));
				if (!old.progressing) {
					old.remove();
				}
			}
		}
	}

	public void remove() {
		finalRemoveWater(sourceblock);
		if(drainedBlock != null) {
			drainedBlock.revertBlock();
			drainedBlock = null;
		}
		remove(id);
	}

	private void focusBlock() {
		location = sourceblock.getLocation();
	}

	public void moveWater() {
		if (sourceblock == null) {
			return;
		}
		if (sourceblock.getWorld() != player.getWorld()) {
			return;
		}
		
		//TODO : Test this :
		if (drainedBlock == null) {
			Information info = Information.fromBlock(sourceblock);
			BlockTools.bendedBlocks.put(sourceblock, info);
		}	
		targetdestination = getTargetLocation(player);
		if (targetdestination.distance(location) <= 1) {
			progressing = false;
			targetdestination = null;
			remove(id);
		} else {
			progressing = true;
			settingup = true;
			firstdestination = getToEyeLevel();
			firstdirection = Tools.getDirection(sourceblock.getLocation(), firstdestination).normalize();
			targetdestination = Tools.getPointOnLine(firstdestination,
					targetdestination, range);
			targetdirection = Tools.getDirection(firstdestination,
					targetdestination).normalize();

			if (BlockTools.isPlant(sourceblock))
				new Plantbending(sourceblock, this);
			addWater(sourceblock);
		}
		BendingPlayer.getBendingPlayer(player).cooldown(Abilities.WaterManipulation);
	}

	private static Location getTargetLocation(Player player) {
		Entity target = EntityTools.getTargettedEntity(player, range);
		Location location;
		if (target == null) {
			location = EntityTools.getTargetedLocation(player, range,
					BlockTools.transparentEarthbending);
		} else {
			// targetting = true;
			location = ((LivingEntity) target).getEyeLocation();
			// location.setY(location.getY() - 1);
		}
		return location;
	}

	private Location getToEyeLevel() {
		Location loc = sourceblock.getLocation().clone();
		double dy = targetdestination.getY() - sourceblock.getY();
		if (dy <= 2) {
			loc.setY(sourceblock.getY() + 2);
		} else {
			loc.setY(targetdestination.getY() - 1);
		}
		return loc;
	}

	private static void remove(int id) {
		Player player = instances.get(id).player;
		if (prepared.containsKey(player)) {
			if (prepared.get(player) == id)
				prepared.remove(player);
		}
		instances.remove(id);
	}

	private void redirect(Player player, Location targetlocation) {
		if (progressing && !settingup) {
			if (location.distance(player.getLocation()) <= range)
				targetdirection = Tools.getDirection(location, targetlocation)
						.normalize();
			targetdestination = targetlocation;
			this.player = player;
		}
	}

	/**
	 * If return false, breakBlock has to be called !
	 * @return
	 */
	private boolean progress() {
		if (player.isDead() || !player.isOnline()
				|| !EntityTools.canBend(player, Abilities.WaterManipulation)) {
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			// removeWater(oldwater);
			if (PluginTools.isRegionProtectedFromBuild(player,
					Abilities.WaterManipulation, location)) {
				return false;
			}

			time = System.currentTimeMillis();

			if (!progressing
					&& !falling
					&& EntityTools.getBendingAbility(player) != Abilities.WaterManipulation) {
				return false;
			}

			if (falling) {
				finalRemoveWater(sourceblock);
				new WaterReturn(player, sourceblock, this);
				return false;

			} else {
				if (!progressing) {
					sourceblock.getWorld().playEffect(location, Effect.SMOKE,
							4, (int) range);
					return true;
				}
				
				if (sourceblock.getLocation().distance(firstdestination) < .5) {
					settingup = false;
				}

				Vector direction;
				if (settingup) {
					direction = firstdirection;
				} else {
					direction = targetdirection;
				}

				Block block = location.getBlock();
				if (displacing) {
					Block targetblock = EntityTools.getTargetBlock(player, displrange);
					direction = Tools.getDirection(location,
							targetblock.getLocation()).normalize();
					if (!location.getBlock().equals(targetblock.getLocation())) {
						location = location.clone().add(direction);

						block = location.getBlock();
						if (block.getLocation().equals(
								sourceblock.getLocation())) {
							location = location.clone().add(direction);
							block = location.getBlock();
						}
					}

				} else {
					PluginTools.removeSpouts(location, player);

					double radius = FireBlast.affectingradius;
					Player source = player;
					if (EarthBlast.annihilateBlasts(location, radius, source)
							|| WaterManipulation.shouldAnnihilateBlasts(location,
									radius, source, false)
							|| FireBlast.annihilateBlasts(location, radius,
									source)) {
						new WaterReturn(player, sourceblock, this);
						return false;
					}

					location = location.clone().add(direction);

					block = location.getBlock();
					if (block.getLocation().equals(sourceblock.getLocation())) {
						location = location.clone().add(direction);
						block = location.getBlock();
					}
				}

				if (trail2 != null) {
					if (trail2.getBlock().equals(block)) {
						trail2.revertBlock();
						trail2 = null;
					}
				}

				if (trail != null) {
					if (trail.getBlock().equals(block)) {
						trail.revertBlock();
						trail = null;
						if (trail2 != null) {
							trail2.revertBlock();
							trail2 = null;
						}
					}
				}

				if (BlockTools.isTransparentToEarthbending(player, block)
						&& !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else if (block.getType() != Material.AIR
						&& !BlockTools.isWater(block)) {
					new WaterReturn(player, sourceblock, this);
					return false;
				}

				if (!displacing) {
					for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location,
							FireBlast.affectingradius)) {
						if (entity.getEntityId() != player.getEntityId()) {
							Location location = player.getEyeLocation();
					 		Vector vector = location.getDirection();
					 		entity.setVelocity(vector.normalize().multiply(pushfactor));
							if (AvatarState.isAvatarState(player)) {
								damage = AvatarState.getValue(damage);
							}
								
							EntityTools.damageEntity(player, entity, PluginTools
											.waterbendingNightAugment(damage,
													player.getWorld()));
							progressing = false;
						}
					}
				}

				if (!progressing) {
					new WaterReturn(player, sourceblock, this);
					return false;
				}

				addWater(block);
				reduceWater(sourceblock);

				if (trail2 != null) {
					trail2.revertBlock();
					trail2 = null;
				}
				if (trail != null) {
					trail2 = trail;
					trail2.setType(Material.WATER, (byte) 2);
				}
				trail = new TempBlock(sourceblock, Material.WATER, (byte) 1);
				sourceblock = block;

				if (location.distance(targetdestination) <= 1
						|| location.distance(firstdestination) > range) {

					falling = true;
					progressing = false;
				}
				return true;
			}
		}
		
		return true;
	}

	private void reduceWater(Block block) {
		if (displacing) {
			removeWater(block);
			return;
		}
		if (affectedblocks.containsKey(block)) {
			if (!BlockTools.adjacentToThreeOrMoreSources(block)) {
				// && !Tools.adjacentToAnyWater(block)) {
				block.setType(Material.AIR);
			}
			// oldwater = block;
			affectedblocks.remove(block);
		}
	}

	private void removeWater(Block block) {
		if (block != null) {
			if (affectedblocks.containsKey(block)) {
				if (!BlockTools.adjacentToThreeOrMoreSources(block)) {
					block.setType(Material.AIR);
				}
				affectedblocks.remove(block);
			}
		}
	}

	private void finalRemoveWater(Block block) {
		if (trail != null) {
			trail.revertBlock();
			trail = null;
		}
		if (trail2 != null) {
			trail2.revertBlock();
			trail = null;
		}
		if (displacing) {
			removeWater(block);
			return;
		}
		if (affectedblocks.containsKey(block)) {
			if (!BlockTools.adjacentToThreeOrMoreSources(block)) {
				// && !Tools.adjacentToAnyWater(block)) {
				block.setType(Material.AIR);
			}
			affectedblocks.remove(block);
		}
	}

	private static void addWater(Block block) {
		if (!affectedblocks.containsKey(block)) {
			affectedblocks.put(block, block);
		}
		if (FreezeMelt.isFrozen(block)){
			FreezeMelt.thawThenRemove(block);
		}
			
		block.setType(Material.WATER);
		block.setData(full);
	}

	public static void moveWater(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (!bPlayer.isOnCooldown(Abilities.WaterManipulation)) {

			if (prepared.containsKey(player)) {
				if (instances.containsKey(prepared.get(player))) {
					instances.get(prepared.get(player)).moveWater();
				}
				prepared.remove(player);
			} else if (WaterReturn.hasWaterBottle(player)) {
				Location eyeloc = player.getEyeLocation();
				Block block = eyeloc.add(eyeloc.getDirection().normalize())
						.getBlock();
				if (BlockTools.isTransparentToEarthbending(player, block)
						&& BlockTools.isTransparentToEarthbending(player,
								eyeloc.getBlock())) {

					if (getTargetLocation(player).distance(block.getLocation()) > 1) {
						block.setType(Material.WATER);
						block.setData(full);
						WaterManipulation watermanip = new WaterManipulation(
								player, null);
						watermanip.moveWater();
						if (!watermanip.progressing) {
							block.setType(Material.AIR);
						} else {
							WaterReturn.emptyWaterBottle(player);
						}
					}
				}
			}
		}

		redirectTargettedBlasts(player);
	}

	private static void redirectTargettedBlasts(Player player) {
		for(WaterManipulation manip : instances.values()) {
			if (!manip.progressing){
				continue;
			}				

			if (!manip.location.getWorld().equals(player.getWorld())){
				continue;
			}			

			if (PluginTools.isRegionProtectedFromBuild(player,
					Abilities.WaterManipulation, manip.location)){
				continue;
			}				

			if (manip.player.equals(player)){
				manip.redirect(player, getTargetLocation(player));
			}			

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distance(location) <= range
					&& Tools.getDistanceFromLine(vector, location,
							manip.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				manip.redirect(player, getTargetLocation(player));
			}

		}
	}

	private static void block(Player player) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for(WaterManipulation manip : instances.values()) {
			if (manip.player.equals(player))
				continue;

			if (!manip.location.getWorld().equals(player.getWorld()))
				continue;

			if (!manip.progressing)
				continue;

			if (PluginTools.isRegionProtectedFromBuild(player,
					Abilities.WaterManipulation, manip.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if (mloc.distance(location) <= range
					&& Tools.getDistanceFromLine(vector, location,
							manip.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				toBreak.add(manip);
			}

		}
		
		for(WaterManipulation manip : toBreak) {
			manip.remove();
		}
	}

	public static void progressAll() {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for(WaterManipulation manip : instances.values()) {
			boolean keep = manip.progress();
			if(!keep) {
				toBreak.add(manip);
			}
		}
		for(WaterManipulation manip : toBreak) {
			manip.remove();
		}
	}

	public static boolean canFlowFromTo(Block from, Block to) {
		// if (to.getType() == Material.TORCH)
		// return true;
		if (affectedblocks.containsKey(to) || affectedblocks.containsKey(from)) {
			// Tools.verbose("affectedblocks");
			return false;
		}
		if (WaterSpout.isAffected(to)
				|| WaterSpout.isAffected(from)) {
			// Tools.verbose("waterspout");
			return false;
		}
		if (WaterWall.isAffectedByWaterWall(to)
				|| WaterWall.isAffectedByWaterWall(from)) {
			// Tools.verbose("waterwallaffectedblocks");
			return false;
		}
		if (WaterWall.isWaterWallPart(to)
				|| WaterWall.isWaterWallPart(from)) {
			// Tools.verbose("waterwallwall");
			return false;
		}
		if (Wave.isBlockWave(to) || Wave.isBlockWave(from)) {
			// Tools.verbose("wave");
			return false;
		}
		if (TempBlock.isTempBlock(to) || TempBlock.isTempBlock(from)) {
			// Tools.verbose("tempblock");
			return false;
		}
		if (BlockTools.adjacentToFrozenBlock(to)
				|| BlockTools.adjacentToFrozenBlock(from)) {
			// Tools.verbose("frozen");
			return false;
		}

		return true;
	}

	public static boolean canPhysicsChange(Block block) {
		if (affectedblocks.containsKey(block))
			return false;
		if (WaterSpout.isAffected(block))
			return false;
		if (WaterWall.isAffectedByWaterWall(block))
			return false;
		if (WaterWall.isWaterWallPart(block))
			return false;
		if (Wave.isBlockWave(block))
			return false;
		if (TempBlock.isTempBlock(block))
			return false;
		if (TempBlock.isTouchingTempBlock(block))
			return false;
		return true;
	}
	
	public static void removeAll() {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>(instances.values());
		for (WaterManipulation manip : toBreak)
			manip.remove();
		prepared.clear();
	}

	public static boolean canBubbleWater(Block block) {
		return canPhysicsChange(block);
	}

	public static void removeAroundPoint(Location location, double radius) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for(WaterManipulation manip : instances.values()) {
			if (manip.location.getWorld().equals(location.getWorld()))
				if (manip.location.distance(location) <= radius)
					toBreak.add(manip);
		}
		for (WaterManipulation manip : toBreak)
			manip.remove();
	}
	
	private static boolean shouldAnnihilateBlasts(Location location, double radius,
			Player source, boolean remove) {
		boolean broke = false;
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for(WaterManipulation manip : instances.values()) {
			if (manip.location.getWorld().equals(location.getWorld())
					&& !source.equals(manip.player))
				if (manip.location.distance(location) <= radius) {
					toBreak.add(manip);
					broke = true;
				}
		}
		if(remove) {
			for (WaterManipulation manip : toBreak)
				manip.remove();
		}
		return broke;
	}

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		return shouldAnnihilateBlasts(location, radius, source, true);
	}
	
	public static boolean isWaterManipulater(Player player) {
		for (WaterManipulation manip : instances.values()) {
			if(manip.player.equals(player)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

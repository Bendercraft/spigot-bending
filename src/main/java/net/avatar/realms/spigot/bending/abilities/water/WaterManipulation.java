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

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPathType;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Water Manipulation", element=BendingType.Water)
public class WaterManipulation implements IAbility {
	private static Map<Integer, WaterManipulation> instances = new HashMap<Integer, WaterManipulation>();
	private static Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private static Map<Player, Integer> prepared = new HashMap<Player, Integer>();

	private static int ID = Integer.MIN_VALUE;

	private static final byte full = 0x0;
	// private static final byte half = 0x4;

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
	// private static double speed = 1.5;

	private static Set<Byte> water = new HashSet<Byte>();

	private static long interval = (long) (1000. / SPEED);

	private Player player;
	private BendingPlayer bender;
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
	private int damage;
	private double range;
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
		
		damage = DAMAGE;
		range = RANGE;
		
		bender = BendingPlayer.getBendingPlayer(player);
		
		if(bender.hasPath(BendingPathType.Marksman)) {
			damage *= 0.8;
			range *= 1.4;
		}
		
		if(bender.hasPath(BendingPathType.Flowless)) {
			damage *= 1.15;
		}
		
		if (prepare()) {
			this.id = ID;
			instances.put(this.id, this);
			prepared.put(player, this.id);
			if (ID == Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			}
			ID++;
			this.time = System.currentTimeMillis();
		}
	}

	private boolean prepare() {
		Block block = BlockTools.getWaterSourceBlock(this.player, range,
				EntityTools.canPlantbend(this.player));
		cancelPrevious();
		block(this.player);
		if (block != null) {
			this.sourceblock = block;
			focusBlock();
			return true;
		}
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(this.player);
		if(bPlayer == null) {
			return false;
		}
		
		//If no block available, check if bender can drainbend !
		if(Drainbending.canDrainBend(this.player) && !bPlayer.isOnCooldown(Abilities.Drainbending)) {
			Location location = this.player.getEyeLocation();
			Vector vector = location.getDirection().clone().normalize();
			block = location.clone().add(vector.clone().multiply(2)).getBlock();
			if(Drainbending.canBeSource(block)) {
				this.drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0x0);
				this.sourceblock = block;
				focusBlock();
				bPlayer.cooldown(Abilities.Drainbending, Drainbending.COOLDOWN);
				return true;
			}
		}
		
		return false;
	}

	private void cancelPrevious() {
		if (prepared.containsKey(this.player)) {
			if (instances.containsKey(prepared.get(this.player))) {
				WaterManipulation old = instances.get(prepared.get(this.player));
				if (!old.progressing) {
					old.remove();
				}
			}
		}
	}

	public void remove() {
		finalRemoveWater(this.sourceblock);
		if(this.drainedBlock != null) {
			this.drainedBlock.revertBlock();
			this.drainedBlock = null;
		}
		remove(this.id);
	}

	private void focusBlock() {
		this.location = this.sourceblock.getLocation();
	}

	public void moveWater() {
		if (this.sourceblock == null) {
			return;
		}
		if (this.sourceblock.getWorld() != this.player.getWorld()) {
			return;
		}
		
		this.targetdestination = getTargetLocation(this.player);
		if (this.targetdestination.distance(this.location) <= 1) {
			this.progressing = false;
			this.targetdestination = null;
			remove(this.id);
		} else {
			this.progressing = true;
			this.settingup = true;
			this.firstdestination = getToEyeLevel();
			this.firstdirection = Tools.getDirection(this.sourceblock.getLocation(), this.firstdestination).normalize();
			this.targetdestination = Tools.getPointOnLine(this.firstdestination,
					this.targetdestination, range);
			this.targetdirection = Tools.getDirection(this.firstdestination,
					this.targetdestination).normalize();

			if (BlockTools.isPlant(this.sourceblock)) {
				new Plantbending(this.sourceblock, this);
			}
			addWater(this.sourceblock);
		}
		BendingPlayer.getBendingPlayer(this.player).cooldown(Abilities.WaterManipulation, COOLDOWN);
	}

	private Location getTargetLocation(Player player) {
		Entity target = null;
		if(!bender.hasPath(BendingPathType.Flowless)) {
			target = EntityTools.getTargettedEntity(player, range);
		}
		Location location = null;
		if ((target == null) || ProtectionManager.isEntityProtectedByCitizens(target)) {
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
		Location loc = this.sourceblock.getLocation().clone();
		double dy = this.targetdestination.getY() - this.sourceblock.getY();
		if (dy <= 2) {
			loc.setY(this.sourceblock.getY() + 2);
		} else {
			loc.setY(this.targetdestination.getY() - 1);
		}
		return loc;
	}

	private static void remove(int id) {
		Player player = instances.get(id).player;
		if (prepared.containsKey(player)) {
			if (prepared.get(player) == id) {
				prepared.remove(player);
			}
		}
		instances.remove(id);
	}

	private void redirect(Player player, Location targetlocation) {
		if (this.progressing && !this.settingup) {
			if (this.location.distance(player.getLocation()) <= range) {
				this.targetdirection = Tools.getDirection(this.location, targetlocation)
						.normalize();
			}
			this.targetdestination = targetlocation;
			this.player = player;
		}
	}

	/**
	 * If return false, breakBlock has to be called !
	 * @return
	 */
	private boolean progress() {
		if (this.player.isDead() || !this.player.isOnline()
				|| !EntityTools.canBend(this.player, Abilities.WaterManipulation)) {
			return false;
		}
		if ((System.currentTimeMillis() - this.time) >= interval) {
			// removeWater(oldwater);
			if (ProtectionManager.isRegionProtectedFromBending(this.player,
					Abilities.WaterManipulation, this.location)) {
				return false;
			}

			this.time = System.currentTimeMillis();

			if (!this.progressing
					&& !this.falling
					&& (EntityTools.getBendingAbility(this.player) != Abilities.WaterManipulation)) {
				return false;
			}

			if (this.falling) {
				finalRemoveWater(this.sourceblock);
				new WaterReturn(this.player, this.sourceblock, this);
				return false;

			} else {
				if (!this.progressing) {
					this.sourceblock.getWorld().playEffect(this.location, Effect.SMOKE,
							4, (int) range);
					return true;
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
				if (this.displacing) {
					Block targetblock = EntityTools.getTargetBlock(this.player, this.displrange);
					direction = Tools.getDirection(this.location,
							targetblock.getLocation()).normalize();
					if (!this.location.getBlock().equals(targetblock.getLocation())) {
						this.location = this.location.clone().add(direction);

						block = this.location.getBlock();
						if (block.getLocation().equals(
								this.sourceblock.getLocation())) {
							this.location = this.location.clone().add(direction);
							block = this.location.getBlock();
						}
					}

				} else {
					PluginTools.removeSpouts(this.location, this.player);

					double radius = FireBlast.AFFECTING_RADIUS;
					Player source = this.player;
					if (EarthBlast.annihilateBlasts(this.location, radius, source)
							|| WaterManipulation.shouldAnnihilateBlasts(this.location,
									radius, source, false)
							|| FireBlast.annihilateBlasts(this.location, radius,
									source)) {
						new WaterReturn(this.player, this.sourceblock, this);
						return false;
					}

					this.location = this.location.clone().add(direction);

					block = this.location.getBlock();
					if (block.getLocation().equals(this.sourceblock.getLocation())) {
						this.location = this.location.clone().add(direction);
						block = this.location.getBlock();
					}
				}

				if (this.trail2 != null) {
					if (this.trail2.getBlock().equals(block)) {
						this.trail2.revertBlock();
						this.trail2 = null;
					}
				}

				if (this.trail != null) {
					if (this.trail.getBlock().equals(block)) {
						this.trail.revertBlock();
						this.trail = null;
						if (this.trail2 != null) {
							this.trail2.revertBlock();
							this.trail2 = null;
						}
					}
				}

				if (BlockTools.isTransparentToEarthbending(this.player, block)
						&& !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else if ((block.getType() != Material.AIR)
						&& !BlockTools.isWater(block)) {
					new WaterReturn(this.player, this.sourceblock, this);
					return false;
				}

				if (!this.displacing) {
					for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location,
							FireBlast.AFFECTING_RADIUS)) {
						if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
							continue;
						}
						if (entity.getEntityId() != this.player.getEntityId()) {
							Location location = this.player.getEyeLocation();
							Vector vector = location.getDirection();
							entity.setVelocity(vector.normalize().multiply(PUSHFACTOR));
							if (AvatarState.isAvatarState(this.player)) {
								this.damage = AvatarState.getValue(this.damage);
							}
							
							EntityTools.damageEntity(this.player, entity, PluginTools
									.waterbendingNightAugment(this.damage,
											this.player.getWorld()));
							this.progressing = false;
						}
					}
				}

				if (!this.progressing) {
					new WaterReturn(this.player, this.sourceblock, this);
					return false;
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
				this.trail = new TempBlock(this.sourceblock, Material.WATER, (byte) 1);
				this.sourceblock = block;

				if ((this.location.distance(this.targetdestination) <= 1)
						|| (this.location.distance(this.firstdestination) > range)) {

					this.falling = true;
					this.progressing = false;
				}
				return true;
			}
		}
		
		return true;
	}

	private void reduceWater(Block block) {
		if (this.displacing) {
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
		if (this.trail != null) {
			this.trail.revertBlock();
			this.trail = null;
		}
		if (this.trail2 != null) {
			this.trail2.revertBlock();
			this.trail = null;
		}
		if (this.displacing) {
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

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
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

			if (ProtectionManager.isRegionProtectedFromBending(player,
					Abilities.WaterManipulation, manip.location)){
				continue;
			}

			if (manip.player.equals(player)){
				manip.redirect(player, manip.getTargetLocation(player));
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if ((mloc.distance(location) <= manip.range)
					&& (Tools.getDistanceFromLine(vector, location,
							manip.location) < deflectrange)
					&& (mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1))))) {
				manip.redirect(player, manip.getTargetLocation(player));
			}

		}
	}

	private static void block(Player player) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for(WaterManipulation manip : instances.values()) {
			if (manip.player.equals(player)) {
				continue;
			}

			if (!manip.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (!manip.progressing) {
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player,
					Abilities.WaterManipulation, manip.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = manip.location;
			if ((mloc.distance(location) <= manip.range)
					&& (Tools.getDistanceFromLine(vector, location,
							manip.location) < deflectrange)
					&& (mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1))))) {
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
		if (WaterSpout.isWaterSpoutBlock(to) || WaterSpout.isWaterSpoutBlock(from)) {
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
		if (affectedblocks.containsKey(block)) {
			return false;
		}
		if (WaterSpout.isWaterSpoutBlock(block)) {
			return false;
		}
		if (WaterWall.isAffectedByWaterWall(block)) {
			return false;
		}
		if (WaterWall.isWaterWallPart(block)) {
			return false;
		}
		if (Wave.isBlockWave(block)) {
			return false;
		}
		if (TempBlock.isTempBlock(block)) {
			return false;
		}
		if (TempBlock.isTouchingTempBlock(block)) {
			return false;
		}
		return true;
	}
	
	public static void removeAll() {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>(instances.values());
		for (WaterManipulation manip : toBreak) {
			manip.remove();
		}
		prepared.clear();
	}

	public static boolean canBubbleWater(Block block) {
		return canPhysicsChange(block);
	}

	public static void removeAroundPoint(Location location, double radius) {
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for(WaterManipulation manip : instances.values()) {
			if (manip.location.getWorld().equals(location.getWorld())) {
				if (manip.location.distance(location) <= radius) {
					toBreak.add(manip);
				}
			}
		}
		for (WaterManipulation manip : toBreak) {
			manip.remove();
		}
	}
	
	private static boolean shouldAnnihilateBlasts(Location location, double radius,
			Player source, boolean remove) {
		boolean broke = false;
		List<WaterManipulation> toBreak = new LinkedList<WaterManipulation>();
		for(WaterManipulation manip : instances.values()) {
			if (manip.location.getWorld().equals(location.getWorld())
					&& !source.equals(manip.player)) {
				if (manip.location.distance(location) <= radius) {
					toBreak.add(manip);
					broke = true;
				}
			}
		}
		if(remove) {
			for (WaterManipulation manip : toBreak) {
				manip.remove();
			}
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
		return this.parent;
	}
}

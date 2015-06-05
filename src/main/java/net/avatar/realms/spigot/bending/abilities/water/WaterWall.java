package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WaterWall implements IAbility {
	private static Map<Integer, WaterWall> instances = new HashMap<Integer, WaterWall>();

	private static final long interval = 30;

	private static Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private static Map<Block, Player> wallblocks = new HashMap<Block, Player>();

	private static final byte full = 0x0;
	// private static final byte half = 0x4;

	private static double range = ConfigManager.waterWallRange;
	private static final double defaultradius = ConfigManager.waterWallRadius;
	// private static double speed = 1.5;

	Player player;
	private Location location = null;
	private Block sourceblock = null;
	// private Block oldwater = null;
	private boolean progressing = false;
	private Location firstdestination = null;
	private Location targetdestination = null;
	private Vector firstdirection = null;
	private Vector targetdirection = null;
	// private boolean falling = false;
	private boolean settingup = false;
	private boolean forming = false;
	private boolean frozen = false;
	private long time;
	private double radius = defaultradius;
	private IAbility parent;

	private TempBlock drainedBlock;

	public WaterWall(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (Wave.isWaving(player)) {
			Wave wave = Wave.getWave(player);
			if (!wave.progressing) {
				Wave.launch(player);
				return;
			}
		}

		if (AvatarState.isAvatarState(player)) {
			radius = AvatarState.getValue(radius);
		}
		if (instances.containsKey(player.getEntityId())) {
			if (instances.get(player.getEntityId()).progressing) {
				freezeThaw(player);
			} else if (prepare()) {
				if (instances.containsKey(player.getEntityId())) {
					instances.get(player.getEntityId()).remove();
				}

				instances.put(player.getEntityId(), this);
				time = System.currentTimeMillis();
			}
		} else if (prepare()) {
			if (instances.containsKey(player.getEntityId())) {
				instances.get(player.getEntityId()).remove();
			}
			instances.put(player.getEntityId(), this);
			time = System.currentTimeMillis();
		}

		if (bPlayer.isOnCooldown(Abilities.Surge)) {
			return;
		}
			

		if (!instances.containsKey(player.getEntityId())
				&& WaterReturn.hasWaterBottle(player)) {

			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (BlockTools.isTransparentToEarthbending(player, block)
					&& BlockTools.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(full);
				Wave wave = new Wave(player, this);
				wave.canhitself = false;
				wave.moveWater();
				if (!wave.progressing) {
					block.setType(Material.AIR);
					wave.remove();
				} else {
					WaterReturn.emptyWaterBottle(player);
				}
			}

		}
	}

	private static void freezeThaw(Player player) {
		instances.get(player.getEntityId()).freezeThaw();
	}

	private void freezeThaw() {
		if (frozen) {
			thaw();
		} else {
			freeze();
		}
	}

	private void freeze() {
		frozen = true;
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				new TempBlock(block, Material.ICE, (byte) 0, player, WaterWall.class);
			}
		}
	}

	private void thaw() {
		frozen = false;
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				new TempBlock(block, Material.WATER, full, player, WaterWall.class);
			}
		}
	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockTools.getWaterSourceBlock(player, range,
				EntityTools.canPlantbend(player));
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
				drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0x0, player, WaterWall.class);
				sourceblock = block;
				focusBlock();
				//Radius is thirded for Drainbending
				radius = radius/3;
				bPlayer.cooldown(Abilities.Drainbending);
				return true;
			}
		}
		return false;
	}

	private void cancelPrevious() {
		if (instances.containsKey(player.getEntityId())) {
			WaterWall old = instances.get(player.getEntityId());
			if (old.progressing) {
				old.removeWater(old.sourceblock);
			} else {
				old.remove();
			}
		}
	}

	public void remove() {
		finalRemoveWater(sourceblock);
		if(drainedBlock != null) {
			drainedBlock.revertBlock();
		}
		instances.remove(player.getEntityId());
	}

	private void focusBlock() {
		location = sourceblock.getLocation();
	}

	public void moveWater() {
		if (sourceblock != null) {
			targetdestination = EntityTools.getTargetedLocation(player, range, BlockTools.getTransparentEarthbending());
			//targetdestination = Tools.getTargetBlock(player, range, Tools.getTransparentEarthbending()).getLocation();

			if (targetdestination.distance(location) <= 1) {
				progressing = false;
				targetdestination = null;
			} else {
				progressing = true;
				settingup = true;
				firstdestination = getToEyeLevel();
				firstdirection = getDirection(sourceblock.getLocation(),
						firstdestination);
				targetdirection = getDirection(firstdestination,
						targetdestination);
				if (BlockTools.isPlant(sourceblock))
					new Plantbending(sourceblock, this);
				if (!BlockTools.adjacentToThreeOrMoreSources(sourceblock)) {
					sourceblock.setType(Material.AIR);
				}
				addWater(sourceblock);
			}

		}
	}

	private Location getToEyeLevel() {
		Location loc = sourceblock.getLocation().clone();
		loc.setY(targetdestination.getY());
		return loc;
	}

	private Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;

		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();

		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();

		return new Vector(x1 - x0, y1 - y0, z1 - z0);

	}

	public boolean progress() {
		//TODO : Try to find why sometimes, the watersource is going far in the sky.
		if (player.isDead() || !player.isOnline()) {
			breakBlock();
			return false;
		}
		if (!EntityTools.canBend(player, Abilities.Surge)) {
			if (!forming)
				breakBlock();
			returnWater();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (!progressing
					&& EntityTools.getBendingAbility(player) != Abilities.Surge) {
				return false;
			}

			if (progressing
					&& (!player.isSneaking() || EntityTools.getBendingAbility(player) != Abilities.Surge)) {
				breakBlock();
				returnWater();
				return false;
			}

			if (!progressing) {
				sourceblock.getWorld().playEffect(location, Effect.SMOKE, 4,
						(int) range);
				return true;
			}

			if (forming) {
				List<Block> blocks = new LinkedList<Block>();
				Set<Material> transparentForSelection = new HashSet<Material>();
				transparentForSelection.add(Material.AIR);
				transparentForSelection.add(Material.WATER);
				transparentForSelection.add(Material.STATIONARY_WATER);
				transparentForSelection.add(Material.SNOW);
				transparentForSelection.add(Material.ICE);
				Location loc = EntityTools.getTargetedLocation(player, (int) range,
						transparentForSelection);
				location = loc.clone();
				Vector dir = player.getEyeLocation().getDirection();
				Vector vec;
				Block block;
				for (double i = 0; i <= PluginTools.waterbendingNightAugment(radius,
						player.getWorld()); i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						// loc.getBlock().setType(Material.GLOWSTONE);
						vec = Tools.getOrthogonalVector(dir.clone(), angle, i);
						block = loc.clone().add(vec).getBlock();
						if (ProtectionManager.isRegionProtectedFromBending(player,
								Abilities.Surge, block.getLocation()))
							continue;
						if (wallblocks.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block)
								&& (block.getType() == Material.AIR
										|| block.getType() == Material.FIRE || BlockTools
											.isWaterbendable(block, player))) {
							wallblocks.put(block, player);
							addWallBlock(block);

							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(
									block.getLocation(), 2);

						}
					}
				}
				
				List<Block> toRemove = new LinkedList<Block>(wallblocks.keySet());
				for (Block blocki : toRemove) {
					if (wallblocks.get(blocki) == player
							&& !blocks.contains(blocki)) {
						finalRemoveWater(blocki);
					}
				}

				return true;
			}

			if (sourceblock.getLocation().distance(firstdestination) < .5
					&& settingup) {
				settingup = false;
			}

			Vector direction;
			if (settingup) {
				direction = firstdirection;
			} else {
				direction = targetdirection;
			}

			location = location.clone().add(direction);

			Block block = location.getBlock();
			if (block.getLocation().equals(sourceblock.getLocation())) {
				location = location.clone().add(direction);
				block = location.getBlock();
			}
			if (block.getType() != Material.AIR) {
				breakBlock();
				returnWater();
				return false;
			}

			if (!progressing) {
				breakBlock();
				return false;
			}

			addWater(block);
			removeWater(sourceblock);
			sourceblock = block;

			if (location.distance(targetdestination) < 1) {
				removeWater(sourceblock);
				forming = true;
			}

			return true;
		}

		return true;
	}

	private void addWallBlock(Block block) {
		if (frozen) {
			new TempBlock(block, Material.ICE, (byte) 0, player, WaterWall.class);
		} else {
			new TempBlock(block, Material.WATER, full, player, WaterWall.class);
		}
	}

	private void breakBlock() {
		finalRemoveWater(sourceblock);
		List<Block> toRemove = new LinkedList<Block>();
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				toRemove.add(block);
			}
		}
		for (Block block : toRemove) {
			finalRemoveWater(block);
		}
	}

	private void removeWater(Block block) {
		if (block != null) {
			if (affectedblocks.containsKey(block)) {
				if (!BlockTools.adjacentToThreeOrMoreSources(block)) {
					TempBlock.revertBlock(block);
				}
				affectedblocks.remove(block);
			}
		}
	}

	private static void finalRemoveWater(Block block) {
		if (affectedblocks.containsKey(block)) {
			TempBlock.revertBlock(block);
			affectedblocks.remove(block);
		}

		if (wallblocks.containsKey(block)) {
			TempBlock.revertBlock(block);
			wallblocks.remove(block);
		}
	}

	private void addWater(Block block) {

		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.Surge,
				block.getLocation()))
			return;

		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.WATER, full, player, WaterWall.class);
			// new TempBlock(block, Material.ICE, (byte) 0);
			affectedblocks.put(block, block);
		}
	}

	public static void moveWater(Player player) {
		if (instances.containsKey(player.getEntityId())) {
			instances.get(player.getEntityId()).moveWater();
		}
	}

	public static void progressAll() {
		List<WaterWall> toRemove = new LinkedList<WaterWall>();
		for(WaterWall wall : instances.values()) {
			boolean keep = wall.progress();
			if(!keep) {
				toRemove.add(wall);
			}
		}
		for(WaterWall wall : toRemove) {
			wall.remove();
		}
	}

	public static void form(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (!instances.containsKey(player.getEntityId())) {
			if (!Wave.isWaving(player)
					&& BlockTools.getWaterSourceBlock(player,
							(int) Wave.defaultrange, EntityTools.canPlantbend(player)) == null
					&& WaterReturn.hasWaterBottle(player)) {

				if (bPlayer.isOnCooldown(Abilities.Surge)) {
					return;
				}
					
				Location eyeloc = player.getEyeLocation();
				Block block = eyeloc.add(eyeloc.getDirection().normalize())
						.getBlock();
				if (BlockTools.isTransparentToEarthbending(player, block)
						&& BlockTools.isTransparentToEarthbending(player,
								eyeloc.getBlock())) {
					block.setType(Material.WATER);
					block.setData(full);
					
					WaterWall wall = new WaterWall(player, null);
					wall.moveWater();
					if (!wall.progressing) {
						block.setType(Material.AIR);
						wall.remove();
					} else {
						WaterReturn.emptyWaterBottle(player);
					}
					return;
				}
			}
			new Wave(player, null);
			return;
		} else {
			if (BlockTools.isWaterbendable(
					EntityTools.getTargetBlock(player, (int) Wave.defaultrange),
					player)) {
				new Wave(player, null);
				return;
			}
		}
		moveWater(player);
	}

	public static void removeAll() {
		List<Block> toRemoveAffected = new LinkedList<Block>(affectedblocks.values());
		for (Block block : toRemoveAffected) {
			TempBlock.revertBlock(block);
			affectedblocks.remove(block);
			wallblocks.remove(block);
		}
		List<Block> toRemoveWall = new LinkedList<Block>(wallblocks.keySet());
		for (Block block : toRemoveWall) {
			TempBlock.revertBlock(block);
			affectedblocks.remove(block);
			wallblocks.remove(block);
		}
	}

	public static boolean canThaw(Block block) {
		if (wallblocks.keySet().contains(block))
			return false;
		return true;
	}

	public static void thaw(Block block) {
		finalRemoveWater(block);
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (instances.containsKey(player.getEntityId())) {
			WaterWall wall = instances.get(player.getEntityId());
			if (wall.sourceblock == null)
				return false;
			if (wall.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	private void returnWater() {
		if (location != null) {
			new WaterReturn(player, location.getBlock(), this);
		}
	}
	
	public static boolean isWaterWalling(Player player) {
		for (WaterWall wall : instances.values()) {
			if (wall.player.equals(player))
				return true;
		}
		return false;
	}
	
	public static boolean isAffectedByWaterWall(Block block) {
		return affectedblocks.containsKey(block);
	}
	
	public static boolean isWaterWallPart(Block block) {
		return wallblocks.containsKey(block);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

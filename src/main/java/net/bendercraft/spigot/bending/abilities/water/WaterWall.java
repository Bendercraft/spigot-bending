package net.bendercraft.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

@ABendingAbility(name = WaterWall.NAME, element = BendingElement.WATER)
public class WaterWall extends BendingActiveAbility {
	public final static String NAME = "Surge";
	
	private static final long interval = 30;

	private static Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private static Map<Block, Player> wallblocks = new HashMap<Block, Player>();

	@ConfigurationParameter("Range")
	private static double RANGE = 5;

	@ConfigurationParameter("Radius")
	private static double RADIUS = 2;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

	private Location location = null;
	private Block sourceblock = null;
	private boolean progressing = false;
	private Location firstdestination = null;
	private Location targetdestination = null;
	private Vector firstdirection = null;
	private Vector targetdirection = null;
	private boolean settingup = false;
	private boolean forming = false;
	private boolean frozen = false;
	private long time;

	private TempBlock drainedBlock;
	private Wave wave;
	private WaterReturn waterReturn;
	
	private double radius;

	public WaterWall(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.radius = RADIUS;
		if(bender.hasPerk(BendingPerk.WATER_WATERWALL_RADIUS_1)) {
			this.radius += 1;
		}
		if(bender.hasPerk(BendingPerk.WATER_WATERWALL_RADIUS_2)) {
			this.radius += 1;
		}
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			if (bender.isOnCooldown(NAME)) {
				return false;
			}
			wave = new Wave(this, player);
			if (wave.prepare()) {
				setState(BendingAbilityState.PREPARED);
			}
		} else if (getState() == BendingAbilityState.PREPARED) {
			if (wave == null && moveWater()) {// Build wall
				setState(BendingAbilityState.PROGRESSING);
				bender.water.ice();
			} else {
				remove();
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
			if (wave != null) {
				wave.freeze();
			}
		}

		return false;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			if (bender.isOnCooldown(NAME)) {
				return false;
			}
			// WaterWall !
			if (prepare()) {
				setState(BendingAbilityState.PREPARED);
			}
		} else if (getState() == BendingAbilityState.PREPARED) {
			if (wave != null) {
				wave.moveWater();
				bender.water.liquid();
				setState(BendingAbilityState.PROGRESSING);
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
			if (wave == null) {
				freezeThaw();
			}
		}
		return false;
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
				TempBlock.makeTemporary(this, block, Material.ICE, false);
			}
		}
	}

	private void thaw() {
		frozen = false;
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				TempBlock.makeTemporary(this, block, Material.WATER, false);
			}
		}
	}

	public boolean prepare() {
		if(this.drainedBlock != null) {
			this.drainedBlock.revertBlock();
			this.drainedBlock = null;
		}
		Block block = BlockTools.getWaterSourceBlock(player, RANGE, EntityTools.canPlantbend(player));
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		// If no block available, check if bender can drainbend !
		if (Drainbending.canDrainBend(player) && !bender.isOnCooldown(Drainbending.NAME)) {
			Location location = player.getEyeLocation();
			Vector vector = location.getDirection().clone().normalize();
			block = location.clone().add(vector.clone().multiply(2)).getBlock();
			if (Drainbending.canBeSource(block)) {
				drainedBlock = TempBlock.makeTemporary(this, block, Material.WATER, false);
				sourceblock = block;
				focusBlock();
				// Radius is thirded for Drainbending
				radius = radius / 3;
				bender.cooldown(Drainbending.NAME, Drainbending.COOLDOWN);
				return true;
			}
		}
		return false;
	}

	@Override
	public void stop() {
		breakBlock();
		finalRemoveWater(sourceblock);
		if (drainedBlock != null) {
			drainedBlock.revertBlock();
		}
		if(wave != null) {
			wave.remove();
		}
		if(waterReturn != null) {
			waterReturn.stop();
		}
	}

	private void focusBlock() {
		location = sourceblock.getLocation();
	}

	public boolean moveWater() {
		if (sourceblock != null) {
			targetdestination = EntityTools.getTargetedLocation(player, RANGE, BlockTools.getTransparentEarthbending());
			// targetdestination = Tools.getTargetBlock(player, range,
			// Tools.getTransparentEarthbending()).getLocation();

			if (targetdestination.distance(location) <= 1.1) {
				progressing = false;
				targetdestination = null;
				return false;
			} else {
				progressing = true;
				settingup = true;
				firstdestination = getToEyeLevel();
				firstdirection = getDirection(sourceblock.getLocation(), firstdestination);
				targetdirection = getDirection(firstdestination, targetdestination);
				if (!BlockTools.adjacentToThreeOrMoreSources(sourceblock)) {
					sourceblock.setType(Material.AIR);
				}
				addWater(sourceblock);
				return true;
			}

		}
		return false;
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

	@Override
	public void progress() {
		if (wave != null) {
			if(!wave.progress()) {
				remove();
			}
			return;
		}

		if (player.isDead() || !player.isOnline()) {
			breakBlock();
			remove();
			return;
		}
		if (!EntityTools.canBend(player, register)) {
			if (!forming)
				breakBlock();
			returnWater();
			return;
		}
		
		if(waterReturn != null) {
			if(!waterReturn.progress()) {
				remove();
				return;
			}
		}
		
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (!progressing && !NAME.equals(EntityTools.getBendingAbility(player))) {
				remove();
				return;
			}

			if (progressing && (!player.isSneaking() || !NAME.equals(EntityTools.getBendingAbility(player)))) {
				breakBlock();
				returnWater();
				return;
			}

			if (!progressing) {
				this.player.spawnParticle(Particle.SMOKE_NORMAL, location.clone().add(0.5, 0, 0.5), 2, 0, 0, 0, 0);
				return;
			}

			if (forming) {
				List<Block> blocks = new LinkedList<Block>();
				Set<Material> transparentForSelection = new HashSet<Material>();
				transparentForSelection.addAll(BlockTools.getAirs());
				transparentForSelection.add(Material.WATER);
				transparentForSelection.add(Material.SNOW);
				transparentForSelection.add(Material.ICE);
				Location loc = EntityTools.getTargetedLocation(player, (int) RANGE, transparentForSelection);
				location = loc.clone();
				Vector dir = player.getEyeLocation().getDirection();
				Vector vec;
				Block block;
				for (double i = 0; i <= radius; i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						// loc.getBlock().setType(Material.GLOWSTONE);
						vec = Tools.getOrthogonalVector(dir.clone(), angle, i);
						block = loc.clone().add(vec).getBlock();
						if (ProtectionManager.isLocationProtectedFromBending(player, register, block.getLocation()))
							continue;
						if (wallblocks.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block) && (BlockTools.isAir(block) || block.getType() == Material.FIRE || BlockTools.isWaterbendable(block, player))) {
							wallblocks.put(block, player);
							addWallBlock(block);

							blocks.add(block);
							FireBlast.removeFireBlastsAroundPoint(block.getLocation(), 2);

						}
					}
				}

				List<Block> toRemove = new LinkedList<Block>(wallblocks.keySet());
				for (Block blocki : toRemove) {
					if (wallblocks.get(blocki) == player && !blocks.contains(blocki)) {
						finalRemoveWater(blocki);
					}
				}

				return;
			}

			if (sourceblock.getLocation().distance(firstdestination) < 1.0 && settingup) {
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
			if (!BlockTools.isAir(block)) {
				breakBlock();
				returnWater();
				return;
			}

			if (!progressing) {
				breakBlock();
				remove();
				return;
			}

			addWater(block);
			removeWater(sourceblock);
			sourceblock = block;

			if (location.distance(targetdestination) < 1) {
				removeWater(sourceblock);
				forming = true;
			}
		}
	}

	private void addWallBlock(Block block) {
		if (frozen) {
			TempBlock.makeTemporary(this, block, Material.ICE, false);
		} else {
			TempBlock.makeTemporary(this, block, Material.WATER, false);
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
		if (ProtectionManager.isLocationProtectedFromBending(player, register, block.getLocation()))
			return;

		if (!TempBlock.isTempBlock(block)) {
			TempBlock.makeTemporary(this, block, Material.WATER, false);
			affectedblocks.put(block, block);
		}
	}

	public static void thaw(Block block) {
		finalRemoveWater(block);
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (AbilityManager.getManager().getInstances(NAME).containsKey(player)) {
			WaterWall wall = (WaterWall) AbilityManager.getManager().getInstances(NAME).get(player);
			if (wall.sourceblock == null)
				return false;
			if (wall.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	private void returnWater() {
		if (location != null) {
			waterReturn = new WaterReturn(player, location.getBlock(), this);
		}
	}

	public static boolean isWaterWalling(Player player) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			WaterWall wall = (WaterWall) ab;
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

	public Wave getWave() {
		return wave;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}
}

package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@ABendingAbility(name = "Surge", bind = BendingAbilities.Surge, element = BendingElement.Water)
public class WaterWall extends BendingActiveAbility {
	private static final long interval = 30;

	private static Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private static Map<Block, Player> wallblocks = new HashMap<Block, Player>();

	@ConfigurationParameter("Range")
	private static double RANGE = 5;

	@ConfigurationParameter("Radius")
	private static double RADIUS = 2;

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
	private double radius = RADIUS;

	private TempBlock drainedBlock;

	private Wave wave;

	public WaterWall(Player player) {
		super(player);
		if (AvatarState.isAvatarState(player)) {
			radius = AvatarState.getValue(radius);
		}
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.Start) {
			if (bender.isOnCooldown(AbilityManager.getManager().getAbilityType(this))) {
				return false;
			}
			wave = new Wave(player);
			if (wave.prepare()) {
				setState(BendingAbilityState.Prepared);
			}
		} else if (getState() == BendingAbilityState.Prepared) {
			if (wave == null) {
				moveWater(); // Build wall
				setState(BendingAbilityState.Progressing);
			} else {
				remove();
			}
		} else if (getState() == BendingAbilityState.Progressing) {
			if (wave != null) {
				wave.freeze();
			}
		}

		return false;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.Start) {
			if (bender.isOnCooldown(AbilityManager.getManager().getAbilityType(this))) {
				return false;
			}
			// WaterWall !
			if (prepare()) {
				setState(BendingAbilityState.Prepared);
			}
		} else if (getState() == BendingAbilityState.Prepared) {
			if (wave != null) {
				wave.moveWater();
				setState(BendingAbilityState.Progressing);
			}
		} else if (getState() == BendingAbilityState.Progressing) {
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
				//new TempBlock(block, Material.ICE, (byte) 0);
				TempBlock.makeTemporary(block, Material.ICE);
			}
		}
	}

	private void thaw() {
		frozen = false;
		for (Block block : wallblocks.keySet()) {
			if (wallblocks.get(block) == player) {
				//new TempBlock(block, Material.WATER, full);
				TempBlock.makeTemporary(block, Material.WATER);
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
		if (Drainbending.canDrainBend(player) && !bender.isOnCooldown(BendingAbilities.Drainbending)) {
			Location location = player.getEyeLocation();
			Vector vector = location.getDirection().clone().normalize();
			block = location.clone().add(vector.clone().multiply(2)).getBlock();
			if (Drainbending.canBeSource(block)) {
				//drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0x0);
				drainedBlock = TempBlock.makeTemporary(block, Material.STATIONARY_WATER);
				sourceblock = block;
				focusBlock();
				// Radius is thirded for Drainbending
				radius = radius / 3;
				bender.cooldown(BendingAbilities.Drainbending, Drainbending.COOLDOWN);
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
		if (wave != null) {
			wave.remove();
		}
	}

	private void focusBlock() {
		location = sourceblock.getLocation();
	}

	public void moveWater() {
		if (sourceblock != null) {
			targetdestination = EntityTools.getTargetedLocation(player, RANGE, BlockTools.getTransparentEarthbending());
			// targetdestination = Tools.getTargetBlock(player, range,
			// Tools.getTransparentEarthbending()).getLocation();

			if (targetdestination.distance(location) <= 1) {
				progressing = false;
				targetdestination = null;
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
		if (!EntityTools.canBend(player, BendingAbilities.Surge)) {
			if (!forming)
				breakBlock();
			returnWater();
			return;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (!progressing && EntityTools.getBendingAbility(player) != BendingAbilities.Surge) {
				remove();
				return;
			}

			if (progressing && (!player.isSneaking() || EntityTools.getBendingAbility(player) != BendingAbilities.Surge)) {
				breakBlock();
				returnWater();
				return;
			}

			if (!progressing) {
				sourceblock.getWorld().playEffect(location, Effect.SMOKE, 4, (int) RANGE);
				return;
			}

			if (forming) {
				List<Block> blocks = new LinkedList<Block>();
				Set<Material> transparentForSelection = new HashSet<Material>();
				transparentForSelection.add(Material.AIR);
				transparentForSelection.add(Material.WATER);
				transparentForSelection.add(Material.STATIONARY_WATER);
				transparentForSelection.add(Material.SNOW);
				transparentForSelection.add(Material.ICE);
				Location loc = EntityTools.getTargetedLocation(player, (int) RANGE, transparentForSelection);
				location = loc.clone();
				Vector dir = player.getEyeLocation().getDirection();
				Vector vec;
				Block block;
				for (double i = 0; i <= PluginTools.waterbendingNightAugment(radius, player.getWorld()); i += 0.5) {
					for (double angle = 0; angle < 360; angle += 10) {
						// loc.getBlock().setType(Material.GLOWSTONE);
						vec = Tools.getOrthogonalVector(dir.clone(), angle, i);
						block = loc.clone().add(vec).getBlock();
						if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.Surge, block.getLocation()))
							continue;
						if (wallblocks.containsKey(block)) {
							blocks.add(block);
						} else if (!blocks.contains(block) && (block.getType() == Material.AIR || block.getType() == Material.FIRE || BlockTools.isWaterbendable(block, player))) {
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

			if (sourceblock.getLocation().distance(firstdestination) < .5 && settingup) {
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
			//new TempBlock(block, Material.ICE, (byte) 0);
			TempBlock.makeTemporary(block, Material.ICE);
		} else {
			//new TempBlock(block, Material.WATER, full);
			TempBlock.makeTemporary(block, Material.WATER);
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
		if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.Surge, block.getLocation()))
			return;

		if (!TempBlock.isTempBlock(block)) {
			//new TempBlock(block, Material.WATER, full);
			TempBlock.makeTemporary(block, Material.WATER);
			affectedblocks.put(block, block);
		}
	}

	public static void thaw(Block block) {
		finalRemoveWater(block);
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (AbilityManager.getManager().getInstances(BendingAbilities.Surge).containsKey(player)) {
			WaterWall wall = (WaterWall) AbilityManager.getManager().getInstances(BendingAbilities.Surge).get(player);
			if (wall.sourceblock == null)
				return false;
			if (wall.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	private void returnWater() {
		if (location != null) {
			// new WaterReturn(player, location.getBlock(), this); TODO temp
		}
	}

	public static boolean isWaterWalling(Player player) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.Surge).values()) {
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

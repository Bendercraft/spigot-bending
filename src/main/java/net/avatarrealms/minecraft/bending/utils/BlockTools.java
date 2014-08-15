package net.avatarrealms.minecraft.bending.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.abilities.earth.EarthColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthPassive;
import net.avatarrealms.minecraft.bending.abilities.water.FreezeMelt;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.Information;
import net.avatarrealms.minecraft.bending.model.TempBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlockTools {
	
	private static final ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
	public static ArrayList<Block> tempnophysics = new ArrayList<Block>();
	private static Set<Material> plantIds = new HashSet<Material>();
	static {
		plantIds.add(Material.SAPLING);
		plantIds.add(Material.LEAVES);
		plantIds.add(Material.LONG_GRASS); // Not sure here, previously ID 31
		plantIds.add(Material.DOUBLE_PLANT);
		plantIds.add(Material.DEAD_BUSH);
		plantIds.add(Material.YELLOW_FLOWER);
		plantIds.add(Material.RED_ROSE);
		plantIds.add(Material.BROWN_MUSHROOM);
		plantIds.add(Material.RED_MUSHROOM);
		plantIds.add(Material.CROPS);
		plantIds.add(Material.CACTUS);
		plantIds.add(Material.SUGAR_CANE_BLOCK);
		plantIds.add(Material.PUMPKIN);
		plantIds.add(Material.HUGE_MUSHROOM_1);// 99, BROWN MUSHROOM CAP
		plantIds.add(Material.HUGE_MUSHROOM_2);// 100, RED MUSHROOM CAP
		plantIds.add(Material.MELON_BLOCK);
		plantIds.add(Material.PUMPKIN_STEM);
		plantIds.add(Material.MELON_STEM);
		plantIds.add(Material.VINE);
		plantIds.add(Material.WATER_LILY);
		plantIds.add(Material.CARROT);
		plantIds.add(Material.POTATO);
		plantIds.add(Material.LEAVES_2); //Wut acacia here ? (Wanted ID 162)
		//plantIds.add(); SUN FLOWER wanted here (ID 175)
	}
	
	public static Set<Material> transparentEarthbending = new HashSet<Material>();
	static {
		transparentEarthbending.add(Material.AIR);
		transparentEarthbending.add(Material.SAPLING);
		transparentEarthbending.add(Material.WATER);
		transparentEarthbending.add(Material.STATIONARY_WATER);
		transparentEarthbending.add(Material.LAVA);
		transparentEarthbending.add(Material.STATIONARY_LAVA);
		transparentEarthbending.add(Material.WEB); // Not sure here, previously ID 30
		transparentEarthbending.add(Material.LONG_GRASS); // Not sure here, previously ID 31
		transparentEarthbending.add(Material.DOUBLE_PLANT);
		transparentEarthbending.add(Material.DEAD_BUSH); 
		transparentEarthbending.add(Material.YELLOW_FLOWER); 
		transparentEarthbending.add(Material.RED_ROSE);
		transparentEarthbending.add(Material.BROWN_MUSHROOM); 
		transparentEarthbending.add(Material.RED_MUSHROOM); 
		transparentEarthbending.add(Material.TORCH);
		transparentEarthbending.add(Material.FIRE);
		transparentEarthbending.add(Material.CROPS); 
		transparentEarthbending.add(Material.SNOW); 
		transparentEarthbending.add(Material.SUGAR_CANE);
		transparentEarthbending.add(Material.VINE);
	}

	public static Set<Material> nonOpaque = new HashSet<Material>();
	static {
		nonOpaque.add(Material.AIR);
		nonOpaque.add(Material.SAPLING);
		nonOpaque.add(Material.WATER);
		nonOpaque.add(Material.STATIONARY_WATER);
		nonOpaque.add(Material.LAVA);
		nonOpaque.add(Material.STATIONARY_LAVA);
		nonOpaque.add(Material.POWERED_RAIL);
		nonOpaque.add(Material.DETECTOR_RAIL);
		nonOpaque.add(Material.WEB); // Not sure here, previously ID 30
		nonOpaque.add(Material.LONG_GRASS); // Not sure here, previously ID 31
		nonOpaque.add(Material.DOUBLE_PLANT);
		nonOpaque.add(Material.DEAD_BUSH);
		nonOpaque.add(Material.YELLOW_FLOWER);
		nonOpaque.add(Material.RED_ROSE);
		nonOpaque.add(Material.BROWN_MUSHROOM);
		nonOpaque.add(Material.RED_MUSHROOM);
		nonOpaque.add(Material.TORCH);
		nonOpaque.add(Material.FIRE);
		nonOpaque.add(Material.REDSTONE_WIRE);
		nonOpaque.add(Material.CROPS);
		nonOpaque.add(Material.RAILS);
		nonOpaque.add(Material.WALL_SIGN);
		nonOpaque.add(Material.LEVER);
		nonOpaque.add(Material.STONE_PLATE);
		nonOpaque.add(Material.WOOD_PLATE);
		nonOpaque.add(Material.REDSTONE_TORCH_OFF);
		nonOpaque.add(Material.REDSTONE_TORCH_ON);
		nonOpaque.add(Material.STONE_BUTTON);
		nonOpaque.add(Material.SNOW);
		nonOpaque.add(Material.SUGAR_CANE);
		nonOpaque.add(Material.PORTAL);
		nonOpaque.add(Material.DIODE);
		nonOpaque.add(Material.DIODE_BLOCK_ON); // 93 ? 
		nonOpaque.add(Material.DIODE_BLOCK_OFF); // 94 ?
		nonOpaque.add(Material.PUMPKIN_STEM);
		nonOpaque.add(Material.MELON_STEM);
		nonOpaque.add(Material.VINE);
		nonOpaque.add(Material.WATER_LILY);
		nonOpaque.add(Material.NETHER_WARTS);
		nonOpaque.add(Material.ENDER_PORTAL);
		nonOpaque.add(Material.COCOA);
		nonOpaque.add(Material.TRIPWIRE_HOOK);
		nonOpaque.add(Material.TRIPWIRE);
	}
	
	public static ConcurrentHashMap<Block, Information> movedEarth = new ConcurrentHashMap<Block, Information>();
	public static ConcurrentHashMap<Integer, Information> tempAir = new ConcurrentHashMap<Integer, Information>();
	
	public static Set<Material> getTransparentEarthbending() {
		Set<Material> set = new HashSet<Material>();
		for (Material i : transparentEarthbending) {
			set.add(i);
		}
		return set;
	}
	
	public static boolean isTransparentToEarthbending(Player player, Block block) {
		return isTransparentToEarthbending(player, Abilities.RaiseEarth, block);
	}

	public static boolean isTransparentToEarthbending(Player player,
			Abilities ability, Block block) {
		if (Tools.isRegionProtectedFromBuild(player, ability,
				block.getLocation()))
			return false;
		if (transparentEarthbending.contains(block.getType()))
			return true;
		return false;
	}
	
	public static boolean isObstructed(Location location1, Location location2) {
		Vector loc1 = location1.toVector();
		Vector loc2 = location2.toVector();

		Vector direction = loc2.subtract(loc1);
		direction.normalize();

		Location loc;

		double max = location1.distance(location2);

		for (double i = 0; i <= max; i++) {
			loc = location1.clone().add(direction.clone().multiply(i));
			Material type = loc.getBlock().getType();
			if (type != Material.AIR
					&& !transparentEarthbending.contains(type))
				return true;
		}
		return false;
	}
	
	public static boolean isSolid(Block block) {
		if (nonOpaque.contains(block.getType()))
			return false;
		return true;
	}
	
	public static boolean isMeltable(Block block) {
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW) {
			return true;
		}
		return false;
	}
	
	public static boolean isEarthbendable(Player player, Block block) {
		return isEarthbendable(player, Abilities.RaiseEarth, block);
	}

	public static boolean isEarthbendable(Player player, Abilities ability,
			Block block) {
		if (Tools.isRegionProtectedFromBuild(player, ability,
				block.getLocation()))
			return false;
		Material material = block.getType();

		for (String s : ConfigManager.earthbendable) {
			if (material == Material.getMaterial(s)) {
				return true;
			}
		}
		return false;
	}
		
	public static int getEarthbendableBlocksLength(Player player, Block block,
			Vector direction, int maxlength) {
		Location location = block.getLocation();
		direction = direction.normalize();
		double j;
		for (int i = 0; i <= maxlength; i++) {
			j = (double) i;
			if (!isEarthbendable(player,
					location.clone().add(direction.clone().multiply(j))
							.getBlock())) {
				return i;
			}
		}
		return maxlength;
	}
	
	public static boolean isPlant(Block block) {
		if (plantIds.contains(block.getType()))
			return true;
		return false;
	}
	
	public static boolean isWater(Block block) {
		if (block.getType() == Material.WATER
				|| block.getType() == Material.STATIONARY_WATER)
			return true;
		return false;
	}
	
	public static boolean isFluid(Block block) {
		if (isWater(block) ||
				isLava(block) ||
				block.getType() == Material.AIR) {
			return true;
		}
		return false;
	}
	
	public static boolean isLava(Block block) {
		if (block.getType() == Material.LAVA 
				|| block.getType() == Material.STATIONARY_LAVA) {
			return true;
		}
		return false;
	}
	
	public static boolean isLavaBased(Block block) {
		if (isLava(block) || block.getType() == Material.OBSIDIAN) {
			return true;
		}
		return false;
	}
	
	public static boolean adjacentToAnyWater(Block block) {
		BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH,
				BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN };
		for (BlockFace face : faces) {
			Block blocki = block.getRelative(face);
			if (isWater(blocki))
				return true;
		}
		return false;
	}
	
	public static boolean isWaterbendable(Block block, Player player) {
		byte full = 0x0;
		if (TempBlock.isTempBlock(block))
			return false;
		if ((block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
				&& block.getData() == full)
			return true;
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW)
			return true;
		if (EntityTools.canPlantbend(player) && isPlant(block))
			return true;
		return false;
	}
	
	public static boolean adjacentToThreeOrMoreSources(Block block) {
		if (TempBlock.isTempBlock(block))
			return false;
		int sources = 0;
		byte full = 0x0;
		BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH,
				BlockFace.SOUTH };
		for (BlockFace face : faces) {
			Block blocki = block.getRelative(face);
			if ((blocki.getType() == Material.WATER || blocki.getType() == Material.STATIONARY_WATER)
					&& blocki.getData() == full
					&& WaterManipulation.canPhysicsChange(blocki))
				sources++;
			if (FreezeMelt.isFrozen(blocki)) {
				if (FreezeMelt.isLevel(blocki,full))
					sources++;
			} else if (blocki.getType() == Material.ICE) {
				sources++;
			}
		}
		if (sources >= 2)
			return true;
		return false;
	}
	
	public static boolean adjacentToFrozenBlock(Block block) {
		BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH,
				BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		boolean adjacent = false;
		for (BlockFace face : faces) {
			if (FreezeMelt.isFrozen((block.getRelative(face))))
				adjacent = true;
		}
		return adjacent;
	}
	
	public static List<Block> getBlocksOnPlane(Location location, int radius) {
		List<Block> blocks = new ArrayList<Block>();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				blocks.add(location.getBlock().getRelative(BlockFace.NORTH, x)
						.getRelative(BlockFace.EAST, y));
			}
		}
		return blocks;
	}
	
	public static List<Block> getBlocksAroundPoint(Location location,
			double radius) {

		List<Block> blocks = new ArrayList<Block>();

		int xorg = location.getBlockX();
		int yorg = location.getBlockY();
		int zorg = location.getBlockZ();

		int r = (int) radius + 4;

		for (int x = xorg - r; x <= xorg + r; x++) {
			for (int y = yorg - r; y <= yorg + r; y++) {
				for (int z = zorg - r; z <= zorg + r; z++) {
					Block block = location.getWorld().getBlockAt(x, y, z);
					if (block.getLocation().distance(location) <= radius) {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}
	
	public static void moveEarth(Player player, Location location,
			Vector direction, int chainlength) {
		moveEarth(player, location, direction, chainlength, true);
	}

	public static void moveEarth(Player player, Location location,
			Vector direction, int chainlength, boolean throwplayer) {
		Block block = location.getBlock();
		moveEarth(player, block, direction, chainlength, throwplayer);
	}

	public static void moveEarth(Player player, Block block, Vector direction,
			int chainlength) {
		moveEarth(player, block, direction, chainlength, true);
	}
	
	public static boolean moveEarth(Player player, Block block,
			Vector direction, int chainlength, boolean throwplayer) {
		if (isEarthbendable(player, block)
				&& !Tools.isRegionProtectedFromBuild(player, Abilities.RaiseEarth,
						block.getLocation())) {

			boolean up = false;
			boolean down = false;
			Vector norm = direction.clone().normalize();
			if (norm.dot(new Vector(0, 1, 0)) == 1) {
				up = true;
			} else if (norm.dot(new Vector(0, -1, 0)) == 1) {
				down = true;
			}
			Vector negnorm = norm.clone().multiply(-1);

			Location location = block.getLocation();

			ArrayList<Block> blocks = new ArrayList<Block>();
			for (double j = -2; j <= chainlength; j++) {
				Block checkblock = location.clone()
						.add(negnorm.clone().multiply(j)).getBlock();
				if (!tempnophysics.contains(checkblock)) {
					blocks.add(checkblock);
					tempnophysics.add(checkblock);
				}
			}

			Block affectedblock = location.clone().add(norm).getBlock();
			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}

			if (affectedblock == null)
				return false;
			if (isTransparentToEarthbending(player, affectedblock)) {
				if (throwplayer) {
					for (Entity entity : EntityTools.getEntitiesAroundPoint(
							affectedblock.getLocation(), 1.75)) {
						if (entity instanceof LivingEntity) {
							LivingEntity lentity = (LivingEntity) entity;
							if (lentity.getEyeLocation().getBlockX() == affectedblock
									.getX()
									&& lentity.getEyeLocation().getBlockZ() == affectedblock
											.getZ())
								if (!(entity instanceof FallingBlock))
									entity.setVelocity(norm.clone().multiply(
											.75));
						} else {
							if (entity.getLocation().getBlockX() == affectedblock
									.getX()
									&& entity.getLocation().getBlockZ() == affectedblock
											.getZ())
								if (!(entity instanceof FallingBlock))
									entity.setVelocity(norm.clone().multiply(
											.75));
						}
					}

				}

				if (up) {
					Block topblock = affectedblock.getRelative(BlockFace.UP);
					if (topblock.getType() != Material.AIR) {
						breakBlock(affectedblock);
					} else if (!affectedblock.isLiquid()
							&& affectedblock.getType() != Material.AIR) {
						// affectedblock.setType(Material.GLASS);
						moveEarthBlock(affectedblock, topblock);
					}
				} else {
					breakBlock(affectedblock);
				}

				moveEarthBlock(block, affectedblock);
				block.getWorld().playEffect(block.getLocation(),
						Effect.GHAST_SHOOT, 0, 4);

				for (double i = 1; i < chainlength; i++) {
					affectedblock = location
							.clone()
							.add(negnorm.getX() * i, negnorm.getY() * i,
									negnorm.getZ() * i).getBlock();
					if (!isEarthbendable(player, affectedblock)) {
						// verbose(affectedblock.getType());
						if (down) {
							if (isTransparentToEarthbending(player,
									affectedblock)
									&& !affectedblock.isLiquid()
									&& affectedblock.getType() != Material.AIR) {
								moveEarthBlock(affectedblock, block);
							}
						}
						break;
					}
					if (EarthPassive.isPassiveSand(affectedblock)) {
						EarthPassive.revertSand(affectedblock);
					}
					if (block == null) {
						for (Block checkblock : blocks) {
							tempnophysics.remove(checkblock);
						}
						return false;
					}
					moveEarthBlock(affectedblock, block);
					block = affectedblock;
				}

				int i = chainlength;
				affectedblock = location
						.clone()
						.add(negnorm.getX() * i, negnorm.getY() * i,
								negnorm.getZ() * i).getBlock();
				if (!isEarthbendable(player, affectedblock)) {
					if (down) {
						if (isTransparentToEarthbending(player, affectedblock)
								&& !affectedblock.isLiquid()) {
							moveEarthBlock(affectedblock, block);
						}
					}
				}

			} else {
				for (Block checkblock : blocks) {
					tempnophysics.remove(checkblock);
				}
				return false;
			}
			for (Block checkblock : blocks) {
				tempnophysics.remove(checkblock);
			}
			return true;
		}
		return false;
	}
	
	public static void moveEarthBlock(Block source, Block target) {
		byte full = 0x0;
		Information info;
		if (movedEarth.containsKey(source)) {
			info = movedEarth.get(source);
			info.setTime(System.currentTimeMillis());
			movedEarth.remove(source);
			movedEarth.put(target, info);
		} else {
			info = new Information();
			info.setBlock(source);

			info.setTime(System.currentTimeMillis());
			info.setState(source.getState());
			movedEarth.put(target, info);
		}

		if (adjacentToThreeOrMoreSources(source)) {
			source.setType(Material.WATER);
			source.setData(full);
		} else {
			source.setType(Material.AIR);
		}
		if (info.getState().getType() == Material.SAND) {
			target.setType(Material.SANDSTONE);
		} else {
			target.setType(info.getState().getType());
			target.setData(info.getState().getRawData());
		}
	}
	
	public static Block getEarthSourceBlock(Player player, double range) {
		Block testblock = EntityTools.getTargetBlock(player, (int) range,
				getTransparentEarthbending());
		if (isEarthbendable(player, testblock))
			return testblock;
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			if (Tools.isRegionProtectedFromBuild(player, Abilities.RaiseEarth,
					location))
				continue;
			if (isEarthbendable(player, block)) {
				return block;
			}
		}
		return null;
	}

	public static Block getWaterSourceBlock(Player player, double range,
			boolean plantbending) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			if (Tools.isRegionProtectedFromBuild(player, Abilities.WaterManipulation,
					location))
				continue;
			if (isWaterbendable(block, player)
					&& (!isPlant(block) || plantbending)) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock tb = TempBlock.get(block);
					byte full = 0x0;
					if (tb.getState().getRawData() != full
							&& (tb.getState().getType() != Material.WATER || tb
									.getState().getType() != Material.STATIONARY_WATER)) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}
	
	public static void addTempAirBlock(Block block) {
		if (movedEarth.containsKey(block)) {
			Information info = movedEarth.get(block);
			block.setType(Material.AIR);
			info.setTime(System.currentTimeMillis());
			movedEarth.remove(block);
			tempAir.put(info.getID(), info);
		} else {
			Information info = new Information();
			info.setBlock(block);
			info.setState(block.getState());
			info.setTime(System.currentTimeMillis());
			block.setType(Material.AIR);
			tempAir.put(info.getID(), info);
		}

	}

	public static void revertAirBlock(int i) {
		revertAirBlock(i, false);
	}

	public static void revertAirBlock(int i, boolean force) {
		if (!tempAir.containsKey(i))
			return;
		Information info = tempAir.get(i);
		Block block = info.getState().getBlock();
		if (block.getType() != Material.AIR && !block.isLiquid()) {
			if (force || !movedEarth.containsKey(block)) {
				dropItems(
						block,
						getDrops(block, info.getState().getType(), info
								.getState().getRawData(), pickaxe));
				tempAir.remove(i);
			} else {
				info.setTime(info.getTime() + 10000);
			}
			return;
		} else {
			info.getState().update(true);
			tempAir.remove(i);
		}
	}

	public static boolean revertBlock(Block block) {
		byte full = 0x0;
		if (movedEarth.containsKey(block)) {
			Information info = movedEarth.get(block);
			Block sourceblock = info.getState().getBlock();

			if (info.getState().getType() == Material.AIR) {
				movedEarth.remove(block);
				return true;
			}

			if (block.equals(sourceblock)) {
				info.getState().update(true);
				if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
					EarthColumn.revertBlock(sourceblock);
				if (EarthColumn.blockInAllAffectedBlocks(block))
					EarthColumn.revertBlock(block);
				EarthColumn.resetBlock(sourceblock);
				EarthColumn.resetBlock(block);
				movedEarth.remove(block);
				return true;
			}

			if (movedEarth.containsKey(sourceblock)) {
				addTempAirBlock(block);
				movedEarth.remove(block);
				return true;
			}

			if (sourceblock.getType() == Material.AIR || sourceblock.isLiquid()) {

				info.getState().update(true);
			} else {
				dropItems(
						block,
						getDrops(block, info.getState().getType(), info
								.getState().getRawData(), pickaxe));

			}

			if (adjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData(full);
			} else {
				block.setType(Material.AIR);
			}

			if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
				EarthColumn.revertBlock(sourceblock);
			if (EarthColumn.blockInAllAffectedBlocks(block))
				EarthColumn.revertBlock(block);
			EarthColumn.resetBlock(sourceblock);
			EarthColumn.resetBlock(block);
			movedEarth.remove(block);
		}
		return true;
	}
	
	public static void breakBlock(Block block) {
		block.breakNaturally(new ItemStack(Material.AIR));
	}
	
	public static void removeBlock(Block block) {
		if (adjacentToThreeOrMoreSources(block)) {
			block.setType(Material.WATER);
			block.setData((byte) 0x0);
		} else {
			block.setType(Material.AIR);
		}
	}
	
	public static void removeRevertIndex(Block block) {
		if (movedEarth.containsKey(block)) {
			Information info = movedEarth.get(block);
			if (block.getType() == Material.SANDSTONE
					&& info.getType() == Material.SAND)
				block.setType(Material.SAND);
			if (EarthColumn.blockInAllAffectedBlocks(block))
				EarthColumn.revertBlock(block);
			EarthColumn.resetBlock(block);
			movedEarth.remove(block);
		}
	}

	public static void removeAllEarthbendedBlocks() {
		for (Block block : movedEarth.keySet()) {
			revertBlock(block);
		}

		for (Integer i : tempAir.keySet()) {
			revertAirBlock(i, true);
		}
	}
	
	public static Collection<ItemStack> getDrops(Block block, Material type,
			byte data, ItemStack breakitem) {
		BlockState tempstate = block.getState();
		block.setType(type);
		block.setData(data);
		Collection<ItemStack> item = block.getDrops();
		tempstate.update(true);
		return item;
	}

	public static void dropItems(Block block, Collection<ItemStack> items) {
		for (ItemStack item : items)
			block.getWorld().dropItem(block.getLocation(), item);
	}

	public static boolean isBlockTouching(Block block1, Block block2) {
		BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH,
				BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		block1 = block1.getLocation().getBlock();
		block2 = block2.getLocation().getBlock();
		for (BlockFace face : faces) {
			if (block1.getRelative(face).equals(block2)) {
				return true;
			}
		}
		return false;
	}	
}

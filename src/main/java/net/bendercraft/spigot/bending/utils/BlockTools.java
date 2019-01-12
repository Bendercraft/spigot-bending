package net.bendercraft.spigot.bending.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthPassive;
import net.bendercraft.spigot.bending.abilities.earth.EarthWall;
import net.bendercraft.spigot.bending.abilities.earth.LavaTrain;
import net.bendercraft.spigot.bending.abilities.earth.MetalBending;
import net.bendercraft.spigot.bending.abilities.water.PhaseChange;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.Settings;

public class BlockTools {
	private static List<Block> tempnophysics = new LinkedList<>();
	
	private static final Set<Material> LEAVES = new HashSet<>();
	static {
		LEAVES.add(Material.ACACIA_LEAVES);
		LEAVES.add(Material.BIRCH_LEAVES);
		LEAVES.add(Material.DARK_OAK_LEAVES);
		LEAVES.add(Material.JUNGLE_LEAVES);
		LEAVES.add(Material.OAK_LEAVES);
		LEAVES.add(Material.SPRUCE_LEAVES);
	}

	private static final Set<Material> SAPLINGS = new HashSet<>();
	static {
		SAPLINGS.add(Material.ACACIA_SAPLING);
		SAPLINGS.add(Material.BIRCH_SAPLING);
		SAPLINGS.add(Material.DARK_OAK_SAPLING);
		SAPLINGS.add(Material.JUNGLE_SAPLING);
		SAPLINGS.add(Material.OAK_SAPLING);
	}

	private static final Set<Material> FRUITS = new HashSet<>();
	static {
		FRUITS.add(Material.PUMPKIN);
		FRUITS.add(Material.MELON);
	}

	private static final Set<Material> FLOWERS = new HashSet<>();
	static {
		FLOWERS.add(Material.ROSE_BUSH);
		FLOWERS.add(Material.PEONY);
		FLOWERS.add(Material.LILAC);
		FLOWERS.add(Material.SUNFLOWER);
		FLOWERS.add(Material.DANDELION);
		FLOWERS.add(Material.DANDELION_YELLOW);
		FLOWERS.add(Material.POPPY);
		FLOWERS.add(Material.BLUE_ORCHID);
		FLOWERS.add(Material.ALLIUM);
		FLOWERS.add(Material.AZURE_BLUET);
		FLOWERS.add(Material.RED_TULIP);
		FLOWERS.add(Material.ORANGE_TULIP);
		FLOWERS.add(Material.PINK_TULIP);
		FLOWERS.add(Material.WHITE_TULIP);
		FLOWERS.add(Material.OXEYE_DAISY);
	}
	
	private static Set<Material> PLANTS = new HashSet<>();
	static {
		PLANTS.addAll(LEAVES);

		PLANTS.addAll(SAPLINGS);

		PLANTS.addAll(FRUITS);

		PLANTS.addAll(FLOWERS);

		PLANTS.add(Material.LILY_PAD);
		PLANTS.add(Material.TALL_GRASS);
		PLANTS.add(Material.FERN);
		PLANTS.add(Material.DEAD_BUSH);

		
		PLANTS.add(Material.WHEAT);
		PLANTS.add(Material.CACTUS);
		PLANTS.add(Material.SUGAR_CANE);
		PLANTS.add(Material.BROWN_MUSHROOM);
		PLANTS.add(Material.RED_MUSHROOM);
		PLANTS.add(Material.PUMPKIN_STEM);
		PLANTS.add(Material.MELON_STEM);
		PLANTS.add(Material.VINE);
		PLANTS.add(Material.CARROT);
		PLANTS.add(Material.POTATO);
	}

	private static Set<Material> transparentEarthbending = new HashSet<>();
	static {
		transparentEarthbending.addAll(LEAVES);
		
		transparentEarthbending.add(Material.AIR);
		transparentEarthbending.add(Material.WATER);
		transparentEarthbending.add(Material.LAVA);
		transparentEarthbending.add(Material.COBWEB);
		transparentEarthbending.add(Material.TALL_GRASS);
		transparentEarthbending.add(Material.ROSE_BUSH);
		transparentEarthbending.add(Material.DEAD_BUSH);
		transparentEarthbending.add(Material.DANDELION);
		transparentEarthbending.add(Material.DANDELION_YELLOW);
		transparentEarthbending.add(Material.ROSE_RED);
		transparentEarthbending.add(Material.BROWN_MUSHROOM);
		transparentEarthbending.add(Material.RED_MUSHROOM);
		transparentEarthbending.add(Material.TORCH);
		transparentEarthbending.add(Material.FIRE);
		transparentEarthbending.add(Material.WHEAT);
		transparentEarthbending.add(Material.SNOW);
		transparentEarthbending.add(Material.SUGAR_CANE);
		transparentEarthbending.add(Material.VINE);
	}

	private static final Set<Material> PLATES = new HashSet<>();
	static {
		PLATES.add(Material.STONE_PRESSURE_PLATE);
		PLATES.add(Material.ACACIA_PRESSURE_PLATE);
		PLATES.add(Material.BIRCH_PRESSURE_PLATE);
		PLATES.add(Material.DARK_OAK_PRESSURE_PLATE);
		PLATES.add(Material.JUNGLE_PRESSURE_PLATE);
		PLATES.add(Material.OAK_PRESSURE_PLATE);
		PLATES.add(Material.SPRUCE_PRESSURE_PLATE);
	}

	private static Set<Material> nonOpaque = new HashSet<>();
	static {
		nonOpaque.addAll(LEAVES);

		nonOpaque.addAll(PLATES);

		nonOpaque.add(Material.VOID_AIR);
		nonOpaque.add(Material.AIR);
		nonOpaque.add(Material.WATER);
		nonOpaque.add(Material.LAVA);
		nonOpaque.add(Material.POWERED_RAIL);
		nonOpaque.add(Material.DETECTOR_RAIL);
		nonOpaque.add(Material.COBWEB);
		nonOpaque.add(Material.TALL_GRASS); // Not sure here, previously ID 31
		nonOpaque.add(Material.ROSE_BUSH);
		nonOpaque.add(Material.DEAD_BUSH);
		nonOpaque.add(Material.DANDELION);
		nonOpaque.add(Material.DANDELION_YELLOW);
		nonOpaque.add(Material.ROSE_RED);
		nonOpaque.add(Material.POPPY);
		nonOpaque.add(Material.BROWN_MUSHROOM);
		nonOpaque.add(Material.RED_MUSHROOM);
		nonOpaque.add(Material.TORCH);
		nonOpaque.add(Material.FIRE);
		nonOpaque.add(Material.REDSTONE_WIRE);
		nonOpaque.add(Material.WHEAT);
		nonOpaque.add(Material.RAIL);
		nonOpaque.add(Material.WALL_SIGN);
		nonOpaque.add(Material.LEVER);

		nonOpaque.add(Material.REDSTONE_TORCH);
		nonOpaque.add(Material.STONE_BUTTON);
		nonOpaque.add(Material.SNOW);
		nonOpaque.add(Material.SUGAR_CANE);
		nonOpaque.add(Material.NETHER_PORTAL);
		nonOpaque.add(Material.END_PORTAL);
		nonOpaque.add(Material.REPEATER);
		nonOpaque.add(Material.PUMPKIN_STEM);
		nonOpaque.add(Material.MELON_STEM);
		nonOpaque.add(Material.VINE);
		nonOpaque.add(Material.LILY_PAD);
		nonOpaque.add(Material.NETHER_WART);
		nonOpaque.add(Material.COCOA);
		nonOpaque.add(Material.TRIPWIRE_HOOK);
		nonOpaque.add(Material.TRIPWIRE);

	}

	private static final Set<Material> IRON_BENDABLES = new HashSet<>();
	static {
		IRON_BENDABLES.add(Material.IRON_BLOCK);
		IRON_BENDABLES.add(Material.IRON_ORE);
		IRON_BENDABLES.add(Material.ANVIL);
		IRON_BENDABLES.add(Material.IRON_BARS);
		IRON_BENDABLES.add(Material.HOPPER);
		IRON_BENDABLES.add(Material.CAULDRON);
	}
	
	private BlockTools() {
	}

	public static Set<Material> getTransparentEarthbending() {
		return new HashSet<>(transparentEarthbending);
	}

	public static boolean isTransparentToEarthbending(Player player, Block block) {
		return isTransparentToEarthbending(player, AbilityManager.getManager().getRegisteredAbility(EarthWall.NAME), block);
	}

	public static boolean isTransparentToEarthbending(Player player, RegisteredAbility ability, Block block) {
		if (ProtectionManager.isLocationProtectedFromBending(player, ability, block.getLocation())) {
			return false;
		}

		if (transparentEarthbending.contains(block.getType())) {
			return true;
		}

		return false;
	}

	public static boolean isObstructed(Location location1, Location location2) {
		// Only used in octopus form
		Vector loc1 = location1.toVector();
		Vector loc2 = location2.toVector();

		Vector direction = loc2.subtract(loc1);
		direction.normalize();

		Location loc;

		double max = location1.distance(location2);

		for (double i = 0; i <= max; i++) {
			loc = location1.clone().add(direction.clone().multiply(i));
			Material type = loc.getBlock().getType();
			if ((type != Material.AIR) && !transparentEarthbending.contains(type)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSolid(Block block) {
		if (nonOpaque.contains(block.getType())) {
			return false;
		}
		return true;
	}

	public static boolean isMeltable(Block block) {
		if ((block.getType() == Material.ICE) || (block.getType() == Material.SNOW)) {
			return true;
		}
		return false;
	}

	public static boolean isEarthbendable(Player player, Block block) {
		return isEarthbendable(player, AbilityManager.getManager().getRegisteredAbility(EarthWall.NAME), block);
	}

	@SuppressWarnings("deprecation")
	public static boolean isEarthbendable(Player player, RegisteredAbility ability, Block block) {
		if (ProtectionManager.isLocationProtectedFromBending(player, ability, block.getLocation())) {
			return false;
		}

		Material material = block.getType();

		if (material == Material.STONE) {
			byte data = block.getData();
			if (data == 0x2 || data == 0x4 || data == 0x6) {
				return false;
			}
		}

		for (String s : Settings.getEarthBendablesBlocksNames()) {
			if (material == Material.getMaterial(s)) {
				return true;
			}
		}

		if (isIronBendable(player, block.getType())) {
			return true;
		}

		if (EntityTools.isSpecialized(player, BendingAffinity.LAVA) 
				&& (block.getType() == Material.OBSIDIAN) 
				&& ((EarthWall.NAME.equals(ability.getName())) || (LavaTrain.NAME.equals(ability.getName())))) {
			return true;
		}

		return false;
	}

	public static boolean isIronBendable(Player p, Material m) {
		if (!EntityTools.canBend(p, MetalBending.NAME)) {
			return false;
		}

		if (!IRON_BENDABLES.contains(m)) {
			return false;
		}

		return true;
	}

	public static int getEarthbendableBlocksLength(Player player, Block block, Vector direction, int maxlength) {
		Location location = block.getLocation();
		direction = direction.normalize();
		double j;
		for (int i = 0; i <= maxlength; i++) {
			j = i;
			if (!isEarthbendable(player, location.clone().add(direction.clone().multiply(j)).getBlock())) {
				return i;
			}
		}
		return maxlength;
	}

	public static boolean isFruit(Block block) {
		if (FRUITS.contains(block.getType())) {
			return true;
		}
		return false;
	}

	public static boolean isFlower(Block block) {
		if (FLOWERS.contains(block.getType())) {
			return true;
		}
		return false;
	}
	
	public static boolean isLeaf(Block block) {
		if (LEAVES.contains(block.getType())) {
			return true;
		}
		return false;
	}

	public static boolean isPlant(Block block) {
		if (PLANTS.contains(block.getType())) {
			return true;
		}
		return false;
	}
	
	public static boolean isWaterBased(Block block) {
		if (block.getType() == Material.WATER || (block.getType() == Material.ICE) || (block.getType() == Material.SNOW_BLOCK) || (block.getType() == Material.SNOW)) {
			return true;
		}
		return false;

	}

	public static boolean isFluid(Block block) {
		if (block == null) {
			return false;
		}

		if (block.getType() == Material.WATER || block.getType() == Material.LAVA || (block.getType() == Material.AIR)) {
			return true;
		}
		return false;
	}

	public static boolean isLavaBased(Block block) {
		if (block.getType() == Material.LAVA || (block.getType() == Material.OBSIDIAN)) {
			return true;
		}
		return false;
	}

	public static boolean isWaterbendable(Block block, Player player) {
		if (TempBlock.isTempBlock(block) && !TempBlock.get(block).isBendAllowed()) {
			return false;
		}

		if (isWaterBased(block) && !block.getType().equals(Material.SNOW_BLOCK)) {
			return true;
		}

		if (EntityTools.canPlantbend(player) && isPlant(block)) {
			return true;
		}

		return false;
	}

	public static boolean adjacentToThreeOrMoreSources(Block block) {
		if (TempBlock.isTempBlock(block)) {
			return false;
		}

		int sources = 0;
		BlockFace[] faces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
		for (BlockFace face : faces) {
			Block blocki = block.getRelative(face);
			if(!(blocki.getBlockData() instanceof Levelled)) {
				continue;
			}
			Levelled data = (Levelled) blocki.getBlockData();
			if ((blocki.getType() == Material.WATER) 
					&& data.getLevel() == data.getMaximumLevel()
					&& !TempBlock.isTempBlock(blocki)
					&& ! TempBlock.isTouchingTempBlock(blocki)) {
				sources++;
			}
			if (PhaseChange.isFrozen(blocki)) {
				if (data.getLevel() == data.getMaximumLevel()) {
					sources++;
				}
			} else if (blocki.getType() == Material.ICE) {
				sources++;
			}
		}
		if (sources >= 2) {
			return true;
		}
		return false;
	}

	public static boolean adjacentToFrozenBlock(Block block) {
		final BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		boolean adjacent = false;
		for (BlockFace face : faces) {
			if (PhaseChange.isFrozen(block.getRelative(face))) {
				adjacent = true;
			}
		}
		return adjacent;
	}

	public static List<Block> getBlocksOnPlane(Location location, int radius) {
		List<Block> blocks = new LinkedList<>();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				blocks.add(location.getBlock().getRelative(BlockFace.NORTH, x).getRelative(BlockFace.EAST, y));
			}
		}
		return blocks;
	}

	public static List<Location> getLocationBetweenRanges(final Location center, double minRadius, double maxRadius) {
		final List<Location> blocks = new LinkedList<>();

		final World world = center.getWorld();
		final int r = (int) maxRadius + 4;

		int xorg = center.getBlockX();
		int yorg = center.getBlockY();
		int zorg = center.getBlockZ();

		for (int x = xorg - r; x <= (xorg + r); x++) {
			for (int y = yorg - r; y <= (yorg + r); y++) {
				for (int z = zorg - r; z <= (zorg + r); z++) {
					Block block = world.getBlockAt(x, y, z);
					Location location = block.getLocation();
					final double distance = location.distance(center);
					if (minRadius <= distance && distance <= maxRadius) {
						blocks.add(location);
					}
				}
			}
		}
		return blocks;
	}

	public static List<Block> getBlocksAroundPoint(final Location location, final double radius) {
		return getBlocksAroundPoint(location, radius, (block) -> true);
	}

	public static List<Block> getBlocksAroundPoint(final Location location, double radius, final Predicate<Block> filter) {
		List<Block> blocks = new LinkedList<>();

		final int xorg = location.getBlockX();
		final int yorg = location.getBlockY();
		final int zorg = location.getBlockZ();

		final int r = (int) radius + 2;

		for (int x = xorg - r; x <= (xorg + r); x++) {
			for (int y = yorg - r; y <= (yorg + r); y++) {
				for (int z = zorg - r; z <= (zorg + r); z++) {
					Block block = location.getWorld().getBlockAt(x, y, z);
					if (block.getLocation().distance(location) <= radius) {
						if (filter.test(block)) {
							blocks.add(block);
						}
					}
				}
			}
		}

		return blocks;
	}

	public static void moveEarth(Player player, Location location, Vector direction, int chainlength) {
		moveEarth(player, location, direction, chainlength, true);
	}

	public static void moveEarth(Player player, Location location, Vector direction, int chainlength, boolean throwplayer) {
		Block block = location.getBlock();
		moveEarth(player, block, direction, chainlength, throwplayer);
	}

	public static void moveEarth(Player player, Block block, Vector direction, int chainlength) {
		moveEarth(player, block, direction, chainlength, true);
	}

	public static boolean moveEarth(Player player, Block block, Vector direction, int chainLength, boolean throwplayer) {
		if (isEarthbendable(player, block) 
				&& !ProtectionManager.isLocationProtectedFromBending(player, AbilityManager.getManager().getRegisteredAbility(EarthWall.NAME), block.getLocation())) {
			boolean up = false;
			boolean down = false;
			Vector norm = direction.clone().normalize();
			if (MathUtils.doubleEquals(norm.dot(new Vector(0, 1, 0)), 1)) {
				up = true;
			} else if (MathUtils.doubleEquals(norm.dot(new Vector(0, -1, 0)), 1)) {
				down = true;
			}
			Vector negnorm = norm.clone().multiply(-1);

			Location location = block.getLocation();

			List<Block> blocks = new LinkedList<>();
			for (double j = -2; j <= chainLength; j++) {
				Block checkblock = location.clone().add(negnorm.clone().multiply(j)).getBlock();
				if (!tempnophysics.contains(checkblock)) {
					blocks.add(checkblock);
					tempnophysics.add(checkblock);
				}
			}

			Block affectedblock = location.clone().add(norm).getBlock();
			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}

			if (affectedblock == null) {
				return false;
			}
			if (isTransparentToEarthbending(player, affectedblock)) {
				if (throwplayer) {
					for (Entity entity : EntityTools.getEntitiesAroundPoint(affectedblock.getLocation(), 1.75)) {
						if (entity instanceof LivingEntity) {
							LivingEntity lentity = (LivingEntity) entity;
							if (lentity.getEyeLocation().getBlockX() == affectedblock.getX() 
									&& lentity.getEyeLocation().getBlockZ() == affectedblock.getZ() 
									&& !(entity instanceof FallingBlock)) {
								entity.setVelocity(norm.clone().multiply(.75));
							}
						} else {
							if (entity.getLocation().getBlockX() == affectedblock.getX() 
									&& entity.getLocation().getBlockZ() == affectedblock.getZ() 
									&& !(entity instanceof FallingBlock)) {
								entity.setVelocity(norm.clone().multiply(.75));
							}
						}
					}
				}

				if (up) {
					Block topblock = affectedblock.getRelative(BlockFace.UP);
					if (topblock.getType() != Material.AIR) {
						breakBlock(affectedblock);
					} else if (!affectedblock.isLiquid() && (affectedblock.getType() != Material.AIR)) {
						moveEarthBlock(affectedblock, topblock);
					}
				} else {
					breakBlock(affectedblock);
				}

				moveEarthBlock(block, affectedblock);
				block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);

				for (double i = 1; i < chainLength; i++) {
					affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
					if (!isEarthbendable(player, affectedblock)) {
						// verbose(affectedblock.getType());
						if (down) {
							if (isTransparentToEarthbending(player, affectedblock) && !affectedblock.isLiquid() && (affectedblock.getType() != Material.AIR)) {
								moveEarthBlock(affectedblock, block);
							}
						}
						break;
					}
					if (EarthPassive.isPassiveSand(affectedblock)) {
						EarthPassive.revertSand(affectedblock);
					}
					if (block == null) {
						for (Block checkBlock : blocks) {
							tempnophysics.remove(checkBlock);
						}
						return false;
					}
					moveEarthBlock(affectedblock, block);
					block = affectedblock;
				}

				affectedblock = location.clone().add(negnorm.getX() * chainLength, negnorm.getY() * chainLength, negnorm.getZ() * chainLength).getBlock();
				if (!isEarthbendable(player, affectedblock) 
						&& down 
						&& isTransparentToEarthbending(player, affectedblock) 
						&& !affectedblock.isLiquid()) {
					moveEarthBlock(affectedblock, block);
				}

			} else {
				for (Block checkBlock : blocks) {
					tempnophysics.remove(checkBlock);
				}
				return false;
			}
			for (Block checkBlock : blocks) {
				tempnophysics.remove(checkBlock);
			}
			return true;
		}
		return false;
	}
	
	public static boolean isTempNoPhysics(Block block) {
		return tempnophysics.contains(block);
	}

	public static void moveEarthBlock(Block source, Block target) {
		if (target.getType() == Material.SAND) {
			TempBlock.makeGlobal(Settings.REVERSE_TIME, target, Material.SANDSTONE, source.getBlockData(), true);
		} else {
			TempBlock.makeGlobal(Settings.REVERSE_TIME, target, source.getType(), source.getBlockData(), true);
		}
		
		if (adjacentToThreeOrMoreSources(source)) {
			TempBlock.makeGlobal(Settings.REVERSE_TIME, source, Material.WATER, false);
		} else {
			TempBlock.makeGlobal(Settings.REVERSE_TIME, source, Material.AIR, false);
		}
	}

	public static Block getEarthSourceBlock(Player player, RegisteredAbility ability, double range) {
		Block testblock = EntityTools.getTargetBlock(player, (int) range, getTransparentEarthbending());
		if (isEarthbendable(player, ability, testblock)) {
			return testblock;
		}
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (ProtectionManager.isLocationProtectedFromBending(player, ability, location)) {
				continue;
			}
			if (isEarthbendable(player, ability, block)) {
				return block;
			}
		}
		return null;
	}

	public static Block getWaterSourceBlock(Player player, double range, boolean plantbending) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (ProtectionManager.isLocationProtectedFromBending(player, AbilityManager.getManager().getRegisteredAbility(WaterManipulation.NAME), location)) {
				continue;
			}

			if (isWaterbendable(block, player) && (!isPlant(block) || plantbending)) {
				TempBlock tempBlock = TempBlock.get(block);
				if (tempBlock != null && !tempBlock.isBendAllowed()) {
					return null;
				}
				return block;
			}
		}
		return null;
	}

	public static void addTempAirBlock(Block block) {
		TempBlock.makeGlobal(Settings.REVERSE_TIME, block, Material.AIR, true);
	}
	
	public static void breakBlock(Block block) {
		block.breakNaturally(new ItemStack(Material.AIR));
	}

	public static void dropItems(Block block, Collection<ItemStack> items) {
		for (ItemStack item : items) {
			block.getWorld().dropItem(block.getLocation(), item);
		}
	}

	public static boolean isBlockTouching(Block block1, Block block2) {
		BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		for (BlockFace face : faces) {
			if (block1.getRelative(face).equals(block2)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean blockEquals(Block a, Block b) {
		if(a.getX() == b.getX() 
				&& a.getY() == b.getY() 
				&& a.getZ() == b.getZ()
				&& a.getWorld().getUID().equals(b.getWorld().getUID())) {
			return true;
		}
		return false;
	}

	public static boolean locationEquals(Location la, Location lb) {
		// Difference with the class method : Do not bother the pitch and the
		// yaw

		if (MathUtils.doubleEquals(la.getX(), lb.getX()) 
				&& MathUtils.doubleEquals(la.getY(), lb.getY()) 
				&& MathUtils.doubleEquals(la.getZ(), lb.getZ())) {
			return true;
		}
		return false;
	}
	
	public static Set<Material> getTransparentEarthBending() {
		return transparentEarthbending;
	}
	
	public static Set<Material> getNonOpaque() {
		return nonOpaque;
	}
}

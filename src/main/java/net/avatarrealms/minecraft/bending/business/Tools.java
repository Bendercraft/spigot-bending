package net.avatarrealms.minecraft.bending.business;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.data.BendingPlayers;
import net.avatarrealms.minecraft.bending.data.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.Information;
import net.avatarrealms.minecraft.bending.model.TempBlock;
import net.avatarrealms.minecraft.bending.model.air.AirBlast;
import net.avatarrealms.minecraft.bending.model.air.AirBubble;
import net.avatarrealms.minecraft.bending.model.air.AirBurst;
import net.avatarrealms.minecraft.bending.model.air.AirScooter;
import net.avatarrealms.minecraft.bending.model.air.AirShield;
import net.avatarrealms.minecraft.bending.model.air.AirSpout;
import net.avatarrealms.minecraft.bending.model.air.AirSuction;
import net.avatarrealms.minecraft.bending.model.air.AirSwipe;
import net.avatarrealms.minecraft.bending.model.air.Speed;
import net.avatarrealms.minecraft.bending.model.air.Tornado;
import net.avatarrealms.minecraft.bending.model.chi.Paralyze;
import net.avatarrealms.minecraft.bending.model.chi.RapidPunch;
import net.avatarrealms.minecraft.bending.model.earth.Catapult;
import net.avatarrealms.minecraft.bending.model.earth.CompactColumn;
import net.avatarrealms.minecraft.bending.model.earth.EarthArmor;
import net.avatarrealms.minecraft.bending.model.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.model.earth.EarthColumn;
import net.avatarrealms.minecraft.bending.model.earth.EarthPassive;
import net.avatarrealms.minecraft.bending.model.earth.EarthTunnel;
import net.avatarrealms.minecraft.bending.model.earth.Shockwave;
import net.avatarrealms.minecraft.bending.model.earth.Tremorsense;
import net.avatarrealms.minecraft.bending.model.fire.Cook;
import net.avatarrealms.minecraft.bending.model.fire.FireBlast;
import net.avatarrealms.minecraft.bending.model.fire.FireBurst;
import net.avatarrealms.minecraft.bending.model.fire.FireJet;
import net.avatarrealms.minecraft.bending.model.fire.FireShield;
import net.avatarrealms.minecraft.bending.model.fire.FireStream;
import net.avatarrealms.minecraft.bending.model.fire.Fireball;
import net.avatarrealms.minecraft.bending.model.fire.Illumination;
import net.avatarrealms.minecraft.bending.model.fire.Lightning;
import net.avatarrealms.minecraft.bending.model.fire.WallOfFire;
import net.avatarrealms.minecraft.bending.model.water.Bloodbending;
import net.avatarrealms.minecraft.bending.model.water.FreezeMelt;
import net.avatarrealms.minecraft.bending.model.water.IceSpike;
import net.avatarrealms.minecraft.bending.model.water.IceSpike2;
import net.avatarrealms.minecraft.bending.model.water.OctopusForm;
import net.avatarrealms.minecraft.bending.model.water.Plantbending;
import net.avatarrealms.minecraft.bending.model.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.model.water.WaterReturn;
import net.avatarrealms.minecraft.bending.model.water.WaterSpout;
import net.avatarrealms.minecraft.bending.model.water.WaterWall;
import net.avatarrealms.minecraft.bending.model.water.Wave;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.massivecraft.factions.listeners.FactionsListenerMain;
import com.massivecraft.mcore.ps.PS;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class Tools {

	public static BendingPlayers config;

	private static final ItemStack pickaxe = new ItemStack(
			Material.DIAMOND_PICKAXE);

	private static final Map<String, ChatColor> colors;

	private static Set<Abilities> harmlessAbilities = new HashSet<Abilities>();
	static { 
		harmlessAbilities.add(Abilities.AirScooter);
		harmlessAbilities.add(Abilities.AirSpout);
		harmlessAbilities.add(Abilities.HealingWaters);
		harmlessAbilities.add(Abilities.HighJump);
		harmlessAbilities.add(Abilities.Illumination);
		harmlessAbilities.add(Abilities.Tremorsense);
		harmlessAbilities.add(Abilities.WaterSpout);
	}

	private static Set<Abilities> localAbilities = new HashSet<Abilities>();
	static { 
		localAbilities.add(Abilities.AirScooter);
		localAbilities.add(Abilities.AirSpout);
		localAbilities.add(Abilities.HealingWaters);
		localAbilities.add(Abilities.HighJump);
		localAbilities.add(Abilities.Illumination);
		localAbilities.add(Abilities.Tremorsense);
		localAbilities.add(Abilities.WaterSpout);
		localAbilities.add(Abilities.AvatarState);
		localAbilities.add(Abilities.FireJet);
		localAbilities.add(Abilities.Paralyze);
		localAbilities.add(Abilities.RapidPunch);
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
		transparentEarthbending.add(Material.GRASS); // Not sure here, previously ID 31
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
		nonOpaque.add(Material.GRASS); // Not sure here, previously ID 31
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

	private static Set<Material> plantIds = new HashSet<Material>();
	static {
		plantIds.add(Material.SAPLING);
		plantIds.add(Material.LEAVES);
		plantIds.add(Material.GRASS); // Not sure here ID 31
		plantIds.add(Material.DEAD_BUSH);
		plantIds.add(Material.YELLOW_FLOWER);
		plantIds.add(Material.RED_ROSE);
		plantIds.add(Material.BROWN_MUSHROOM);
		plantIds.add(Material.RED_MUSHROOM);
		plantIds.add(Material.CROPS);
		plantIds.add(Material.CACTUS);
		plantIds.add(Material.SUGAR_CANE);
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

	public static final long timeinterval = ConfigManager.globalCooldown;

	public static ConcurrentHashMap<Block, Information> movedearth = new ConcurrentHashMap<Block, Information>();
	public static ConcurrentHashMap<Integer, Information> tempair = new ConcurrentHashMap<Integer, Information>();
	public static ConcurrentHashMap<Player, Long> blockedchis = new ConcurrentHashMap<Player, Long>();
	public static ConcurrentHashMap<Player, Player> tempflyers = new ConcurrentHashMap<Player, Player>();
	public static List<Player> toggledBending = new ArrayList<Player>();

	public static ArrayList<Block> tempnophysics = new ArrayList<Block>();

	private static boolean allowharmless = true;
	private static boolean respectWorldGuard = true;
	private static boolean respectFactions = false;

	public Tools(BendingPlayers config2) {
		config = config2;
	}

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

	public static List<Entity> getEntitiesAroundPoint(Location location,
			double radius) {

		List<Entity> entities = location.getWorld().getEntities();
		List<Entity> list = location.getWorld().getEntities();

		for (Entity entity : entities) {
			if (entity.getWorld() != location.getWorld()) {
				list.remove(entity);
			} else if (entity.getLocation().distance(location) > radius) {
				list.remove(entity);
			}
		}

		return list;

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

	public static Block getTargetBlock(Player player, double range) {
		return getTargetBlock(player, range,
				Collections.singleton(Material.AIR));
	}

	public static Block getTargetBlock(Player player,
			double originselectrange, Set<Material> nonOpaque2) {
		BlockIterator iter = new BlockIterator(player,
				(int) originselectrange + 1);
		Block block = iter.next();
		while (iter.hasNext()) {
			block = iter.next();
			if (nonOpaque2.contains(block.getType())) {
				continue;
			}
			break;
		}
		return block;
	}

	public static Location getTargetedLocation(Player player, double range) {
		return getTargetedLocation(player, range,
				Collections.singleton(Material.AIR));
	}

	public static Location getTargetedLocation(Player player,
			double originselectrange, Set<Material> nonOpaque2) {
		Location origin = player.getEyeLocation();
		Vector direction = origin.getDirection();

		BlockIterator iter = new BlockIterator(player,
				(int) originselectrange + 1);
		Block block = iter.next();
		while (iter.hasNext()) {
			block = iter.next();
			if (nonOpaque2.contains(block.getType())) {
				break;
			}
		}
		double distance = block.getLocation().distance(origin) - 1.5;
		Location location = origin.add(direction.multiply(distance));

		return location;
	}

	public static List<Block> getBlocksAroundPoint(Location location,
			double radius) {

		List<Block> blocks = new ArrayList<Block>();

		int xorg = location.getBlockX();
		int yorg = location.getBlockY();
		int zorg = location.getBlockZ();

		int r = (int) radius + 4;

		// Block originblock = location.getBlock();

		for (int x = xorg - r; x <= xorg + r; x++) {
			for (int y = yorg - r; y <= yorg + r; y++) {
				for (int z = zorg - r; z <= zorg + r; z++) {
					Block block = location.getWorld().getBlockAt(x, y, z);
					// if
					// (block.getLocation().distance(originblock.getLocation())
					// <= radius) {
					if (block.getLocation().distance(location) <= radius) {
						blocks.add(block);
					}
				}
			}
		}

		return blocks;

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
				&& !isRegionProtectedFromBuild(player, Abilities.RaiseEarth,
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
			// if (block.getType() == Material.SAND) {
			// block.setType(Material.SANDSTONE);
			// }

			if (affectedblock == null)
				return false;
			if (isTransparentToEarthbending(player, affectedblock)) {
				if (throwplayer) {
					for (Entity entity : getEntitiesAroundPoint(
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

				// affectedblock.setType(block.getType());
				// affectedblock.setData(block.getData());
				//
				// addTempEarthBlock(block, affectedblock);
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
						// if (!Tools.adjacentToThreeOrMoreSources(block)
						// && Tools.isWater(block)) {
						// block.setType(Material.AIR);
						// } else {
						// byte full = 0x0;
						// block.setType(Material.WATER);
						// block.setData(full);
						// }
						break;
					}
					if (EarthPassive.isPassiveSand(affectedblock)) {
						EarthPassive.revertSand(affectedblock);
					}
					// if (affectedblock.getType() == Material.SAND) {
					// affectedblock.setType(Material.SANDSTONE);
					// }
					if (block == null) {
						for (Block checkblock : blocks) {
							tempnophysics.remove(checkblock);
						}
						return false;
					}
					// block.setType(affectedblock.getType());
					// block.setData(affectedblock.getData());
					// addTempEarthBlock(affectedblock, block);
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
		if (movedearth.containsKey(source)) {
			// verbose("Moving something already moved.");
			info = movedearth.get(source);
			info.setTime(System.currentTimeMillis());
			movedearth.remove(source);
			movedearth.put(target, info);
		} else {
			// verbose("Moving something for the first time.");
			info = new Information();
			info.setBlock(source);
			// info.setType(source.getType());
			// info.setData(source.getData());
			info.setTime(System.currentTimeMillis());
			info.setState(source.getState());
			movedearth.put(target, info);
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

	public static void addTempAirBlock(Block block) {
		if (movedearth.containsKey(block)) {
			Information info = movedearth.get(block);
			block.setType(Material.AIR);
			info.setTime(System.currentTimeMillis());
			movedearth.remove(block);
			tempair.put(info.getID(), info);
		} else {
			Information info = new Information();
			info.setBlock(block);
			// info.setType(block.getType());
			// info.setData(block.getData());
			info.setState(block.getState());
			info.setTime(System.currentTimeMillis());
			block.setType(Material.AIR);
			tempair.put(info.getID(), info);
		}

	}

	public static void revertAirBlock(int i) {
		revertAirBlock(i, false);
	}

	public static void revertAirBlock(int i, boolean force) {
		if (!tempair.containsKey(i))
			return;
		Information info = tempair.get(i);
		Block block = info.getState().getBlock();
		if (block.getType() != Material.AIR && !block.isLiquid()) {
			if (force || !movedearth.containsKey(block)) {
				dropItems(
						block,
						getDrops(block, info.getState().getType(), info
								.getState().getRawData(), pickaxe));
				// ItemStack item = new ItemStack(info.getType());
				// item.setData(new MaterialData(info.getType(),
				// info.getData()));
				// block.getWorld().dropItem(block.getLocation(), item);
				tempair.remove(i);
			} else {
				info.setTime(info.getTime() + 10000);
			}
			return;
		} else {
			// block.setType(info.getType());
			// block.setData(info.getData());
			info.getState().update(true);
			tempair.remove(i);
		}
	}

	// public static boolean revertBlock(Block block) {
	// return revertBlock(block, true);
	// }

	public static boolean revertBlock(Block block) {
		byte full = 0x0;
		if (movedearth.containsKey(block)) {
			Information info = movedearth.get(block);
			Block sourceblock = info.getState().getBlock();

			if (info.getState().getType() == Material.AIR) {
				movedearth.remove(block);
				return true;
			}

			if (block.equals(sourceblock)) {
				// verbose("Equals!");
				// if (block.getType() == Material.SANDSTONE
				// && info.getState().getType() == Material.SAND)
				// block.setType(Material.SAND);
				info.getState().update(true);
				if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
					EarthColumn.revertBlock(sourceblock);
				if (EarthColumn.blockInAllAffectedBlocks(block))
					EarthColumn.revertBlock(block);
				EarthColumn.resetBlock(sourceblock);
				EarthColumn.resetBlock(block);
				movedearth.remove(block);
				return true;
			}

			if (movedearth.containsKey(sourceblock)) {
				addTempAirBlock(block);
				movedearth.remove(block);
				return true;
				// verbose("Block: " + block);
				// verbose("Sourceblock: " + sourceblock);
				// verbose("StartBlock: " + startblock);
				// if (startblock != null) {
				// if (startblock.equals(sourceblock)) {
				// sourceblock.setType(info.getType());
				// sourceblock.setData(info.getData());
				// if (adjacentToThreeOrMoreSources(block)) {
				// block.setType(Material.WATER);
				// block.setData(full);
				// } else {
				// block.setType(Material.AIR);
				// }
				// movedearth.get(startblock).setInteger(10);
				// if (EarthColumn
				// .blockInAllAffectedBlocks(sourceblock))
				// EarthColumn.revertBlock(sourceblock);
				// if (EarthColumn.blockInAllAffectedBlocks(block))
				// EarthColumn.revertBlock(block);
				// EarthColumn.resetBlock(sourceblock);
				// EarthColumn.resetBlock(block);
				// movedearth.remove(block);
				// return true;
				// }
				//
				// } else {
				// startblock = block;
				// }
				// revertBlock(sourceblock, startblock, true);
			}

			if (sourceblock.getType() == Material.AIR || sourceblock.isLiquid()) {
				// sourceblock.setType(info.getType());
				// sourceblock.setData(info.getData());
				info.getState().update(true);
			} else {
				// if (info.getType() != Material.AIR) {
				// ItemStack item = new ItemStack(info.getType());
				// item.setData(new MaterialData(info.getType(), info
				// .getData()));
				// block.getWorld().dropItem(block.getLocation(), item);
				dropItems(
						block,
						getDrops(block, info.getState().getType(), info
								.getState().getRawData(), pickaxe));
				// }
			}

			// if (info.getInteger() != 10) {
			if (adjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData(full);
			} else {
				block.setType(Material.AIR);
			}
			// }

			if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
				EarthColumn.revertBlock(sourceblock);
			if (EarthColumn.blockInAllAffectedBlocks(block))
				EarthColumn.revertBlock(block);
			EarthColumn.resetBlock(sourceblock);
			EarthColumn.resetBlock(block);
			movedearth.remove(block);
		}
		return true;
	}

	public static void removeRevertIndex(Block block) {
		if (movedearth.containsKey(block)) {
			Information info = movedearth.get(block);
			if (block.getType() == Material.SANDSTONE
					&& info.getType() == Material.SAND)
				block.setType(Material.SAND);
			if (EarthColumn.blockInAllAffectedBlocks(block))
				EarthColumn.revertBlock(block);
			EarthColumn.resetBlock(block);
			movedearth.remove(block);
		}
	}

	public static void removeAllEarthbendedBlocks() {
		for (Block block : movedearth.keySet()) {
			// block.setType(Material.GLASS);
			// movedearth.remove(block);
			// removeEarthbendedBlockByIndex(block);
			revertBlock(block);
		}

		for (Integer i : tempair.keySet()) {
			revertAirBlock(i, true);
		}
	}

	public static Collection<ItemStack> getDrops(Block block, Material type,
			byte data, ItemStack breakitem) {
		BlockState tempstate = block.getState();
		// byte olddata = block.getData();
		// Material oldtype = block.getType();
		block.setType(type);
		block.setData(data);
		// Collection<ItemStack> item = block.getDrops(breakitem);
		Collection<ItemStack> item = block.getDrops();
		// block.setType(oldtype);
		// block.setData(olddata);
		tempstate.update(true);
		return item;
	}

	public static void dropItems(Block block, Collection<ItemStack> items) {
		for (ItemStack item : items)
			block.getWorld().dropItem(block.getLocation(), item);
	}

	public static boolean isWater(Block block) {
		if (block.getType() == Material.WATER
				|| block.getType() == Material.STATIONARY_WATER)
			return true;
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

	public static boolean isEarthbendable(Player player, Block block) {

		return isEarthbendable(player, Abilities.RaiseEarth, block);

	}

	public static boolean isEarthbendable(Player player, Abilities ability,
			Block block) {
		if (Tools.isRegionProtectedFromBuild(player, ability,
				block.getLocation()))
			return false;
		Material material = block.getType();

		// if ((material == Material.STONE) || (material == Material.CLAY)
		// || (material == Material.COAL_ORE)
		// || (material == Material.DIAMOND_ORE)
		// || (material == Material.DIRT)
		// || (material == Material.GOLD_ORE)
		// || (material == Material.GRASS)
		// || (material == Material.GRAVEL)
		// || (material == Material.IRON_ORE)
		// || (material == Material.LAPIS_ORE)
		// || (material == Material.NETHERRACK)
		// || (material == Material.REDSTONE_ORE)
		// || (material == Material.SAND)
		// || (material == Material.SANDSTONE)) {
		// return true;
		// }
		for (String s : ConfigManager.earthbendable) {

			if (material == Material.getMaterial(s)) {

				return true;

			}

		}
		return false;
	}

	public static boolean isWeapon(Material mat) {

		if (mat == Material.WOOD_AXE || mat == Material.WOOD_PICKAXE
				|| mat == Material.WOOD_SPADE || mat == Material.WOOD_SWORD

				|| mat == Material.STONE_AXE || mat == Material.STONE_PICKAXE
				|| mat == Material.STONE_SPADE || mat == Material.STONE_SWORD

				|| mat == Material.IRON_AXE || mat == Material.IRON_PICKAXE
				|| mat == Material.IRON_SPADE || mat == Material.IRON_SWORD

				|| mat == Material.GOLD_AXE || mat == Material.GOLD_PICKAXE
				|| mat == Material.GOLD_SPADE || mat == Material.GOLD_SWORD

				|| mat == Material.DIAMOND_AXE
				|| mat == Material.DIAMOND_PICKAXE
				|| mat == Material.DIAMOND_SPADE
				|| mat == Material.DIAMOND_SWORD)

			return true;

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
			// || block.getType() == Material.SNOW_BLOCK)
			return true;
		if (canPlantbend(player) && isPlant(block))
			return true;
		return false;
	}

	public static boolean canPlantbend(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}

		if (player.hasPermission("bending.water.plantbending")
				|| bPlayer.hasLevel("plantbending")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean hasAbility(Player player, Abilities ability) {
		// return config.hasAbility(player, ability);
		return canBend(player, ability);
	}

	public static boolean isPlant(Block block) {
		if (plantIds.contains(block.getType()))
			return true;
		return false;
	}

	// public static boolean isBender(Player player, BendingType type) {
	// //return config.isBender(player, type);
	// return Bending.benders.get(player.getName()).contains(type);
	// }

	public static boolean isBender(String player, BendingType type) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return false;
		return bPlayer.isBender(type);

	}

	public static boolean isBender(String player) {
		// return config.isBender(player, type);
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return false;
		return bPlayer.isBender();
		// if (Bending.benders.containsKey(player)) {
		// if (Bending.benders.get(player).size() > 0)
		// return true;
		// }
		// return false;
	}

	public static Abilities getBendingAbility(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return null;
		return bPlayer.getAbility();
		// return config.getAbility(player);
	}

	public static List<BendingType> getBendingTypes(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return null;
		return bPlayer.getBendingTypes();
		// return config.getBendingTypes(player);
	}

	public static double getDistanceFromLine(Vector line, Location pointonline,
			Location point) {

		Vector AP = new Vector();
		double Ax, Ay, Az;
		Ax = pointonline.getX();
		Ay = pointonline.getY();
		Az = pointonline.getZ();

		double Px, Py, Pz;
		Px = point.getX();
		Py = point.getY();
		Pz = point.getZ();

		AP.setX(Px - Ax);
		AP.setY(Py - Ay);
		AP.setZ(Pz - Az);

		return (AP.crossProduct(line).length()) / (line.length());
	}

	public static <T> void verbose(T something) {
		if (something != null) {
			Bending.log.info("[Bending] " + something.toString());
		}
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

	public static Entity getTargettedEntity(Player player, double range) {
		return getTargettedEntity(player, range, new ArrayList<Entity>());
	}

	public static Entity getTargettedEntity(Player player, double range,
			List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (Entity entity : origin.getWorld().getEntities()) {
			if (avoid.contains(entity))
				continue;
			if (entity.getLocation().distance(origin) < longestr
					&& getDistanceFromLine(direction, origin,
							entity.getLocation()) < 2
					&& (entity instanceof LivingEntity)
					&& entity.getEntityId() != player.getEntityId()
					&& entity.getLocation().distance(
							origin.clone().add(direction)) < entity
							.getLocation().distance(
									origin.clone().add(
											direction.clone().multiply(-1)))) {
				target = entity;
				longestr = entity.getLocation().distance(origin);
			}
		}
		return target;
	}

	public static void damageEntity(Player player, Entity entity, double damage) {
		if (entity instanceof LivingEntity) {
			if (AvatarState.isAvatarState(player)) {
				damage = AvatarState.getValue(damage);
			}

			// EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
			// player, entity, DamageCause.CUSTOM, damage);
			// Bending.plugin.getServer().getPluginManager().callEvent(event);
			// verbose(event.isCancelled());

			((LivingEntity) entity).damage(damage, player);
			((LivingEntity) entity)
					.setLastDamageCause(new EntityDamageByEntityEvent(player,
							entity, DamageCause.CUSTOM, damage));
		}
	}

	public static Vector rotateVectorAroundVector(Vector axis, Vector rotator,
			double degrees) {
		double angle = Math.toRadians(degrees);
		Vector rotation = axis.clone();
		Vector rotate = rotator.clone();
		rotation = rotation.normalize();

		Vector thirdaxis = rotation.crossProduct(rotate).normalize()
				.multiply(rotate.length());

		return rotate.multiply(Math.cos(angle)).add(
				thirdaxis.multiply(Math.sin(angle)));

		// return new Vector(x, z, y);
	}

	public static Vector getOrthogonalVector(Vector axis, double degrees,
			double length) {

		Vector ortho = new Vector(axis.getY(), -axis.getX(), 0);
		ortho = ortho.normalize();
		ortho = ortho.multiply(length);

		return rotateVectorAroundVector(axis, ortho, degrees);

	}

	public static Location getPointOnLine(Location origin, Location target,
			double distance) {
		return origin.clone().add(
				getDirection(origin, target).normalize().multiply(distance));

	}

	public static Vector getDirection(Location location, Location destination) {
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

	public static boolean isMeltable(Block block) {
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW) {
			return true;
		}
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
			if (FreezeMelt.frozenblocks.containsKey(blocki)) {
				if (FreezeMelt.frozenblocks.get(blocki) == full)
					sources++;
			} else if (blocki.getType() == Material.ICE) {
				sources++;
			}
		}
		if (sources >= 2)
			return true;
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

	public static void stopAllBending() {
		AirBlast.removeAll();
		AirBubble.removeAll();
		AirShield.instances.clear();
		AirSuction.instances.clear();
		AirScooter.removeAll();
		AirSpout.removeAll();
		AirSwipe.instances.clear();
		Speed.instances.clear();
		Tornado.instances.clear();
		AirBurst.removeAll();

		Catapult.removeAll();
		CompactColumn.removeAll();
		EarthBlast.removeAll();
		EarthColumn.removeAll();
		EarthPassive.removeAll();
		EarthArmor.removeAll();
		EarthTunnel.instances.clear();
		Shockwave.removeAll();
		Tremorsense.removeAll();

		FreezeMelt.removeAll();
		IceSpike.removeAll();
		IceSpike2.removeAll();
		WaterManipulation.removeAll();
		WaterSpout.removeAll();
		WaterWall.removeAll();
		Wave.removeAll();
		Plantbending.regrowAll();
		OctopusForm.removeAll();
		Bloodbending.instances.clear();

		FireStream.removeAll();
		Fireball.removeAll();
		WallOfFire.instances.clear();
		Lightning.instances.clear();
		FireShield.removeAll();
		FireBlast.removeAll();
		FireBurst.removeAll();
		FireJet.instances.clear();
		Cook.removeAll();
		Illumination.removeAll();

		RapidPunch.instance.clear();

		// BendingManager.removeFlyers();
		Flight.removeAll();
		WaterReturn.removeAll();
		TempBlock.removeAll();
		removeAllEarthbendedBlocks();
	}

	public static boolean canBend(Player player, Abilities ability) {
		if (ability == null) {
			return false;
		}

		if (player == null) {
			return false;
		}

		if (hasPermission(player, ability) && ability == Abilities.AvatarState)
			return true;

		if (!hasPermission(player, ability)) {
			return false;
		}
		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player)))
			return false;

		if (Abilities.isAirbending(ability)
				&& !isBender(player.getName(), BendingType.Air)) {
			return false;
		}
		if (Abilities.isChiBlocking(ability)
				&& !isBender(player.getName(), BendingType.ChiBlocker)) {
			return false;
		}
		if (Abilities.isEarthbending(ability)
				&& !isBender(player.getName(), BendingType.Earth)) {
			return false;
		}
		if (Abilities.isFirebending(ability)
				&& !isBender(player.getName(), BendingType.Fire)) {
			return false;
		}
		if (Abilities.isWaterbending(ability)
				&& !isBender(player.getName(), BendingType.Water)) {
			return false;
		}
		if (hasPermission(player, ability)
				&& (!isLocalAbility(ability) || !isRegionProtectedFromBuild(
						player, Abilities.AirBlast, player.getLocation()))
				&& !toggledBending(player)) {
			return true;
		}

		if (allowharmless && Tools.isHarmlessAbility(ability)
				&& !toggledBending(player))
			return true;
		return false;

	}

	public static boolean canBeBloodbent(Player player) {
		if (AvatarState.isAvatarState(player))
			return false;
		if ((isChiBlocked(player)))
			return true;
		Abilities ability = Abilities.Bloodbending;
		if (canBend(player, ability) && !toggledBending(player))
			return false;
		return true;
	}

	public static boolean isHarmlessAbility(Abilities ability) {
		return harmlessAbilities.contains(ability);
	}

	public static boolean isLocalAbility(Abilities ability) {
		return localAbilities.contains(ability);
	}

	public static boolean isRangedAbility(Abilities ability) {
		return !isLocalAbility(ability);
	}

	public static boolean toggledBending(Player player) {
		if (toggledBending.contains(player))
			return true;
		return false;
	}

	public static void printHooks() {
		Plugin wgp = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if (wgp != null) {
			verbose("Recognized WorldGuard...");
			if (respectWorldGuard) {
				verbose("Bending is set to respect WorldGuard's build flags.");
			} else {
				verbose("But Bending is set to ignore WorldGuard's flags.");
			}
		}

		Plugin fcp = Bukkit.getPluginManager().getPlugin("Factions");
		if (fcp != null) {
			verbose("Recognized Factions...");
			if (respectFactions) {
				verbose("Bending is set to respect Factions' claimed lands.");
			} else {
				verbose("But Bending is set to ignore Factions' claimed lands.");
			}
		}
	}

	public static boolean isRegionProtectedFromBuild(Player player,
			Abilities ability, Location loc) {

		List<Abilities> ignite = new ArrayList<Abilities>();
		ignite.add(Abilities.Blaze);
		List<Abilities> explode = new ArrayList<Abilities>();
		explode.add(Abilities.FireBlast);
		explode.add(Abilities.Lightning);

		if (ability == null && allowharmless)
			return false;
		if (isHarmlessAbility(ability) && allowharmless)
			return false;

		// if (ignite.contains(ability)) {
		// BlockIgniteEvent event = new BlockIgniteEvent(location.getBlock(),
		// IgniteCause.FLINT_AND_STEEL, player);
		// Bending.plugin.getServer().getPluginManager().callEvent(event);
		// if (event.isCancelled())
		// return false;
		// event.setCancelled(true);
		// }

		PluginManager pm = Bukkit.getPluginManager();

		Plugin wgp = pm.getPlugin("WorldGuard");
		Plugin fcp = pm.getPlugin("Factions");
		Plugin mcore = pm.getPlugin("mcore");

		for (Location location : new Location[] { loc, player.getLocation() }) {

			if (wgp != null && respectWorldGuard) {
				WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit
						.getPluginManager().getPlugin("WorldGuard");
				if (!player.isOnline())
					return true;

				if (ignite.contains(ability)) {
					if (!wg.hasPermission(player, "worldguard.override.lighter")) {
						if (wg.getGlobalStateManager().get(location.getWorld()).blockLighter)
							return true;
						if (!wg.getGlobalRegionManager().hasBypass(player,
								location.getWorld())
								&& !wg.getGlobalRegionManager()
										.get(location.getWorld())
										.getApplicableRegions(location)
										.allows(DefaultFlag.LIGHTER,
												wg.wrapPlayer(player)))
							return true;
					}

				}

				if (explode.contains(ability)) {
					if (wg.getGlobalStateManager().get(location.getWorld()).blockTNTExplosions)
						return true;
					if (!wg.getGlobalRegionManager().get(location.getWorld())
							.getApplicableRegions(location)
							.allows(DefaultFlag.TNT))
						return true;
				}

				if ((!(wg.getGlobalRegionManager().canBuild(player, location)) || !(wg
						.getGlobalRegionManager()
						.canConstruct(player, location)))) {
					return true;
				}
			}

			if (fcp != null && mcore != null && respectFactions) {
				if (ignite.contains(ability)) {

				}

				if (explode.contains(ability)) {

				}

				if (!FactionsListenerMain.canPlayerBuildAt(player,
						PS.valueOf(loc.getBlock()), false)) {
					return true;
				}

				// if (!FactionsBlockListener.playerCanBuildDestroyBlock(player,
				// location, "build", true)) {
				// return true;
				// }
			}
		}

		return false;
	}

	// public static boolean isRegionProtected(Player player, Abilities ability,
	// boolean look) {
	//
	// Plugin wgp = Bukkit.getPluginManager().getPlugin("WorldGuard");
	// Plugin psp = Bukkit.getPluginManager().getPlugin("PreciousStone");
	// Plugin fcp = Bukkit.getPluginManager().getPlugin("Factions");
	// Plugin twnp = Bukkit.getPluginManager().getPlugin("Towny");
	//
	// if (wgp != null && respectWorldGuard) {
	// WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager()
	// .getPlugin("WorldGuard");
	// if (!player.isOnline())
	// return true;
	// if (look) {
	// int range = 20;
	// Block c = player.getTargetBlock(null, range);
	// if ((!(wg.getGlobalRegionManager().canBuild(player,
	// c.getLocation())) || !(wg.getGlobalRegionManager()
	// .canConstruct(player, c.getLocation())))) {
	// return true;
	// }
	// } else if (!(wg.getGlobalRegionManager().canBuild(player,
	// player.getLocation()))
	// || !(wg.getGlobalRegionManager().canConstruct(player,
	// player.getLocation()))) {
	// return true;
	// }
	// }
	//
	// if (psp != null && respectPreciousStones) {
	// PreciousStones ps = (PreciousStones) psp;
	// Block b = player.getLocation().getBlock();
	//
	// if (look) {
	//
	// int range = 20;
	// Block c = player.getTargetBlock(null, range);
	// if (ps.getForceFieldManager().hasSourceField(c.getLocation(),
	// FieldFlag.PREVENT_PLACE))
	// return true;
	//
	// } else {
	// if (ps.getForceFieldManager().hasSourceField(
	// player.getLocation(), FieldFlag.PREVENT_PLACE))
	// return true;
	// }
	//
	// if (ps.getForceFieldManager().hasSourceField(b.getLocation(),
	// FieldFlag.PREVENT_PLACE))
	// return true;
	// }
	//
	// if (fcp != null && respectFactions) {
	// if (isLocalAbility(ability)
	// && !FactionsBlockListener.playerCanBuildDestroyBlock(
	// player, player.getLocation(), "build", false)) {
	// return true;
	// } else if (!isLocalAbility(ability)
	// && !FactionsBlockListener.playerCanBuildDestroyBlock(
	// player, getTargetedLocation(player, 20), "build",
	// false)) {
	// return true;
	// }
	// }
	//
	// if (twnp != null && respectTowny) {
	// Towny twn = (Towny) twnp;
	// Block block;
	// if (isLocalAbility(ability)) {
	// block = player.getLocation().getBlock();
	// } else {
	// block = player.getTargetBlock(null, 20);
	// }
	//
	// WorldCoord worldCoord;
	//
	// try {
	// TownyWorld world = TownyUniverse.getDataSource().getWorld(
	// block.getWorld().getName());
	// worldCoord = new WorldCoord(world.getName(),
	// Coord.parseCoord(block));
	//
	// // Get build permissions (updates if none exist)
	// boolean bBuild = PlayerCacheUtil.getCachePermission(player,
	// block.getLocation(), 3, (byte) 0,
	// TownyPermission.ActionType.BUILD);
	//
	// // Allow build if we are permitted
	// if (!bBuild) {
	//
	// /*
	// * Fetch the players cache
	// */
	// PlayerCache cache = twn.getCache(player);
	// TownBlockStatus status = cache.getStatus();
	//
	// /*
	// * Flag war
	// */
	// if (((status == TownBlockStatus.ENEMY) && TownyWarConfig
	// .isAllowingAttacks())) {
	//
	// try {
	// TownyWar.callAttackCellEvent(twn, player, block,
	// worldCoord);
	// } catch (Exception e) {
	// TownyMessaging.sendErrorMsg(player, e.getMessage());
	// }
	//
	// return true;
	//
	// } else if (status == TownBlockStatus.WARZONE) {
	// } else {
	// return true;
	// }
	//
	// /*
	// * display any error recorded for this plot
	// */
	// if ((cache.hasBlockErrMsg()))
	// TownyMessaging.sendErrorMsg(player,
	// cache.getBlockErrMsg());
	// }
	//
	// } catch (Exception e1) {
	// TownyMessaging.sendErrorMsg(player,
	// TownySettings.getLangString("msg_err_not_configured"));
	// }
	//
	// }
	//
	// // EntityDamageByEntityEvent damageEvent = new
	// // EntityDamageByEntityEvent(player, player,
	// // EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1);
	// // Bukkit.getServer().getPluginManager().callEvent(damageEvent);
	//
	// // if (damageEvent.isCancelled())
	// // {
	// // return true;
	// // }
	// // }
	//
	// return false;
	// }

	public static boolean canBendPassive(Player player, BendingType type) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player))
				&& !AvatarState.isAvatarState(player))
			return false;
		if (!player.hasPermission("bending." + type + ".passive")) {
			if (type == BendingType.Earth
					&& bPlayer.getLevel(type) >= ConfigManager.earthPassiveLevelRequired) {
				return true;
			}
			if (type == BendingType.Air
					&& bPlayer.getLevel(type) >= ConfigManager.airPassiveLevelRequired) {
				return true;
			}
			if (type == BendingType.ChiBlocker && bPlayer.getLevel(type) >= 1) {
				return true;
			}
			return false;
		}
		if (allowharmless && type != BendingType.Earth)
			return true;
		if (isRegionProtectedFromBuild(player, null, player.getLocation()))
			return false;
		return true;
	}

	public static boolean hasPermission(Player player, Abilities ability) {
		if (ability == Abilities.AvatarState
				&& player.hasPermission("bending.admin.AvatarState")) {
			return true;
		}

		if (Abilities.isAirbending(ability)
				&& player.hasPermission("bending.air." + ability)) {
			return true;
		}
		if (Abilities.isWaterbending(ability)
				&& player.hasPermission("bending.water." + ability)) {
			return true;
		}
		if (Abilities.isEarthbending(ability)
				&& player.hasPermission("bending.earth." + ability)) {
			return true;
		}
		if (Abilities.isFirebending(ability)
				&& player.hasPermission("bending.fire." + ability)) {
			return true;
		}
		if (Abilities.isChiBlocking(ability)
				&& player.hasPermission("bending.chiblocker." + ability)) {
			return true;
		}
		if (Abilities.isChiBlocking(ability)
				&& player.hasPermission("bending.chiblocking." + ability)) {
			return true;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.hasLevel(ability)) {
			return true;
		}
		return false;
	}

	public static ChatColor getColor(String input) {
		return (ChatColor) colors.get(input.toLowerCase().replace("&", ""));

	}

	public static boolean isDay(World world) {
		long time = world.getTime();
		if (time >= 23500 || time <= 12500) {
			return true;
		}
		return false;
	}

	public static double firebendingDayAugment(double value, World world) {
		if (isDay(world)) {
			return ConfigManager.dayFactor * value;
		}
		return value;
	}

	public static double getFirebendingDayAugment(World world) {
		if (isDay(world))
			return ConfigManager.dayFactor;
		return 1;
	}

	public static double waterbendingNightAugment(double value, World world) {
		if (isNight(world)) {
			return ConfigManager.nightFactor * value;
		}
		return value;
	}

	public static double getWaterbendingNightAugment(World world) {
		if (isNight(world))
			return ConfigManager.nightFactor;
		return 1;
	}

	public static boolean isNight(World world) {
		if (world.getEnvironment() == Environment.NETHER
				|| world.getEnvironment() == Environment.THE_END) {
			return false;
		}
		long time = world.getTime();
		if (time >= 12950 && time <= 23050) {
			return true;
		}
		return false;
	}

	public static boolean isSolid(Block block) {
		if (nonOpaque.contains(block.getType()))
			return false;
		return true;
	}

	public static void blockChi(Player player, long time) {
		if (blockedchis.containsKey(player)) {
			blockedchis.replace(player, time);
		} else {
			blockedchis.put(player, time);
		}
	}

	public static boolean isChiBlocked(Player player) {
		if (Paralyze.isParalyzed(player) && !AvatarState.isAvatarState(player))
			return true;
		if (blockedchis.containsKey(player)) {
			long time = System.currentTimeMillis();
			if (time > blockedchis.get(player) + ConfigManager.chiblockduration
					|| AvatarState.isAvatarState(player)) {
				blockedchis.remove(player);
				return false;
			}
			return true;
		}
		return false;
	}

	public static void breakBlock(Block block) {
		block.breakNaturally(new ItemStack(Material.AIR));
	}

	public static boolean adjacentToFrozenBlock(Block block) {
		BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH,
				BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		boolean adjacent = false;
		for (BlockFace face : faces) {
			if (FreezeMelt.frozenblocks.containsKey((block.getRelative(face))))
				adjacent = true;
		}
		return adjacent;
	}

	public static <T> void writeToLog(T something) {
		String string = "";
		if (something != null) {
			string = something.toString();
		}
		try {
			FileWriter fstream = new FileWriter("bending.log", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(string);
			out.newLine();
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	public static void removeBlock(Block block) {
		if (adjacentToThreeOrMoreSources(block)) {
			block.setType(Material.WATER);
			block.setData((byte) 0x0);
		} else {
			block.setType(Material.AIR);
		}
	}

	public static void removeSpouts(Location location, double radius,
			Player sourceplayer) {
		WaterSpout.removeSpouts(location, radius, sourceplayer);
		AirSpout.removeSpouts(location, radius, sourceplayer);
	}

	public static void removeSpouts(Location location, Player sourceplayer) {
		removeSpouts(location, 1.5, sourceplayer);
	}

	public static Block getEarthSourceBlock(Player player, double range) {
		Block testblock = getTargetBlock(player, (int) range,
				getTransparentEarthbending());
		if (Tools.isEarthbendable(player, testblock))
			return testblock;
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			if (isRegionProtectedFromBuild(player, Abilities.RaiseEarth,
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
		// byte full = 0x0;
		// Block block = player.getTargetBlock(null, range);
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			if (isRegionProtectedFromBuild(player, Abilities.WaterManipulation,
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
			// } else if ((block.getType() == Material.WATER || block.getType()
			// == Material.STATIONARY_WATER)
			// && block.getData() == full) {
			// return block;
			// }
		}
		return null;
	}

	public static void playFocusWaterEffect(Block block) {
		block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4, 20);
	}

	public static BlockFace getCardinalDirection(Vector vector) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST,
				BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
				BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
		Vector n, ne, e, se, s, sw, w, nw;
		w = new Vector(-1, 0, 0);
		n = new Vector(0, 0, -1);
		s = n.clone().multiply(-1);
		e = w.clone().multiply(-1);
		ne = n.clone().add(e.clone()).normalize();
		se = s.clone().add(e.clone()).normalize();
		nw = n.clone().add(w.clone()).normalize();
		sw = s.clone().add(w.clone()).normalize();

		Vector[] vectors = { n, ne, e, se, s, sw, w, nw };

		double comp = 0;
		int besti = 0;
		for (int i = 0; i < vectors.length; i++) {
			double dot = vector.dot(vectors[i]);
			if (dot > comp) {
				comp = dot;
				besti = i;
			}
		}

		return faces[besti];

	}

	public static String getSupportedLanguages() {
		String languages = "";
		List<String> suplangs = Bending.language.getSupportedLanguages();
		for (int i = 0; i < suplangs.size(); i++) {
			String string = suplangs.get(i);
			if (i != suplangs.size() - 1) {
				string = string + ", ";
			}
			languages = languages + string;
		}
		return languages;
	}

	public static String getDefaultLanguage() {
		return Bending.language.getDefaultLanguage();
	}

	public static void sendMessage(Player player, String key) {
		sendMessage(player, ChatColor.WHITE, key);
	}

	public static void sendMessage(Player player, ChatColor color, String key) {
		String message = getMessage(player, key);
		if (player == null) {
			verbose(color + message);
		} else {
			player.sendMessage(color + message);
		}
	}

	public static String getMessage(Player player, String key) {
		String language = getLanguage(player);
		String message = Bending.language.getMessage(language, key);
		return message;
	}

	public static String getLanguage(Player player) {
		String language = getDefaultLanguage();
		if (player != null)
			language = BendingPlayer.getBendingPlayer(player).getLanguage();
		return language;
	}

	public static boolean isLanguageSupported(String language) {
		return (Bending.language.getSupportedLanguages().contains(language
				.toLowerCase()));
	}

	// public static void logBending(Player player, Abilities ability) {
	// if (Bending.logblock != null) {
	//
	// }
	// }

	public static int getIntCardinalDirection(Vector vector) {
		BlockFace face = getCardinalDirection(vector);
		switch (face) {
		case SOUTH:
			return 7;
		case SOUTH_WEST:
			return 6;
		case WEST:
			return 3;
		case NORTH_WEST:
			return 0;
		case NORTH:
			return 1;
		case NORTH_EAST:
			return 2;
		case EAST:
			return 5;
		case SOUTH_EAST:
			return 8;
		default:
			break;
		}
		return 4;
	}

	static {
		Map<String, ChatColor> tmpMap = new HashMap<String, ChatColor>();
		tmpMap.put("black", ChatColor.BLACK);
		tmpMap.put("0", ChatColor.BLACK);

		tmpMap.put("dark blue", ChatColor.DARK_BLUE);
		tmpMap.put("dark_blue", ChatColor.DARK_BLUE);
		tmpMap.put("1", ChatColor.DARK_BLUE);

		tmpMap.put("dark green", ChatColor.DARK_GREEN);
		tmpMap.put("dark_green", ChatColor.DARK_GREEN);
		tmpMap.put("2", ChatColor.DARK_GREEN);

		tmpMap.put("dark aqua", ChatColor.DARK_AQUA);
		tmpMap.put("dark_aqua", ChatColor.DARK_AQUA);
		tmpMap.put("teal", ChatColor.DARK_AQUA);
		tmpMap.put("3", ChatColor.DARK_AQUA);

		tmpMap.put("dark red", ChatColor.DARK_RED);
		tmpMap.put("dark_red", ChatColor.DARK_RED);
		tmpMap.put("4", ChatColor.DARK_RED);

		tmpMap.put("dark purple", ChatColor.DARK_PURPLE);
		tmpMap.put("dark_purple", ChatColor.DARK_PURPLE);
		tmpMap.put("purple", ChatColor.DARK_PURPLE);
		tmpMap.put("5", ChatColor.DARK_PURPLE);

		tmpMap.put("gold", ChatColor.GOLD);
		tmpMap.put("orange", ChatColor.GOLD);
		tmpMap.put("6", ChatColor.GOLD);

		tmpMap.put("gray", ChatColor.GRAY);
		tmpMap.put("grey", ChatColor.GRAY);
		tmpMap.put("7", ChatColor.GRAY);

		tmpMap.put("dark gray", ChatColor.DARK_GRAY);
		tmpMap.put("dark_gray", ChatColor.DARK_GRAY);
		tmpMap.put("dark grey", ChatColor.DARK_GRAY);
		tmpMap.put("dark_grey", ChatColor.DARK_GRAY);
		tmpMap.put("8", ChatColor.DARK_GRAY);

		tmpMap.put("blue", ChatColor.BLUE);
		tmpMap.put("9", ChatColor.BLUE);

		tmpMap.put("bright green", ChatColor.GREEN);
		tmpMap.put("bright_green", ChatColor.GREEN);
		tmpMap.put("green", ChatColor.GREEN);
		tmpMap.put("a", ChatColor.GREEN);

		tmpMap.put("aqua", ChatColor.AQUA);
		tmpMap.put("b", ChatColor.AQUA);

		tmpMap.put("red", ChatColor.RED);
		tmpMap.put("c", ChatColor.RED);

		tmpMap.put("light purple", ChatColor.LIGHT_PURPLE);
		tmpMap.put("light_purple", ChatColor.LIGHT_PURPLE);
		tmpMap.put("pink", ChatColor.LIGHT_PURPLE);
		tmpMap.put("d", ChatColor.LIGHT_PURPLE);

		tmpMap.put("yellow", ChatColor.YELLOW);
		tmpMap.put("e", ChatColor.YELLOW);

		tmpMap.put("white", ChatColor.WHITE);
		tmpMap.put("f", ChatColor.WHITE);

		tmpMap.put("random", ChatColor.MAGIC);
		tmpMap.put("magic", ChatColor.MAGIC);
		tmpMap.put("k", ChatColor.MAGIC);

		colors = Collections.unmodifiableMap(tmpMap);
	}

}

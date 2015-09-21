package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name = "Blaze", element = BendingElement.Fire)
public class FireStream extends BendingActiveAbility {
	private static Map<Block, Player> ignitedblocks = new HashMap<Block, Player>();
	private static Map<Block, Long> ignitedtimes = new HashMap<Block, Long>();
	static final long soonesttime = Tools.timeinterval;

	private static final Material[] overwriteable = { Material.SAPLING, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
			Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.FIRE, Material.SNOW, Material.TORCH };

	private static final Material[] ignitables = { Material.BEDROCK, Material.BOOKSHELF, Material.BRICK, Material.CLAY, Material.CLAY_BRICK,
			Material.COAL_ORE, Material.COBBLESTONE, Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.DIRT, Material.ENDER_STONE,
			Material.GLOWING_REDSTONE_ORE, Material.GOLD_BLOCK, Material.GRAVEL, Material.GRASS, Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2,
			Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.LOG, Material.MOSSY_COBBLESTONE, Material.MYCEL, Material.NETHER_BRICK,
			Material.NETHERRACK, Material.OBSIDIAN, Material.REDSTONE_ORE, Material.SAND, Material.SANDSTONE, Material.SMOOTH_BRICK, Material.STONE,
			Material.SOUL_SAND, Material.SNOW_BLOCK, Material.WOOD, Material.WOOL, Material.LEAVES };

	public static int firedamage = 3;
	public static int tickdamage = 2;

	private static int ID = Integer.MIN_VALUE;

	@ConfigurationParameter("Speed")
	private static double SPEED = 15;
	private static long interval = (long) (1000. / SPEED);

	@ConfigurationParameter("Dissipate-Time")
	private static long dissipateAfter = 1200;

	private Location origin;
	private Location location;
	private Vector direction;
	private int id;
	private long time;
	private double range;

	public FireStream(Location location, Vector direction, Player player, int range, IBendingAbility parent) {
		super(player, parent);
		this.range = PluginTools.firebendingDayAugment(range, player.getWorld());
		this.player = player;
		this.origin = location.clone();
		this.location = this.origin.clone();
		this.direction = direction.clone();
		this.direction.setY(0);
		this.direction = this.direction.clone().normalize();
		this.location = this.location.clone().add(this.direction);
		this.id = ID++;
		this.time = this.startedTime;
		AbilityManager.getManager().addInstance(this);
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.FireStream, this.location)) {
			return false;
		}
		if ((System.currentTimeMillis() - this.time) >= interval) {
			this.location = this.location.clone().add(this.direction);
			this.time = System.currentTimeMillis();
			if (this.location.distance(this.origin) > this.range) {
				return false;
			}
			Block block = this.location.getBlock();
			if (isIgnitable(this.player, block)) {
				ignite(block);
				return true;
			} else if (isIgnitable(this.player, block.getRelative(BlockFace.DOWN))) {
				ignite(block.getRelative(BlockFace.DOWN));
				this.location = block.getRelative(BlockFace.DOWN).getLocation();
				return true;
			} else if (isIgnitable(this.player, block.getRelative(BlockFace.UP))) {
				ignite(block.getRelative(BlockFace.UP));
				this.location = block.getRelative(BlockFace.UP).getLocation();
				return true;
			} else {
				return false;
			}

		}
		return true;
	}

	private void ignite(Block block) {
		block.setType(Material.FIRE);
		ignitedblocks.put(block, this.player);
		ignitedtimes.put(block, System.currentTimeMillis());
	}

	public static boolean isIgnitable(Player player, Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.Blaze, block.getLocation())) {
			return false;
		}

		if (Arrays.asList(overwriteable).contains(block.getType())) {
			return true;
		} else if (block.getType() != Material.AIR) {
			return false;
		}

		Block belowblock = block.getRelative(BlockFace.DOWN);
		if (Arrays.asList(ignitables).contains(belowblock.getType())) {
			return true;
		}

		return false;
	}

	public static void removeAll() {
		List<Block> toRemove = new LinkedList<Block>(ignitedblocks.keySet());
		for (Block block : toRemove) {
			remove(block);
		}
	}

	public static void removeAllNoneFireIgnitedBlock() {
		List<Block> toRemove = new LinkedList<Block>(ignitedblocks.keySet());
		for (Block block : toRemove) {
			if (block.getType() != Material.FIRE) {
				remove(block);
			}
		}
	}

	public static void dissipateAll() {
		if (dissipateAfter != 0) {
			List<Block> toRemove = new LinkedList<Block>(ignitedtimes.keySet());
			for (Block block : toRemove) {
				if (block.getType() != Material.FIRE) {
					remove(block);
				} else {
					long time = ignitedtimes.get(block);
					if (System.currentTimeMillis() > (time + dissipateAfter)) {
						block.setType(Material.AIR);
						remove(block);
					}
				}
			}
		}
	}

	public static void remove(Block block) {
		if (ignitedblocks.containsKey(block)) {
			ignitedblocks.remove(block);
		}
		if (ignitedtimes.containsKey(block)) {
			ignitedtimes.remove(block);
		}

	}

	public static void removeAroundPoint(Location location, double radius) {
		List<FireStream> toRemove = new LinkedList<FireStream>();
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireStream);
		for (IBendingAbility ability : instances.values()) {
			FireStream stream = (FireStream) ability;
			if (stream.location.getWorld().equals(location.getWorld())) {
				if (stream.location.distance(location) <= radius) {
					toRemove.add(stream);
				}
			}
		}
		for (FireStream stream : toRemove) {
			stream.remove();
		}
	}

	public static void addIgnitedBlock(Block block, Player player, long time) {
		ignitedblocks.put(block, player);
		ignitedtimes.put(block, time);
	}

	public static boolean isIgnited(Block block) {
		return ignitedblocks.containsKey(block);
	}

	public static Player getIgnited(Block block) {
		return ignitedblocks.get(block);
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public BendingAbilities getAbilityType() {
		return BendingAbilities.FireStream;
	}
}

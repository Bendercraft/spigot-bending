package net.bendercraft.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

public class FireStream {
	private static Map<Block, Player> ignitedblocks = new HashMap<Block, Player>();
	private static Map<Block, Long> ignitedtimes = new HashMap<Block, Long>();

	@ConfigurationParameter("Speed")
	private static double SPEED = 15;

	private Location origin;
	private Location location;
	private Vector direction;
	private long time;
	private double range;
	private Player player;
	private long interval;
	private long duration;

	private final RegisteredAbility blazeRegister;

	public FireStream(Location location, Vector direction, Player player, int range, long duration) {
		this.range = range;
		this.player = player;
		this.origin = location.clone();
		this.location = this.origin.clone();
		this.direction = direction.clone();
		this.direction.setY(0);
		this.direction = this.direction.clone().normalize();
		this.location = this.location.clone().add(this.direction);
		this.time = System.currentTimeMillis();
		this.blazeRegister = AbilityManager.getManager().getRegisteredAbility(Blaze.NAME);
		this.duration = duration;
		this.interval = (long) (1000. / SPEED);
	}

	public boolean progress() {
		if (ProtectionManager.isLocationProtectedFromBending(this.player, blazeRegister, this.location)) {
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
		addIgnitedBlock(block, player, duration);
	}

	public static boolean isIgnitable(Player player, Block block) {
		if (ProtectionManager.isLocationProtectedFromBending(player, AbilityManager.getManager().getRegisteredAbility(Blaze.NAME), block.getLocation())) {
			return false;
		}

		if (block.getRelative(BlockFace.DOWN).getType().isSolid()
				&& (BlockTools.isPlant(block) || BlockTools.isAir(block))) {
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

	public static void progressAll() {
		long now = System.currentTimeMillis();
		List<Block> toRemove = new LinkedList<Block>(ignitedtimes.keySet());
		for (Block block : toRemove) {
			if (block.getType() != Material.FIRE) {
				remove(block);
			} else {
				long time = ignitedtimes.get(block);
				if (now > time) {
					block.setType(Material.AIR);
					remove(block);
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
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(Blaze.NAME);
		for (BendingAbility ability : instances.values()) {
			Blaze blaze = (Blaze) ability;
			for (FireStream stream : blaze.getFirestreams()) {
				if (stream.location.getWorld().equals(location.getWorld())) {
					if (stream.location.distance(location) <= radius) {
						toRemove.add(stream);
					}
				}
			}
		}
	}

	public static void addIgnitedBlock(Block block, Player player, long time) {
		ignitedblocks.put(block, player);
		ignitedtimes.put(block, System.currentTimeMillis()+time);
	}

	public static boolean isIgnited(Block block) {
		return ignitedblocks.containsKey(block);
	}

	public static Player getIgnited(Block block) {
		return ignitedblocks.get(block);
	}
}

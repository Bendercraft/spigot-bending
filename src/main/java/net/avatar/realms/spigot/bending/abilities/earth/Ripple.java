package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Ripple {
	@ConfigurationParameter("Radius")
	static double RADIUS = 15;

	@ConfigurationParameter("Damage")
	private final int DAMAGE = 5;

	private Player player;
	private BendingPlayer bender;

	private Vector direction;
	private Location origin, location;

	private List<Location> locations = new LinkedList<Location>();
	private List<Entity> entities = new LinkedList<Entity>();

	private Block block1, block2, block3, block4;
	private int step = 0;
	private int maxstep;

	private Map<Integer[], Block> blocks = new HashMap<Integer[], Block>();

	public Ripple(Player player, Vector direction) {
		this(player, getInitialLocation(player, direction), direction);
	}

	public Ripple(Player player, Location origin, Vector direction) {
		this.player = player;
		if (origin == null)
			return;
		this.direction = direction.clone().normalize();
		this.origin = origin.clone();
		this.location = origin.clone();

		initializeLocations();
		maxstep = locations.size();

		bender = BendingPlayer.getBendingPlayer(player);
	}

	private static Location getInitialLocation(Player player, Vector direction) {
		Location location = player.getLocation().clone().add(0, -1, 0);
		direction = direction.normalize();

		Block block1 = location.getBlock();

		while (location.getBlock().equals(block1))
			location = location.clone().add(direction);
		for (int i : new int[] { 1, 2, 3, 0, -1 }) {
			Location loc;
			loc = location.clone().add(0, i, 0);
			Block topBlock = loc.getBlock();
			Block botBlock = loc.clone().add(0, -1, 0).getBlock();
			if (BlockTools.isTransparentToEarthbending(player, topBlock) && BlockTools.isEarthbendable(player, botBlock)) {
				location = loc.clone().add(0, -1, 0);
				return location;
			}
		}

		return null;
	}

	public boolean progress() {
		boolean result = true;
		if(origin == null) {
			return false; //TODO problem
		}
		if (!BlockTools.isEarthbendable(player, origin.getBlock())) {
			return false;
		}
		blocks.clear();
		if (step < maxstep) {
			Location newLocation = locations.get(step);
			Block block = location.getBlock();
			location = newLocation.clone();
			if (!newLocation.getBlock().equals(block)) {
				block1 = block2;
				block2 = block3;
				block3 = block4;
				block4 = newLocation.getBlock();

				if (block1 != null)
					if (hasAnyMoved(block1)) {
						block1 = null;
					}
				if (block2 != null)
					if (hasAnyMoved(block2)) {
						block2 = null;
					}
				if (block3 != null)
					if (hasAnyMoved(block3)) {
						block3 = null;
					}
				if (block4 != null)
					if (hasAnyMoved(block4)) {
						block4 = null;
					}

				if (step == 0) {

					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				} else if (step == 1) {

					if (increase(block3))
						block3 = block3.getRelative(BlockFace.UP);
					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				} else if (step == 2) {

					if (decrease(block2))
						block2 = block2.getRelative(BlockFace.DOWN);
					if (increase(block3))
						block3 = block3.getRelative(BlockFace.UP);
					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				} else {

					if (decrease(block1))
						block1 = block1.getRelative(BlockFace.DOWN);
					if (decrease(block2))
						block2 = block2.getRelative(BlockFace.DOWN);
					if (increase(block3))
						block3 = block3.getRelative(BlockFace.UP);
					if (increase(block4))
						block4 = block4.getRelative(BlockFace.UP);

				}
			}
		} else if (step == maxstep) {

			if (decrease(block2))
				block2 = block2.getRelative(BlockFace.DOWN);
			if (decrease(block3))
				block3 = block3.getRelative(BlockFace.DOWN);
			if (increase(block4))
				block4 = block4.getRelative(BlockFace.UP);

		} else if (step == maxstep + 1) {

			if (decrease(block3))
				block3 = block3.getRelative(BlockFace.DOWN);
			if (decrease(block4))
				block4 = block4.getRelative(BlockFace.DOWN);

		} else if (step == maxstep + 2) {

			if (decrease(block4))
				block4 = block4.getRelative(BlockFace.DOWN);
			result = false;

		}

		step += 1;

		for (Entity entity : entities)
			affect(entity);
		entities.clear();
		return result;
	}

	private void initializeLocations() {
		Location location = origin.clone();
		locations.add(location);

		while (location.distance(origin) < RADIUS) {
			location = location.clone().add(direction);
			for (int i : new int[] { 1, 2, 3, 0, -1 }) {
				Location loc;
				loc = location.clone().add(0, i, 0);
				Block topBlock = loc.getBlock();
				Block botBlock = loc.clone().add(0, -1, 0).getBlock();
				if (BlockTools.isTransparentToEarthbending(player, topBlock) && !topBlock.isLiquid() && BlockTools.isEarthbendable(player, botBlock)) {
					location = loc.clone().add(0, -1, 0);
					locations.add(location);
					break;
				} else if (i == -1) {
					return;
				}
			}
		}
	}

	private boolean decrease(Block block) {
		if (block == null)
			return false;
		if (hasAnyMoved(block))
			return false;
		setMoved(block);
		Block botBlock = block.getRelative(BlockFace.DOWN);
		int length = 1;
		if (BlockTools.isEarthbendable(player, botBlock)) {
			length = 2;
			block = botBlock;
		}
		return BlockTools.moveEarth(player, block, new Vector(0, -1, 0), length, false);
	}

	private boolean increase(Block block) {
		if (block == null)
			return false;
		if (hasAnyMoved(block))
			return false;
		setMoved(block);
		Block botblock = block.getRelative(BlockFace.DOWN);
		int length = 1;
		if (BlockTools.isEarthbendable(player, botblock)) {
			length = 2;
		}
		if (BlockTools.moveEarth(player, block, new Vector(0, 1, 0), length, false)) {
			for (Entity entity : EntityTools.getEntitiesAroundPoint(block.getLocation().clone().add(0, 1, 0), 2)) {
				if (entity.getEntityId() != player.getEntityId() && !entities.contains(entity)) {
					if (!(entity instanceof FallingBlock)) {
						entities.add(entity);
					}
				}
			}
			return true;
		}
		return false;
	}

	private void affect(Entity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}

		double damage = DAMAGE;
		if (bender.hasPath(BendingPath.TOUGH)) {
			damage *= 0.85;
		}

		if (bender.hasPath(BendingPath.RECKLESS)) {
			damage *= 1.15;
		}

		if (entity instanceof LivingEntity) {
			EntityTools.damageEntity(bender, entity, damage);
		}

		Vector vector = direction.clone();
		vector.setY(.5);
		entity.setVelocity(vector);
	}

	private void setMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		blocks.put(pair, block);
	}

	private boolean hasAnyMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		if (blocks.containsKey(pair))
			return true;
		return false;
	}
}

package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Ripple implements IAbility {
	private static Map<Integer, Ripple> instances = new HashMap<Integer, Ripple>();
	private static Map<Integer[], Block> blocks = new HashMap<Integer[], Block>();

	static final double radius = 15;
	private static final int damage = 5;

	private static int ID = Integer.MIN_VALUE;

	private Player player;
	private Vector direction;
	private Location origin, location;

	private List<Location> locations = new LinkedList<Location>();
	private List<Entity> entities = new LinkedList<Entity>();

	private Block block1, block2, block3, block4;
	private int id;
	private int step = 0;
	private int maxstep;
	private IAbility parent;

	public Ripple(Player player, Vector direction, IAbility parent) {
		this(player, getInitialLocation(player, direction), direction, parent);
	}

	public Ripple(Player player, Location origin, Vector direction, IAbility parent) {
		this.parent = parent;
		this.player = player;
		if (origin == null)
			return;
		this.direction = direction.clone().normalize();
		this.origin = origin.clone();
		this.location = origin.clone();

		initializeLocations();
		maxstep = locations.size();

		if (BlockTools.isEarthbendable(player, origin.getBlock())) {
			id = ID++;
			if (ID >= Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			instances.put(id, this);
		}

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
			Block topblock = loc.getBlock();
			Block botblock = loc.clone().add(0, -1, 0).getBlock();
			if (BlockTools.isTransparentToEarthbending(player, topblock)
					&& BlockTools.isEarthbendable(player, botblock)) {
				location = loc.clone().add(0, -1, 0);
				return location;
			}
		}

		return null;
	}

	private boolean progress() {
		boolean result = true;
		if (step < maxstep) {
			Location newlocation = locations.get(step);
			Block block = location.getBlock();
			location = newlocation.clone();
			if (!newlocation.getBlock().equals(block)) {
				// if (block2 != null)
				// block1 = block2;
				// if (block3 != null)
				// block2 = block3;
				// if (block4 != null)
				// block3 = block4;
				block1 = block2;
				block2 = block3;
				block3 = block4;
				block4 = newlocation.getBlock();

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

	private void remove() {
		instances.remove(id);
	}

	private void initializeLocations() {
		Location location = origin.clone();
		locations.add(location);

		while (location.distance(origin) < radius) {
			location = location.clone().add(direction);
			for (int i : new int[] { 1, 2, 3, 0, -1 }) {
				Location loc;
				loc = location.clone().add(0, i, 0);
				Block topblock = loc.getBlock();
				Block botblock = loc.clone().add(0, -1, 0).getBlock();
				if (BlockTools.isTransparentToEarthbending(player, topblock)
						&& !topblock.isLiquid()
						&& BlockTools.isEarthbendable(player, botblock)) {
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
		Block botblock = block.getRelative(BlockFace.DOWN);
		int length = 1;
		if (BlockTools.isEarthbendable(player, botblock)) {
			length = 2;
			block = botblock;
		}
		return BlockTools.moveEarth(player, block, new Vector(0, -1, 0), length,
				false);
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
			for (Entity entity : EntityTools.getEntitiesAroundPoint(block
					.getLocation().clone().add(0, 1, 0), 2)) {
				if (entity.getEntityId() != player.getEntityId()
						&& !entities.contains(entity)) {
					if (!(entity instanceof FallingBlock)){
						entities.add(entity); 
					}
				}
			}
			return true;
		}
		return false;
	}

	private void affect(Entity entity) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (entity instanceof LivingEntity) {
			EntityTools.damageEntity(player, entity, damage);
		}

		Vector vector = direction.clone();
		vector.setY(.5);
		entity.setVelocity(vector);

	}

	private static void setMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		blocks.put(pair, block);
	}

	private static boolean hasAnyMoved(Block block) {
		int x = block.getX();
		int z = block.getZ();
		Integer[] pair = new Integer[] { x, z };
		if (blocks.containsKey(pair))
			return true;
		return false;
	}

	public static void progressAll() {
		blocks.clear();
		List<Ripple> toRemove = new LinkedList<Ripple>();
		for (Ripple ripple : instances.values()) {
			boolean keep = ripple.progress();
			if(!keep) {
				toRemove.add(ripple);
			}
		}
		for (Ripple ripple : toRemove) {
			ripple.remove();
		}
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

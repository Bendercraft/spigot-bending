package net.bendercraft.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.*;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

public class TorrentBurst {
	private static double defaultmaxradius = 15;
	private static double dr = 0.5;
	private static double defaultfactor = 1.5;
	private static long interval = Torrent.interval;

	private Player                             player;
	private long                               time;
	private Location                           origin;
	private double                             radius           = dr;
	private double                             maxradius        = defaultmaxradius;
	private double                             factor           = defaultfactor;
	private Int2ObjectMap<Int2DoubleMap> heights          = new Int2ObjectOpenHashMap<>();
	private List<TempBlock>                    blocks           = new LinkedList<>();
	private List<Entity>                       affectedEntities = new LinkedList<>();
	private BendingAbility                     parent;

	public TorrentBurst(Player player, double radius, BendingAbility parent) {
		this.parent = parent;
		this.player = player;
		origin = player.getEyeLocation().clone();
		time = System.currentTimeMillis();
		this.radius = radius;
		initializeHeightsMap();
	}

	private void initializeHeightsMap() {
		for (int i = -1; i <= 1; i++) {
			Int2DoubleMap angles = new Int2DoubleOpenHashMap();
			double dtheta = Math.toDegrees(1 / (maxradius + 2));
			int j = 0;
			for (double theta = 0; theta < 360; theta += dtheta) {
				angles.put(j, theta);
				j++;
			}
			heights.put(i, angles);
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (System.currentTimeMillis() > time + interval) {
			if (radius < maxradius) {
				radius += dr;
			} else {
				return false;
			}

			boolean result = formBurst();
			time = System.currentTimeMillis();
			return result;
		}
		return true;
	}

	private boolean formBurst() {
		for (TempBlock tempBlock : blocks) {
			tempBlock.revertBlock();
		}

		blocks.clear();

		affectedEntities.clear();

		List<Entity> indexlist = EntityTools.getEntitiesAroundPoint(origin, radius + 2);

		List<Block> torrentblocks = new LinkedList<>();

		indexlist.remove(player);

		final ObjectIterator<Int2ObjectMap.Entry<Int2DoubleMap>> heightsIterator = Int2ObjectMaps.fastIterator(heights);
		while(heightsIterator.hasNext()) {
			Int2ObjectMap.Entry<Int2DoubleMap> entry = heightsIterator.next();
			int id = entry.getIntKey();

			Int2DoubleMap angles = entry.getValue();
			ObjectIterator<Int2DoubleMap.Entry> anglesIterator = Int2DoubleMaps.fastIterator(angles);
			while(anglesIterator.hasNext()) {
				Int2DoubleMap.Entry entryAngle = anglesIterator.next();
				double angle = entryAngle.getDoubleValue();
				double theta = Math.toRadians(angle);
				double dx = Math.cos(theta) * radius;
				double dy = id;
				double dz = Math.sin(theta) * radius;
				Location location = origin.clone().add(dx, dy, dz);
				Block block = location.getBlock();
				if (torrentblocks.contains(block)) {
					continue;
				}

				if (BlockTools.isTransparentToEarthbending(player, parent.getRegister(), block)) {
					TempBlock tempBlock = TempBlock.makeTemporary(parent, block, Material.WATER, false);
					blocks.add(tempBlock);
					torrentblocks.add(block);
				}
				else {
					anglesIterator.remove();
					continue;
				}
				for (Entity entity : indexlist) {
					if (ProtectionManager.isEntityProtected(entity)) {
						continue;
					}
					if (!affectedEntities.contains(entity)) {
						if (entity.getLocation().distance(location) <= 2) {
							affectedEntities.add(entity);
							affect(entity);
						}
					}
				}
			}

			if (angles.isEmpty()) {
				heightsIterator.remove();
			}
		}

		if (heights.isEmpty()) {
			return false;
		}

		return true;
	}

	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(parent, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		Vector direction = Tools.getDirection(origin, entity.getLocation());
		direction.setY(0);
		direction.normalize();
		entity.setVelocity(entity.getVelocity().clone().add(direction.multiply(factor)));
	}

	public void remove() {
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
	}
}

package net.bendercraft.spigot.bending.abilities.water;

import java.util.Iterator;
import java.util.List;

import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

	private static int MAX_HEIGHT = 3;

	private Player                       player;
	private long                         time;
	private Location                     origin;
	private double                       radius           = dr;
	private double                       maxradius        = defaultmaxradius;
	private double                       factor           = defaultfactor;
	private Double2ObjectMap<IntList> angles           = new Double2ObjectArrayMap<>();
	private BendingAbility               parent;

	public TorrentBurst(Player player, double radius, BendingAbility parent) {
		this.parent = parent;
		this.player = player;
		origin = player.getEyeLocation().clone();
		time = System.currentTimeMillis();
		this.radius = radius;
		initializeHeightsMap();
	}

	private void initializeHeightsMap() {
		final double drad = 1/(maxradius+2);
		for (double rad = 0; rad < (2*Math.PI); rad += drad) {
			IntList heights = new IntArrayList(MAX_HEIGHT);
			for (int i = -(MAX_HEIGHT/2); i <= (MAX_HEIGHT/2); i++) {
				heights.add(i);
			}
			this.angles.put(rad, heights);
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
		TempBlock.revertForAbility(parent, false);

		List<Entity> entitiesList = EntityTools.getEntitiesAroundPoint(origin, radius + 2, (e) -> !player.equals(e) && !ProtectionManager.isEntityProtected(e));

		ObjectSet<Location> torrentLocations = new ObjectArraySet<>((int)(radius * Math.PI * 2)+1);

		ObjectIterator<Double2ObjectMap.Entry<IntList>> anglesIterator = Double2ObjectMaps.fastIterator(angles);
		while(anglesIterator.hasNext()) {
			Double2ObjectMap.Entry<IntList> entry = anglesIterator.next();
			final double angle = entry.getDoubleKey();
			final double dx = Math.cos(angle) * radius;
			final double dz = Math.sin(angle) * radius;

			IntList heightList = entry.getValue();
			IntListIterator heightIterator = heightList.iterator();
			while(heightIterator.hasNext()){
				final int height = heightIterator.nextInt();
				Location location = origin.clone().add(dx, height, dz);

				final boolean added = torrentLocations.add(location);
				if (!added) {
					continue;
				}
				Block block = location.getBlock();
				if (BlockTools.isTransparentToEarthbending(player, parent.getRegister(), block)) {
					TempBlock.makeTemporary(parent, block, Material.WATER, false);
				}
				else {
					heightIterator.remove();
					continue;
				}

				Iterator<Entity> entityIterator = entitiesList.iterator();
				while (entityIterator.hasNext()) {
					Entity entity = entityIterator.next();
					if (entity.getLocation().distance(location) <= 2) {
						affect(entity);
						entityIterator.remove();
					}
				}
			}

			if (heightList.isEmpty()) {
				anglesIterator.remove();
			}
		}
		if (angles.isEmpty()) {
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
		TempBlock.revertForAbility(parent, false);
	}
}

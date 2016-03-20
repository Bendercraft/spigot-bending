package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class TorrentBurst {
	private static double defaultmaxradius = 15;
	private static double dr = 0.5;
	private static double defaultfactor = 1.5;
	private static long interval = Torrent.interval;

	private Player player;
	private long time;
	private Location origin;
	private double radius = dr;
	private double maxradius = defaultmaxradius;
	private double factor = defaultfactor;
	private Map<Integer, Map<Integer, Double>> heights = new HashMap<Integer, Map<Integer, Double>>();
	private List<TempBlock> blocks = new LinkedList<TempBlock>();
	private List<Entity> affectedentities = new LinkedList<Entity>();
	private final RegisteredAbility torrentRegister;

	public TorrentBurst(Player player, double radius, BendingAbility parent) {
		this.player = player;
		World world = player.getWorld();
		origin = player.getEyeLocation().clone();
		time = System.currentTimeMillis();
		factor = PluginTools.waterbendingNightAugment(factor, world);
		maxradius = PluginTools.waterbendingNightAugment(maxradius, world);
		this.radius = radius;
		this.torrentRegister = AbilityManager.getManager().getRegisteredAbility(Torrent.NAME);
		initializeHeightsMap();
	}

	private void initializeHeightsMap() {
		for (int i = -1; i <= 1; i++) {
			Map<Integer, Double> angles = new HashMap<Integer, Double>();
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

		if (!EntityTools.canBend(player, torrentRegister)) {
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

		affectedentities.clear();

		List<Entity> indexlist = EntityTools.getEntitiesAroundPoint(origin, radius + 2);

		List<Block> torrentblocks = new LinkedList<Block>();

		if (indexlist.contains(player)) {
			indexlist.remove(player);
		}

		List<Integer> toRemoveHeights = new LinkedList<Integer>();
		for (Entry<Integer, Map<Integer, Double>> entry : heights.entrySet()) {
			int id = entry.getKey();
			List<Integer> toRemoveAngles = new LinkedList<Integer>();
			Map<Integer, Double> angles = entry.getValue();
			for (Entry<Integer, Double> entryAngle : angles.entrySet()) {
				int index = entryAngle.getKey();
				double angle = entryAngle.getValue();
				double theta = Math.toRadians(angle);
				double dx = Math.cos(theta) * radius;
				double dy = id;
				double dz = Math.sin(theta) * radius;
				Location location = origin.clone().add(dx, dy, dz);
				Block block = location.getBlock();
				if (torrentblocks.contains(block))
					continue;
				if (BlockTools.isTransparentToEarthbending(player, torrentRegister, block)) {
					//TempBlock tempBlock = new TempBlock(block, Material.WATER, full);
					TempBlock tempBlock = TempBlock.makeTemporary(block, Material.WATER, false);
					blocks.add(tempBlock);
					torrentblocks.add(block);
				} else {
					toRemoveAngles.add(index);
					continue;
				}
				for (Entity entity : indexlist) {
					if (ProtectionManager.isEntityProtected(entity)) {
						continue;
					}
					if (!affectedentities.contains(entity)) {
						if (entity.getLocation().distance(location) <= 2) {
							affectedentities.add(entity);
							affect(entity);
						}
					}
				}
			}
			for (int angle : toRemoveAngles) {
				angles.remove(angle);
			}

			if (angles.isEmpty())
				toRemoveHeights.add(id);
		}

		for (int id : toRemoveHeights) {
			heights.remove(id);
		}

		if (heights.isEmpty())
			return false;

		return true;
	}

	private void affect(Entity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}
		Vector direction = Tools.getDirection(origin, entity.getLocation());
		direction.setY(0);
		direction.normalize();
		entity.setVelocity(entity.getVelocity().clone().add(direction.multiply(factor)));
	}

	private void clear() {
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
	}

	public void remove() {
		this.clear();
	}
}

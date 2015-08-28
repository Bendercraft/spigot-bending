package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Earth Tunnel", element=BendingType.Earth)
public class EarthTunnel implements IAbility {
	private static Map<Player, EarthTunnel> instances = new HashMap<Player, EarthTunnel>();

	@ConfigurationParameter("Max-Radius")
	private static double RADIUS = 1.0;
	
	@ConfigurationParameter("Range")
	private static double range = 9.0;
	
	@ConfigurationParameter("Radius")
	private static double radiusinc = 0.25;

	@ConfigurationParameter("Revert")
	private static boolean REVERT = true;

	@ConfigurationParameter("Interval")
	private static long INTERVAL = 30;

	private Player player;
	private Block block;
	private Location origin, location;
	private Vector direction;
	private double depth, radius, angle;
	private long time;
	private IAbility parent;

	public EarthTunnel(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		location = player.getEyeLocation().clone();
		origin = EntityTools.getTargetBlock(player, range).getLocation();
		block = origin.getBlock();
		direction = location.getDirection().clone().normalize();
		depth = origin.distance(location) - 1;
		if (depth < 0)
			depth = 0;
		angle = 0;
		radius = radiusinc;
		time = System.currentTimeMillis();
		
		instances.put(player, this);
	}
	
	private void remove() {
		instances.remove(player);
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (System.currentTimeMillis() - time >= INTERVAL) {
			time = System.currentTimeMillis();
			// Tools.verbose("progressing");
			if (Math.abs(Math.toDegrees(player.getEyeLocation().getDirection()
					.angle(direction))) > 20
					|| !player.isSneaking()) {
				return false;
			} else {
				while (!BlockTools.isEarthbendable(player, block)) {
					// Tools.verbose("going");
					if (!BlockTools.isTransparentToEarthbending(player, block)) {
						return false;
					}
					if (angle >= 360) {
						angle = 0;
						if (radius >= RADIUS) {
							radius = radiusinc;
							if (depth >= range) {
								return false;
							} else {
								depth += .5;
							}
						} else {
							radius += radiusinc;
						}
					} else {
						angle += 20;
					}
					// block.setType(Material.GLASS);
					Vector vec = Tools.getOrthogonalVector(direction, angle,
							radius);
					block = location.clone()
							.add(direction.clone().normalize().multiply(depth))
							.add(vec).getBlock();
				}

				if (REVERT) {
					BlockTools.addTempAirBlock(block);
				} else {
					block.breakNaturally();
				}

				return true;
			}
		}
		return true;
	}

	public static void progressAll() {
		List<EarthTunnel> toRemove = new LinkedList<EarthTunnel>();
		for(EarthTunnel tunnel : instances.values()) {
			boolean keep = tunnel.progress();
			if(!keep) {
				toRemove.add(tunnel);
			}
		}
		for(EarthTunnel tunnel : toRemove) {
			tunnel.remove();
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

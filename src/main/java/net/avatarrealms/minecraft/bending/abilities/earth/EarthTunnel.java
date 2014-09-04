package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EarthTunnel implements IAbility {
	private static Map<Player, EarthTunnel> instances = new HashMap<Player, EarthTunnel>();

	private static final double maxradius = ConfigManager.earthTunnelMaxRadius;
	private static final double range = ConfigManager.earthTunnelRange;
	private static final double radiusinc = ConfigManager.earthTunnelRadius;

	private static boolean revert = ConfigManager.earthTunnelRevert;

	private static final long interval = ConfigManager.earthTunnelInterval;

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
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		instances.put(player, this);
	}
	
	private void remove() {
		instances.remove(player);
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
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
						if (radius >= maxradius) {
							radius = radiusinc;
							if (depth >= range) {
								instances.remove(player);
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

				if (revert) {
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

	public static String getDescription() {
		return "Earth Tunnel is a completely utility ability for earthbenders. "
				+ "To use, simply sneak (default: shift) in the direction you want to tunnel. "
				+ "You will slowly begin tunneling in the direction you're facing for as long as you "
				+ "sneak or if the tunnel has been dug long enough. This ability will be interupted "
				+ "if it hits a block that cannot be earthbent.";
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

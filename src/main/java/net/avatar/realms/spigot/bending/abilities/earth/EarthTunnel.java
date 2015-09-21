package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name = "Earth Tunnel", bind = BendingAbilities.EarthTunnel, element = BendingElement.Earth)
public class EarthTunnel extends BendingActiveAbility {
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

	private Block block;
	private Location origin, location;
	private Vector direction;
	private double depth, radius, angle;
	private long time;

	public EarthTunnel(Player player) {
		super(player, null);
	}

	@Override
	public boolean sneak() {
		if (state == BendingAbilityState.CanStart) {
			return false;
		}
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

		AbilityManager.getManager().addInstance(this);
		state = BendingAbilityState.Progressing;
		return false;
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (System.currentTimeMillis() - time >= INTERVAL) {
			time = System.currentTimeMillis();
			// Tools.verbose("progressing");
			if (Math.abs(Math.toDegrees(player.getEyeLocation().getDirection().angle(direction))) > 20 || !player.isSneaking()) {
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
					Vector vec = Tools.getOrthogonalVector(direction, angle, radius);
					block = location.clone().add(direction.clone().normalize().multiply(depth)).add(vec).getBlock();
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

	@Override
	public Object getIdentifier() {
		return player;
	}

}

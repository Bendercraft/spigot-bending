package net.bendercraft.spigot.bending.abilities.earth;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.Tools;

@ABendingAbility(name = EarthTunnel.NAME, element = BendingElement.EARTH, canBeUsedWithTools = true)
public class EarthTunnel extends BendingActiveAbility {
	public final static String NAME = "EarthTunnel";
	
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

	public EarthTunnel(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.START) {
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

		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	public void progress() {
		if (System.currentTimeMillis() - time >= INTERVAL) {
			time = System.currentTimeMillis();
			// Tools.verbose("progressing");
			if (Math.abs(Math.toDegrees(player.getEyeLocation().getDirection().angle(direction))) > 20 || !player.isSneaking()) {
				remove();
				return;
			} else {
				while (!BlockTools.isEarthbendable(player, block)) {
					// Tools.verbose("going");
					if (!BlockTools.isTransparentToEarthbending(player, block)) {
						remove();
						return;
					}
					if (angle >= 360) {
						angle = 0;
						if (radius >= RADIUS) {
							radius = radiusinc;
							if (depth >= range) {
								remove();
								return;
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
			}
		}
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public void stop() {
		
	}

}

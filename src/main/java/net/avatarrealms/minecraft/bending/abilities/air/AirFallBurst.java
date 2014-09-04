package net.avatarrealms.minecraft.bending.abilities.air;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatarrealms.minecraft.bending.model.IAbility;

public class AirFallBurst implements IAbility  {
	private static double threshold = 10;
	private static double pushfactor = 1.5;
	private static double deltheta = 10;
	private static double delphi = 10;
	
	private IAbility parent;
	
	public AirFallBurst(Player player, IAbility parent) {
		this.parent = parent;
		if(player.getFallDistance() < threshold) {
			return;
		}
		Location location = player.getLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 75; theta < 105; theta += deltheta) {
			double dphi = delphi / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				new AirBlast(location, direction.normalize(), player,
						pushfactor, this);
			}
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

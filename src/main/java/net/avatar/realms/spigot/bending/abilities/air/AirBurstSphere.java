package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;

//TODO : Parent = AirBurst

@BendingAbility(name="Air Burst Sphere", element=BendingType.Air)
public class AirBurstSphere extends Ability  {

	public AirBurstSphere(Player player, Ability parent) {
		super(player, parent);
		if(!AirBurst.isAirBursting(player)) {
			return;
		}
		AirBurst burst = AirBurst.getAirBurst(player);
		if(!burst.isCharged()) {
			return;
		}

		Location location = player.getEyeLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += AirBurst.DELTHETA) {
			double dphi = AirBurst.DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				new AirBlast(location, direction.normalize(), player,
						AirBurst.PUSHFACTOR, this);
			}
		}
		burst.consume();
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.AirBurst;
	}

	@Override
	public Object getIdentifier() {
		return getParent().getIdentifier();
	}
}

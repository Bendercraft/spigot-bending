package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;

//TODO : Parent = AirBurst
@BendingAbility(name="Air Burst Cone", element=BendingType.Air)
public class AirBurstCone extends ActiveAbility  {

	public AirBurstCone(Player player, ActiveAbility parent) {
		super(player, parent);
		if(!AirBurst.isAirBursting(player)) {
			return;
		}
		AirBurst burst = AirBurst.getAirBurst(player);
		if(!burst.isCharged()) {
			return;
		}
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection();
		double angle = Math.toRadians(30);
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
				if (direction.angle(vector) <= angle) {
					// Tools.verbose(direction.angle(vector));
					// Tools.verbose(direction);
					new AirBlast(location, direction.normalize(), player,
							AirBurst.PUSHFACTOR, this);
				}
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

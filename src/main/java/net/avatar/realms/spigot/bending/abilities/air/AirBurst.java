package net.avatar.realms.spigot.bending.abilities.air;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * State Preparing : Player is sneaking but the Burst is not ready
 * State Prepared : Player is sneaking and the burst is ready
 *
 * @author Koudja
 */

@BendingAbility(name="Air Burst", element=BendingType.Air)
public class AirBurst extends ActiveAbility {

	@ConfigurationParameter("Charge-Time")
	public static long DEFAULT_CHARGETIME = 1750;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 2000;

	@ConfigurationParameter("Push-Factor")
	public static double PUSHFACTOR = 1.5;

	@ConfigurationParameter("Del-Theta")
	public static double DELTHETA = 10;

	@ConfigurationParameter("Del-Phi")
	public static double DELPHI = 10;
	
	@ConfigurationParameter ("Fall-Threshold")
	private static double THRESHOLD = 10;

	private long chargetime = DEFAULT_CHARGETIME;

	public AirBurst (Player player) {
		super(player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (AvatarState.isAvatarState(player)) {
			this.chargetime = (long) (DEFAULT_CHARGETIME / AvatarState.FACTOR);
		}
	}

	@Override
	public boolean sneak () {
		if (this.state.equals(AbilityState.CanStart)) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Preparing);
			return false;
		}

		return false;
	}

	@Override
	public boolean swing () {
		if (this.state == AbilityState.Prepared) {
			coneBurst();
			return false;
		}

		return true;
	}
	
	@Override
	public boolean fall () {
		if (this.player.getFallDistance() < THRESHOLD) {
			return false;
		}

		fallBurst();
		
		return true;
	}

	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}

		if ((EntityTools.getBendingAbility(this.player) != Abilities.AirBurst)) {
			return false;
		}

		if (!this.player.isSneaking()) {
			if (this.state.equals(AbilityState.Prepared)) {
				sphereBurst();
			}
			return false;
		}

		if (!this.state.equals(AbilityState.Prepared) && (System.currentTimeMillis() > (this.startedTime + this.chargetime))) {
			setState(AbilityState.Prepared);
		}

		if (this.state == AbilityState.Prepared) {
			Location location = this.player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.SMOKE, Tools.getIntCardinalDirection(location.getDirection()), 3);
		}
		return true;
	}

	public static boolean isAirBursting (Player player) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.AirBurst);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}
		return instances.containsKey(player);
	}

	public boolean isCharged() {
		return (this.state == AbilityState.Prepared);
	}

	public static AirBurst getAirBurst (Player player) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.AirBurst);
		if ((instances == null) || instances.isEmpty()) {
			return null;
		}
		if (!instances.containsKey(player)) {
			return null;
		}
		return (AirBurst) instances.get(player);
	}

	private void sphereBurst () {
		Location location = this.player.getEyeLocation();
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
				new AirBlast(location, direction.normalize(), this.player, AirBurst.PUSHFACTOR, this);
			}
		}
		setState(AbilityState.Ended);
	}
	
	private void coneBurst () {
		Location location = this.player.getEyeLocation();
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
					new AirBlast(location, direction.normalize(), this.player, AirBurst.PUSHFACTOR, this);
				}
			}
		}
		setState(AbilityState.Ended);
	}
	
	private void fallBurst () {
		Location location = this.player.getLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 75; theta < 105; theta += AirBurst.DELTHETA) {
			double dphi = AirBurst.DELTHETA / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				new AirBlast(location, direction.normalize(), this.player, AirBurst.PUSHFACTOR, this);
			}
		}
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.AirBurst;
	}

	@Override
	protected long getMaxMillis () {
		return 60 * 10 * 1000;
	}

	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (isAirBursting(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}
}

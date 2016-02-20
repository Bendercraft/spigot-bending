package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@ABendingAbility(name = Shockwave.NAME, element = BendingElement.EARTH)
public class Shockwave extends BendingActiveAbility {
	public final static String NAME = "Shockwave";
	
	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 2500;
	@ConfigurationParameter("Fall-Threshold")
	public static double FALL_THRESHOLD = 9;
	@ConfigurationParameter("Cone-Angle")
	private static double CONE_ANGLE = 40;

	private long starttime;
	private long chargetime = CHARGE_TIME;
	private double angle;

	private List<Ripple> ripples = new LinkedList<Ripple>();

	public Shockwave(RegisteredAbility register, Player player) {
		super(register, player);

		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;

		angle = Math.toRadians(CONE_ANGLE);
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.PREPARED) {
			double dtheta = 360. / (2 * Math.PI * Ripple.RADIUS) - 1;
			for (double theta = 0; theta < 360; theta += dtheta) {
				double rtheta = Math.toRadians(theta);
				Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
				if (vector.angle(player.getEyeLocation().getDirection()) < angle)
					ripples.add(new Ripple(player, vector.normalize()));
			}
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!EntityTools.canBend(player, NAME) ||
				(!bender.getAbility().equals(NAME) && !getState().equals(BendingAbilityState.PROGRESSING))) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if (getState() == BendingAbilityState.PREPARING) {
			if (!player.isSneaking()) {
				remove();
				return;
			}
			if (System.currentTimeMillis() > starttime + chargetime) {
				setState(BendingAbilityState.PREPARED);
			}
		} else if (getState() == BendingAbilityState.PREPARED) {
			Location location = player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.SMOKE, Tools.getIntCardinalDirection(player.getEyeLocation().getDirection()), 3);

			if (!player.isSneaking() || (player.getFallDistance() < Shockwave.FALL_THRESHOLD && !BlockTools.isEarthbendable(player, player.getLocation().add(0, -1, 0).getBlock()))) {
				// Area - either because unsneak or falling
				double dtheta = 360. / (2 * Math.PI * Ripple.RADIUS) - 1;
				for (double theta = 0; theta < 360; theta += dtheta) {
					double rtheta = Math.toRadians(theta);
					Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
					ripples.add(new Ripple(player, vector.normalize()));
				}
				setState(BendingAbilityState.PROGRESSING);
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
			List<Ripple> toRemove = new LinkedList<Ripple>();
			for (Ripple ripple : ripples) {
				if (!ripple.progress()) {
					toRemove.add(ripple);
				}
			}
			ripples.removeAll(toRemove);
			if (ripples.isEmpty()) {
				remove();
				return;
			}
		} else {
			remove();
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

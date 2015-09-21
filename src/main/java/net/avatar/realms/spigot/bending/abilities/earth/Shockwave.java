package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Shockwave", element=BendingType.Earth)
public class Shockwave extends ActiveAbility {
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

	public Shockwave(Player player) {
		super(player, null);
		
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		
		angle = Math.toRadians(CONE_ANGLE);
	}
	
	@Override
	public boolean swing() {
		if(state == AbilityState.Prepared) {
			double dtheta = 360. / (2 * Math.PI * Ripple.RADIUS) - 1;
			for (double theta = 0; theta < 360; theta += dtheta) {
				double rtheta = Math.toRadians(theta);
				Vector vector = new Vector(Math.cos(rtheta), 0,
						Math.sin(rtheta));
				if (vector.angle(player.getEyeLocation().getDirection()) < angle)
					ripples.add(new Ripple(player, vector.normalize()));
			}
			state = AbilityState.Progressing;
		}
		return false;
	}
	
	@Override
	public boolean sneak() {
		if(state == AbilityState.CanStart) {
			AbilityManager.getManager().addInstance(this);
			state = AbilityState.Preparing;
		}
		return false;
	}

	@Override
	public boolean progress() {
		if(!super.progress()) {
			return false;
		}
		if (!EntityTools.canBend(player, Abilities.Shockwave)
				|| EntityTools.getBendingAbility(player) != Abilities.Shockwave) {
			return false;
		}
		
		if(state == AbilityState.Preparing) {
			if (!player.isSneaking()) {
				return false;
			}
			if (System.currentTimeMillis() > starttime + chargetime) {
				state = AbilityState.Prepared;
			}
		} else if(state == AbilityState.Prepared) {
			Location location = player.getEyeLocation();
			location.getWorld().playEffect(
					location,
					Effect.SMOKE,
					Tools.getIntCardinalDirection(player.getEyeLocation()
							.getDirection()), 3);
			
			if(!player.isSneaking() || (player.getFallDistance() < Shockwave.FALL_THRESHOLD 
					&& !BlockTools.isEarthbendable(player, player.getLocation().add(0, -1, 0).getBlock()))) {
				// Area - either because unsneak or falling
				double dtheta = 360. / (2 * Math.PI * Ripple.RADIUS) - 1;
				for (double theta = 0; theta < 360; theta += dtheta) {
					double rtheta = Math.toRadians(theta);
					Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
					ripples.add(new Ripple(player, vector.normalize()));
				}
				state = AbilityState.Progressing;
			}
		} else if(state == AbilityState.Progressing) {
			List<Ripple> toRemove = new LinkedList<Ripple>();
			for(Ripple ripple : ripples) {
				if(!ripple.progress()) {
					toRemove.add(ripple);
				}
			}
			ripples.removeAll(toRemove);
			if(ripples.isEmpty()) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.Shockwave;
	}

}

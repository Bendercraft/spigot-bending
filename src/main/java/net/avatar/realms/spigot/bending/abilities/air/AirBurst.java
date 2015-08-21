package net.avatar.realms.spigot.bending.abilities.air;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * AirBurst is just an utility abilities, it does no damage or whatever, only providing a way to check if a player has charged
 * Classes AirSphereBurst, AirConeBurst, AirFallBurst consumes charge and remove it
 * 
 * @author Koudja
 *
 */

@BendingAbility(name="Air Burst", element=BendingType.Air)
public class AirBurst extends Ability {

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

	private long chargetime = DEFAULT_CHARGETIME;
	private boolean charged = false;

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

		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}

		if (this.state.equals(AbilityState.CanStart)) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Progressing);
			return false;
		}



		return false;
	}

	@Override
	public boolean progress () {

		if (!super.progress()) {
			return false;
		}

		if (!EntityTools.canBend(this.player, Abilities.AirBurst)
				|| (EntityTools.getBendingAbility(this.player) != Abilities.AirBurst)) {
			return false;
		}

		if (!this.player.isSneaking()) {
			return false;
		}

		if ((System.currentTimeMillis() > (this.startedTime + this.chargetime)) && !this.charged) {
			this.charged = true;
		}

		if (this.charged) {
			Location location = this.player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			location.getWorld().playEffect(
					location,
					Effect.SMOKE,
					Tools.getIntCardinalDirection(this.player.getEyeLocation()
							.getDirection()), 3);
		}
		return true;
	}

	public static boolean isAirBursting (Player player) {
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.AirBurst);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}
		return instances.containsKey(player);
	}

	public void consume () {
		setState(AbilityState.Ended);
	}

	public boolean isCharged() {
		return this.charged;
	}

	public static AirBurst getAirBurst (Player player) {
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.AirBurst);
		if ((instances == null) || instances.isEmpty()) {
			return null;
		}
		if (!instances.containsKey(player)) {
			return null;
		}
		return (AirBurst) instances.get(player);
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

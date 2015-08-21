package net.avatar.realms.spigot.bending.abilities.air;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Air Shield", element=BendingType.Air)
public class AirShield extends Ability {

	@ConfigurationParameter("Max-Radius")
	private static double MAX_RADIUS = 5.0;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 5 * 60 * 1000;


	private static int numberOfStreams = (int)(.75 * MAX_RADIUS);

	private double radius = 2;

	private double speedfactor;

	private Map<Integer, Integer> angles = new HashMap<Integer, Integer>();

	public AirShield (Player player) {
		super(player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		int angle = 0;
		int di = (int)((MAX_RADIUS * 2) / numberOfStreams);
		for (int i = -(int)MAX_RADIUS + di ; i < (int)MAX_RADIUS ; i += di) {
			this.angles.put(i, angle);
			angle += 90;
			if (angle == 360) {
				angle = 0;
			}
		}
		this.speedfactor = 1;
	}

	@Override
	public boolean swing() {

		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}

		if (AvatarState.isAvatarState(this.player)) {
			if (this.state == AbilityState.CanStart) {
				AbilityManager.getManager().addInstance(this);
				setState(AbilityState.Progressing);
			}
			else if (this.state == AbilityState.Progressing) {
				setState(AbilityState.Ended);
			}
		}

		return false;
	}

	@Override
	public boolean sneak () {

		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}

		if (this.state == AbilityState.CanStart) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Progressing);
			return false;
		}

		if (this.state == AbilityState.Progressing) {
			return false;
		}

		return true;
	}

	private void rotateShield () {
		Location origin = this.player.getLocation();

		FireBlast.removeFireBlastsAroundPoint(origin, this.radius);

		for (Entity entity : EntityTools.getEntitiesAroundPoint(origin, this.radius)) {
			if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}

			if ((entity instanceof ExperienceOrb) || (entity instanceof FallingBlock) || (entity instanceof ItemFrame)
					|| (entity instanceof Item)) {
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.AirShield, entity.getLocation())) {
				continue;
			}

			if (entity instanceof Player) {
				entity.setFireTicks(0);
			}

			if (origin.distance(entity.getLocation()) > 2) {
				double x, z, vx, vz, mag;
				double angle = 50;
				angle = Math.toRadians(angle);

				x = entity.getLocation().getX() - origin.getX();
				z = entity.getLocation().getZ() - origin.getZ();

				mag = Math.sqrt((x * x) + (z * z));

				vx = ((x * Math.cos(angle)) - (z * Math.sin(angle))) / mag;
				vz = ((x * Math.sin(angle)) + (z * Math.cos(angle))) / mag;

				Vector velocity = entity.getVelocity();
				if (AvatarState.isAvatarState(this.player)) {
					velocity.setX(AvatarState.getValue(vx));
					velocity.setZ(AvatarState.getValue(vz));
				}
				else {
					velocity.setX(vx);
					velocity.setZ(vz);
				}

				velocity.multiply(this.radius / MAX_RADIUS);
				entity.setVelocity(velocity);
				entity.setFallDistance(0);
			}
		}

		Set<Integer> keys = this.angles.keySet();
		for (int i : keys) {
			double x, y, z;
			double angle = this.angles.get(i);
			angle = Math.toRadians(angle);

			double factor = this.radius / MAX_RADIUS;

			y = origin.getY() + (factor * i);

			// double theta = Math.asin(y/radius);
			double f = Math.sqrt(1 - (factor * factor * (i / this.radius) * (i / this.radius)));

			x = origin.getX() + (this.radius * Math.cos(angle) * f);
			z = origin.getZ() + (this.radius * Math.sin(angle) * f);

			Location effect = new Location(origin.getWorld(), x, y, z);
			if (!ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.AirShield, effect)) {
				origin.getWorld().playEffect(effect, Effect.SMOKE, 4, (int)AirBlast.DEFAULT_RANGE);
			}

			this.angles.put(i, this.angles.get(i) + (int)(10 * this.speedfactor));
		}

		if (this.radius < MAX_RADIUS) {
			this.radius += .3;
		}

		if (this.radius > MAX_RADIUS) {
			this.radius = MAX_RADIUS;
		}

	}

	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}

		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		if (((EntityTools.getBendingAbility(this.player) != Abilities.AirShield) || (!this.player.isSneaking()))
				&& !AvatarState.isAvatarState(this.player)) {
			return false;
		}

		rotateShield();
		return true;
	}

	@Override
	public void remove () {
		this.bender.cooldown(Abilities.AirShield, COOLDOWN);
		super.remove();
	}

	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (isShielded(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	protected long getMaxMillis () {
		return MAX_DURATION;
	}

	public static boolean isShielded (Player player) {
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.AirShield);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		return instances.containsKey(player);
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.AirShield;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

}

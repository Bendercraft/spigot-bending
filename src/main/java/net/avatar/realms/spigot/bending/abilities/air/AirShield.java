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

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.DamageTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = AirShield.NAME, element = BendingElement.AIR)
public class AirShield extends BendingActiveAbility {
	public final static String NAME = "AirShield";

	@ConfigurationParameter("Max-Radius")
	private static double MAX_RADIUS = 5.0;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 5 * 60 * 1000L;

	private int numberOfStreams = (int) (.75 * MAX_RADIUS);

	private double radius = 2;
	private double maxRadius = MAX_RADIUS;

	private double speedfactor;

	private Map<Integer, Integer> angles = new HashMap<Integer, Integer>();

	public AirShield(RegisteredAbility register, Player player) {
		super(register, player);

		int angle = 0;
		int di = (int) ((maxRadius * 2) / numberOfStreams);
		for (int i = -(int) maxRadius + di; i < (int) maxRadius; i += di) {
			this.angles.put(i, angle);
			angle += 90;
			if (angle == 360) {
				angle = 0;
			}
		}
		this.speedfactor = 1;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (isShielded(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean swing() {
		if (AvatarState.isAvatarState(this.player)) {
			if (getState() == BendingAbilityState.START) {
				setState(BendingAbilityState.PROGRESSING);
			} else if (getState() == BendingAbilityState.PROGRESSING) {
				remove();
			}
		}

		return false;
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PROGRESSING);
			return false;
		}

		if (getState() == BendingAbilityState.PROGRESSING) {
			return false;
		}

		return true;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		if ((!NAME.equals(EntityTools.getBendingAbility(player)) || !this.player.isSneaking()) 
				&& !AvatarState.isAvatarState(this.player)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		Location origin = this.player.getLocation();

		FireBlast.removeFireBlastsAroundPoint(origin, this.radius);

		for (Entity entity : EntityTools.getEntitiesAroundPoint(origin, this.radius)) {
			if (ProtectionManager.isEntityProtected(entity)
					|| (entity instanceof ExperienceOrb) 
					|| (entity instanceof FallingBlock) 
					|| (entity instanceof ItemFrame) 
					|| (entity instanceof Item)
					|| ProtectionManager.isLocationProtectedFromBending(this.player, register, entity.getLocation())) {
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
				} else {
					velocity.setX(vx);
					velocity.setZ(vz);
				}

				velocity.multiply(this.radius / maxRadius);

				if (bender.hasPath(BendingPath.RENEGADE)) {
					DamageTools.damageEntity(bender, entity, 1);
					velocity.multiply(2);
				}
				entity.setVelocity(velocity);
				entity.setFallDistance(0);

				if (bender.hasPath(BendingPath.RENEGADE)) {
					remove();
					return;
				}
			}
		}

		Set<Integer> keys = this.angles.keySet();
		for (int i : keys) {
			double x, y, z;
			double angle = this.angles.get(i);
			angle = Math.toRadians(angle);

			double factor = this.radius / maxRadius;

			y = origin.getY() + (factor * i);

			double f = Math.sqrt(1 - (factor * factor * (i / this.radius) * (i / this.radius)));

			x = origin.getX() + (this.radius * Math.cos(angle) * f);
			z = origin.getZ() + (this.radius * Math.sin(angle) * f);

			Location effect = new Location(origin.getWorld(), x, y, z);
			if (!ProtectionManager.isLocationProtectedFromBending(this.player, register, effect)) {
				origin.getWorld().playEffect(effect, Effect.SMOKE, 4, (int) AirBlast.DEFAULT_RANGE);
			}

			this.angles.put(i, this.angles.get(i) + (int) (10 * this.speedfactor));
		}

		if (this.radius < maxRadius) {
			this.radius += .3;
		}

		if (this.radius > maxRadius) {
			this.radius = maxRadius;
		}
	}

	@Override
	public void stop() {
		long cooldown = COOLDOWN;
		if (bender.hasPath(BendingPath.RENEGADE)) {
			cooldown *= 1.2;
		}
		this.bender.cooldown(NAME, cooldown);
	}


	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

	public static boolean isShielded(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		return instances.containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

package net.avatar.realms.spigot.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Air Shield", element=BendingType.Air)
public class AirShield implements IAbility {

	private static Map<Integer, AirShield> instances = new HashMap<Integer, AirShield>();

	@ConfigurationParameter("Max-Radius")
	private static double MAX_RADIUS = 5.0;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;
	
	
	private static int numberOfStreams = (int)(.75 * (double)MAX_RADIUS);

	private double radius = 2;

	private double speedfactor;

	private Player player;
	private Map<Integer, Integer> angles = new HashMap<Integer, Integer>();
	private IAbility parent;

	public AirShield (Player player, IAbility parent) {
		this.parent = parent;
		if (AvatarState.isAvatarState(player) && instances.containsKey(player.getEntityId())) {
			instances.remove(player.getEntityId());
			return;
		}
		this.player = player;
		int angle = 0;
		int di = (int)((MAX_RADIUS * 2) / numberOfStreams);
		for (int i = -(int)MAX_RADIUS + di ; i < (int)MAX_RADIUS ; i += di) {
			this.angles.put(i, angle);
			angle += 90;
			if (angle == 360) {
				angle = 0;
			}
		}

		instances.put(player.getEntityId(), this);
		BendingPlayer.getBendingPlayer(this.player).cooldown(Abilities.AirShield, COOLDOWN);
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
			double angle = (double)this.angles.get(i);
			angle = Math.toRadians(angle);

			double factor = this.radius / MAX_RADIUS;

			y = origin.getY() + (factor * (double)i);

			// double theta = Math.asin(y/radius);
			double f = Math.sqrt(1 - (factor * factor * ((double)i / this.radius) * ((double)i / this.radius)));

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

	public boolean progress () {
		if (this.player.isDead() || !this.player.isOnline()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.AirShield, this.player.getLocation())) {
			return false;
		}
		this.speedfactor = 1;
		if (!EntityTools.canBend(this.player, Abilities.AirShield) || this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		if (((EntityTools.getBendingAbility(this.player) != Abilities.AirShield) || (!this.player.isSneaking()))
				&& !AvatarState.isAvatarState(this.player)) {
			return false;
		}
		rotateShield();
		return true;
	}

	public static void progressAll () {
		List<AirShield> toRemove = new LinkedList<AirShield>();
		for (AirShield shield : instances.values()) {
			boolean keep = shield.progress();
			if (!keep) {
				toRemove.add(shield);
			}
		}

		for (AirShield shield : toRemove) {
			shield.remove();
		}
	}

	private void remove () {
		instances.remove(this.player.getEntityId());
	}

	public static void removeAll () {
		instances.clear();
	}

	@Override
	public IAbility getParent () {
		return this.parent;
	}

}

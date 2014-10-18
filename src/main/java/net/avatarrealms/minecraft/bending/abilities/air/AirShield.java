package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirShield implements IAbility {

	private static Map<Integer, AirShield> instances = new HashMap<Integer, AirShield>();

	private static double maxradius = ConfigManager.airShieldRadius;
	private static int numberOfStreams = (int) (.75 * (double) maxradius);

	private double radius = 2;

	private double speedfactor;

	private Player player;
	private Map<Integer, Integer> angles = new HashMap<Integer, Integer>();
	private IAbility parent;

	public AirShield(Player player, IAbility parent) {
		this.parent = parent;
		if (AvatarState.isAvatarState(player)
				&& instances.containsKey(player.getEntityId())) {
			instances.remove(player.getEntityId());
			return;
		}
		this.player = player;
		int angle = 0;
		int di = (int) (maxradius * 2 / numberOfStreams);
		for (int i = -(int) maxradius + di; i < (int) maxradius; i += di) {
			angles.put(i, angle);
			angle += 90;
			if (angle == 360)
				angle = 0;
		}

		instances.put(player.getEntityId(), this);
		BendingPlayer.getBendingPlayer(this.player).cooldown(Abilities.AirShield);
	}

	private void rotateShield() {
		Location origin = player.getLocation();
		
		FireBlast.removeFireBlastsAroundPoint(origin, radius);

		for (Entity entity : EntityTools.getEntitiesAroundPoint(origin, radius)) {
			
			if ((entity instanceof ExperienceOrb) || (entity instanceof FallingBlock)
					|| (entity instanceof ItemFrame) || (entity instanceof Item)) {
				continue;
			}
			
			if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirShield,
					entity.getLocation())){
				continue;
			}
				
			if (origin.distance(entity.getLocation()) > 2) {
				double x, z, vx, vz, mag;
				double angle = 50;
				angle = Math.toRadians(angle);

				x = entity.getLocation().getX() - origin.getX();
				z = entity.getLocation().getZ() - origin.getZ();

				mag = Math.sqrt(x * x + z * z);

				vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
				vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

				Vector velocity = entity.getVelocity();
				if (AvatarState.isAvatarState(player)) {
					velocity.setX(AvatarState.getValue(vx));
					velocity.setZ(AvatarState.getValue(vz));
				} else {
					velocity.setX(vx);
					velocity.setZ(vz);
				}

				velocity.multiply(radius / maxradius);
				entity.setVelocity(velocity);
				entity.setFallDistance(0);
			}
		}
		
		Set<Integer> keys = angles.keySet();
		for (int i : keys) {
			double x, y, z;
			double angle = (double) angles.get(i);
			angle = Math.toRadians(angle);

			double factor = radius / maxradius;

			y = origin.getY() + factor * (double) i;

			// double theta = Math.asin(y/radius);
			double f = Math.sqrt(1 - factor * factor * ((double) i / radius)
					* ((double) i / radius));

			x = origin.getX() + radius * Math.cos(angle) * f;
			z = origin.getZ() + radius * Math.sin(angle) * f;

			Location effect = new Location(origin.getWorld(), x, y, z);
			if (!ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirShield,
					effect)) {
				origin.getWorld().playEffect(effect, Effect.SMOKE, 4,
						(int) AirBlast.defaultrange);
			}
				

			angles.put(i, angles.get(i) + (int) (10 * speedfactor));
		}

		if (radius < maxradius) {
			radius += .3;
		}

		if (radius > maxradius) {
			radius = maxradius;
		}
			

	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirShield,
				player.getLocation())) {
			return false;
		}
		speedfactor = 1;
		if (!EntityTools.canBend(player, Abilities.AirShield)
				|| player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		if (((EntityTools.getBendingAbility(player) != Abilities.AirShield) || (!player
				.isSneaking())) && !AvatarState.isAvatarState(player)) {
			return false;
		}
		rotateShield();
		return true;
	}
	
	public static void progressAll() {
		List<AirShield> toRemove = new LinkedList<AirShield>();
		for(AirShield shield : instances.values()) {
			boolean keep = shield.progress();
			if(!keep) {
				toRemove.add(shield);
			}
		}
		
		for(AirShield shield : toRemove) {
			shield.remove();
		}
	}
	
	private void remove() {
		instances.remove(this.player.getEntityId());
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}
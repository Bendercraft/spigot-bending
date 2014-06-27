package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Shockwave implements IAbility {
	private static Map<Player, Shockwave> instances = new HashMap<Player, Shockwave>();

	private static final double angle = Math.toRadians(40);
	private static final long defaultchargetime = 2500;
	private static final double threshold = 10;

	private Player player;
	private long starttime;
	private long chargetime = defaultchargetime;
	private boolean charged = false;
	private IAbility parent;

	public Shockwave(Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player))
			return;
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);

	}

	public static void fallShockwave(Player player) {
		if (!EntityTools.canBend(player, Abilities.Shockwave)
				|| EntityTools.getBendingAbility(player) != Abilities.Shockwave
				|| instances.containsKey(player)
				|| player.getFallDistance() < threshold
				|| !BlockTools.isEarthbendable(player,
						player.getLocation().add(0, -1, 0).getBlock())) {
			return;
		}
		areaShockwave(player);
	}
	
	private void remove() {
		instances.remove(player);
	}

	private boolean progress() {
		if (!EntityTools.canBend(player, Abilities.Shockwave)
				|| EntityTools.getBendingAbility(player) != Abilities.Shockwave) {
			return false;
		}
		if (System.currentTimeMillis() > starttime + chargetime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				areaShockwave(player);
			}
			return false;
		} else if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			location.getWorld().playEffect(
					location,
					Effect.SMOKE,
					Tools.getIntCardinalDirection(player.getEyeLocation()
							.getDirection()), 3);
		}
		
		return true;
	}

	public static void progressAll() {
		List<Shockwave> toRemove = new LinkedList<Shockwave>();
		for (Shockwave shockwave : instances.values()) {
			boolean keep = shockwave.progress();
			if(!keep) {
				toRemove.add(shockwave);
			}
		}
		for (Shockwave shockwave : toRemove) {
			shockwave.remove();
		}
		Ripple.progressAll();
	}

	private static void areaShockwave(Player player) {
		double dtheta = 360. / (2 * Math.PI * Ripple.radius) - 1;
		for (double theta = 0; theta < 360; theta += dtheta) {
			double rtheta = Math.toRadians(theta);
			Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
			//TODO HEY SHOCKWAVE HERE
			new Ripple(player, vector.normalize(), null);
		}
	}

	public static void coneShockwave(Player player) {
		if (instances.containsKey(player)) {
			if (instances.get(player).charged) {
				double dtheta = 360. / (2 * Math.PI * Ripple.radius) - 1;
				for (double theta = 0; theta < 360; theta += dtheta) {
					double rtheta = Math.toRadians(theta);
					Vector vector = new Vector(Math.cos(rtheta), 0,
							Math.sin(rtheta));
					if (vector.angle(player.getEyeLocation().getDirection()) < angle)
						//TODO HEY SHOCKWAVE HERE
						new Ripple(player, vector.normalize(), null);
				}
				instances.remove(player);
			}
		}
	}

	public static String getDescription() {
		return "This is one of the most powerful moves in the earthbender's arsenal. "
				+ "To use, you must first charge it by holding sneak (default: shift). "
				+ "Once charged, you can release sneak to create an enormous shockwave of earth, "
				+ "disturbing all earth around you and expanding radially outwards. "
				+ "Anything caught in the shockwave will be blasted back and dealt damage. "
				+ "If you instead click while charged, the disruption is focused in a cone in front of you. "
				+ "Lastly, if you fall from a great enough height with this ability selected, you will automatically create a shockwave.";
	}

	public static void removeAll() {
		instances.clear();
		Ripple.removeAll();
	}

	@Override
	public int getBaseExperience() {
		return 11;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

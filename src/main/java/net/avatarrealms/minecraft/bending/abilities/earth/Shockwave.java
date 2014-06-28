package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Shockwave implements IAbility {
	private static Map<Player, Shockwave> instances = new HashMap<Player, Shockwave>();

	private static final long defaultchargetime = 2500;
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
	
	public void remove() {
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

		if (charged) {
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
		return 0;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
	
	public boolean isCharged() {
		return charged;
	}
	
	public static boolean isShockwaving(Player player) {
		return instances.containsKey(player);
	}
	
	public static Shockwave getShockwave(Player player) {
		return instances.get(player);
	}

}

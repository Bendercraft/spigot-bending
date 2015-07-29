package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@BendingAbility(name="Shockwave", element=BendingType.Earth)
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
		if (!player.isSneaking()) {
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

	public static void removeAll() {
		instances.clear();
		Ripple.removeAll();
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

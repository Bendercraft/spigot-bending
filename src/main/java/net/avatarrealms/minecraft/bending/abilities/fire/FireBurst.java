package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FireBurst implements IAbility {
	private static Map<Player, FireBurst> instances = new HashMap<Player, FireBurst>();

	private Player player;
	private long starttime;
	private long chargetime = 2500;
	private boolean charged = false;
	private IAbility parent;

	public FireBurst(Player player, IAbility parent) {
		this.parent = parent;
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.FireBurst))
			return;
		if (instances.containsKey(player))
			return;
		starttime = System.currentTimeMillis();
		if (Tools.isDay(player.getWorld())) {
			chargetime /= ConfigManager.dayFactor;
		}
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);
	}
	
	public void remove() {
		instances.remove(player);
	}

	private boolean progress() {
		if (!EntityTools.canBend(player, Abilities.FireBurst)
				|| EntityTools.getBendingAbility(player) != Abilities.FireBurst) {
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
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES,
					4, 3);
		}
		return true;
	}

	public static void progressAll() {
		List<FireBurst> toRemove = new LinkedList<FireBurst>();
		for (FireBurst burst : instances.values()) {
			boolean keep = burst.progress();
			if(!keep) {
				toRemove.add(burst);
			}
		}
		
		for (FireBurst burst : toRemove) {
			burst.remove();
		}
	}

	public static String getDescription() {
		return "FireBurst is a very powerful firebending ability. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of flames in front of you, or click to release the burst in a sphere around you. ";
	}

	public static void removeAll() {
		instances.clear();
	}
	
	public boolean isCharged() {
		return charged;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
	
	public static boolean isFireBursting(Player player) {
		return instances.containsKey(player);
	}
	public static FireBurst getFireBurst(Player player) {
		return instances.get(player);
	}
}

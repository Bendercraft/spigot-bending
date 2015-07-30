package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@BendingAbility(name="Fire Burst", element=BendingType.Fire)
public class FireBurst implements IAbility {
	private static Map<Player, FireBurst> instances = new HashMap<Player, FireBurst>();
	
	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 2500;
	
	@ConfigurationParameter("Damage")
	  		static int DAMAGE = 3;
	
	@ConfigurationParameter("Del-Theta")
			static double DELTHETA = 10;
	
	@ConfigurationParameter("Del-Phi")
			static double DELPHI = 10;

	private Player player;
	private long starttime;
	private long chargetime = CHARGE_TIME;
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
			chargetime /= Settings.DAY_FACTOR;
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

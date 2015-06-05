package net.avatar.realms.spigot.bending.abilities.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class AvatarState {

	public static Map<Player, AvatarState> instances = new HashMap<Player, AvatarState>();
	private static List<Player> toRemove = new ArrayList<Player>();

	private static final double factor = 4.5;

	private static final long duration = ConfigManager.avatarstateDuration;

	private Player player;
	private BendingPlayer bPlayer;
	private long startedTime;

	public AvatarState (Player player) {
		if (instances.containsKey(player)) {
			instances.remove(player);
		}
		else {
			this.bPlayer = BendingPlayer.getBendingPlayer(player);
			if (this.bPlayer.isOnCooldown(Abilities.AvatarState)) {
				return;
			}
			new Flight(player);
			instances.put(player, this);
			this.player = player;
			this.startedTime = System.currentTimeMillis();
		}
	}

	public static void progressAll () {
		for (Player player : instances.keySet()) {
			AvatarState as = instances.get(player);
			boolean keep = as.progress();
			if (!keep) {
				as.bPlayer.cooldown(Abilities.AvatarState);
				toRemove.add(player);
			}
		}
		for (Player pl : toRemove) {
			instances.remove(pl);
		}
		toRemove.clear();
	}

	public boolean progress () {
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.AvatarState, this.player.getLocation())) {
			return false;
		}
		if (!EntityTools.canBend(this.player, Abilities.AvatarState)) {
			return false;
		}
		long now = System.currentTimeMillis();
		if ((now - this.startedTime) > duration) {
			return false;
		}
		addPotionEffects();
		return true;
	}

	private void addPotionEffects () {
		int duration = 70;
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0));
	}

	public static boolean isAvatarState (Player player) {
		if (instances.containsKey(player)) {
			return true;
		}
		return false;
	}

	public static double getValue (double value) {
		return factor * value;
	}

	public static int getValue (int value) {
		return (int)factor * value;
	}

	public static ArrayList<Player> getPlayers () {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}
}

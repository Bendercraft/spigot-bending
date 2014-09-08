package net.avatarrealms.minecraft.bending.abilities.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AvatarState{

	public static Map<Player, AvatarState> instances = new HashMap<Player, AvatarState>();
	private static List<Player> toRemove = new ArrayList<Player>();

	private static final double factor = 5;

	private Player player;

	// boolean canfly = false;

	public AvatarState(Player player) {
		this.player = player;
		if (instances.containsKey(player)) {
			instances.remove(player);
		} else {
			new Flight(player);
			instances.put(player, this);
		}
	}
	
	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
		for (Player pl : toRemove) {
			instances.remove(pl);
		}
		toRemove.clear();
	}

	public boolean progress() {
		if (!EntityTools.canBend(player, Abilities.AvatarState)) {
			toRemove.add(player);
			return false;
		}
		addPotionEffects();
		return true;
	}

	private void addPotionEffects() {
		int duration = 70;
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
				duration, 3));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
				duration, 2));
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.DAMAGE_RESISTANCE, duration, 2));
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.FIRE_RESISTANCE, duration, 2));
	}

	public static boolean isAvatarState(Player player) {
		if (instances.containsKey(player))
			return true;
		return false;
	}

	public static double getValue(double value) {
		return factor * value;
	}

	public static int getValue(int value) {
		return (int) factor * value;
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}
}

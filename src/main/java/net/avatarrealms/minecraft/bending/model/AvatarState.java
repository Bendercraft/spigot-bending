package net.avatarrealms.minecraft.bending.model;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.business.Tools;
import net.avatarrealms.minecraft.bending.controller.Flight;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AvatarState implements IAbility{

	public static ConcurrentHashMap<Player, AvatarState> instances = new ConcurrentHashMap<Player, AvatarState>();

	private static final double factor = 5;

	Player player;

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
	}

	public boolean progress() {
		if (!Tools.canBend(player, Abilities.AvatarState)) {
			instances.remove(player);
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

	public static String getDescription() {
		return "The signature ability of the Avatar, this is a toggle. Click to activate to become "
				+ "nearly unstoppable. While in the Avatar State, the user takes severely reduced damage from "
				+ "all sources, regenerates health rapidly, and is granted extreme speed. Nearly all abilities "
				+ "are incredibly amplified in this state. Additionally, AirShield and FireJet become toggle-able "
				+ "abilities and last until you deactivate them or the Avatar State. Click again with the Avatar "
				+ "State selected to deactivate it.";
	}

}

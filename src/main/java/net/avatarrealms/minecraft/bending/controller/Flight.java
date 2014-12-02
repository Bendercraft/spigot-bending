package net.avatarrealms.minecraft.bending.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.air.AirScooter;
import net.avatarrealms.minecraft.bending.abilities.air.AirSpout;
import net.avatarrealms.minecraft.bending.abilities.air.Speed;
import net.avatarrealms.minecraft.bending.abilities.air.Tornado;
import net.avatarrealms.minecraft.bending.abilities.earth.Catapult;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.fire.FireJet;
import net.avatarrealms.minecraft.bending.abilities.water.Bloodbending;
import net.avatarrealms.minecraft.bending.abilities.water.WaterSpout;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class Flight {

	private static Map<Player, Flight> instances = new HashMap<Player, Flight>();

	private static long duration = 5000;

	private Player player = null, source = null;
	private boolean couldFly = false, wasFlying = false;
	private long time;

	public Flight(Player player) {
		this(player, null);
	}

	public Flight(Player player, Player source) {

		if (instances.containsKey(player)) {
			Flight flight = instances.get(player);
			flight.refresh(source);
			instances.put(player, flight);
			return;
		}

		couldFly = player.getAllowFlight();
		wasFlying = player.isFlying();
		this.player = player;
		this.source = source;
		time = System.currentTimeMillis();
		instances.put(player, this);
	}

	public static Player getLaunchedBy(Player player) {
		if (instances.containsKey(player)) {
			return instances.get(player).source;
		}

		return null;
	}

	private void revert() {
		player.setAllowFlight(couldFly);
		player.setFlying(wasFlying);
	}

	private void remove() {
		revert();
		instances.remove(player);
	}

	private void refresh(Player source) {
		this.source = source;
		time = System.currentTimeMillis();
		instances.put(player, this);
	}

	public static void handle() {
		List<Player> players = new LinkedList<Player>();
		List<Player> newflyingplayers = new LinkedList<Player>();
		List<Player> avatarstateplayers = new LinkedList<Player>();
		List<Player> airscooterplayers = new LinkedList<Player>();
		List<Player> waterspoutplayers = new LinkedList<Player>();
		List<Player> airspoutplayers = AirSpout.getPlayers();

		players.addAll(Tornado.getPlayers());
		players.addAll(Speed.getPlayers());
		players.addAll(FireJet.getPlayers());
		players.addAll(Catapult.getPlayers());
		avatarstateplayers = AvatarState.getPlayers();
		airscooterplayers = AirScooter.getPlayers();
		waterspoutplayers = WaterSpout.getPlayers();

		List<Flight> toRemove = new LinkedList<Flight>();
		for (Player player : instances.keySet()) {
			Flight flight = instances.get(player);
			if (avatarstateplayers.contains(player)
					|| airscooterplayers.contains(player)
					|| waterspoutplayers.contains(player)
					|| airspoutplayers.contains(player)) {
				continue;
			}
			if (Bloodbending.isBloodbended(player)) {
				player.setAllowFlight(true);
				player.setFlying(false);
				continue;
			}

			if (players.contains(player)) {
				flight.refresh(null);
				player.setAllowFlight(true);
				if (player.getGameMode() != GameMode.CREATIVE) {
					player.setFlying(false);
				}
				newflyingplayers.add(player);
				continue;
			}

			if (flight.source == null) {
				flight.revert();
				toRemove.add(flight);
			} else {
				if (System.currentTimeMillis() > flight.time + duration) {
					toRemove.add(flight);
				}
			}
		}
		
		for(Flight flight : toRemove) {
			flight.remove();
		}

	}

	public static void removeAll() {
		for (Flight flight : instances.values()) {
			if (flight.source != null) {
				flight.revert();
			}
		}
		instances.clear();
	}

}

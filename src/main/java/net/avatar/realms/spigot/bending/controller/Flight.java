package net.avatar.realms.spigot.bending.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.air.AirScooter;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.earth.Catapult;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;

@Deprecated
//Use Flying Player instead
public class Flight {
	
	private static Map<UUID, Flight> instances = new HashMap<UUID, Flight>();
	
	private static long duration = 5000;
	
	private Player source = null;
	private Player player = null;
	private boolean couldFly = false;
	private boolean wasFlying = false;
	private long time;
	
	public Flight (Player player) {
		this(player, null);
	}
	
	public Flight (Player player, Player source) {
		if (instances.containsKey(player)) {
			Flight flight = instances.get(player);
			flight.refresh(source);
			return;
		}
		
		this.couldFly = player.getAllowFlight();
		this.wasFlying = player.isFlying();
		this.player = player;
		this.time = System.currentTimeMillis();
		instances.put(player.getUniqueId(), this);
	}
	
	private void revert () {
		this.player.setAllowFlight(this.couldFly);
		this.player.setFlying(this.wasFlying);
	}
	
	private void remove () {
		revert();
		instances.remove(this.player);
	}
	
	private void refresh (Player source) {
		this.time = System.currentTimeMillis();
		instances.put(this.player.getUniqueId(), this);
	}
	
	public static void handle () {
		List<Player> players = new LinkedList<Player>();
		List<Player> newflyingplayers = new LinkedList<Player>();
		List<Player> avatarstateplayers = new LinkedList<Player>();
		List<Player> airscooterplayers = new LinkedList<Player>();
		List<Player> waterspoutplayers = new LinkedList<Player>();
		List<Player> airspoutplayers = AirSpout.getPlayers();
		
		players.addAll(Tornado.getPlayers());
		//players.addAll(Speed.getPlayers());
		players.addAll(FireJet.getPlayers());
		players.addAll(Catapult.getPlayers());
		avatarstateplayers = AvatarState.getPlayers();
		airscooterplayers = AirScooter.getPlayers();
		waterspoutplayers = WaterSpout.getPlayers();
		
		List<Flight> toRemove = new LinkedList<Flight>();
		for (UUID player : instances.keySet()) {
			Flight flight = instances.get(player);
			if (avatarstateplayers.contains(player) || airscooterplayers.contains(player) || waterspoutplayers.contains(player)
					|| airspoutplayers.contains(player)) {
				continue;
			}
			if (Bloodbending.isBloodbended(flight.player)) {
				flight.player.setAllowFlight(true);
				flight.player.setFlying(false);
				continue;
			}
			
			if (players.contains(player)) {
				flight.refresh(null);
				flight.player.setAllowFlight(true);
				if (flight.player.getGameMode() != GameMode.CREATIVE) {
					flight.player.setFlying(false);
				}
				newflyingplayers.add(flight.player);
				continue;
			}
			
			if (flight.source == null) {
				flight.revert();
				toRemove.add(flight);
			}
			else {
				if (System.currentTimeMillis() > (flight.time + duration)) {
					toRemove.add(flight);
				}
			}
		}
		
		for (Flight flight : toRemove) {
			flight.remove();
		}
	}
	
	public static void removeAll () {
		for (Flight flight : instances.values()) {
			if (flight.source != null) {
				flight.revert();
			}
		}
		instances.clear();
	}
	
	public static boolean revert (Player player) {
		if (player == null) {
			return false;
		}
		
		Flight flight = instances.get(player.getUniqueId());
		
		if (flight == null) {
			return false;
		}
		
		flight.remove();
		
		return true;
	}
	
}

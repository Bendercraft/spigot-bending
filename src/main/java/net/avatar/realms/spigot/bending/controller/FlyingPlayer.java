package net.avatar.realms.spigot.bending.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;

// I continue to use an extern class to handle flying because there can be many reasons to be flying
// : WaterSpout and Tornado for example.
// If your were WaterSpouting when a tornado comes against you, there may be a conflict giving the
// flying to a player
public class FlyingPlayer {
	
	private static Map<UUID, FlyingPlayer> flyingPlayers = new HashMap<UUID, FlyingPlayer>();

	private Player player;
	private boolean couldFly;
	private boolean wasFlying;
	private List<Abilities> causes;
	
	private FlyingPlayer (Player player) {
		this.player = player;
		this.couldFly = player.getAllowFlight();
		this.wasFlying = player.isFlying();
		this.causes = new LinkedList<Abilities>();
	}
	
	private void fly () {
		this.player.setAllowFlight(true);
		this.player.setFlying(true);
	}
	
	private void resetState () {
		this.player.setAllowFlight(this.couldFly);
		this.player.setFlying(this.wasFlying);
	}
	
	public boolean addCause (Abilities cause) {
		if (this.causes == null) {
			return false;
		}

		this.causes.add(cause);
		return true;
	}
	
	public boolean hasCauses () {
		if (this.causes == null) {
			return false;
		}
		
		if (this.causes.isEmpty()) {
			return false;
		}
		
		return true;
	}

	public boolean hasCause (Abilities cause) {
		if (this.causes == null) {
			return false;
		}
		
		if (this.causes.isEmpty()) {
			return false;
		}

		return this.causes.contains(cause);
	}

	public void removeCause (Abilities cause) {
		if (this.causes == null) {
			return;
		}

		if (this.causes.contains(cause)) {
			this.causes.remove(cause);
		}
	}
	
	public static void addFlyingPlayer (Player player, Abilities cause) {
		if ((player == null) || (cause == null)) {
			return;
		}

		FlyingPlayer flying = null;
		if (flyingPlayers.containsKey(player.getUniqueId())) {
			flying = flyingPlayers.get(player.getUniqueId());
			if (flying == null) {
				flying = new FlyingPlayer(player);
			}
		}
		else {
			flying = new FlyingPlayer(player);
		}

		flying.addCause(cause);
		if (flying.hasCauses()) {
			flyingPlayers.put(player.getUniqueId(), flying);
			flying.fly();
		}
	}
	
	public static void removeFlyingPlayer (Player player, Abilities cause) {
		if (!flyingPlayers.containsKey(player.getUniqueId())) {
			return;
		}

		FlyingPlayer flying = flyingPlayers.get(player.getUniqueId());
		if (flying == null) {
			flyingPlayers.remove(player.getUniqueId());
			return;
		}

		if (flying.hasCause(cause)) {
			flying.removeCause(cause);
			if (!flying.hasCauses()) {
				flying.resetState();
				flyingPlayers.remove(player.getUniqueId());
			}
		}
	}

}

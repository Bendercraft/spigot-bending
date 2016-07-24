package net.bendercraft.spigot.bending.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;

public class FlyingPlayer {

	private static Map<UUID, FlyingPlayer> flyingPlayers = new HashMap<UUID, FlyingPlayer>();

	private Player player;
	private Map<BendingAbility, Long> causes;

	private FlyingPlayer(Player player) {
		this.player = player;
		this.causes = new HashMap<BendingAbility, Long>();
	}

	private void fly(boolean fly) {
		this.player.setAllowFlight(true);
		this.player.setFlying(fly);
	}

	private void resetState() {
		this.player.setAllowFlight(false);
		this.player.setFlying(false);
	}

	private boolean addCause(BendingAbility cause, Long maxDuration) {
		if (this.causes == null) {
			return false;
		}

		this.causes.put(cause, maxDuration);
		return true;
	}

	private boolean hasCauses() {
		if (this.causes == null) {
			return false;
		}

		return !this.causes.isEmpty();
	}

	private boolean hasCause(BendingAbility cause) {
		if (this.causes == null) {
			return false;
		}

		if (this.causes.isEmpty()) {
			return false;
		}

		return this.causes.containsKey(cause);
	}

	private void removeCause(BendingAbility cause) {
		if (this.causes == null) {
			return;
		}

		if (this.causes.containsKey(cause)) {
			this.causes.remove(cause);
		}
	}

	public static FlyingPlayer addFlyingPlayer(Player player, BendingAbility cause, Long maxDuration, boolean fly) {
		if ((player == null) || (cause == null)) {
			return null;
		}

		FlyingPlayer flying = null;
		if (flyingPlayers.containsKey(player.getUniqueId())) {
			flying = flyingPlayers.get(player.getUniqueId());
		} else {
			flying = new FlyingPlayer(player);
		}

		flying.addCause(cause, System.currentTimeMillis() + maxDuration);
		flyingPlayers.put(player.getUniqueId(), flying);
		flying.fly(fly);
		return flying;
	}

	public static void removeFlyingPlayer(Player player, BendingAbility cause) {
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

	private boolean handle() {
		long now = System.currentTimeMillis();
		List<BendingAbility> toRemove = new LinkedList<BendingAbility>();
		for (BendingAbility ab : this.causes.keySet()) {
			if (now > this.causes.get(ab) || ab.getState() == BendingAbilityState.ENDED) {
				toRemove.add(ab);
			}
		}

		for (BendingAbility ab : toRemove) {
			this.causes.remove(ab);
		}

		return hasCauses();
	}

	public static void handleAll() {
		List<UUID> toRemove = new LinkedList<UUID>();
		for (UUID id : flyingPlayers.keySet()) {
			if (!flyingPlayers.get(id).handle()) {
				toRemove.add(id);
			}
		}

		for (UUID id : toRemove) {
			flyingPlayers.get(id).resetState();
			flyingPlayers.remove(id);
		}
	}

	public static boolean isFlying(Player p) {
		return flyingPlayers.containsKey(p.getUniqueId());
	}
}

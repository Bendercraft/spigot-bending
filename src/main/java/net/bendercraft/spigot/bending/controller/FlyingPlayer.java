package net.bendercraft.spigot.bending.controller;

import java.util.*;

import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;

public class FlyingPlayer {
/* When dealing with flying players in Minecraft, there is two important booleans :
	1. allowFlight : allows the player to fly by double-jumping like in creative mode.
		Whatever the ability, we want to set this boolean to true as the server will not try to kick players for cheating.
		We prevent the players from actually using double-jumping in BendingPlayerListener.
	2. flying : Make the player fly, giving him the ability to go up and down with space and sneak respectively.

	Some abilities (like FireJet) do not set flying to true, while others (like AirSpout) do.
	It is important for us to keep track of which ability sets this flag to true in order to revoke the fly
	whenever it is not allowed anymore, even if other abilities still make the player considered as a FlyingPlayer.
 */
	private static Map<UUID, FlyingPlayer> flyingPlayers = new HashMap<UUID, FlyingPlayer>();

	private Player player;
	private List<FlyingCause> causes;

	private class FlyingCause {
		BendingAbility ability;
		Long endTime;
		boolean setFlying; //If this FlyingCause actually sets player.setFlying(true)

		FlyingCause(BendingAbility ability, Long endTime, boolean setFlying) {
			this.ability = ability;
			this.endTime = endTime;
			this.setFlying = setFlying;
		}
	}

	private FlyingPlayer(Player player) {
		this.player = player;
		this.causes = new LinkedList<>();
	}

	private void updatePermission() {
		this.player.setAllowFlight(true);
		this.player.setFlying(hasOneSetFlyingCause());
	}

	private void resetState() {
		this.player.setAllowFlight(false);
		this.player.setFlying(false);
	}

	private void addCause(BendingAbility cause, Long endTime, boolean setFlying) {
		this.causes.add(new FlyingCause(cause, endTime, setFlying));
		this.updatePermission();
	}

	private boolean hasCauses() {
		return !this.causes.isEmpty();
	}

	private boolean hasOneSetFlyingCause() {
		for (FlyingCause cause : this.causes) {
			if (cause.setFlying) {
				return true;
			}
		}
		return false;
	}

	private void removeCause(BendingAbility abilityCause) {
		this.causes.removeIf(cause -> cause.ability.equals(abilityCause));
		this.updatePermission();
	}

	public static FlyingPlayer addFlyingPlayer(Player player, BendingAbility cause, Long maxDuration, boolean setFlying) {
		if ((player == null) || (cause == null)) {
			return null;
		}

		FlyingPlayer fp;
		if (flyingPlayers.containsKey(player.getUniqueId())) {
			fp = flyingPlayers.get(player.getUniqueId());
		} else {
			fp = new FlyingPlayer(player);
		}

		fp.addCause(cause, System.currentTimeMillis() + maxDuration, setFlying);
		flyingPlayers.put(player.getUniqueId(), fp);
		return fp;
	}

	public static void removeFlyingPlayer(Player player, BendingAbility cause) {
		if (!flyingPlayers.containsKey(player.getUniqueId())) {
			return;
		}

		FlyingPlayer fp = flyingPlayers.get(player.getUniqueId());
		if (fp == null) {
			flyingPlayers.remove(player.getUniqueId());
			return;
		}

		fp.removeCause(cause);
		if (!fp.hasCauses()) {
			fp.resetState();
			flyingPlayers.remove(player.getUniqueId());
		}
	}

	private void handle() {
		long now = System.currentTimeMillis();

		causes.removeIf(cause -> now > cause.endTime || cause.ability.getState() == BendingAbilityState.ENDED);
		updatePermission();
	}

	public static void handleAll() {
		Iterator<UUID> it = flyingPlayers.keySet().iterator();
		while (it.hasNext()) {
			FlyingPlayer fp = flyingPlayers.get(it.next());
			fp.handle();
			if (!fp.hasCauses()) {
				fp.resetState();
				it.remove();
			}
		}
	}

	public static boolean isFlying(Player p) {
		return flyingPlayers.containsKey(p.getUniqueId());
	}

}

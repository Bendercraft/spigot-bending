package net.avatarrealms.minecraft.bending.model.fire;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.business.Tools;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.data.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FireJet {

	public static ConcurrentHashMap<Player, FireJet> instances = new ConcurrentHashMap<Player, FireJet>();
	private static final double defaultfactor = ConfigManager.fireJetSpeed;
	private static final long defaultduration = ConfigManager.fireJetDuration;
	// private static final long cooldown = ConfigManager.fireJetCooldown;

	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();

	private Player player;
	// private boolean canfly;
	private long time;
	private long duration = defaultduration;
	private double factor = defaultfactor;

	public FireJet(Player player) {
		if (instances.containsKey(player)) {
			// player.setAllowFlight(canfly);
			instances.remove(player);
			return;
		}
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player)
		// + (long) ((double) cooldown / Tools
		// .getFirebendingDayAugment(player.getWorld()))) {
		// return;
		// }
		// }
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.FireJet))
			return;

		factor = Tools.firebendingDayAugment(defaultfactor, player.getWorld());
		Block block = player.getLocation().getBlock();
		if (FireStream.isIgnitable(player, block)
				|| block.getType() == Material.AIR
				|| AvatarState.isAvatarState(player)) {
			player.setVelocity(player.getEyeLocation().getDirection().clone()
					.normalize().multiply(factor));
			block.setType(Material.FIRE);
			this.player = player;
			// canfly = player.getAllowFlight();
			new Flight(player);
			player.setAllowFlight(true);
			time = System.currentTimeMillis();
			// timers.put(player, time);
			instances.put(player, this);
			bPlayer.cooldown(Abilities.FireJet);
		}

	}

	public static boolean checkTemporaryImmunity(Player player) {
		if (instances.containsKey(player)) {
			return true;
		}
		return false;
	}

	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			// player.setAllowFlight(canfly);
			instances.remove(player);
			return;
		}
		if ((Tools.isWater(player.getLocation().getBlock()) || System
				.currentTimeMillis() > time + duration)
				&& !AvatarState.isAvatarState(player)) {
			// player.setAllowFlight(canfly);
			instances.remove(player);
		} else {
			player.getWorld().playEffect(player.getLocation(),
					Effect.MOBSPAWNER_FLAMES, 1);
			double timefactor;
			if (AvatarState.isAvatarState(player)) {
				timefactor = 1;
			} else {
				timefactor = 1 - ((double) (System.currentTimeMillis() - time))
						/ (2.0 * duration);
			}
			Vector velocity = player.getEyeLocation().getDirection().clone()
					.normalize().multiply(factor * timefactor);
			// Vector velocity = player.getVelocity().clone();
			// velocity.add(player.getEyeLocation().getDirection().clone()
			// .normalize().multiply(factor * timefactor));
			player.setVelocity(velocity);
			player.setFallDistance(0);
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}

	public static String getDescription() {
		return "This ability is used for a limited burst of flight for firebenders. Clicking with this "
				+ "ability selected will launch you in the direction you're looking, granting you "
				+ "controlled flight for a short time. This ability can be used mid-air to prevent falling "
				+ "to your death, but on the ground it can only be used if standing on a block that's "
				+ "ignitable (e.g. not snow or water).";
	}

}

package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FireJet implements IAbility {
	private static Map<Player, FireJet> instances = new HashMap<Player, FireJet>();
	private static final double defaultfactor = ConfigManager.fireJetSpeed;
	private static final long defaultduration = ConfigManager.fireJetDuration;

	private Player player;
	private long time;
	private long duration = defaultduration;
	private double factor = defaultfactor;
	private IAbility parent;

	public FireJet(Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player)) {
			instances.remove(player);
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.FireJet))
			return;

		factor = PluginTools.firebendingDayAugment(defaultfactor, player.getWorld());
		Block block = player.getLocation().getBlock();
		if (FireStream.isIgnitable(player, block)
				|| block.getType() == Material.AIR
				|| AvatarState.isAvatarState(player)) {
			player.setVelocity(player.getEyeLocation().getDirection().clone()
					.normalize().multiply(factor));
			//block.setType(Material.FIRE);
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
	
	private void remove() {
		instances.remove(player);
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if ((BlockTools.isWater(player.getLocation().getBlock()) || System
				.currentTimeMillis() > time + duration)
				&& !AvatarState.isAvatarState(player)) {
			return false;
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
			player.setVelocity(velocity);
			player.setFallDistance(0);
		}
		return true;
	}

	public static void progressAll() {
		List<FireJet> toRemove = new LinkedList<FireJet>();
		for (FireJet jet : instances.values()) {
			boolean keep = jet.progress();
			if(!keep) {
				toRemove.add(jet);
			}
		}
		
		for (FireJet jet : toRemove) {
			jet.remove();
		}
	}

	public static List<Player> getPlayers() {
		return new LinkedList<Player>(instances.keySet());
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

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

	public static String getDescription() {
		return "This ability is used for a limited burst of flight for firebenders. Clicking with this "
				+ "ability selected will launch you in the direction you're looking, granting you "
				+ "controlled flight for a short time. This ability can be used mid-air to prevent falling "
				+ "to your death, but on the ground it can only be used if standing on a block that's "
				+ "ignitable (e.g. not snow or water).";
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public int getBaseExperience() {
		return 2;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

package net.avatarrealms.minecraft.bending.model.air;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempPotionEffect;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Speed {

	public static ConcurrentHashMap<Integer, Speed> instances = new ConcurrentHashMap<Integer, Speed>();

	private Player player;
	private int id;

	// private boolean canfly = false;

	public Speed(Player player) {
		this.player = player;
		id = player.getEntityId();
		// canfly = player.getAllowFlight();
		new Flight(player);
		player.setAllowFlight(true);
		instances.put(id, this);
	}

	public boolean progress() {
		// if (player.isFlying() && player.getGameMode() != GameMode.CREATIVE
		// && !AirScooter.getPlayers().contains(player)
		// && !AvatarState.isAvatarState(player))
		// player.setFlying(false);
		if (player.isSprinting()
				&& Tools.isBender(player.getName(), BendingType.Air)
				&& Tools.canBendPassive(player, BendingType.Air)) {
			applySpeed();
			return true;
		}
		if (player.isSprinting()
				&& Tools.isBender(player.getName(), BendingType.ChiBlocker)) {
			applySpeed();
			return true;
		}
		// player.setAllowFlight(canfly);
		instances.remove(id);
		return false;
	}

	private void applySpeed() {
		int factor = 0;
		if (Tools.isBender(player.getName(), BendingType.Air)
				&& Tools.canBendPassive(player, BendingType.Air)) {
			factor = 1;
		}
		int jumpfactor = factor + 1;
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70,
				factor);
		PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70,
				jumpfactor);
		// player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 70,
		// factor));
		new TempPotionEffect(player, speed);
		if (Tools.getBendingAbility(player) != Abilities.AirScooter)
			new TempPotionEffect(player, jump);
		// player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 70,
		// jumpfactor));
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (int id : instances.keySet()) {
			Player player = instances.get(id).player;
			if (player.isSprinting()) {
				players.add(instances.get(id).player);
			} else {
				instances.remove(id);
			}
		}
		return players;
	}

}

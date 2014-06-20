package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempPotionEffect;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Speed {

	public static ConcurrentHashMap<Integer, Speed> instances = new ConcurrentHashMap<Integer, Speed>();

	private Player player;
	private int id;


	public Speed(Player player) {
		this.player = player;
		id = player.getEntityId();
		new Flight(player);
		player.setAllowFlight(true);
		instances.put(id, this);
	}

	public boolean progress() {
		if (player.isSprinting()
				&& EntityTools.isBender(player, BendingType.Air)
				&& EntityTools.canBendPassive(player, BendingType.Air)) {
			applySpeed();
			return true;
		}
		if (player.isSprinting()
				&& EntityTools.isBender(player, BendingType.ChiBlocker)) {
			applySpeed();
			return true;
		}
		instances.remove(id);
		return false;
	}

	private void applySpeed() {
		int factor = 0;
		if (EntityTools.isBender(player, BendingType.Air)
				&& EntityTools.canBendPassive(player, BendingType.Air)) {
			factor = 1;
		}
		int jumpfactor = factor + 1;
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70,
				factor);
		PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70,
				jumpfactor);

		new TempPotionEffect(player, speed);
		if (EntityTools.getBendingAbility(player) != Abilities.AirScooter)
			new TempPotionEffect(player, jump);

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

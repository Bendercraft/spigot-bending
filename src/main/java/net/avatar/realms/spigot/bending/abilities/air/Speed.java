package net.avatar.realms.spigot.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Speed implements IPassiveAbility {

	private static Map<Integer, Speed> instances = new HashMap<Integer, Speed>();

	private Player player;
	private int id;


	public Speed(Player player) {
		if (ProtectionManager.isRegionProtectedFromBendingPassives(player, player.getLocation())) {
			return;
		}
		this.player = player;
		id = player.getEntityId();
		new Flight(player);
		player.setAllowFlight(true);
		instances.put(id, this);
	}

	public boolean progress() {
		if (ProtectionManager.isRegionProtectedFromBendingPassives(player, player.getLocation())) {
			return false;
		}
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
		return false;
	}
	
	private void remove() {
		instances.remove(id);
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

	public static void progressAll() {
		List<Speed> toRemove = new LinkedList<Speed>();
		for(Speed speed : instances.values()) {
			boolean keep = speed.progress();
			if(!keep) {
				toRemove.add(speed);
			}
		}
		
		for(Speed speed : toRemove) {
			speed.remove();
		}
	}

	public static List<Player> getPlayers() {
		List<Player> players = new LinkedList<Player>();
		List<Integer> toRemove = new LinkedList<Integer>();
		for (Entry<Integer, Speed> entry : instances.entrySet()) {
			Player player = entry.getValue().player;
			if (player.isSprinting()) {
				players.add(player);
			} else {
				toRemove.add(entry.getKey());
			}
		}
		
		for(int id : toRemove) {
			instances.remove(id);
		}
		
		return players;
	}

	public static void removeAll() {
		instances.clear();
	}

}

package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class RapidPunch {
	private static int damage = ConfigManager.rapidPunchDamage;
	private int distance = ConfigManager.rapidPunchDistance;
	private static int punches = ConfigManager.rapidPunchPunches;
	
	private static Map<Player, RapidPunch> instances = new HashMap<Player, RapidPunch>();
	private int numpunches;

	private Player player;
	private Entity target;
	public static List<Player> punching = new ArrayList<Player>();

	public RapidPunch(Player p) {// , Entity t) {
		if (instances.containsKey(p))
			return;

		if (BendingPlayer.getBendingPlayer(p)
				.isOnCooldown(Abilities.RapidPunch))
			return;

		Entity t = EntityTools.getTargettedEntity(p, distance);

		if (t == null)
			return;

		target = t;
		numpunches = 0;
		player = p;
		instances.put(p, this);
	}

	public boolean progress() {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (numpunches >= punches) {
			return false;
		}
		if (numpunches%2 == 0) {
			if ((target instanceof Player) ||(target instanceof Monster)) {	
				if (bPlayer != null) {
					bPlayer.earnXP(BendingType.ChiBlocker);
				}
			}
		}
					
		if (target != null && target instanceof LivingEntity) {
			LivingEntity lt = (LivingEntity) target;
			EntityTools.damageEntity(player, target, bPlayer.getCriticalHit(BendingType.ChiBlocker,damage));
			if (target instanceof Player)
				EntityTools.blockChi((Player) target, System.currentTimeMillis());
			lt.setNoDamageTicks(0);
		}
		BendingPlayer.getBendingPlayer(player).cooldown(Abilities.RapidPunch);
		numpunches++;
		return true;
	}
	
	public static void progressAll() {
		List<RapidPunch> toRemove = new LinkedList<RapidPunch>();
		for (RapidPunch punch : instances.values()) {
			boolean keep = punch.progress();
			if(!keep) {
				toRemove.add(punch);
			}
		}
		for (RapidPunch punch : toRemove) {
			punch.remove();
		}
	}
	
	private void remove() {
		instances.remove(player);
	}

	public static void removeAll() {
		instances.clear();
	}

	public static String getDescription() {
		return "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch."
				+ " This has a short cooldown.";
	}

}
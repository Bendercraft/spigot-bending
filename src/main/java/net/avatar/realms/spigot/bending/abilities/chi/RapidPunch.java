package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RapidPunch implements IAbility {
	private static int damage = ConfigManager.rapidPunchDamage;
	private int distance = ConfigManager.rapidPunchDistance;
	private static int punches = ConfigManager.rapidPunchPunches;
	
	private static Map<Player, RapidPunch> instances = new HashMap<Player, RapidPunch>();
	private int numpunches;

	private Player player;
	private Entity target;
	public static List<Player> punching = new ArrayList<Player>();
	private IAbility parent;

	public RapidPunch(Player p, IAbility parent) {
		this.parent = parent;
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
		BendingPlayer.getBendingPlayer(player).cooldown(Abilities.RapidPunch);
	}

	public boolean progress() {
		if (numpunches >= punches) {
			return false;
		}
					
		if (target != null && target instanceof LivingEntity) {
			LivingEntity lt = (LivingEntity) target;
			EntityTools.damageEntity(player, target, damage);
			if (target instanceof Player)
				EntityTools.blockChi((Player) target, System.currentTimeMillis());
			lt.setNoDamageTicks(0);
		}
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

	@Override
	public IAbility getParent() {
		return parent;
	}

}
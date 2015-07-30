package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * 
 * This ability will be modified : 
 * When you hit an entity, you deal some damage to it.
 * The more you hit, the more you deal damage. (But the more big the cooldown will be)
 *
 */
@BendingAbility(name="Rapid Punch", element=BendingType.ChiBlocker)
public class RapidPunch implements IAbility {
	
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 7;
	
	@ConfigurationParameter("Range")
	public static int RANGE = 4;
	
	@ConfigurationParameter("Punches")
	private static int punches = 4;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;
	
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

		Entity t = EntityTools.getTargettedEntity(p, RANGE);

		if (t == null)
			return;

		target = t;
		numpunches = 0;
		player = p;
		instances.put(p, this);
		BendingPlayer.getBendingPlayer(player).cooldown(Abilities.RapidPunch, COOLDOWN);
	}

	public boolean progress() {
		if (numpunches >= punches) {
			return false;
		}
					
		if (target != null && target instanceof LivingEntity) {
			LivingEntity lt = (LivingEntity) target;
			EntityTools.damageEntity(player, target, DAMAGE);
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
package model.chiblocking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import model.Abilities;
import model.BendingPlayer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dataAccess.ConfigManager;
import business.Tools;

public class RapidPunch {

	private static int damage = ConfigManager.rapidPunchDamage;
	private int distance = ConfigManager.rapidPunchDistance;
	// private long cooldown = ConfigManager.rapidPunchCooldown;
	private static int punches = ConfigManager.rapidPunchPunches;

	// private static Map<String, Long> cooldowns = new HashMap<String, Long>();
	public static ConcurrentHashMap<Player, RapidPunch> instance = new ConcurrentHashMap<Player, RapidPunch>();
	private int numpunches;
	// private long timers;
	private Entity target;
	public static List<Player> punching = new ArrayList<Player>();

	public RapidPunch(Player p) {// , Entity t) {
		if (instance.containsKey(p))
			return;
		// if (cooldowns.containsKey(p.getName())
		// && cooldowns.get(p.getName()) + cooldown >= System
		// .currentTimeMillis())
		// return;

		if (BendingPlayer.getBendingPlayer(p)
				.isOnCooldown(Abilities.RapidPunch))
			return;

		Entity t = Tools.getTargettedEntity(p, distance);

		if (t == null)
			return;

		target = t;
		numpunches = 0;
		instance.put(p, this);
		// Tools.verbose("PUNCH MOFO");
	}

	public void startPunch(Player p) {
		if (numpunches >= punches)
			instance.remove(p);
		if (target instanceof LivingEntity && target != null) {
			LivingEntity lt = (LivingEntity) target;
			Tools.damageEntity(p, target, damage);
			if (target instanceof Player)
				Tools.blockChi((Player) target, System.currentTimeMillis());
			lt.setNoDamageTicks(0);
			// Tools.verbose("PUNCHIN MOFO");
		}
		// cooldowns.put(p.getName(), System.currentTimeMillis());
		BendingPlayer.getBendingPlayer(p).cooldown(Abilities.RapidPunch);
		swing(p);
		numpunches++;
	}

	private void swing(Player p) {
		// punching.add(p);
		// timers = System.currentTimeMillis();
		// Packet18ArmAnimation packet = new Packet18ArmAnimation();
		// packet.a = p.getEntityId();
		// packet.b = (byte) 1;
		// for (Player observer : p.getWorld().getPlayers())
		// ((CraftPlayer) observer).getHandle().netServerHandler
		// .sendPacket(packet);
	}

	public static String getDescription() {
		return "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch."
				+ " This has a short cooldown.";
	}

}
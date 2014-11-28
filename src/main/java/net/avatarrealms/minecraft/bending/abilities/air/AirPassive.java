package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.HashMap;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.IPassiveAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class AirPassive implements IPassiveAbility {

	private static Map<Player, Float> foodlevels = new HashMap<Player, Float>();
	private static float factor = ConfigManager.airPassiveFactor;

	private static float getFoodExhaustionLevel(Player player, float level) {
		if (!foodlevels.keySet().contains(player)) {
			foodlevels.put(player, level);
			return level;
		} else {
			float oldlevel = foodlevels.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				level = (level - oldlevel) * factor + oldlevel;
			}
			foodlevels.put(player, level);
			return level;
		}
	}

	public static void handlePassive(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			if (EntityTools.isBender(player, BendingType.Air)) {
				player.setExhaustion(getFoodExhaustionLevel(player,
						player.getExhaustion()));
			}
		}
	}
}

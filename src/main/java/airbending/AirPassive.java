package airbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import tools.BendingType;
import tools.ConfigManager;
import tools.Tools;

public class AirPassive {

	private static ConcurrentHashMap<Player, Float> foodlevels = new ConcurrentHashMap<Player, Float>();
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
			foodlevels.replace(player, level);
			return level;
		}
	}

	public static void handlePassive(Server server) {
		for (World world : server.getWorlds()) {
			for (Player player : world.getPlayers()) {
				if (Tools.isBender(player.getName(), BendingType.Air)) {
					player.setExhaustion(getFoodExhaustionLevel(player,
							player.getExhaustion()));
				}
			}
		}
	}

}

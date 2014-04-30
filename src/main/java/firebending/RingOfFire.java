package firebending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tools.Abilities;
import tools.AvatarState;
import tools.BendingPlayer;
import tools.ConfigManager;

public class RingOfFire {

	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();
	// static final long soonesttime = Tools.timeinterval;

	static final int defaultrange = ConfigManager.ringOfFireRange;

	public RingOfFire(Player player) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + soonesttime) {
		// return;
		// }
		// }
		// timers.put(player, System.currentTimeMillis());
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Blaze))
			return;

		Location location = player.getLocation();

		for (double degrees = 0; degrees < 360; degrees += 10) {
			double angle = Math.toRadians(degrees);
			Vector direction = player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			int range = defaultrange;
			if (AvatarState.isAvatarState(player))
				range = AvatarState.getValue(range);

			new FireStream(location, direction, player, range);
		}

		bPlayer.cooldown(Abilities.Blaze);
	}

	public static String getDescription() {
		return "To use, simply left-click. "
				+ "A circle of fire will emanate from you, "
				+ "engulfing everything around you. Use with extreme caution.";
	}

}

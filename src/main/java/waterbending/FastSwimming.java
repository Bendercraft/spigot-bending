package waterbending;

import java.util.Arrays;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import tools.Abilities;
import tools.BendingType;
import tools.ConfigManager;
import tools.TempBlock;
import tools.Tools;

public class FastSwimming {

	// private static Map<Player, Location> locations = new HashMap<Player,
	// Location>();
	// private static Map<Player, Long> timers = new HashMap<Player, Long>();
	// private static long interval = ConfigManager.fastSwimmingInterval;
	private static double factor = ConfigManager.fastSwimmingFactor;

	private static final Abilities[] shiftabilities = {
			Abilities.WaterManipulation, Abilities.Surge,
			Abilities.HealingWaters, Abilities.PhaseChange,
			Abilities.Bloodbending, Abilities.IceSpike, Abilities.OctopusForm,
			Abilities.Torrent, Abilities.AirBlast, Abilities.AirBurst,
			Abilities.AirShield, Abilities.AirSuction, Abilities.AirSwipe,
			Abilities.Blaze, Abilities.Collapse, Abilities.EarthBlast,
			Abilities.EarthTunnel, Abilities.FireBlast, Abilities.FireBurst,
			Abilities.FireShield, Abilities.Lightning, Abilities.RaiseEarth,
			Abilities.Shockwave, Abilities.Tornado, Abilities.Tremorsense };

	public static void HandleSwim(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			Abilities ability = Tools.getBendingAbility(player);
			if (Tools.isBender(player.getName(), BendingType.Water)
					&& Tools.canBendPassive(player, BendingType.Water)
					&& player.isSneaking()
					&& Tools.isWater(player.getLocation().getBlock())
					&& !TempBlock.isTempBlock(player.getLocation().getBlock())
					&& !(Arrays.asList(shiftabilities).contains(ability))) {
				player.setVelocity(player.getEyeLocation().getDirection()
						.clone().normalize().multiply(factor));
				// for (Entity entity : Tools.getEntitiesAroundPoint(
				// player.getLocation(), 1.5)) {
				// if (entity instanceof Player) {
				// Player tagalong = (Player) entity;
				// if (tagalong.isSneaking()
				// && !(Arrays.asList(shiftabilities)
				// .contains(Tools
				// .getBendingAbility(tagalong))))
				// tagalong.setVelocity(player.getEyeLocation()
				// .getDirection().clone().normalize()
				// .multiply(factor));
				// }
				// }
			}
		}
		// for (Player p : s.getOnlinePlayers()) {
		// if ((!Tools.isBender(p, BendingType.Water)
		// || !Tools.canBendPassive(p, BendingType.Water) || p
		// .isSneaking()) && timers.containsKey(p))
		// timers.remove(p);
		// if (Tools.isBender(p, BendingType.Water)
		// && Tools.canBendPassive(p, BendingType.Water)
		// && p.getLocation().getBlock().isLiquid()
		// && !timers.containsKey(p)) {
		// timers.put(p, System.currentTimeMillis());
		// }
		// if (timers.containsKey(p)) {
		// if (timers.get(p) + (interval - 21) >= System
		// .currentTimeMillis()) {
		// locations.put(p, p.getLocation().getBlock().getLocation());
		// }
		// }
		// if (timers.containsKey(p)) {
		// if (!(timers.get(p) + interval >= System.currentTimeMillis())
		// && locations.containsKey(p)
		// && ((int) locations.get(p).getX() != (int) p
		// .getLocation().getBlock().getLocation().getX() || (int) locations
		// .get(p).getZ() != (int) p.getLocation()
		// .getBlock().getLocation().getZ())
		// && p.getLocation().getBlock().isLiquid()) {
		//
		// if (!p.getEyeLocation().getBlock().isLiquid()) {
		// timers.put(p, System.currentTimeMillis());
		// if ((p.getLocation().getYaw() > -45 && p.getLocation()
		// .getYaw() <= 45)
		// && locations.get(p).getZ() < p.getLocation()
		// .getZ()) {
		// Vector v = p.getLocation().getDirection().setY(0);
		// p.setVelocity(v.normalize().multiply(factor));
		// } else if ((p.getLocation().getYaw() > 45 && p
		// .getLocation().getYaw() <= 135)
		// && locations.get(p).getX() > p.getLocation()
		// .getX()) {
		// Vector v = p.getLocation().getDirection().setY(0);
		// p.setVelocity(v.normalize().multiply(factor));
		// } else if ((p.getLocation().getYaw() > 135 && p
		// .getLocation().getYaw() <= 225)
		// && locations.get(p).getZ() > p.getLocation()
		// .getZ()) {
		// Vector v = p.getLocation().getDirection().setY(0);
		// p.setVelocity(v.normalize().multiply(factor));
		// } else if ((p.getLocation().getYaw() > 225 && p
		// .getLocation().getYaw() <= 315)
		// && locations.get(p).getX() < p.getLocation()
		// .getX()) {
		// Vector v = p.getLocation().getDirection().setY(0);
		// p.setVelocity(v.normalize().multiply(factor));
		// }
		// } else {
		// timers.put(p, System.currentTimeMillis());
		// Vector v = p.getLocation().getDirection().normalize()
		// .multiply(factor);
		// p.setVelocity(v);
		// }
		// }
		// }
		// }
	}
}

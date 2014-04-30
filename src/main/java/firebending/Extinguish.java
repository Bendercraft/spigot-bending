package firebending;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import tools.Abilities;
import tools.BendingPlayer;
import tools.BendingType;
import tools.ConfigManager;
import tools.Tools;
import airbending.AirBlast;

public class Extinguish {

	private static double defaultrange = ConfigManager.extinguishRange;
	private static double defaultradius = ConfigManager.extinguishRadius;
	private static byte full = AirBlast.full;

	public Extinguish(Player player) {

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.HeatControl))
			return;

		double range = Tools.firebendingDayAugment(defaultrange,
				player.getWorld());
		if (Tools.isMeltable(player.getTargetBlock(null, (int) range))) {
			new HeatMelt(player);
			return;
		}
		double radius = Tools.firebendingDayAugment(defaultradius,
				player.getWorld());
		for (Block block : Tools.getBlocksAroundPoint(
				player.getTargetBlock(null, (int) range).getLocation(), radius)) {
			if (Tools.isRegionProtectedFromBuild(player, Abilities.Blaze,
					block.getLocation()))
				continue;
			if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.STATIONARY_LAVA) {
				block.setType(Material.OBSIDIAN);
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			}
		}

		bPlayer.cooldown(Abilities.HeatControl);
	}

	public static boolean canBurn(Player player) {
		if (Tools.getBendingAbility(player) == Abilities.HeatControl
				|| FireJet.checkTemporaryImmunity(player)) {
			player.setFireTicks(0);
			return false;
		}

		if (player.getFireTicks() > 80
				&& Tools.canBendPassive(player, BendingType.Fire)) {
			player.setFireTicks(80);
		}

		// Tools.verbose(player.getFireTicks());

		return true;
	}

	public static String getDescription() {
		return "While this ability is selected, the firebender becomes impervious "
				+ "to fire damage and cannot be ignited. "
				+ "If the user left-clicks with this ability, the targeted area will be "
				+ "extinguished, although it will leave any creature burning engulfed in flames. "
				+ "This ability can also cool lava. If this ability is used while targetting ice or snow, it"
				+ " will instead melt blocks in that area. Finally, sneaking with this ability will cook any food in your hand.";
	}
}

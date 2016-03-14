package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.TempBlock;

/**
 * Is not an ability as is, but can still be on cooldown
 * @author Koudja
 *
 */
public class Drainbending {
	public static final String NAME = "Drainbending";

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

	public static boolean canDrainBend(Player player) {
		if (player == null) {
			return false;
		}
		
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (bender == null || !bender.hasAffinity(BendingAffinity.DRAIN)) {
			return false;
		}
		
		return true;
	}

	public static boolean canBeSource(Block block) {
		if (TempBlock.isTempBlock(block)) {
			return false;
		}
		return block.isEmpty();
	}
}

package net.bendercraft.spigot.bending.abilities.water;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

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
		if (bender == null || !bender.hasAffinity(BendingAffinity.DRAIN) || EntityTools.speToggled(player)) {
			return false;
		}
		
		return true;
	}

	public static boolean canBeSource(Block block) {
		if (TempBlock.isTempBlock(block)) {
			return false;
		}
		return BlockTools.isAir(block);
	}
}

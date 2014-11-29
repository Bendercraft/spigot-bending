package net.avatarrealms.minecraft.bending.abilities.water;

import net.avatarrealms.minecraft.bending.abilities.IPassiveAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WaterPassive implements IPassiveAbility{
	public static Vector handle(Player player, Vector velocity) {
		Vector vec = velocity.clone();
		return vec;
	}

	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock();
		Block fallblock = block.getRelative(BlockFace.DOWN);
		if (BlockTools.isWaterbendable(block, player) && !BlockTools.isPlant(block))
			return true;
		if (fallblock.getType() == Material.AIR)
			return true;
		if ((BlockTools.isWaterbendable(fallblock, player) && !BlockTools.isPlant(fallblock))
				|| fallblock.getType() == Material.SNOW_BLOCK)
			return true;
		return false;
	}

}

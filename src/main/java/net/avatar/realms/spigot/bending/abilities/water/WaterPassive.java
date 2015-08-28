package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.utils.BlockTools;

@BendingAbility(name="Water Passive", element=BendingType.Water)
public class WaterPassive implements net.avatar.realms.spigot.bending.abilities.deprecated.IPassiveAbility{
	public static Vector handle(Player player, Vector velocity) {
		Vector vec = velocity.clone();
		return vec;
	}

	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock();
		Block fallblock = block.getRelative(BlockFace.DOWN);
		if (BlockTools.isWaterbendable(block, player) && !BlockTools.isPlant(block)) {
			return true;
		}
		if (fallblock.getType() == Material.AIR) {
			return true;
		}
		if ((BlockTools.isWaterbendable(fallblock, player) && !BlockTools.isPlant(fallblock))
				|| (fallblock.getType() == Material.SNOW_BLOCK)) {
			return true;
		}
		return false;
	}

}

package net.avatarrealms.minecraft.bending.abilities.water;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

public class Drainbending {
	public static boolean canDrainBend(Player player) {
		return EntityTools.canBend(player, Abilities.Drainbending);
	}
	
	public static boolean canBeSource(Block block) {
		return block.isEmpty();
	}
}

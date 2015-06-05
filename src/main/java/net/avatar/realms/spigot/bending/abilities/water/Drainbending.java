package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.utils.EntityTools;

public class Drainbending {
	public static boolean canDrainBend(Player player) {
		return EntityTools.canBend(player, Abilities.Drainbending);
	}
	
	public static boolean canBeSource(Block block) {
		return block.isEmpty();
	}
}

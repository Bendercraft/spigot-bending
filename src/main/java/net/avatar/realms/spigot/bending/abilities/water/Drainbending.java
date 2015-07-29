package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Drain Bending", element=BendingType.Water, specialization=BendingSpecializationType.DrainBend)
public class Drainbending {
	public static boolean canDrainBend(Player player) {
		return EntityTools.canBend(player, Abilities.Drainbending);
	}
	
	public static boolean canBeSource(Block block) {
		return block.isEmpty();
	}
}

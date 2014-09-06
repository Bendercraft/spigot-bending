package net.avatarrealms.minecraft.bending.abilities.water;

import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

public class Drainbending {
	public static boolean canDrainBend(Player player) {
		if(!EntityTools.hasPermission(player, Abilities.Drainbending)) {
			return false;
		}
		if(!EntityTools.isSpecialized(player, Abilities.Drainbending.getSpecialization())) {
			return false;
		}
		return true;
	}
}

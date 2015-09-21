package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Drain Bending", element=BendingElement.Water, specialization=BendingAffinity.DrainBend)
public class Drainbending {
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;
	
	public static boolean canDrainBend(Player player) {
		return EntityTools.canBend(player, BendingAbilities.Drainbending);
	}
	
	public static boolean canBeSource(Block block) {
		return block.isEmpty();
	}
}

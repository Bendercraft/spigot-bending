package net.avatarrealms.minecraft.bending.abilities.water;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.IPassiveAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FastSwimming implements IPassiveAbility {

	private static double factor = ConfigManager.fastSwimmingFactor;

	public static void HandleSwim(Server server) {
		for (Player player : server.getOnlinePlayers()) {				
			if (!(EntityTools.isBender(player, BendingType.Water)
					&& EntityTools.canBendPassive(player, BendingType.Water)
					&& player.isSneaking())){
				continue;
			}
			Abilities ability = EntityTools.getBendingAbility(player);
			if (ability != null && ability.isShiftAbility()) {
				continue;
			}
			if (BlockTools.isWater(player.getLocation().getBlock())
					&& !TempBlock.isTempBlock(player.getLocation().getBlock())) {
				Vector dir = player.getEyeLocation().getDirection().clone();
				player.setVelocity(dir.normalize().multiply(factor));
			}
			else {
				Block block = player.getLocation().clone().add(0, -1, 0).getBlock();
				if (BlockTools.isWaterbendable(block, player)) {
					if (BlockTools.isWater(block)) {
						
					}
				}
				
			}
		}
		
	}
}

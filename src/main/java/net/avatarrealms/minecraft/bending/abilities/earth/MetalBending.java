package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class MetalBending {

	private static Map<Material, Integer> metals = new HashMap<Material, Integer>();
	static {
		metals.put(Material.IRON_ORE, 1);
		metals.put(Material.IRON_BLOCK, 9);
		metals.put(Material.IRON_DOOR, 6);
		metals.put(Material.IRON_AXE, 3);
		metals.put(Material.IRON_PICKAXE, 3);
		metals.put(Material.IRON_HOE, 2);
		metals.put(Material.IRON_SWORD, 2);
		metals.put(Material.IRON_HELMET, 5);
		metals.put(Material.IRON_LEGGINGS, 7);
		metals.put(Material.IRON_BOOTS, 4);
		metals.put(Material.IRON_CHESTPLATE, 8);
		metals.put(Material.SHEARS, 2);
	}

	public static void use(Player pl, Block bl) {
		// Don't really like it, magic value
		if (EntityTools.isBender(pl, BendingType.Earth)
				&& EntityTools.getBendingAbility(pl) == Abilities.MetalBending) {
			if (EntityTools.canBend(pl, Abilities.MetalBending)) {
				if (bl.getType() == Material.IRON_DOOR_BLOCK) {
					if (bl.getData() >= 8) {
						bl = bl.getRelative(BlockFace.DOWN);
					}
					if (bl.getType() == Material.IRON_DOOR_BLOCK) {						
						if (!Tools.isRegionProtectedFromBuild(pl,
								Abilities.MetalBending, bl.getLocation())) {
							if (bl.getData() < 4) {
								bl.setData((byte)(bl.getData() + 4));
								bl.getWorld().playEffect(bl.getLocation(), Effect.DOOR_TOGGLE, 0);
							}
							else {
								bl.setData((byte)(bl.getData() - 4));
								bl.getWorld().playEffect(bl.getLocation(), Effect.DOOR_TOGGLE, 0);
							}
						}
					}
				}
			}
		}
	}

	public static void metalMelt(Player player) {

	}
}

package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Earth Passive", element=BendingType.Earth)
public class EarthPassive implements net.avatar.realms.spigot.bending.abilities.deprecated.IPassiveAbility {
	private static Map<Block, Long> sandblocks = new HashMap<Block, Long>();
	private static Map<Block, Material> sandidentities = new HashMap<Block, Material>();

	@ConfigurationParameter("Time-Before-Reverse")
	private static long DURATION = 3000;

	public static boolean softenLanding(Player player) {
		Block block = player.getLocation().getBlock()
				.getRelative(BlockFace.DOWN);
		if (ProtectionManager.isRegionProtectedFromBendingPassives(player, player.getLocation())) {
			return false;
		}
		if (BlockTools.isEarthbendable(player, Abilities.RaiseEarth, block)
				|| BlockTools.isTransparentToEarthbending(player,
						Abilities.RaiseEarth, block)) {

			if (!BlockTools.isTransparentToEarthbending(player, block)) {
				Material type = block.getType();
				if (BlockTools.isSolid(block.getRelative(BlockFace.DOWN))) {
					block.setType(Material.SAND);
					if (!sandblocks.containsKey(block)) {
						sandidentities.put(block, type);
						sandblocks.put(block, System.currentTimeMillis());
					}
				}

			}

			for (Block affectedblock : BlockTools.getBlocksAroundPoint(
					block.getLocation(), 2)) {
				if (BlockTools.isEarthbendable(player, affectedblock)
						&& !BlockTools.isIronBendable(player,affectedblock.getType())) {
					if (BlockTools.isSolid(affectedblock.getRelative(BlockFace.DOWN))) {
						Material type = affectedblock.getType();
						affectedblock.setType(Material.SAND);
						if (!sandblocks.containsKey(affectedblock)) {
							sandidentities.put(affectedblock, type);
							sandblocks.put(affectedblock,
									System.currentTimeMillis());
						}
					}

				}
			}
			return true;
		}

		if (BlockTools.isEarthbendable(player, null, block)
				|| BlockTools.isTransparentToEarthbending(player, null, block)) {
			return true;
		}
		return false;
	}

	public static boolean isPassiveSand(Block block) {
		return (sandblocks.containsKey(block));
	}

	public static void revertSand(Block block) {
		Material type = sandidentities.get(block);
		sandidentities.remove(block);
		sandblocks.remove(block);
		if (block.getType() == Material.SAND) {
			block.setType(type);
		}
	}

	public static void revertSands() {
		List<Block> temp = new LinkedList<Block>(sandblocks.keySet());
		for (Block block : temp) {
			if (System.currentTimeMillis() >= (sandblocks.get(block) + DURATION)) {
				revertSand(block);
			}
		}

	}

	public static void revertAllSand() {
		List<Block> temp = new LinkedList<Block>(sandblocks.keySet());
		for (Block block : temp) {
			revertSand(block);
		}
	}

	public static void removeAll() {
		revertAllSand();
	}

}

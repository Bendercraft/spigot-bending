package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.TempBlock;

@ABendingAbility(name = "Earth Passive", bind = BendingAbilities.EarthPassive, element = BendingElement.Earth)
public class EarthPassive extends BendingPassiveAbility {

	@ConfigurationParameter("Time-Before-Reverse")
	private static long DURATION = 2500;

	private List<TempBlock> blocks = new LinkedList<TempBlock>();

	public EarthPassive(Player player) {
		super(player);
	}

	@Override
	public boolean start() {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (BlockTools.isEarthbendable(player, BendingAbilities.EarthPassive, block) || BlockTools.isTransparentToEarthbending(player, BendingAbilities.EarthPassive, block)) {

			if (!BlockTools.isTransparentToEarthbending(player, block)) {
				if (BlockTools.isSolid(block.getRelative(BlockFace.DOWN))) {
					if (!isPassiveSand(block)) {
						blocks.add(new TempBlock(block, Material.SAND, (byte) 0x0));
					}
				}
			}

			for (Block affectedblock : BlockTools.getBlocksAroundPoint(block.getLocation(), 2)) {
				if (BlockTools.isEarthbendable(player, affectedblock) && !BlockTools.isIronBendable(player, affectedblock.getType())) {
					if (BlockTools.isSolid(affectedblock.getRelative(BlockFace.DOWN))) {
						if (!isPassiveSand(affectedblock) && !block.getLocation().equals(affectedblock.getLocation())) {
							blocks.add(new TempBlock(affectedblock, Material.SAND, (byte) 0x0));
						}
					}
				}
			}
			
			setState(BendingAbilityState.Progressing);
			return true;
		}

		return false;
	}

	@Override
	protected long getMaxMillis() {
		return DURATION;
	}

	@Override
	public void stop() {
		for (TempBlock state : blocks) {
			state.revertBlock();
		}
		blocks.clear();
	}

	public static boolean isPassiveSand(Block block) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.EarthPassive);
		if (instances == null || instances.isEmpty()) {
			return false;
		}
		if(!TempBlock.isTempBlock(block)) {
			return false;
		}
		for (BendingAbility passive : instances.values()) {
			if (((EarthPassive) passive).blocks.contains(TempBlock.get(block))) {
				return true;
			}
		}
		return false;
	}

	public static void revertSand(Block block) {
		if(isPassiveSand(block)) {
			TempBlock.get(block).revertBlock();
		}
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (!bender.isBender(BendingElement.Earth)) {
			return false;
		}

		if (!EntityTools.canBendPassive(player, BendingElement.Earth)) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.EarthPassive);
		if (instances == null) {
			return true;
		}

		return !instances.containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public void progress() {
		
	}
}

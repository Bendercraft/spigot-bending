package net.bendercraft.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPassiveAbility;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = EarthPassive.NAME, element = BendingElement.EARTH, passive = true)
public class EarthPassive extends BendingPassiveAbility {
	public final static String NAME = "EarthPassive";

	@ConfigurationParameter("Time-Before-Reverse")
	private static long DURATION = 2500;

	private List<TempBlock> blocks = new LinkedList<TempBlock>();

	public EarthPassive(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean start() {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (BlockTools.isEarthbendable(player, register, block) || BlockTools.isTransparentToEarthbending(player, register, block)) {

			if (!BlockTools.isTransparentToEarthbending(player, block)) {
				if (BlockTools.isSolid(block.getRelative(BlockFace.DOWN))) {
					if (!isPassiveSand(block)) {
						//blocks.add(new TempBlock(block, Material.SAND, (byte) 0x0));
						blocks.add(TempBlock.makeTemporary(block, Material.SAND, false));
					}
				}
			}

			for (Block affectedBlock : BlockTools.getBlocksAroundPoint(block.getLocation(), 2)) {
				if (BlockTools.isEarthbendable(player, affectedBlock) && !BlockTools.isIronBendable(player, affectedBlock.getType())) {
					if (BlockTools.isSolid(affectedBlock.getRelative(BlockFace.DOWN))) {
						if (!isPassiveSand(affectedBlock) && !block.getLocation().equals(affectedBlock.getLocation())) {
							//blocks.add(new TempBlock(affectedBlock, Material.SAND, (byte) 0x0));
							blocks.add(TempBlock.makeTemporary(affectedBlock, Material.SAND, false));
						}
					}
				}
			}
			
			setState(BendingAbilityState.PROGRESSING);
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
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
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

		if (!bender.isBender(BendingElement.EARTH)) {
			return false;
		}

		if (!EntityTools.canBendPassive(player, BendingElement.EARTH)) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return true;
		}

		return !instances.containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return UUID.randomUUID();
	}

	@Override
	public void progress() {
		
	}
}

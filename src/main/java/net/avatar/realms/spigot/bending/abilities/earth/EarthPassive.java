package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.base.PassiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Earth Passive", element=BendingType.Earth)
public class EarthPassive extends PassiveAbility {

	@ConfigurationParameter("Time-Before-Reverse")
	private static long DURATION = 2500;
	
	private Map<Block, BlockState> blocks = new HashMap<Block, BlockState>();
	
	public EarthPassive(Player player) {
		super(player, null);
	}
	
	@Override
	public boolean start() {
		if (state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		
		if (BlockTools.isEarthbendable(player, Abilities.RaiseEarth, block)
				|| BlockTools.isTransparentToEarthbending(player,
						Abilities.RaiseEarth, block)) {

			if (!BlockTools.isTransparentToEarthbending(player, block)) {
				if (BlockTools.isSolid(block.getRelative(BlockFace.DOWN))) {
					if (!isPassiveSand(block)) {
						blocks.put(block, block.getState());
						block.setType(Material.SAND);
					}
				}
			}

			for (Block affectedblock : BlockTools.getBlocksAroundPoint(block.getLocation(), 2)) {
				if (BlockTools.isEarthbendable(player, affectedblock)
						&& !BlockTools.isIronBendable(player,affectedblock.getType())) {
					if (BlockTools.isSolid(affectedblock.getRelative(BlockFace.DOWN))) {
						if (!isPassiveSand(affectedblock)) {
							blocks.put(affectedblock, affectedblock.getState());
							affectedblock.setType(Material.SAND);
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
	
	@Override
	protected long getMaxMillis() {
		return DURATION;
	}

	@Override
	public void stop() {
		for (BlockState state : blocks.values()) {
			state.update(true);
		}
		blocks.clear();
	}

	public static boolean isPassiveSand(Block block) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.EarthPassive);
		if (instances == null || instances.isEmpty()) {
			return false;
		}
		for (IAbility passive : instances.values()) {
			if (((EarthPassive)passive).blocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}

	public static void revertSand(Block block) {
		EarthPassive passive = null;
		for (IAbility abil : AbilityManager.getManager().getInstances(Abilities.EarthPassive).values()) {
			if (((EarthPassive)abil).blocks.containsKey(block)) {
				passive = ((EarthPassive)abil);
				break;
			}
		}
		if (passive == null) {
			return;
		}
		passive.blocks.get(block).update(true);
		passive.blocks.remove(block);
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if (!bender.isBender(BendingType.Earth)) {
			return false;
		}
		
		if (!EntityTools.canBendPassive(player, BendingType.Earth)) {
			return false;
		}
		
		return true;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.EarthPassive;
	}
}

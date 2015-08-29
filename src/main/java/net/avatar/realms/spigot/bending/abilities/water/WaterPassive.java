package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.PassiveAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Water Passive", element=BendingType.Water)
public class WaterPassive extends PassiveAbility{
	
	public WaterPassive(Player player) {
		super(player, null);
	}

	public static Vector handle(Player player, Vector velocity) {
		Vector vec = velocity.clone();
		return vec;
	}
	
	@Override
	public boolean start() {
		if (state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		
		Block block = player.getLocation().getBlock();
		Block fallblock = block.getRelative(BlockFace.DOWN);
		
		if (fallblock.getType() == Material.AIR) {
			return true;
		}
		
		if (BlockTools.isWaterbendable(block, player) && !BlockTools.isPlant(block)) {
			return true;
		}
		
		if ((BlockTools.isWaterbendable(fallblock, player) && !BlockTools.isPlant(fallblock))
				|| (fallblock.getType() == Material.SNOW_BLOCK)) {
			return true;
		}
		
		return false;
	}

//	public static boolean softenLanding(Player player) {
////		Block block = player.getLocation().getBlock();
////		Block fallblock = block.getRelative(BlockFace.DOWN);
//		return false;
//	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.FastSwimming;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if (!bender.isBender(BendingType.Water)) {
			return false;
		}
		
		if (!EntityTools.canBendPassive(player, BendingType.Water)) {
			return false;
		}
		
		return true;
	}

	

}

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
		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		
		Block block = this.player.getLocation().getBlock();
		Block fallblock = block.getRelative(BlockFace.DOWN);
		
		if (fallblock.getType() == Material.AIR) {
			return true;
		}
		
		if (BlockTools.isWaterbendable(block, this.player) && !BlockTools.isPlant(block)) {
			return true;
		}
		
		if ((BlockTools.isWaterbendable(fallblock, this.player) && !BlockTools.isPlant(fallblock))
				|| (fallblock.getType() == Material.SNOW_BLOCK)) {
			return true;
		}
		
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
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
		
		if (!this.bender.isBender(BendingType.Water)) {
			return false;
		}
		
		if (!EntityTools.canBendPassive(this.player, BendingType.Water)) {
			return false;
		}
		
		return true;
	}

	

}

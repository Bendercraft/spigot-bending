package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.IPassiveAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FastSwimming implements IPassiveAbility {

	private static double factor = ConfigManager.fastSwimmingFactor;
	private static Map<Player, FastSwimming> instances = new HashMap<Player, FastSwimming>();
	private static final byte full = 0x0;
	
	private Player player;
	private List<TempBlock> affectedBlocks = new LinkedList<TempBlock>();
	
	public FastSwimming (Player player) {
		if (!player.isOnline() || player.isDead()) {
			return;
		}
		if (!EntityTools.canBendPassive(player, BendingType.Water)) {
			return;
		}
		
		this.player = player;
		instances.put(player, this);
	}
	
	public boolean progress() {		
		if (!player.isOnline() || player.isDead()) {
			return false;
		}
		
		if (!(EntityTools.canBendPassive(player, BendingType.Water)
				&& player.isSneaking())){
			return false;
		}
		Abilities ability = EntityTools.getBendingAbility(player);
		if (ability != null && ability.isShiftAbility()) {
			return false;
		}
		resetAffectedBlocks(5);
		if (BlockTools.isWater(player.getLocation().getBlock())
				&& !TempBlock.isTempBlock(player.getLocation().getBlock())) {
			swimFast();
		}
		else {
			Block block = player.getLocation().clone().add(0, -1, 0).getBlock();
			if (BlockTools.isWaterBased(block) || 
					player.getLocation().getBlock().getType().equals(Material.SNOW)) {
				runFast(block);
			}
		}
		return true;
	}
	
	public static void progressAll() {
		LinkedList<Player> toRemove = new LinkedList<Player>();
		boolean keep;
		for (Player p : instances.keySet()) {
			keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
			}
		}
		for (Player p : toRemove) {
			instances.get(p).remove();
		}
	}
	
	public void remove() {
		resetAffectedBlocks();
		instances.remove(player);
	}
	
	private void resetAffectedBlocks() {
		for (TempBlock tb : affectedBlocks) {
			tb.revertBlock();
		}
	}
	
	private void resetAffectedBlocks(int distance) {
		for (TempBlock tb : affectedBlocks) {
			if (tb.getBlock().getLocation().distance(player.getLocation()) < distance) {
				tb.revertBlock();
			}		
		}
	}
	
	private void swimFast() {
		Vector dir = player.getEyeLocation().getDirection().clone();
		player.setVelocity(dir.normalize().multiply(factor));
	}
	
	private void runFast(Block block) {
		if (BlockTools.isWater(block)) {
			affectedBlocks.add(new TempBlock(block, Material.ICE, full));
		}
		Vector dir = player.getEyeLocation().getDirection().clone().setY(0);
		
		Block advancedBlock = block;
		for (int i = 0; i < 2; i ++) {
			advancedBlock = advancedBlock.getLocation().add(dir).getBlock();
			if (BlockTools.isWater(advancedBlock)) {
				affectedBlocks.add(new TempBlock(advancedBlock, Material.ICE, full));
			}
			if (player.getEyeLocation().getDirection().getY() > 0) {
				Block upperBlock = advancedBlock.getLocation().add(0,1,0).getBlock();
				if (!BlockTools.isFluid(advancedBlock) && upperBlock.getType().equals(Material.AIR)) {
					upperBlock.setType(Material.SNOW);
				}
			}				
		}
		player.setVelocity(dir.normalize().multiply(factor/1.35));
	}
	
}

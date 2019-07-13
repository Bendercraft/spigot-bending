package net.bendercraft.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = PhaseChange.NAME, element = BendingElement.WATER)
public class PhaseChange extends BendingActiveAbility {
	public final static String NAME = "PhaseChange";
	
	public final static String NAME_SHIFT = "BalanceShift";
	
	@ConfigurationParameter("Range")
	public static int RANGE = 20;

	@ConfigurationParameter("Radius")
	public static int RADIUS = 3;

	@ConfigurationParameter("Depth")
	public static int DEPTH = 1;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 500;
	
	@ConfigurationParameter("Cooldown-Shift")
	public static long COOLDOWN_SHIFT = 5000;

	private List<TempBlock> frozens = new LinkedList<>();
	private List<TempBlock> melted = new LinkedList<>();

	private int radius;

	public PhaseChange(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.radius = RADIUS;
		if(bender.hasPerk(BendingPerk.WATER_PHASECHANGE_RADIUS)) {
			this.radius += 1;
		}
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PROGRESSING);
		}
		
		if (bender.isOnCooldown(NAME)) {
			return false;
		}

		if(player.isSneaking()) {
			if(!bender.isOnCooldown(NAME_SHIFT)) {
				int balance = bender.water.getBalance();
				bender.water.setBalance(0);
				bender.cooldown(NAME_SHIFT, COOLDOWN_SHIFT*Math.abs(balance));
			}
		}
		else {
			//Freeze
			int range = RANGE;
			if (AvatarState.isAvatarState(player)) {
				range = AvatarState.getValue(range);
			}
			Block targetBlock = player.getTargetBlockExact(range, FluidCollisionMode.ALWAYS);
			Location location = targetBlock.getLocation();
			final int y = (int) location.getY();
			final int minimumLevel = y - DEPTH;
			List<Block> freezableBlocks = BlockTools.getFilteredBlocksAroundPoint(location, radius, (block) -> block.getLocation().getY() >= minimumLevel && isFreezable(player, block));
			for (Block block : freezableBlocks) {
				PhaseChange owner = get(block);
				if(owner != null) {
					TempBlock b = TempBlock.get(block);
					if(b != null) {
						b.revertBlock();
						owner.melted.remove(b);
					}
				}
				else {
					frozens.add(TempBlock.makeTemporary(this, block, Material.ICE, Material.ICE.createBlockData(), true));
				}
			}
			if(!frozens.isEmpty()) {
				bender.cooldown(NAME, COOLDOWN);
			}
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PROGRESSING);
		}
		
		if (bender.isOnCooldown(NAME)) {
			return false;
		}
		//Thaw
		int range = RANGE;
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
		}

		Block targetBlock = player.getTargetBlockExact(range);
		if (targetBlock != null) {
			Location location = targetBlock.getLocation();
			final int y = (int) location.getY();
			final int minimumLevel = y - DEPTH;
			List<Block> thawableBlocks = BlockTools.getFilteredBlocksAroundPoint(location, radius, (block) -> block.getLocation().getY() >= minimumLevel && isThawable(player, block));
			for (Block block : thawableBlocks) {
				PhaseChange owner = get(block);
				if(owner != null) {
					TempBlock b = TempBlock.get(block);
					if(b != null) {
						b.revertBlock();
						owner.frozens.remove(b);
					}
				}
				else if(TempBlock.isTempBlock(block)) {
					TempBlock.get(block).revertBlock();
				}
				else {
					melted.add(TempBlock.makeTemporary(this, block, Material.WATER, Material.WATER.createBlockData(), true));
				}
			}

			if(!melted.isEmpty()) {
				bender.cooldown(NAME, COOLDOWN);
			}
		}
		return false;
	}

	private static boolean isFreezable(Player player, Block block) {
		if (ProtectionManager.isLocationProtectedFromBending(player, AbilityManager.getManager().getRegisteredAbility(NAME), block.getLocation())) {
			return false;
		}
		if (block.getType() == Material.WATER) {
			if (!TempBlock.isTempBlock(block) || isMelted(block)) {
				return true;
			}
		}

		return false;
	}
	
	private static boolean isThawable(Player player, Block block) {
		if (ProtectionManager.isLocationProtectedFromBending(player, AbilityManager.getManager().getRegisteredAbility(NAME), block.getLocation())) {
			return false;
		}
		if (block.getType() == Material.ICE) {
			return true;
		}

		return false;
	}
	

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if(melted.isEmpty() && frozens.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		
	}
	
	@Override
	public void stop() {
		for(TempBlock b : frozens) {
			b.revertBlock();
		}
		frozens.clear();
		for(TempBlock b : melted) {
			b.revertBlock();
		}
		melted.clear();
	}

	public static boolean isFrozen(Block block) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			PhaseChange fm = (PhaseChange) ab;
			for(TempBlock b : fm.frozens) {
				if(b.getBlock().getLocation().equals(block.getLocation())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isMelted(Block block) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			PhaseChange fm = (PhaseChange) ab;
			for(TempBlock b : fm.melted) {
				if(b.getBlock().getLocation().equals(block.getLocation())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static PhaseChange get(Block block) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			PhaseChange fm = (PhaseChange) ab;
			for(TempBlock b : fm.melted) {
				if(b.getBlock().getLocation().equals(block.getLocation())) {
					return fm;
				}
			}
			for(TempBlock b : fm.frozens) {
				if(b.getBlock().getLocation().equals(block.getLocation())) {
					return fm;
				}
			}
		}
		return null;
	}

	public static void thawThenRemove(Block block) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			PhaseChange fm = (PhaseChange) ab;
			for(TempBlock b : fm.frozens) {
				if(b.getBlock().getLocation().equals(block.getLocation())) {
					fm.remove();
					break;
				}
			}
		}
	}

	public boolean isBlockLevel(Block block, int level) {
		for(TempBlock b : frozens) {
			if(b.getBlock().getLocation().equals(block.getLocation())) {
				Levelled data = (Levelled) b.getBlock().getBlockData();
				return data.getLevel() == level;
			}
		}
		return false;
	}

	public static boolean isLevel(Block block, int level) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			PhaseChange fm = (PhaseChange) ab;
			if (fm.isBlockLevel(block, level)) {
				return true;
			}
		}
		return false;

	}

	@Override
	public Object getIdentifier() {
		return player;
	}
}

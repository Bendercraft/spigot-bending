package net.avatar.realms.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name = "Phase Change", bind = BendingAbilities.PhaseChange, element = BendingElement.Water)
public class PhaseChange extends BendingActiveAbility {
	@ConfigurationParameter("Range")
	public static int RANGE = 20;

	@ConfigurationParameter("Radius")
	public static int RADIUS = 3;

	@ConfigurationParameter("Depth")
	public static int DEPTH = 1;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 500;

	private List<TempBlock> frozens = new LinkedList<TempBlock>();
	private List<TempBlock> melted = new LinkedList<TempBlock>();

	public PhaseChange(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		if(state == BendingAbilityState.CanStart) {
			AbilityManager.getManager().addInstance(this);
			setState(BendingAbilityState.Progressing);
		}
		
		if (bender.isOnCooldown(BendingAbilities.PhaseChange)) {
			return false;
		}
		//Freeze
		int range = (int) PluginTools.waterbendingNightAugment(RANGE, player.getWorld());
		int radius = (int) PluginTools.waterbendingNightAugment(RADIUS, player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
		}
		Location location = EntityTools.getTargetedLocation(player, range);
		int y = (int) location.getY();
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (block.getLocation().getY() >= (y - DEPTH)) {
				if(isFreezable(player, block)) {
					PhaseChange owner = get(block);
					if(owner != null) {
						TempBlock b = TempBlock.get(block);
						b.revertBlock();
						owner.melted.remove(b);
					} else {
						frozens.add(new TempBlock(block, Material.ICE, block.getData()));
					}
				}
			}
		}
		bender.cooldown(BendingAbilities.PhaseChange, COOLDOWN);
		return false;
	}

	@Override
	public boolean sneak() {
		if(state == BendingAbilityState.CanStart) {
			AbilityManager.getManager().addInstance(this);
			setState(BendingAbilityState.Progressing);
		}
		
		if (bender.isOnCooldown(BendingAbilities.PhaseChange)) {
			return false;
		}
		//Thaw
		int range = (int) PluginTools.waterbendingNightAugment(RANGE, player.getWorld());
		int radius = (int) PluginTools.waterbendingNightAugment(RADIUS, player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
		}
		Location location = EntityTools.getTargetedLocation(player, range);
		int y = (int) location.getY();
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (block.getLocation().getY() >= (y - DEPTH)) {
				if(isThawable(player, block)) {
					PhaseChange owner = get(block);
					if(owner != null) {
						TempBlock b = TempBlock.get(block);
						b.revertBlock();
						owner.frozens.remove(b);
					} else {
						melted.add(new TempBlock(block, Material.WATER, block.getData()));
					}
				}
			}
		}
		bender.cooldown(BendingAbilities.PhaseChange, COOLDOWN);
		return false;
	}

	private static boolean isFreezable(Player player, Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.PhaseChange, block.getLocation())) {
			return false;
		}
		if ((block.getType() == Material.WATER) || (block.getType() == Material.STATIONARY_WATER)) {
			if (!TempBlock.isTempBlock(block) || isMelted(block)) {
				return true;
			}
		}

		return false;
	}
	
	private static boolean isThawable(Player player, Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.PhaseChange, block.getLocation())) {
			return false;
		}
		if (block.getType() == Material.ICE) {
			if (!TempBlock.isTempBlock(block) || isFrozen(block)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean progress() {
		if(!super.progress()) {
			return false;
		}
		if(melted.isEmpty() && frozens.isEmpty()) {
			return false;
		}
		return true;
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
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
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
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
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
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
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
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
			PhaseChange fm = (PhaseChange) ab;
			for(TempBlock b : fm.frozens) {
				if(b.getBlock().getLocation().equals(block.getLocation())) {
					fm.setState(BendingAbilityState.Ended);
					break;
				}
			}
		}
	}

	public boolean isBlockLevel(Block block, Byte level) {
		for(TempBlock b : frozens) {
			if(b.getBlock().getLocation().equals(block.getLocation())) {
				return b.getBlock().getState().getRawData() == level;
			}
		}
		return false;
	}

	public static boolean isLevel(Block block, byte level) {
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
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

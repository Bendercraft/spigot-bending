package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.abilities.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Phase Change", element=BendingElement.Water)
public class FreezeMelt extends BendingActiveAbility {
	@ConfigurationParameter("Range")
	public static int RANGE = 20;

	@ConfigurationParameter("Radius")
	public static int RADIUS = 3;

	@ConfigurationParameter("Depth")
	public static int DEPTH = 1;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 500;

	private Map<Block, Byte> frozenblocks = new HashMap<Block, Byte>();
	private Block source;

	public FreezeMelt(Player player, IBendingAbility parent, Block block) {
		super(player, parent);
		if(isFreezable(player, block)) {
			source = block;
			byte data = block.getData();
			block.setType(Material.ICE);
			this.frozenblocks.put(block, data);
			AbilityManager.getManager().addInstance(this);
		}
	}

	public FreezeMelt(Player player, IBendingAbility parent) {
		super(player, parent);

		if (bender.isOnCooldown(BendingAbilities.PhaseChange)) {
			return;
		}

		int range = (int) PluginTools.waterbendingNightAugment(RANGE,
				player.getWorld());
		int radius = (int) PluginTools.waterbendingNightAugment(RADIUS,
				player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
		}

		Location location = EntityTools.getTargetedLocation(player, range);
		int y = (int) location.getY();
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (block.getLocation().getY() >= (y - DEPTH)) {
				new FreezeMelt(player, parent, block);
			}
		}

		bender.cooldown(BendingAbilities.PhaseChange, COOLDOWN);
	}

	private static boolean isFreezable(Player player, Block block) {	
		if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.PhaseChange,
				block.getLocation())) {
			return false;
		}
		if ((block.getType() == Material.WATER)
				|| (block.getType() == Material.STATIONARY_WATER)) {
			if (WaterManipulation.canPhysicsChange(block)
					&& !TempBlock.isTempBlock(block)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean swing() {
		@SuppressWarnings("deprecation")
		byte data = source.getData();
		source.setType(Material.ICE);
		this.frozenblocks.put(source, data);
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean thaw(Block block) {
		if (this.frozenblocks.containsKey(block)) {
			byte data = this.frozenblocks.get(block);
			block.setType(Material.WATER);
			block.setData(data);
			return false;
		}
		return true;
	}

	@Override
	public boolean progress() {
		List<Block> toRemove = new LinkedList<Block>();
		for (Block block : this.frozenblocks.keySet()) {
			if (canThaw(block)) {
				boolean keep = thaw(block);
				if (!keep) {
					toRemove.add(block);
				}
			}
		}
		for (Block block : toRemove) {
			this.frozenblocks.remove(block);
		}
		return !this.frozenblocks.isEmpty();
	}

	public boolean canThaw(Block block) {
		if (!WaterManipulation.canPhysicsChange(block)) {
			return false;
		}
		if (this.frozenblocks.containsKey(block)) {
			if (EntityTools.canBend(getPlayer(), BendingAbilities.PhaseChange)) {
				double range = PluginTools.waterbendingNightAugment(
						RANGE, getPlayer().getWorld());
				if (AvatarState.isAvatarState(getPlayer())) {
					range = AvatarState.getValue(range);
				}
				//Player just changed world, allow thaw
				if(!block.getLocation().getWorld().getUID().equals(getPlayer().getLocation().getWorld().getUID())) {
					return true;
				}
				if (block.getLocation().distance(getPlayer().getLocation()) <= range) {
					return false;
				}
			}
			if (EntityTools.getBendingAbility(getPlayer()) == BendingAbilities.OctopusForm) {
				if (block.getLocation().distance(getPlayer().getLocation()) <= (OctopusForm.radius + 2)) {
					return false;
				}
			}
			return true;

		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void clear() {
		List<Block> toRemove = new LinkedList<Block>();
		for (Entry<Block, Byte> entry : this.frozenblocks.entrySet()) {
			Block block = entry.getKey();
			if (block.getType() == Material.ICE) {
				byte data = entry.getValue();
				block.setType(Material.WATER);
				block.setData(data);
				toRemove.add(block);
			}
		}
		this.frozenblocks.clear();
	}

	private boolean isBlockFrozen(Block block) {
		return this.frozenblocks.containsKey(block);
	}

	public static boolean isFrozen(Block block) {
		for(IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
			FreezeMelt fm = (FreezeMelt) ab;
			if(fm.isBlockFrozen(block)) {
				return true;
			}
		}
		return false;
	}

	public static void thawThenRemove(Block block) {
		for(IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
			FreezeMelt fm = (FreezeMelt) ab;
			if(fm.thaw(block)) {
				fm.remove(block);
			}
		}
	}

	private void remove(Block block) {
		this.frozenblocks.remove(block);
	}

	public boolean isBlockLevel(Object block, Byte level) {
		return this.frozenblocks.get(block) == level;
	}

	public static boolean isLevel(Block block, byte level) {
		for(IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.PhaseChange).values()) {
			FreezeMelt fm = (FreezeMelt) ab;
			if(fm.isBlockLevel(block, level)) {
				return true;
			}
		}
		return false;

	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public BendingAbilities getAbilityType() {
		return BendingAbilities.PhaseChange;
	}


}

package net.bendercraft.spigot.bending.abilities.multi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.air.AirBubble;
import net.bendercraft.spigot.bending.abilities.water.WaterBubble;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

public abstract class Bubble extends BendingActiveAbility {

	protected double radius;
	protected Location lastLocation;

	protected Map<Block, TempBlock> origins;

	protected Set<Material> pushedMaterials;

	public Bubble(RegisteredAbility register, Player player) {
		super(register, player);
		this.lastLocation = player.getLocation();
		this.origins = new HashMap<Block, TempBlock>();
		this.pushedMaterials = new HashSet<Material>();
	}

	@Override
	public void progress() {
		if (AirBubble.NAME.equals(EntityTools.getBendingAbility(player))
				|| WaterBubble.NAME.equals(EntityTools.getBendingAbility(player))) {
			pushWater();
		}
		else {
			remove();
		}
	}

	@Override
	public boolean swing() {
		if (getState().equals(BendingAbilityState.START)) {
			setState(BendingAbilityState.PROGRESSING);
			return false;
		}

		if (!getState().equals(BendingAbilityState.PROGRESSING)) {
			return false;
		}

		return false;
	}

	public boolean blockInBubble(Block block) {
		if (block.getWorld() != this.player.getWorld()) {
			return false;
		}
		if (block.getLocation().distance(this.player.getLocation()) <= this.radius) {
			return true;
		}
		return false;
	}

	private void pushWater() {
		Location location = this.player.getLocation();

		// Do not bother entering this loop if player location has not been
		// modified
		if (!BlockTools.locationEquals(this.lastLocation, location)) {

			List<Block> toRemove = new LinkedList<Block>();
			for (Entry<Block, TempBlock> entry : this.origins.entrySet()) {
				if (!blockInBubble(entry.getKey())) {
					toRemove.add(entry.getKey());
				}
			}

			for (Block block : toRemove) {
				if ((block.getType() == Material.AIR) || this.pushedMaterials.contains(block.getType())) {
					origins.get(block).revertBlock();
				}
				origins.remove(block);
			}

			for (Block block : BlockTools.getBlocksAroundPoint(location, this.radius)) {
				if (this.origins.containsKey(block)) {
					continue;
				}
				if (ProtectionManager.isLocationProtectedFromBending(this.player, register, block.getLocation())) {
					continue;
				}
				if (this.pushedMaterials.contains(block.getType())) {
					if (WaterManipulation.canBubbleWater(block)) {
						//this.origins.put(block, new TempBlock(block, Material.AIR, (byte) 0x0));
						this.origins.put(block, TempBlock.makeTemporary(block, Material.AIR, true));
					}
				}
			}
			this.lastLocation = this.player.getLocation();
		}
	}

	public static boolean canFlowTo(Block block) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(AirBubble.NAME);
		if (instances == null) {
			instances = new HashMap<Object, BendingAbility>();
		}
		Map<Object, BendingAbility> insts = AbilityManager.getManager().getInstances(WaterBubble.NAME);

		if (insts != null) {
			instances.putAll(insts);
		}

		if (instances.isEmpty()) {
			return true;
		}

		for (Object o : instances.keySet()) {
			Bubble bubble = (Bubble) instances.get(o);
			if (bubble.blockInBubble(block)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void stop() {
		for (Entry<Block, TempBlock> entry : this.origins.entrySet()) {
			if ((entry.getKey().getType() == Material.AIR) || entry.getKey().isLiquid()) {
				entry.getValue().revertBlock();
			}
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

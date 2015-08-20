package net.avatar.realms.spigot.bending.abilities.multi;

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
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

public abstract class Bubble extends Ability {

	protected double radius;
	protected Location lastLocation;

	protected Map<Block, BlockState> origins;

	protected Set<Material> pushedMaterials;

	public Bubble (Player player, Ability parent) {
		super(player, parent);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		this.lastLocation = player.getLocation();
		this.origins = new HashMap<Block, BlockState>();
		this.pushedMaterials = new HashSet<Material>();
	}

	@Override
	public boolean progress () {

		if (!super.progress()) {
			return false;
		}

		if (EntityTools.getBendingAbility(this.player) == this.getAbilityType()) {
			pushWater();
			return true;
		}
		return false;
	}

	@Override
	public boolean sneak () {

		if (this.state.isBefore(AbilityState.CanStart)) {
			return true;
		}

		if (this.state.equals(AbilityState.CanStart)) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Progressing);
			return false;
		}

		if (!this.state.equals(AbilityState.Progressing)) {
			return false;
		}

		setState(AbilityState.Ended);

		return false;
	}

	public boolean blockInBubble (Block block) {
		if (block.getWorld() != this.player.getWorld()) {
			return false;
		}
		if (block.getLocation().distance(this.player.getLocation()) <= this.radius) {
			return true;
		}
		return false;
	}

	private void pushWater () {
		Location location = this.player.getLocation();

		// Do not bother entering this loop if player location has not been
		// modified
		if (!BlockTools.locationEquals(this.lastLocation, location)) {

			List<Block> toRemove = new LinkedList<Block>();
			for (Entry<Block, BlockState> entry : this.origins.entrySet()) {
				if (entry.getKey().getWorld() != location.getWorld()) {
					toRemove.add(entry.getKey());
				}
				else if (entry.getKey().getLocation().distance(location) > this.radius) {
					toRemove.add(entry.getKey());
				}
			}

			for (Block block : toRemove) {
				if ((block.getType() == Material.AIR) || this.pushedMaterials.contains(block.getType())) {
					this.origins.get(block).update(true, false);
				}
				this.origins.remove(block);
			}

			for (Block block : BlockTools.getBlocksAroundPoint(location, this.radius)) {
				if (this.origins.containsKey(block)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(this.player, this.getAbilityType(), block.getLocation())) {
					continue;
				}
				if (this.pushedMaterials.contains(block.getType())) {
					if (WaterManipulation.canBubbleWater(block)) {
						this.origins.put(block, block.getState());
						block.setType(Material.AIR);
					}
				}
			}
			this.lastLocation = this.player.getLocation();
		}
	}

	public static boolean canFlowTo (Block block) {
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.AirBubble);
		if (instances == null) {
			instances = new HashMap<Object, Ability>();
		}
		Map<Object, Ability> insts = AbilityManager.getManager().getInstances(Abilities.WaterBubble);

		if (insts != null) {
			instances.putAll(insts);
		}

		if (instances.isEmpty()) {
			return true;
		}

		for (Object o : instances.keySet()) {
			AirBubble bubble = (AirBubble) instances.get(o);
			if (bubble.blockInBubble(block)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void stop () {
		for (Entry<Block, BlockState> entry : this.origins.entrySet()) {
			if ((entry.getKey().getType() == Material.AIR) || entry.getKey().isLiquid()) {
				entry.getValue().update(true);
			}
		}
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}
}

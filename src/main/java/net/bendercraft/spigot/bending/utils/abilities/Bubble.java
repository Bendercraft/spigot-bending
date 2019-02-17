package net.bendercraft.spigot.bending.utils.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.air.AirBubble;
import net.bendercraft.spigot.bending.abilities.water.WaterBubble;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

public abstract class Bubble extends BendingActiveAbility {

	protected double radius;
	protected Location lastLocation;

	protected Map<Block, TempBlock> origins;

	public Bubble(RegisteredAbility register, Player player) {
		super(register, player);
		this.lastLocation = player.getLocation();
		this.origins = new HashMap<>();
	}

	@Override
	public void progress() {
		if (getName().equals(EntityTools.getBendingAbility(player))) {
			pushLiquids();
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

	private void pushLiquids() {
		Location location = this.player.getLocation();

		// Do not bother entering this loop if player location has not been
		// modified
		if (!BlockTools.locationEquals(this.lastLocation, location)) {

			resetOldBlocks();

			pushNewBlocks();

			this.lastLocation = this.player.getLocation();
		}
	}

	protected abstract void pushNewBlocks();

	private void resetOldBlocks() {
		Iterator<Entry<Block, TempBlock>> originsIterator = this.origins.entrySet().iterator();
		while (originsIterator.hasNext()) {
			Entry<Block, TempBlock> entry = originsIterator.next();
			if (!blockInBubble(entry.getKey())) {
				entry.getValue().revertBlock();
				originsIterator.remove();
			}
		}
	}

	public static boolean canFlowTo(Block block) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(AirBubble.NAME);
		if (instances == null) {
			instances = new HashMap<>();
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
			entry.getValue().revertBlock();
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

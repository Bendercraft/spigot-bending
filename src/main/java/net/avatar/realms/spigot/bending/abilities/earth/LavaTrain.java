package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.MathUtils;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;

/**
 * State Preparing : The lava train is growing State Progressing : The lava
 * train has finished growing and is now continuing until the end of the
 * duration
 */

@ABendingAbility(name = LavaTrain.NAME, affinity = BendingAffinity.LAVA)
public class LavaTrain extends BendingActiveAbility {
	public final static String NAME = "Lavatrain";
	
	@ConfigurationParameter("Speed")
	public static double SPEED = 5;

	@ConfigurationParameter("Range")
	public static int RANGE = 7;

	@ConfigurationParameter("Train-Width")
	public static int TRAIN_WIDTH = 1;

	@ConfigurationParameter("Random-Width")
	public static int RANDOM_WIDTH = 2;

	@ConfigurationParameter("Random-Chance")
	public static double RANDOM_CHANCE = 0.25;

	@ConfigurationParameter("Reach-Width")
	public static int REACH_WIDTH = 3;

	@ConfigurationParameter("Max-Duration")
	public static long DURATION = 20000; // ms

	@ConfigurationParameter("Cooldown-Factor")
	public static int COOLDOWN_FACTOR = 2;

	private Location origin;
	private Block safePoint;
	private Location current;
	private Vector direction;
	private long interval;

	private Map<Block, TempBlock> affecteds = new HashMap<Block, TempBlock>();

	private long time;

	public LavaTrain(RegisteredAbility register, Player player) {
		super(register, player);
		interval = (long) (1000. / SPEED);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			if (!this.player.isSneaking()) {
				return false;
			}
			this.safePoint = this.player.getLocation().getBlock();
			this.time = this.startedTime;

			this.direction = this.player.getEyeLocation().getDirection().clone();
			this.direction.setY(0);
			this.direction = this.direction.normalize();
			this.origin = this.player.getLocation().clone().add(this.direction.clone().multiply(TRAIN_WIDTH + 1 + RANDOM_WIDTH));
			this.origin.setY(this.origin.getY() - 1);
			this.current = this.origin.clone();

			setState(BendingAbilityState.PREPARING);
			
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isLocationProtectedFromBending(this.player, NAME, this.current)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if (MathUtils.doubleEquals(this.direction.getX(), 0) && MathUtils.doubleEquals(this.direction.getZ(), 0)) {
			if (!getState().equals(BendingAbilityState.PROGRESSING)) {
				this.affectBlocks(this.current, REACH_WIDTH);
				setState(BendingAbilityState.PROGRESSING);
			} else {
				if ((System.currentTimeMillis() - this.time) > DURATION) {
					remove();
				}
				return;
			}
		}
		if ((System.currentTimeMillis() - this.time) >= interval) {
			if (this.origin.distance(this.current) >= RANGE) {
				if (!getState().equals(BendingAbilityState.PROGRESSING)) {
					this.affectBlocks(this.current, REACH_WIDTH);
					setState(BendingAbilityState.PROGRESSING);
				} else {
					if ((System.currentTimeMillis() - this.time) > DURATION) {
						remove();
					}
					return;
				}
			} else {
				this.affectBlocks(this.current, TRAIN_WIDTH);
			}

			if (this.affecteds.isEmpty()) {
				remove();
				return;
			}

			this.time = System.currentTimeMillis();
			this.current = this.current.clone().add(this.direction);
		}
	}

	private void affectBlocks(Location current, int width) {
		List<Block> safe = BlockTools.getBlocksOnPlane(this.safePoint.getLocation(), 1);
		safe.add(this.safePoint);

		for (int i = -1; i <= 2; i++) {
			Location tmp = current.clone();
			tmp.setY(current.getY() + i);
			List<Block> potentialsBlocks = BlockTools.getBlocksOnPlane(tmp, width);
			// Add small random in generation
			List<Block> potentialsAddsBlocks = BlockTools.getBlocksOnPlane(tmp, width + RANDOM_WIDTH);
			for (Block potentialsBlock : potentialsAddsBlocks) {
				if (Math.random() < RANDOM_CHANCE) {
					potentialsBlocks.add(potentialsBlock);
				}
			}

			for (Block potentialsBlock : potentialsBlocks) {
				if (BlockTools.isEarthbendable(this.player, NAME, potentialsBlock) && !TempBlock.isTempBlock(potentialsBlock)) {
					// Do not let block behind bender to be bend, this whill be
					// stupid
					if (!safe.contains(potentialsBlock)) {
						//this.affecteds.put(potentialsBlock, new TempBlock(potentialsBlock, Material.LAVA));
						this.affecteds.put(potentialsBlock, TempBlock.makeTemporary(potentialsBlock, Material.LAVA));
						potentialsBlock.getWorld().playEffect(potentialsBlock.getLocation(), Effect.EXTINGUISH, 10);
					}
				}
			}
		}
	}

	@Override
	public void stop() {
		for (TempBlock affected : this.affecteds.values()) {
			affected.revertBlock();
		}
		this.affecteds.clear();
		this.bender.cooldown(NAME, DURATION * COOLDOWN_FACTOR);
	}

	public static LavaTrain getLavaTrain(Block b) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return null;
		}

		for (BendingAbility ab : instances.values()) {
			LavaTrain train = (LavaTrain) ab;
			if (train.affecteds.containsKey(b)) {
				return train;
			}
		}
		return null;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return true;
		}

		return !instances.containsKey(this.player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

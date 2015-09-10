package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

// TODO : Use ability State instead of reached

@BendingAbility(name="Lavatrain", element=BendingType.Earth, specialization=BendingSpecializationType.Lavabend)
public class LavaTrain extends ActiveAbility {
	public static double speed = 5;
	private static long interval = (long) (1000. / speed);
	
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
	public static long DURATION = 20000; //ms
	
	@ConfigurationParameter("Cooldown-Factor")
	public static int COOLDOWN_FACTOR = 2;
	
	private Location origin;
	private Block safePoint;
	private Location current;
	private Vector direction;
	private boolean reached = false;
	
	private Map<Block, BlockState> affecteds = new HashMap<Block, BlockState>();

	private long time;
	
	public LavaTrain (Player player) {
		super(player, null);
	}
	
	@Override
	public boolean swing () {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;
			case CanStart:
				if (!this.player.isSneaking()) {
					return false;
				}
				this.safePoint = this.player.getLocation().getBlock();
				this.time = this.startedTime;
				
				this.direction = this.player.getEyeLocation().getDirection().clone();
				this.direction.setY(0);
				this.direction = this.direction.normalize();
				this.origin = this.player.getLocation().clone()
						.add(this.direction.clone().multiply(TRAIN_WIDTH + 1 + RANDOM_WIDTH));
				this.origin.setY(this.origin.getY() - 1);
				this.current = this.origin.clone();
				
				setState(AbilityState.Preparing);
				AbilityManager.getManager().addInstance(this);
				return false;
			case Preparing:
			case Prepared:
			case Progressing:
			case Ending:
			case Ended:
			case Removed:
			default:
				return false;
		}
	}
	
	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.LavaTrain, this.current)) {
			return false;
		}
		
		if((this.direction.getX() == 0) && (this.direction.getZ() == 0)) {
			if(!this.reached) {
				this.affectBlocks(this.current, REACH_WIDTH);
				this.reached = true;
			} else {
				if ((System.currentTimeMillis() - this.time) > DURATION) {
					return false;
				}
				return true;
			}
		}
		if ((System.currentTimeMillis() - this.time) >= interval) {
			if(this.origin.distance(this.current) >= RANGE) {
				if(!this.reached) {
					this.affectBlocks(this.current, REACH_WIDTH);
					this.reached = true;
				} else {
					if ((System.currentTimeMillis() - this.time) > DURATION) {
						return false;
					}
					return true;
				}
			} else {
				this.affectBlocks(this.current, TRAIN_WIDTH);
			}
			
			if(this.affecteds.isEmpty()) {
				return false;
			}
			
			this.time = System.currentTimeMillis();
			this.current = this.current.clone().add(this.direction);
		}
		
		return true;
	}
	
	@Override
	public void remove () {
		this.bender.cooldown(Abilities.LavaTrain, DURATION * COOLDOWN_FACTOR); //TODO : Real duration * COOLDOWN_FACTOR
		super.remove();
	}
	
	private void affectBlocks(Location current, int width) {
		List<Block> safe = BlockTools.getBlocksOnPlane(this.safePoint.getLocation(), 1);
		safe.add(this.safePoint);
		
		for(int i=-1; i <= 2 ; i++) {
			Location tmp = current.clone();
			tmp.setY(current.getY()+i);
			List<Block> potentialsBlocks = BlockTools.getBlocksOnPlane(tmp, width);
			//Add small random in generation
			List<Block> potentialsAddsBlocks = BlockTools.getBlocksOnPlane(tmp, width+RANDOM_WIDTH);
			for(Block potentialsBlock : potentialsAddsBlocks) {
				if(Math.random() < RANDOM_CHANCE) {
					potentialsBlocks.add(potentialsBlock);
				}
			}
			
			for(Block potentialsBlock : potentialsBlocks) {
				if (BlockTools.isEarthbendable(this.player, Abilities.LavaTrain, potentialsBlock)
						&& !BlockTools.isTempBlock(potentialsBlock)) {
					//Do not let block behind bender to be bend, this whill be stupid
					if(!safe.contains(potentialsBlock)) {
						this.affecteds.put(potentialsBlock, potentialsBlock.getState());
						potentialsBlock.setType(Material.LAVA);
					}
				}
			}
		}
	}
	
	
	@Override
	public void stop () {
		for (BlockState affected : this.affecteds.values()) {
			affected.update(true);
		}
		this.affecteds.clear();
	}
	
	public static boolean isLavaPart(Block block) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.LavaTrain);
		if (instances == null) {
			return false;
		}
		for (IAbility ab : instances.values()) {
			LavaTrain train = (LavaTrain) ab;
			if (train.affecteds.containsKey(block)) {
				return true;
			}
		}
		return false;
	}
	
	public static LavaTrain getLavaTrain(Block b) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.LavaTrain);
		if (instances == null) {
			return null;
		}
		
		for (IAbility ab : instances.values()) {
			LavaTrain train = (LavaTrain) ab;
			if (train.affecteds.containsKey(b)) {
				return train;
			}
		}
		return null;
	}
	
	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.LavaTrain);
		if (instances == null) {
			return true;
		}

		return !instances.containsKey(this.player);
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.LavaTrain;
	}
}

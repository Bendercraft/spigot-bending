package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

//Tchou tchou !
public class LavaTrain implements IAbility {
	private static Map<Player, LavaTrain> instances = new HashMap<Player, LavaTrain>();
	
	//public static double speed = ConfigManager.lavaTrainSpeed;
	public static double speed = 5;
	private static long interval = (long) (1000. / speed);
	public static int range = ConfigManager.lavaTrainRange;
	public static int trainWidth = ConfigManager.lavaTrainWidth;
	public static int randomWidth = ConfigManager.lavaTrainRandomWidth;
	public static double randomChance = ConfigManager.lavaTrainRandomChance;
	public static int reachWidth = ConfigManager.lavaTrainReachWidth;
	public static long keepAlive = ConfigManager.lavaTrainDuration; //ms
	private static final byte full = 0x0;
	
	private IAbility parent;
	private Location origin;
	private Block safePoint;
	private Location current;
	private Vector direction;
	private BendingPlayer bPlayer;
	private Player player;
	private boolean reached = false;
	
	private List<Block> affecteds = new LinkedList<Block>();

	private long time;
	
	public LavaTrain(Player player, IAbility parent) {
		if(instances.containsKey(player)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if(bPlayer == null) {
			return;
		}
		
		if (bPlayer.isOnCooldown(Abilities.LavaTrain))
			return;
		if(!EntityTools.canBend(player, Abilities.LavaTrain)) {
			return;
		}
		
		this.parent = parent;
		this.player = player;
		this.bPlayer = bPlayer;
		this.safePoint = this.bPlayer.getPlayer().getLocation().getBlock();
		
		this.direction = player.getEyeLocation().getDirection().clone();
		this.direction.setY(0);
		this.direction = this.direction.normalize();
		origin = player.getLocation().clone().add(direction.clone().multiply(trainWidth+1+randomWidth));
		origin.setY(origin.getY()-1);
		current = origin.clone();
		
		time = System.currentTimeMillis();
		bPlayer.cooldown(Abilities.LavaTrain);
		instances.put(player, this);
	}
	
	public boolean progress() {
		if(bPlayer == null || bPlayer.getPlayer() == null) {
			return false;
		}
		
		if (bPlayer.getPlayer().isDead() || !bPlayer.getPlayer().isOnline()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(bPlayer.getPlayer(), Abilities.LavaTrain, current)) {
			return false;
		}
		if(this.direction.getX() == 0 && this.direction.getZ() == 0) {
			if(!reached) {
				this.affectBlocks(current, reachWidth);
				reached = true;
			} else {
				if (System.currentTimeMillis() - time > keepAlive) {
					return false;
				}
				return true;
			}
		}
		if (System.currentTimeMillis() - time >= interval) {
			if(origin.distance(current) >= range) {
				if(!reached) {
					this.affectBlocks(current, reachWidth);
					reached = true;
				} else {
					if (System.currentTimeMillis() - time > keepAlive) {
						return false;
					}
					return true;
				}
			} else {
				this.affectBlocks(current, trainWidth);
			}
			
			if(affecteds.isEmpty()) {
				return false;
			}
			
			time = System.currentTimeMillis();
			current = current.clone().add(direction);
		}
		
		return true;
	}
	
	private void affectBlocks(Location current, int width) {
		List<Block> safe = BlockTools.getBlocksOnPlane(this.safePoint.getLocation(), 1);
		safe.add(safePoint);
		
		for(int i=-1; i <= 2 ; i++) {
			Location tmp = current.clone();
			tmp.setY(current.getY()+i);
			List<Block> potentialsBlocks = BlockTools.getBlocksOnPlane(tmp, width);
			//Add small random in generation
			List<Block> potentialsAddsBlocks = BlockTools.getBlocksOnPlane(tmp, width+randomWidth);
			for(Block potentialsBlock : potentialsAddsBlocks) {
				if(Math.random() < randomChance) {
					potentialsBlocks.add(potentialsBlock);
				}
			}
			
			for(Block potentialsBlock : potentialsBlocks) {
				if(BlockTools.isEarthbendable(bPlayer.getPlayer(),Abilities.LavaTrain, potentialsBlock) && !TempBlock.isTempBlock(potentialsBlock)) {
					//Do not let block behind bender to be bend, this whill be stupid
					if(!safe.contains(potentialsBlock)) {
						new TempBlock(potentialsBlock, Material.LAVA, full);
						affecteds.add(potentialsBlock);
					}
				}
			}
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void remove() {
		for(Block affected : affecteds) {
			TempBlock temp = TempBlock.get(affected);
			if(temp != null) {
				temp.revertBlock();
			}
		}
		affecteds.clear();
		instances.remove(this.bPlayer.getPlayer());
	}
	
	public static void progressAll() {
		List<LavaTrain> toRemove = new LinkedList<LavaTrain>();
		for(LavaTrain train : instances.values()) {
			if (!train.progress()) {
				toRemove.add(train);
			}
		}
		
		for(LavaTrain train : toRemove) {
			train.remove();
		}
	}
	
	public static void removeAll() {
		List<LavaTrain> toRemove = new LinkedList<LavaTrain>();
		toRemove.addAll(instances.values());
		
		for(LavaTrain train : toRemove) {
			train.remove();
		}
	}
	
	public static boolean isLavaPart(Block block) {
		for(LavaTrain train : instances.values()) {
			if(train.affecteds.contains(block)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IAbility getParent() {
		return parent;
	}
	
	public static LavaTrain getLavaTrain(Block b) {
		for (LavaTrain train : instances.values()){
			if (train.affecteds.contains(b)) {
				return train;
			}
		}	
		return null;
	}

}

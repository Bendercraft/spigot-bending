package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

//Tchou tchou !
public class LavaTrain implements IAbility {
	private static Map<Player, LavaTrain> instances = new HashMap<Player, LavaTrain>();
	
	//public static double speed = ConfigManager.lavaTrainSpeed;
	public static double speed = 10;
	private static long interval = (long) (1000. / speed);
	public static int range = 7;
	public static int trainWidth = 1;
	public static int reachWidth = 3;
	public static int keepAlive = 10000; //ms
	private static final byte full = 0x0;
	
	private IAbility parent;
	private Location origin;
	private Location current;
	private Vector direction;
	private BendingPlayer player;
	private boolean reached = false;
	
	private List<Block> affecteds = new LinkedList<Block>();

	private long time;
	
	public LavaTrain(Player player, IAbility parent) {
		if(instances.containsKey(player)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.isOnCooldown(Abilities.LavaTrain))
			return;
		if(!EntityTools.canBend(player, Abilities.LavaTrain)) {
			return;
		}
		
		this.parent = parent;
		this.player = bPlayer;
		
		this.direction = player.getEyeLocation().getDirection().clone();
		this.direction.setY(0);
		origin = player.getLocation().clone().add(direction.clone().multiply(trainWidth+1));
		origin.setY(origin.getY()-1);
		current = origin.clone();
		
		time = System.currentTimeMillis();
		bPlayer.cooldown(Abilities.LavaTrain);
		instances.put(player, this);
	}
	
	public boolean progress() {
		if (player.getPlayer().isDead() || !player.getPlayer().isOnline()) {
			return false;
		}
		if (Tools.isRegionProtectedFromBuild(player.getPlayer(), Abilities.LavaTrain, current)) {
			return false;
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
		for(int i=0; i <= 2 ; i++) {
			Location tmp = current.clone();
			tmp.setY(current.getY()+i);
			List<Block> potentialsBlocks = BlockTools.getBlocksOnPlane(tmp, width);
			for(Block potentialsBlock : potentialsBlocks) {
				if(isBendable(potentialsBlock.getType()) && !TempBlock.isTempBlock(potentialsBlock)) {
					new TempBlock(potentialsBlock, Material.LAVA, full);
					affecteds.add(potentialsBlock);
				}
			}
		}
	}
	
	public void remove() {
		for(Block affected : affecteds) {
			TempBlock temp = TempBlock.get(affected);
			if(temp != null) {
				temp.revertBlock();
			}
		}
		affecteds.clear();
		instances.remove(this.player.getPlayer());
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
	
	private boolean isBendable(Material material) {
		if(material.equals(Material.DIRT)) {
			return true;
		}
		if(material.equals(Material.GRASS)) {
			return true;
		}
		if(material.equals(Material.GRAVEL)) {
			return true;
		}
		return false;
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

}

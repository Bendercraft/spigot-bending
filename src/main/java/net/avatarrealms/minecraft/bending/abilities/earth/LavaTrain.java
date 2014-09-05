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

import net.avatarrealms.minecraft.bending.Bending;
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
	public static double speed = 2;
	public static int range = 7;
	public static int trainWidth = 3;
	public static int reachWidth = 5;
	public static int maxticks = 10000;
	private static final byte full = 0x0;
	
	private IAbility parent;
	private Location origin;
	private Location current;
	private Vector direction;
	private BendingPlayer player;
	private int ticks = 0;
	
	private List<Block> affecteds = new LinkedList<Block>();
	
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
		origin = player.getEyeLocation();
		current = origin;
		direction = player.getEyeLocation().getDirection().normalize();
		
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
		if(origin.distance(current) > range) {
			ticks++;
			if (ticks > maxticks) {
				return false;
			}
		} else if(origin.distance(current) == range) {
			List<Block> potentialsBlocks = BlockTools.getBlocksAroundPoint(current, reachWidth);
			for(Block potentialsBlock : potentialsBlocks) {
				if(potentialsBlock.getType().equals(Material.DIRT)) {
					new TempBlock(potentialsBlock, Material.LAVA, full);
					affecteds.add(potentialsBlock);
				}
			}
		} else {
			List<Block> potentialsBlocks = BlockTools.getBlocksAroundPoint(current, trainWidth);
			for(Block potentialsBlock : potentialsBlocks) {
				if(potentialsBlock.getType().equals(Material.DIRT)) {
					new TempBlock(potentialsBlock, Material.LAVA, full);
					affecteds.add(potentialsBlock);
				}
			}
		}
		
		//Prepare next current
		double speedfactor = speed * (Bending.time_step / 1000.);
		current.add(direction.clone().multiply(speedfactor));
		
		return true;
	}
	
	public void remove() {
		for(Block affected : affecteds) {
			TempBlock temp = TempBlock.get(affected);
			if(temp != null) {
				temp.revertBlock();
			}
		}
		affecteds.clear();
		instances.remove(this.player);
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
	
	@Override
	public IAbility getParent() {
		return parent;
	}

}

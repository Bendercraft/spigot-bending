package net.bendercraft.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = LavaFlow.NAME, affinity = BendingAffinity.LAVA)
public class LavaFlow extends BendingActiveAbility {
	public final static String NAME = "LavaFlow";
	
	@ConfigurationParameter("Speed")
	public static double SPEED = 10;
	
	@ConfigurationParameter("Area")
	public static int AREA = 2;
	
	@ConfigurationParameter("Cooldown")
	public static int COOLDOWN = 5000;
	
	@ConfigurationParameter("Select-Range")
	public static double SELECT_RANGE = 10;
	
	@ConfigurationParameter("Time-to-act")
	public static double TIME_TO_ACT = 2;
	
	@ConfigurationParameter("Column-Height")
	public static int COLUMN_HEIGHT = 3;
	
	@ConfigurationParameter("Stream-Reach")
	public static int STREAM_REACH = 8;
	
	private List<Location> streams;
	private Location current;
	private long interval;
	private int columnHeight;
	private int currentReach;
	private long time;
	private Vector direction;
	
	private List<TempBlock> blocks;
	
	public LavaFlow(RegisteredAbility register, Player player) {
		super(register, player);
		
		interval = (long) (1000. / SPEED);
		columnHeight = 0;
		streams = new LinkedList<Location>();
		blocks = new LinkedList<TempBlock>();
		time = this.startedTime;
		currentReach = 0;
	}
	
	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.PREPARED) {
			direction = player.getEyeLocation().getDirection().normalize().setY(-((double)COLUMN_HEIGHT/(double)STREAM_REACH));
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
			time = System.currentTimeMillis();
			setState(BendingAbilityState.PROGRESSING);
		} else { 
			remove();
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			Block block = EntityTools.getTargetBlock(player, SELECT_RANGE);
			if(block == null) {
				return false;
			}
			List<Block> test = BlockTools.getBlocksOnPlane(block.getLocation(), AREA);
			for(Block temp : test) {
				if(temp.getType() == Material.LAVA) {
					if(!TempBlock.isTempBlock(block) || LavaTrain.getLavaTrain(block) != null) {
						streams.add(temp.getLocation());
					}
				}
			}
			if(streams.isEmpty()) {
				return false;
			}
			setState(BendingAbilityState.PREPARING);
			bender.cooldown(this, COOLDOWN);
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (current != null && ProtectionManager.isLocationProtectedFromBending(this.player, register, current)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if(getState() == BendingAbilityState.PREPARING) {
			if (now - time >= interval) {
				time = now;
				List<Location> futurStreams = new LinkedList<Location>();
				for(Location loc : streams) {
					loc = loc.add(0, 1, 0);
					if(BlockTools.isAir(loc.getBlock())) {
						futurStreams.add(loc);
						blocks.add(TempBlock.makeTemporary(this, loc.getBlock(), Material.LAVA, false));
					}
				}
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
				streams = futurStreams;
				
				columnHeight++;
				
				if(columnHeight >= COLUMN_HEIGHT) {
					setState(BendingAbilityState.PREPARED);
				}
			}
		} else if(getState() == BendingAbilityState.PREPARED) {
			if(now > time + TIME_TO_ACT*1000) {
				remove();
			}
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			if (now - time >= interval) {
				time = now;
				
				List<Location> futurStreams = new LinkedList<Location>();
				for(Location loc : streams) {
					loc = loc.add(direction);
					if(BlockTools.isAir(loc.getBlock()) || (TempBlock.isTempBlock(loc.getBlock()) && blocks.contains(TempBlock.get(loc.getBlock())))) {
						futurStreams.add(loc);
						blocks.add(TempBlock.makeTemporary(this, loc.getBlock(), Material.LAVA, false));
					}
				}
				streams = futurStreams;
				
				currentReach++;
				
				if(currentReach > STREAM_REACH) {
					remove();
				}
			}
		}
	}

	@Override
	public void stop() {
		for(TempBlock block : blocks) {
			block.revertBlock();
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

package net.bendercraft.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.listeners.BendingDenyItem;
import net.bendercraft.spigot.bending.listeners.BendingPlayerListener;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;


/**
 * FireBlade works by giving player a GOLDEN_SWORD, that is allowed by bending {@link BendingDenyItem}.
 * This sword does no damage @link {@link BendingPlayerListener} but calls {@link #affect(LivingEntity)}
 *
 */
@ABendingAbility(name = WaterWhip.NAME, element = BendingElement.WATER, shift=false)
public class WaterWhip extends BendingActiveAbility {
	public final static String NAME = "WaterWhip";
	
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;

	@ConfigurationParameter("Length")
	private static int LENGTH = 14;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 35;
	
	@ConfigurationParameter("Cooldown")
	private static int COOLDOWN = 6000;
	
	private ArrayList<Dot> points = new ArrayList<Dot>();
	private Location previous;
	
	private List<TempBlock> blocks = new LinkedList<TempBlock>();

	private long time;
	private long interval;

	public WaterWhip(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.interval = (long) (1000. / SPEED);
	}

	@Override
	public boolean swing() {
		if(isState(BendingAbilityState.START)) {
			previous = player.getEyeLocation().clone();
			Location eye = previous.clone();
			for(int i=0 ; i < LENGTH ; i++) {
				Dot dot = new Dot();
				dot.point = eye.clone().add(previous.getDirection().clone().multiply(i+1));
				dot.next = dot.point.clone();
				points.add(dot);
			}
			time = System.currentTimeMillis();
			setState(BendingAbilityState.PROGRESSING);
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			remove();
		}
		return false;
	}
	
	@Override
	public boolean canBeInitialized() {
		if(!super.canBeInitialized()) {
			return false;
		}
		if(!BlockTools.isWaterBased(player.getLocation().getBlock())) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if(!BlockTools.isWaterBased(player.getLocation().getBlock())) {
			return false;
		}
		if (!NAME.equals(EntityTools.getBendingAbility(player))) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if ((System.currentTimeMillis() - time) >= interval) {
			time = System.currentTimeMillis();
			for(TempBlock block : blocks) {
				block.revertBlock();
			}
			List<Block> old = new LinkedList<Block>();
			blocks.forEach(x -> old.add(x.getBlock()));
			blocks.clear();
			boolean broken = false;
			Location direction = player.getEyeLocation().clone();
			for(int i=0 ; i < points.size() ; i++) {
				Dot dot = points.get(i);
				if(i != 0 && !broken) {
					// Get all blocks between previous location and current one, 
					// so that if movement is huge, it still appears as a continuous stream
					Vector temp = points.get(i-1).point.clone().subtract(dot.point).toVector().normalize();
					double distance = points.get(i-1).point.distance(dot.point);
					double j = 0;
					while(j < distance && !broken) {
						Block block = dot.point.clone().add(temp.clone().multiply(j)).getBlock();
						if(!BlockTools.isWaterBased(block) && block.getType() != Material.AIR) {
							broken = true;
						}
						if(!broken) {
							blocks.add(TempBlock.makeTemporary(block, Material.WATER, false));
							if(!old.contains(block)) {
								for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(block.getLocation(), 1)) {
									affect(entity);
								}
							}
						}
						j = j +0.4;
					}
				}
			}
			for(int i=points.size()-1 ; i >= 0 ; i--) {
				Dot dot = points.get(i);
				dot.point = dot.next.clone();
				if(i == 0) {
					dot.next = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().clone().normalize());
				} else {
					dot.next = points.get(i-1).next.clone().add(points.get(i-1).next.getDirection().clone().normalize());
				}
			}
			previous = direction.clone();
		}
	}

	@Override
	public void stop() {
		for(TempBlock block : blocks) {
			block.revertBlock();
		}
		blocks.clear();
		bender.cooldown(this, COOLDOWN);
	}
	
	public void affect(Entity entity) {
		DamageTools.damageEntity(bender, entity, DAMAGE);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	private class Dot {
		public Location point;
		public Location next;
	}
}

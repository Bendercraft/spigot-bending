package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EarthGrab {

	private static double range = ConfigManager.earthGrabRange;
	private static Map<Integer, EarthGrab> instances = new HashMap<Integer, EarthGrab>();
	private static Integer ID = Integer.MIN_VALUE;
	private int id;
	private List<EarthColumn> columns = new ArrayList<EarthColumn>();
	private boolean self;
	private BendingPlayer bPlayer;
	private Player bender;
	private LivingEntity target;

 	public EarthGrab(Player player, boolean self) {
		// Tools.verbose("initiating");
		this.self = self;
		this.bender = player;
		bPlayer = BendingPlayer.getBendingPlayer(bender);
		if (bPlayer.isOnCooldown(Abilities.EarthGrab))
			return;
		
		if (self) {
			grabEntity(bender,bender);
		}
		else {
			Location origin = player.getEyeLocation();
			Vector direction = origin.getDirection();
			double lowestdistance = range + 1;
			Entity closestentity = null;
			for (Entity entity : EntityTools.getEntitiesAroundPoint(origin, range)) {
				if (Tools.getDistanceFromLine(direction, origin,
						entity.getLocation()) <= 3
						&& (entity instanceof LivingEntity)
						&& (entity.getEntityId() != bender.getEntityId())) {
					double distance = origin.distance(entity.getLocation());
					if (distance < lowestdistance) {
						closestentity = entity;
						lowestdistance = distance;
					}
				}
			}	
			grabEntity(bender,closestentity);	
		}
		if (target != null) {
			if (ID == Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			}
			id = ID++;
			instances.put(id,this);
		}
		
	}
	
	public void grabEntity(Player player, Entity entity) {
		if (entity != null) {
			if (entity instanceof LivingEntity) {
				target = (LivingEntity)entity;
				Location location = entity.getLocation();
				Location cLoc[] = new Location[4];
				cLoc[0] = location.clone().add(0,-1,-1);
				cLoc[1] = location.clone().add(0,-1,1);
				cLoc[2] = location.clone().add(-1,-1,0);
				cLoc[3] = location.clone().add(1,-1,0);
				if (BlockTools.isEarthbendable(player,cLoc[0].getBlock()) &&
						BlockTools.isEarthbendable(player,cLoc[1].getBlock()) &&
						BlockTools.isEarthbendable(player,cLoc[2].getBlock()) &&
						BlockTools.isEarthbendable(player,cLoc[3].getBlock())) {
					int duration;
					if (self) {
						duration = 100; // 5 secs
					}
					else {
						duration = 6000; // 5 minutes
					}
					PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, duration, 150); // The entity cannot move
					PotionEffect jumpless = new PotionEffect(PotionEffectType.JUMP, duration, 150); // The entity cannot jump
					target.addPotionEffect(slowness);
					target.addPotionEffect(jumpless);
					double x = (int)target.getLocation().getX();
					double y = (int)target.getLocation().getY();
					double z = (int)target.getLocation().getZ();

					x = (x<0)? x-0.5 : x+0.5 ;
					
					z = (z<0)? z-0.5 : z+0.5;
					
					target.teleport(new Location(target.getWorld(),x,y,z));
					// To be sure the guy is locked in the grab
					
					if (target instanceof Player) {
						EntityTools.grab((Player) target,System.currentTimeMillis());
					}
					
				}
				//player.sendMessage("Location : " + target.getLocation().getX() + " " + target.getLocation().getY() + " " + target.getLocation().getZ());
						
				columns.add(new EarthColumn(player, cLoc[0], this));
				columns.add(new EarthColumn(player, cLoc[1], this));
				columns.add(new EarthColumn(player, cLoc[2], this));
				columns.add(new EarthColumn(player, cLoc[3], this));
				
				if (!columns.isEmpty()) // never empty ?
					bPlayer.cooldown(Abilities.EarthGrab);
				}
			
			}	
		}	

	public static String getDescription() {
		return "To use, simply left-click while targeting a creature within range. "
				+ "This ability will erect a circle of earth to trap the creature in.";
	}
	
	public static Integer blockInEarthGrab(Block block) {
		for (Integer ID : instances.keySet()) {
			for (EarthColumn column : instances.get(ID).columns) {
				if (column.blockInAffectedBlocks(block)) {
					return ID;
				}
			}
		}
		return null;
	}
	
	public static boolean revertEarthGrab(Integer ID) {
		if (ID == null) {
			return false;
		}
		for (EarthColumn column : instances.get(ID).columns) {
			for (Block block : column.getAffectedBlocks()) {
				BlockTools.revertBlock(block);
			}
		}		
		instances.get(ID).columns.clear();
		LivingEntity targ = instances.get(ID).target;
		if (targ!= null) {
			instances.get(ID).target.removePotionEffect(PotionEffectType.SLOW);
			instances.get(ID).target.removePotionEffect(PotionEffectType.JUMP);
			if (targ instanceof Player) {
				EntityTools.unGrab((Player)targ);
			}
		}
	
		instances.remove(ID);
		
		return true;
	}
}

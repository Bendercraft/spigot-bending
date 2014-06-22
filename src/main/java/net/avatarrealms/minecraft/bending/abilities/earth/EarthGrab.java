package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
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

	public EarthGrab(Player player, boolean self) {
		// Tools.verbose("initiating");
		this.self = self;
		bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.isOnCooldown(Abilities.EarthGrab))
			return;
		
		if (self) {
			grabEntity(player,player);
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
						&& (entity.getEntityId() != player.getEntityId())) {
					double distance = origin.distance(entity.getLocation());
					if (distance < lowestdistance) {
						closestentity = entity;
						lowestdistance = distance;
					}
				}
			}	
			grabEntity(player,closestentity);	
		}
		id = ID;
		instances.put(id,this);
	}
	
	public void grabEntity(Player player, Entity entity) {
		if (entity != null) {
			if (entity instanceof LivingEntity) {
				LivingEntity lEnt = (LivingEntity)entity;
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
					lEnt.addPotionEffect(slowness);
					lEnt.addPotionEffect(jumpless);
					lEnt.teleport(new Location(lEnt.getWorld(),(int)(lEnt.getLocation().getX())+0.5, (int)(lEnt.getLocation().getY())+0.5, (int)(lEnt.getLocation().getZ())+0.5));
					// To be sure the guy is locked in the grab
					
				}
				player.sendMessage("Location : " + lEnt.getLocation().getX() + " " + lEnt.getLocation().getY() + " " + lEnt.getLocation().getZ());
						
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
}

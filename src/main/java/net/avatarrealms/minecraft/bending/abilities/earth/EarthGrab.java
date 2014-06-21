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
	private List<EarthColumn> columns = new ArrayList<EarthColumn>();

	public EarthGrab(Player player, boolean self) {
		// Tools.verbose("initiating");

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(Abilities.EarthGrab))
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
	}
	
	public void grabEntity(Player player, Entity entity) {
		if (entity != null) {
			if (entity instanceof LivingEntity) {
				LivingEntity lEnt = (LivingEntity)entity;
				PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 600, 150); // The entity cannot move
				PotionEffect jumpless = new PotionEffect(PotionEffectType.JUMP, 600, 150); // The entity cannot jump
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				lEnt.addPotionEffect(slowness);
				lEnt.addPotionEffect(jumpless);
				
				Location location = entity.getLocation();
				Location cLoc = location.clone();
				cLoc.add(0,-1,-1);
				columns.add(new EarthColumn(player, cLoc, 1));
				cLoc.add(0,0,2);
				columns.add(new EarthColumn(player, cLoc, 1));
				cLoc.add(-1,0,-1);
				columns.add(new EarthColumn(player, cLoc, 1));
				cLoc.add(2,0,0);
				columns.add(new EarthColumn(player, cLoc, 1));
				
				if (!columns.isEmpty())
					bPlayer.cooldown(Abilities.EarthGrab);
				}
			
			}	
		}	

	public static String getDescription() {
		return "To use, simply left-click while targeting a creature within range. "
				+ "This ability will erect a circle of earth to trap the creature in.";
	}
}

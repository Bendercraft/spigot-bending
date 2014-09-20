package net.avatarrealms.minecraft.bending.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingSpecializationType;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.chi.Paralyze;
import net.avatarrealms.minecraft.bending.abilities.energy.AstralProjection;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.water.Bloodbending;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class EntityTools {
	
	public static Map<Player, Long> blockedChis = new HashMap<Player, Long>();
	public static Map<Player, Long> grabedPlayers = new HashMap<Player, Long>();
	public static List<Player> toggledBending = new ArrayList<Player>();
	
	public static boolean isBender(Player player, BendingType type) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return false;
		return bPlayer.isBender(type);
	}
	
	public static boolean isSpecialized(Player player, BendingSpecializationType specialization) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return false;
		return bPlayer.isSpecialized(specialization);
	}
	
	public static boolean isBender(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}		
		return true;
	}
	
	public static List<BendingType> getBendingTypes(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return null;
		return bPlayer.getBendingTypes();
	}
	
	public static boolean hasAbility(Player player, Abilities ability) {
		// return config.hasAbility(player, ability);
		return canBend(player, ability);
	}
	
	public static Abilities getBendingAbility(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null)
			return null;
		return bPlayer.getAbility();
	}
	

	
	public static String getPermissionKey(Abilities ability) {
		return "bending."+ability.getElement().name().toLowerCase()+"."+ability.name().toLowerCase();
	}
	
	public static boolean hasPermission(Player player, Abilities ability) {
		if (ability == Abilities.AvatarState
				&& player.hasPermission("bending.admin.AvatarState")) {
			return true;
		}
		if (ability == Abilities.AstralProjection 
				&& player.hasPermission("bending.admin.AstralProjection")){
			return true;
		}

		if (Abilities.isAirbending(ability)
				&& player.hasPermission("bending.air." + ability)) {
			return true;
		}
		if (Abilities.isWaterbending(ability)
				&& player.hasPermission("bending.water." + ability)) {
			return true;
		}
		if (Abilities.isEarthbending(ability)
				&& player.hasPermission("bending.earth." + ability)) {
			return true;
		}
		if (Abilities.isFirebending(ability)
				&& player.hasPermission("bending.fire." + ability)) {
			return true;
		}
		if (Abilities.isChiBlocking(ability)
				&& player.hasPermission("bending.chiblocker." + ability)) {
			return true;
		}
		if (Abilities.isChiBlocking(ability)
				&& player.hasPermission("bending.chiblocking." + ability)) {
			return true;
		}
		return false;
	}
	
	public static boolean canBend(Player player, Abilities ability) {
		if (ability == null) {
			return false;
		}

		if (player == null) {
			return false;
		}
		
		if (!hasPermission(player, ability)) {
			return false;
		}

		if (ability == Abilities.AvatarState) {
			return true;
		}
		
		if(toggledBending(player)) {
			return false;
		}
		
		if ((isChiBlocked(player) 
				|| Bloodbending.isBloodbended(player) 
				|| isGrabed(player)
				|| AstralProjection.isAstralProjecting(player)))
			return false;

		if (Abilities.isAirbending(ability)
				&& !isBender(player, BendingType.Air)) {
			return false;
		}
		if (Abilities.isChiBlocking(ability)
				&& !isBender(player, BendingType.ChiBlocker)) {
			return false;
		}
		if (Abilities.isEarthbending(ability)
				&& !isBender(player, BendingType.Earth)) {
			return false;
		}
		if (Abilities.isFirebending(ability)
				&& !isBender(player, BendingType.Fire)) {
			return false;
		}
		if (Abilities.isWaterbending(ability)
				&& !isBender(player, BendingType.Water)) {
			return false;
		}
		
		if(ability.isSpecialization()) {
			if(!isSpecialized(player, ability.getSpecialization())) {
				return false;
			}
		}

		if (PluginTools.allowharmless && PluginTools.isHarmlessAbility(ability)) {
			return true;
		}
		
		return !Tools.isRegionProtectedFromBuild(player, ability, player.getLocation());
	}
	
	public static boolean canBendPassive(Player player, BendingType type) {
		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player) || isGrabed(player))
				&& !AvatarState.isAvatarState(player))
			return false;
		if (!player.hasPermission("bending." + type + ".passive")) {
			return false;
		}
		if (PluginTools.allowharmless && type != BendingType.Earth)
			return true;
		if (Tools.isRegionProtectedFromBuild(player, null, player.getLocation()))
			return false;
		return true;
	}
	
	public static boolean canPlantbend(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}
		/*
		if (player.hasPermission("bending.water.plantbending")) {
			return true;
		}
		*/
		if (EntityTools.isSpecialized(player, BendingSpecializationType.DrainBend)) {
			return true;
		}
		return false;
	}
	
	public static void blockChi(Player player, long time) {
		blockedChis.put(player,time);
	}
	
	public static boolean isChiBlocked(Player player) {
		if (Paralyze.isParalyzed(player) && !AvatarState.isAvatarState(player))
			return true;
		if (blockedChis.containsKey(player)) {
			long time = System.currentTimeMillis();
			if (time > blockedChis.get(player) + ConfigManager.chiblockduration
					|| AvatarState.isAvatarState(player)) {
				blockedChis.remove(player);
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static void grab(Player player, long time) {
		grabedPlayers.put(player,time);
	}
	
	public static void unGrab(Player player) {
		if (grabedPlayers.containsKey(player)) {
			grabedPlayers.remove(player);
		}
	}
	
	public static boolean isGrabed(Player player) {

		if (grabedPlayers.containsKey(player)) {
			long time = System.currentTimeMillis();
			if (time > grabedPlayers.get(player) + ConfigManager.earthGrabDuration
					|| AvatarState.isAvatarState(player)) {
				grabedPlayers.remove(player);
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static boolean canBeBloodbent(Player player) {
		
		if (player.isOp()) {
			return false;
		}
		
		if (AvatarState.isAvatarState(player)) {
			return false;
		}
			
		if ((isChiBlocked(player)) || isGrabed(player)) {
			return true;
		}
			
		if (canBend(player, Abilities.Bloodbending) && !toggledBending(player)) {
			return false;
		}
			
		return true;
	}
	
	public static boolean toggledBending(Player player) {
		if (toggledBending.contains(player)) {
			return true;
		}
			
		return false;
	}
	
	public static List<Entity> getEntitiesAroundPoint(Location location,
			double radius) {

		List<Entity> entities = location.getWorld().getEntities();
		List<Entity> list = new LinkedList<Entity>();

		for (Entity entity : entities) {
			if (entity.getWorld() == location.getWorld() 
					&& entity.getLocation().distance(location) < radius) {
				list.add(entity);
			}
		}
		return list;
	}
	
	public static List<LivingEntity> getLivingEntitiesAroundPoint(Location location, double radius) {
		List<LivingEntity> list = new LinkedList<LivingEntity>();
		
		for (LivingEntity le : location.getWorld().getLivingEntities()) {
			if (le.getWorld() == location.getWorld()
					&& le.getLocation().distance(location) < radius) {
				list.add(le);
			}
		}
		return list;
	}
	
	public static Location getTargetedLocation(Player player, double range) {
		return getTargetedLocation(player, range,
				Collections.singleton(Material.AIR));
	}

	public static Location getTargetedLocation(Player player,
			double originselectrange, Set<Material> nonOpaque2) {
		Location origin = player.getEyeLocation();
		Vector direction = origin.getDirection();

		BlockIterator iter = new BlockIterator(player,
				(int) originselectrange + 1);
		Block block = iter.next();
		while (iter.hasNext()) {
			block = iter.next();
			if (nonOpaque2.contains(block.getType())) {
				continue;
			}
			break;
		}
		double distance = block.getLocation().distance(origin) - 1.5;
		Location location = origin.add(direction.multiply(distance));

		return location;
	}
	
	public static Block getTargetBlock(Player player, double range) {
		return getTargetBlock(player, range,
				Collections.singleton(Material.AIR));
	}

	public static Block getTargetBlock(Player player,
			double originselectrange, Set<Material> nonOpaque2) {
		BlockIterator iter = new BlockIterator(player,
				(int) originselectrange + 1);
		Block block = iter.next();
		while (iter.hasNext()) {
			block = iter.next();
			if (nonOpaque2.contains(block.getType())) {
				continue;
			}
			break;
		}
		return block;
	}
	
	public static Entity getTargettedEntity(Player player, double range) {
		return getTargettedEntity(player, range, new ArrayList<Entity>());
	}

	public static Entity getTargettedEntity(Player player, double range,
			List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (Entity entity : origin.getWorld().getLivingEntities()) {
			if (avoid.contains(entity)) {
				continue;
			}			
			if (entity.getLocation().distance(origin) < longestr
					&& Tools.getDistanceFromLine(direction, origin,
							entity.getLocation()) < 2
					&& entity.getEntityId() != player.getEntityId()
					&& entity.getLocation().distance(
							origin.clone().add(direction)) < entity
							.getLocation().distance(
									origin.clone().add(
											direction.clone().multiply(-1)))) {
				target = entity;
				longestr = entity.getLocation().distance(origin);
			}
		}
		return target;
	}
	
	public static void damageEntity(Player player, Entity entity, double damage) {
		if (entity instanceof LivingEntity) {
			if (AvatarState.isAvatarState(player)) {
				damage = AvatarState.getValue(damage);
			}

			((LivingEntity) entity).damage(damage, player);
			((LivingEntity) entity)
					.setLastDamageCause(new EntityDamageByEntityEvent(player,
							entity, DamageCause.CUSTOM, damage));
		}
	}
	
	public static boolean isWeapon(Material mat) {
		
		switch(mat) {
		case WOOD_AXE :
		case WOOD_PICKAXE :
		case WOOD_SPADE :
		case WOOD_SWORD :
		case STONE_AXE :
		case STONE_PICKAXE :
		case STONE_SPADE :
		case STONE_SWORD :
		case IRON_AXE :
		case IRON_PICKAXE :
		case IRON_SPADE :
		case IRON_SWORD :
		case GOLD_AXE :
		case GOLD_PICKAXE :
		case GOLD_SPADE :
		case GOLD_SWORD :
		case DIAMOND_AXE :
		case DIAMOND_PICKAXE :	
		case DIAMOND_SPADE :
		case DIAMOND_SWORD :
			return true;
					
		default : return false;
		}
	}
}

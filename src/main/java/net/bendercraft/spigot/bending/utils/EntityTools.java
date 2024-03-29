package net.bendercraft.spigot.bending.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPassiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.arts.Concussion;
import net.bendercraft.spigot.bending.abilities.earth.EarthGrab;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.abilities.water.Bloodbending;
import net.bendercraft.spigot.bending.abilities.water.Drainbending;

public class EntityTools {
	private static Map<Player, Long> blockedChis = new HashMap<>();
	private static Map<Player, Long> grabedPlayers = new HashMap<>();
	private static List<UUID> toggledBending = new LinkedList<>();
	private static List<UUID> affToggledBenders = new LinkedList<>();

	// Tornados, Metalwire,...
	public static final Map<UUID, Long> fallImmunity = new HashMap<>();

	public static boolean isBender(Player player, BendingElement type) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}
		return bPlayer.isBender(type);
	}

	public static boolean isSpecialized(Player player, BendingAffinity specialization) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}
		return bPlayer.hasAffinity(specialization);
	}

	public static boolean isBender(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}
		return true;
	}

	public static String getBendingAbility(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return null;
		}
		return bPlayer.getAbility();
	}

	public static boolean isFallImmune(Player player) {
		if (!fallImmunity.containsKey(player.getUniqueId())) {
			return false;
		}

		long now = System.currentTimeMillis();
		if (now >= fallImmunity.get(player.getUniqueId())) {
			fallImmunity.remove(player.getUniqueId());
			return false;
		}

		return true;
	}

	public static boolean hasPermission(Player player, RegisteredAbility ability) {
		if (ability == null) {
			return false;
		}

		if (ability.getAffinity() != BendingAffinity.NONE && !EntityTools.isSpecialized(player, ability.getAffinity())) {
			return false;
		}
		
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if(bender == null) {
			return false;
		}

		if(ability.isPassive() || bender.hasAbility(ability.getName())) {
			return true;
		}

		return false;
	}

	public static boolean canBend(Player player, String ability) {
		return canBend(player, AbilityManager.getManager().getRegisteredAbility(ability));
	}

	public static boolean canBend(Player player, RegisteredAbility ability) {
		if (ability == null) {
			return false;
		}

		if (player == null) {
			return false;
		}

		if (!hasPermission(player, ability)) {
			return false;
		}
		
		if (ability.getElement() == BendingElement.ENERGY) {
			return true;
		}

		if (toggledBending(player) && !BendingPassiveAbility.isPassive(ability)) {
			return false;
		}

		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player) || isGrabed(player) || Concussion.getTarget(player) != null)) {
			return false;
		}

		if (!isBender(player, ability.getElement())) {
			return false;
		}

		if (ability.getAffinity() != BendingAffinity.NONE) {
			if (!isSpecialized(player, ability.getAffinity())) {
				return false;
			}
			if (speToggled(player)) {
				return false;
			}
		}

		return !ProtectionManager.isLocationProtectedFromBending(player, ability, player.getLocation());
	}

	public static boolean canBendPassive(Player player, BendingElement element) {
		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player) || isGrabed(player)) && !AvatarState.isAvatarState(player)) {
			return false;
		}
		if (!player.hasPermission("bending." + element.name() + ".passive")) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBendingPassives(player, player.getLocation())) {
			return false;
		}
		return true;
	}

	public static boolean canPlantbend(Player player) {
		return Drainbending.canDrainBend(player);
	}

	public static void blockChi(Player player, long time) {
		blockedChis.put(player, System.currentTimeMillis()+time);
	}
	
	public static long blockedChiTimeLeft(Player player, long now) {
		if (isChiBlocked(player)) {
			return blockedChis.get(player) - now;
		}
		return 0;
	}

	public static boolean isChiBlocked(Player player) {
		if (blockedChis.containsKey(player)) {
			long now = System.currentTimeMillis();
			if ((now > blockedChis.get(player)) || AvatarState.isAvatarState(player)) {
				blockedChis.remove(player);
				return false;
			}
			return true;
		}
		return false;
	}

	public static void grab(Player player, long time) {
		grabedPlayers.put(player, time);
	}

	public static void unGrab(Player player) {
		if (grabedPlayers.containsKey(player)) {
			grabedPlayers.remove(player);
		}
	}

	public static boolean isGrabed(Player player) {

		if (grabedPlayers.containsKey(player)) {
			long time = System.currentTimeMillis();
			if ((time > (grabedPlayers.get(player) + (EarthGrab.OTHER_DURATION * 1000))) || AvatarState.isAvatarState(player)) {
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

		if (canBend(player, AbilityManager.getManager().getRegisteredAbility(Bloodbending.NAME)) && !toggledBending(player)) {
			return false;
		}

		return true;
	}

	public static boolean toggledBending(Player player) {
		return toggledBending.contains(player.getUniqueId());
	}

	public static boolean speToggled(Player p) {
		return affToggledBenders.contains(p.getUniqueId());
	}

	public static List<Entity> getEntitiesAroundPoint(Location location, double radius) {

		List<Entity> entities = location.getWorld().getEntities();
		List<Entity> list = new LinkedList<>();

		for (Entity entity : entities) {
			if ((entity.getWorld() == location.getWorld()) && (entity.getLocation().distance(location) < radius)) {
				list.add(entity);
			}
		}
		return list;
	}

	public static List<Entity> getEntitiesAroundPoint(final Location location, final double radius, final Predicate<Entity> filter) {
		final World world = location.getWorld();
		return world.getEntities().stream()
			 .filter(entity -> entity.getLocation().distance(location) < radius)
			 .filter(filter)
			 .collect(Collectors.toList());
	}

	public static List<LivingEntity> getLivingEntitiesAroundPoint(Location location, double radius) {
		List<LivingEntity> list = new LinkedList<>();

		for (Entity e : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
			if (e instanceof LivingEntity) {
				list.add((LivingEntity)e);
			}
		}
		return list;
	}

	public static Location getTargetedLocation(Player player, double range) {
		return getTargetedLocation(player, range, BlockTools.getAirs());
	}

	public static Location getTargetedLocation(Player player, double originselectrange, Set<Material> nonOpaque2) {
		Location origin = player.getEyeLocation();
		Vector direction = origin.getDirection();

		BlockIterator iter = new BlockIterator(player, (int) originselectrange + 1);
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
		return getTargetBlock(player, range, BlockTools.getAirs());
	}

	/**
	 * 
	 * @param player Player to start from (uses eye location)
	 * @param range maximum distance before returning
	 * @param nonOpaque2 block to go ignore if encountered while iterating
	 * @return a block, never null
	 */
	public static Block getTargetBlock(Player player, double range, Set<Material> nonOpaque2) {
		BlockIterator iter = new BlockIterator(player, (int) range + 1);
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

	public static LivingEntity getTargetedEntity(Player player, double range) {
		return getTargetedEntity(player, range, new LinkedList<>());
	}

	public static LivingEntity getTargetedEntity(Player player, double range, List<Entity> avoid) {
		double longestRange = range + 1;
		LivingEntity target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (LivingEntity entity : origin.getWorld().getLivingEntities()) {
			if (avoid.contains(entity)) {
				continue;
			}
			if (!ProtectionManager.isEntityProtected(entity) 
					&& entity.getLocation().getWorld() == origin.getWorld()
					&& entity.getLocation().distance(origin) < longestRange
					&& Tools.getDistanceFromLine(direction, origin, entity.getLocation()) < 2
					&& entity.getEntityId() != player.getEntityId()
					&& entity.getLocation().distance(origin.clone().add(direction)) < entity.getLocation().distance(origin.clone().add(direction.clone().multiply(-1)))) {
				target = entity;
				longestRange = entity.getLocation().distance(origin);
			}
		}
		if(target != null) {
			Vector line = target.getEyeLocation().subtract(player.getEyeLocation()).toVector();
			double distance = line.length();
			for(int i=0 ; i < distance ; i = i + 1) {
				Location location = player.getEyeLocation().clone().add(line.clone().normalize().multiply(i));
				if (location.getBlock().getType().isSolid()) {
					target = null;
					break;
				}
			}
		}
		return target;
	}

	public static LivingEntity getNearestLivingEntity(Location location, double radius) {
		return getNearestLivingEntity(location, radius, Collections.emptyList());
	}

	public static LivingEntity getNearestLivingEntity(Location location, double radius, LivingEntity exclude) {
		return getNearestLivingEntity(location, radius, Collections.singletonList(exclude));
	}

	public static LivingEntity getNearestLivingEntity(Location location, double radius, List<LivingEntity> excludes) {
		LivingEntity result = null;
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, radius)) {
			if(result == null || result.getLocation().distanceSquared(location) > entity.getLocation().distanceSquared(location)) {
				if(!excludes.contains(entity)) {
					result = entity;
				}
			}
		}
		return result;
	}

	public static LivingEntity getLivingEntityByID(int entityID) {
		for (World world : Bukkit.getWorlds()) {
			for (LivingEntity entity : world.getLivingEntities()) {
				if (entity.getEntityId() == entityID) {
					return entity;
				}
			}
		}

		return null;
	}
	
	public static void giveItemInOffHand(Player player, ItemStack itemstack) {
		ItemStack offhand = player.getInventory().getItemInOffHand();
		if (offhand != null) {
			int i = player.getInventory().firstEmpty();
			if (i != -1) {
				// Inventory full ? Player will loose current held item !
				player.getInventory().setItem(i, offhand);
			}
		}
		player.getInventory().setItemInOffHand(itemstack);
	}
	
	public static void giveItemInMainHand(Player player, ItemStack itemstack) {
		int slot = player.getInventory().getHeldItemSlot();
		ItemStack hand = player.getInventory().getItem(slot);
		if (hand != null) {
			int i = player.getInventory().firstEmpty();
			if (i != -1) {
				// Inventory full ? Player will loose current held item !
				player.getInventory().setItem(i, hand);
			}
		}
		player.getInventory().setItem(slot, itemstack);
	}

	public static boolean isTool(Material mat) {
		switch (mat) {
			case WOODEN_AXE:
			case WOODEN_PICKAXE:
			case WOODEN_SHOVEL:
			case WOODEN_HOE:
			case WOODEN_SWORD:
			case STONE_AXE:
			case STONE_PICKAXE:
			case STONE_SHOVEL:
			case STONE_HOE:
			case STONE_SWORD:
			case IRON_AXE:
			case IRON_PICKAXE:
			case IRON_SHOVEL:
			case IRON_HOE:
			case IRON_SWORD:
			case GOLDEN_AXE:
			case GOLDEN_PICKAXE:
			case GOLDEN_SHOVEL:
			case GOLDEN_HOE:
			case GOLDEN_SWORD:
			case DIAMOND_AXE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SHOVEL:
			case DIAMOND_HOE:
			case DIAMOND_SWORD:
			case BOW:
				return true;

			default:
				return false;
		}
	}
	
	public static boolean holdsTool(Player player) {
		if(player.getInventory().getItemInMainHand() == null) {
			return false;
		}
		return isTool(player.getInventory().getItemInMainHand().getType());
	}
	
	public static List<UUID> getToggledBendings() {
		return toggledBending;
	}
	
	public static List<UUID> getToggledAffinities() {
		return affToggledBenders;
	}
}

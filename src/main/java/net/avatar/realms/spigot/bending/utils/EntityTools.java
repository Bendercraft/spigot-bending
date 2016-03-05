package net.avatar.realms.spigot.bending.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.earth.EarthGrab;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;

public class EntityTools {

	private static Map<Player, Long> blockedChis = new HashMap<Player, Long>();
	private static Map<Player, Long> grabedPlayers = new HashMap<Player, Long>();
	private static List<UUID> toggledBending = new LinkedList<UUID>();
	private static List<UUID> affToggledBenders = new LinkedList<UUID>();

	// Tornados, Metalwire,...
	public static final Map<UUID, Long> fallImmunity = new HashMap<UUID, Long>();

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

	public static boolean hasPermission(Player player, String ability) {
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);
		if (register.getAffinity() != BendingAffinity.NONE && !EntityTools.isSpecialized(player, register.getAffinity())) {
			return false;
		}

		if (player.hasPermission(register.getPermission())) {
			return true;
		}

		return false;
	}

	public static boolean canBend(Player player, String ability) {
		if (ability == null) {
			return false;
		}

		if (player == null) {
			return false;
		}

		if (!hasPermission(player, ability)) {
			return false;
		}
		
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);

		if (register.getElement() == BendingElement.ENERGY) {
			return true;
		}

		if (toggledBending(player) && !BendingPassiveAbility.isPassive(register)) {
			return false;
		}

		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player) || isGrabed(player))) {
			return false;
		}

		if (!isBender(player, register.getElement())) {
			return false;
		}

		if (register.getAffinity() != BendingAffinity.NONE) {
			if (!isSpecialized(player, register.getAffinity())) {
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
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}
		if (EntityTools.isSpecialized(player, BendingAffinity.DRAIN)) {
			return true;
		}
		return false;
	}

	public static void blockChi(Player player, long time) {
		blockedChis.put(player, time);
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

		if (canBend(player, Bloodbending.NAME) && !toggledBending(player)) {
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
		List<Entity> list = new LinkedList<Entity>();

		for (Entity entity : entities) {
			if ((entity.getWorld() == location.getWorld()) && (entity.getLocation().distance(location) < radius)) {
				list.add(entity);
			}
		}
		return list;
	}

	public static List<LivingEntity> getLivingEntitiesAroundPoint(Location location, double radius) {
		List<LivingEntity> list = new LinkedList<LivingEntity>();

		for (LivingEntity le : location.getWorld().getLivingEntities()) {
			if ((le.getWorld() == location.getWorld()) && (le.getLocation().distance(location) < radius)) {
				list.add(le);
			}
		}
		return list;
	}

	public static Location getTargetedLocation(Player player, double range) {
		return getTargetedLocation(player, range, Collections.singleton(Material.AIR));
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
		return getTargetBlock(player, range, Collections.singleton(Material.AIR));
	}

	public static Block getTargetBlock(Player player, double originselectrange, Set<Material> nonOpaque2) {
		BlockIterator iter = new BlockIterator(player, (int) originselectrange + 1);
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
		return getTargetedEntity(player, range, new LinkedList<Entity>());
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
			if ((entity.getLocation().distance(origin) < longestRange) && (Tools.getDistanceFromLine(direction, origin, entity.getLocation()) < 2) && (entity.getEntityId() != player.getEntityId()) && (entity.getLocation().distance(origin.clone().add(direction)) < entity.getLocation().distance(origin.clone().add(direction.clone().multiply(-1))))) {
				target = entity;
				longestRange = entity.getLocation().distance(origin);
			}
		}
		return target;
	}

	public static LivingEntity getNearestLivingEntity(Location location, double radius) {
		return getNearestLivingEntity(location, radius, Collections.<LivingEntity> emptyList());
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

	@SuppressWarnings("deprecation")
	public static void damageEntity(Player player, Entity entity, double damage) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}
		if (entity instanceof LivingEntity) {
			if (AvatarState.isAvatarState(player)) {
				damage = AvatarState.getValue(damage);
			}

			((LivingEntity) entity).damage(damage, player);
			entity.setLastDamageCause(new EntityDamageByEntityEvent(player, entity, DamageCause.CUSTOM, damage));
			Map<DamageModifier, Double> damages = new HashMap<DamageModifier, Double>();
			damages.put(DamageModifier.BASE, damage);
			
			
		}
	}

	public static boolean isTool(Material mat) {
		switch (mat) {
			case WOOD_AXE:
			case WOOD_PICKAXE:
			case WOOD_SPADE:
			case WOOD_HOE:
			case WOOD_SWORD:
			case STONE_AXE:
			case STONE_PICKAXE:
			case STONE_SPADE:
			case STONE_HOE:
			case STONE_SWORD:
			case IRON_AXE:
			case IRON_PICKAXE:
			case IRON_SPADE:
			case IRON_HOE:
			case IRON_SWORD:
			case GOLD_AXE:
			case GOLD_PICKAXE:
			case GOLD_SPADE:
			case GOLD_HOE:
			case GOLD_SWORD:
			case DIAMOND_AXE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SPADE:
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

package net.avatar.realms.spigot.bending.utils;

import java.util.ArrayList;
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
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.earth.EarthGrab;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;

public class EntityTools {

	public static Map<Player, Long> blockedChis = new HashMap<Player, Long>();
	public static Map<Player, Long> grabedPlayers = new HashMap<Player, Long>();
	public static List<UUID> toggledBending = new ArrayList<UUID>();
	public static List<UUID> speToggledBenders = new ArrayList<UUID>();

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
		return bPlayer.isSpecialized(specialization);
	}

	public static boolean isBender(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		}
		return true;
	}

	public static List<BendingElement> getBendingTypes(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return null;
		}
		return bPlayer.getBendingTypes();
	}

	public static BendingAbilities getBendingAbility(Player player) {
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

	public static String getPermissionKey(BendingAbilities ability) {
		return "bending." + ability.getElement().name().toLowerCase() + "." + ability.name().toLowerCase();
	}

	public static boolean hasPermission(Player player, BendingAbilities ability) {

		if (ability.isSpecialization() && !EntityTools.isSpecialized(player, ability.getSpecialization())) {
			return false;
		}

		if (player.hasPermission(ability.getPermission())) {
			return true;
		}

		return false;
	}

	public static boolean canBend(Player player, BendingAbilities ability) {
		if (ability == null) {
			return false;
		}

		if (player == null) {
			return false;
		}

		if (!hasPermission(player, ability)) {
			return false;
		}

		if (ability == BendingAbilities.AvatarState) {
			return true;
		}

		if (toggledBending(player)) {
			return false;
		}

		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player) || isGrabed(player))) {
			return false;
		}

		if (ability.isAirbending() && !isBender(player, BendingElement.Air)) {
			return false;
		}
		if (ability.isChiblocking() && !isBender(player, BendingElement.ChiBlocker)) {
			return false;
		}
		if (ability.isEarthbending() && !isBender(player, BendingElement.Earth)) {
			return false;
		}
		if (ability.isFirebending() && !isBender(player, BendingElement.Fire)) {
			return false;
		}
		if (ability.isWaterbending() && !isBender(player, BendingElement.Water)) {
			return false;
		}

		if (ability.isSpecialization()) {
			if (!isSpecialized(player, ability.getSpecialization())) {
				return false;
			}
			if (speToggled(player)) {
				return false;
			}
		}

		if (ProtectionManager.isAllowedEverywhereAbility(ability)) {
			return true;
		}

		return !ProtectionManager.isRegionProtectedFromBending(player, ability, player.getLocation());
	}

	public static boolean canBendPassive(Player player, BendingElement type) {
		if ((isChiBlocked(player) || Bloodbending.isBloodbended(player) || isGrabed(player)) && !AvatarState.isAvatarState(player)) {
			return false;
		}
		if (!player.hasPermission("bending." + type + ".passive")) {
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
		if (EntityTools.isSpecialized(player, BendingAffinity.DrainBend)) {
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

		if (canBend(player, BendingAbilities.Bloodbending) && !toggledBending(player)) {
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

	public static boolean speToggled(Player p) {
		return speToggledBenders.contains(p);
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

	public static LivingEntity getTargettedEntity(Player player, double range) {
		return getTargettedEntity(player, range, new ArrayList<Entity>());
	}

	public static LivingEntity getTargettedEntity(Player player, double range, List<Entity> avoid) {
		double longestr = range + 1;
		LivingEntity target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (LivingEntity entity : origin.getWorld().getLivingEntities()) {
			if (avoid.contains(entity)) {
				continue;
			}
			if ((entity.getLocation().distance(origin) < longestr) && (Tools.getDistanceFromLine(direction, origin, entity.getLocation()) < 2)
					&& (entity.getEntityId() != player.getEntityId()) && (entity.getLocation().distance(origin.clone().add(direction)) < entity
							.getLocation().distance(origin.clone().add(direction.clone().multiply(-1))))) {
				target = entity;
				longestr = entity.getLocation().distance(origin);
			}
		}
		return target;
	}

	@SuppressWarnings("deprecation")
	public static void damageEntity(Player player, Entity entity, double damage) {
		if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		if (entity instanceof LivingEntity) {
			if (AvatarState.isAvatarState(player)) {
				damage = AvatarState.getValue(damage);
			}

			((LivingEntity) entity).damage(damage, player);
			((LivingEntity) entity).setLastDamageCause(new EntityDamageByEntityEvent(player, entity, DamageCause.CUSTOM, damage));
		}
	}

	public static boolean isWeapon(Material mat) {
		switch (mat) {
			case WOOD_AXE:
			case WOOD_PICKAXE:
			case WOOD_SPADE:
			case WOOD_SWORD:
			case STONE_AXE:
			case STONE_PICKAXE:
			case STONE_SPADE:
			case STONE_SWORD:
			case IRON_AXE:
			case IRON_PICKAXE:
			case IRON_SPADE:
			case IRON_SWORD:
			case GOLD_AXE:
			case GOLD_PICKAXE:
			case GOLD_SPADE:
			case GOLD_SWORD:
			case DIAMOND_AXE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SPADE:
			case DIAMOND_SWORD:
				return true;

			default:
				return false;
		}
	}
}

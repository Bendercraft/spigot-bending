package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * AirBurst is just an utility abilities, it does no damage or whatever, only providing a way to check if a player has charged
 * Classes AirSphereBurst, AirConeBurst, AirFallBurst consumes charge and remove it
 * 
 * @author Koudja
 *
 */
public class AirBurst implements IAbility {
	private static Map<Player, AirBurst> instances = new HashMap<Player, AirBurst>();

	private Player player;
	private long starttime;
	private long chargetime = 1750;
	private boolean charged = false;
	private List<Entity> affectedentities = new LinkedList<Entity>();
	
	private IAbility parent;

	public AirBurst(Player player, IAbility parent) {
		this.parent = parent;
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.AirBurst))
			return;

		if (instances.containsKey(player))
			return;
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);
	}

	public void addAffectedEntity(Entity entity) {
		affectedentities.add(entity);
	}

	public boolean isAffectedEntity(Entity entity) {
		return affectedentities.contains(entity);
	}

	private boolean progress() {
		if (!EntityTools.canBend(player, Abilities.AirBurst)
				|| EntityTools.getBendingAbility(player) != Abilities.AirBurst) {
			return false;
		}
		
		if (!player.isSneaking()) {
			return false;
		}
		
		if (System.currentTimeMillis() > starttime + chargetime && !charged) {
			charged = true;
		}

		if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			location.getWorld().playEffect(
					location,
					Effect.SMOKE,
					Tools.getIntCardinalDirection(player.getEyeLocation()
							.getDirection()), 3);
		}
		return true;
	}
	
	public void remove() {
		instances.remove(player);
	}

	public static void progressAll() {
		List<AirBurst> toRemove = new LinkedList<AirBurst>();
		for (AirBurst burst : instances.values()) {
			boolean keep = burst.progress();
			if(!keep) {
				toRemove.add(burst);
			}
		}
		
		for(AirBurst burst : toRemove) {
			burst.remove();
		}
	}

	public static String getDescription() {
		return "AirBurst is one of the most powerful abilities in the airbender's arsenal. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of air in front of you, or click to release the burst in a sphere around you. "
				+ "Additionally, having this ability selected when you land on the ground from a "
				+ "large enough fall will create a burst of air around you.";
	}

	public static void removeAll() {
		instances.clear();
	}
	
	public static boolean isAirBursting(Player player) {
		return instances.containsKey(player);
	}
	
	public boolean isCharged() {
		return charged;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

	public static AirBurst getAirBurst(Player player) {
		return instances.get(player);
	}
}

package net.avatar.realms.spigot.bending.abilities.fire;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = "Fire Ferret", bind = BendingAbilities.FireFerret, element = BendingElement.Fire)
public class FireFerret extends BendingActiveAbility {
	@ConfigurationParameter("Search-Radius")
	public static double SEARCH_RADIUS = 20;
	
	@ConfigurationParameter("Affecting-Radius")
	public static double AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Time-target")
	public static long TIME_TO_TARGET = 1; // In seconds
	
	@ConfigurationParameter("Range")
	private static int RANGE = 40;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 1;
	
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 3;
	
	private Location origin;
	private Location location;
	
	private LivingEntity target;
	
	private long time;

	private double speedfactor;
	
	public FireFerret(Player player) {
		super(player);
		
		speedfactor = SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.Start) {
			if(player.isSneaking()) {
				origin = player.getEyeLocation().clone();
				location = origin.clone();
				target = EntityTools.getNearestLivingEntity(location, SEARCH_RADIUS, player);
				if(target == null) {
					return false;
				}
				time = System.currentTimeMillis();
				setState(BendingAbilityState.Progressing);
			}
		}
		return false;
	}

	@Override
	public boolean sneak() {
		return false;
	}

	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if(now - time > TIME_TO_TARGET * 1000) {
			time = now;
			target = EntityTools.getNearestLivingEntity(location, SEARCH_RADIUS, player);
			if(target == null) {
				remove();
				return;
			}
		}
		
		if(target.isDead()) {
			remove();
			return;
		}

		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0, RANGE + 4);
		
		if(target.getLocation().distance(location) < AFFECTING_RADIUS) {
			affect(target);
			remove();
			return;
		}
		
		Vector direction = target.getLocation().toVector().clone().subtract(location.toVector());
		location = this.location.add(direction.multiply(speedfactor));
	}

	@Override
	public Object getIdentifier() {
		return player;
	}
	
	private boolean affect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return false;
		}
		if (entity.getEntityId() != this.player.getEntityId()) {
			EntityTools.damageEntity(this.player, entity, DAMAGE);
			entity.setFireTicks(2);
			return false;
		}
		return true;
	}

	@Override
	public void stop() {
		
	}
}

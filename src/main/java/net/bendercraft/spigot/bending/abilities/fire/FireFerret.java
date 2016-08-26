package net.bendercraft.spigot.bending.abilities.fire;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = FireFerret.NAME, element = BendingElement.FIRE)
public class FireFerret extends BendingActiveAbility {
	public final static String NAME = "FireFerret";
	
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
	
	@ConfigurationParameter("Power")
	public static int POWER = 3;
	
	private Location origin;
	private Location location;
	
	private LivingEntity target;
	
	private long time;

	private double speedfactor;
	
	public FireFerret(RegisteredAbility register, Player player) {
		super(register, player);
		
		speedfactor = SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, POWER)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			if(player.isSneaking()) {
				origin = player.getEyeLocation().clone();
				location = origin.clone();
				target = EntityTools.getNearestLivingEntity(location, SEARCH_RADIUS, player);
				if(target == null || ProtectionManager.isEntityProtected(target)) {
					return false;
				}
				time = System.currentTimeMillis();
				setState(BendingAbilityState.PROGRESSING);
				bender.fire.consume(NAME, POWER);
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
			if(target == null || ProtectionManager.isEntityProtected(target)) {
				remove();
				return;
			}
		}
		
		if(target.isDead()) {
			remove();
			return;
		}
		
		if (BlockTools.isSolid(location.getBlock())) {
			remove();
			return;
		}

		location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
		
		if(target.getLocation().distance(location) < AFFECTING_RADIUS) {
			if(affect(target)) {
				remove();
				return;
			}
		}
		
		Vector direction = target.getEyeLocation().toVector().clone().subtract(location.toVector());
		location = this.location.add(direction.multiply(speedfactor));
	}

	@Override
	public Object getIdentifier() {
		return player;
	}
	
	private boolean affect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		if (entity == player) {
			return false;
		}
		DamageTools.damageEntity(bender, entity, this, DAMAGE);
		Enflamed.enflame(player, entity, 2, this);
		return true;
	}

	@Override
	public void stop() {
		
	}
}

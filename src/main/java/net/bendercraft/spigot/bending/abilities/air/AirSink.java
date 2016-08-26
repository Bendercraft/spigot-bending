package net.bendercraft.spigot.bending.abilities.air;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = AirSink.NAME, affinity = BendingAffinity.TORNADO, canBeUsedWithTools = true)
public class AirSink extends BendingActiveAbility {
	public final static String NAME = "AirSink";

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 10000;
	
	@ConfigurationParameter("Duration")
	public static long DURATION = 10000;

	@ConfigurationParameter("Radius")
	private static double RADIUS = 10;

	@ConfigurationParameter("Range")
	private static double RANGE = 25;

	@ConfigurationParameter("Push")
	private static double PUSH = 0.3;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 35;
	
	private Location origin;
	private long interval;
	private long time;
	
	private double particleDistance;
	
	private int noDisplayTick = 0;

	public AirSink(RegisteredAbility register, Player player) {
		super(register, player);
		this.interval = (long) (1000. / SPEED);
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			origin = EntityTools.getTargetBlock(player, RANGE).getLocation();
			time = System.currentTimeMillis();
			particleDistance = RADIUS;
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public void stop() {
		bender.cooldown(this, COOLDOWN);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isLocationProtectedFromBending(player, register, origin)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if ((System.currentTimeMillis() - time) >= interval) {
			time = System.currentTimeMillis();
			
			// Compute effect
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(origin, RADIUS)) {
				affect(entity);
			}
			
			if(noDisplayTick <= 0) {
				// Compute particles
				for(double theta = 0 ; theta < 360 ; theta+=36) {
					for(double phi = 0 ; phi < 360 ; phi+=36) {
						double x = particleDistance * Math.cos(Math.toRadians(theta)) * Math.sin(Math.toRadians(phi));
						double y = particleDistance * Math.sin(Math.toRadians(theta)) * Math.sin(Math.toRadians(phi));
						double z = particleDistance * Math.cos(Math.toRadians(phi));
						origin.getWorld().playEffect(origin.clone().add(x,y,z), Effect.SMOKE, 4, (int) RANGE);
					}
				}
				particleDistance -= 1;
				if(particleDistance < 0) {
					particleDistance = RADIUS;
				}
				noDisplayTick = 4;
			}
			noDisplayTick--;
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public long getMaxMillis() {
		return DURATION;
	}

	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		if(entity == player) {
			return;
		}
		Vector direction = origin.clone().subtract(entity.getLocation()).toVector();
		double distance = direction.length();
		entity.setVelocity(entity.getVelocity().add(direction.normalize().multiply(PUSH*distance/RADIUS)));
		entity.setFallDistance(0);
	}
}

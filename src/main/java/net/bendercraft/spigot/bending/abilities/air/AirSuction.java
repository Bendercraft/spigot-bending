package net.bendercraft.spigot.bending.abilities.air;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.*;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.Tools;

/**
 * State Preparing = Origin Set State Progressing = AirSuction thrown
 */
@ABendingAbility(name = AirSuction.NAME, element = BendingElement.AIR)
public class AirSuction extends BendingActiveAbility {
	public final static String NAME = "AirSuction";

	@ConfigurationParameter("Speed")
	private static double SPEED = 25.0;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Affecting-Radius")
	private static double AFFECTING_RADIUS = 2.0;

	@ConfigurationParameter("Push-Factor")
	private static double PUSH_FACTOR = 2.5;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 250;

	@ConfigurationParameter("Origin-Range")
	private static double SELECT_RANGE = 10;
	
	static final double maxspeed = 1. / PUSH_FACTOR;

	private Location location;
	private Location origin;
	private Vector direction;
	private UUID id = UUID.randomUUID();
	private double range;
	private boolean otherorigin = false;

	private double speedfactor;

	private long cooldown;

	public AirSuction(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.AIR_AIRSUCTION_RANGE)) {
			this.range += 2;
		}
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.AIR_AIRSUCTION_COOLDOWN)) {
			this.cooldown -= 500;
		}

		double speed = SPEED;
		if(bender.hasPerk(BendingPerk.AIR_AIRSUCTION_SPEED)) {
			speed *= 1.1;
		}
		
		this.speedfactor = speed * (Bending.getInstance().getManager().getTimestep() / 1000.); // Really used ?
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			Location loc = getNewOriginLocation(this.player);
			if (loc == null) {
				return false;
			}
			this.origin = loc;
			setState(BendingAbilityState.PREPARING);
			this.otherorigin = true;
			return false;
		}

		if (getState() == BendingAbilityState.PREPARING) {
			Location loc = getNewOriginLocation(this.player);
			if (loc != null) {
				this.origin = loc;
			}
			return false;
		}

		return true;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			this.origin = this.player.getEyeLocation();
			setState(BendingAbilityState.PREPARING);
		}

		if (getState() == BendingAbilityState.PREPARING) {
			Entity entity = EntityTools.getTargetedEntity(this.player, this.range);
			if (entity != null) {
				this.direction = Tools.getDirection(entity.getLocation(), this.origin).normalize();
				this.location = getLocation(this.origin, this.direction.clone().multiply(-1));
			} else {
				this.location = EntityTools.getTargetedLocation(this.player, this.range, BlockTools.getNonOpaque());
				this.direction = Tools.getDirection(this.location, this.origin).normalize();
			}
			setState(BendingAbilityState.PROGRESSING);
			return false;
		}
		return true;
	}

	private Location getLocation(Location origin, Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= this.range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if (!BlockTools.isTransparentToEarthbending(this.player, location.getBlock()) || ProtectionManager.isLocationProtectedFromBending(this.player, register, location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}

	public static Location getNewOriginLocation(Player player) {
		Location location = EntityTools.getTargetedLocation(player, SELECT_RANGE, BlockTools.getNonOpaque());
		if (location.getBlock().isLiquid() || BlockTools.isSolid(location.getBlock())) {
			return null;
		}

		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(AirSuction.NAME);
		if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
			return null;
		}

		return location;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!isState(BendingAbilityState.PROGRESSING) && !NAME.equals(bender.getAbility())) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if (getState().equals(BendingAbilityState.PREPARING)) {
			if (!this.origin.getWorld().equals(this.player.getWorld()) 
					|| this.origin.distance(this.player.getEyeLocation()) > SELECT_RANGE) {
				remove();
			}
			this.player.spawnParticle(Particle.SMOKE_NORMAL, this.origin, 1, 0, 0, 0, 0);
			return;
		}
		
		if (!getState().equals(BendingAbilityState.PROGRESSING) 
				|| this.location.distance(this.origin) > this.range 
				|| this.location.distance(this.origin) <= 1) {
			remove();
			return;
		}
		
		if(ProtectionManager.isLocationProtectedFromBending(this.player, register, this.location)) {
			remove();
			return;
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location, AFFECTING_RADIUS)) {
			affect(entity);
		}
		advanceLocation();
	}

	@Override
	public void stop() {
		this.bender.cooldown(AirSuction.NAME, cooldown);
	}

	private static final Particle.DustOptions DISPLAY = new Particle.DustOptions(Color.fromRGB(220, 250, 250), 2.5f);
	private void advanceLocation() {
		this.location.getWorld().spawnParticle(Particle.REDSTONE, this.location, 1, 0.125, 0.125, 0.125, 0, DISPLAY, true);
		this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
	}

	@Override
	protected long getMaxMillis() {
		return 1000 * 60 * 2;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}
	
	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}

		if (entity.getType().equals(EntityType.ENDER_PEARL)) {
			return;
		}

		if (entity.getFireTicks() > 0) {
			entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
			entity.setFireTicks(0);
		}
		if (entity == player && !this.otherorigin) {
			return;
		}
		Vector velocity = entity.getVelocity();
		double max = maxspeed;
		double factor = PUSH_FACTOR;

		Vector push = this.direction.clone();
		if (Math.abs(push.getY()) > max && entity != player) {
			if (push.getY() < 0) {
				push.setY(-max);
			} else {
				push.setY(max);
			}
		}

		factor *= 1 - (this.location.distance(this.origin) / (2 * this.range));

		double comp = velocity.dot(push.clone().normalize());
		if (comp > factor) {
			velocity.multiply(.5);
			velocity.add(push.clone().normalize().multiply(velocity.clone().dot(push.clone().normalize())));
		} else if ((comp + (factor * .5)) > factor) {
			velocity.add(push.clone().multiply(factor - comp));
		} else {
			velocity.add(push.clone().multiply(factor * .5));
		}
		if (entity.getEntityId() == this.player.getEntityId()) {
			velocity.multiply(1.0 / 1.85);
		}
		entity.setVelocity(velocity);
		entity.setFallDistance(0);
	}

}

package net.bendercraft.spigot.bending.abilities.air;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.*;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
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

	static final long soonesttime = Tools.timeinterval;

	private static int ID = Integer.MIN_VALUE;
	static final double maxspeed = AirBlast.maxspeed;

	@ConfigurationParameter("Speed")
	private static double speed = 25.0;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Affecting-Radius")
	private static double affectingradius = 2.0;

	@ConfigurationParameter("Push-Factor")
	private static double pushfactor = 2.5;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 250;

	@ConfigurationParameter("Origin-Range")
	private static double SELECT_RANGE = 10;

	private Location location;
	private Location origin;
	private Vector direction;
	private int id;
	private double range = RANGE;
	private boolean otherorigin = false;

	private double speedfactor;

	public AirSuction(RegisteredAbility register, Player player) {
		super(register, player);

		this.speedfactor = speed * (Bending.getInstance().getManager().getTimestep() / 1000.); // Really used ?

		this.id = ID++;

		if (this.bender.hasPath(BendingPath.RENEGADE)) {
			this.range *= 0.6;
		}
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
		if (!getState().equals(BendingAbilityState.PROGRESSING) && !bender.getAbility().equals(AirSuction.NAME)) {
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
			this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4, (int) SELECT_RANGE);
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

		for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location, affectingradius)) {
			if (ProtectionManager.isEntityProtected(entity)) {
				continue;
			}

			if (entity.getType().equals(EntityType.ENDER_PEARL)) {
				continue;
			}

			if (entity.getFireTicks() > 0) {
				entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
				entity.setFireTicks(0);
			}
			if (!((entity.getEntityId() != this.player.getEntityId()) || this.otherorigin)) {
				continue;
			}
			Vector velocity = entity.getVelocity();
			double max = maxspeed;
			double factor = pushfactor;
			if (AvatarState.isAvatarState(this.player)) {
				max = AvatarState.getValue(maxspeed);
				factor = AvatarState.getValue(factor);
			}

			Vector push = this.direction.clone();
			if ((Math.abs(push.getY()) > max) && (entity.getEntityId() != this.player.getEntityId())) {
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
		advanceLocation();
	}

	@Override
	public void stop() {
		this.bender.cooldown(AirSuction.NAME, COOLDOWN);
	}

	private void advanceLocation() {
		this.location.getWorld().playEffect(this.location, Effect.SMOKE, 4, (int) AirBlast.DEFAULT_RANGE);
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

}

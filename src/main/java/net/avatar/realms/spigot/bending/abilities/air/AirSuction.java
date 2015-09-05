package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPathType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Air Suction", element=BendingType.Air)
public class AirSuction extends ActiveAbility {
	
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
	
	public AirSuction (Player player) {
		super(player, null);
		
		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}
		
		this.speedfactor = speed * (Bending.time_step / 1000.); // Really used ?
		
		this.id = ID++;
		
		if(this.bender.hasPath(BendingPathType.Renegade)) {
			this.range *= 0.6;
		}
	}
	
	@Override
	public boolean sneak () {
		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		
		if (this.state == AbilityState.CanStart) {
			Location loc = getNewOriginLocation(this.player);
			if (loc == null) {
				return false;
			}
			this.origin = loc;
			setState(AbilityState.Preparing);
			AbilityManager.getManager().addInstance(this);
			this.otherorigin = true;
			return false;
		}
		
		if (this.state == AbilityState.Preparing) {
			Location loc = getNewOriginLocation(this.player);
			if (loc != null) {
				this.origin = loc;
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean swing () {
		
		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		
		if (this.state == AbilityState.CanStart) {
			this.origin = this.player.getEyeLocation();
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Preparing);
		}
		
		if (this.state == AbilityState.Preparing) {
			
			Entity entity = EntityTools.getTargettedEntity(this.player, this.range);
			if (entity != null) {
				this.direction = Tools.getDirection(entity.getLocation(), this.origin).normalize();
				this.location = getLocation(this.origin, this.direction.clone().multiply(-1));
			}
			else {
				this.location = EntityTools.getTargetedLocation(this.player, this.range, BlockTools.nonOpaque);
				this.direction = Tools.getDirection(this.location, this.origin).normalize();
			}
			
			setState(AbilityState.Progressing);
			return false;
		}
		
		return true;
	}
	
	private Location getLocation(Location origin, Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= this.range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if (!BlockTools.isTransparentToEarthbending(this.player, location.getBlock())
					|| ProtectionManager.isRegionProtectedFromBending(this.player,
							Abilities.AirSuction, location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}
	
	public static Location getNewOriginLocation (Player player) {
		Location location = EntityTools.getTargetedLocation(player,
				SELECT_RANGE, BlockTools.nonOpaque);
		if (location.getBlock().isLiquid()
				|| BlockTools.isSolid(location.getBlock())) {
			return null;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirSuction,
				location)) {
			return null;
		}
		
		return location;
	}
	
	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		
		if ((EntityTools.getBendingAbility(this.player) != Abilities.AirSuction)) {
			return false;
		}
		
		if (this.state.equals(AbilityState.Preparing)) {
			if (!this.origin.getWorld().equals(this.player.getWorld())) {
				return false;
			}
			if (this.origin.distance(this.player.getEyeLocation()) > SELECT_RANGE) {
				return false;
			}
			this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4, (int) SELECT_RANGE);
			return true;
		}
		
		if (!this.state.equals(AbilityState.Progressing)) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.AirSuction,
				this.location)) {
			// Info : This is checking the position of the suction and not the
			// position of the bender
			return false;
		}
		
		if ((this.location.distance(this.origin) > this.range)
				|| (this.location.distance(this.origin) <= 1)) {
			return false;
		}
		
		for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location,
				affectingradius)) {
			if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
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
			if ((Math.abs(push.getY()) > max)
					&& (entity.getEntityId() != this.player.getEntityId())) {
				if (push.getY() < 0) {
					push.setY(-max);
				}
				else {
					push.setY(max);
				}
			}
			
			factor *= 1 - (this.location.distance(this.origin) / (2 * this.range));
			
			double comp = velocity.dot(push.clone().normalize());
			if (comp > factor) {
				velocity.multiply(.5);
				velocity.add(push
						.clone()
						.normalize()
						.multiply(
								velocity.clone().dot(
										push.clone().normalize())));
			} else if ((comp + (factor * .5)) > factor) {
				velocity.add(push.clone().multiply(factor - comp));
			} else {
				velocity.add(push.clone().multiply(factor * .5));
			}
			if (entity.getEntityId() == this.player.getEntityId()) {
				velocity.multiply(1.0/1.85);
			}
			entity.setVelocity(velocity);
			entity.setFallDistance(0);
			//			if ((entity.getEntityId() != this.player.getEntityId())
			//					&& (entity instanceof Player)) {
			//				FlyingPlayer.addFlyingPlayer(this.player, this, 2000L);
			//			}
			
		}
		advanceLocation();
		return true;
	}
	
	@Override
	public void remove () {
		this.bender.cooldown(Abilities.AirSuction, COOLDOWN);
		super.remove();
	}
	
	private void advanceLocation() {
		this.location.getWorld().playEffect(this.location, Effect.SMOKE, 4, (int) AirBlast.DEFAULT_RANGE);
		this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
	}
	
	@Override
	protected long getMaxMillis () {
		return 1000 * 60 * 2;
	}
	
	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.AirSuction;
	}
	
	@Override
	public Object getIdentifier () {
		return this.id;
	}
	
}

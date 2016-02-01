package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * Preparing state = Origin set Progressing state = Airblast thrown
 */
@ABendingAbility(name = "Air Blast", bind = BendingAbilities.AirBlast, element = BendingElement.Air)
public class AirBlast extends BendingActiveAbility {
	private static int ID = Integer.MIN_VALUE;

	@ConfigurationParameter("Speed")
	public static double SPEED = 25.0;

	@ConfigurationParameter("Range")
	public static double DEFAULT_RANGE = 20;

	@ConfigurationParameter("Radius")
	public static double AFFECT_RADIUS = 2.0;

	@ConfigurationParameter("Push-Factor")
	public static double PUSH_FACTOR = 3.0;

	@ConfigurationParameter("Origin-Range")
	private static double SELECT_RANGE = 10;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 250;

	static final double maxspeed = 1. / PUSH_FACTOR;

	private Location location;
	private Location origin;
	private Vector direction;
	private int id;
	private double speedfactor;
	private double range = DEFAULT_RANGE;
	private double pushfactor = PUSH_FACTOR;
	private boolean otherOrigin = false;

	public AirBlast(Player player) {
		super(player);
		this.id = ID++;
		if (this.bender.hasPath(BendingPath.Renegade)) {
			this.range = this.range * 0.6;
		}
	}

	@Override
	public boolean canBeInitialized() {
		if(!super.canBeInitialized() 
				|| this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.Start) {
			this.origin = this.player.getEyeLocation();
			setState(BendingAbilityState.Preparing);
		}
		//It is NORMAL that both if could follow in same click
		if(getState() == BendingAbilityState.Preparing) {
			Entity entity = EntityTools.getTargetedEntity(this.player, this.range);
			if (this.bender.hasPath(BendingPath.Mobile)) {
				entity = null;
			}
			if (entity != null) {
				this.direction = Tools.getDirection(this.origin, entity.getLocation()).normalize();
			} else {
				this.direction = Tools.getDirection(this.origin, EntityTools.getTargetedLocation(this.player, this.range)).normalize();
			}
			this.location = this.origin.clone();
			long cooldown = COOLDOWN;
			if (this.bender.hasPath(BendingPath.Renegade)) {
				cooldown *= 1.2;
			}
			if (this.bender.hasPath(BendingPath.Mobile)) {
				cooldown *= 0.8;
			}
			this.bender.cooldown(BendingAbilities.AirBlast, cooldown);
			setState(BendingAbilityState.Progressing);
			return false;
		}
		return true;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.Start 
				|| getState() == BendingAbilityState.Preparing) {
			Location originLocation = EntityTools.getTargetedLocation(this.player, SELECT_RANGE, BlockTools.getNonOpaque());
			if (originLocation.getBlock().isLiquid() || BlockTools.isSolid(originLocation.getBlock())) {
				return false;
			}

			if ((originLocation == null) || ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.AirBlast, originLocation)) {
				return false;
			}

			this.origin = originLocation;
			this.otherOrigin = true;
			setState(BendingAbilityState.Preparing);
			return false;
		}
		return true;
	}

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (getState() == BendingAbilityState.Preparing && this.bender.getAbility() != AbilityManager.getManager().getAbilityType(this)) {
			return false;
		}
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void progress() {
		if (getState() == BendingAbilityState.Preparing) {
			this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4, (int) SELECT_RANGE);
			return;
		}

		if (getState() != BendingAbilityState.Progressing) {
			remove();
			return;
		}

		this.speedfactor = SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);

		Block block = this.location.getBlock();
		for (Block testblock : BlockTools.getBlocksAroundPoint(this.location, AFFECT_RADIUS)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			}
		}
		if(BlockTools.isSolid(block) || block.isLiquid()) {
			if ((block.getType() == Material.LAVA) || ((block.getType() == Material.STATIONARY_LAVA) && !TempBlock.isTempBlock(block))) {
				if (block.getData() == BlockTools.FULL) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			remove();
			return;
		}

		if (this.location.distance(this.origin) > this.range) {
			remove();
			return;
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location, AFFECT_RADIUS)) {
			if ((entity.getEntityId() != this.player.getEntityId()) || this.otherOrigin) {
				affect(entity);
			}
		}
		advanceLocation();
	}

	private void advanceLocation() {
		this.location.getWorld().playEffect(this.location, Effect.SMOKE, 4, (int) this.range);
		this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
	}

	private void affect(Entity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}

		if (entity.getType().equals(EntityType.ENDER_PEARL)) {
			return;
		}

		boolean isUser = entity.getEntityId() == this.player.getEntityId();
		if (entity.getFireTicks() > 0) {
			entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
			entity.setFireTicks(0);
		}
		Vector velocity = entity.getVelocity();

		double max = maxspeed;
		double factor = this.pushfactor;
		if (AvatarState.isAvatarState(this.player)) {
			max = AvatarState.getValue(maxspeed);
			factor = AvatarState.getValue(factor);
		}

		Vector push = this.direction.clone();
		if ((Math.abs(push.getY()) > max) && !isUser) {
			if (push.getY() < 0) {
				push.setY(-max);
			} else {
				push.setY(max);
			}
		}

		factor *= 1 - (this.location.distance(this.origin) / (2 * this.range));

		if (isUser && BlockTools.isSolid(this.player.getLocation().add(0, -.5, 0).getBlock())) {
			factor *= .5;
		}

		double comp = velocity.dot(push.clone().normalize());
		if (comp > factor) {
			velocity.multiply(.5);
			velocity.add(push.clone().normalize().multiply(velocity.clone().dot(push.clone().normalize())));
		} else if ((comp + (factor * .5)) > factor) {
			velocity.add(push.clone().multiply(factor - comp));
		} else {
			velocity.add(push.clone().multiply(factor * .5));
		}
		if (isUser) {
			velocity.multiply(1.0 / 2.2);
		}
		entity.setVelocity(velocity);
		entity.setFallDistance(0);

		if (this.bender.hasPath(BendingPath.Renegade) && !entity.getUniqueId().equals(player.getUniqueId())) {
			EntityTools.damageEntity(this.player, entity, 1);
		}
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public void stop() {

	}
}

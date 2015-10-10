package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * Preparing state = Origin set Progressing state = Airblast thrown
 */
@BendingAbility(name = "Air Blast", bind = BendingAbilities.AirBlast, element = BendingElement.Air)
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

	private List<Block> affectedlevers = new ArrayList<Block>();

	public AirBlast(Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}

		this.id = ID++;

		if (this.bender.hasPath(BendingPath.Renegade)) {
			this.range = this.range * 0.6;
		}
	}

	public AirBlast(Location location, Vector direction, Player player, double factorpush, BendingActiveAbility parent) {
		super(player, parent);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		if (location.getBlock().isLiquid()) {
			return;
		}

		this.player = player;
		this.origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();
		this.id = ID++;
		this.pushfactor *= factorpush;

		setState(BendingAbilityState.Progressing);
		AbilityManager.getManager().addInstance(this);

		if (this.bender.hasPath(BendingPath.Renegade)) {
			this.range = this.range * 0.6;
		}
	}

	public void setOtherOrigin(Player player) {
		Location location = EntityTools.getTargetedLocation(player, SELECT_RANGE, BlockTools.getNonOpaque());
		if (location.getBlock().isLiquid() || BlockTools.isSolid(location.getBlock())) {
			setState(BendingAbilityState.CannotStart);
			return;
		}

		if ((location == null) || ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.AirBlast, location)) {
			setState(BendingAbilityState.CannotStart);
			return;
		}

		this.origin = location;
		System.out.println(this.origin.getBlock().getType());
		setState(BendingAbilityState.Preparing);
		this.otherOrigin = true;
	}

	@Override
	public boolean swing() {
		if(state == BendingAbilityState.CanStart) {
			this.origin = this.player.getEyeLocation();
			AbilityManager.getManager().addInstance(this);
			setState(BendingAbilityState.Preparing);
		}
		if(state == BendingAbilityState.Preparing) {
			Entity entity = EntityTools.getTargettedEntity(this.player, this.range);
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
		switch (this.state) {
			case None:
			case CannotStart:
				return true;
			case CanStart:
				setOtherOrigin(this.player);
				AbilityManager.getManager().addInstance(this);
				return false;
			case Preparing:
				setOtherOrigin(this.player);
				return false;
			default:
				return true;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if(this.bender.getAbility() != AbilityManager.getManager().getAbilityType(this)) {
			return false;
		}

		if (this.state == BendingAbilityState.Preparing) {
			this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4, (int) SELECT_RANGE);
			return true;
		}

		if (this.state != BendingAbilityState.Progressing) {
			return false;
		}

		this.speedfactor = SPEED * (Bending.getInstance().manager.getTimestep() / 1000.);

		Block block = this.location.getBlock();
		for (Block testblock : BlockTools.getBlocksAroundPoint(this.location, AFFECT_RADIUS)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
			}
		}
		if ((BlockTools.isSolid(block) || block.isLiquid()) && !this.affectedlevers.contains(block)) {
			if ((block.getType() == Material.LAVA) || ((block.getType() == Material.STATIONARY_LAVA) && !BlockTools.isTempBlock(block))) {
				if (block.getData() == BlockTools.FULL) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			return false;
		}

		if (this.location.distance(this.origin) > this.range) {
			return false;
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location, AFFECT_RADIUS)) {
			if (((entity.getEntityId() != this.player.getEntityId()) || this.otherOrigin)) {
				affect(entity);
			}
		}
		advanceLocation();
		return true;
	}

	private void advanceLocation() {
		this.location.getWorld().playEffect(this.location, Effect.SMOKE, 4, (int) this.range);
		this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
	}

	private void affect(Entity entity) {
		if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
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
		// double mag = Math.abs(velocity.getY());
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

		if (this.bender.hasPath(BendingPath.Renegade)) {
			EntityTools.damageEntity(this.player, entity, 0.5);
		}
		// if (!isUser && (entity instanceof Player)) {
		// new Flight((Player) entity, this.player);
		// }
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}
}

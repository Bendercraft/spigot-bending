package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Air Blast", element=BendingType.Air)
public class AirBlast extends ActiveAbility {
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
	public static long COOLDOWN = 500;

	static final double maxspeed = 1. / PUSH_FACTOR;
	public static byte full = 0x0;

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
		super (player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}

		this.id = ID++;
	}

	public AirBlast(Location location, Vector direction, Player player,
			double factorpush, ActiveAbility parent) {
		super(player, parent);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (location.getBlock().isLiquid()) {
			return;
		}

		this.player = player;
		this.origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();
		this.id = ID;
		this.pushfactor *= factorpush;

		setState(AbilityState.Progressing);
		AbilityManager.getManager().addInstance(this);
		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;
	}

	public void setOtherOrigin(Player player) {
		Location location = EntityTools.getTargetedLocation(player,
				SELECT_RANGE, BlockTools.nonOpaque);
		if (location.getBlock().isLiquid()
				|| BlockTools.isSolid(location.getBlock())) {
			setState(AbilityState.CannotStart);
			return;
		}

		if ((location == null) || ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirBlast, location))
		{
			setState(AbilityState.CannotStart);
			return;
		}

		this.origin = location;
		System.out.println(this.origin.getBlock().getType());
		setState(AbilityState.Preparing);
		this.otherOrigin = true;
	}

	@Override
	public boolean swing() {
		switch (this.state) {
			case None :
			case CannotStart:
				return true;
			case CanStart:
				this.origin = this.player.getEyeLocation();
				AbilityManager.getManager().addInstance(this);
				setState(AbilityState.Preparing);
			case Preparing:
				Entity entity = EntityTools.getTargettedEntity(this.player, this.range);
				if (entity != null) {
					this.direction = Tools.getDirection(this.origin, entity.getLocation()).normalize();
				} else {
					this.direction = Tools.getDirection(this.origin, EntityTools.getTargetedLocation(this.player, this.range)).normalize();
				}
				this.location = this.origin.clone();
				this.bender.cooldown(Abilities.AirBlast, COOLDOWN);
				setState(AbilityState.Progressing);
				return false;
			default:
				return true;
		}
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
			default :
				return true;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (this.state == AbilityState.Preparing) {
			this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4,
					(int) SELECT_RANGE);
			return true;
		}

		if (this.state != AbilityState.Progressing) {
			return false;
		}

		this.speedfactor = SPEED * (Bending.time_step / 1000.);

		Block block = this.location.getBlock();
		for (Block testblock : BlockTools.getBlocksAroundPoint(this.location,
				AFFECT_RADIUS)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(),
						Effect.EXTINGUISH, 0);
			}
		}
		if ((BlockTools.isSolid(block) || block.isLiquid())
				&& !this.affectedlevers.contains(block)) {
			if ((block.getType() == Material.LAVA)
					|| ((block.getType() == Material.STATIONARY_LAVA) && !BlockTools.isTempBlock(block))) {
				if (block.getData() == full) {
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

		for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location,
				AFFECT_RADIUS)) {
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
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
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
			}
			else {
				push.setY(max);
			}
		}

		factor *= 1 - (this.location.distance(this.origin) / (2 * this.range));

		if (isUser
				&& BlockTools.isSolid(this.player.getLocation().add(0, -.5, 0)
						.getBlock())) {
			factor *= .5;
		}

		double comp = velocity.dot(push.clone().normalize());
		if (comp > factor) {
			velocity.multiply(.5);
			velocity.add(push
					.clone()
					.normalize()
					.multiply(
							velocity.clone().dot(push.clone().normalize())));
		} else if ((comp + (factor * .5)) > factor) {
			velocity.add(push.clone().multiply(factor - comp));
		} else {
			velocity.add(push.clone().multiply(factor * .5));
		}
		if (isUser) {
			velocity.multiply(1.0/2.2);
		}
		entity.setVelocity(velocity);
		entity.setFallDistance(0);
		//		if (!isUser && (entity instanceof Player)) {
		//			new Flight((Player) entity, this.player);
		//		}
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.AirBlast;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}
}

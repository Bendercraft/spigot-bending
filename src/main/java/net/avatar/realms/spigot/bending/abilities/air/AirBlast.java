package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.List;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Air Blast", element=BendingType.Air)
public class AirBlast extends Ability {
	private static int ID = Integer.MIN_VALUE;

	public static double speed = Bending.plugin.configuration.getDoubleAttribute(configPrefix + "Air.AirBlast.Speed");
	public static double defaultrange = Bending.plugin.configuration.getDoubleAttribute(configPrefix + "Air.AirBlast.Range");
	public static double affectingradius = Bending.plugin.configuration.getDoubleAttribute(configPrefix + "Air.AirBlast.Radius");
	public static double defaultpushfactor = Bending.plugin.configuration.getDoubleAttribute(configPrefix + "Air.AirBlast.Push-Factor");
	private static double originselectrange = Bending.plugin.configuration.getDoubleAttribute(configPrefix + "Air.AirBlast.Origin-Range");
	static final double maxspeed = 1. / defaultpushfactor;
	public static byte full = 0x0;

	private Location location;
	private Location origin;
	private Vector direction;
	private int id;
	private double speedfactor;
	private double range = defaultrange;
	private double pushfactor = defaultpushfactor;
	private boolean otherorigin = false;

	private List<Block> affectedlevers = new ArrayList<Block>();

	public AirBlast(Player player) {
		super (player, null);
		
		if (state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}

		id = ID++;
	}

	public AirBlast(Location location, Vector direction, Player player,
			double factorpush, Ability parent) {
		super(player, parent);
		if (location.getBlock().isLiquid()) {
			return;
		}

		this.player = player;
		origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();
		id = ID;
		pushfactor *= factorpush;
		AbilityManager.getManager().addInstance(this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	public void setOtherOrigin(Player player) {
		Location location = EntityTools.getTargetedLocation(player,
				originselectrange, BlockTools.nonOpaque);
		if (location.getBlock().isLiquid()
				|| BlockTools.isSolid(location.getBlock())) {
			setState(AbilityState.CannotStart);
			return;
		}

		if (location == null || ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirBlast, location))
		{
			setState(AbilityState.CannotStart);
			return;
		}
		
		origin = location;
		System.out.println(origin.getBlock().getType());
		setState(AbilityState.Started);
		otherorigin = true;
	}
	
	@Override
	public boolean swing() {
		switch (state) {
			case None :
			case CannotStart:
				return true;
			case CanStart:
				origin = player.getEyeLocation();
				AbilityManager.getManager().addInstance(this);
				setState(AbilityState.Started);
			case Started:
				Entity entity = EntityTools.getTargettedEntity(player, range);
				if (entity != null) {
					direction = Tools.getDirection(origin, entity.getLocation()).normalize();
				} else {
					direction = Tools.getDirection(origin, EntityTools.getTargetedLocation(player, range)).normalize();
				}
				location = origin.clone();
				bender.cooldown(Abilities.AirBlast);
				setState(AbilityState.Progressing);
				return false;
			default:
				return true;
		}
	}

	@Override
	public boolean sneak() {
		switch (state) {
			case None:
			case CannotStart:
				return true;
			case CanStart:
				setOtherOrigin(player);
				AbilityManager.getManager().addInstance(this);
				return false;
			case Started:
				setState(AbilityState.CannotStart);
				remove();
				return true;
			default : 
				return true;
		}
	}

	@SuppressWarnings("deprecation")
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		
		if (state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		
		if (state == AbilityState.Started) {
			origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
					(int) originselectrange);
			return true;
		}
		
		if (state != AbilityState.Progressing) {
			return false;
		}

		speedfactor = speed * (Bending.time_step / 1000.);

		Block block = location.getBlock();
		for (Block testblock : BlockTools.getBlocksAroundPoint(location,
				affectingradius)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(),
						Effect.EXTINGUISH, 0);
			}
		}
		if ((BlockTools.isSolid(block) || block.isLiquid())
				&& !affectedlevers.contains(block)) {
			if (block.getType() == Material.LAVA
					|| block.getType() == Material.STATIONARY_LAVA && !TempBlock.isTempBlock(block)) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			return false;
		}

		if (location.distance(origin) > range) {
			return false;
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
				affectingradius)) {
			if ((entity.getEntityId() != player.getEntityId() || otherorigin)) {
				affect(entity);
			}		
		}	
		advanceLocation();
		return true;
	}

	private void advanceLocation() {
		location.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
		location = location.add(direction.clone().multiply(speedfactor));
	}

	private void affect(Entity entity) {
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		
		boolean isUser = entity.getEntityId() == player.getEntityId();
		if (entity.getFireTicks() > 0) {
			entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
			entity.setFireTicks(0);
		}	
		Vector velocity = entity.getVelocity();
		// double mag = Math.abs(velocity.getY());
		double max = maxspeed;
		double factor = pushfactor;
		if (AvatarState.isAvatarState(player)) {
			max = AvatarState.getValue(maxspeed);
			factor = AvatarState.getValue(factor);
		}

		Vector push = direction.clone();
		if (Math.abs(push.getY()) > max && !isUser) {
			if (push.getY() < 0)
				push.setY(-max);
			else
				push.setY(max);
		}

		factor *= 1 - location.distance(origin) / (2 * range);

		if (isUser
				&& BlockTools.isSolid(player.getLocation().add(0, -.5, 0)
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
		} else if (comp + factor * .5 > factor) {
			velocity.add(push.clone().multiply(factor - comp));
		} else {
			velocity.add(push.clone().multiply(factor * .5));
		}
		if (isUser) {
			velocity.multiply(1.0/2.2);
		}
		entity.setVelocity(velocity);
		entity.setFallDistance(0);
		if (!isUser && entity instanceof Player) {
			new Flight((Player) entity, player);
		}			
	}
	
	@Override
	public void remove() {
		AbilityManager.getManager().getInstances(Abilities.AirBlast).remove(this);
		super.remove();
	}

//	private static void playOriginEffect(Player player) {
//		if (!origins.containsKey(player))
//			return;
//		Location origin = origins.get(player);
//		if (!origin.getWorld().equals(player.getWorld())) {
//			origins.remove(player);
//			return;
//		}
//
//		if (EntityTools.getBendingAbility(player) != Abilities.AirBlast
//				|| !EntityTools.canBend(player, Abilities.AirBlast)) {
//			origins.remove(player);
//			return;
//		}
//
//		if (origin.distance(player.getEyeLocation()) > originselectrange) {
//			origins.remove(player);
//			return;
//		}
//
//		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
//				(int) originselectrange);
//	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.AirBlast;
	}

	@Override
	public Object getIdentifier() {
		return id;
	}
	
	@Override
	protected long getMaxMillis() {
		return 25000;
	}
}

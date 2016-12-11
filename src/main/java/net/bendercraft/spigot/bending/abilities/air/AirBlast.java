package net.bendercraft.spigot.bending.abilities.air;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

/**
 * Preparing state = Origin set Progressing state = Airblast thrown
 */
@ABendingAbility(name = AirBlast.NAME, element = BendingElement.AIR)
public class AirBlast extends BendingActiveAbility {
	public final static String NAME = "AirBlast";

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
	private UUID id = UUID.randomUUID();
	private double speedfactor;
	private double range;
	private double pushfactor = PUSH_FACTOR;
	private boolean otherOrigin = false;

	public AirBlast(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.range = DEFAULT_RANGE;
		if(bender.hasPerk(BendingPerk.AIR_AIRBLAST_RANGE)) {
			this.range += 2;
		}
		if(bender.hasPerk(BendingPerk.AIR_CUT)) {
			this.range *= 0.7;
		}
		this.pushfactor = PUSH_FACTOR;
		if(bender.hasPerk(BendingPerk.AIR_AIRBLAST_PUSH)) {
			this.pushfactor *= 1.1;
		}
		if(bender.hasPerk(BendingPerk.AIR_PRESSURE)) {
			this.pushfactor *= 0.5;
		}
		double speed = SPEED;
		if(bender.hasPerk(BendingPerk.AIR_AIRBLAST_SPEED)) {
			speed *= 1.1;
		}
		this.speedfactor = speed * (Bending.getInstance().getManager().getTimestep() / 1000.);
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
		if(getState() == BendingAbilityState.START) {
			this.origin = this.player.getEyeLocation();
			setState(BendingAbilityState.PREPARING);
		}
		//It is NORMAL that both if could follow in same click
		if(getState() == BendingAbilityState.PREPARING) {
			Entity entity = EntityTools.getTargetedEntity(this.player, this.range);
			if (entity != null) {
				this.direction = Tools.getDirection(this.origin, entity.getLocation()).normalize();
			} else {
				this.direction = Tools.getDirection(this.origin, EntityTools.getTargetedLocation(this.player, this.range)).normalize();
			}
			this.location = this.origin.clone();
			long cooldown = COOLDOWN;
			this.bender.cooldown(NAME, cooldown);
			setState(BendingAbilityState.PROGRESSING);
			return false;
		}
		return true;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START 
				|| getState() == BendingAbilityState.PREPARING) {
			Location originLocation = EntityTools.getTargetedLocation(this.player, SELECT_RANGE, BlockTools.getNonOpaque());
			if (originLocation.getBlock().isLiquid() || BlockTools.isSolid(originLocation.getBlock())) {
				return false;
			}

			if ((originLocation == null) || ProtectionManager.isLocationProtectedFromBending(this.player, register, originLocation)) {
				return false;
			}

			this.origin = originLocation;
			this.otherOrigin = true;
			setState(BendingAbilityState.PREPARING);
			return false;
		}
		return true;
	}

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (getState() == BendingAbilityState.PREPARING && !NAME.equals(bender.getAbility())) {
			return false;
		}
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void progress() {
		if (getState() == BendingAbilityState.PREPARING) {
			this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4, (int) SELECT_RANGE);
			return;
		}

		if (getState() != BendingAbilityState.PROGRESSING) {
			remove();
			return;
		}

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
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
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
		if(bender.hasPerk(BendingPerk.AIR_CUT) && entity != player) {
			DamageTools.damageEntity(bender, entity, this, 1);
		}
		entity.setVelocity(velocity);
		entity.setFallDistance(0);
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public void stop() {

	}
	
	public static boolean removeOneBlastAroundPoint(Location location, double radius) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		for (BendingAbility ability : instances.values()) {
			AirBlast blast = (AirBlast) ability;
			Location loc = blast.location;
			if(blast.getBender().hasPerk(BendingPerk.AIR_PRESSURE)) {
				return true;
			}
			if (loc != null && location.getWorld() == loc.getWorld()) {
				if (location.distance(loc) <= radius) {
					blast.remove();
					return true;
				}
			}
		}
		return false;
	}
	
	public static void removeBlastsAroundPoint(Location location, double radius) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		for (BendingAbility ability : instances.values()) {
			AirBlast blast = (AirBlast) ability;
			Location loc = blast.location;
			if(blast.getBender().hasPerk(BendingPerk.AIR_PRESSURE)) {
				continue;
			}
			if (location.getWorld() == loc.getWorld()) {
				if (location.distance(loc) <= radius) {
					blast.remove();
				}
			}
		}
	}
}

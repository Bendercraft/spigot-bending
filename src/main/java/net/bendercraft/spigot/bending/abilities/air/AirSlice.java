package net.bendercraft.spigot.bending.abilities.air;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = AirSlice.NAME, element = BendingElement.AIR)
public class AirSlice extends BendingActiveAbility {
	public final static String NAME = "AirSlice";

	@ConfigurationParameter("Select-Range")
	private static double SELECT_RANGE = 3;

	@ConfigurationParameter("Speed")
	public static double SPEED = 25.0;

	@ConfigurationParameter("Range")
	public static double RANGE = 20;

	@ConfigurationParameter("Distance")
	public static double DISTANCE = 6;

	@ConfigurationParameter("Radius")
	public static double AFFECT_RADIUS = 2.0;

	@ConfigurationParameter("Push-Factor")
	public static double PUSH_FACTOR = 4.0;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;

	public static final byte FULL = 0x0;

	private Location first = null;
	private Location second = null;
	private Vector direction = null;

	private Location origin = null;
	private List<Location> onGoing = new LinkedList<>();
	
	private double distance;
	private double range;
	private long cooldown;
	private double speed;
	

	public AirSlice(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.distance = DISTANCE;
		if(bender.hasPerk(BendingPerk.AIR_AIRSLICE_DISTANCE)) {
			this.distance += 2;
		}
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.AIR_AIRSLICE_RANGE)) {
			this.range += 2;
		}
		if(bender.hasPerk(BendingPerk.AIR_CUT)) {
			this.range *= 0.7;
		}
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.AIR_AIRSLICE_COOLDOWN)) {
			this.cooldown -= 500;
		}
		this.speed = SPEED;
		if(bender.hasPerk(BendingPerk.AIR_AIRSLICE_SPEED)) {
			this.speed *= 1.1;
		}
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
	public boolean swing() {
		if(getState() == BendingAbilityState.PREPARING) {
			remove();
		} else if(getState() == BendingAbilityState.PREPARED) {
			if(this.first == null || this.second == null) {
				return false;
			}

			this.direction = this.player.getEyeLocation().getDirection();
			this.origin = this.first.clone().add(this.second).multiply(0.5);
			double distance = this.second.distance(this.first);
			Vector dir = this.second.toVector().subtract(this.first.toVector()).normalize();

			Vector temp = this.first.toVector();
			for(int i=0 ; i < distance ; i++) {
				this.onGoing.add(temp.toLocation(this.player.getWorld()));
				temp = temp.add(dir);
			}

			setState(BendingAbilityState.PROGRESSING);
			bender.cooldown(this, cooldown);
		}

		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			this.first = EntityTools.getTargetedLocation(this.player, SELECT_RANGE, BlockTools.getNonOpaque());
			if (this.first == null || this.first.getBlock().isLiquid() || BlockTools.isSolid(this.first.getBlock()) || ProtectionManager.isLocationProtectedFromBending(this.player, register, this.first)) {
				return false;
			}
			setState(BendingAbilityState.PREPARING);
		}
		else if(getState() == BendingAbilityState.PREPARING || getState() == BendingAbilityState.PREPARED) {
			this.second = EntityTools.getTargetedLocation(this.player, SELECT_RANGE, BlockTools.getNonOpaque());
			if(this.second != null && this.second.distance(this.first) > distance) {
				this.second = null;
			}
			if (this.second == null || this.second.getBlock().isLiquid() || BlockTools.isSolid(this.second.getBlock()) || ProtectionManager.isLocationProtectedFromBending(this.player, register, this.second)) {
				this.second = null;
				if(getState() == BendingAbilityState.PREPARED) {
					setState(BendingAbilityState.PREPARING);
				}
				return false;
			}
			if(getState() == BendingAbilityState.PREPARING) {
				setState(BendingAbilityState.PREPARED);
			}
		}
		return false;
	}

	private static final Particle.DustOptions DISPLAY = new Particle.DustOptions(Color.fromRGB(220,250,250),2.0f);

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PREPARING || getState() == BendingAbilityState.PREPARED) {
			if(this.first != null) {
				this.player.spawnParticle(Particle.SMOKE_NORMAL, this.first, 1, 0, 0, 0, 0);
			}
			if(this.second != null) {
				this.player.spawnParticle(Particle.SMOKE_NORMAL, this.second, 1, 0, 0, 0, 0);
			}
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			if(this.direction == null || this.origin == null || this.onGoing.isEmpty()) {
				remove();
				return;
			}

			double speedfactor = speed * (Bending.getInstance().getManager().getTimestep() / 1000.);

			final World world = this.origin.getWorld();
			List<Location> toRemove = new LinkedList<>();
			for(Location location : this.onGoing) {
				Block block = location.getBlock();
				for (Block testblock : BlockTools.getBlocksAroundPoint(location, AFFECT_RADIUS)) {
					if (testblock.getType() == Material.FIRE) {
						testblock.setType(Material.AIR);
						testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
					}
				}
				if (BlockTools.isSolid(block) || block.isLiquid()) {
					if (block.getType() == Material.LAVA && !TempBlock.isTempBlock(block)) {
						Levelled data = (Levelled) block.getBlockData();
						if (data.getLevel() == data.getMaximumLevel()) {
							block.setType(Material.OBSIDIAN);
						} else {
							block.setType(Material.COBBLESTONE);
						}
					}
					toRemove.add(location);
					continue;
				}

				for (Entity entity : EntityTools.getEntitiesAroundPoint(location, AFFECT_RADIUS)) {
					affect(location, entity);
				}
				world.spawnParticle(Particle.REDSTONE, location, 1, 0.125, 0.125, 0.125, 0, DISPLAY, true);
				location.add(this.direction.clone().multiply(speedfactor));

				if (location.distance(this.origin) > range) {
					toRemove.add(location);
					continue;
				}
			}

			this.onGoing.removeAll(toRemove);

			if(this.onGoing.isEmpty()) {
				remove();
			}
		}
	}

	private void affect(Location location, Entity entity) {
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

		double max = 1/PUSH_FACTOR;
		double factor = PUSH_FACTOR;

		Vector push = this.direction.clone();
		if ((Math.abs(push.getY()) > max) && !isUser) {
			if (push.getY() < 0) {
				push.setY(-max);
			} else {
				push.setY(max);
			}
		}

		factor *= 1 - (location.distance(this.origin) / (2 * range));

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
		if(bender.hasPerk(BendingPerk.AIR_CUT) && entity != player) {
			DamageTools.damageEntity(bender, entity, this, 2);
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}

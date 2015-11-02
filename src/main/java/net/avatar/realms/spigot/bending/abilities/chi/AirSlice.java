package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.LinkedList;
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
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = "Air Slice", bind = BendingAbilities.AirSlice, element = BendingElement.ChiBlocker, affinity = BendingAffinity.ChiAir)
public class AirSlice extends BendingActiveAbility {

	@ConfigurationParameter("Select-Range")
	private static double SELECT_RANGE = 6;

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

	public static final byte FULL = 0x0;

	private Location first = null;
	private Location second = null;
	private Vector direction = null;

	private Location origin = null;
	private List<Location> onGoing = new LinkedList<Location>();

	public AirSlice(Player player) {
		super(player);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.Preparing) {
			remove();
		} else if(getState() == BendingAbilityState.Prepared) {
			if(this.first == null || this.second == null) {
				return false;
			}

			if(!ComboPoints.consume(this.player,1)) {
				remove();
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

			setState(BendingAbilityState.Progressing);
		}

		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.Start) {
			this.first = EntityTools.getTargetedLocation(this.player, SELECT_RANGE, BlockTools.getNonOpaque());
			if (this.first == null || this.first.getBlock().isLiquid() || BlockTools.isSolid(this.first.getBlock()) || ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.AirSlice, this.first)) {
				return false;
			}
			if(!ComboPoints.consume(this.player,1)) {
				remove();
				return false;
			}
			setState(BendingAbilityState.Preparing);
		} else if(getState() == BendingAbilityState.Preparing || getState() == BendingAbilityState.Prepared) {
			this.second = EntityTools.getTargetedLocation(this.player, SELECT_RANGE, BlockTools.getNonOpaque());
			if(this.second != null && this.second.distance(this.first) > DISTANCE) {
				this.second = null;
			}
			if (this.second == null || this.second.getBlock().isLiquid() || BlockTools.isSolid(this.second.getBlock()) || ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.AirSlice, this.second)) {
				this.second = null;
				if(getState() == BendingAbilityState.Prepared) {
					setState(BendingAbilityState.Preparing);
				}
				return false;
			}
			if(getState() == BendingAbilityState.Preparing) {
				setState(BendingAbilityState.Prepared);
			}
			if(!ComboPoints.consume(this.player,1)) {
				remove();
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if(getState() == BendingAbilityState.Preparing || getState() == BendingAbilityState.Prepared) {
			if(this.first != null) {
				this.first.getWorld().playEffect(this.first, Effect.SMOKE, 4, (int) SELECT_RANGE+4);
			}
			if(this.second != null) {
				this.second.getWorld().playEffect(this.second, Effect.SMOKE, 4, (int) SELECT_RANGE+4);
			}
		} else if(getState() == BendingAbilityState.Progressing) {
			if(this.direction == null || this.origin == null || this.onGoing.isEmpty()) {
				remove();
				return;
			}

			double speedfactor = SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);

			List<Location> toRemove = new LinkedList<Location>();
			for(Location location : this.onGoing) {
				Block block = location.getBlock();
				for (Block testblock : BlockTools.getBlocksAroundPoint(location, AFFECT_RADIUS)) {
					if (testblock.getType() == Material.FIRE) {
						testblock.setType(Material.AIR);
						testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
					}
				}
				if (BlockTools.isSolid(block) || block.isLiquid()) {
					if ((block.getType() == Material.LAVA) || ((block.getType() == Material.STATIONARY_LAVA) && !BlockTools.isTempBlock(block))) {
						if (block.getData() == FULL) {
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
				location.getWorld().playEffect(location, Effect.SMOKE, 4, (int) RANGE+4);
				location.add(this.direction.clone().multiply(speedfactor));

				if (location.distance(this.origin) > RANGE) {
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

		factor *= 1 - (location.distance(this.origin) / (2 * RANGE));

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
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}

package net.avatar.realms.spigot.bending.abilities.air;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@ABendingAbility(name = "Air Burst", bind = BendingAbilities.AirBurst, element = BendingElement.Air)
public class AirBurst extends BendingActiveAbility {
	
	@ConfigurationParameter("Charge-Time")
	public static long DEFAULT_CHARGETIME = 1750;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 2000;
	
	@ConfigurationParameter("Push-Factor")
	public static double PUSHFACTOR = 1.5;
	
	@ConfigurationParameter("Del-Theta")
	public static double DELTHETA = 10;
	
	@ConfigurationParameter("Del-Phi")
	public static double DELPHI = 10;
	
	@ConfigurationParameter("Fall-Threshold")
	private static double THRESHOLD = 10;
	
	@ConfigurationParameter("Blast-Speed")
	public static double BLAST_SPEED = 25.0;
	
	@ConfigurationParameter("Blast-Select-Range")
	private static double BLAST_SELECT_RANGE = 10;
	
	@ConfigurationParameter("Blast-Radius")
	public static double BLAST_AFFECT_RADIUS = 2.0;
	
	@ConfigurationParameter("Blast-Range")
	public static double BLAST_RANGE = 20;
	
	@ConfigurationParameter("Blast-Push-Factor")
	public static double BLAST_PUSH_FACTOR = 3.0;
	
	private long chargetime;
	private List<BurstBlast> blasts;
	
	public AirBurst(Player player) {
		super(player);
		
		blasts = new LinkedList<BurstBlast>();
		chargetime = DEFAULT_CHARGETIME;
		
		if (AvatarState.isAvatarState(player) && AvatarState.FACTOR != 0) {
			this.chargetime = (long) (DEFAULT_CHARGETIME / AvatarState.FACTOR);
		}
	}
	
	@Override
	public boolean sneak() {
		if (getState().equals(BendingAbilityState.Start)) {
			setState(BendingAbilityState.Preparing);
		}
		return false;
	}
	
	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.Prepared) {
			coneBurst();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean fall() {
		if (this.player.getFallDistance() < THRESHOLD) {
			return false;
		}
		
		fallBurst();
		
		return true;
	}
	
	@Override
	public void progress() {
		if(getState() == BendingAbilityState.Progressing) {
			List<BurstBlast> toRemove = new LinkedList<BurstBlast>();
			for(BurstBlast blast : blasts) {
				if(!blast.progress()) {
					toRemove.add(blast);
				}
			}
			blasts.removeAll(toRemove);
			if(blasts.isEmpty()) {
				remove();
			}
			return;
		}
		if (!this.player.isSneaking()) {
			if (getState().equals(BendingAbilityState.Prepared)) {
				sphereBurst();
			}
		} else if (!getState().equals(BendingAbilityState.Prepared) && (System.currentTimeMillis() > (this.startedTime + this.chargetime))) {
			if (EntityTools.getBendingAbility(this.player) != BendingAbilities.AirBurst) {
				remove();
				return;
			}
			setState(BendingAbilityState.Prepared);
		} else if (getState() == BendingAbilityState.Prepared) {
			Location location = this.player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.SMOKE, Tools.getIntCardinalDirection(location.getDirection()), 3);
		}
	}
	
	public static boolean isAirBursting(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AirBurst);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}
		return instances.containsKey(player);
	}
	
	public boolean isCharged() {
		return getState() == BendingAbilityState.Prepared;
	}
	
	public static AirBurst getAirBurst(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AirBurst);
		if ((instances == null) || instances.isEmpty()) {
			return null;
		}
		if (!instances.containsKey(player)) {
			return null;
		}
		return (AirBurst) instances.get(player);
	}
	
	private void sphereBurst() {
		Location location = this.player.getEyeLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += AirBurst.DELTHETA) {
			double dphi = AirBurst.DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				blasts.add(new BurstBlast(location, direction.normalize(), AirBurst.PUSHFACTOR, player));
			}
		}
		setState(BendingAbilityState.Progressing);
	}
	
	private void coneBurst() {
		Location location = this.player.getEyeLocation();
		Vector vector = location.getDirection();
		double angle = Math.toRadians(30);
		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += AirBurst.DELTHETA) {
			double dphi = AirBurst.DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				if (direction.angle(vector) <= angle) {
					blasts.add(new BurstBlast(location, direction.normalize(), AirBurst.PUSHFACTOR, player));
				}
			}
		}
		setState(BendingAbilityState.Progressing);
	}
	
	private void fallBurst() {
		Location location = this.player.getLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 75; theta < 105; theta += AirBurst.DELTHETA) {
			double dphi = AirBurst.DELTHETA / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				blasts.add(new BurstBlast(location, direction.normalize(), AirBurst.PUSHFACTOR, player));
			}
		}
		setState(BendingAbilityState.Progressing);
	}
	
	@Override
	protected long getMaxMillis() {
		return 60 * 10 * 1000L;
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if (isAirBursting(this.player)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}
	
	private class BurstBlast {
		private Location origin;
		private Vector direction;
		private Location location;
		private double pushfactor;
		private double speedfactor;
		private Player player;
		private double range;

		public BurstBlast(Location location, Vector direction, double factorpush, Player player) {
			if (location.getBlock().isLiquid()) {
				return;
			}
			this.player = player;
			this.origin = location.clone();
			this.direction = direction.clone();
			this.location = location.clone();
			this.pushfactor *= factorpush;
			this.range = BLAST_RANGE;
		}
		
		@SuppressWarnings("deprecation")
		public boolean progress() {
			if (getState() == BendingAbilityState.Preparing) {
				this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4, (int) BLAST_SELECT_RANGE);
				return true;
			}

			if (getState() != BendingAbilityState.Progressing) {
				return false;
			}

			this.speedfactor = BLAST_SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);

			Block block = this.location.getBlock();
			for (Block testblock : BlockTools.getBlocksAroundPoint(this.location, BLAST_AFFECT_RADIUS)) {
				if (testblock.getType() == Material.FIRE) {
					testblock.setType(Material.AIR);
					testblock.getWorld().playEffect(testblock.getLocation(), Effect.EXTINGUISH, 0);
				}
			}
			if (BlockTools.isSolid(block) || block.isLiquid()) {
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

			for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location, BLAST_AFFECT_RADIUS)) {
				if (entity.getEntityId() != this.player.getEntityId()) {
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
			
			double max = 1/BLAST_PUSH_FACTOR;
			double factor = this.pushfactor;
			if (AvatarState.isAvatarState(this.player)) {
				max = AvatarState.getValue(1/BLAST_PUSH_FACTOR);
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
		}
	}
}

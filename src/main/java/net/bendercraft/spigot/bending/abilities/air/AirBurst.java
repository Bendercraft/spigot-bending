package net.bendercraft.spigot.bending.abilities.air;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = AirBurst.NAME, element = BendingElement.AIR)
public class AirBurst extends BendingActiveAbility {
	public final static String NAME = "AirBurst";
	
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
	
	public AirBurst(RegisteredAbility register, Player player) {
		super(register, player);
		
		blasts = new LinkedList<BurstBlast>();
		chargetime = DEFAULT_CHARGETIME;
	}
	
	@Override
	public boolean sneak() {
		if (getState().equals(BendingAbilityState.START)) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}
	
	@Override
	public boolean swing() {
		if (isState(BendingAbilityState.PREPARED)) {
			coneBurst();
			return false;
		}
		return true;
	}
	
	@Override
	public boolean fall() {
		if (player.getFallDistance() < THRESHOLD) {
			return false;
		}
		
		fallBurst();
		
		return true;
	}

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if(isState(BendingAbilityState.PREPARING) && !player.isSneaking()) {
			return false;
		}
		if (!isState(BendingAbilityState.PROGRESSING) && !NAME.equals(bender.getAbility())) {
			return false;
		}
		return true;
	}
	
	@Override
	public void progress() {
		if(isState(BendingAbilityState.PREPARING)) {
			if(System.currentTimeMillis() > (this.startedTime + this.chargetime)) {
				setState(BendingAbilityState.PREPARED);
			}
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.SPELL, loc, 1, 0, 0, 0, 0);
		} else if(isState(BendingAbilityState.PREPARED)) {
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
			if (!this.player.isSneaking()) {
				sphereBurst();
			}
		} else if(isState(BendingAbilityState.PROGRESSING)) {
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
				blasts.add(new BurstBlast(this, location, direction.normalize(), AirBurst.PUSHFACTOR, player));
			}
		}
		setState(BendingAbilityState.PROGRESSING);
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
					blasts.add(new BurstBlast(this, location, direction.normalize(), AirBurst.PUSHFACTOR, player));
				}
			}
		}
		setState(BendingAbilityState.PROGRESSING);
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
				blasts.add(new BurstBlast(this, location, direction.normalize(), AirBurst.PUSHFACTOR, player));
			}
		}
		setState(BendingAbilityState.PROGRESSING);
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

		if (AbilityManager.getManager().isUsingAbility(this.player, NAME)) {
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
		private AirBurst parent;

		public BurstBlast(AirBurst parent, Location location, Vector direction, double factorpush, Player player) {
			if (location.getBlock().isLiquid()) {
				return;
			}
			this.parent = parent;
			this.player = player;
			this.origin = location.clone();
			this.direction = direction.clone();
			this.location = location.clone();
			this.pushfactor = PUSHFACTOR * factorpush;
			this.range = BLAST_RANGE;
		}
		
		@SuppressWarnings("deprecation")
		public boolean progress() {
			if (getState() == BendingAbilityState.PREPARING) {
				this.origin.getWorld().playEffect(this.origin, Effect.SMOKE, 4, (int) BLAST_SELECT_RANGE);
				return true;
			}

			if (getState() != BendingAbilityState.PROGRESSING) {
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
				if ((block.getType() == Material.LAVA) || ((block.getType() == Material.STATIONARY_LAVA) && !TempBlock.isTempBlock(block))) {
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
			BendingHitEvent event = new BendingHitEvent(parent, entity);
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
				velocity = velocity.multiply(.5);
				velocity = velocity.add(push.clone().normalize().multiply(velocity.clone().dot(push.clone().normalize())));
			} else if ((comp + (factor * .5)) > factor) {
				velocity = velocity.add(push.clone().multiply(factor - comp));
			} else {
				velocity = velocity.add(push.clone().multiply(factor * .5));
			}
			if (isUser) {
				velocity = velocity.multiply(1.0 / 2.2);
			}
			entity.setVelocity(velocity);
			entity.setFallDistance(0);
		}
	}
}

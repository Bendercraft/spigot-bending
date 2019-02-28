package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
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
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;

/**
 * State Preparing : Player is sneaking but burst is not ready yet State
 * Prepared : Player is sneaking and burst is ready
 *
 * @author Noko
 */
@ABendingAbility(name = FireBurst.NAME, element = BendingElement.FIRE)
public class FireBurst extends BendingActiveAbility {
	public final static String NAME = "FireBurst";
	
	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 1500;

	@ConfigurationParameter("Damage-Cone")
	private static int DAMAGE_CONE = 8;
	
	@ConfigurationParameter("Damage-Sphere")
	private static int DAMAGE_SPHERE = 4;

	@ConfigurationParameter("Del-Theta")
	private static double DELTHETA = 10;

	@ConfigurationParameter("Del-Phi")
	private static double DELPHI = 10;

	@ConfigurationParameter("Power")
	private static int POWER = 5;
	
	@ConfigurationParameter("Range-Cone")
	private static int RANGE_CONE = 25;
	
	@ConfigurationParameter("Range-Sphere")
	private static int RANGE_SPHERE = 15;
	
	@ConfigurationParameter("Blast-Speed")
	private static double BLAST_SPEED = 15;
	
	@ConfigurationParameter("Blast-Radius")
	public static double BLAST_AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Blast-Push")
	private static double BLAST_PUSH_FACTOR = 0.3;
	
	@ConfigurationParameter("Flame-Time")
	private static int FLAME_TIME = 1;

	private long chargetime = CHARGE_TIME;
	
	private List<BurstBlast> blasts;

	private int rangeCone;
	private int rangeSphere;
	private int damageCone;
	private int damageSphere;

	public FireBurst(RegisteredAbility register, Player player) {
		super(register, player);

		if (AvatarState.isAvatarState(player)) {
			this.chargetime = 0;
		}
		
		this.rangeCone = RANGE_CONE;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBURST_CONE_RANGE_1)) {
			this.rangeCone += 1;
		}
		if(bender.hasPerk(BendingPerk.FIRE_FIREBURST_CONE_RANGE_2)) {
			this.rangeCone += 1;
		}
		
		this.rangeSphere = RANGE_SPHERE;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBURST_AREA_ZONE)) {
			this.rangeSphere += 1;
		}
		
		this.damageCone = DAMAGE_CONE;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBURST_CONE_DAMAGE)) {
			this.damageCone += 1;
		}
		
		this.damageSphere = DAMAGE_SPHERE;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBURST_DAMAGE_ZONE)) {
			this.damageSphere += 1;
		}
		
		if(bender.hasPerk(BendingPerk.FIRE_OVERLOAD)) {
			this.damageCone *= 1.1;
			this.rangeCone *= 0.95;
			
			this.damageSphere *= 1.1;
			this.rangeSphere *= 0.95;
		}
		if(bender.hasPerk(BendingPerk.FIRE_SNIPER)) {
			this.damageCone *= 0.95;
			this.rangeCone *= 1.1;
			
			this.damageSphere *= 0.95;
			this.rangeSphere *= 1.1;
		}
		
		this.blasts = new LinkedList<>();
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, POWER)) {
			return false;
		}

		return true;
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
		if (getState() == BendingAbilityState.PREPARED) {
			coneBurst();
		}
		return false;
	}

	private void coneBurst() {
		Location location = this.player.getEyeLocation();
		Vector vector = location.getDirection();
		double angle = Math.toRadians(30);
		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += DELTHETA) {
			double dphi = DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				if (direction.angle(vector) <= angle) {
					blasts.add(new BurstBlast(this.player, this.bender, this, location, direction.normalize(), rangeCone, damageCone));
				}
			}
		}
		bender.fire.consume(NAME, POWER);
		setState(BendingAbilityState.PROGRESSING);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!NAME.equals(EntityTools.getBendingAbility(player))) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PROGRESSING) {
			List<BurstBlast> toRemove = new LinkedList<>();
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
			if (getState().equals(BendingAbilityState.PREPARED)) {
				sphereBurst();
			} else {
				remove();
			}
			return;
		}

		if (getState() != BendingAbilityState.PREPARED) {
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.SPELL, loc, 1, 0, 0, 0, 0);
			if (System.currentTimeMillis() > (this.startedTime + this.chargetime)) {
				setState(BendingAbilityState.PREPARED);
			}
			return;
		}

		if (getState() == BendingAbilityState.PREPARED) {
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
		}
	}

	private void sphereBurst() {
		Location location = this.player.getEyeLocation();

		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += DELTHETA) {
			double dphi = DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				blasts.add(new BurstBlast(this.player, this.bender, this, location, direction.normalize(), rangeSphere, damageSphere));
			}
		}
		bender.fire.consume(NAME, POWER);
		setState(BendingAbilityState.PROGRESSING);
	}

	@Override
	public void stop() {
		
	}

	public boolean isCharged() {
		return getState() == BendingAbilityState.PREPARED;
	}

	public static boolean isFireBursting(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		return instances.containsKey(player);
	}

	public static FireBurst getFireBurst(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		return (FireBurst) instances.get(player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	public static class BurstBlast {

		private static final double PARTICLE_SPEED   = 1.0 / 48.0;

		private Player player;
		private Location location;
		private Location origin;
		private Vector direction;
		private BendingPlayer bender;
		private double range;
		private double damage;
		private double speedfactor;
		private BendingAbility ability;
		private int flameTime;

		public BurstBlast(Player player, BendingPlayer bender, BendingAbility ability, Location location, Vector direction, int range, double damage) {
			this.player = player;
			this.bender = bender;
			this.ability = ability;
			this.range = range;
			this.location = location.clone();
			this.origin = location.clone();
			this.direction = direction.clone().normalize();
			this.damage = damage;
			this.speedfactor = BLAST_SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);
			this.flameTime = FLAME_TIME;
			if(bender.hasPerk(BendingPerk.FIRE_FIREBURST_FLAME)) {
				this.flameTime += 1;
			}
		}
		
		public boolean progress() {
			if (this.location.distance(this.origin) > this.range) {
				return false;
			}
			
			if(this.location.getBlock().getType().isSolid() || this.location.getBlock().isLiquid()) {
				return false;
			}

			PluginTools.removeSpouts(this.location, this.player);

			double radius = FireBlast.AFFECTING_RADIUS;
			Player source = this.player;
			if (EarthBlast.annihilateBlasts(this.location, radius, source) || WaterManipulation.annihilateBlasts(this.location, radius, source) || FireBlast.shouldAnnihilateBlasts(this.location, radius, source, false)) {
				return false;
			}

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, BLAST_AFFECTING_RADIUS)) {
				if (affect(entity)) {
					return false;
				}
			}

			location.getWorld().spawnParticle(Particle.FLAME, location, 2, 0.75, 0.75, 0.75, PARTICLE_SPEED, null, true);
			this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
			
			return true;
		}

		private boolean affect(LivingEntity entity) {
			BendingHitEvent event = new BendingHitEvent(ability, entity);
			Bending.callEvent(event);
			if(event.isCancelled()) {
				return false;
			}
			if (entity == player) {
				return false;
			}
			
			if (AvatarState.isAvatarState(this.player)) {
				entity.setVelocity(this.direction.clone().multiply(AvatarState.getValue(BLAST_PUSH_FACTOR)));
			} else {
				entity.setVelocity(this.direction.clone().multiply(BLAST_PUSH_FACTOR));
			}
			
			DamageTools.damageEntity(bender, entity, ability, damage);
			Enflamed.enflame(this.player, entity, flameTime, ability);
			return true;
		}
		
	}
}

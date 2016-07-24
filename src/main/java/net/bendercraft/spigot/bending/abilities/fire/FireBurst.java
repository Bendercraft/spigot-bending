package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
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
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

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
	private static int DAMAGE_CONE = 6;
	
	@ConfigurationParameter("Damage-Sphere")
	private static int DAMAGE_SPHERE = 3;

	@ConfigurationParameter("Del-Theta")
	private static double DELTHETA = 10;

	@ConfigurationParameter("Del-Phi")
	private static double DELPHI = 10;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 2500;
	
	@ConfigurationParameter("Range-Cone")
	private static int RANGE_CONE = 15;
	
	@ConfigurationParameter("Range-Sphere")
	private static int RANGE_SPHERE = 10;
	
	@ConfigurationParameter("Blast-Speed")
	private static double BLAST_SPEED = 15;
	
	@ConfigurationParameter("Blast-Radius")
	public static double BLAST_AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Blast-Push")
	private static double BLAST_PUSH_FACTOR = 0.3;

	private long chargetime = CHARGE_TIME;
	
	private List<BurstBlast> blasts;

	public FireBurst(RegisteredAbility register, Player player) {
		super(register, player);

		if (AvatarState.isAvatarState(player)) {
			this.chargetime = 0;
		}
		
		blasts = new LinkedList<BurstBlast>();
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
					blasts.add(new BurstBlast(this.player, this.bender, this, location, direction.normalize(), RANGE_CONE, DAMAGE_CONE));
				}
			}
		}
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
			if (getState().equals(BendingAbilityState.PREPARED)) {
				sphereBurst();
			}
			return;
		}

		if (getState() != BendingAbilityState.PREPARED) {
			if (System.currentTimeMillis() > (this.startedTime + this.chargetime)) {
				setState(BendingAbilityState.PREPARED);
			}
		}

		if (getState() == BendingAbilityState.PREPARED) {
			Location location = this.player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4, 3);
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
				blasts.add(new BurstBlast(this.player, this.bender, this, location, direction.normalize(), RANGE_SPHERE, DAMAGE_SPHERE));
			}
		}
		
		setState(BendingAbilityState.PROGRESSING);
	}

	@Override
	public void stop() {
		this.bender.cooldown(NAME, COOLDOWN);
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
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances.containsKey(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	public static class BurstBlast {
		private Player player;
		private Location location;
		private Location origin;
		private Vector direction;
		private BendingPlayer bender;
		private double range;
		private double damage;
		private double speedfactor;
		private BendingAbility ability;

		public BurstBlast(Player player, BendingPlayer bender, BendingAbility ability, Location location, Vector direction, int range, double damage) {
			this.player = player;
			this.bender = bender;
			this.ability = ability;
			this.range = range;
			this.location = location.clone();
			this.origin = location.clone();
			this.direction = direction.clone().normalize();
			this.damage = damage*1.5;
			this.speedfactor = BLAST_SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);
			if (this.bender.hasPath(BendingPath.NURTURE)) {
				this.damage *= 0.8;
			}
			if (this.bender.hasPath(BendingPath.LIFELESS)) {
				this.damage *= 1.1;
			}
		}
		
		public boolean progress() {
			if (this.location.distance(this.origin) > this.range) {
				return false;
			}

			PluginTools.removeSpouts(this.location, this.player);

			double radius = FireBlast.AFFECTING_RADIUS;
			Player source = this.player;
			if (EarthBlast.annihilateBlasts(this.location, radius, source) || WaterManipulation.annihilateBlasts(this.location, radius, source) || FireBlast.shouldAnnihilateBlasts(this.location, radius, source, false)) {
				return false;
			}

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, BLAST_AFFECTING_RADIUS)) {
				boolean result = affect(entity);
				// If result is true, do not return here ! we need to iterate
				// fully !
				if (result == false) {
					return false;
				}
			}

			this.location.getWorld().playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 0, (int) this.range);
			this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
			
			return true;
		}

		private boolean affect(LivingEntity entity) {
			if (entity.getEntityId() != this.player.getEntityId()) {
				if (ProtectionManager.isEntityProtected(entity)) {
					return false;
				}
				if (AvatarState.isAvatarState(this.player)) {
					entity.setVelocity(this.direction.clone().multiply(AvatarState.getValue(BLAST_PUSH_FACTOR)));
				} else {
					entity.setVelocity(this.direction.clone().multiply(BLAST_PUSH_FACTOR));
				}
				Enflamed.enflame(this.player, entity, 1);
				DamageTools.damageEntity(bender, entity, ability, damage);
				return false;
			}
			return true;
		}
	}
}

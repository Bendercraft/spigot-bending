package net.bendercraft.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Shockwave.NAME, element = BendingElement.EARTH)
public class Shockwave extends BendingActiveAbility {
	public final static String NAME = "Shockwave";
	
	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 2500;
	@ConfigurationParameter("Fall-Threshold")
	public static double FALL_THRESHOLD = 9;
	@ConfigurationParameter("Cone-Angle")
	private static double CONE_ANGLE = 40;
	@ConfigurationParameter("Radius")
	private static double RADIUS = 15;
	@ConfigurationParameter("Damage")
	private final double DAMAGE = 5;

	private long chargetime;
	private double angle;
	private double radius;
	private double damage;
	private double fallThresold;

	private List<Ripple> ripples = new LinkedList<Ripple>();

	public Shockwave(RegisteredAbility register, Player player) {
		super(register, player);

		this.radius = RADIUS;
		if(bender.hasPerk(BendingPerk.EARTH_SHOCKWAVE_RANGE)) {
			this.radius += 2;
		}
		
		this.chargetime = CHARGE_TIME;
		if(bender.hasPerk(BendingPerk.EARTH_SHOCKWAVE_CHARGETIME)) {
			this.chargetime -= 500;
		}
		
		this.damage = DAMAGE;
		if(bender.hasPerk(BendingPerk.EARTH_SHOCKWAVE_DAMAGE)) {
			this.damage += 1;
		}
		
		this.fallThresold = FALL_THRESHOLD;
		if(bender.hasPerk(BendingPerk.EARTH_SHOCKWAVE_POWER)) {
			this.fallThresold -= 2;
		}
		
		double angle = CONE_ANGLE;
		if(bender.hasPerk(BendingPerk.EARTH_SHOCKWAVE_ANGLE)) {
			angle += 10;
		}
		this.angle = Math.toRadians(angle);
	}

	@Override
	public boolean swing() {
		if (isState(BendingAbilityState.PREPARED)) {
			double dtheta = 360. / (2 * Math.PI * radius) - 1;
			for (double theta = 0; theta < 360; theta += dtheta) {
				double rtheta = Math.toRadians(theta);
				Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
				if (vector.angle(player.getEyeLocation().getDirection()) < angle)
					ripples.add(new Ripple(player, this, vector.normalize(), radius, damage));
			}
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if (isState(BendingAbilityState.START)) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}

	@Override
	public boolean fall() {
		if (!isState(BendingAbilityState.START)
			|| player.getFallDistance() < fallThresold
			|| !BlockTools.isEarthbendable(player, player.getLocation().add(0, -1, 0).getBlock())){
			return false;
		}
		areaWave();
		return true;
	}

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!EntityTools.canBend(player, register) ||
				(!NAME.equals(bender.getAbility()) && !isState(BendingAbilityState.PROGRESSING))) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if (isState(BendingAbilityState.PREPARING)) {
			if (!player.isSneaking() || !BlockTools.isEarthbendable(player, player.getLocation().add(0, -1, 0).getBlock())) {
				remove();
				return;
			}
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.SPELL, loc, 1, 0, 0, 0, 0);
			if (System.currentTimeMillis() > startedTime + chargetime) {
				setState(BendingAbilityState.PREPARED);
			}
		} else if (isState(BendingAbilityState.PREPARED)) {
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);

			if (!player.isSneaking()) {
				areaWave();
			}
		} else if (isState(BendingAbilityState.PROGRESSING)) {
			List<Ripple> toRemove = new LinkedList<Ripple>();
			for (Ripple ripple : ripples) {
				if (!ripple.progress()) {
					toRemove.add(ripple);
				}
			}
			ripples.removeAll(toRemove);
			if (ripples.isEmpty()) {
				remove();
				return;
			}
		} else {
			remove();
		}
	}

	private void areaWave() {
		// Area - either because unsneak or falling
		double dtheta = 360. / (2 * Math.PI * radius/2) - 1;
		for (double theta = 0; theta < 360; theta += dtheta) {
			double rtheta = Math.toRadians(theta);
			Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
			ripples.add(new Ripple(player, this, vector.normalize(), radius/2, damage));
		}
		setState(BendingAbilityState.PROGRESSING);
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public void stop() {
		
	}

}

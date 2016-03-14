package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = ExplosiveShot.NAME, affinity = BendingAffinity.BOW)
public class ExplosiveShot extends BendingActiveAbility {
	public final static String NAME = "ExplosiveShot";
	
	@ConfigurationParameter("Range-Damage")
	private static int RANGE_DAMAGE = 3;
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 7;
	@ConfigurationParameter("Sound-Radius")
	private static int SOUND_RADIUS = 35;
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;
	
	private static final Particle EXPLODE = Particle.SPELL;
	
	private Arrow arrow;
	private List<FireStream> firestreams = new LinkedList<FireStream>();

	public ExplosiveShot(RegisteredAbility register, Player player) {
		super(register, player);
	}
	
	/**
	 * Entry point for this ability, gets constructed when a player shot an arrow
	 */
	public void shot(Arrow arrow) {
		this.arrow = arrow;
		setState(BendingAbilityState.PREPARED);
		bender.cooldown(NAME, COOLDOWN);
	}
	
	/**
	 * When projectile hits, it EXPLODES §§§§
	 */
	public void explode() {
		if(arrow == null) {
			return;
		}
		Location location = arrow.getLocation();
		
		for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, RANGE_DAMAGE)) {
			EntityTools.damageEntity(bender, entity, DAMAGE);
		}

		Vector direction = arrow.getLocation().getDirection().clone();
		for (double degrees = 0; degrees < 360; degrees += 10) {
			double angle = Math.toRadians(degrees);
			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = (x * Math.cos(angle)) - (z * Math.sin(angle));
			vz = (x * Math.sin(angle)) + (z * Math.cos(angle));

			direction.setX(vx);
			direction.setZ(vz);

			firestreams.add(new FireStream(location, direction, player, RANGE));
			firestreams.add(new FireStream(location.add(0, 1, 0), direction, player, RANGE));
			firestreams.add(new FireStream(location.add(0, -1, 0), direction, player, RANGE));
			setState(BendingAbilityState.PROGRESSING);
		}
		location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, SOUND_RADIUS / 16.0f, 1);
		location.getWorld().spawnParticle(EXPLODE, location, 1, 0, 0, 0);
	}

	@Override
	public boolean swing() {
		
		return false;
	}

	@Override
	public boolean sneak() {
		
		return false;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PROGRESSING) {
			List<FireStream> test = new LinkedList<FireStream>(firestreams);
			for(FireStream stream : test) {
				if(!stream.progress()) {
					firestreams.remove(stream);
				}
			}
			if(firestreams.isEmpty()) {
				remove();
			}
		}
	}
	
	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public void stop() {
		
	}

}

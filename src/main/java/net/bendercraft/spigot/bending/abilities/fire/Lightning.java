package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Lightning.NAME, affinity = BendingAffinity.LIGHTNING, canBeUsedWithTools = true)
public class Lightning extends BendingActiveAbility {
	public final static String NAME = "Lightning";

	@ConfigurationParameter("Range")
	public static int RANGE = 50;

	@ConfigurationParameter("Charge-Time")
	private static long WARMUP = 4000;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 10;

	@ConfigurationParameter("Power")
	public static int POWER = 5;
	
	@ConfigurationParameter("Radius")
	public static double RADIUS = 2;

	private int damage = DAMAGE;
	private long warmup;
	private Location targetLocation;
	private Location redirectLocation;

	public Lightning(RegisteredAbility register, Player player) {
		super(register, player);

		this.warmup = WARMUP;
		if (AvatarState.isAvatarState(this.player)) {
			this.warmup *= 0.5;
		}
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
		if(getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}

	private void strike() {
		bender.fire.consume(NAME, POWER);
		
		// Get paths & targets
		getTargetLocation();
		List<Location> locations = new LinkedList<Location>();
		if(redirectLocation != null) {
			locations.addAll(randomizePath(player.getEyeLocation(), redirectLocation));
			locations.addAll(randomizePath(redirectLocation, targetLocation));
		} else {
			locations.addAll(randomizePath(player.getEyeLocation(), targetLocation));
		}
		
		// Effect + sound
		for(int i=0; i < locations.size()-1 ; i++) {
			Vector direction = locations.get(i+1).clone().subtract(locations.get(i)).toVector();
			double distance = direction.length();
			direction = direction.normalize();
			for(double j=0 ; j < distance ; j += 0.1) {
				Location loc = locations.get(i).clone().add(direction.clone().multiply(j));
				int red = 0x80;
				int green = 0xf2;
				int blue = 0xff;
				DustOptions data = new Particle.DustOptions(Color.fromRGB(red, green, blue), 1);
				loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, data);
			}
		}
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.0f);
		if(redirectLocation != null) {
			redirectLocation.getWorld().playSound(redirectLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.0f);
		}
		targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.0f);
		
		// Damage !
		for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(targetLocation, RADIUS)) {
			affect(entity);
		}
	}
	
	private List<Location> randomizePath(Location start, Location end) {
		Vector direction = end.clone().subtract(start.clone()).toVector();
		double distance = direction.length();
		direction = direction.normalize();
		List<Location> locations = new LinkedList<Location>();
		
		locations.add(start.clone());
		for(int i = 0 ; i < distance; i+=3) {
			int[] randoms = new Random().ints(3, -1, 1).toArray();
			Location loc = start.clone().add(direction.clone().multiply(i));
			loc = loc.add(randoms[0], randoms[1], randoms[2]);
			locations.add(loc);
		}
		locations.add(end.clone());
		
		return locations;
	}

	private void getTargetLocation() {
		targetLocation = EntityTools.getTargetedLocation(this.player, RANGE);
		LivingEntity target = EntityTools.getTargetedEntity(this.player, RANGE);
		if (target != null) {
			targetLocation = target.getEyeLocation();
			// Check redirection
			if (target instanceof Player) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) target);
				if ((bPlayer != null) && (bPlayer.getAbility() != null) && bPlayer.getAbility().equals(NAME)) {
					redirectLocation = targetLocation;
					// Redirection !
					targetLocation = EntityTools.getTargetedLocation((Player) target, RANGE);
					LivingEntity targetRedirect = EntityTools.getTargetedEntity((Player) target, RANGE);
					if(targetRedirect != null) {
						targetLocation = targetRedirect.getEyeLocation();
					}
				}
			}
		}
	}

	@Override
	public void stop() {
		
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
		if(isState(BendingAbilityState.PREPARING)) {
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.SPELL, loc, 1, 0, 0, 0, 0);
			if (System.currentTimeMillis() > (startedTime + warmup)) {
				setState(BendingAbilityState.PREPARED);
			}
			if (!this.player.isSneaking()) {
				remove();
			}
		} else if (isState(BendingAbilityState.PREPARED)) {
			if (player.isSneaking()) {
				Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
				player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
			} else {
				strike();
				remove();
			}
		}
	}

	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		DamageTools.damageEntity(bender, entity, this, damage);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = NebularChain.NAME, affinity = BendingAffinity.SWORD)
public class NebularChain extends BendingActiveAbility {
	public final static String NAME = "NebularChain";
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 8000;
	
	@ConfigurationParameter("Range")
	public static double RANGE = 17;
	
	@ConfigurationParameter("Push")
	public static double PUSH = 3.0;

	private double range;

	private long cooldown;

	public NebularChain(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.MASTER_BLANKPOINTPUSH_POISONNEDARTRANGE_NEBULARCHAINRANGE)) {
			this.range += 1;
		}
		if(bender.hasPerk(BendingPerk.MASTER_BLANKPOINTDAMAGE_PARASTICKCD_NEBULARRANGE)) {
			this.range += 1;
		}
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_STRAIGHTSHOTCD_C4RADIUS_NEBULARCD)) {
			this.cooldown -= 500;
		}
		if(bender.hasPerk(BendingPerk.MASTER_EXPLOSIVESHOTRADIUSDAMAGE_SMOKEBOMBPARASTICKDAMAGE_NEBULARCD)) {
			this.cooldown -= 500;
		}
	}

	@Override
	public boolean swing() {
		LivingEntity target = EntityTools.getTargetedEntity(player, range);
		if(target != null && affect(target)) {
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 5, 1);
			bender.cooldown(this, cooldown);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		
		return false;
	}

	@Override
	public void progress() {
		
	}
	
	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}
	
	private boolean affect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		Vector direction = entity.getEyeLocation().subtract(player.getEyeLocation()).toVector();
		double distance = direction.length();
		for(double i=0; i < distance ; i = i + 0.01) {
			Location location = player.getEyeLocation().clone().add(direction.clone().normalize().multiply(i));
			location.getWorld().spawnParticle(Particle.SPELL_WITCH, location, 1, 0, 0, 0, 0);
		}
		
		Location location = player.getEyeLocation().add(player.getEyeLocation().getDirection());
	    location.setPitch(entity.getEyeLocation().getPitch());
	    location.setYaw(entity.getEyeLocation().getYaw());
	    entity.teleport(location);
		return true;
	}

}

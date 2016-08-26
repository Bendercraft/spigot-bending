package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Concussion.NAME, affinity = BendingAffinity.SWORD)
public class Concussion extends BendingActiveAbility {
	public final static String NAME = "Concussion";
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;
	
	@ConfigurationParameter("Range")
	public static long RANGE = 3;
	
	@ConfigurationParameter("Duration")
	public static long DURATION = 2000;
	
	private LivingEntity target;

	public Concussion(RegisteredAbility register, Player player) {
		super(register, player);
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if(player.getInventory().getItemInOffHand() == null || player.getInventory().getItemInOffHand().getType() != Material.SHIELD) {
			return false;
		}
		return true;
	}

	@Override
	public boolean sneak() {
		target = EntityTools.getTargetedEntity(player, RANGE);
		if(target != null) {
			setState(BendingAbilityState.PROGRESSING);
			bender.cooldown(this, COOLDOWN);
		}
		return false;
	}

	@Override
	public void progress() {
		if(target != null) {
			if(startedTime + DURATION < System.currentTimeMillis()) {
				remove();
				return;
			}
			affect(target);
		}
	}
	
	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}
	
	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		
		target.setVelocity(new Vector(0, 0, 0));
		target.getWorld().spawnParticle(Particle.CLOUD, target.getEyeLocation().add(target.getEyeLocation().getDirection().multiply(0.5)), 1, 0, 0, 0, 0);
	}
	
	public static Concussion getTarget(LivingEntity entity) {
		for(BendingAbility ability : AbilityManager.getManager().getInstances(NAME).values()) {
			Concussion concussion = (Concussion) ability;
			if(concussion.target == entity) {
				return concussion;
			}
		}
		return null;
	}

}

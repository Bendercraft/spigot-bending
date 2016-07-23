package net.bendercraft.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Slice.NAME, affinity = BendingAffinity.SWORD, canBeUsedWithTools = true)
public class Slice extends BendingActiveAbility {
	public final static String NAME = "Slice";
	
	@ConfigurationParameter("Damage-Direct")
	private static int DAMAGE_DIRECT = 4;
	
	@ConfigurationParameter("Damage-Bleed")
	private static int DAMAGE_BLEED = 1;
	
	@ConfigurationParameter("Interval-Tick-Bleed")
	private static long INTERVAl_TICK_BLEED = 1000;
	
	@ConfigurationParameter("Max-Tick-Bleed")
	private static int MAX_TICK_BLEED = 6;
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 15000;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 3;
	
	private LivingEntity target;
	private int ticks;

	public Slice(RegisteredAbility register, Player player) {
		super(register, player);
		ticks = 0;
	}

	@Override
	public boolean swing() {
		target = EntityTools.getTargetedEntity(player, RANGE);
		if(target != null) {
			player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize()), 1);
			bender.cooldown(this, COOLDOWN);
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		
		return false;
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		if (instances.containsKey(player)) {
			return false;
		}

		return true;
	}

	@Override
	public void progress() {
		if(getState() != BendingAbilityState.PROGRESSING) {
			remove();
			return;
		}
		int current = (int) Math.ceil((System.currentTimeMillis() - startedTime) / INTERVAl_TICK_BLEED);
		while(ticks < current) {
			if(ticks > MAX_TICK_BLEED) {
				remove();
				return;
			}
			DamageTools.damageEntity(bender, target, this, DAMAGE_BLEED, true, 0, 0.0f, true);
			ticks++;
		}
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		
		if(target != null && (target.isDead() || !target.isValid())) {
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

}

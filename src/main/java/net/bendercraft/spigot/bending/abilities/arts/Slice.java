package net.bendercraft.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
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
	private static long COOLDOWN = 6000;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 3;
	
	private LivingEntity target;
	private int ticks;

	private long cooldown;
	private int damageBleed;
	private int damageDirect;
	private int maxTick;
	private long interval;

	public Slice(RegisteredAbility register, Player player) {
		super(register, player);
		ticks = 0;
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_AIMCD_C4CD_SLICE_CD)) {
			this.cooldown -= 500;
		}
		
		this.damageDirect = DAMAGE_DIRECT;
		this.damageBleed = DAMAGE_BLEED;
		if(bender.hasPerk(BendingPerk.MASTER_STRAIGHTSHOTDAMAGE_SMOKEBOMBDURATION_SLICEBLEEDDAMAGE)) {
			this.damageBleed += 1;
			this.damageDirect -= 2;
		}
		if(bender.hasPerk(BendingPerk.MASTER_STRAIGHTSHOTRANGE_SMOKEBOMBRADIUS_SLICEDIRECTDAMAGE)) {
			this.damageDirect += 1;
		}
		
		this.maxTick = MAX_TICK_BLEED;
		if(bender.hasPerk(BendingPerk.MASTER_EXPLOSIVESHOTCD_POISONNEDDARTCD_SLICEDURATION)) {
			this.maxTick += 2;
		}
		
		this.interval = INTERVAl_TICK_BLEED;
		if(bender.hasPerk(BendingPerk.MASTER_AIMRANGE_VITALPOINTCHI_SLICEINTERVAL)) {
			this.interval *= 0.8;
		}
	}

	@Override
	public boolean swing() {
		if(isState(BendingAbilityState.START)) {
			target = EntityTools.getTargetedEntity(player, RANGE);
			if(target != null && affectDirect(target)) {
				player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize()), 1);
				bender.cooldown(this, cooldown);
				setState(BendingAbilityState.PROGRESSING);
			}
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
		if(!isState(BendingAbilityState.PROGRESSING)) {
			remove();
			return;
		}
		int current = (int) Math.ceil((System.currentTimeMillis() - startedTime) / interval);
		while(ticks < current) {
			if(ticks > maxTick) {
				remove();
				return;
			}
			affect(target);
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
	
	private boolean affectDirect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		DamageTools.damageEntity(bender, target, this, damageDirect, true, 0, 0.0f, true);
		return true;
	}

	private void affect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		DamageTools.damageEntity(bender, target, this, damageBleed, true, 0, 0.0f, true);
	}
}

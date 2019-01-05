package net.bendercraft.spigot.bending.abilities.arts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = SmokeBomb.NAME, affinity = BendingAffinity.CHI, shift=false)
public class SmokeBomb extends BendingActiveAbility {
	public final static String NAME = "SmokeBomb";

	@ConfigurationParameter("Radius")
	public static int RADIUS = 7;

	@ConfigurationParameter("Duration")
	public static int DURATION = 10;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 6000;

	@ConfigurationParameter("Sound-Radius")
	public static float SOUND_RADIUS = 20;
	
	@ConfigurationParameter("Parastick-Damage")
	public static double PARASTICK_DAMAGE = 4;

	private static Integer ID = Integer.MIN_VALUE;

	private static PotionEffect blindnessBomber = new PotionEffect(PotionEffectType.BLINDNESS, 20, 2);

	private PotionEffect blindnessTarget;
	private List<LivingEntity> targets;
	private Location origin;
	private int ticksRemaining;
	private List<Location> locs;

	private Integer id;

	private long cooldown;
	private int radius;

	public SmokeBomb(RegisteredAbility register, Player player) {
		super(register, player);
		
		int duration = DURATION;
		if(bender.hasPerk(BendingPerk.MASTER_STRAIGHTSHOTDAMAGE_SMOKEBOMBDURATION_SLICEBLEEDDAMAGE)) {
			duration += 2;
		}
		
		this.radius = RADIUS;
		if(bender.hasPerk(BendingPerk.MASTER_STRAIGHTSHOTRANGE_SMOKEBOMBRADIUS_SLICEDIRECTDAMAGE)) {
			this.radius += 1;
		}

		this.origin = player.getLocation();
		this.id = ID++;
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_BLANKPOINTCD_SMOKEBOMBCD_DASHSTUN)) {
			this.cooldown -= 500;
		}
		this.ticksRemaining = duration * 20;
		this.locs = new ArrayList<>();
		this.targets = new ArrayList<>();

		final double maxParticulesRadius = RADIUS;
		double minParticlesRadius = maxParticulesRadius - 2.0D;
		if (minParticlesRadius < 0) {
			minParticlesRadius = 0;
		}
		this.locs = BlockTools.getLocationBetweenRanges(this.origin, minParticlesRadius, maxParticulesRadius);
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.PREPARED) {
			return true;
		}

		if (!getState().equals(BendingAbilityState.START)) {
			return false;
		}

		setState(BendingAbilityState.PREPARED);

		this.origin.getWorld().playSound(this.origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST, (SOUND_RADIUS / 16.0f), 1.1f);
		this.player.addPotionEffect(blindnessBomber);

		this.bender.cooldown(SmokeBomb.NAME, this.cooldown);
		
		if(ParaStick.hasParaStick(player)) {
			ParaStick stick = ParaStick.getParaStick(player);
			stick.consume();
			double damage = PARASTICK_DAMAGE;
			if(stick.isEnhanced()) {
				damage *= 1.5;
			}
			
			for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.origin, radius)) {
				if(bender.hasPerk(BendingPerk.MASTER_EXPLOSIVESHOTRADIUSDAMAGE_SMOKEBOMBPARASTICKDAMAGE_NEBULARCD)) {
					if(entity == bender.getPlayer()) {
						continue;
					}
				}
				DamageTools.damageEntity(bender, entity, this, damage);
			}
		}

		if (getState() == BendingAbilityState.PREPARED) {
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public void progress() {
		if (getState() != BendingAbilityState.PROGRESSING) {
			remove();
			return;
		}
		List<LivingEntity> newTargets = EntityTools.getLivingEntitiesAroundPoint(this.origin, radius);

		this.blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS, this.ticksRemaining, 2);

		for (LivingEntity targ : this.targets) {
			if (!newTargets.contains(targ)) {
				if (targ != player) {
					targ.removePotionEffect(PotionEffectType.BLINDNESS);
				} else {
					targ.removePotionEffect(PotionEffectType.INVISIBILITY);
				}
			}
		}
		this.targets.clear();

		for (LivingEntity targ : newTargets) {
			if(affect(targ)) {
				this.targets.add(targ);
			}
		}

		if ((this.ticksRemaining % 20) == 0) {
			for (Location loc : this.locs) {
				loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, 0, null, true);
			}
		}

		this.ticksRemaining--;
		if (this.ticksRemaining <= 0) {
			remove();
		}
	}

	@Override
	protected long getMaxMillis() {
		return (DURATION * 1000) + 1;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.holdsTool(player)) {
			return false;
		}
		return true;
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
		if (entity != player) {
			entity.addPotionEffect(this.blindnessTarget);
		} else {
			PotionEffect invisibilityLauncher = new PotionEffect(PotionEffectType.INVISIBILITY, this.ticksRemaining, 1);
			entity.addPotionEffect(invisibilityLauncher);
		}
		return true;
	}
}

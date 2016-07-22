package net.bendercraft.spigot.bending.abilities.arts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = SmokeBomb.NAME, affinity = BendingAffinity.CHI, shift=false)
public class SmokeBomb extends BendingActiveAbility {
	public final static String NAME = "SmokeBomb";

	@ConfigurationParameter("Radius")
	public static int RADIUS = 5;

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

	public SmokeBomb(RegisteredAbility register, Player player) {
		super(register, player);

		this.origin = player.getLocation();
		this.id = ID++;
		this.cooldown = COOLDOWN;
		this.ticksRemaining = DURATION * 20;
		this.locs = new ArrayList<Location>();
		this.targets = new ArrayList<LivingEntity>();

		List<Block> blocks = BlockTools.getBlocksAroundPoint(this.origin, RADIUS);
		for (Block block : blocks) {
			this.locs.add(block.getLocation());
		}
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

		this.origin.getWorld().playSound(this.origin, Sound.ENTITY_FIREWORK_BLAST, (SOUND_RADIUS / 16.0f), 1.1f);
		this.player.addPotionEffect(blindnessBomber);

		this.bender.cooldown(SmokeBomb.NAME, this.cooldown);
		
		if(ParaStick.hasParaStick(player)) {
			ParaStick stick = ParaStick.getParaStick(player);
			stick.consume();
			
			for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.origin, RADIUS)) {
				DamageTools.damageEntity(bender, entity, PARASTICK_DAMAGE);
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
		List<LivingEntity> newTargets = EntityTools.getLivingEntitiesAroundPoint(this.origin, RADIUS);

		this.blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS, this.ticksRemaining, 2);

		for (LivingEntity targ : this.targets) {
			if (!newTargets.contains(targ)) {
				if (targ.getEntityId() != this.player.getEntityId()) {
					targ.removePotionEffect(PotionEffectType.BLINDNESS);
				}
				else {
					targ.removePotionEffect(PotionEffectType.INVISIBILITY);
				}
			}
		}
		this.targets.clear();

		for (LivingEntity targ : newTargets) {
			if (targ.getEntityId() != this.player.getEntityId()) {
				targ.addPotionEffect(this.blindnessTarget);
			} else {
				PotionEffect invisibilityLauncher = new PotionEffect(PotionEffectType.INVISIBILITY, this.ticksRemaining, 1);
				targ.addPotionEffect(invisibilityLauncher);
			}
			this.targets.add(targ);
		}

		if ((this.ticksRemaining % 16) == 0) {
			for (Location loc : this.locs) {
				loc.getWorld().playEffect(loc, Effect.SMOKE, 1, 15);
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
}

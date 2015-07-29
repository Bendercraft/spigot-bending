package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.ArrayList;
import java.util.List;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SmokeBomb extends Ability {
	
	public static final int RADIUS = Bending.plugin.configuration.getIntAttribute(configPrefix + "Chi.SmokeBomb.Radius");
	public static final int DURATION = Bending.plugin.configuration.getIntAttribute(configPrefix + "Chi.SmokeBomb.Duration");
	public static final long COOLDOWN = Bending.plugin.configuration.getIntAttribute(configPrefix + "Chi.SmokeBomb.Cooldown");
	
	private static Integer ID = Integer.MIN_VALUE;

	private static PotionEffect blindnessBomber = new PotionEffect(
			PotionEffectType.BLINDNESS, 20, 2);

	private PotionEffect blindnessTarget;
	private List<LivingEntity> targets;
	private Location origin;
	private int ticksRemaining;
	private List<Location> locs;
	
	private Integer id;

	public SmokeBomb(Player player) {
		super(player, null);
		
		if (state.equals(AbilityState.CannotStart)) {
			return;
		}
		
		this.origin = player.getLocation();
		id = ID++;
		
		this.ticksRemaining = DURATION * 20;
		locs = new ArrayList<Location>();
		targets = new ArrayList<LivingEntity>();

		List<Block> blocks = BlockTools.getBlocksAroundPoint(origin, RADIUS);
		for (Block block : blocks) {
			locs.add(block.getLocation());
		}		
	}
	
	@Override
	public boolean swing() {
		if (state == AbilityState.CannotStart || state == AbilityState.Started) {
			return true;
		}
		
		setState(AbilityState.Started);
		
		origin.getWorld().playSound(origin, Sound.FIREWORK_BLAST, 10, 1);
		player.addPotionEffect(blindnessBomber);
		
		bender.cooldown(Abilities.SmokeBomb, COOLDOWN);
		AbilityManager.getManager().addInstance(this);
		
		if (state == AbilityState.Started) {
			setState(AbilityState.Progressing);
		}
		return false;
	}

	@Override
	public boolean progress() {
		
		Bending.plugin.getLogger().info("Before : " + state);
		if (state != AbilityState.Progressing) {
			return false;
		}
		List<LivingEntity> newTargets = EntityTools.getLivingEntitiesAroundPoint(
				origin, RADIUS);

		blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS,
				ticksRemaining, 2);
		
		for (LivingEntity targ : targets) {
			if (!newTargets.contains(targ)) {
				if (targ.getEntityId() != player.getEntityId()) {
					targ.removePotionEffect(PotionEffectType.BLINDNESS);
				}
				else {
					targ.removePotionEffect(PotionEffectType.INVISIBILITY);
				}	
			}
		}
		
		targets.clear();
		
		for (LivingEntity targ : newTargets) {
			if (targ.getEntityId() != player.getEntityId()) {
				targ.addPotionEffect(blindnessTarget);
			}
			else {
				PotionEffect invisibilityLauncher = new PotionEffect(PotionEffectType.INVISIBILITY, ticksRemaining,1);
				targ.addPotionEffect(invisibilityLauncher);
			}
			targets.add(targ);
		}
		
		if (ticksRemaining % 16 == 0) {
			for (Location loc : locs) {
				loc.getWorld().playEffect(loc, Effect.SMOKE, 1, 15);
			}
		}

		ticksRemaining--;
		Bending.plugin.getLogger().info("Ticks : " + ticksRemaining);
		if (ticksRemaining <= 0) {
			setState(AbilityState.Ended);
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public void remove() {
		AbilityManager.getManager().getInstances(Abilities.SmokeBomb).remove(id);
		super.remove();
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.SmokeBomb;
	}

	@Override
	public Object getIdentifier() {
		return id;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if (EntityTools.isWeapon(player.getItemInHand().getType())) {
			return false;
		}
		return true;
	}
}

package net.avatar.realms.spigot.bending.abilities.chi;

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

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Smoke Bomb", element=BendingType.ChiBlocker)
public class SmokeBomb extends Ability {
	
	@ConfigurationParameter("Radius")
	public static int RADIUS = 5;
	
	@ConfigurationParameter("Duration")
	public static int DURATION = 10;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 6000;
	
	@ConfigurationParameter("Sound-Radius")
	public static float SOUND_RADIUS = 20;
	
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
		
		if (this.state.equals(AbilityState.CannotStart)) {
			return;
		}
		
		this.origin = player.getLocation();
		this.id = ID++;
		
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
		if ((this.state == AbilityState.CannotStart) || (this.state == AbilityState.Started)) {
			return true;
		}
		
		setState(AbilityState.Started);
		
		this.origin.getWorld().playSound(this.origin, Sound.FIREWORK_BLAST,(SOUND_RADIUS/16.0f), 1.1f);
		this.player.addPotionEffect(blindnessBomber);
		
		this.bender.cooldown(Abilities.SmokeBomb, COOLDOWN);
		AbilityManager.getManager().addInstance(this);
		
		if (this.state == AbilityState.Started) {
			setState(AbilityState.Progressing);
		}
		return false;
	}

	@Override
	public boolean progress() {
		
		Bending.plugin.getLogger().info("Before : " + this.state);
		if (this.state != AbilityState.Progressing) {
			return false;
		}
		List<LivingEntity> newTargets = EntityTools.getLivingEntitiesAroundPoint(
				this.origin, RADIUS);

		this.blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS,
				this.ticksRemaining, 2);
		
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
			}
			else {
				PotionEffect invisibilityLauncher = new PotionEffect(PotionEffectType.INVISIBILITY, this.ticksRemaining,1);
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
		Bending.plugin.getLogger().info("Ticks : " + this.ticksRemaining);
		if (this.ticksRemaining <= 0) {
			setState(AbilityState.Ended);
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.SmokeBomb;
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
		
		if (EntityTools.isWeapon(this.player.getItemInHand().getType())) {
			return false;
		}
		return true;
	}
}

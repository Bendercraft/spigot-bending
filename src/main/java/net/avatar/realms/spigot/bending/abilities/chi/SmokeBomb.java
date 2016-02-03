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

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = "Smoke Bomb", bind = BendingAbilities.SmokeBomb, element = BendingElement.ChiBlocker)
public class SmokeBomb extends BendingActiveAbility {

	@ConfigurationParameter("Radius")
	public static int RADIUS = 5;

	@ConfigurationParameter("Duration")
	public static int DURATION = 10;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 6000;

	@ConfigurationParameter("Sound-Radius")
	public static float SOUND_RADIUS = 20;

	private static Integer ID = Integer.MIN_VALUE;

	private static PotionEffect blindnessBomber = new PotionEffect(PotionEffectType.BLINDNESS, 20, 2);

	private PotionEffect blindnessTarget;
	private List<LivingEntity> targets;
	private Location origin;
	private int ticksRemaining;
	private List<Location> locs;

	private boolean blind = false;
	private boolean invisibility = false;

	private Integer id;

	private long cooldown;

	public SmokeBomb(Player player) {
		super(player);

		this.origin = player.getLocation();
		this.id = ID++;
		this.cooldown = COOLDOWN;
		this.ticksRemaining = DURATION * 20;
		if(this.bender.hasPath(BendingPath.Seeker)) {
			this.ticksRemaining *= 1.2;
		}
		if(this.bender.hasPath(BendingPath.Restless)) {
			this.ticksRemaining *= 0.9;
			this.cooldown *= 1.2; 
		}
		this.locs = new ArrayList<Location>();
		this.targets = new ArrayList<LivingEntity>();

		List<Block> blocks = BlockTools.getBlocksAroundPoint(this.origin, RADIUS);
		for (Block block : blocks) {
			this.locs.add(block.getLocation());
		}
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.Prepared) {
			return true;
		}

		if (!getState().equals(BendingAbilityState.Start)) {
			return false;
		}

		setState(BendingAbilityState.Prepared);

		this.origin.getWorld().playSound(this.origin, Sound.FIREWORK_BLAST, (SOUND_RADIUS / 16.0f), 1.1f);
		this.player.addPotionEffect(blindnessBomber);

		int combo = ComboPoints.getComboPointAmount(this.player);
		if (combo >= 1) {
			this.blind = true;
			if (combo >= 2) {
				this.invisibility = true;
				ComboPoints.consume(this.player, 1);
			}
		}

		this.bender.cooldown(BendingAbilities.SmokeBomb, this.cooldown);

		if (getState() == BendingAbilityState.Prepared) {
			setState(BendingAbilityState.Progressing);
		}
		return false;
	}

	@Override
	public void progress() {
		if (getState() != BendingAbilityState.Progressing) {
			remove();
			return;
		}
		List<LivingEntity> newTargets = EntityTools.getLivingEntitiesAroundPoint(this.origin, RADIUS);

		this.blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS, this.ticksRemaining, 2);

		if (this.blind || this.invisibility) {
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
		}

		this.targets.clear();

		if (this.blind || this.invisibility) {
			for (LivingEntity targ : newTargets) {
				if (targ.getEntityId() != this.player.getEntityId()) {
					targ.addPotionEffect(this.blindnessTarget);
				}
				else if (this.invisibility) {
					PotionEffect invisibilityLauncher = new PotionEffect(PotionEffectType.INVISIBILITY, this.ticksRemaining, 1);
					targ.addPotionEffect(invisibilityLauncher);
				}
				this.targets.add(targ);
			}
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

		if (EntityTools.isTool(this.player.getItemInHand().getType())) {
			return false;
		}
		return true;
	}

	@Override
	public void stop() {
		
	}
}

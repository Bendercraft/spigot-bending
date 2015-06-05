package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SmokeBomb implements IAbility {
	public static int radius = ConfigManager.smokeRadius;
	public static int duration = ConfigManager.smokeDuration;

	private static List<SmokeBomb> instances = new ArrayList<SmokeBomb>();

	private static PotionEffect blindnessBomber = new PotionEffect(
			PotionEffectType.BLINDNESS, 20, 2);

	private Player player;
	private PotionEffect blindnessTarget;
	private List<LivingEntity> targets;
	private Location origin;
	private int ticksRemaining;
	private List<Location> locs;
	private IAbility parent;

	public SmokeBomb(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		this.origin = player.getLocation();
		
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.SmokeBomb,
				origin)) {
			return;
		}

		if (!bPlayer.isOnCooldown(Abilities.SmokeBomb)) {
			
			this.ticksRemaining = duration * 20;
			locs = new ArrayList<Location>();
			targets = new ArrayList<LivingEntity>();
			instances.add(this);

			List<Block> blocks = BlockTools
					.getBlocksAroundPoint(origin, radius);
			for (Block block : blocks) {
				locs.add(block.getLocation());
			}
			
			origin.getWorld().playSound(origin, Sound.FIREWORK_BLAST, 10, 1);
			player.addPotionEffect(blindnessBomber);
			bPlayer.cooldown(Abilities.SmokeBomb);
		}
	}

	public void progress() {
		List<LivingEntity> newTargets = EntityTools.getLivingEntitiesAroundPoint(
				origin, radius);

		blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS,
				ticksRemaining, 2);
		
		for (LivingEntity targ : targets) {
			if (!newTargets.contains(targ)) {
				if (targ.getEntityId() != player.getEntityId()) {
					targ.removePotionEffect(PotionEffectType.BLINDNESS);
				}
				else {
					targ.removePotionEffect(PotionEffectType.INVISIBILITY);
					if (targ instanceof Player) {
						//Bending.plugin.ghostManager.addGhost((Player)targ);
					}	
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
	}

	public static void progressAll() {
		List<SmokeBomb> toRemove = new LinkedList<SmokeBomb>();
		for (SmokeBomb bomb : instances) {
			bomb.progress();
			if (bomb.ticksRemaining <= 0) {
				toRemove.add(bomb);
			}
		}
		
		for (SmokeBomb toRem : toRemove) {
			instances.remove(toRem);
		}
		toRemove.clear();
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

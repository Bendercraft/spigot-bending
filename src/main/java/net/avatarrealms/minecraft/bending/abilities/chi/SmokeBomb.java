package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
		
		if (Tools.isRegionProtectedFromBuild(player, Abilities.SmokeBomb,
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
				locs.add(block.getLocation().clone());
			}
			List<Entity> entitiesAround = EntityTools.getEntitiesAroundPoint(origin,radius+10);
			for (Entity ent : entitiesAround) {
				if (ent instanceof Player) {
					((Player)ent).playSound(origin,Sound.FIREWORK_BLAST,10,1);
					//((Player)ent).playSound(origin,Sound.FIREWORK_BLAST2,10,1);
				}
			}
			player.addPotionEffect(blindnessBomber);
			bPlayer.cooldown(Abilities.SmokeBomb);
			bPlayer.earnXP(BendingType.ChiBlocker,this);
		}
	}

	public void progress() {
		List<LivingEntity> newTargets = new LinkedList<LivingEntity>();
		List<Entity> entitiesAround = EntityTools.getEntitiesAroundPoint(
				origin, radius);
		for (Entity e : entitiesAround) {
			if (e instanceof LivingEntity) {
				newTargets.add((LivingEntity) e);
			}
		}
		entitiesAround.clear();
		blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS,
				ticksRemaining, 2);
		
		for (LivingEntity targ : targets) {
			if (!newTargets.contains(targ)) {
				targ.removePotionEffect(PotionEffectType.BLINDNESS);
			}
		}
		
		targets.clear();
		
		for (LivingEntity targ : newTargets) {
			if (targ.getEntityId() != player.getEntityId()) {
				targ.addPotionEffect(blindnessTarget);
				targets.add(targ);
			}
		}
		
		if (ticksRemaining % 10 == 0) {
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
	public int getBaseExperience() {
		return 4;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

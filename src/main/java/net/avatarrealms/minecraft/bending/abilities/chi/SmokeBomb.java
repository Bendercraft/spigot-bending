package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.ArrayList;
import java.util.List;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SmokeBomb {
	public static int radius = ConfigManager.smokeRadius;
	public static int duration = ConfigManager.smokeDuration;

	private static List<SmokeBomb> instances = new ArrayList<SmokeBomb>();

	private static PotionEffect blindnessBomber = new PotionEffect(
			PotionEffectType.BLINDNESS, 20, 2);
	/*
	 * private static Type fwType = Type.BURST; private static Color fwColor =
	 * Color.BLACK;
	 */

	private Player bomber;
	private PotionEffect blindnessTarget;
	private List<LivingEntity> targets;
	private Location origin;
	private int ticksRemaining;
	private List<Location> locs;

	public SmokeBomb(Player player) {
		this.bomber = player;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(bomber);

		if (!bPlayer.isOnCooldown(Abilities.SmokeBomb)) {
			this.origin = bomber.getLocation();
			this.ticksRemaining = duration * 20;
			locs = new ArrayList<Location>();
			/*
			 * Firework fw = (Firework)
			 * bomber.getWorld().spawnEntity(origin,EntityType.FIREWORK);
			 * FireworkMeta fwm = fw.getFireworkMeta(); FireworkEffect effect =
			 * FireworkEffect.builder(). flicker(true). withColor(fwColor).
			 * with(fwType). trail(true). build(); fwm.addEffect(effect);
			 * fwm.setPower(0); fw.setFireworkMeta(fwm);
			 */
			instances.add(this);

			List<Block> blocks = BlockTools
					.getBlocksAroundPoint(origin, radius);
			for (Block block : blocks) {
				locs.add(block.getLocation().clone());
			}
			bomber.addPotionEffect(blindnessBomber);
			bPlayer.cooldown(Abilities.SmokeBomb);
		}
	}

	public void progress() {
		targets = new ArrayList<LivingEntity>();
		List<Entity> entitiesAround = EntityTools.getEntitiesAroundPoint(
				origin, radius);
		for (Entity e : entitiesAround) {
			if (e instanceof LivingEntity) {
				targets.add((LivingEntity) e);
			}
		}
		entitiesAround.clear();
		blindnessTarget = new PotionEffect(PotionEffectType.BLINDNESS,
				ticksRemaining, 2);
		for (LivingEntity targ : targets) {
			if (targ.getEntityId() != bomber.getEntityId()) {
				targ.addPotionEffect(blindnessTarget);
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
		List<SmokeBomb> toRemove = new ArrayList<SmokeBomb>();
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
}

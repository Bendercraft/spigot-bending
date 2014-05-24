package net.avatarrealms.minecraft.bending.model.water;

import net.avatarrealms.minecraft.bending.business.Tools;
import net.avatarrealms.minecraft.bending.data.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempBlock;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealingWaters {

	private static final double range = ConfigManager.healingWatersRadius;
	private static final long interval = ConfigManager.healingWatersInterval;

	private static long time = 0;

	public static void heal(Server server) {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			for (Player player : server.getOnlinePlayers()) {
				if (Tools.getBendingAbility(player) == Abilities.HealingWaters
						&& Tools.canBend(player, Abilities.HealingWaters)) {
					heal(player);
				}
			}
		}
	}

	private static void heal(Player player) {
		if (inWater(player)) {
			if (player.isSneaking()) {
				Entity entity = Tools.getTargettedEntity(player, range);
				if (entity instanceof LivingEntity && inWater(entity)) {
					giveHPToEntity((LivingEntity) entity);
					if (((entity instanceof Player) ||(entity instanceof Monster)) && (entity.getEntityId() != player.getEntityId())){
						BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
						if (bPlayer != null) {
							bPlayer.earnXP(BendingType.Water);
						}
					}
				}
			} else {
				giveHP(player);
			}
		}
	}

	private static void giveHPToEntity(LivingEntity le) {
		if (!le.isDead() && le.getHealth() < le.getMaxHealth()) {
			applyHealingToEntity(le);
		}
	}
	
	private static void giveHP(Player player) {
		if (!player.isDead() && player.getHealth() < 20) {
			// int hp = player.getHealth();
			// if (hp < 20) {
			// hp++;
			// }
			// player.setHealth(hp);
			applyHealing(player);
		}
	}
	
	

	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		if (Tools.isWater(block) && !TempBlock.isTempBlock(block))
			return true;
		// if (entity.getLocation().getBlock().getType() == Material.WATER
		// || entity.getLocation().getBlock().getType() ==
		// Material.STATIONARY_WATER) &&
		// return true;
		return false;
	}

	private static void applyHealing(Player player) {
		if (!Tools.isRegionProtectedFromBuild(player, Abilities.HealingWaters,
				player.getLocation()))
			player.addPotionEffect(new PotionEffect(
					PotionEffectType.REGENERATION, 70, 1));
	}
	
	private static void applyHealingToEntity(LivingEntity le) {
		le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
	}

	public static String getDescription() {
		return "To use, the bender must be at least partially submerged in water. "
				+ "If the user is not sneaking, this ability will automatically begin "
				+ "working provided the user has it selected. If the user is sneaking, "
				+ "he/she is channeling the healing to their target in front of them. "
				+ "In order for this channel to be successful, the user and the target must "
				+ "be at least partially submerged in water.";
	}
}

package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class HealingWaters implements IAbility {

	private static final double range = ConfigManager.healingWatersRadius;
	private static Map<Player, HealingWaters> instances = new HashMap<Player, HealingWaters>();
	private Player healer;
	private BendingPlayer bPlayer;
	private long time = 0;
	private IAbility parent;
	private LivingEntity target;

	public HealingWaters (final Player player) {
		if (instances.containsKey(player)) {
			return;
		}
		if (!inWater(player) && !(isWaterPotion(player.getItemInHand()))) {
			return;
		}
		this.healer = player;
		this.bPlayer = BendingPlayer.getBendingPlayer(this.healer);
		this.time = System.currentTimeMillis();
		LivingEntity temp = EntityTools.getTargettedEntity(player, range);
		if (temp == null) {
			temp = this.healer;
		}
		this.target = temp;
		instances.put(player, this);
	}

	public static void progressAll () {
		final LinkedList<Player> toRemove = new LinkedList<Player>();
		boolean keep;
		for (final Player p : instances.keySet()) {
			keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
			}
		}
		for (final Player p : toRemove) {
			instances.remove(p);
		}
	}

	public boolean progress () {
		if (!this.healer.isOnline() || this.healer.isDead()) {
			return false;
		}
		if (!this.healer.isSneaking()) {
			return false;
		}
		if (this.bPlayer.getAbility() != Abilities.HealingWaters) {
			return false;
		}
		LivingEntity entity = EntityTools.getTargettedEntity(this.healer, range);
		if (entity == null) {
			entity = this.healer;
		}
		if (ProtectionManager
				.isRegionProtectedFromBending(this.healer, Abilities.HealingWaters, entity
						.getLocation())) {
			return false;
		}
		if (entity.getEntityId() != this.target.getEntityId()) {
			this.time = System.currentTimeMillis();
		}
		this.target = entity;
		if (isWaterPotion(this.healer.getItemInHand())) {
			giveHPToEntity(this.target);
		}
		else if (inWater(this.healer)) {
			if (!inWater(this.target)) {
				return true;
			}
			giveHPToEntity(this.target);
		}
		else {
			return true;
		}
		final long now = System.currentTimeMillis();
		if ((now - this.time) > 1000) {
			this.target.setFireTicks(0);
			if ((now - this.time) > 3500) {
				for (final PotionEffect pe : this.target.getActivePotionEffects()) {
					if (isNegativePotionEffect(pe.getType())) {
						this.target.removePotionEffect(pe.getType());
					}
				}
			}
		}
		return true;
	}

	public static boolean isNegativePotionEffect (final PotionEffectType peType) {
		if (peType.equals(PotionEffectType.BLINDNESS)) {
			return true;
		}
		if (peType.equals(PotionEffectType.CONFUSION)) {
			return true;
		}
		if (peType.equals(PotionEffectType.HUNGER)) {
			return true;
		}
		if (peType.equals(PotionEffectType.POISON)) {
			return true;
		}
		if (peType.equals(PotionEffectType.SLOW)) {
			return true;
		}
		if (peType.equals(PotionEffectType.WEAKNESS)) {
			return true;
		}
		if (peType.equals(PotionEffectType.WITHER)) {
			return true;
		}
		return false;
	}

	private static boolean isWaterPotion (final ItemStack item) {
		if ((item.getType() == Material.POTION) && (item.getDurability() == 0)) {
			return true;
		}
		return false;
	}

	private static void giveHPToEntity (LivingEntity le) {
		if (!le.isDead()) {
			return;
		}
		final double current = le.getHealth();
		final double max = le.getMaxHealth();
		if (current < max) {
			applyHealingToEntity(le);
		}
	}

	private static boolean inWater (final Entity entity) {
		final Block block = entity.getLocation().getBlock();
		if (BlockTools.isWater(block) && !TempBlock.isTempBlock(block)) {
			return true;
		}
		return false;
	}

	private static void applyHealingToEntity (final LivingEntity le) {
		le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
	}

	public static void removeAll () {
		instances.clear();
	}

	@Override
	public IAbility getParent () {
		return this.parent;
	}
}

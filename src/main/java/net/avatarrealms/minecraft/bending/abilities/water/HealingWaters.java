package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
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
	private static final long interval = ConfigManager.healingWatersInterval;
	private static Map<Player, HealingWaters> instances = new HashMap<Player, HealingWaters>();

	private Player healer;
	private long time = 0;
	private IAbility parent;
	private LivingEntity target;
	
	public HealingWaters(Player player) {
		if (instances.containsKey(player)) {
			return;
		}
		
		if (!inWater(player) && !(isWaterPotion(player.getItemInHand()))){
			return;
		}
		
		this.healer = player;
		this.time = System.currentTimeMillis();
		
		LivingEntity temp = EntityTools.getTargettedEntity(player, range);
		if (temp == null) {
			temp = healer;
		}
		
		target = temp;
		
		instances.put(player, this);
	}
	
	public static void progressAll() {
		LinkedList<Player> toRemove = new LinkedList<Player>();
		boolean keep;
		for (Player p : instances.keySet()) {
			keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
			}
		}
	
		for (Player p : toRemove) {
			instances.remove(p);
		}
	}
	
	public boolean progress() {
		if (!healer.isOnline() || healer.isDead()) {
			return false;
		}
		
		if (healer.isSneaking()) {
			return false;
		}
		
		LivingEntity entity = EntityTools.getTargettedEntity(healer, range);
		if (entity == null) {
			entity = healer;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(healer, Abilities.HealingWaters, entity.getLocation())) {
			return false;
		}
		
		if (entity.getEntityId() != target.getEntityId()) {
			time = System.currentTimeMillis();
		}
		target = entity;
		
		
		
		if (isWaterPotion(healer.getItemInHand())) {
			giveHPToEntity(target);
		}
		else if (inWater(healer)){
			if (!inWater(target)) {
				return true;
			}
			giveHPToEntity(target);
		}
		else {
			return true;
		}
		if (System.currentTimeMillis() - time > 2500) {
			for (PotionEffect pe : target.getActivePotionEffects()) {
				if (isNegativePotionEffect(pe.getType())) {
					target.removePotionEffect(pe.getType());
				}
			}
		}
		return true;
	}
	
	public static boolean isNegativePotionEffect(PotionEffectType peType) {
		if (peType == PotionEffectType.BLINDNESS) {
			return true;
		}
		if (peType == PotionEffectType.CONFUSION) {
			return true;
		}
		if (peType == PotionEffectType.HUNGER) {
			return true;
		}
		if (peType == PotionEffectType.POISON) {
			return true;
		}
		if (peType == PotionEffectType.SLOW) {
			return true;
		}
		if (peType == PotionEffectType.WEAKNESS) {
			return true;
		}
		if (peType == PotionEffectType.WITHER) {
			return true;
		}		
		return false;
	}
	
	private static boolean isWaterPotion(ItemStack item) {
		if (item.getType() == Material.POTION && item.getDurability() == 0) {
			return true;
		}
		return false;
	}

	private static void giveHPToEntity(LivingEntity le) {
		if (!le.isDead() && le.getHealth() < le.getMaxHealth()) {
			applyHealingToEntity(le);
		}
	}

	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		if (BlockTools.isWater(block) && !TempBlock.isTempBlock(block)) {
			return true;
		}			
		return false;
	}
	
	private static void applyHealingToEntity(LivingEntity le) {
		le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
	}
	
	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

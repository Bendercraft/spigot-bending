package net.avatar.realms.spigot.bending.abilities.water;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;

@ABendingAbility(name = HealingWaters.NAME, element = BendingElement.WATER, canBeUsedWithTools = true)
public class HealingWaters extends BendingActiveAbility {
	public final static String NAME = "HealingWaters";

	@ConfigurationParameter("Range")
	private static double RANGE = 5.0;

	private long time = 0;
	private LivingEntity target;

	public HealingWaters(RegisteredAbility register, Player player) {
		super(register, player);
		this.time = this.startedTime;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			LivingEntity temp = EntityTools.getTargetedEntity(this.player, RANGE);
			if (temp == null) {
				temp = this.player;
			}
			this.target = temp;
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!this.player.isSneaking()
				|| !this.bender.getAbility().equals(NAME)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		LivingEntity entity = EntityTools.getTargetedEntity(this.player, RANGE);
		if (entity == null) {
			entity = this.player;
		}
		if (ProtectionManager.isEntityProtected(entity) 
				|| ProtectionManager.isLocationProtectedFromBending(this.player, NAME, entity.getLocation())) {
			remove();
			return;
		}

		final long now = System.currentTimeMillis();
		if (entity.getEntityId() != this.target.getEntityId()) {
			this.time = now;
		}

		this.target = entity;
		if (isWaterPotion(this.player.getInventory().getItemInMainHand())) {
			giveHPToEntity(this.target);
		} else if (inWater(this.player)) {
			if (!inWater(this.target)) {
				return;
			}
			giveHPToEntity(this.target);
		} else {
			return;
		}

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
	}

	public static boolean isNegativePotionEffect(final PotionEffectType peType) {
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

	private static boolean isWaterPotion(final ItemStack item) {
		if ((item.getType() == Material.POTION) && (item.getDurability() == 0)) {
			return true;
		}
		return false;
	}

	private static void giveHPToEntity(LivingEntity le) {
		if (le.isDead()) {
			return;
		}
		final double current = le.getHealth();
		final double max = le.getMaxHealth();
		if (current < max) {
			applyHealingToEntity(le);
		}
	}

	private static boolean inWater(final Entity entity) {
		final Block block = entity.getLocation().getBlock();
		if (BlockTools.isWater(block) && !TempBlock.isTempBlock(block)) {
			return true;
		}
		return false;
	}

	private static void applyHealingToEntity(final LivingEntity le) {
		le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (!inWater(this.player) && !(isWaterPotion(this.player.getInventory().getItemInMainHand()))) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return true;
		}
		return !instances.containsKey(this.player);
	}

	@Override
	public void stop() {
		
	}
}

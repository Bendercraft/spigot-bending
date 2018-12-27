package net.bendercraft.spigot.bending.abilities.water;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

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
				|| ProtectionManager.isLocationProtectedFromBending(this.player, register, entity.getLocation())) {
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
		if (item.getType() != Material.POTION || !item.hasItemMeta() || !(item.getItemMeta() instanceof PotionMeta)) {
			return false;
		}
		PotionMeta potionData = (PotionMeta) item.getItemMeta();
		return potionData.getBasePotionData().getType() == PotionType.WATER;
	}

	private static void giveHPToEntity(LivingEntity le) {
		if (le.isDead()) {
			return;
		}
		
		final double current = le.getHealth();
		final double max = le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
		if (current < max) {
			applyHealingToEntity(le);
		}
	}

	private static boolean inWater(final Entity entity) {
		final Block block = entity.getLocation().getBlock();
		if (block.getType() == Material.WATER && !TempBlock.isTempBlock(block)) {
			return true;
		}
		return false;
	}

	private static void applyHealingToEntity(final LivingEntity le) {
		le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
	}
	
	public static HealingWaters hasBuff(Entity entity) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			HealingWaters ability = (HealingWaters) ab;
			if(ability.target != null 
					&& ability.target == entity 
					&& ability.getBender().hasPerk(BendingPerk.WATER_BATTERY)) {
				return ability;
			}
		}
		return null;
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
		if(bender.hasPerk(BendingPerk.WATER_BATTERY)) {
			bender.cooldown(this, 5000);
		}
	}
}

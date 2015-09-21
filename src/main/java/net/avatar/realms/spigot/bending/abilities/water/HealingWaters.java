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

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Healing Waters", element=BendingElement.Water)
public class HealingWaters extends BendingActiveAbility {
	
	@ConfigurationParameter("Range")
	private static double RANGE = 5.0;

	private long time = 0;
	private LivingEntity target;
	
	public HealingWaters (final Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}
		this.time = this.startedTime;
	}

	@Override
	public boolean sneak () {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;
			case CanStart:
				LivingEntity temp = EntityTools.getTargettedEntity(this.player, RANGE);
				if (temp == null) {
					temp = this.player;
				}
				this.target = temp;
				setState(BendingAbilityState.Progressing);
				AbilityManager.getManager().addInstance(this);
				return false;
			case Preparing:
			case Prepared:
			case Progressing:
			case Ended:
			case Removed:
			default:
				return false;
		}
	}
	
	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}
		if (!this.player.isSneaking()) {
			return false;
		}
		if (this.bender.getAbility() != BendingAbilities.HealingWaters) {
			return false;
		}
		LivingEntity entity = EntityTools.getTargettedEntity(this.player, RANGE);
		if (entity == null) {
			entity = this.player;
		}
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.HealingWaters, entity.getLocation())) {
			return false;
		}
		
		final long now = System.currentTimeMillis();
		if (entity.getEntityId() != this.target.getEntityId()) {
			this.time = now;
		}

		this.target = entity;
		if (isWaterPotion(this.player.getItemInHand())) {
			giveHPToEntity(this.target);
		}
		else if (inWater(this.player)) {
			if (!inWater(this.target)) {
				return true;
			}
			giveHPToEntity(this.target);
		} else {
			return true;
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
		if (le.isDead()) {
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
		if (BlockTools.isWater(block) && !BlockTools.isTempBlock(block)) {
			return true;
		}
		return false;
	}
	
	private static void applyHealingToEntity (final LivingEntity le) {
		le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	public BendingAbilities getAbilityType () {
		return BendingAbilities.HealingWaters;
	}
	
	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (!inWater(this.player) && !(isWaterPotion(this.player.getItemInHand()))) {
			return false;
		}
		
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.HealingWaters);
		if (instances ==  null) {
			return true;
		}
		return !instances.containsKey(this.player);
	}
}

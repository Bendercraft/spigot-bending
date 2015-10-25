package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

/**
 * 
 * This ability throws a poisonned dart to straight foward. If the dart hit an
 * entity, this entity gets poisonned. The type of poisonned can change if
 * specifics items are hold in hand.
 *
 */

@ABendingAbility(name = "Poisonned Dart", bind = BendingAbilities.PoisonnedDart, element = BendingElement.ChiBlocker, affinity = BendingAffinity.Inventor)
public class PoisonnedDart extends BendingActiveAbility {

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;

	@ConfigurationParameter("Range")
	private static int RANGE = 20;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 2000;

	private static final ParticleEffect VISUAL = ParticleEffect.VILLAGER_HAPPY;

	private Location origin;
	private Location location;
	private Vector direction;
	private List<PotionEffect> effects;

	public PoisonnedDart(Player player) {
		super(player);
	}

	@Override
	public boolean swing() {
		if (getState().equals(BendingAbilityState.Preparing)) {
			return true;
		}

		if (!getState().equals(BendingAbilityState.Start)) {
			return false;
		}

		this.origin = this.player.getEyeLocation();
		this.location = this.origin.clone();
		this.direction = this.origin.getDirection().normalize();

		setState(BendingAbilityState.Preparing);

		ItemStack is = this.player.getItemInHand();
		this.effects = new LinkedList<PotionEffect>();
		switch (is.getType()) {
			case MILK_BUCKET:
				this.effects = null;
				is.setType(Material.BUCKET);
				is.setAmount(1);
				break;
			case POTION:
				Potion potion = Potion.fromItemStack(is);
				this.effects.addAll(potion.getEffects());
				this.player.getInventory().removeItem(is);
				this.player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
				break;
			case EYE_OF_ENDER:
				this.effects.add(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 1));
				if (is.getAmount() == 1) {
					this.player.getInventory().removeItem(is);
				} else {
					is.setAmount(is.getAmount() - 1);
				}
				break;
			case MUSHROOM_SOUP:
				this.effects.add(new PotionEffect(PotionEffectType.CONFUSION, 20 * 12, 1));
				is.setType(Material.BOWL);
				is.setAmount(1);
				break;
			case SKULL_ITEM:
				@SuppressWarnings("deprecation")
				byte data = is.getData().getData();
				// If this is a wither skull
				if (data == 1) {
					this.effects.add(new PotionEffect(PotionEffectType.WITHER, 20 * 60, 0));
					if (is.getAmount() == 1) {
						this.player.getInventory().removeItem(is);
					} else {
						is.setAmount(is.getAmount() - 1);
					}
				}
				break;
			default:
				this.effects.add(new PotionEffect(PotionEffectType.POISON, 20 * 1, 0));
				break;
		}

		if (this.effects != null && ComboPoints.getComboPointAmount(this.player) >= 3) {
			ComboPoints.consume(this.player, 2);
			List<PotionEffect> newEffects = new LinkedList<PotionEffect>();
			for (PotionEffect effect : this.effects) {
				newEffects.add(new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier() + 1));
			}
			this.effects.clear();
			this.effects = newEffects;
		}

		this.origin.getWorld().playSound(this.origin, Sound.SHOOT_ARROW, 10, 1);
		this.bender.cooldown(BendingAbilities.PoisonnedDart, COOLDOWN);

		return false;
	}

	@Override
	public void progress() {
		if (getState() == BendingAbilityState.Preparing) {
			setState(BendingAbilityState.Progressing);
		}

		if (getState() != BendingAbilityState.Progressing) {
			return;
		}

		if (!this.player.getWorld().equals(this.location.getWorld()) 
				|| this.location.distance(this.origin) > RANGE 
				|| BlockTools.isSolid(this.location.getBlock())) {
			remove();
			return;
		}

		advanceLocation();
		if (!affectAround()) {
			remove();
		}
	}

	private boolean affectAround() {
		if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.PoisonnedDart, this.location)) {
			return false;
		}
		int cptEnt = 0;
		boolean health = areHealthEffects();
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, 2.1)) {
			if (entity.getEntityId() == this.player.getEntityId()) {
				continue;
			}

			if (this.effects == null) {
				for (PotionEffect effect : entity.getActivePotionEffects()) {
					entity.removePotionEffect(effect.getType());
				}
				entity.getActivePotionEffects().clear();
			} else {
				for (PotionEffect effect : this.effects) {
					entity.addPotionEffect(effect);
				}
			}
			if (!health && (this.effects != null)) {
				EntityTools.damageEntity(this.player, entity, DAMAGE);
			}
			cptEnt++;
			break;
		}

		if (cptEnt > 0) {
			return false;
		}
		return true;
	}

	private boolean areHealthEffects() {
		if (this.effects == null) {
			return false;
		}
		LinkedList<PotionEffect> healthEffects = new LinkedList<PotionEffect>();
		for (PotionEffect effect : this.effects) {
			if (effect.getType().equals(PotionEffectType.HEAL) || effect.getType().equals(PotionEffectType.HEALTH_BOOST) || effect.getType().equals(PotionEffectType.REGENERATION)) {
				healthEffects.add(effect);
			}
		}

		return !healthEffects.isEmpty();
	}

	private void advanceLocation() {
		VISUAL.display(0, 0, 0, 1, 1, this.location, 20);
		this.location = this.location.add(this.direction.clone().multiply(1.5));
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.isWeapon(this.player.getItemInHand().getType())) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.PoisonnedDart);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		if (instances.containsKey(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}

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

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

/**
 * 
 * This ability throws a poisonned dart to straight foward.
 * If the darth hit an entity, this entity gets poisonned.
 * The type of poisonned can change if specifics items are hold in hand.
 *
 */

@BendingAbility(name="Poisonned Dart", element=BendingType.ChiBlocker, specialization = BendingSpecializationType.Inventor)
public class PoisonnedDart extends Ability{
	
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
		super(player, null);
		
		if (this.state.equals(AbilityState.CannotStart)) {
			return;
		}
	}
	
	@Override
	public boolean swing() {
		
		if (this.state.equals(AbilityState.CannotStart) || this.state.equals(AbilityState.Started)) {
			return true;
		}
		
		this.origin = this.player.getEyeLocation();
		this.location = this.origin.clone();
		this.direction = this.origin.getDirection().normalize();
		
		setState(AbilityState.Started);
		
		AbilityManager.getManager().addInstance(this);
		
		ItemStack is = this.player.getItemInHand();
		this.effects = new LinkedList<PotionEffect>();
		System.out.println(is.toString());
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
				this.effects.add(new PotionEffect(PotionEffectType.BLINDNESS,20*10,1));
				if (is.getAmount() == 1) {
					this.player.getInventory().removeItem(is);
				}
				else {
					is.setAmount(is.getAmount() - 1);
				}
				break;
			case MUSHROOM_SOUP:
				this.effects.add(new PotionEffect(PotionEffectType.CONFUSION,20*12,1));
				is.setType(Material.BOWL);
				is.setAmount(1);
				break;
			case SKULL_ITEM:
				byte data = is.getData().getData();
				// If this is a wither skull
				if (data == 1) {
					this.effects.add(new PotionEffect(PotionEffectType.WITHER, 20 * 15, 1));
					if (is.getAmount() == 1) {
						this.player.getInventory().removeItem(is);
					}
					else {
						is.setAmount(is.getAmount() - 1);
					}
				}
				break;
			default : 
				this.effects.add(new PotionEffect(PotionEffectType.POISON, 20*1, 0));
				break;
		}

		this.origin.getWorld().playSound(this.origin, Sound.SHOOT_ARROW, 10, 1);
		this.bender.cooldown(Abilities.PoisonnedDart, COOLDOWN);
		
		return false;
	}

	@Override
	public boolean progress() {	
		if (!super.progress()) {
			return false;
		}
		
		if (this.state.isBefore(AbilityState.Started)) {
			return true;
		}
		
		if (this.state == AbilityState.Started) {
			setState(AbilityState.Progressing);
		}
		
		if (this.state != AbilityState.Progressing){
			return true;
		}
		
		if (!this.player.getWorld().equals(this.location.getWorld())) {
			return false;
		}
		if (this.location.distance(this.origin) > RANGE) {
			return false;
		}
		
		if (BlockTools.isSolid(this.location.getBlock())) {
			return false;
		}
		
		advanceLocation();
		if (!affectAround()) {
			return false;
		}
		return true;
	}
	
	private boolean affectAround() {
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.PoisonnedDart, this.location)) {
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
			}
			else {
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
			if (effect.getType().equals(PotionEffectType.HEAL)
					|| effect.getType().equals(PotionEffectType.HEALTH_BOOST)
					|| effect.getType().equals(PotionEffectType.REGENERATION)) {
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
		
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.PoisonnedDart);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}
			
		if (instances.containsKey(this.player)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public Abilities getAbilityType() {
		return Abilities.PoisonnedDart;
	}
	
	@Override
	public void remove() {
		AbilityManager.getManager().getInstances(Abilities.PoisonnedDart).remove(this.player);
		super.remove();
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
}

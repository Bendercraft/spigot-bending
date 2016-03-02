package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = FireBlade.NAME, element = BendingElement.FIRE, shift=false)
public class FireBlade extends BendingActiveAbility {
	public final static String NAME = "FireBlade";
	
	private static String LORE_NAME = "FireBlade";

	private static final Enchantment SHARP = Enchantment.DAMAGE_ALL;
	private static final Enchantment DURA = Enchantment.DURABILITY;
	private static final Enchantment KNOCKBACK = Enchantment.KNOCKBACK;

	@ConfigurationParameter("Sharpness-Level")
	private static int SHARPNESS_LEVEL = 1;

	@ConfigurationParameter("Durability-Level")
	private static int DURABILITY_LEVEL = 3;

	@ConfigurationParameter("Duration")
	private static int DURATION = 40000;

	@ConfigurationParameter("Cooldown-Factor")
	public static float COOLDOWN_FACTOR = 0.75f;

	private ItemStack blade;

	public FireBlade(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			giveFireBlade();
			
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		String abilityName = EntityTools.getBendingAbility(this.player);
		if (this.blade == null 
				|| !isFireBlade(this.player.getItemInHand())
				|| abilityName == null
				|| !abilityName.equals(NAME)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		
	}

	@Override
	public void stop() {
		ItemStack toRemove = null;
		for (ItemStack is : this.player.getInventory().getContents()) {
			if ((is != null) && isFireBlade(is)) {
				toRemove = is;
				break;
			}
		}
		if (toRemove != null) {
			this.player.getInventory().remove(toRemove);
		}
		this.player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
		long now = System.currentTimeMillis();
		long realDuration = now - this.startedTime;
		this.bender.cooldown(NAME, (long) (realDuration * COOLDOWN_FACTOR));
	}

	public static void removeFireBlade(ItemStack is) {
		FireBlade toRemove = null;
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return;
		}
		for (BendingAbility ab : instances.values()) {
			FireBlade blade = (FireBlade) ab;
			if ((blade.getBlade() != null) && isFireBlade(blade.getBlade())) {
				toRemove = blade;
			}
		}

		if (toRemove != null) {
			toRemove.remove();
		}
	}

	public ItemStack getBlade() {
		return this.blade;
	}

	public static boolean isFireBlade(ItemStack is) {
		if (is == null) {
			return false;
		}
		if ((is.getItemMeta() != null) && (is.getItemMeta().getLore() != null) && is.getItemMeta().getLore().contains(LORE_NAME)) {
			return true;
		}
		return false;
	}

	public static boolean isFireBlading(Player p) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return false;
		}
		return instances.containsKey(p);
	}

	public static FireBlade getFireBlading(Player p) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return null;
		}
		if (!instances.containsKey(p)) {
			return null;
		}
		return (FireBlade) instances.get(p);
	}

	public void giveFireBlade() {
		ItemStack fireB = new ItemStack(Material.GOLD_SWORD);
		if (SHARPNESS_LEVEL > 0) {
			fireB.addEnchantment(SHARP, SHARPNESS_LEVEL);
		}
		if (DURABILITY_LEVEL > 0) {
			fireB.addEnchantment(DURA, DURABILITY_LEVEL);
		}
		fireB.addEnchantment(KNOCKBACK, 1);
		List<String> lore = new LinkedList<String>();
		lore.add(LORE_NAME);
		ItemMeta meta = fireB.getItemMeta();
		meta.setLore(lore);
		fireB.setItemMeta(meta);

		int slot = this.player.getInventory().getHeldItemSlot();
		ItemStack hand = this.player.getInventory().getItem(slot);
		if (hand != null) {
			int i = this.player.getInventory().firstEmpty();
			if (i != -1) {
				// else the player will loose his hand item, his bad
				this.player.getInventory().setItem(i, hand);
			}
		}
		this.player.getInventory().setItem(slot, fireB);
		this.blade = fireB;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

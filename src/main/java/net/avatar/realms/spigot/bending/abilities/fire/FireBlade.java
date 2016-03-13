package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
		if (this.blade == null 
				|| !isFireBlade(this.player.getInventory().getItemInMainHand()) 
				|| !NAME.equals(EntityTools.getBendingAbility(player))) {
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
		blade = new ItemStack(Material.GOLD_SWORD);
		ItemMeta meta = blade.getItemMeta();
		meta.setLore(Arrays.asList(LORE_NAME));
		blade.setItemMeta(meta);
		EntityTools.giveItemInHand(player, blade);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

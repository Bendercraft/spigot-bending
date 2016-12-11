package net.bendercraft.spigot.bending.abilities.arts;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.listeners.BendingDenyItem;
import net.bendercraft.spigot.bending.listeners.BendingPlayerListener;
import net.bendercraft.spigot.bending.utils.EntityTools;


/**
 * FireBlade works by giving player a GOLDEN_SWORD, that is allowed by bending {@link BendingDenyItem}.
 * This sword does no damage @link {@link BendingPlayerListener} but calls {@link #affect(LivingEntity)}
 *
 */
@ABendingAbility(name = ParaStick.NAME, affinity=BendingAffinity.CHI, shift=false)
public class ParaStick extends BendingActiveAbility {
	public final static String NAME = "ParaStick";
	
	private static String LORE_NAME = "ParaStick";
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;

	private ItemStack stick;
	private long cooldown;
	
	private boolean enhanced = false;

	public ParaStick(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_AIMCHARGETIME_PARASTICKCD_CONCUSSIONCD)) {
			this.cooldown -= 500;
		}
		if(bender.hasPerk(BendingPerk.MASTER_BLANKPOINTDAMAGE_PARASTICKCD_NEBULARRANGE)) {
			this.cooldown -= 500;
		}
	}
	
	@Override
	public boolean swing() {
		if(isState(BendingAbilityState.PROGRESSING)) {
			if(bender.hasPerk(BendingPerk.MASTER_DISENGAGE_PARAPARASTICK_CONSTITUTION)
					&& !enhanced) {
				enhanced = true;
				cooldown *= 2;
			}
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			stick = new ItemStack(Material.STICK);
			stick.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			ItemMeta meta = stick.getItemMeta();
			meta.setLore(Arrays.asList(LORE_NAME, "Glows from electricity"));
			stick.setItemMeta(meta);
			EntityTools.giveItemInOffHand(player, stick);
			
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (stick == null 
				|| !isParaStick(player.getInventory().getItemInOffHand())) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		
	}
	
	public void consume() {
		remove();
		bender.cooldown(this, cooldown);
	}
	
	public boolean isEnhanced() {
		return enhanced;
	}

	@Override
	public void stop() {
		ItemStack toRemove = null;
		for (ItemStack is : player.getInventory().getContents()) {
			if ((is != null) && isParaStick(is)) {
				toRemove = is;
				break;
			}
		}
		if (toRemove != null) {
			player.getInventory().remove(toRemove);
		}
		if(isParaStick(player.getInventory().getItemInOffHand())) {
			player.getInventory().setItemInOffHand(null);
		}
	}

	public static boolean isParaStick(ItemStack is) {
		if (is == null) {
			return false;
		}
		if ((is.getItemMeta() != null) && (is.getItemMeta().getLore() != null) && is.getItemMeta().getLore().contains(LORE_NAME)) {
			return true;
		}
		return false;
	}

	public static boolean hasParaStick(Player p) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return false;
		}
		return instances.containsKey(p) && isParaStick(p.getInventory().getItemInOffHand());
	}
	
	public static ParaStick getParaStick(Player p) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return null;
		}
		return (ParaStick) instances.get(p);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

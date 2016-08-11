package net.bendercraft.spigot.bending.abilities.fire;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.listeners.BendingDenyItem;
import net.bendercraft.spigot.bending.listeners.BendingPlayerListener;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;


/**
 * FireBlade works by giving player a GOLDEN_SWORD, that is allowed by bending {@link BendingDenyItem}.
 * This sword does no damage @link {@link BendingPlayerListener} but calls {@link #affect(LivingEntity)}
 *
 */
@ABendingAbility(name = FireBlade.NAME, element = BendingElement.FIRE, shift=false)
public class FireBlade extends BendingActiveAbility {
	public final static String NAME = "FireBlade";
	
	private static String LORE_NAME = "FireBlade";

	@ConfigurationParameter("Duration")
	private static int DURATION = 40000;
	
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 5;

	private ItemStack blade;

	public FireBlade(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			blade = new ItemStack(Material.GOLD_SWORD);
			ItemMeta meta = blade.getItemMeta();
			meta.setLore(Arrays.asList(LORE_NAME));
			blade.setItemMeta(meta);
			EntityTools.giveItemInMainHand(player, blade);
			
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (blade == null 
				|| !isFireBlade(player.getInventory().getItemInMainHand()) 
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
		for (ItemStack is : player.getInventory().getContents()) {
			if ((is != null) && isFireBlade(is)) {
				toRemove = is;
				break;
			}
		}
		if (toRemove != null) {
			player.getInventory().remove(toRemove);
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
	
	public void affect(Entity entity) {
		DamageTools.damageEntity(bender, entity, this, DAMAGE);
		Enflamed.enflame(player, entity, 4);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

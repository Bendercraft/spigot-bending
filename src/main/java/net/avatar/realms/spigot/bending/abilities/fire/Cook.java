package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

@BendingAbility(name="Heat Control", element=BendingType.Fire)
public class Cook extends ActiveAbility {

	@ConfigurationParameter("Cook-Time")
	private static long COOK_TIME = 2000;

	private static final Material[] cookables = {Material.RAW_BEEF,
			Material.RAW_CHICKEN,
			Material.RAW_FISH,
			Material.PORK,
			Material.POTATO_ITEM,
			Material.POISONOUS_POTATO,
			Material.STICK,
			Material.RABBIT,
			Material.MUTTON
	};

	private ItemStack items;
	private long time;

	public Cook(Player player) {
		super (player, null);
	}
	
	@Override
	public boolean sneak() {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;
			case CanStart:
				this.items = this.player.getItemInHand();
				if (isCookable(this.items.getType())) {
					this.time = this.startedTime;
					AbilityManager.getManager().addInstance(this);
					setState(AbilityState.Progressing);
				}
				return false;
			default:
				return false;
		}
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (!this.player.isSneaking()) {
			return false;
		}
		
		long now = System.currentTimeMillis();
		if (!this.items.equals(this.player.getItemInHand())) {
			this.time = now;
			this.items = this.player.getItemInHand();
		}

		if (!isCookable(this.items.getType())) {
			return false;
		}

		if (now > (this.time + COOK_TIME)) {
			cook();
			this.time = now;
		}

		this.player.getWorld().playEffect(this.player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 0, 10);
		return true;
	}

	private static boolean isCookable(Material material) {
		return Arrays.asList(cookables).contains(material);
	}

	private void cook() {
		ItemStack newitem = getCooked(this.items.getType());

		HashMap<Integer, ItemStack> cantfit = this.player.getInventory().addItem(
				newitem);
		for (int id : cantfit.keySet()) {
			this.player.getWorld().dropItem(this.player.getEyeLocation(), cantfit.get(id));
		}
		int amount = this.items.getAmount();
		if (amount == 1) {
			this.player.getInventory().clear(this.player.getInventory().getHeldItemSlot());
		} else {
			this.items.setAmount(amount - 1);
		}
	}

	private ItemStack getCooked(Material material) {
		ItemStack cooked = new ItemStack(Material.AIR);
		switch (material) {
			case RAW_BEEF:
				cooked.setType(Material.COOKED_BEEF);
				cooked.setAmount(1);
				break;
			case RAW_FISH:
				cooked.setType(Material.COOKED_FISH);
				cooked.setAmount(1);
			case RAW_CHICKEN:
				cooked.setType(Material.COOKED_CHICKEN);
				cooked.setAmount(1);
				break;
			case PORK:
				cooked.setType(Material.GRILLED_PORK);
				cooked.setAmount(1);
				break;
			case POTATO_ITEM:
			case POISONOUS_POTATO :
				cooked.setType(Material.BAKED_POTATO);
				cooked.setAmount(1);
				break;
			case STICK:
				cooked.setType(Material.TORCH);
				cooked.setAmount(4);
				break;
			case RABBIT:
				cooked.setType(Material.COOKED_RABBIT);
				cooked.setAmount(1);
				break;
			case MUTTON:
				cooked.setType(Material.COOKED_MUTTON);
				cooked.setAmount(1);
			default:
				break;
		}
		return cooked;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.HeatControl;
	}
}

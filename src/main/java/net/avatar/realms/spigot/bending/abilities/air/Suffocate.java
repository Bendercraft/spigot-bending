package net.avatar.realms.spigot.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name = "Suffocate", bind = BendingAbilities.Suffocate, element = BendingElement.Air, affinity = BendingAffinity.Suffocate)
public class Suffocate extends BendingActiveAbility {
	private static Map<Player, Suffocate> instances = new HashMap<Player, Suffocate>();
	private static String LORE_NAME = "Suffocation";

	@ConfigurationParameter("Range")
	private static int RANGE = 10;

	@ConfigurationParameter("Damage")
	private static int baseDamage = 1;

	@ConfigurationParameter("Speed")
	public static double speed = 1;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 2000;

	private static long interval = (long) (1000. / speed);

	private Player target;
	private Block targetLocation;
	private ItemStack helmet;
	private ItemStack temp;
	private long time;

	public Suffocate(Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		if (!this.player.isSneaking()) {
			setState(BendingAbilityState.CannotStart);
			return;
		}
		this.time = this.startedTime;

		this.temp = new ItemStack(Material.STAINED_GLASS, 1, (byte) 0x0);
		List<String> lore = new LinkedList<String>();
		lore.add(LORE_NAME);
		ItemMeta meta = this.temp.getItemMeta();
		meta.setLore(lore);
		this.temp.setItemMeta(meta);
	}

	@Override
	public boolean swing() {
		if(this.state == BendingAbilityState.CanStart) {
			Entity target = EntityTools.getTargettedEntity(this.player, RANGE);

			if (!(target instanceof Player)) {
				return false;
			}

			this.target = (Player) target;
			this.targetLocation = this.target.getLocation().getBlock();

			if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.Suffocate, this.target.getLocation())) {
				return false;
			}

			this.helmet = this.target.getInventory().getHelmet();
			this.target.getInventory().setHelmet(this.temp);

			setState(BendingAbilityState.Progressing);
			AbilityManager.getManager().addInstance(this);
		}
		return false;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.Suffocate, this.target.getLocation())) {
			return false;
		}

		if (this.target.isDead()) {
			return false;
		}

		if (!this.state.equals(BendingAbilityState.Progressing)) {
			return false;
		}

		// Must have line of sight anyway
		if (!this.player.hasLineOfSight(this.target)) {
			return false;
		}

		if (this.target.getLocation().getWorld() != this.player.getLocation().getWorld()) {
			return false;
		}

		if (this.target.getLocation().distance(this.player.getLocation()) > (2 * RANGE)) {
			return false;
		}

		// Target should be slowed to hell
		if (!this.target.hasPotionEffect(PotionEffectType.SLOW)) {
			this.target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 500, 1));
		}
		// Target is weakened
		if (!this.target.hasPotionEffect(PotionEffectType.WEAKNESS)) {
			this.target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1));
		}

		if ((System.currentTimeMillis() - this.time) >= interval) {
			this.time = System.currentTimeMillis();
			double addtionnalDamage = (this.targetLocation.getLocation().distance(this.target.getLocation()) / 10);
			if (addtionnalDamage > 6) {
				addtionnalDamage = 6;
			}

			this.target.getWorld().playEffect(this.target.getEyeLocation(), Effect.SMOKE, 4);
			this.target.damage(baseDamage + addtionnalDamage, this.player);

			this.targetLocation = this.target.getLocation().getBlock();
		}

		return true;
	}

	public void restoreTargetHelmet() {
		if (this.temp != null) {
			this.target.getInventory().setHelmet(this.helmet);
			this.temp = null;
		}
	}

	@Override
	public void stop() {
		this.restoreTargetHelmet();
	}

	@Override
	public void remove() {
		this.bender.cooldown(BendingAbilities.Suffocate, COOLDOWN);
		super.remove();
	}

	public static boolean isTempHelmet(ItemStack is) {
		if (is == null) {
			return false;
		}
		if ((is.getItemMeta() != null) && (is.getItemMeta().getLore() != null) && is.getItemMeta().getLore().contains(LORE_NAME)) {
			return true;
		}
		return false;
	}

	public static boolean isTargeted(Player p) {
		for (Suffocate suffocate : instances.values()) {
			if (suffocate.target.getUniqueId().equals(p.getUniqueId())) {
				return true;
			}
		}

		return false;
	}

	public static Suffocate getSuffocateByTarget(Player p) {
		for (Suffocate suffocate : instances.values()) {
			if (suffocate.target.getUniqueId().equals(p.getUniqueId())) {
				return suffocate;
			}
		}
		return null;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

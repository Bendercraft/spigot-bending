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

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = Suffocate.NAME, affinity = BendingAffinity.Suffocate)
public class Suffocate extends BendingActiveAbility {
	public final static String NAME = "Suffocate";
	
	private static Map<Player, Suffocate> instances = new HashMap<Player, Suffocate>();
	private static String LORE_NAME = "Suffocation";

	@ConfigurationParameter("Range")
	private static int RANGE = 10;

	@ConfigurationParameter("Damage")
	private static int baseDamage = 1;

	@ConfigurationParameter("Speed")
	public static double SPEED = 1;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 2000;

	private long interval;
	private Player target;
	private Block targetLocation;
	private ItemStack helmet;
	private ItemStack temp;
	private long time;

	public Suffocate(RegisteredAbility register, Player player) {
		super(register, player);

		this.time = this.startedTime;

		this.temp = new ItemStack(Material.STAINED_GLASS, 1, (byte) 0x0);
		List<String> lore = new LinkedList<String>();
		lore.add(LORE_NAME);
		ItemMeta meta = this.temp.getItemMeta();
		meta.setLore(lore);
		this.temp.setItemMeta(meta);
		
		interval = (long) (1000. / SPEED);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized() 
				|| !this.player.isSneaking()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.Start) {
			Entity target = EntityTools.getTargetedEntity(this.player, RANGE);

			if (!(target instanceof Player)) {
				return false;
			}

			this.target = (Player) target;
			this.targetLocation = this.target.getLocation().getBlock();

			if (ProtectionManager.isRegionProtectedFromBending(this.player, NAME, this.target.getLocation())) {
				return false;
			}

			this.helmet = this.target.getInventory().getHelmet();
			this.target.getInventory().setHelmet(this.temp);

			setState(BendingAbilityState.Progressing);
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick() 
				|| ProtectionManager.isRegionProtectedFromBending(this.player, NAME, this.target.getLocation()) 
				|| this.target.isDead() 
				|| !getState().equals(BendingAbilityState.Progressing) 
				|| !this.player.hasLineOfSight(this.target) 
				|| this.target.getLocation().getWorld() != this.player.getLocation().getWorld() 
				|| this.target.getLocation().distance(this.player.getLocation()) > (2 * RANGE)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
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
		this.bender.cooldown(NAME, COOLDOWN);
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

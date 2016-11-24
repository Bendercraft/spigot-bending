package net.bendercraft.spigot.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = Suffocate.NAME, affinity = BendingAffinity.SUFFOCATE)
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
		if(isState(BendingAbilityState.START)) {
			LivingEntity target = EntityTools.getTargetedEntity(this.player, RANGE);
			if(affect(target)) {
				setState(BendingAbilityState.PROGRESSING);
			}
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick() 
				|| ProtectionManager.isLocationProtectedFromBending(this.player, register, this.target.getLocation())
				|| this.target.isDead() 
				|| !getState().equals(BendingAbilityState.PROGRESSING) 
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

		if ((System.currentTimeMillis() - this.time) >= interval) {
			this.time = System.currentTimeMillis();
			double addtionnalDamage = (this.targetLocation.getLocation().distance(this.target.getLocation()) / 10);
			if (addtionnalDamage > 6) {
				addtionnalDamage = 6;
			}

			this.target.getWorld().playEffect(this.target.getEyeLocation(), Effect.SMOKE, 4);
			DamageTools.damageEntity(bender, target, this, baseDamage + addtionnalDamage, true, 0, 0.0F, true);

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
	
	private boolean affect(LivingEntity entity) {
		if (!(entity instanceof Player)) {
			return false;
		}

		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}

		target = (Player) entity;
		targetLocation = target.getLocation().getBlock();
		helmet = target.getInventory().getHelmet();
		target.getInventory().setHelmet(temp);
		return true;
	}
}

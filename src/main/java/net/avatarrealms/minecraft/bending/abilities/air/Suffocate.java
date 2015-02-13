package net.avatarrealms.minecraft.bending.abilities.air;

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

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;

public class Suffocate implements IAbility {
	private static Map<Player, Suffocate> instances = new HashMap<Player, Suffocate>();
	private static String LORE_NAME = "Suffocation";
	//private int distance = ConfigManager.suffocateDistance;
	private static int distance = 10;
	private static int baseDamage = 1;
	public static double speed = 1;
	private static long interval = (long) (1000. / speed);
	
	private IAbility parent;
	private Player player;
	private BendingPlayer bPlayer;
	private Player target;
	private Block targetLocation;
	private ItemStack helmet;
	private ItemStack temp;
	private long time;
	
	public Suffocate(Player player, IAbility parent) {
		if(instances.containsKey(player)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.isOnCooldown(Abilities.Suffocate))
			return;
		
		if(!EntityTools.canBend(player, Abilities.Suffocate)) {
			return;
		}
		
		Entity target = EntityTools.getTargettedEntity(player, distance);
		
		if(!(target instanceof Player)) {
			return;
		}
		
		this.parent = parent;
		this.player = player;
		this.bPlayer = bPlayer;
		this.target = (Player)target;
		this.targetLocation = this.target.getLocation().getBlock();
		
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.Suffocate, target.getLocation())) {
			return;
		}
		
		
		helmet = this.target.getInventory().getHelmet();
		
		temp = new ItemStack(Material.STAINED_GLASS, 1, (byte) 0x0);
		List<String> lore = new LinkedList<String>();
		lore.add(LORE_NAME);
		ItemMeta meta = temp.getItemMeta();
		meta.setLore(lore);
		temp.setItemMeta(meta);
		
		this.target.getInventory().setHelmet(temp);
		bPlayer.cooldown(Abilities.Suffocate);
		instances.put(player, this);
	}
	
	public boolean progress() {
		if(bPlayer.getPlayer() == null || target == null) {
			return false;
		}
		
		if (bPlayer.getPlayer().isDead() || !bPlayer.getPlayer().isOnline()) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.Suffocate, target.getLocation())) {
			return false;
		}
		
		//if target is dead, no longer bend
		if(target.isDead()) {
			return false;
		}
		
		//Must have line of sight anyway
		if(!bPlayer.getPlayer().hasLineOfSight(target)) {
			return false;
		}
		
		if(target.getLocation().getWorld() != player.getLocation().getWorld()) {
			return false;
		}
		
		if (target.getLocation().distance(player.getLocation()) > 2*distance) {
			return false;
		}
		
		//Target should be slowed to hell
		if(!target.hasPotionEffect(PotionEffectType.SLOW)) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 500, 1));
		}
		//Target is weakened
		if(!target.hasPotionEffect(PotionEffectType.WEAKNESS)) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1));
		}
		
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			double addtionnalDamage = 0;
			try {
				addtionnalDamage = (this.targetLocation.getLocation().distance(target.getLocation())/10);
				if(addtionnalDamage > 6) {
					addtionnalDamage = 6;
				}
			} catch(Exception e) {
				//Quiet, does not matter
			}
			target.getWorld().playEffect(target.getEyeLocation(), Effect.SMOKE, 4);
			target.damage(baseDamage+addtionnalDamage, bPlayer.getPlayer());
					
			this.targetLocation = target.getLocation().getBlock();
		}
		
		return true;
	}
	
	public void restoreTargetHelmet() {
		if(temp != null) {
			this.target.getInventory().setHelmet(helmet);
			temp = null;
		}
	}
	
	public void remove() {
		//Potions effects will end naturally, so leave them be
		this.restoreTargetHelmet();
		instances.remove(this.bPlayer.getPlayer());
	}
	
	public static void progressAll() {
		List<Suffocate> toRemove = new LinkedList<Suffocate>();
		for(Suffocate suffocate : instances.values()) {
			if (!suffocate.progress()) {
				toRemove.add(suffocate);
			}
		}
		
		for(Suffocate suffocate : toRemove) {
			suffocate.remove();
		}
	}
	
	public static void removeAll() {
		List<Suffocate> toRemove = new LinkedList<Suffocate>();
		toRemove.addAll(instances.values());
		
		for(Suffocate suffocate : toRemove) {
			suffocate.remove();
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
	
	public static boolean isTempHelmet(ItemStack is) {
		if(is == null) {
			return false;
		}
		if(is.getItemMeta() != null 
				&& is.getItemMeta().getLore() != null
				&& is.getItemMeta().getLore().contains(LORE_NAME)) {
			return true;
		}
		return false;
	}
	
	public static boolean isTargeted(Player p) {
		for(Suffocate suffocate : instances.values()) {
			if(suffocate.target.getUniqueId().equals(p.getUniqueId())) {
				return true;
			}
		}
		
		return false;
	}
	
	public static Suffocate getSuffocateByTarget(Player p) {
		for(Suffocate suffocate : instances.values()) {
			if(suffocate.target.getUniqueId().equals(p.getUniqueId())) {
				return suffocate;
			}
		}
		return null;
	}
}

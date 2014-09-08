package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

public class Suffocate implements IAbility {
	private static Map<Player, Suffocate> instances = new HashMap<Player, Suffocate>();
	//private int distance = ConfigManager.suffocateDistance;
	private static int distance = 10;
	private static int baseDamage = 1;
	public static double speed = 1;
	private static long interval = (long) (1000. / speed);
	
	private IAbility parent;
	private BendingPlayer player;
	private Block location;
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
		this.player = bPlayer;
		this.location = player.getLocation().getBlock();
		this.target = (Player)target;
		this.targetLocation = this.target.getLocation().getBlock();
		helmet = this.target.getInventory().getHelmet();
		temp = new ItemStack(Material.STAINED_GLASS, 1, (byte) 0x0);
		this.target.getInventory().setHelmet(temp);
		bPlayer.cooldown(Abilities.Suffocate);
		instances.put(player, this);
	}
	
	public boolean progress() {
		if (player.getPlayer().isDead() || !player.getPlayer().isOnline()) {
			return false;
		}
		
		//If bender is no longer on suffocation bend, then remove his bending
		if(!player.getAbility().equals(Abilities.Suffocate)) {
			return false;
		}
		
		//If bender has moved (for some reason), remove this bending
		if(!this.location.equals(player.getPlayer().getLocation().getBlock())) {
			return false;
		}
		
		//if target is dead, no longer bend
		if(target.isDead()) {
			return false;
		}
		
		//Must have line of sight anyway
		if(!player.getPlayer().hasLineOfSight(target)) {
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
			target.damage(baseDamage+addtionnalDamage);
					
			this.targetLocation = target.getLocation().getBlock();
		}
		
		//TODO : Decrease the breath level of the target
		//TODO : Display a smoke effect on the target head
		
		return true;
	}
	
	public void remove() {
		//Potions effects will end naturally, so leave them be
		this.target.getInventory().setHelmet(helmet);
		instances.remove(this.player.getPlayer());
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
	
	public static boolean isTempHelmet(ItemStack stack) {
		for(Suffocate suffocate : instances.values()) {
			if(suffocate.temp.equals(stack)) {
				return true;
			}
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
}

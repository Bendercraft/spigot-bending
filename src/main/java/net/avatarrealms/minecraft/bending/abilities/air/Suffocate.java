package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

public class Suffocate implements IAbility {
	private static Map<Player, Suffocate> instances = new HashMap<Player, Suffocate>();
	//private int distance = ConfigManager.suffocateDistance;
	private int distance = 10;
	
	private IAbility parent;
	private BendingPlayer player;
	private Location location;
	private Player target;
	
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
		this.location = player.getLocation();
		this.target = (Player)target;
		
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
		if(!this.location.equals(player.getPlayer().getLocation())) {
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
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1, 1));
		}
		//Target is weakened
		if(target.hasPotionEffect(PotionEffectType.WEAKNESS)) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1, 1));
		}
		//Target is poisoned
		if(target.hasPotionEffect(PotionEffectType.POISON)) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1, 1));
		}
		
		//TODO : Decrease the breath level of the target
		//TODO : Display a smoke effect on the target head
		
		return true;
	}
	
	public void remove() {
		//Potions effects will end naturally, so leave them be
		instances.remove(this.player);
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
}

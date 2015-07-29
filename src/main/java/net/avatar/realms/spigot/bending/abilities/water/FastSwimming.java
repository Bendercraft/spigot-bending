package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Dolphin", element=BendingType.Water)
public class FastSwimming implements IPassiveAbility {

	private static double factor = ConfigManager.fastSwimmingFactor;
	private static Map<Player, FastSwimming> instances = new HashMap<Player, FastSwimming>();
	
	private Player player;
	
	public FastSwimming (Player player) {
		if (!player.isOnline() || player.isDead()) {
			return;
		}
		if (!EntityTools.canBendPassive(player, BendingType.Water)) {
			return;
		}
		
		this.player = player;
		instances.put(player, this);
	}
	
	public boolean progress() {		
		if (!player.isOnline() || player.isDead()) {
			return false;
		}
		
		if (!(EntityTools.canBendPassive(player, BendingType.Water)
				&& player.isSneaking())){
			return false;
		}
		Abilities ability = EntityTools.getBendingAbility(player);
		if (ability != null && ability.isShiftAbility() && ability != Abilities.WaterSpout) {
			return false;
		}
		if (BlockTools.isWater(player.getLocation().getBlock())
				&& !TempBlock.isTempBlock(player.getLocation().getBlock())) {
			swimFast();
		}
		return true;
	}
	
	public static void progressAll() {
		LinkedList<Player> toRemove = new LinkedList<Player>();
		boolean keep;
		for (Player p : instances.keySet()) {
			keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
			}
		}
		for (Player p : toRemove) {
			instances.get(p).remove();
		}
	}
	
	public void remove() {
		instances.remove(player);
	}
	
	
	private void swimFast() {
		Vector dir = player.getEyeLocation().getDirection().clone();
		player.setVelocity(dir.normalize().multiply(factor));
	}	
}

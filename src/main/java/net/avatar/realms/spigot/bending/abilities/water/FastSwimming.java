package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Dolphin", element=BendingType.Water)
public class FastSwimming implements net.avatar.realms.spigot.bending.abilities.deprecated.IPassiveAbility {

	@ConfigurationParameter("Speed-Factor")
	private static double FACTOR = 0.7;
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
		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}

		if (!(EntityTools.canBendPassive(this.player, BendingType.Water)
				&& this.player.isSneaking())){
			return false;
		}
		Abilities ability = EntityTools.getBendingAbility(this.player);
		if ((ability != null) && ability.isShiftAbility() && (ability != Abilities.WaterSpout)) {
			return false;
		}
		if (BlockTools.isWater(this.player.getLocation().getBlock())
				&& !TempBlock.isTempBlock(this.player.getLocation().getBlock())) {
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
		instances.remove(this.player);
	}


	private void swimFast() {
		Vector dir = this.player.getEyeLocation().getDirection().clone();
		this.player.setVelocity(dir.normalize().multiply(FACTOR));
	}	
}

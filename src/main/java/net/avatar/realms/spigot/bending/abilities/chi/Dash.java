package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigManager;

public class Dash implements IAbility {

	private static Map<Player, Dash> instances = new HashMap<Player, Dash>();

	private static double length = ConfigManager.dashLength;
	private static double height = ConfigManager.dashHeight;
	
	private Player player;
	private IAbility parent;
	private Vector direction;

	public Dash(Player player, IAbility parent) {
		this.player = player;
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (!bPlayer.isOnCooldown(Abilities.Dash)) {
			instances.put(player, this);
			bPlayer.cooldown(Abilities.Dash);
		}

	}

	public static boolean isDashing(Player player) {
		return instances.containsKey(player);
	}

	public static Dash getDash(Player pl) {
		return instances.get(pl);
	}

	public void dash() {
		Vector dir = new Vector(direction.getX() * length, height,
				direction.getZ() * length);
		player.setVelocity(dir);

		instances.remove(player);
	}

	public void setDirection(Vector d) {
		if (Double.isNaN(d.getX()) 
				|| Double.isNaN(d.getY())
				|| Double.isNaN(d.getZ())
				|| ((d.getX() < 0.005 && d.getX() > -0.005)
				&& (d.getZ() < 0.005 && d.getZ() > -0.005))) {
			this.direction = player.getLocation().getDirection().normalize();
		} else {
			this.direction = d.normalize();
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

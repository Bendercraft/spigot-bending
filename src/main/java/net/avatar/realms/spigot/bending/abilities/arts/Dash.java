package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

@ABendingAbility(name = Dash.NAME, element = BendingElement.MASTER, shift=false)
public class Dash extends BendingActiveAbility {
	public final static String NAME = "Dash";
	
	@ConfigurationParameter("Length")
	private static double LENGTH = 1.95;

	@ConfigurationParameter("Height")
	private static double HEIGHT = 0.71;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 4000;

	private Vector direction;

	public Dash(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARING);
		}

		return false;
	}

	public static boolean isDashing(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}
		return instances.containsKey(player);
	}

	public static Dash getDash(Player pl) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		return (Dash) instances.get(pl);
	}

	@Override
	public void progress() {
		if (getState() == BendingAbilityState.PROGRESSING) {
			dash();
		}
	}

	public void dash() {
		Vector dir = new Vector(this.direction.getX() * LENGTH, HEIGHT, this.direction.getZ() * LENGTH);
		this.player.setVelocity(dir);
		remove();
	}

	// This should be called in OnMoveEvent to set the direction dash the same
	// as the player
	public void setDirection(Vector d) {
		if (getState() != BendingAbilityState.PREPARING) {
			return;
		}
		if (Double.isNaN(d.getX()) || Double.isNaN(d.getY()) || Double.isNaN(d.getZ()) || (((d.getX() < 0.005) && (d.getX() > -0.005)) && ((d.getZ() < 0.005) && (d.getZ() > -0.005)))) {
			this.direction = this.player.getLocation().getDirection().clone().normalize();
		} else {
			this.direction = d.normalize();
		}
		setState(BendingAbilityState.PROGRESSING);
	}

	@Override
	public void stop() {
		this.bender.cooldown(NAME, COOLDOWN);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

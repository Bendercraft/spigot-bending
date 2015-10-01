package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name = "AirGlide", bind = BendingAbilities.AirGlide, element = BendingElement.Air)
public class AirGlide extends BendingPassiveAbility {

	@ConfigurationParameter("Fall-Factor")
	private static double FALL_FACTOR = 0.45;

	private FlyingPlayer fly;
	private double y;

	public AirGlide(Player player) {
		super(player, null);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (this.player.isSneaking()) {
			return false;
		}

		if (!EntityTools.canBendPassive(this.player, BendingElement.Air)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean start() {
		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return false;
		}
		this.y = this.player.getVelocity().getY() * FALL_FACTOR;
		AbilityManager.getManager().addInstance(this);
		setState(BendingAbilityState.Progressing);
		this.fly = FlyingPlayer.addFlyingPlayer(this.player, this, 0L);
		this.bender.cooldown(this, 500);
		return true;
	}

	@Override
	public void stop() {
		if (this.fly != null) {
			this.fly.resetState();
		}
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (!(EntityTools.canBendPassive(this.player, BendingElement.Air) && this.player.isSneaking())) {
			return false;
		}
		BendingAbilities ability = EntityTools.getBendingAbility(this.player);
		if ((ability != null) && ability.isShiftAbility()) {
			return false;
		}

		if (this.player.getLocation().getBlock().getType() == Material.AIR) {
			Vector vel = this.player.getVelocity();
			vel.setY(this.y);
			this.player.setVelocity(vel);
		}
		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

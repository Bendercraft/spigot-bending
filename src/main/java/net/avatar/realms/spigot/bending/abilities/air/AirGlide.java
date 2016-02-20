package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = AirGlide.NAME, element = BendingElement.AIR)
public class AirGlide extends BendingPassiveAbility {
	public final static String NAME = "AirGlide";

	@ConfigurationParameter("Fall-Factor")
	private static double FALL_FACTOR = 0.45;

	private FlyingPlayer fly;
	private double x;
	private double y;
	private double z;

	public AirGlide(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (this.player.isSneaking()) {
			return false;
		}

		if (!EntityTools.canBendPassive(this.player, BendingElement.AIR)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean start() {
		this.x = this.player.getVelocity().getX();
		this.y = this.player.getVelocity().getY() * FALL_FACTOR;
		this.z = this.player.getVelocity().getZ();

		setState(BendingAbilityState.PROGRESSING);
		this.fly = FlyingPlayer.addFlyingPlayer(this.player, this, 60 * 1000L);
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
	public boolean canTick() {
		if(!super.canTick() 
				|| !(EntityTools.canBendPassive(this.player, BendingElement.AIR) && this.player.isSneaking())) {
			return false;
		}
		String ability = EntityTools.getBendingAbility(this.player);
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);
		if ((ability != null) && register.isShift()) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if (this.y > 0) {
			remove();
		}
		if (this.player.getLocation().getBlock().getType() == Material.AIR) {
			Vector vel = this.player.getVelocity();
			vel.setX(this.x);
			vel.setY(this.y);
			vel.setZ(this.z);
			this.player.setVelocity(vel);
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

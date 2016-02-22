package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = AirSpeed.NAME, element = BendingElement.AIR, passive = true)
public class AirSpeed extends BendingPassiveAbility {
	public final static String NAME = "AirSpeed";

	public AirSpeed(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public boolean start() {
		setState(BendingAbilityState.PROGRESSING);
		return true;
	}

	@Override
	public void progress() {
		if (this.player.isSprinting() 
				&& this.bender.isBender(BendingElement.AIR)
				&& EntityTools.canBendPassive(this.player, BendingElement.AIR)) {
			applySpeed();
		} else {
			remove();
		}
	}

	private void applySpeed() {
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70, 1);

		this.player.addPotionEffect(speed);
		if (!EntityTools.getBendingAbility(this.player).equals(AirScooter.NAME)) {
			PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70, 2);
			this.player.addPotionEffect(jump);
		}
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (!(this.bender.isBender(BendingElement.AIR))) {
			return false;
		}

		return true;
	}

	@Override
	public void stop() {
		
	}

}

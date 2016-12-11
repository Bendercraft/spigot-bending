package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPassiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;

@ABendingAbility(name = Speed.NAME, element = BendingElement.MASTER, shift = false, passive = true)
public class Speed extends BendingPassiveAbility {
	public final static String NAME = "Speed";

	private int speedAmplifier = 0;
	public Speed(RegisteredAbility register, Player player) {
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
		if (this.player.isSprinting()) {
			if (this.bender.isBender(BendingElement.MASTER)) {
				applySpeed();
				return;
			}
		}

		remove();
	}

	private void applySpeed() {
		if(bender.hasPerk(BendingPerk.MASTER_TRAINING)) {
			PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70, this.speedAmplifier);
			PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70, 1);
			this.player.addPotionEffect(speed);
			this.player.addPotionEffect(jump);
		}
	}

	@Override
	public void stop() {
		
	}

}

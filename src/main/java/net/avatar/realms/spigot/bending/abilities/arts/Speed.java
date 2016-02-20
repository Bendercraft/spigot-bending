package net.avatar.realms.spigot.bending.abilities.arts;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;

@ABendingAbility(name = "ChiSpeed", bind = BendingAbilities.ChiSpeed, element = BendingElement.Master)
public class Speed extends BendingPassiveAbility {

	private int speedAmplifier = 0;
	public Speed(Player player) {
		super(player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public boolean start() {
		setState(BendingAbilityState.Progressing);
		return true;
	}

	@Override
	public void progress() {
		if (this.player.isSprinting()) {
			if (this.bender.isBender(BendingElement.Master)) {
				applySpeed();
				return;
			}
		}

		remove();
	}

	private void applySpeed() {
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70, this.speedAmplifier);
		PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70, 1);
		this.player.addPotionEffect(speed);
		this.player.addPotionEffect(jump);
	}

	@Override
	public void stop() {
		
	}

}

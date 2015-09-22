package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.base.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name = "AirSpeed", bind = BendingAbilities.AirSpeed, element = BendingElement.Air)
public class AirSpeed extends BendingPassiveAbility {

	public AirSpeed(Player player) {
		super(player, null);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public boolean start() {
		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return false;
		}
		// I've separate it from constructor in case where a player bind Speed
		// (as it became an Abilities enum) and make fun by clicking...
		// new Flight(this.player);
		// this.player.setAllowFlight(true);
		AbilityManager.getManager().addInstance(this);
		setState(BendingAbilityState.Progressing);
		return true;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (this.player.isSprinting()) {
			if (this.bender.isBender(BendingElement.Air)) {
				if (EntityTools.canBendPassive(this.player, BendingElement.Air)) {
					applySpeed();
					return true;
				}
			}
		}

		return false;
	}

	private void applySpeed() {
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70, 1);

		new TempPotionEffect(this.player, speed);
		if (EntityTools.getBendingAbility(this.player) != BendingAbilities.AirScooter) {
			PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70, 2);
			new TempPotionEffect(this.player, jump);
		}
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (!(this.bender.isBender(BendingElement.Air))) {
			return false;
		}

		return true;
	}

}

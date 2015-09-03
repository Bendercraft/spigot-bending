package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.base.PassiveAbility;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility (name = "AirSpeed", element = BendingType.Air)
public class AirSpeed extends PassiveAbility {
	
	public AirSpeed (Player player) {
		super(player, null);
	}
	
	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.AirSpeed;
	}
	
	@Override
	public boolean start () {
		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		// I've separate it from constructor in case where a player bind Speed
		// (as it became an Abilities enum) and make fun by clicking...
		//new Flight(this.player);
		//this.player.setAllowFlight(true);
		AbilityManager.getManager().addInstance(this);
		setState(AbilityState.Progressing);
		return true;
	}

	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}
		
		if (this.player.isSprinting()) {
			if (this.bender.isBender(BendingType.Air)) {
				if (EntityTools.canBendPassive(this.player, BendingType.Air)) {
					applySpeed();
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void applySpeed () {
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70, 1);
		PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70, 2);
		
		new TempPotionEffect(this.player, speed);
		if (EntityTools.getBendingAbility(this.player) != Abilities.AirScooter) {
			new TempPotionEffect(this.player, jump);
		}
	}

	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if (!(this.bender.isBender(BendingType.Air))) {
			return false;
		}
		
		return true;
	}
	
}

package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.abilities.base.PassiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Dolphin", element=BendingType.Water)
public class FastSwimming extends PassiveAbility {

	@ConfigurationParameter("Speed-Factor")
	private static double FACTOR = 0.7;

	public FastSwimming (Player player) {
		super (player, null);
	}

	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (this.player.isSneaking()) {
			return false;
		}

		if (!EntityTools.canBendPassive(this.player, BendingType.Water)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean start () {
		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}

		AbilityManager.getManager().addInstance(this);
		setState(AbilityState.Progressing);
		
		return true;
	}	

	@Override
	public boolean progress() {		
		if (!super.progress()) {
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

	private void swimFast() {
		Vector dir = this.player.getEyeLocation().getDirection().clone();
		this.player.setVelocity(dir.normalize().multiply(FACTOR));
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.WaterPassive;
	}
}

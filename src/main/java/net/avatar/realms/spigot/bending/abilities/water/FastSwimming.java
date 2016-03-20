package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.TempBlock;

@ABendingAbility(name = FastSwimming.NAME, element = BendingElement.WATER, passive = true, canBeUsedWithTools = true)
public class FastSwimming extends BendingPassiveAbility {
	public final static String NAME = "Dolphin";

	@ConfigurationParameter("Speed-Factor")
	private static double FACTOR = 0.7;

	public FastSwimming(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean start() {
		setState(BendingAbilityState.PROGRESSING);
		return true;
	}
	

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		
		if (!(EntityTools.canBendPassive(player, BendingElement.WATER) && player.isSneaking())) {
			return false;
		}
		String ability = EntityTools.getBendingAbility(player);
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);
		if ((ability != null) && register.isShift() && !ability.equals(WaterSpout.NAME)) {
			return false;
		}

		if (WaterSpout.isBending(player)) {
			return false;
		}
		
		return true;
	}

	@Override
	public void progress() {
		if (BlockTools.isWater(player.getLocation().getBlock()) && !TempBlock.isTempBlock(player.getLocation().getBlock())) {
			swimFast();
		}
	}

	private void swimFast() {
		Vector dir = player.getEyeLocation().getDirection().clone();
		player.setVelocity(dir.normalize().multiply(FACTOR));
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}
}

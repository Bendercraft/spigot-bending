package net.bendercraft.spigot.bending.abilities.water;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPassiveAbility;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

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
		if (player.getLocation().getBlock().getType() == Material.WATER && !TempBlock.isTempBlock(player.getLocation().getBlock())) {
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

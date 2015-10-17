package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = "Dolphin", bind = BendingAbilities.FastSwimming, element = BendingElement.Water)
public class FastSwimming extends BendingPassiveAbility {

	@ConfigurationParameter("Speed-Factor")
	private static double FACTOR = 0.7;

	public FastSwimming(Player player) {
		super(player, null);
	}

	@Override
	public boolean start() {
		setState(BendingAbilityState.Progressing);
		return true;
	}
	

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		
		if (!(EntityTools.canBendPassive(this.player, BendingElement.Water) && this.player.isSneaking())) {
			return false;
		}
		BendingAbilities ability = EntityTools.getBendingAbility(this.player);
		if ((ability != null) && ability.isShiftAbility() && (ability != BendingAbilities.WaterSpout)) {
			return false;
		}

		if (WaterSpout.isBending(this.player)) {
			return false;
		}
		
		return true;
	}

	@Override
	public void progress() {
		if (BlockTools.isWater(this.player.getLocation().getBlock()) && !BlockTools.isTempBlock(this.player.getLocation().getBlock())) {
			swimFast();
		}
	}

	private void swimFast() {
		Vector dir = this.player.getEyeLocation().getDirection().clone();
		this.player.setVelocity(dir.normalize().multiply(FACTOR));
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}
}

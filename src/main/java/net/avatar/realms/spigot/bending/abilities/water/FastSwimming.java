package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name = "Dolphin", bind = BendingAbilities.FastSwimming, element = BendingElement.Water)
public class FastSwimming extends BendingPassiveAbility {

	@ConfigurationParameter("Speed-Factor")
	private static double FACTOR = 0.7;

	public FastSwimming(Player player) {
		super(player, null);
	}

	@Override
	public boolean start() {
		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return false;
		}

		AbilityManager.getManager().addInstance(this);
		setState(BendingAbilityState.Progressing);

		return true;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
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

		if (BlockTools.isWater(this.player.getLocation().getBlock()) && !BlockTools.isTempBlock(this.player.getLocation().getBlock())) {
			swimFast();
		}
		return true;
	}

	private void swimFast() {
		Vector dir = this.player.getEyeLocation().getDirection().clone();
		this.player.setVelocity(dir.normalize().multiply(FACTOR));
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

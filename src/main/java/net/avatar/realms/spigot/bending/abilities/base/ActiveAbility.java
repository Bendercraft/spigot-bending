package net.avatar.realms.spigot.bending.abilities.base;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

public abstract class ActiveAbility extends Ability {

	/**
	 * Construct the bases of a new active ability instance
	 *
	 * @param player
	 *        The player that launches this ability
	 * @param parent
	 *        The ability that generates this ability. null if none
	 */
	public ActiveAbility (Player player, IAbility parent) {
		super(player, parent);

		if (canBeInitialized()) {
			this.startedTime = System.currentTimeMillis();
			setState(AbilityState.CanStart);
		}
		else {
			setState(AbilityState.CannotStart);
		}
	}

	/**
	 * What should the ability do when the player click
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean swing () {
		return false;
	}

	/**
	 * What should the ability do when the player jump. Not used for the moment
	 * as no way to detect player jump.
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean jump () {
		return false;
	}

	/**
	 * What should the ability do when the player sneaks.
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean sneak () {
		return false;
	}

	/**
	 * What should the ability do when the player falls.
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean fall () {
		return false;
	}

	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (this.bender.isOnCooldown(this.getAbilityType())) {
			return false;
		}

		return true;
	}

	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBending(this.player, this.getAbilityType(), this.player.getLocation())) {
			return false;
		}

		return true;
	}

	@Override
	protected long getMaxMillis () {
		return 60000;
	}
}

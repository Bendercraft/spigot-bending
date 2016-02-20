package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.utils.ProtectionManager;

public abstract class BendingActiveAbility extends BendingAbility {

	public BendingActiveAbility(RegisteredAbility register, Player player) {
		super(register, player);
	}

	/**
	 * What should the ability do when the player click
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean swing() {
		return false;
	}

	/**
	 * What should the ability do when the player jump. Not used for the moment
	 * as no way to detect player jump.
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean jump() {
		return false;
	}

	/**
	 * What should the ability do when the player sneaks.
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean sneak() {
		return false;
	}

	/**
	 * What should the ability do when the player falls.
	 *
	 * @return <code>true</code> if we should create a new version of the
	 *         ability <code>false</code> otherwise
	 */
	public boolean fall() {
		return false;
	}

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(this.player, getName(), this.player.getLocation())) {
			return false;
		}
		return true;
	}

	@Override
	protected long getMaxMillis() {
		return 60000;
	}
}

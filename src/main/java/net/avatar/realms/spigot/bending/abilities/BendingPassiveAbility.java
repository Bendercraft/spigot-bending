package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.utils.ProtectionManager;

public abstract class BendingPassiveAbility extends BendingAbility {

	public BendingPassiveAbility(Player player, BendingAbility parent) {
		super(player, parent);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBendingPassives(this.player, this.player.getLocation())) {
			return false;
		}

		return true;
	}

	/**
	 * Start the new instance of the passive ability
	 * 
	 * @return <code>true</code> if the passive properly worked
	 *         <code>false</code> if the passive did NOT properly work
	 */
	public abstract boolean start();
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBendingPassives(this.player, this.player.getLocation()) 
				|| !getState().equals(BendingAbilityState.Progressing)) {
			return false;
		}
		return true;
	}

	@Override
	protected long getMaxMillis() {
		return 0;
	}
}

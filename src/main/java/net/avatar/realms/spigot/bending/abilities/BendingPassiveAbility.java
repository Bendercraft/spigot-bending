package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.utils.ProtectionManager;

public abstract class BendingPassiveAbility extends BendingAbility {

	public BendingPassiveAbility(RegisteredAbility register, Player player) {
		super(register, player);
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
				|| !getState().equals(BendingAbilityState.PROGRESSING)) {
			return false;
		}
		return true;
	}

	@Override
	protected long getMaxMillis() {
		return 0;
	}
	
	public static boolean isPassive(RegisteredAbility register) {
		return BendingPassiveAbility.class.isAssignableFrom(register.getAbility());
	}
}

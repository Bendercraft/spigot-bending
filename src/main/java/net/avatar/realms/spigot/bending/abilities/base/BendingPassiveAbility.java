package net.avatar.realms.spigot.bending.abilities.base;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

public abstract class BendingPassiveAbility extends BendingAbility {
	
	public BendingPassiveAbility (Player player, BendingAbility parent) {
		super(player, parent);
		if (canBeInitialized()) {
			this.startedTime = System.currentTimeMillis();
			setState(BendingAbilityState.CanStart);
		}
		else {
			setState(BendingAbilityState.CannotStart);
		}
	}
	
	@Override
	public boolean canBeInitialized () {
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
	public abstract boolean start ();
	
	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBendingPassives(this.player, this.player.getLocation())) {
			return false;
		}
		
		if (!this.state.equals(BendingAbilityState.Progressing)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	protected long getMaxMillis () {
		return 0;
	}
	
}

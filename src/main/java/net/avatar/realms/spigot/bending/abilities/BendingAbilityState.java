package net.avatar.realms.spigot.bending.abilities;

public enum BendingAbilityState {

	None 		(0),
	CannotStart	(1),
	CanStart 	(2),
	Preparing 	(3),
	Prepared 	(4),
	Progressing (5),
	Ending		(6),
	/**
	 * When the state is on Ended, the ability will be removed in the next tick
	 */
	Ended		(7),
	Removed		(8);

	private int step;

	BendingAbilityState (int step) {
		this.step = step;
	}

	public boolean isBefore (BendingAbilityState state) {
		if (this.step < state.step) {
			return true;
		}
		return false;
	}
}

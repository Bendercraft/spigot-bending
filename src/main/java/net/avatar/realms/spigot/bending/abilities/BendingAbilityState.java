package net.avatar.realms.spigot.bending.abilities;

public enum BendingAbilityState {

	Start(0), Preparing(1), Prepared(2), Progressing(3),
	/**
	 * When the state is on Ended, the ability will be removed in the next tick
	 */
	Ended(4);

	private int step;

	BendingAbilityState(int step) {
		this.step = step;
	}

	public boolean isBefore(BendingAbilityState state) {
		if (this.step < state.step) {
			return true;
		}
		return false;
	}
}

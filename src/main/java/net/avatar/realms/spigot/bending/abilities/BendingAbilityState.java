package net.avatar.realms.spigot.bending.abilities;

public enum BendingAbilityState {

	START(0), PREPARING(1), PREPARED(2), PROGRESSING(3),
	/**
	 * When the state is on Ended, the ability will be removed in the next tick
	 */
	ENDED(4);

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

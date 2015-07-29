package net.avatar.realms.spigot.bending.abilities;

public enum AbilityState {
	
	None 		(0),
	CannotStart	(1),
	CanStart 	(2),
	Started		(3),
	Progressing	(4),
	Ended		(5),
	Removed		(6);
	
	private int step;
	
	AbilityState (int step) {
		this.step = step;
	}
	
	public boolean isBefore (AbilityState state) {
		if (this.step < state.step) {
			return true;
		}
		return false;
	}
}

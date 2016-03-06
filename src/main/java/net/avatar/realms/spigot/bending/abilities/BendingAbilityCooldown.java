package net.avatar.realms.spigot.bending.abilities;

public class BendingAbilityCooldown {
	private final String ability;
	private final long start;
	private long duration;
	
	public BendingAbilityCooldown(String ability, long start, long duration) {
		this.ability = ability;
		this.start = start;
		this.duration = duration;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getAbility() {
		return ability;
	}

	public long getStart() {
		return start;
	}
	
	public long timeLeft(long when) {
		long left = (start + duration) - when;
		return left < 0 ? 0 : left;
	}
}

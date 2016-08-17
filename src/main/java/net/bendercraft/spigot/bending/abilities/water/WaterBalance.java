package net.bendercraft.spigot.bending.abilities.water;

import net.bendercraft.spigot.bending.controller.ConfigurationParameter;

public class WaterBalance {
	@ConfigurationParameter("Max")
	public static int MAX = 4;
	
	@ConfigurationParameter("Penality")
	public static double PENALITY = 0.85;

	private State state = State.STABLE;
	private int balance = MAX;
	
	public void liquid() {
		if(isState(State.EXCITED)) {
			balance--;
			if(balance <= 0) {
				state = State.STABLE;
				balance = MAX;
			}
		}
	}
	
	public void ice() {
		if(isState(State.STABLE)) {
			balance--;
			if(balance <= 0) {
				state = State.EXCITED;
				balance = MAX;
			}
		}
	}
	
	public double damage(Damage type, double value) {
		if((isState(State.STABLE) && type == Damage.LIQUID)
				|| (isState(State.EXCITED) && type == Damage.ICE)) {
			value *= PENALITY;
		}
		return value;
	}
	
	public State getState() {
		return state;
	}
	
	public boolean isState(State test) {
		return state == test;
	}
	
	public int getBalance() {
		return balance;
	}
	
	public static enum State {
		STABLE, EXCITED
	}
	
	public static enum Damage {
		LIQUID, ICE
	}

	public void shift() {
		if(isState(State.EXCITED)) {
			state = State.STABLE;
			balance = MAX;
		} else {
			state = State.EXCITED;
			balance = MAX;
		}
	}
}

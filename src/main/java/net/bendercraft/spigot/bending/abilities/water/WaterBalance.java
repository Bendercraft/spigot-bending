package net.bendercraft.spigot.bending.abilities.water;

import net.bendercraft.spigot.bending.controller.ConfigurationParameter;

public class WaterBalance {
	@ConfigurationParameter("Max")
	public static int MAX = 4;
	
	@ConfigurationParameter("Penality-Per-Point")
	public static double PENALITY_PER_POINT = 0.1;

	private int balance = 0;
	
	public void liquid() {
		balance++;
		if(balance > MAX) {
			balance = MAX;
		}
	}
	
	public void ice() {
		balance--;
		if(balance <= -MAX) {
			balance = -MAX;
		}
	}
	
	public double damage(Damage type, double value) {
		if(toward(type)) {
			value *= 1-(PENALITY_PER_POINT*Math.abs(balance));
		}
		return value;
	}
	
	public boolean toward(Damage type) {
		if((balance < 0 && type == Damage.ICE) || (balance > 0 && type == Damage.LIQUID)) {
			return true;
		}
		return false;
	}
	
	public int getBalance() {
		return balance;
	}
	
	public void setBalance(int balance) {
		this.balance = balance;
	}

	public static enum Damage {
		LIQUID, ICE
	}
}

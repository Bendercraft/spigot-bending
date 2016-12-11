package net.bendercraft.spigot.bending.abilities.water;

import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;

public class WaterBalance {
	@ConfigurationParameter("Max")
	private static int MAX = 4;
	
	@ConfigurationParameter("Penality-Per-Point")
	public static double PENALITY_PER_POINT = 0.1;

	private int balance = 0;
	private int max;
	private double penalty;
	
	public WaterBalance(BendingPlayer bender) {
		this.max = MAX;
		if(bender.hasPerk(BendingPerk.WATER_EQUILIBIRUM)) {
			this.max -= 1;
		}
		
		this.penalty = PENALITY_PER_POINT;
		if(bender.hasPerk(BendingPerk.WATER_COMMUNION)) {
			this.penalty *= 0.5;
		}
	}
	
	public void liquid() {
		balance++;
		if(balance > max) {
			balance = max;
		}
	}
	
	public void ice() {
		balance--;
		if(balance <= -max) {
			balance = -max;
		}
	}
	
	public double damage(Damage type, double value) {
		if(toward(type)) {
			value *= 1-(penalty*Math.abs(balance));
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

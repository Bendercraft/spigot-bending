package net.bendercraft.spigot.bending.abilities.fire;

import net.bendercraft.spigot.bending.controller.ConfigurationParameter;

public class FirePower {
	@ConfigurationParameter("Max")
	public static int MAX = 20;
	
	@ConfigurationParameter("Regen")
	private static int REGEN = 1;
	
	@ConfigurationParameter("Penality")
	private static int PENALITY = 2;
	
	@ConfigurationParameter("Tick")
	private static long TICK = 1000;
	
	private boolean halt = false;
	private int power = 0;
	private long time = System.currentTimeMillis();
	private String lastAbility;

	public void progress() {
		long now = System.currentTimeMillis();
		if(time + TICK < now) {
			time = now;
			if(!halt && power < MAX) {
				power += REGEN;
			}
		}
	}
	
	public void halt() {
		halt = true;
	}
	
	public void resume() {
		halt = false;
	}
	
	public void set(int value) {
		power = value;
	}
	
	public void grant(int value) {
		power += value;
		if(power > MAX) {
			power = MAX;
		}
	}
	
	public void consume(String ability, int amount) {
		consume(ability, amount, true);
	}
	
	public void consume(String ability, int amount, boolean penality) {
		if(lastAbility != null && lastAbility.equals(ability)) {
			amount *= PENALITY;
		}
		power -= amount;
		lastAbility = ability;
	}
	
	public boolean can(String ability, int amount) {
		if(lastAbility != null && lastAbility.equals(ability)) {
			amount *= PENALITY;
		}
		return (power >= amount);
	}
	
	public int getPower() {
		return power;
	}
	
	public String getLastAbility() {
		return lastAbility;
	}
}

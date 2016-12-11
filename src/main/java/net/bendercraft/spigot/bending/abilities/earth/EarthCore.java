package net.bendercraft.spigot.bending.abilities.earth;

import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;

public class EarthCore {
	@ConfigurationParameter("Max")
	private static int MAX = 4;
	
	@ConfigurationParameter("Penality-Per-Point")
	public static double PENALITY_PER_POINT = 0.1;

	private long lastDamageDone;
	private long lastPreventedDeath;

	private BendingPlayer bender;
	
	public EarthCore(BendingPlayer bender) {
		this.bender = bender;
		this.lastDamageDone = System.currentTimeMillis();
	}
	
	public void damageDone() {
		this.lastDamageDone = System.currentTimeMillis();
	}
	
	public void preventDeath() {
		this.lastPreventedDeath = System.currentTimeMillis();
	}
	
	public boolean hasPreventDeath() {
		if(bender.hasPerk(BendingPerk.EARTH_RESISTANCE)) {
			if(System.currentTimeMillis() > lastPreventedDeath+120000) {
				return true;
			}
		}
		return false;
	}
	
	public double getBonus() {
		return 2;
	}
	
	public boolean hasBonus() {
		if(bender.hasPerk(BendingPerk.EARTH_PATIENCE)) {
			if(System.currentTimeMillis() > lastDamageDone+5000) {
				return true;
			}
		}
		return false;
	}
}

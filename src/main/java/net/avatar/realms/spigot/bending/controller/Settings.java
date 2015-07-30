package net.avatar.realms.spigot.bending.controller;

public class Settings {

	@ConfigurationParameter("Abilities.Global-Cooldown")
	public static long GLOBAL_COOLDOWN = 250;
	
	@ConfigurationParameter("Fire.Day-Factor")
	public static double DAY_FACTOR = 1.1;
	
	@ConfigurationParameter("Water.Night_Factor")
	public static double NIGHT_FACTOR = 1.1;
}

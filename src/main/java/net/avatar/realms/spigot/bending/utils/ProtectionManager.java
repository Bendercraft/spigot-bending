package net.avatar.realms.spigot.bending.utils;

import net.avatar.realms.spigot.bending.integrations.worldguard.WorldGuardProtection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;

public class ProtectionManager {

	private static WorldGuardProtection worldguard;
	// private static boolean useFactions = false;
	private static boolean useCitizens = false;


	public static void init() {
		// Plugin fcp = Bukkit.getPluginManager().getPlugin("Factions");
		// if (fcp != null) {
		// PluginTools.verbose("Recognized Factions...");
		// if (Settings.RESPECT_FACTIONS) {
		// useFactions = true;
		// PluginTools.verbose("Bending is set to respect Factions' claimed lands.");
		// }
		// else {
		// PluginTools.verbose("But Bending is set to ignore Factions' claimed lands.");
		// }
		// }

		try {
			worldguard = new WorldGuardProtection(Bending.getInstance());
		}
		catch (NoClassDefFoundError e) {
			Bukkit.getLogger().warning("WorldGuard classes not found.");
			worldguard = null;
		}


		Plugin citizens = Bukkit.getPluginManager().getPlugin("Citizens");
		if (citizens != null) {
			Bending.getInstance().getLogger().info("Recognized Citizens...");
			if (Settings.RESPECT_CITIZENS) {
				useCitizens = true;
				Bending.getInstance().getLogger().info("Bending is set to respect Citizens traits.");
			} else {
				Bending.getInstance().getLogger().info("But Bending is set to ignore Citizens traits.");
			}
		}
	}

	public static boolean isEntityProtected(Entity entity) {
		if (useCitizens) {
			if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
				for (Trait trait : CitizensAPI.getNPCRegistry().getNPC(entity).getTraits()) {
					if (trait.getName().equals("bendable")) {
						return false;
					}
				}
				return true;
			}
		}
		return entity.hasPermission("bending.immune");
	}

	public static boolean isLocationProtectedFromExplosion(Player player, String ability, Location loc) {
		if (Settings.RESPECT_WORLDGUARD && worldguard != null) {
			RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);
			return worldguard.isRegionProtectedFromExplosion(player, register, loc);
		}
		return false;
	}

	public static boolean isLocationProtectedFromBending(Player player, String ability, Location loc) {
		if (ability == null || player == null) {
			return false;
		}
		
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);

		if (player.hasPermission("bending.protection.bypass")) {
			return false;
		}

		if (Settings.RESPECT_WORLDGUARD && worldguard != null) {
			return worldguard.isRegionProtectedFromBending(player, register, loc);
		}

		return false;
	}

	public static boolean isRegionProtectedFromBendingPassives(Player player, Location loc) {
		if (player == null) {
			return false;
		}
		if (player.hasPermission("bending.protection.bypass")) {
			return false;
		}
		if (Settings.RESPECT_WORLDGUARD && worldguard != null) {
			return worldguard.isRegionProtectedFromBendingPassives(player, loc);
		}
		return false;
	}
}

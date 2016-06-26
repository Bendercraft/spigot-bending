package net.bendercraft.spigot.bending.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.integrations.worldguard.WorldGuardProtection;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;

public class ProtectionManager {

	private static WorldGuardProtection worldguard = null;
	private static boolean useCitizens = false;

	public static void init(Bending plugin) {
		try {
			worldguard = new WorldGuardProtection(plugin);
		} catch (NoClassDefFoundError e) {
			Bukkit.getLogger().warning("WorldGuard classes not found.");
			worldguard = null;
		}


		Plugin citizens = Bukkit.getPluginManager().getPlugin("Citizens");
		if (citizens != null) {
			plugin.getLogger().info("Recognized Citizens...");
			if (Settings.RESPECT_CITIZENS) {
				useCitizens = true;
				plugin.getLogger().info("Bending is set to respect Citizens traits.");
			} else {
				plugin.getLogger().info("But Bending is set to ignore Citizens traits.");
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

	public static boolean isLocationProtectedFromBending(Player player, RegisteredAbility ability, Location loc) {
		if (ability == null || player == null) {
			return false;
		}
		
		if (player.hasPermission("bending.protection.bypass")) {
			return false;
		}

		if (Settings.RESPECT_WORLDGUARD && worldguard != null) {
			return worldguard.isRegionProtectedFromBending(player, ability, loc);
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

	public static void disable() {
		
	}
}

package net.avatar.realms.spigot.bending.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;

public class ProtectionManager {
	private static PluginManager pm;

	private static WorldGuardPlugin worldguard = null;
	private static WGCustomFlagsPlugin wgCustomFlags = null;

	private static boolean useWG = false;
	private static boolean useCustomFlagsWG = false;
	// private static boolean useFactions = false;
	private static boolean useCitizens = false;

	private static StateFlag BENDING;
	private static StateFlag BENDING_AIR;
	private static StateFlag BENDING_CHI;
	private static StateFlag BENDING_EARTH;
	private static StateFlag BENDING_FIRE;
	private static StateFlag BENDING_WATER;
	private static StateFlag BENDING_PASSIVES;
	private static StateFlag BENDING_SPE;
	private static StateFlag BENDING_ENERGY;

	public static void init() {
		// WorldGuard
		if (Settings.RESPECT_WORLDGUARD) {
			pm = Bending.getInstance().getServer().getPluginManager();
			Plugin plugin = pm.getPlugin("WorldGuard");
			if ((plugin != null) && (plugin.isEnabled())) {
				worldguard = (WorldGuardPlugin) plugin;
				useWG = true;
			}

			plugin = pm.getPlugin("WGCustomFlags");
			if ((plugin != null) && (plugin.isEnabled())) {
				wgCustomFlags = (WGCustomFlagsPlugin) plugin;
				useCustomFlagsWG = true;
			}
		}

		if (useWG && useCustomFlagsWG) {
			BENDING = new StateFlag("bending", true);
			BENDING_AIR = new StateFlag("bending-air", true);
			BENDING_CHI = new StateFlag("bending-chi", true);
			BENDING_EARTH = new StateFlag("bending-earth", true);
			BENDING_FIRE = new StateFlag("bending-fire", true);
			BENDING_WATER = new StateFlag("bending-water", true);
			BENDING_PASSIVES = new StateFlag("bending-passives", true);
			BENDING_SPE = new StateFlag("bending-spe", true);
			BENDING_ENERGY = new StateFlag("bending-energy", true);

			wgCustomFlags.addCustomFlag(BENDING);
			wgCustomFlags.addCustomFlag(BENDING_PASSIVES);
			wgCustomFlags.addCustomFlag(BENDING_AIR);
			wgCustomFlags.addCustomFlag(BENDING_CHI);
			wgCustomFlags.addCustomFlag(BENDING_EARTH);
			wgCustomFlags.addCustomFlag(BENDING_FIRE);
			wgCustomFlags.addCustomFlag(BENDING_WATER);
			wgCustomFlags.addCustomFlag(BENDING_SPE);
			wgCustomFlags.addCustomFlag(BENDING_ENERGY);
		}

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

	public static boolean isRegionProtectedFromExplosion(Player player, String ability, Location loc) {
		if (isRegionProtectedFromBending(player, ability, loc)) {
			return true;
		}

		if (useWG) {
			LocalPlayer localPlayer = worldguard.wrapPlayer(player);
			for (Location location : new Location[] { loc, player.getLocation() }) {
				if (!player.isOnline()) {
					return true;
				}
				RegionContainer container = worldguard.getRegionContainer();
				RegionQuery query = container.createQuery();
				if (!query.testState(location, localPlayer, DefaultFlag.OTHER_EXPLOSION)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isRegionProtectedFromBending(Player player, String ability, Location loc) {
		if (!useWG) {
			return false;
		}

		if (ability == null) {
			return false;
		}
		
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);

		if (player.hasPermission("bending.protection.bypass")) {
			return false;
		}

		LocalPlayer localPlayer = worldguard.wrapPlayer(player);
		RegionContainer container = worldguard.getRegionContainer();
		RegionQuery query = container.createQuery();

		// If passive
		if (BendingPassiveAbility.isPassive(register)) {
			if (!query.testState(loc, localPlayer, BENDING_PASSIVES)) {
				return true;
			}
			return false;
		}

		if (!query.testState(loc, localPlayer, BENDING)) {
			return true;
		}

		if (register.getAffinity() != BendingAffinity.NONE && !query.testState(loc, localPlayer, BENDING_SPE)) {
			return true;
		}

		if (register.getElement() == BendingElement.ENERGY && !query.testState(loc, localPlayer, BENDING_ENERGY)) {
			return true;
		}

		if (register.getElement() == BendingElement.AIR && !query.testState(loc, localPlayer, BENDING_AIR)) {
			return true;
		}

		if (register.getElement() == BendingElement.MASTER && !query.testState(loc, localPlayer, BENDING_CHI)) {
			return true;
		}

		if (register.getElement() == BendingElement.EARTH && !query.testState(loc, localPlayer, BENDING_EARTH)) {
			return true;
		}

		if (register.getElement() == BendingElement.FIRE && !query.testState(loc, localPlayer, BENDING_FIRE)) {
			return true;
		}

		if (register.getElement() == BendingElement.WATER && !query.testState(loc, localPlayer, BENDING_WATER)) {
			return true;
		}

		return false;
	}

	public static boolean isRegionProtectedFromBendingPassives(Player player, Location loc) {
		if (useWG) {
			LocalPlayer localPlayer = worldguard.wrapPlayer(player);
			RegionContainer container = worldguard.getRegionContainer();
			RegionQuery query = container.createQuery();
			return !query.testState(loc, localPlayer, BENDING_PASSIVES);
		}
		return false;
	}
}

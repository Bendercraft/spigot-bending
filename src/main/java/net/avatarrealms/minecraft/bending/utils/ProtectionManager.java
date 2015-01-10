package net.avatarrealms.minecraft.bending.utils;

import java.util.HashSet;
import java.util.Set;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;


public class ProtectionManager {

	private static Set<Abilities> allowedEverywhereAbilities = new HashSet<Abilities>();
	static {
		allowedEverywhereAbilities.add(Abilities.HealingWaters);
	}

	public static boolean respectWorldGuard = false;
	public static boolean respectFactions = false;
	public static boolean respectCitizens = true;

	private static WorldGuardPlugin worldguard = null;
	private static WGCustomFlagsPlugin wgCustomFlags = null;
	private static PluginManager pm;

	private static StateFlag BENDING;
	private static StateFlag BENDING_AIR;
	private static StateFlag BENDING_CHI;
	private static StateFlag BENDING_EARTH;
	private static StateFlag BENDING_FIRE;
	private static StateFlag BENDING_WATER;
	private static StateFlag BENDING_PASSIVES;
	private static StateFlag BENDING_SPE;
	private static StateFlag BENDING_ENERGY;

	public static void init () {
		// WorldGuard
		if (respectWorldGuard) {
			pm = Bending.plugin.getServer().getPluginManager();
			Plugin plugin = pm.getPlugin("WorldGuard");
			if ((plugin != null) && (plugin instanceof WorldGuardPlugin)) {
				worldguard = (WorldGuardPlugin)plugin;
			}

			plugin = pm.getPlugin("WGCustomFlags");
			if ((plugin != null) && (plugin instanceof WGCustomFlagsPlugin)) {
				wgCustomFlags = (WGCustomFlagsPlugin)plugin;
			}

			if ((worldguard == null) || (wgCustomFlags == null)) {
				throw new RuntimeException("WorldGuard is setted to be respected, but not loaded");
			}
		}

		if (respectWorldGuard) {
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

		Plugin fcp = Bukkit.getPluginManager().getPlugin("Factions");
		if (fcp != null) {
			PluginTools.verbose("Recognized Factions...");
			if (respectFactions) {
				PluginTools.verbose("Bending is set to respect Factions' claimed lands.");
			}
			else {
				PluginTools.verbose("But Bending is set to ignore Factions' claimed lands.");
			}
		}

		Plugin citizens = Bukkit.getPluginManager().getPlugin("Citizens");
		if (citizens != null) {
			PluginTools.verbose("Recognized Citizens...");
			if (respectCitizens) {
				PluginTools.verbose("Bending is set to respect Citizens traits.");
			}
			else {
				PluginTools.verbose("But Bending is set to ignore Citizens traits.");
			}
		}
	}

	public static boolean isEntityProtectedByCitizens (Entity entity) {
		if (respectCitizens) {
			if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
				for (Trait trait : CitizensAPI.getNPCRegistry().getNPC(entity).getTraits()) {
					if (trait.getName().equals("unbendable")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isRegionProtectedFromExplosion (Player player, Abilities ability, Location loc) {
		if (isRegionProtectedFromBending(player, ability, loc)) {
			return true;
		}

		PluginManager pm = Bukkit.getPluginManager();
		Plugin wgp = pm.getPlugin("WorldGuard");
		if ((wgp != null) && respectWorldGuard) {
			WorldGuardPlugin wg = (WorldGuardPlugin)wgp;
			for (Location location : new Location[] {loc, player.getLocation()}) {
				if (!player.isOnline()) {
					return true;
				}
				if (!wg.getGlobalRegionManager().get(location.getWorld()).getApplicableRegions(location)
						.allows(DefaultFlag.OTHER_EXPLOSION)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isRegionProtectedFromBending (Player player, Abilities ability, Location loc) {
		if (!respectWorldGuard) {
			return false;
		}

		if (ability == null) {
			return false;
		}

		if (isAllowedEverywhereAbility(ability)) {
			return false;
		}

		if (!worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
				.allows(BENDING, worldguard.wrapPlayer(player))) {
			return true;
		}

		if (ability.isSpecialization()
				&& !worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
						.allows(BENDING_SPE, worldguard.wrapPlayer(player))) {
			return true;
		}

		if ((ability == Abilities.AvatarState)
				&& !worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
						.allows(BENDING_ENERGY, worldguard.wrapPlayer(player))) {
			return true;
		}

		if (Abilities.isAirbending(ability)
				&& !worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
						.allows(BENDING_AIR, worldguard.wrapPlayer(player))) {
			return true;
		}

		if (Abilities.isChiBlocking(ability)
				&& !worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
						.allows(BENDING_CHI, worldguard.wrapPlayer(player))) {
			return true;
		}

		if (Abilities.isEarthbending(ability)
				&& !worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
						.allows(BENDING_EARTH, worldguard.wrapPlayer(player))) {
			return true;
		}

		if (Abilities.isFirebending(ability)
				&& !worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
						.allows(BENDING_FIRE, worldguard.wrapPlayer(player))) {
			return true;
		}

		if (Abilities.isWaterbending(ability)
				&& !worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
						.allows(BENDING_WATER, worldguard.wrapPlayer(player))) {
			return true;
		}

		return false;
	}

	public static boolean isRegionProtectedFromBendingPassives (Player player, Location loc) {
		if (!respectWorldGuard) {
			return false;
		}
		if (!worldguard.getRegionManager(loc.getWorld()).getApplicableRegions(loc)
				.allows(BENDING_PASSIVES, worldguard.wrapPlayer(player))) {
			return true;
		}
		return false;
	}

	public static boolean isAllowedEverywhereAbility (Abilities ability) {
		return allowedEverywhereAbilities.contains(ability);
	}
}

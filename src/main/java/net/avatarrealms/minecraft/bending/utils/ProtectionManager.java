package net.avatarrealms.minecraft.bending.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class ProtectionManager {
	
	private static Set<Abilities> allowedEverywhereAbilities = new HashSet<Abilities>();
	static { 
		allowedEverywhereAbilities.add(Abilities.HealingWaters);
		allowedEverywhereAbilities.add(Abilities.Illumination);
		allowedEverywhereAbilities.add(Abilities.Tremorsense);
	}

	public static boolean respectWorldGuard = false;	
	private static WorldGuardPlugin worldguard = null;
	private static WGCustomFlagsPlugin wgCustomFlags = null;
	private static PluginManager pm;
	static {
		pm = Bending.plugin.getServer().getPluginManager();
		Plugin plugin = pm.getPlugin("WorldGuard");
		if (plugin != null && plugin instanceof WorldGuardPlugin) {
			worldguard = (WorldGuardPlugin) plugin;
		}
		
		plugin = pm.getPlugin("WGCustomFlags");
		if (plugin != null && plugin instanceof WGCustomFlagsPlugin) {
			wgCustomFlags = (WGCustomFlagsPlugin) plugin;
		}
		
		if (worldguard!= null && wgCustomFlags != null) {
			respectWorldGuard = true;
		}
		
	}
	
	public static boolean respectsWorldGuard() {
		return respectWorldGuard;
	}
	
	public static boolean isRegionProtectedFromExplosion(Player player,
			Abilities ability, Location loc) {
		if(isRegionProtectedFromBuild(player, ability, loc)) {
			return true;
		}
		
		PluginManager pm = Bukkit.getPluginManager();
		Plugin wgp = pm.getPlugin("WorldGuard");
		if (wgp != null && respectWorldGuard) {
			WorldGuardPlugin wg = (WorldGuardPlugin) wgp;
			for (Location location : new Location[] { loc, player.getLocation() }) {
				if (!player.isOnline()) {
					return true;
				}
				if(!wg.getGlobalRegionManager()
						.get(location.getWorld())
						.getApplicableRegions(location).allows(DefaultFlag.OTHER_EXPLOSION)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isRegionProtectedFromBending(Player player, Abilities ability, Location loc) {
		if (isRegionProtectedFromBuild(player, ability, loc)) {
			return true;
		}
		
		return false;
	}

	public static boolean isRegionProtectedFromBuild(Player player,
			Abilities ability, Location loc) {

		List<Abilities> ignite = new ArrayList<Abilities>();
		ignite.add(Abilities.Blaze);

		if (ability == null){
			return false;
		}
			
		if (isAllowedEverywhereAbility(ability)) {
			return false;
		}
		
		PluginManager pm = Bukkit.getPluginManager();

		Plugin wgp = pm.getPlugin("WorldGuard");

		for (Location location : new Location[] { loc, player.getLocation() }) {

			if (wgp != null && respectWorldGuard) {
				WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit
						.getPluginManager().getPlugin("WorldGuard");
				if (!player.isOnline())
					return true;

				if (ignite.contains(ability)) {
					if (!wg.hasPermission(player, "worldguard.override.lighter")) {
						if (wg.getGlobalStateManager().get(location.getWorld()).blockLighter)
							return true;
						if (!wg.getGlobalRegionManager().hasBypass(player,
								location.getWorld())
								&& !wg.getGlobalRegionManager()
										.get(location.getWorld())
										.getApplicableRegions(location)
										.allows(DefaultFlag.LIGHTER,
												wg.wrapPlayer(player)))
							return true;
					}

				}

				if ((!(wg.getGlobalRegionManager().canBuild(player, location)) || !(wg
						.getGlobalRegionManager()
						.canConstruct(player, location)))) {
					return true;
				}
			}
		}
		return false;
	}	
	
	public static boolean isAllowedEverywhereAbility(Abilities ability) {
		return allowedEverywhereAbilities.contains(ability);
	}
}

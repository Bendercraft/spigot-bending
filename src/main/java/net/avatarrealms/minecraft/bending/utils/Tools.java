package net.avatarrealms.minecraft.bending.utils;

import java.util.ArrayList;
import java.util.List;

import net.avatarrealms.minecraft.bending.controller.BendingPlayers;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.TempBackup;
import net.avatarrealms.minecraft.bending.model.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import com.massivecraft.factions.listeners.FactionsListenerMain;
import com.massivecraft.massivecore.ps.PS;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class Tools {
	public static BendingPlayers config;
	public static TempBackup tBackup;
	
	public static final long timeinterval = ConfigManager.globalCooldown;
	//public static ConcurrentHashMap<Player, Player> tempFlyers = new ConcurrentHashMap<Player, Player>();

	public Tools(BendingPlayers config2, TempBackup tb) {
		config = config2;
		tBackup = tb;
	}
	
	public static double getDistanceFromLine(Vector line, Location pointonline,
			Location point) {
		Vector AP = new Vector();
		double Ax, Ay, Az;
		Ax = pointonline.getX();
		Ay = pointonline.getY();
		Az = pointonline.getZ();

		double Px, Py, Pz;
		Px = point.getX();
		Py = point.getY();
		Pz = point.getZ();

		AP.setX(Px - Ax);
		AP.setY(Py - Ay);
		AP.setZ(Pz - Az);

		return (AP.crossProduct(line).length()) / (line.length());
	}

	public static Vector rotateVectorAroundVector(Vector axis, Vector rotator,
			double degrees) {
		double angle = Math.toRadians(degrees);
		Vector rotation = axis.clone();
		Vector rotate = rotator.clone();
		rotation = rotation.normalize();

		Vector thirdaxis = rotation.crossProduct(rotate).normalize()
				.multiply(rotate.length());

		return rotate.multiply(Math.cos(angle)).add(
				thirdaxis.multiply(Math.sin(angle)));
	}

	public static Vector getOrthogonalVector(Vector axis, double degrees,
			double length) {

		Vector ortho = new Vector(axis.getY(), -axis.getX(), 0);
		ortho = ortho.normalize();
		ortho = ortho.multiply(length);

		return rotateVectorAroundVector(axis, ortho, degrees);
	}

	public static Location getPointOnLine(Location origin, Location target,
			double distance) {
		return origin.clone().add(
				getDirection(origin, target).normalize().multiply(distance));

	}

	public static Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;

		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();

		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();

		return new Vector(x1 - x0, y1 - y0, z1 - z0);
	}

	public static boolean isRegionProtectedFromBuild(Player player,
			Abilities ability, Location loc) {

		List<Abilities> ignite = new ArrayList<Abilities>();
		ignite.add(Abilities.Blaze);
		List<Abilities> explode = new ArrayList<Abilities>();
		explode.add(Abilities.FireBlast);
		explode.add(Abilities.Lightning);

		if (ability == null && PluginTools.allowharmless)
			return false;
		if (PluginTools.isHarmlessAbility(ability) && PluginTools.allowharmless)
			return false;

		PluginManager pm = Bukkit.getPluginManager();

		Plugin wgp = pm.getPlugin("WorldGuard");
		Plugin fcp = pm.getPlugin("Factions");
		Plugin mcore = pm.getPlugin("MassiveCore");

		for (Location location : new Location[] { loc, player.getLocation() }) {

			if (wgp != null && PluginTools.respectWorldGuard) {
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

				if (explode.contains(ability)) {
					if (wg.getGlobalStateManager().get(location.getWorld()).blockTNTExplosions)
						return true;
					if (!wg.getGlobalRegionManager().get(location.getWorld())
							.getApplicableRegions(location)
							.allows(DefaultFlag.TNT))
						return true;
				}

				if ((!(wg.getGlobalRegionManager().canBuild(player, location)) || !(wg
						.getGlobalRegionManager()
						.canConstruct(player, location)))) {
					return true;
				}
			}

			if (fcp != null && mcore != null && PluginTools.respectFactions) {
				if (ignite.contains(ability)) {
				}

				if (explode.contains(ability)) {
				}

				if (!FactionsListenerMain.canPlayerBuildAt(player,
						PS.valueOf(loc.getBlock()), false)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void playFocusWaterEffect(Block block) {
		block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4, 20);
	}

	public static BlockFace getCardinalDirection(Vector vector) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.NORTH_EAST,
				BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
				BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
		Vector n, ne, e, se, s, sw, w, nw;
		w = new Vector(-1, 0, 0);
		n = new Vector(0, 0, -1);
		s = n.clone().multiply(-1);
		e = w.clone().multiply(-1);
		ne = n.clone().add(e.clone()).normalize();
		se = s.clone().add(e.clone()).normalize();
		nw = n.clone().add(w.clone()).normalize();
		sw = s.clone().add(w.clone()).normalize();

		Vector[] vectors = { n, ne, e, se, s, sw, w, nw };

		double comp = 0;
		int besti = 0;
		for (int i = 0; i < vectors.length; i++) {
			double dot = vector.dot(vectors[i]);
			if (dot > comp) {
				comp = dot;
				besti = i;
			}
		}
		return faces[besti];
	}

	public static int getIntCardinalDirection(Vector vector) {
		BlockFace face = getCardinalDirection(vector);
		switch (face) {
		case SOUTH:
			return 7;
		case SOUTH_WEST:
			return 6;
		case WEST:
			return 3;
		case NORTH_WEST:
			return 0;
		case NORTH:
			return 1;
		case NORTH_EAST:
			return 2;
		case EAST:
			return 5;
		case SOUTH_EAST:
			return 8;
		default:
			break;
		}
		return 4;
	}
	
	public static boolean isDay(World world) {
		long time = world.getTime();
		if (time >= 23500 || time <= 12500) {
			return true;
		}
		return false;
	}

	public static boolean isNight(World world) {
		if (world.getEnvironment() == Environment.NETHER
				|| world.getEnvironment() == Environment.THE_END) {
			return false;
		}
		long time = world.getTime();
		if (time >= 12950 && time <= 23050) {
			return true;
		}
		return false;
	}
}

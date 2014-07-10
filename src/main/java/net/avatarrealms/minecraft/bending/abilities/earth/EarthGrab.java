package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EarthGrab implements IAbility {

	private static double range = ConfigManager.earthGrabRange;
	private static Map<Integer, EarthGrab> instances = new HashMap<Integer, EarthGrab>();
	private static Integer ID = Integer.MIN_VALUE;
	private static int benderTargettedDuration = 100; // 5 secs
	private static int otherTargettedDuration = 6000; // 5 minutes

	private int id;
	private List<EarthColumn> columns = new ArrayList<EarthColumn>();
	private boolean self;
	private BendingPlayer bPlayer;
	private Player bender;
	private LivingEntity target;
	private IAbility parent;
	private Location origin;
	private long time = 0;
	private boolean toKeep = true;

	public EarthGrab(Player player, boolean self, IAbility parent) {
		this.parent = parent;
		// Tools.verbose("initiating");
		this.self = self;
		this.bender = player;
		bPlayer = BendingPlayer.getBendingPlayer(bender);
		if (bPlayer.isOnCooldown(Abilities.EarthGrab))
			return;

		if (self) {
			grabEntity(bender, bender);
		} else {
			Location origin = player.getEyeLocation();
			Vector direction = origin.getDirection();
			double lowestdistance = range + 1;
			Entity closestentity = null;
			for (Entity entity : EntityTools.getEntitiesAroundPoint(origin,
					range)) {
				if (Tools.getDistanceFromLine(direction, origin,
						entity.getLocation()) <= 3
						&& (entity instanceof LivingEntity)
						&& (entity.getEntityId() != bender.getEntityId())) {
					double distance = origin.distance(entity.getLocation());
					if (distance < lowestdistance) {
						closestentity = entity;
						lowestdistance = distance;
					}
				}
			}
			grabEntity(bender, closestentity);
		}
		if (target != null) {
			if (ID == Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			}
			id = ID++;
			instances.put(id, this);
		}

	}

	public void grabEntity(Player player, Entity entity) {
		if (entity != null) {
			if (entity instanceof LivingEntity) {
				int cpt = 0;
				target = (LivingEntity) entity;
				time = System.currentTimeMillis();
				toKeep = true;

				double x = (int) target.getLocation().getX();
				double y = (int) target.getLocation().getY();
				double z = (int) target.getLocation().getZ();

				x = (x < 0) ? x - 0.5 : x + 0.5;

				z = (z < 0) ? z - 0.5 : z + 0.5;

				origin = new Location(entity.getLocation().getWorld(), x, y, z);

				target.teleport(origin);
				// To be sure the guy is locked in the grab

				Location cLoc[] = new Location[4];

				cLoc[0] = origin.clone().add(0, 0, -1);
				cLoc[1] = origin.clone().add(0, 0, 1);
				cLoc[2] = origin.clone().add(-1, 0, 0);
				cLoc[3] = origin.clone().add(1, 0, 0);

				for (int i = 0; i < 4; i++) {
					if (cLoc[i].getBlock().getType() == Material.AIR
							|| cLoc[i].getBlock().getType() == Material.WATER
							|| cLoc[i].getBlock().getType() == Material.STATIONARY_WATER
							|| cLoc[i].getBlock().getType() == Material.LAVA
							|| cLoc[i].getBlock().getType() == Material.STATIONARY_LAVA
							|| BlockTools.isPlant(cLoc[i].getBlock())) {

						// Bending.log.info("Can column");
						cLoc[i].add(0, -1, 0);
						if (BlockTools.isEarthbendable(player,
								cLoc[i].getBlock())) {
							cpt++;
							columns.add(new EarthColumn(player, cLoc[i], 1,
									this, this));
						} else if (cLoc[i].getBlock().getType() == Material.AIR) {
							cLoc[i].add(0, -1, 0);
							if (BlockTools.isEarthbendable(player,
									cLoc[i].getBlock())) {
								cpt++;
								columns.add(new EarthColumn(player, cLoc[i], 2,
										this, this));
							}
						}

					} else if (BlockTools.isEarthbendable(bender,
							cLoc[i].getBlock())) {
						Bending.log.info("Cannot column but bendable");
						cpt++;
					}
				}

				if (cpt >= 4) {
					int duration;
					Bending.log.info("Can effect");
					if (self) {
						duration = benderTargettedDuration;
					} else {
						duration = otherTargettedDuration; // 5 minutes
					}
					PotionEffect slowness = new PotionEffect(
							PotionEffectType.SLOW, duration, 150); // The entity
																	// cannot
																	// move
					PotionEffect jumpless = new PotionEffect(
							PotionEffectType.JUMP, duration, 150); // The entity
																	// cannot
																	// jump
					target.addPotionEffect(slowness);
					target.addPotionEffect(jumpless);

					if (target instanceof Player
							&& target.getEntityId() != bender.getEntityId()) {
						EntityTools.grab((Player) target, time);
					}
					bPlayer.cooldown(Abilities.EarthGrab);
					bPlayer.earnXP(BendingType.Earth, this);
				}
			}
		}
	}

	public static String getDescription() {
		return "To use, simply left-click while targeting a creature within range. "
				+ "This ability will erect a circle of earth to trap the creature in.";
	}

	public void setToKeep(boolean k) {
		toKeep = k;
	}

	public boolean progress() {
		Location loc = target.getLocation();

		if (!toKeep) {
			Bending.log.info("toKeep : false");
			return false;
		}

		if (target == null) {
			// Bending.log.info("Target : false");
			return false;
		}
		if (bender.getEntityId() == target.getEntityId()) {
			if (System.currentTimeMillis() > time
					+ ((benderTargettedDuration / 20) * 1000)) {
				// Bending.log.info("Time : false");
				return false;
			}
		}

		if (loc.getWorld() != origin.getWorld()) {
			// Bending.log.info("World : false");
			return false;
		}
		if ((int) loc.getX() != (int) origin.getX()) {
			// Bending.log.info("X : false");
			return false;
		}
		if ((int) loc.getZ() != (int) origin.getZ()) {
			// Bending.log.info("Z : false");
			return false;
		}
		if ((int) loc.getY() != (int) origin.getY()) {
			// Bending.log.info("Y : false");
			return false;
		}

		if (!BlockTools.isEarthbendable(bender, loc.add(0, 0, -1).getBlock())) {
			Bending.log.info("North : false");
			return false;
		}
		if (!BlockTools.isEarthbendable(bender, loc.add(0, 0, +2).getBlock())) {
			Bending.log.info("South : false");
			return false;
		}
		if (!BlockTools.isEarthbendable(bender, loc.add(-1, 0, -1).getBlock())) {
			Bending.log.info("West : false");
			return false;
		}
		if (!BlockTools.isEarthbendable(bender, loc.add(+2, 0, 0).getBlock())) {
			Bending.log.info("East : false");
			return false;
		}
		return true;
	}

	public static void progressAll() {
		List<Integer> toRemove = new LinkedList<Integer>();
		boolean keep;
		for (Integer iD : instances.keySet()) {
			keep = instances.get(iD).progress();
			if (!keep) {
				toRemove.add(iD);
			}
		}

		for (Integer iD : toRemove) {
			instances.get(iD).revertEarthGrab();
			instances.remove(iD);
		}
	}

	public boolean revertEarthGrab() {
		for (EarthColumn column : columns) {
			for (Block block : column.getAffectedBlocks()) {
				BlockTools.revertBlock(block);
			}
		}
		columns.clear();

		if (target != null) {
			target.removePotionEffect(PotionEffectType.SLOW);
			target.removePotionEffect(PotionEffectType.JUMP);
			if (target instanceof Player) {
				EntityTools.unGrab((Player) target);
			}
		}
		return true;
	}

	public static EarthGrab blockInEarthGrab(Block block) {
		for (EarthGrab grab : instances.values()) {
			if (grab.locInEarthGrab(block.getLocation())) {
				return grab;
			}
		}
		return null;
	}

	public boolean locInEarthGrab(Location loc) {
		int x = (int) loc.getX();
		int y = (int) loc.getY();
		int z = (int) loc.getZ();
		
		if (z < 0) {
			z++;
		}
		if (x < 0) {
			x++;
		}
		
		if (origin.getWorld() == loc.getWorld()) {
			int lY = (int) origin.getY();
			int lX = (int) origin.getX();
			int lZ = (int) origin.getZ();
			
			lZ++; // South
			if (lX == x && lY == y && lZ == z) {
				return true;
			}
			Bending.log.info( "Not South \n"
					+ x + " != " + lX + "\n"
					+ y + " != " + lY + "\n"
					+ z + " != " + lZ + "");
			
			lZ-=2; // North
			if (lX == x && lY == y && lZ == z) {
				return true;
			}
			Bending.log.info( "Not North \n"
					+ x + " != " + lX + "\n"
					+ y + " != " + lY + "\n"
					+ z + " != " + lZ + "");
			
			lZ++; lX ++; // East
			if (lX == x && lY == y && lZ == z) {
				return true;
			}
			Bending.log.info( "Not East \n"
					+ x + " != " + lX + "\n"
					+ y + " != " + lY + "\n"
					+ z + " != " + lZ + "");

			lX-= 2; // West
			if (lX == x && lY == y && lZ == z) {
				return true;
			}
			Bending.log.info( "Not West \n"
					+ x + " != " + lX + "\n"
					+ y + " != " + lY + "\n"
					+ z + " != " + lZ + "");
		}

		return false;
	}

	@Override
	public int getBaseExperience() {
		return 4;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

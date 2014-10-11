package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EarthBlast implements IAbility {
	private static Map<Integer, EarthBlast> instances = new HashMap<Integer, EarthBlast>();

	private static boolean hitself = ConfigManager.earthBlastHitSelf;
	private static double preparerange = ConfigManager.earthBlastPrepareRange;
	private static double range = ConfigManager.earthBlastRange;
	private static int earthDamage = ConfigManager.earthBlastDamage;
	private static final int sandDamage = 5;
	private static int speDamage = ConfigManager.ironBlastDamage;
	private int damage;
	private static double speed = ConfigManager.earthBlastSpeed;
	private static final double deflectrange = 3;

	private static boolean revert = ConfigManager.earthBlastRevert;
	private static double pushfactor = ConfigManager.earthBlastPush;
	// private static double speed = 1.5;

	private static long interval = (long) (1000. / speed);

	private static int ID = Integer.MIN_VALUE;

	private Player player;
	private int id;
	private Location location = null;
	private Block sourceblock = null;
	private Material sourcetype = null;
	private boolean progressing = false;
	private Location destination = null;
	private Location firstdestination = null;
	private boolean falling = false;
	private long time;
	private boolean settingup = true;
	private IAbility parent;

	public EarthBlast(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		if (prepare()) {
			id = ID++;
			if (ID >= Integer.MAX_VALUE)
				ID = Integer.MIN_VALUE;
			instances.put(id, this);
			time = System.currentTimeMillis();
		}

	}

	public boolean prepare() {
		Block block = BlockTools.getEarthSourceBlock(player, Abilities.EarthBlast, preparerange);
		cancelPrevious(block);	
		block(player);
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		return false;
	}

	private static Location getTargetLocation(Player player) {
		Entity target = EntityTools.getTargettedEntity(player, range);
		Location location;
		if (target == null) {
			location = EntityTools.getTargetedLocation(player, range);
		} else {
			// targetting = true;
			location = ((LivingEntity) target).getEyeLocation();
			// location.setY(location.getY() - 1);
		}
		return location;
	}

	private void cancelPrevious(Block b) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (EarthBlast blast : instances.values()) {
			if ((blast.sourceblock.equals(b) || blast.player == player) && !blast.progressing) {
				blast.cancel();
				toRemove.add(blast);
			}
		}
		for (EarthBlast blast : toRemove) {
			blast.remove();
		}
	}

	/**
	 * Should remove() after this method
	 */
	public void cancel() {
		sourceblock.setType(sourcetype);
	}

	private void focusBlock() {
		
		if (EarthPassive.isPassiveSand(sourceblock)) {
			EarthPassive.revertSand(sourceblock);
		}		
		sourcetype = sourceblock.getType();
		damage = earthDamage;
		if (sourcetype == Material.SAND) {
			sourceblock.setType(Material.SANDSTONE);
			damage = sandDamage;
		} else if (sourcetype == Material.STONE) {
			sourceblock.setType(Material.COBBLESTONE);
		} else {
			if (EntityTools.canBend(player, Abilities.MetalBending)
					&& BlockTools.isIronBendable(player, sourceblock.getType())) {
				if (sourcetype == Material.IRON_BLOCK){
					sourceblock.setType(Material.IRON_ORE);
				}
				else {
					sourceblock.setType(Material.IRON_BLOCK);
				}
				damage = speDamage;
			}
			else if (EntityTools.canBend(player, Abilities.LavaTrain)
					&& sourceblock.getType() == Material.OBSIDIAN) {
				damage = speDamage;
				sourceblock.setType(Material.BEDROCK);
			}
			else {
				sourceblock.setType(Material.STONE);
			}
			
		}
		
		

		location = sourceblock.getLocation();
	}

	private void remove() {
		instances.remove(id);
	}

	public void throwEarth() {
		if (sourceblock != null) {		
			if (sourceblock.getWorld() == player.getWorld()) {
				if (BlockTools.movedEarth.containsKey(sourceblock)) {
					if (!revert)
						BlockTools.removeRevertIndex(sourceblock);
					// Tools.removeEarthbendedBlockIndex(sourceblock);
				}
				Entity target = EntityTools.getTargettedEntity(player, range);
				// Tools.verbose(target);
				if (target == null) {
					destination = EntityTools.getTargetBlock(player, range, BlockTools.getTransparentEarthbending()).getLocation();
					firstdestination = sourceblock.getLocation().clone();
					firstdestination.setY(destination.getY());
				} else {
					destination = ((LivingEntity) target).getEyeLocation();
					firstdestination = sourceblock.getLocation().clone();
					firstdestination.setY(destination.getY());
					destination = Tools.getPointOnLine(firstdestination,
							destination, range);
				}

				if (destination.distance(location) <= 1) {
					progressing = false;
					destination = null;
				} else {
					progressing = true;
					sourceblock.getWorld().playEffect(
							sourceblock.getLocation(), Effect.GHAST_SHOOT, 0,
							10);
					// direction = getDirection().normalize();
					if (sourcetype != Material.SAND
							&& sourcetype != Material.GRAVEL) {
						sourceblock.setType(sourcetype);
					}
				}
			}

		}
	}

	public static EarthBlast getBlastFromSource(Block block) {
		for (int id : instances.keySet()) {
			EarthBlast blast = instances.get(id);
			if (blast.sourceblock.equals(block))
				return blast;
		}
		return null;
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()
				|| !EntityTools.canBend(player, Abilities.EarthBlast)) {
			breakBlock();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (falling) {
				breakBlock();
				return false;
			}

			if (!BlockTools.isEarthbendable(player, Abilities.EarthBlast, sourceblock)
					&& sourceblock.getType() != Material.COBBLESTONE
					&& sourceblock.getType() != Material.SANDSTONE
					&& sourceblock.getType() != Material.BEDROCK) {
				return false;
			}

			if (!progressing && !falling) {

				if (EntityTools.getBendingAbility(player) != Abilities.EarthBlast) {
					cancel();
					return false;
				}
				if (sourceblock == null) {
					return false;
				}
				if (player.getWorld() != sourceblock.getWorld()) {
					cancel();
					return false;
				}
				if (sourceblock.getLocation().distance(player.getLocation()) > preparerange) {
					cancel();
					return false;
				}
			}

			if (falling) {
				breakBlock();
				return false;
			} else {
				if (!progressing) {
					return true;
				}

				if (sourceblock.getY() == firstdestination.getBlockY())
					settingup = false;

				Vector direction;
				if (settingup) {
					direction = Tools.getDirection(location, firstdestination)
							.normalize();
				} else {
					direction = Tools.getDirection(location, destination)
							.normalize();
				}

				location = location.clone().add(direction);

				PluginTools.removeSpouts(location, player);

				Block block = location.getBlock();
				if (block.getLocation().equals(sourceblock.getLocation())) {
					location = location.clone().add(direction);
					block = location.getBlock();
				}

				if (BlockTools.isTransparentToEarthbending(player, block)
						&& !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else if (!settingup) {
					breakBlock();
					return false;
				} else {
					location = location.clone().subtract(direction);
					direction = Tools.getDirection(location, destination)
							.normalize();
					location = location.clone().add(direction);

					PluginTools.removeSpouts(location, player);
					double radius = FireBlast.affectingradius;
					Player source = player;
					if (EarthBlast.shouldAnnihilateBlasts(location, radius, source, false)
							|| WaterManipulation.annihilateBlasts(location,
									radius, source)
							|| FireBlast.annihilateBlasts(location, radius,
									source)) {
						breakBlock();
						return false;
					}

					Block block2 = location.getBlock();
					if (block2.getLocation().equals(sourceblock.getLocation())) {
						location = location.clone().add(direction);
						block2 = location.getBlock();
					}

					if (BlockTools.isTransparentToEarthbending(player, block)
							&& !block.isLiquid()) {
						BlockTools.breakBlock(block);
					} else {
						breakBlock();
						return false;
					}
				}
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location,
						FireBlast.affectingradius)) {
					if (PluginTools.isRegionProtectedFromBuild(player,
							Abilities.EarthBlast, entity.getLocation())) {
						continue;
					}
						
					if ((entity.getEntityId() != player.getEntityId() || hitself)) {
						Location location = player.getEyeLocation();
				 		Vector vector = location.getDirection();
				 		entity.setVelocity(vector.normalize().multiply(pushfactor));
				 		EntityTools.damageEntity(player, entity, damage);
						progressing = false;
					}
				}

				if (!progressing) {
					breakBlock();
					return false;
				}

				if (revert) {
					// Tools.addTempEarthBlock(sourceblock, block);
					sourceblock.setType(sourcetype);
					BlockTools.moveEarthBlock(sourceblock, block);
					if (block.getType() == Material.SAND) {
						block.setType(Material.SANDSTONE);
					}
						
					if (block.getType() == Material.GRAVEL) {
						block.setType(Material.STONE);
					}
						
				} else {
					block.setType(sourceblock.getType());
					sourceblock.setType(Material.AIR);
				}

				sourceblock = block;

				if (location.distance(destination) < 1) {
					if (sourcetype == Material.SAND
							|| sourcetype == Material.GRAVEL) {
						progressing = false;
						sourceblock.setType(sourcetype);
					}
					falling = true;
					progressing = false;
				}
				return true;
			}
		}
		return true;
	}

	/**
	 * Should remove() after this method
	 */
	private void breakBlock() {
		sourceblock.setType(sourcetype);
		if (revert) {
			BlockTools.addTempAirBlock(sourceblock);
		} else {
			sourceblock.breakNaturally();
		}
	}

	public static void throwEarth(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		ArrayList<EarthBlast> ignore = new ArrayList<EarthBlast>();

		if (!bPlayer.isOnCooldown(Abilities.EarthBlast)) {

			boolean cooldown = false;
			for (int id : instances.keySet()) {
				EarthBlast blast = instances.get(id);
				if (blast.player == player && !blast.progressing) {
					blast.throwEarth();
					cooldown = true;
					ignore.add(blast);
				}
			}

			if (cooldown) {
				bPlayer.cooldown(Abilities.EarthBlast);
			}
		}

		redirectTargettedBlasts(player, ignore);
	}

	public static void progressAll() {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for(EarthBlast blast : instances.values()) {
			boolean keep = blast.progress();
			if(!keep) {
				toRemove.add(blast);
			}
		}
		for(EarthBlast blast : toRemove) {
			blast.remove();
		}
	}

	public static void removeAll() {
		for(EarthBlast blast : instances.values()) {
			blast.breakBlock();
		}
		instances.clear();
	}

	private static void redirectTargettedBlasts(Player player,
			ArrayList<EarthBlast> ignore) {
		for(EarthBlast blast : instances.values()) {
			if (!blast.progressing || ignore.contains(blast))
				continue;

			if (!blast.location.getWorld().equals(player.getWorld()))
				continue;

			if (PluginTools.isRegionProtectedFromBuild(player, Abilities.EarthBlast,
					blast.location))
				continue;

			if (blast.player.equals(player))
				blast.redirect(player, getTargetLocation(player));

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= range
					&& Tools.getDistanceFromLine(vector, location,
							blast.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				blast.redirect(player, getTargetLocation(player));
			}

		}
	}

	private void redirect(Player player, Location targetlocation) {
		if (progressing) {
			if (location.distance(player.getLocation()) <= range) {
				settingup = false;
				destination = targetlocation;
			}
		}
	}

	private static void block(Player player) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (EarthBlast blast : instances.values()) {
			if (blast.player.equals(player))
				continue;

			if (!blast.location.getWorld().equals(player.getWorld()))
				continue;

			if (!blast.progressing)
				continue;

			if (PluginTools.isRegionProtectedFromBuild(player, Abilities.EarthBlast,
					blast.location))
				continue;

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= range
					&& Tools.getDistanceFromLine(vector, location,
							blast.location) < deflectrange
					&& mloc.distance(location.clone().add(vector)) < mloc
							.distance(location.clone().add(
									vector.clone().multiply(-1)))) {
				blast.breakBlock();
				toRemove.add(blast);
			}
		}
		for(EarthBlast blast : toRemove) {
			blast.remove();
		}
	}

	public static void removeAroundPoint(Location location, double radius) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (EarthBlast blast : instances.values()) {
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distance(location) <= radius) {
					blast.breakBlock();
					toRemove.add(blast);
				}
			}
		}
		for(EarthBlast blast : toRemove) {
			blast.remove();
		}
	}
	
	public static boolean shouldAnnihilateBlasts(Location location, double radius,
			Player source, boolean remove) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		boolean broke = false;
		for (EarthBlast blast : instances.values()) {
			if (blast.location.getWorld().equals(location.getWorld())
					&& !source.equals(blast.player))
				if (blast.location.distance(location) <= radius) {
					blast.breakBlock();
					broke = true;
					toRemove.add(blast);
				}
		}
		if(remove) {
			for(EarthBlast blast : toRemove) {
				blast.remove();
			}
		}
		return broke;
	}

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		return shouldAnnihilateBlasts(location, radius, source, true);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

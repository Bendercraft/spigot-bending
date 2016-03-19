package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * State Prepared = block is selected but still not thrown State Progressing =
 * block is thrown
 *
 * @author Koudja
 */
@ABendingAbility(name = EarthBlast.NAME, element = BendingElement.EARTH)
public class EarthBlast extends BendingActiveAbility {
	public final static String NAME = "EarthBlast";
	
	@ConfigurationParameter("Hit-Self")
	private static boolean HITSELF = false;

	@ConfigurationParameter("Select-Range")
	private static double SELECT_RANGE = 11;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Earth-Damage")
	private static int EARTH_DAMAGE = 7;

	@ConfigurationParameter("Sand-Damage")
	private static int SAND_DAMAGE = 5;

	@ConfigurationParameter("Iron-Damage")
	private static int IRON_DAMAGE = 9;

	@ConfigurationParameter("Speed")
	private static double SPEED = 35;

	@ConfigurationParameter("Push-Factor")
	private static double PUSHFACTOR = 0.3;

	@ConfigurationParameter("Revert")
	private static long COOLDOWN = 500;

	private static final double DEFLECT_RANGE = 3;

	private long interval;

	private static int ID = Integer.MIN_VALUE;

	private int id;
	private Location location = null;
	private TempBlock source;
	private Block sourceBlock = null;
	private Material sourceType = null;
	private Location destination = null;
	private Location firstDestination = null;
	private long time;
	private boolean settingUp = true;

	private int damage;

	public EarthBlast(RegisteredAbility register, Player player) {
		super(register, player);

		this.id = ID++;
		this.time = this.startedTime;

		double speed = SPEED;
		if (this.bender.hasPath(BendingPath.RECKLESS)) {
			speed *= 0.8;
		}

		this.interval = (long) (1000. / speed);
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.START 
				&& getState() != BendingAbilityState.PREPARING 
				&& getState() != BendingAbilityState.PREPARED) {
			return true;
		}
		
		cancel();
		Block block = BlockTools.getEarthSourceBlock(player, NAME, SELECT_RANGE);

		setState(BendingAbilityState.PREPARING);

		block(this.player);
		if (block != null) {
			sourceBlock = block;
			sourceType = sourceBlock.getType();
			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}
			damage = EARTH_DAMAGE;
			if (block.getType() == Material.SAND) {
				//this.source = new TempBlock(sourceBlock, Material.SANDSTONE);
				source = TempBlock.makeTemporary(sourceBlock, Material.SANDSTONE, false);
				damage = SAND_DAMAGE;
			}
			else if (block.getType() == Material.STONE) {
				//this.source = new TempBlock(sourceBlock, Material.COBBLESTONE);
				source = TempBlock.makeTemporary(sourceBlock, Material.COBBLESTONE, false);
			} else {
				if (EntityTools.canBend(this.player, MetalBending.NAME) && BlockTools.isIronBendable(player, sourceType)) {
					if (block.getType() == Material.IRON_BLOCK) {
						//this.source = new TempBlock(sourceBlock, Material.IRON_ORE);
						source = TempBlock.makeTemporary(sourceBlock, Material.IRON_ORE, false);
					} else {
						//this.source = new TempBlock(sourceBlock, Material.IRON_BLOCK);
						source = TempBlock.makeTemporary(sourceBlock, Material.IRON_BLOCK, false);
					}
					damage = IRON_DAMAGE;
				} else if (EntityTools.canBend(this.player, LavaTrain.NAME) && (sourceType == Material.OBSIDIAN)) {
					damage = IRON_DAMAGE;
					//this.source = new TempBlock(sourceBlock, Material.BEDROCK);
					source = TempBlock.makeTemporary(sourceBlock, Material.BEDROCK, false);
				} else {
					//this.source = new TempBlock(sourceBlock, Material.STONE);
					source = TempBlock.makeTemporary(sourceBlock, Material.STONE, false);
				}

			}
			location = sourceBlock.getLocation();

			if (bender.hasPath(BendingPath.TOUGH)) {
				damage *= 0.85;
			}
			if (bender.hasPath(BendingPath.RECKLESS)) {
				damage *= 1.15;
			}

			setState(BendingAbilityState.PREPARED);
		}
		return false;
	}

	@Override
	public boolean swing() {
		List<EarthBlast> ignore = null;
		if (!this.bender.isOnCooldown(NAME)) {
			if (getState() == BendingAbilityState.PREPARED) {
				throwEarth();
				this.bender.cooldown(NAME, COOLDOWN);
				ignore = Collections.singletonList(this);
			}
		}
		if (ignore == null) {
			ignore = new LinkedList<EarthBlast>();
		}

		redirectTargetedBlasts(this.player, ignore);
		return false;
	}

	private static Location getTargetLocation(Player player) {
		Entity target = EntityTools.getTargetedEntity(player, RANGE);
		Location location;
		if (target == null) {
			location = EntityTools.getTargetedLocation(player, RANGE);
		} else {
			// targetting = true;
			location = ((LivingEntity) target).getEyeLocation();
			// location.setY(location.getY() - 1);
		}
		return location;
	}

	/**
	 * Should remove() after this method
	 */
	public void cancel() {
		if(source != null) {
			source.revertBlock();
			source = null;
		}
	}

	public void throwEarth() {
		if (sourceBlock == null) {
			return;
		}

		if (sourceBlock.getWorld() != player.getWorld()) {
			return;
		}

		LivingEntity target = EntityTools.getTargetedEntity(player, RANGE);
		if (target == null) {
			destination = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
			firstDestination = sourceBlock.getLocation().clone();
			firstDestination.setY(this.destination.getY());
		} else {
			destination = target.getEyeLocation();
			firstDestination = sourceBlock.getLocation().clone();
			firstDestination.setY(destination.getY());
			destination = Tools.getPointOnLine(firstDestination, destination, RANGE);
		}

		if (destination.distance(location) <= 1) {
			destination = null;
			remove();
			return;
		} else {
			setState(BendingAbilityState.PROGRESSING);
			sourceBlock.getWorld().playEffect(sourceBlock.getLocation(), Effect.GHAST_SHOOT, 0, 10);
			if (source != null && (source.getState().getType() != Material.SAND) && (source.getState().getType() != Material.GRAVEL)) {
				source.revertBlock();
				source = null;
			}
		}
	}

	@Override
	public void progress() {
		if (getState().isBefore(BendingAbilityState.PREPARED)) {
			remove();
			return;
		}

		if ((System.currentTimeMillis() - time) >= interval) {
			time = System.currentTimeMillis();
			
			if (getState() == BendingAbilityState.PREPARED) {
				if (!NAME.equals(EntityTools.getBendingAbility(player))) {
					cancel();
					remove();
					return;
				}
				if (sourceBlock == null) {
					remove();
					return;
				}
				if (player.getWorld() != sourceBlock.getWorld()) {
					cancel();
					remove();
					return;
				}
				if (sourceBlock.getLocation().distance(player.getLocation()) > SELECT_RANGE) {
					cancel();
					remove();
					return;
				}
				return;
			}

			if (sourceBlock.getY() == firstDestination.getBlockY()) {
				settingUp = false;
			}

			Vector direction;
			if (settingUp) {
				direction = Tools.getDirection(location, firstDestination).normalize();
			} else {
				direction = Tools.getDirection(location, destination).normalize();
			}

			location = location.clone().add(direction);

			PluginTools.removeSpouts(location, player);

			Block block = location.getBlock();
			if (block.getLocation().equals(sourceBlock.getLocation())) {
				location = location.clone().add(direction);
				block = location.getBlock();
			}

			if (BlockTools.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
				BlockTools.breakBlock(block);
			} else if (!settingUp) {
				remove();
				return;
			} else {
				location = location.clone().subtract(direction);
				direction = Tools.getDirection(location, destination).normalize();
				location = location.clone().add(direction);

				PluginTools.removeSpouts(location, player);
				double radius = FireBlast.AFFECTING_RADIUS;
				Player source = player;
				if (EarthBlast.shouldAnnihilateBlasts(location, radius, source) 
						|| WaterManipulation.annihilateBlasts(location, radius, source) 
						|| FireBlast.annihilateBlasts(location, radius, source)) {
					remove();
					return;
				}

				Block block2 = location.getBlock();
				if (block2.getLocation().equals(sourceBlock.getLocation())) {
					location = location.clone().add(direction);
					block2 = location.getBlock();
				}

				if (BlockTools.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else {
					remove();
					return;
				}
			}
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, FireBlast.AFFECTING_RADIUS)) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (ProtectionManager.isLocationProtectedFromBending(player, NAME, entity.getLocation())) {
					continue;
				}

				if (((entity.getEntityId() != player.getEntityId()) || HITSELF)) {
					Location location = player.getEyeLocation();
					Vector vector = location.getDirection();
					entity.setVelocity(vector.normalize().multiply(PUSHFACTOR));
					EntityTools.damageEntity(bender, entity, damage);
					remove();
					return;
				}
			}

			if (getState() != BendingAbilityState.PROGRESSING) {
				remove();
				return;
			}

			if(source != null) {
				source.revertBlock();
				source = null;
			}
			BlockTools.moveEarthBlock(sourceBlock, block);
			if (block.getType() == Material.SAND) {
				block.setType(Material.SANDSTONE);
			}

			if (block.getType() == Material.GRAVEL) {
				block.setType(Material.STONE);
			}

			sourceBlock = block;

			if (location.distance(destination) < 1) {
				if (source != null && ((source.getState().getType() == Material.SAND) || (source.getState().getType() == Material.GRAVEL))) {
					source.revertBlock();
					source = null;
				}
				remove();
				return;
			}
		}
	}

	private static void redirectTargetedBlasts(Player player, List<EarthBlast> ignore) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if ((blast.getState() != BendingAbilityState.PROGRESSING) || ignore.contains(blast)) {
				continue;
			}

			if (!blast.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (ProtectionManager.isLocationProtectedFromBending(player, NAME, blast.location)) {
				continue;
			}

			if (blast.player.equals(player)) {
				blast.redirect(player, getTargetLocation(player));
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mLoc = blast.location;
			if ((mLoc.distance(location) <= RANGE) && (Tools.getDistanceFromLine(vector, location, blast.location) < DEFLECT_RANGE) && (mLoc.distance(location.clone().add(vector)) < mLoc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				blast.redirect(player, getTargetLocation(player));
			}

		}
	}

	private void redirect(Player player, Location targetlocation) {
		if (getState() == BendingAbilityState.PROGRESSING) {
			if (location.distance(player.getLocation()) <= RANGE) {
				settingUp = false;
				destination = targetlocation;
			}
		}
	}

	private static void block(Player player) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.player.equals(player)) {
				continue;
			}

			if (blast.location == null || !blast.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (blast.getState() == BendingAbilityState.PREPARED) {
				continue;
			}

			if (ProtectionManager.isLocationProtectedFromBending(player, NAME, blast.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if ((mloc.distance(location) <= RANGE) && (Tools.getDistanceFromLine(vector, location, blast.location) < DEFLECT_RANGE) && (mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				toRemove.add(blast);
			}
		}
		for (EarthBlast blast : toRemove) {
			blast.remove();
		}
	}

	public static void removeAroundPoint(Location location, double radius) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distance(location) <= radius) {
					toRemove.add(blast);
				}
			}
		}
		for (EarthBlast blast : toRemove) {
			blast.remove();
		}
	}

	public static boolean shouldAnnihilateBlasts(Location location, double radius, Player source) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		boolean broke = false;
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if(blast == null || blast.location == null || blast.player == null) {
				continue;
			}
			if (blast.location.getWorld().equals(location.getWorld()) && !source.equals(blast.player)) {
				if (blast.location.distance(location) <= radius) {
					broke = true;
					toRemove.add(blast);
				}
			}
		}
		for (EarthBlast blast : toRemove) {
			blast.remove();
		}
		return broke;
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		return shouldAnnihilateBlasts(location, radius, source);
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public void stop() {
		if(source != null) {
			source.revertBlock();
			source = null;
		}
		if(sourceBlock != null) {
			BlockTools.addTempAirBlock(sourceBlock);
		}
	}
}

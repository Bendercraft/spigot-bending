package net.bendercraft.spigot.bending.abilities.earth;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

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

	@ConfigurationParameter("Deflect-Range")
	private static double DEFLECT_RANGE = 3;

	private long interval;

	private UUID uuid;
	
	private Location location; // For accurate advance
	private TempBlock current; // To revert each time we change, also holds source selection if not yet thrown
	
	private Material type; // What type of block is this earhblast made of ?
	
	private boolean settingUp = true; // Are we trying to reach first destination ?
	private Location firstDestination; // First destination (raising block)
	private Location destination; // Final destination
	
	private long time;
	private int damage;

	public EarthBlast(RegisteredAbility register, Player player) {
		super(register, player);

		this.uuid = UUID.randomUUID();

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
		
		// Player is selecting a block as source
		if(isState(BendingAbilityState.START) 
				|| isState(BendingAbilityState.PREPARED)) {
			block(player); // Despite EarthBlast being allowed to have multiple instance for same player, when selecting we do NOT want that behavior
			
			Block block = BlockTools.getEarthSourceBlock(player, register, SELECT_RANGE);
			if (block != null) {
				if (EarthPassive.isPassiveSand(block)) {
					EarthPassive.revertSand(block);
				}
				
				type = block.getType();
				location = block.getLocation().clone();
				
				damage = EARTH_DAMAGE;
				if (block.getType() == Material.SAND) {
					current = TempBlock.makeTemporary(block, Material.SANDSTONE, false);
					damage = SAND_DAMAGE;
				} else if (block.getType() == Material.STONE) {
					current = TempBlock.makeTemporary(block, Material.COBBLESTONE, false);
				} else if (EntityTools.canBend(this.player, MetalBending.NAME) && BlockTools.isIronBendable(player, type)) {
					if (block.getType() == Material.IRON_BLOCK) {
						current = TempBlock.makeTemporary(block, Material.IRON_ORE, false);
					} else {
						current = TempBlock.makeTemporary(block, Material.IRON_BLOCK, false);
					}
					damage = IRON_DAMAGE;
				} else if (EntityTools.canBend(this.player, LavaTrain.NAME) && (type == Material.OBSIDIAN)) {
					damage = IRON_DAMAGE;
					current = TempBlock.makeTemporary(block, Material.BEDROCK, false);
				} else {
					current = TempBlock.makeTemporary(block, Material.STONE, false);
				}

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
		return true;
	}

	@Override
	public boolean swing() {
		// A swing could both send a prepared earthblast or redirect others
		List<EarthBlast> ignore = null;
		if (!bender.isOnCooldown(NAME)) {
			if (getState() == BendingAbilityState.PREPARED) {
				throwEarth();
				bender.cooldown(NAME, COOLDOWN);
				ignore = Collections.singletonList(this);
			}
		}
		if (ignore == null) {
			ignore = new LinkedList<EarthBlast>();
		}

		redirectTargetedBlasts(player, ignore);
		return false;
	}

	private void throwEarth() {
		if(!isState(BendingAbilityState.PREPARED) 
				|| current == null 
				|| current.getBlock().getWorld() != player.getWorld()) {
			return;
		}
		
		time = System.currentTimeMillis();
		LivingEntity target = EntityTools.getTargetedEntity(player, RANGE);
		destination = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
		if (target == null) {
			firstDestination = location.clone();
			firstDestination.setY(destination.getY());
		} else {
			firstDestination = location.clone();
			firstDestination.setY(target.getEyeLocation().getY());
			destination = Tools.getPointOnLine(firstDestination, destination, RANGE);
		}

		// If destination is less than 1 block away, this blast is screwed !
		if (destination.distance(location) <= 1) {
			destination = null;
			remove();
			return;
		}
		
		setState(BendingAbilityState.PROGRESSING);
		location.getWorld().playEffect(location, Effect.GHAST_SHOOT, 0, 10);
	}

	@Override
	public void progress() {
		if(isState(BendingAbilityState.PREPARED)) {
			if (!NAME.equals(bender.getAbility()) 
					|| current == null 
					|| location == null
					|| player.getWorld() != location.getWorld()
					|| location.distance(player.getLocation()) > SELECT_RANGE) {
				remove();
			}
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			if ((System.currentTimeMillis() - time) >= interval) {
				time = System.currentTimeMillis();
				
				if (location.getBlockY() == firstDestination.getBlockY()) {
					settingUp = false;
				}

				Vector direction = null;
				if (settingUp) {
					direction = Tools.getDirection(location, firstDestination).normalize();
				} else {
					direction = Tools.getDirection(location, destination).normalize();
				}

				location = location.clone().add(direction);

				PluginTools.removeSpouts(location, player);

				Block block = location.getBlock();
				if (block.getLocation().equals(current.getLocation())) {
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
					if (block2.getLocation().equals(current.getLocation())) {
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
					if (ProtectionManager.isLocationProtectedFromBending(player, register, entity.getLocation())) {
						continue;
					}

					if (((entity.getEntityId() != player.getEntityId()) || HITSELF)) {
						Location location = player.getEyeLocation();
						Vector vector = location.getDirection();
						entity.setVelocity(vector.normalize().multiply(PUSHFACTOR));
						DamageTools.damageEntity(bender, entity, damage, false, 2, 0.5f, false);
						remove();
						return;
					}
				}

				if (getState() != BendingAbilityState.PROGRESSING) {
					remove();
					return;
				}

				BlockTools.moveEarthBlock(current.getBlock(), block);
				if (block.getType() == Material.SAND) {
					block.setType(Material.SANDSTONE);
				}

				if (block.getType() == Material.GRAVEL) {
					block.setType(Material.STONE);
				}

				current = TempBlock.get(block);

				if (location.distance(destination) < 1) {
					remove();
					return;
				}
			}
		} else {
			remove();
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
	
	@Override
	public void stop() {
		if(current != null) {
			current.revertBlock();
			current = null;
		}
	}

	@Override
	public Object getIdentifier() {
		return this.uuid;
	}
	
	private static Location getTargetLocation(Player player) {
		Entity target = EntityTools.getTargetedEntity(player, RANGE);
		Location location;
		if (target == null) {
			location = EntityTools.getTargetedLocation(player, RANGE);
		} else {
			location = ((LivingEntity) target).getEyeLocation();
		}
		return location;
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

			RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(NAME);
			if (ProtectionManager.isLocationProtectedFromBending(player, register, blast.location)) {
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


	private static void redirectTargetedBlasts(Player player, List<EarthBlast> ignore) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if ((blast.getState() != BendingAbilityState.PROGRESSING) || ignore.contains(blast)) {
				continue;
			}

			if (!blast.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(NAME);
			if (ProtectionManager.isLocationProtectedFromBending(player, register, blast.location)) {
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
}

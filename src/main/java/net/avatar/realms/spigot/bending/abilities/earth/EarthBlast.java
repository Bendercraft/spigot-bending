package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPathType;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * State Prepared = block is selected but still not threw
 * State Progressing = block is threw
 * 
 * @author Koudja
 *
 */
@BendingAbility(name="Earth Blast", element=BendingType.Earth)
public class EarthBlast extends ActiveAbility {
	@ConfigurationParameter("Hit-Self")
	private static boolean HITSELF = false;
	
	@ConfigurationParameter("Select-Range")
	private static double SELECT_RANGE = 11;
	
	@ConfigurationParameter("Range")
	private static double RANGE = 20;
	
	@ConfigurationParameter("Earth-Damage")
	private static int EARTH_DAMAGE = 7;
	
	@ConfigurationParameter("Sand-Damage")
	private static int SANG_DAMAGE = 5;
	
	@ConfigurationParameter("Iron-Damage")
	private static int IRON_DAMAGE = 9;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 35;
	
	@ConfigurationParameter("Push-Factor")
	private static double PUSHFACTOR = 0.3;
	
	@ConfigurationParameter("Revert")
	private static boolean REVERT = true;
	
	@ConfigurationParameter("Revert")
	private static long COOLDOWN = 1000;
	
	private int damage;
	
	private static final double deflectrange = 3;
	
	// private static double speed = 1.5;

	private long interval;

	private static int ID = Integer.MIN_VALUE;

	private int id;
	private Location location = null;
	private Block sourceblock = null;
	private Material sourcetype = null;
	private Location destination = null;
	private Location firstdestination = null;
	private long time;
	private boolean settingup = true;

	public EarthBlast(Player player) {
		super (player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}
		
		id = ID++;
		if (ID >= Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		time = System.currentTimeMillis();
		
		bender = BendingPlayer.getBendingPlayer(player);
		double speed = SPEED;
		if(bender.hasPath(BendingPathType.Reckless)) {
			speed *= 0.8;
		}
		
		this.interval = (long) (1000. / speed);
		state = AbilityState.Preparing;
		
		AbilityManager.getManager().addInstance(this);
	}

	@Override
	public boolean sneak() {
		if(this.state != AbilityState.Preparing && this.state != AbilityState.Prepared) {
			return true;
		}
		state = AbilityState.Preparing;
		Block block = BlockTools.getEarthSourceBlock(player, Abilities.EarthBlast, SELECT_RANGE);
		cancelPrevious(block);	
		block(player);
		if (block != null) {
			sourceblock = block;
			if (EarthPassive.isPassiveSand(sourceblock)) {
				 EarthPassive.revertSand(sourceblock);
			}		
			sourcetype = sourceblock.getType();
			damage = EARTH_DAMAGE;
			if (sourcetype == Material.SAND) {
				sourceblock.setType(Material.SANDSTONE);
				damage = SANG_DAMAGE;
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
					damage = IRON_DAMAGE;
				}
				else if (EntityTools.canBend(player, Abilities.LavaTrain)
						&& sourceblock.getType() == Material.OBSIDIAN) {
					damage = IRON_DAMAGE;
					sourceblock.setType(Material.BEDROCK);
				}
				else {
					sourceblock.setType(Material.STONE);
				}
				
			}
			location = sourceblock.getLocation();
			
			if(bender.hasPath(BendingPathType.Tough)) {
				damage *= 0.85;
			}
			if(bender.hasPath(BendingPathType.Reckless)) {
				damage *= 1.15;
			}
			
			state = AbilityState.Prepared;
		}
		return false;
	}

	private static Location getTargetLocation(Player player) {
		Entity target = EntityTools.getTargettedEntity(player, RANGE);
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

	private void cancelPrevious(Block b) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (IAbility ab : AbilityManager.getManager().getInstances(getAbilityType()).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.state == AbilityState.Prepared && (blast.sourceblock.equals(b) || blast.player == player)) {
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

	public void throwEarth() {
		if (sourceblock == null) {
			return;
		}
		
		if (sourceblock.getWorld() != player.getWorld()) {
			return;
		}	

		if (BlockTools.bendedBlocks.containsKey(sourceblock)) {
			if (!REVERT) {
				BlockTools.removeRevertIndex(sourceblock);
			// Tools.removeEarthbendedBlockIndex(sourceblock);
			}				
		}
		LivingEntity target = EntityTools.getTargettedEntity(player, RANGE);
		// Tools.verbose(target);
		if (target == null) {
			destination = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
			firstdestination = sourceblock.getLocation().clone();
			firstdestination.setY(destination.getY());
		} else {
			destination = target.getEyeLocation();
			firstdestination = sourceblock.getLocation().clone();
			firstdestination.setY(destination.getY());
			destination = Tools.getPointOnLine(firstdestination,
					destination, RANGE);
		}

		if (destination.distance(location) <= 1) {
			state = AbilityState.Ended;
			destination = null;
		} else {
			state = AbilityState.Progressing;
			sourceblock.getWorld().playEffect(sourceblock.getLocation(),
												Effect.GHAST_SHOOT, 0,
												10);
			if (sourcetype != Material.SAND
					&& sourcetype != Material.GRAVEL) {
				sourceblock.setType(sourcetype);
			}
		}	
	}

	public static EarthBlast getBlastFromSource(Block block) {
		for (IAbility ab : AbilityManager.getManager().getInstances(Abilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.sourceblock.equals(block))
				return blast;
		}
		return null;
	}

	@Override
	public boolean progress() {
		if(!super.progress()) {
			return false;
		}
		if (player.isDead() || !player.isOnline()
				|| !EntityTools.canBend(player, Abilities.EarthBlast)) {
			breakBlock();
			return false;
		}
		
		if(this.state.isBefore(AbilityState.Prepared)) {
			return false;
		}
		
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (!BlockTools.isEarthbendable(player, Abilities.EarthBlast, sourceblock)
					&& sourceblock.getType() != Material.COBBLESTONE
					&& sourceblock.getType() != Material.SANDSTONE
					&& sourceblock.getType() != Material.BEDROCK) {
				return false;
			}

			if (state ==  AbilityState.Prepared) {
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
				if (sourceblock.getLocation().distance(player.getLocation()) > SELECT_RANGE) {
					cancel();
					return false;
				}
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
				double radius = FireBlast.AFFECTING_RADIUS;
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
					FireBlast.AFFECTING_RADIUS)) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(player,
						Abilities.EarthBlast, entity.getLocation())) {
					continue;
				}
					
				if ((entity.getEntityId() != player.getEntityId() || HITSELF)) {
					Location location = player.getEyeLocation();
			 		Vector vector = location.getDirection();
			 		entity.setVelocity(vector.normalize().multiply(PUSHFACTOR));
			 		EntityTools.damageEntity(player, entity, damage);
					state = AbilityState.Ended;
				}
			}

			if (state != AbilityState.Progressing) {
				breakBlock();
				return false;
			}

			if (REVERT) {
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
					sourceblock.setType(sourcetype);
				}
				state = AbilityState.Ended;
				breakBlock();
				return false;
			}
		}
		return true;
	}

	/**
	 * Should remove() after this method
	 */
	private void breakBlock() {
		sourceblock.setType(sourcetype);
		if (REVERT) {
			BlockTools.addTempAirBlock(sourceblock);
		} else {
			sourceblock.breakNaturally();
		}
	}

	@Override
	public boolean swing() {
		ArrayList<EarthBlast> ignore = new ArrayList<EarthBlast>();

		if (!bender.isOnCooldown(getAbilityType())) {
			if (state == AbilityState.Prepared) {
				throwEarth();
				bender.cooldown(Abilities.EarthBlast, COOLDOWN);
				ignore.add(this);
			}
		}

		redirectTargettedBlasts(player, ignore);
		return false;
	}

	private static void redirectTargettedBlasts(Player player,
			ArrayList<EarthBlast> ignore) {
		for(IAbility ab : AbilityManager.getManager().getInstances(Abilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.state != AbilityState.Progressing || ignore.contains(blast))
				continue;

			if (!blast.location.getWorld().equals(player.getWorld()))
				continue;

			if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.EarthBlast,
					blast.location))
				continue;

			if (blast.player.equals(player))
				blast.redirect(player, getTargetLocation(player));

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= RANGE
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
		if (state == AbilityState.Progressing) {
			if (location.distance(player.getLocation()) <= RANGE) {
				settingup = false;
				destination = targetlocation;
			}
		}
	}

	private static void block(Player player) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for(IAbility ab : AbilityManager.getManager().getInstances(Abilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.player.equals(player)) {
				continue;
			}			

			if (!blast.location.getWorld().equals(player.getWorld())){
				continue;
			}			

			if (blast.state == AbilityState.Prepared){
				continue;
			}			

			if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.EarthBlast,
					blast.location)) {
				continue;
			}		

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distance(location) <= RANGE
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
		for(IAbility ab : AbilityManager.getManager().getInstances(Abilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
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
		for(IAbility ab : AbilityManager.getManager().getInstances(Abilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
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
	public Object getIdentifier() {
		return id;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.EarthBlast;
	}

}

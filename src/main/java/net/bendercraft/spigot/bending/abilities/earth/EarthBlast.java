package net.bendercraft.spigot.bending.abilities.earth;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
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
	
	@ConfigurationParameter("Affecting-Radius")
	public static double AFFECTING_RADIUS = 2;

	@ConfigurationParameter("Select-Range")
	private static double SELECT_RANGE = 11;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Earth-Damage")
	private static int BASE_DAMAGE = 7;

	@ConfigurationParameter("Sand-Bonus-Damage")
	private static int BONUS_DAMAGE_SAND = -2;

	@ConfigurationParameter("Iron-Bonus-Damage")
	private static int BONUS_DAMAGE_IRON = 2;

	@ConfigurationParameter("Speed")
	private static double SPEED = 35;

	@ConfigurationParameter("Push-Factor")
	private static double PUSHFACTOR = 0.3;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 500;

	@ConfigurationParameter("Deflect-Range")
	private static double DEFLECT_RANGE = 3;

	private long interval;

	private UUID uuid = UUID.randomUUID();
	
	private Location location; // For accurate advance
	private List<TempBlock> current = new LinkedList<TempBlock>(); // To revert each time we change, also holds source selection if not yet thrown
	
	private Material type; // What type of block is this earhblast made of ?
	
	private boolean settingUp = true; // Are we trying to reach first destination ?
	private Location firstDestination; // First destination (raising block)
	private Location destination; // Final destination
	
	private long time;
	private int damage;
	private int damageBonus = 0;
	private double selectRange;
	private double range;
	private double deflectRange;
	
	private int radius = 0;

	public EarthBlast(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.selectRange = SELECT_RANGE;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_SELECT_RANGE_1)) {
			this.selectRange += 2;
		}
		if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_SELECT_RANGE_2)) {
			this.selectRange += 2;
		}
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_RANGE)) {
			this.range += 5;
		}
		
		this.deflectRange = DEFLECT_RANGE;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_DEFLECT_RANGE)) {
			this.deflectRange += 1;
		}
		
		this.damage = BASE_DAMAGE;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_DAMAGE)) {
			this.damage += 1;
		}
		
		double speed = SPEED;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_SPEED)) {
			speed *= 1.15;
		}
		this.interval = (long) (1000. / speed);
	}
	
	private void select(Block block) {
		if(block == null) {
			return;
		}
		
		if (EarthPassive.isPassiveSand(block)) {
			EarthPassive.revertSand(block);
		}
		
		if(!BlockTools.isEarthbendable(player, block)) {
			return;
		}
		
		if(type == null) {
			type = block.getType();
			if(type == Material.SAND) {
				type = Material.SANDSTONE;
			}
		}
		location = block.getLocation().clone();
		
		if (block.getType() == Material.SAND) {
			current.add(TempBlock.makeTemporary(this, block, Material.SANDSTONE, false));
			damageBonus = BONUS_DAMAGE_SAND;
		} else if (block.getType() == Material.STONE) {
			current.add(TempBlock.makeTemporary(this, block, Material.COBBLESTONE, false));
		} else if (EntityTools.canBend(this.player, MetalBending.NAME) && BlockTools.isIronBendable(player, type)) {
			if (block.getType() == Material.IRON_BLOCK) {
				current.add(TempBlock.makeTemporary(this, block, Material.IRON_ORE, false));
			} else {
				current.add(TempBlock.makeTemporary(this, block, Material.IRON_BLOCK, false));
			}
			damageBonus = BONUS_DAMAGE_IRON;
		} else if (EntityTools.canBend(this.player, LavaTrain.NAME) && (type == Material.OBSIDIAN)) {
			damageBonus = BONUS_DAMAGE_IRON;
			current.add(TempBlock.makeTemporary(this, block, Material.BEDROCK, false));
		} else {
			current.add(TempBlock.makeTemporary(this, block, Material.STONE, false));
		}
	}

	@Override
	public boolean sneak() {
		// Player is selecting a block as source
		if(isState(BendingAbilityState.START) || isState(BendingAbilityState.PREPARED)) {
			Block block = BlockTools.getEarthSourceBlock(player, register, selectRange);
			if(block == null) {
				remove();
				return false;
			}
			
			if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_MULTISELECT)
					&& !current.isEmpty() 
					&& block.getLocation().distanceSquared(current.get(0).getLocation()) <= 3*3) {
				if(radius < 2) {
					radius++;
					List<Block> done = new LinkedList<Block>();
					current.forEach(t -> done.add(t.getBlock()));
					for(Block temp : BlockTools.getBlocksAroundPoint(current.get(0).getBlock().getLocation(), radius)) {
						if(!done.contains(temp)) {
							select(temp);
						}
					}
				}
			} else {
				radius = 0;
				current.forEach(t -> t.revertBlock());
				current.clear();
				select(block);
			}
			setState(BendingAbilityState.PREPARED);
			
			return false;
		}
		return true;
	}

	@Override
	public boolean swing() {
		// A swing could both send a prepared earthblast or redirect others
		List<EarthBlast> ignore = null;
		if (!bender.isOnCooldown(NAME)) {
			if (isState(BendingAbilityState.PREPARED)) {
				throwEarth();
				bender.cooldown(NAME, COOLDOWN + (1000*radius));
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
				|| current.isEmpty()
				|| current.get(0).getBlock().getWorld() != player.getWorld()) {
			return;
		}
		
		time = System.currentTimeMillis();
		LivingEntity target = EntityTools.getTargetedEntity(player, getRange());
		destination = EntityTools.getTargetBlock(player, getRange(), BlockTools.getTransparentEarthbending()).getLocation();
		if (target == null) {
			firstDestination = location.clone();
			firstDestination.setY(destination.getY());
		} else {
			firstDestination = location.clone();
			firstDestination.setY(target.getEyeLocation().getY());
			destination = Tools.getPointOnLine(firstDestination, destination, getRange());
		}

		// If destination is less than 1 block away, this blast is screwed !
		if (destination.distance(location) <= 1) {
			destination = null;
			remove();
			return;
		}
		
		setState(BendingAbilityState.PROGRESSING);
		location.getWorld().playSound(location, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
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
				
				if (block.getLocation().equals(current.get(0).getLocation())) {
					return;
				}
				
				current.forEach(t -> t.revertBlock());
				current.clear();
				
				if(BlockTools.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else if(!BlockTools.isEarthbendable(player, block)) {
					remove();
					return;
				}
				
				double affectRadius = radius+AFFECTING_RADIUS;
				
				PluginTools.removeSpouts(location, player);
				Player source = player;
				if (EarthBlast.shouldAnnihilateBlasts(location, affectRadius, source) 
						|| WaterManipulation.annihilateBlasts(location, affectRadius, source) 
						|| FireBlast.annihilateBlasts(location, affectRadius, source)) {
					radius--;
					if(radius == -1) {
						remove();
						return;
					}
				}
				
				boolean affected = false;
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, affectRadius)) {
					if(affect(entity)) {
						affected = true;
						if(radius == 0) {
							remove();
							return;
						}
					}
				}
				if(affected) {
					remove();
					return;
				}
				
				current.add(TempBlock.makeTemporary(this, block, type, false));
				for(Block temp : BlockTools.getBlocksAroundPoint(location, radius)) {
					if(temp.getType() == Material.AIR) {
						current.add(TempBlock.makeTemporary(this, temp, type, false));
					}
				}
				if(current.isEmpty()) {
					remove();
					return;
				}
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
			if (location.distance(player.getLocation()) <= getRange()) {
				settingUp = false;
				destination = targetlocation;
			}
		}
	}
	
	@Override
	public void stop() {
		current.forEach(t -> t.revertBlock());
	}

	@Override
	public Object getIdentifier() {
		return this.uuid;
	}
	
	private boolean affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		if(entity == player) {
			return false;
		}
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection();
		entity.setVelocity(vector.normalize().multiply(PUSHFACTOR));
		DamageTools.damageEntity(bender, entity, this, damage+damageBonus, false, 2, 0.5f, false);
		return true;
	}
	
	private Location getTargetLocation(Player player) {
		Entity target = EntityTools.getTargetedEntity(player, getRange());
		Location location;
		if (target == null) {
			location = EntityTools.getTargetedLocation(player, getRange());
		} else {
			location = ((LivingEntity) target).getEyeLocation();
		}
		return location;
	}
	
	private double getRange() {
		return range + 5*radius;
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
	
	public static boolean removeOneAroundPoint(Location location, Player player, double radius) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.player != player
					&& blast.location.getWorld().equals(location.getWorld()) 
					&& blast.location.distance(location) <= radius) {
				blast.remove();
				return true;
			}
		}
		return false;
	}

	public static void removeAroundPoint(Location location, double radius) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distance(location) <= radius) {
					blast.remove();
				}
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

			RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(NAME);
			if (ProtectionManager.isLocationProtectedFromBending(player, register, blast.location)) {
				continue;
			}

			if (blast.player.equals(player)) {
				blast.redirect(player, blast.getTargetLocation(player));
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mLoc = blast.location;
			if ((mLoc.distance(location) <= RANGE) && (Tools.getDistanceFromLine(vector, location, blast.location) < blast.deflectRange) && (mLoc.distance(location.clone().add(vector)) < mLoc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				blast.redirect(player, blast.getTargetLocation(player));
			}

		}
	}
}

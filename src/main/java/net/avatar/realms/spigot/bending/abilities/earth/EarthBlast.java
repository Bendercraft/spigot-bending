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
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
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
@ABendingAbility(name = "Earth Blast", bind = BendingAbilities.EarthBlast, element = BendingElement.Earth)
public class EarthBlast extends BendingActiveAbility {
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
	private Location destination = null;
	private Location firstDestination = null;
	private long time;
	private boolean settingUp = true;

	private int damage;

	public EarthBlast(Player player) {
		super(player);

		this.id = ID++;
		this.time = this.startedTime;

		double speed = SPEED;
		if (this.bender.hasPath(BendingPath.Reckless)) {
			speed *= 0.8;
		}

		this.interval = (long) (1000. / speed);
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.Start 
				&& getState() != BendingAbilityState.Preparing 
				&& getState() != BendingAbilityState.Prepared) {
			return true;
		}
		
		cancel();
		Block block = BlockTools.getEarthSourceBlock(this.player, BendingAbilities.EarthBlast, SELECT_RANGE);

		setState(BendingAbilityState.Preparing);

		block(this.player);
		if (block != null) {
			this.sourceBlock = block;
			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}
			this.damage = EARTH_DAMAGE;
			if (block.getType() == Material.SAND) {
				//this.source = new TempBlock(sourceBlock, Material.SANDSTONE);
				this.source = TempBlock.makeTemporary(sourceBlock, Material.SANDSTONE);
				this.damage = SAND_DAMAGE;
			}
			else if (block.getType() == Material.STONE) {
				//this.source = new TempBlock(sourceBlock, Material.COBBLESTONE);
				this.source = TempBlock.makeTemporary(sourceBlock, Material.COBBLESTONE);
			} else {
				if (EntityTools.canBend(this.player, BendingAbilities.MetalBending) && BlockTools.isIronBendable(this.player, this.sourceBlock.getType())) {
					if (block.getType() == Material.IRON_BLOCK) {
						//this.source = new TempBlock(sourceBlock, Material.IRON_ORE);
						this.source = TempBlock.makeTemporary(sourceBlock, Material.IRON_ORE);
					} else {
						//this.source = new TempBlock(sourceBlock, Material.IRON_BLOCK);
						this.source = TempBlock.makeTemporary(sourceBlock, Material.IRON_BLOCK);
					}
					this.damage = IRON_DAMAGE;
				} else if (EntityTools.canBend(this.player, BendingAbilities.LavaTrain) && (this.sourceBlock.getType() == Material.OBSIDIAN)) {
					this.damage = IRON_DAMAGE;
					//this.source = new TempBlock(sourceBlock, Material.BEDROCK);
					this.source = TempBlock.makeTemporary(sourceBlock, Material.BEDROCK);
				} else {
					//this.source = new TempBlock(sourceBlock, Material.STONE);
					this.source = TempBlock.makeTemporary(sourceBlock, Material.STONE);
				}

			}
			this.location = this.sourceBlock.getLocation();

			if (this.bender.hasPath(BendingPath.Tough)) {
				this.damage *= 0.85;
			}
			if (this.bender.hasPath(BendingPath.Reckless)) {
				this.damage *= 1.15;
			}

			setState(BendingAbilityState.Prepared);
		}
		return false;
	}

	@Override
	public boolean swing() {
		List<EarthBlast> ignore = null;
		if (!this.bender.isOnCooldown(BendingAbilities.EarthBlast)) {
			if (getState() == BendingAbilityState.Prepared) {
				throwEarth();
				this.bender.cooldown(BendingAbilities.EarthBlast, COOLDOWN);
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
		if (this.sourceBlock == null) {
			return;
		}

		if (this.sourceBlock.getWorld() != this.player.getWorld()) {
			return;
		}

		LivingEntity target = EntityTools.getTargetedEntity(this.player, RANGE);
		if (target == null) {
			this.destination = EntityTools.getTargetBlock(this.player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
			this.firstDestination = this.sourceBlock.getLocation().clone();
			this.firstDestination.setY(this.destination.getY());
		} else {
			this.destination = target.getEyeLocation();
			this.firstDestination = this.sourceBlock.getLocation().clone();
			this.firstDestination.setY(this.destination.getY());
			this.destination = Tools.getPointOnLine(this.firstDestination, this.destination, RANGE);
		}

		if (this.destination.distance(this.location) <= 1) {
			this.destination = null;
			remove();
			return;
		} else {
			setState(BendingAbilityState.Progressing);
			this.sourceBlock.getWorld().playEffect(this.sourceBlock.getLocation(), Effect.GHAST_SHOOT, 0, 10);
			if (source != null && (source.getState().getType() != Material.SAND) && (source.getState().getType() != Material.GRAVEL)) {
				source.revertBlock();
				source = null;
			}
		}
	}

	@Override
	public void progress() {
		if (getState().isBefore(BendingAbilityState.Prepared)) {
			remove();
			return;
		}

		if ((System.currentTimeMillis() - this.time) >= this.interval) {
			this.time = System.currentTimeMillis();

			if (!BlockTools.isEarthbendable(this.player, BendingAbilities.EarthBlast, this.sourceBlock) && (this.sourceBlock.getType() != Material.COBBLESTONE) && (this.sourceBlock.getType() != Material.SANDSTONE) && (this.sourceBlock.getType() != Material.BEDROCK)) {
				remove();
				return;
			}

			if (getState() == BendingAbilityState.Prepared) {
				if (EntityTools.getBendingAbility(this.player) != BendingAbilities.EarthBlast) {
					cancel();
					remove();
					return;
				}
				if (this.sourceBlock == null) {
					remove();
					return;
				}
				if (this.player.getWorld() != this.sourceBlock.getWorld()) {
					cancel();
					remove();
					return;
				}
				if (this.sourceBlock.getLocation().distance(this.player.getLocation()) > SELECT_RANGE) {
					cancel();
					remove();
					return;
				}
				return;
			}

			if (this.sourceBlock.getY() == this.firstDestination.getBlockY()) {
				this.settingUp = false;
			}

			Vector direction;
			if (this.settingUp) {
				direction = Tools.getDirection(this.location, this.firstDestination).normalize();
			} else {
				direction = Tools.getDirection(this.location, this.destination).normalize();
			}

			this.location = this.location.clone().add(direction);

			PluginTools.removeSpouts(this.location, this.player);

			Block block = this.location.getBlock();
			if (block.getLocation().equals(this.sourceBlock.getLocation())) {
				this.location = this.location.clone().add(direction);
				block = this.location.getBlock();
			}

			if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
				BlockTools.breakBlock(block);
			} else if (!this.settingUp) {
				remove();
				return;
			} else {
				this.location = this.location.clone().subtract(direction);
				direction = Tools.getDirection(this.location, this.destination).normalize();
				this.location = this.location.clone().add(direction);

				PluginTools.removeSpouts(this.location, this.player);
				double radius = FireBlast.AFFECTING_RADIUS;
				Player source = this.player;
				if (EarthBlast.shouldAnnihilateBlasts(this.location, radius, source) || WaterManipulation.annihilateBlasts(this.location, radius, source) || FireBlast.annihilateBlasts(this.location, radius, source)) {
					remove();
					return;
				}

				Block block2 = this.location.getBlock();
				if (block2.getLocation().equals(this.sourceBlock.getLocation())) {
					this.location = this.location.clone().add(direction);
					block2 = this.location.getBlock();
				}

				if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else {
					remove();
					return;
				}
			}
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, FireBlast.AFFECTING_RADIUS)) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.EarthBlast, entity.getLocation())) {
					continue;
				}

				if (((entity.getEntityId() != this.player.getEntityId()) || HITSELF)) {
					Location location = this.player.getEyeLocation();
					Vector vector = location.getDirection();
					entity.setVelocity(vector.normalize().multiply(PUSHFACTOR));
					EntityTools.damageEntity(this.player, entity, this.damage);
					remove();
					return;
				}
			}

			if (getState() != BendingAbilityState.Progressing) {
				remove();
				return;
			}

			if(source != null) {
				source.revertBlock();
				source = null;
			}
			BlockTools.moveEarthBlock(this.sourceBlock, block);
			if (block.getType() == Material.SAND) {
				block.setType(Material.SANDSTONE);
			}

			if (block.getType() == Material.GRAVEL) {
				block.setType(Material.STONE);
			}

			this.sourceBlock = block;

			if (this.location.distance(this.destination) < 1) {
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
		for (BendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if ((blast.getState() != BendingAbilityState.Progressing) || ignore.contains(blast)) {
				continue;
			}

			if (!blast.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.EarthBlast, blast.location)) {
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
		if (getState() == BendingAbilityState.Progressing) {
			if (this.location.distance(player.getLocation()) <= RANGE) {
				this.settingUp = false;
				this.destination = targetlocation;
			}
		}
	}

	private static void block(Player player) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (BendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.player.equals(player)) {
				continue;
			}

			if (blast.location == null || !blast.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (blast.getState() == BendingAbilityState.Prepared) {
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.EarthBlast, blast.location)) {
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
		for (BendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
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
		for (BendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
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
			BlockTools.addTempAirBlock(this.sourceBlock);
		}
	}
}

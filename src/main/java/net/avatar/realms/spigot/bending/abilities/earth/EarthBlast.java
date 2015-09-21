package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
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

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * State Prepared = block is selected but still not threw State Progressing =
 * block is thrown
 *
 * @author Koudja
 *
 */
@BendingAbility(name = "Earth Blast", bind = BendingAbilities.EarthBlast, element = BendingElement.Earth)
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
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		this.id = ID++;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		this.time = this.startedTime;

		double speed = SPEED;
		if (this.bender.hasPath(BendingPath.Reckless)) {
			speed *= 0.8;
		}

		this.interval = (long) (1000. / speed);
		this.state = BendingAbilityState.Preparing;

		AbilityManager.getManager().addInstance(this);
	}

	@Override
	public boolean sneak() {
		if ((this.state != BendingAbilityState.Preparing) && (this.state != BendingAbilityState.Prepared)) {
			return true;
		}

		this.state = BendingAbilityState.Preparing;
		Block block = BlockTools.getEarthSourceBlock(this.player, BendingAbilities.EarthBlast, SELECT_RANGE);

		cancelPrevious(block);
		block(this.player);
		if (block != null) {
			this.sourceblock = block;
			if (EarthPassive.isPassiveSand(this.sourceblock)) {
				EarthPassive.revertSand(this.sourceblock);
			}
			this.sourcetype = this.sourceblock.getType();
			this.damage = EARTH_DAMAGE;
			if (this.sourcetype == Material.SAND) {
				this.sourceblock.setType(Material.SANDSTONE);
				this.damage = SAND_DAMAGE;
			} else if (this.sourcetype == Material.STONE) {
				this.sourceblock.setType(Material.COBBLESTONE);
			} else {
				if (EntityTools.canBend(this.player, BendingAbilities.MetalBending) && BlockTools.isIronBendable(this.player, this.sourceblock.getType())) {
					if (this.sourcetype == Material.IRON_BLOCK) {
						this.sourceblock.setType(Material.IRON_ORE);
					} else {
						this.sourceblock.setType(Material.IRON_BLOCK);
					}
					this.damage = IRON_DAMAGE;
				} else if (EntityTools.canBend(this.player, BendingAbilities.LavaTrain) && (this.sourceblock.getType() == Material.OBSIDIAN)) {
					this.damage = IRON_DAMAGE;
					this.sourceblock.setType(Material.BEDROCK);
				} else {
					this.sourceblock.setType(Material.STONE);
				}

			}
			this.location = this.sourceblock.getLocation();

			if (this.bender.hasPath(BendingPath.Tough)) {
				this.damage *= 0.85;
			}
			if (this.bender.hasPath(BendingPath.Reckless)) {
				this.damage *= 1.15;
			}

			this.state = BendingAbilityState.Prepared;
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
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(AbilityManager.getManager().getAbilityType(this)).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if ((blast.state == BendingAbilityState.Prepared) && (blast.sourceblock.equals(b) || (blast.player == this.player))) {
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
		this.sourceblock.setType(this.sourcetype);
	}

	public void throwEarth() {
		if (this.sourceblock == null) {
			return;
		}

		if (this.sourceblock.getWorld() != this.player.getWorld()) {
			return;
		}

		if (BlockTools.bendedBlocks.containsKey(this.sourceblock)) {
			if (!REVERT) {
				BlockTools.removeRevertIndex(this.sourceblock);
				// Tools.removeEarthbendedBlockIndex(sourceblock);
			}
		}
		LivingEntity target = EntityTools.getTargettedEntity(this.player, RANGE);
		// Tools.verbose(target);
		if (target == null) {
			this.destination = EntityTools.getTargetBlock(this.player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
			this.firstdestination = this.sourceblock.getLocation().clone();
			this.firstdestination.setY(this.destination.getY());
		} else {
			this.destination = target.getEyeLocation();
			this.firstdestination = this.sourceblock.getLocation().clone();
			this.firstdestination.setY(this.destination.getY());
			this.destination = Tools.getPointOnLine(this.firstdestination, this.destination, RANGE);
		}

		if (this.destination.distance(this.location) <= 1) {
			this.state = BendingAbilityState.Ended;
			this.destination = null;
		} else {
			this.state = BendingAbilityState.Progressing;
			this.sourceblock.getWorld().playEffect(this.sourceblock.getLocation(), Effect.GHAST_SHOOT, 0, 10);
			if ((this.sourcetype != Material.SAND) && (this.sourcetype != Material.GRAVEL)) {
				this.sourceblock.setType(this.sourcetype);
			}
		}
	}

	public static EarthBlast getBlastFromSource(Block block) {
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.sourceblock.equals(block)) {
				return blast;
			}
		}
		return null;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			breakBlock();
			return false;
		}

		if (this.state.isBefore(BendingAbilityState.Prepared)) {
			return false;
		}

		if ((System.currentTimeMillis() - this.time) >= this.interval) {
			this.time = System.currentTimeMillis();

			if (!BlockTools.isEarthbendable(this.player, BendingAbilities.EarthBlast, this.sourceblock) && (this.sourceblock.getType() != Material.COBBLESTONE) && (this.sourceblock.getType() != Material.SANDSTONE) && (this.sourceblock.getType() != Material.BEDROCK)) {
				return false;
			}

			if (this.state == BendingAbilityState.Prepared) {
				if (EntityTools.getBendingAbility(this.player) != BendingAbilities.EarthBlast) {
					cancel();
					return false;
				}
				if (this.sourceblock == null) {
					return false;
				}
				if (this.player.getWorld() != this.sourceblock.getWorld()) {
					cancel();
					return false;
				}
				if (this.sourceblock.getLocation().distance(this.player.getLocation()) > SELECT_RANGE) {
					cancel();
					return false;
				}
				return true;
			}

			if (this.sourceblock.getY() == this.firstdestination.getBlockY()) {
				this.settingup = false;
			}

			Vector direction;
			if (this.settingup) {
				direction = Tools.getDirection(this.location, this.firstdestination).normalize();
			} else {
				direction = Tools.getDirection(this.location, this.destination).normalize();
			}

			this.location = this.location.clone().add(direction);

			PluginTools.removeSpouts(this.location, this.player);

			Block block = this.location.getBlock();
			if (block.getLocation().equals(this.sourceblock.getLocation())) {
				this.location = this.location.clone().add(direction);
				block = this.location.getBlock();
			}

			if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
				BlockTools.breakBlock(block);
			} else if (!this.settingup) {
				breakBlock();
				return false;
			} else {
				this.location = this.location.clone().subtract(direction);
				direction = Tools.getDirection(this.location, this.destination).normalize();
				this.location = this.location.clone().add(direction);

				PluginTools.removeSpouts(this.location, this.player);
				double radius = FireBlast.AFFECTING_RADIUS;
				Player source = this.player;
				if (EarthBlast.shouldAnnihilateBlasts(this.location, radius, source, false) || WaterManipulation.annihilateBlasts(this.location, radius, source) || FireBlast.annihilateBlasts(this.location, radius, source)) {
					breakBlock();
					return false;
				}

				Block block2 = this.location.getBlock();
				if (block2.getLocation().equals(this.sourceblock.getLocation())) {
					this.location = this.location.clone().add(direction);
					block2 = this.location.getBlock();
				}

				if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
					BlockTools.breakBlock(block);
				} else {
					breakBlock();
					return false;
				}
			}
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, FireBlast.AFFECTING_RADIUS)) {
				if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
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
					this.state = BendingAbilityState.Ended;
				}
			}

			if (this.state != BendingAbilityState.Progressing) {
				breakBlock();
				return false;
			}

			if (REVERT) {
				// Tools.addTempEarthBlock(sourceblock, block);
				this.sourceblock.setType(this.sourcetype);
				BlockTools.moveEarthBlock(this.sourceblock, block);
				if (block.getType() == Material.SAND) {
					block.setType(Material.SANDSTONE);
				}

				if (block.getType() == Material.GRAVEL) {
					block.setType(Material.STONE);
				}

			} else {
				block.setType(this.sourceblock.getType());
				this.sourceblock.setType(Material.AIR);
			}

			this.sourceblock = block;

			if (this.location.distance(this.destination) < 1) {
				if ((this.sourcetype == Material.SAND) || (this.sourcetype == Material.GRAVEL)) {
					this.sourceblock.setType(this.sourcetype);
				}
				this.state = BendingAbilityState.Ended;
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
		this.sourceblock.setType(this.sourcetype);
		if (REVERT) {
			BlockTools.addTempAirBlock(this.sourceblock);
		} else {
			this.sourceblock.breakNaturally();
		}
	}

	@Override
	public boolean swing() {
		ArrayList<EarthBlast> ignore = new ArrayList<EarthBlast>();
		if (!this.bender.isOnCooldown(AbilityManager.getManager().getAbilityType(this))) {
			if (this.state == BendingAbilityState.Prepared) {
				throwEarth();
				this.bender.cooldown(BendingAbilities.EarthBlast, COOLDOWN);
				ignore.add(this);
			}
		}

		redirectTargettedBlasts(this.player, ignore);
		return false;
	}

	private static void redirectTargettedBlasts(Player player, ArrayList<EarthBlast> ignore) {
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if ((blast.state != BendingAbilityState.Progressing) || ignore.contains(blast)) {
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
			Location mloc = blast.location;
			if ((mloc.distance(location) <= RANGE) && (Tools.getDistanceFromLine(vector, location, blast.location) < deflectrange) && (mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				blast.redirect(player, getTargetLocation(player));
			}

		}
	}

	private void redirect(Player player, Location targetlocation) {
		if (this.state == BendingAbilityState.Progressing) {
			if (this.location.distance(player.getLocation()) <= RANGE) {
				this.settingup = false;
				this.destination = targetlocation;
			}
		}
	}

	private static void block(Player player) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.player.equals(player)) {
				continue;
			}

			if (!blast.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (blast.state == BendingAbilityState.Prepared) {
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.EarthBlast, blast.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if ((mloc.distance(location) <= RANGE) && (Tools.getDistanceFromLine(vector, location, blast.location) < deflectrange) && (mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				blast.breakBlock();
				toRemove.add(blast);
			}
		}
		for (EarthBlast blast : toRemove) {
			blast.remove();
		}
	}

	public static void removeAroundPoint(Location location, double radius) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distance(location) <= radius) {
					blast.breakBlock();
					toRemove.add(blast);
				}
			}
		}
		for (EarthBlast blast : toRemove) {
			blast.remove();
		}
	}

	public static boolean shouldAnnihilateBlasts(Location location, double radius, Player source, boolean remove) {
		List<EarthBlast> toRemove = new LinkedList<EarthBlast>();
		boolean broke = false;
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthBlast).values()) {
			EarthBlast blast = (EarthBlast) ab;
			if (blast.location.getWorld().equals(location.getWorld()) && !source.equals(blast.player)) {
				if (blast.location.distance(location) <= radius) {
					blast.breakBlock();
					broke = true;
					toRemove.add(blast);
				}
			}
		}
		if (remove) {
			for (EarthBlast blast : toRemove) {
				blast.remove();
			}
		}
		return broke;
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		return shouldAnnihilateBlasts(location, radius, source, true);
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}
}

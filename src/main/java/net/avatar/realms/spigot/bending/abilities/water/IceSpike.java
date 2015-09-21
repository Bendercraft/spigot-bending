package net.avatar.realms.spigot.bending.abilities.water;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@BendingAbility(name = "Ice Spikes", bind = BendingAbilities.IceSpike, element = BendingElement.Water)
public class IceSpike extends BendingActiveAbility {
	private static double defaultrange = 20;
	private static int defaultdamage = 1;
	private static int defaultmod = 2;
	private static int ID = Integer.MIN_VALUE;
	static long slowCooldown = 5000;

	private static final long interval = 20;
	private static final byte data = 0;
	private static final double affectingradius = 2;
	private static final double deflectrange = 3;

	private int id;
	private double range;
	private boolean plantbending = false;
	private Block sourceblock;
	private TempBlock source;
	private boolean prepared = false;
	private boolean settingup = false;
	private boolean progressing = false;
	private long time;

	private Location location;
	private Location firstdestination;
	private Location destination;

	private SpikeField field = null;
	private WaterReturn waterReturn;

	public IceSpike(Player player) {
		super(player, null);
		block(player);
		if (EntityTools.canPlantbend(player))
			plantbending = true;
		range = PluginTools.waterbendingNightAugment(defaultrange, player.getWorld());

	}

	// SNEAK
	@Override
	public boolean sneak() {
		if (field != null) {
			return false;
		}
		sourceblock = BlockTools.getWaterSourceBlock(player, range, plantbending);
		if (sourceblock == null) {
			field = new SpikeField(player, this);
			AbilityManager.getManager().addInstance(this);
			return false;
		}

		for (IBendingAbility ab : AbilityManager.getManager().getInstances(AbilityManager.getManager().getAbilityType(this)).values()) {
			IceSpike ice = (IceSpike) ab;
			if (ice.prepared && ice.player == player) {
				ice.remove();
			}
		}
		location = sourceblock.getLocation();
		prepared = true;

		id = ID++;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		AbilityManager.getManager().addInstance(this);
		return false;
	}

	// SWING
	@Override
	public boolean swing() {
		if (field != null) {
			return false;
		}
		redirect(player);
		boolean activate = false;

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(BendingAbilities.IceSpike)) {
			return false;
		}

		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.IceSpike).values()) {
			IceSpike ice = (IceSpike) ab;
			if (ice.prepared && ice.player == player) {
				ice.throwIce();
				activate = true;
			}
		}

		if (!activate) {
			IceSpike spike = new IceSpike(player);
			if (spike.id == 0 && WaterReturn.hasWaterBottle(player)) {
				Location eyeloc = player.getEyeLocation();
				Block block = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
				if (BlockTools.isTransparentToEarthbending(player, block) && BlockTools.isTransparentToEarthbending(player, eyeloc.getBlock())) {

					LivingEntity target = (LivingEntity) EntityTools.getTargettedEntity(player, defaultrange);
					Location destination;
					if (target == null) {
						destination = EntityTools.getTargetedLocation(player, defaultrange, BlockTools.transparentEarthbending);
					} else {
						destination = Tools.getPointOnLine(player.getEyeLocation(), target.getEyeLocation(), defaultrange);
					}

					if (destination.distance(block.getLocation()) < 1)
						return false;

					block.setType(Material.WATER);
					block.setData((byte) 0x0);
					throwIce();

					if (progressing) {
						WaterReturn.emptyWaterBottle(player);
					} else {
						block.setType(Material.AIR);
					}
				}
			}
		}

		return false;
	}

	private void throwIce() {
		if (!prepared)
			return;
		LivingEntity target = (LivingEntity) EntityTools.getTargettedEntity(player, range);
		if (target == null) {
			destination = EntityTools.getTargetedLocation(player, range, BlockTools.transparentEarthbending);
		} else {
			destination = target.getEyeLocation();
		}

		location = sourceblock.getLocation();
		if (destination.distance(location) < 1)
			return;
		firstdestination = location.clone();
		if (destination.getY() - location.getY() > 2) {
			firstdestination.setY(destination.getY() - 1);
		} else {
			firstdestination.add(0, 2, 0);
		}
		destination = Tools.getPointOnLine(firstdestination, destination, range);
		progressing = true;
		settingup = true;
		prepared = false;

		if (BlockTools.isPlant(sourceblock)) {
			sourceblock.setType(Material.AIR);
		} else if (!BlockTools.adjacentToThreeOrMoreSources(sourceblock)) {
			sourceblock.setType(Material.AIR);
		}

		source = new TempBlock(sourceblock, Material.ICE, data);
	}

	@Override
	public boolean progress() {
		if (waterReturn != null) {
			return waterReturn.progress();
		}

		if (field != null) {
			return field.progress();
		}

		if (player.isDead() || !player.isOnline() || !EntityTools.canBend(player, BendingAbilities.IceSpike)) {
			return false;
		}

		if (!player.getWorld().equals(location.getWorld())) {
			return false;
		}

		if (player.getEyeLocation().distance(location) >= range) {
			if (progressing) {
				returnWater();
			}
			return false;
		}

		if (EntityTools.getBendingAbility(player) != BendingAbilities.IceSpike && prepared) {
			return false;
		}

		if (System.currentTimeMillis() < time + interval) {
			// Not enough time has passed to progress, just waiting
			return true;
		}

		time = System.currentTimeMillis();

		if (progressing) {
			Vector direction = null;

			if (location.getBlockY() == firstdestination.getBlockY()) {
				settingup = false;
			}

			if (location.distance(destination) <= 2) {
				returnWater();
				return false;
			}

			if (settingup) {
				direction = Tools.getDirection(location, firstdestination).normalize();
			} else {
				direction = Tools.getDirection(location, destination).normalize();
			}

			location.add(direction);

			Block block = location.getBlock();

			if (block.equals(sourceblock)) {
				return true;
			}

			source.revertBlock();
			source = null;

			if (BlockTools.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
				BlockTools.breakBlock(block);
			} else if (!BlockTools.isWater(block)) {
				returnWater();
				return false;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.IceSpike, location)) {
				returnWater();
				return false;
			}

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, affectingradius)) {
				if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (entity.getEntityId() != player.getEntityId()) {
					affect(entity);
					progressing = false;
					returnWater();
				}
			}

			if (!progressing) {
				return false;
			}

			sourceblock = block;
			source = new TempBlock(sourceblock, Material.ICE, data);

		} else if (prepared) {
			Tools.playFocusWaterEffect(sourceblock);
		}

		if (field != null) {
			return field.progress();
		}

		return true;
	}

	private void affect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		int mod = (int) PluginTools.waterbendingNightAugment(defaultmod, player.getWorld());
		double damage = (int) PluginTools.waterbendingNightAugment(defaultdamage, player.getWorld());
		if (entity instanceof Player) {
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(slowCooldown);
				entity.damage(damage, player);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
			new TempPotionEffect(entity, effect);
			entity.damage(damage, player);
		}

	}

	private static void redirect(Player player) {
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.IceSpike).values()) {
			IceSpike ice = (IceSpike) ab;

			if (!ice.progressing)
				continue;

			if (!ice.location.getWorld().equals(player.getWorld()))
				continue;

			if (ice.player.equals(player)) {
				Location location;
				Entity target = EntityTools.getTargettedEntity(player, defaultrange);
				if (target == null) {
					location = EntityTools.getTargetedLocation(player, defaultrange);
				} else {
					location = ((LivingEntity) target).getEyeLocation();
				}
				location = Tools.getPointOnLine(ice.location, location, defaultrange * 2);
				ice.redirect(location, player);
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = ice.location;
			if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.IceSpike, mloc))
				continue;
			if (mloc.distance(location) <= defaultrange && Tools.getDistanceFromLine(vector, location, ice.location) < deflectrange && mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
				Location loc;
				Entity target = EntityTools.getTargettedEntity(player, defaultrange);
				if (target == null) {
					loc = EntityTools.getTargetedLocation(player, defaultrange);
				} else {
					loc = ((LivingEntity) target).getEyeLocation();
				}
				loc = Tools.getPointOnLine(ice.location, loc, defaultrange * 2);
				ice.redirect(loc, player);
			}

		}
	}

	private static void block(Player player) {
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.IceSpike).values()) {
			IceSpike ice = (IceSpike) ab;

			if (ice.player.equals(player)) {
				continue;
			}

			if (!ice.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (!ice.progressing) {
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.IceSpike, ice.location)) {
				continue;
			}

			if (player != null) {
				Location location = player.getEyeLocation();
				Vector vector = location.getDirection();
				Location mloc = ice.location;
				if (mloc.distance(location) <= defaultrange && Tools.getDistanceFromLine(vector, location, ice.location) < deflectrange && mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1)))) {
					ice.state = BendingAbilityState.Ended;
				}
			}
		}
	}

	private void redirect(Location destination, Player player) {
		this.destination = destination;
		this.player = player;
	}

	/**
	 * Remove cleanly this ability in game, but does not remove it on instances
	 * list; assuming it is done after
	 */
	private void clear() {
		if (progressing) {
			if (source != null)
				source.revertBlock();
			progressing = false;
		}
	}

	@Override
	public void remove() {
		if (waterReturn != null) {
			waterReturn.remove();
		}
		this.clear();
		super.remove();
	}

	private void returnWater() {
		waterReturn = new WaterReturn(player, sourceblock, this);
	}

	public static boolean isBending(Player player) {
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.IceSpike).values()) {
			IceSpike ice = (IceSpike) ab;
			if (ice.player.equals(player))
				return true;
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return id;
	}

}

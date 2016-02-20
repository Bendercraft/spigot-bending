package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

@ABendingAbility(name = IceSpike.NAME, element = BendingElement.Water)
public class IceSpike extends BendingActiveAbility {
	public final static String NAME = "IceSpike";
	
	private static double defaultrange = 20;
	private static int defaultdamage = 1;
	private static int defaultmod = 2;
	private static int ID = Integer.MIN_VALUE;
	static long slowCooldown = 5000;

	private static final long interval = 20;
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

	public IceSpike(RegisteredAbility register, Player player) {
		super(register, player);
		if (EntityTools.canPlantbend(player)) {
			this.plantbending = true;
		}
		this.range = PluginTools.waterbendingNightAugment(defaultrange, player.getWorld());
		this.id = ID++;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.Start || getState() == BendingAbilityState.Prepared) {
			this.sourceblock = BlockTools.getWaterSourceBlock(this.player, this.range, this.plantbending);
			if (this.sourceblock == null) {
				this.field = new SpikeField(this.player, this);
				setState(BendingAbilityState.Progressing);
			} else {
				this.location = this.sourceblock.getLocation();
				this.prepared = true;
				setState(BendingAbilityState.Prepared);
			}
		}
		return false;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.Prepared) {
			if (WaterReturn.hasWaterBottle(this.player)) {
				Location eyeloc = this.player.getEyeLocation();
				Block block = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
				if (BlockTools.isTransparentToEarthbending(this.player, block) && BlockTools.isTransparentToEarthbending(this.player, eyeloc.getBlock())) {

					LivingEntity target = EntityTools.getTargetedEntity(this.player, defaultrange);
					Location destination;
					if (target == null) {
						destination = EntityTools.getTargetedLocation(this.player, defaultrange, BlockTools.getTransparentEarthBending());
					} else {
						destination = Tools.getPointOnLine(this.player.getEyeLocation(), target.getEyeLocation(), defaultrange);
					}

					if (destination.distance(block.getLocation()) < 1) {
						return false;
					}

					WaterReturn.emptyWaterBottle(this.player);
				}
			}
			throwIce();
			setState(BendingAbilityState.Progressing);
		} else {
			redirect(this.player);
		}
		return false;
	}

	private void throwIce() {
		if (!this.prepared) {
			return;
		}
		LivingEntity target = EntityTools.getTargetedEntity(this.player, this.range);
		if (target == null) {
			this.destination = EntityTools.getTargetedLocation(this.player, this.range, BlockTools.getTransparentEarthBending());
		} else {
			this.destination = target.getEyeLocation();
		}

		this.location = this.sourceblock.getLocation();
		if (this.destination.distance(this.location) < 1) {
			return;
		}
		this.firstdestination = this.location.clone();
		if ((this.destination.getY() - this.location.getY()) > 2) {
			this.firstdestination.setY(this.destination.getY() - 1);
		} else {
			this.firstdestination.add(0, 2, 0);
		}
		this.destination = Tools.getPointOnLine(this.firstdestination, this.destination, this.range);
		this.progressing = true;
		this.settingup = true;
		this.prepared = false;

		if (BlockTools.isPlant(this.sourceblock)) {
			this.sourceblock.setType(Material.AIR);
		} else if (!BlockTools.adjacentToThreeOrMoreSources(this.sourceblock)) {
			this.sourceblock.setType(Material.AIR);
		}

		//this.source = new TempBlock(this.sourceblock, Material.ICE, (byte) 0x0);
		this.source = TempBlock.makeTemporary(sourceblock, Material.ICE);
	}

	@Override
	public void progress() {
		if (this.waterReturn != null) {
			if(!this.waterReturn.progress()) {
				remove();
			}
			return;
		}

		if (this.field != null) {
			if(!this.field.progress()) {
				remove();
			}
			return;
		}

		if (this.player.getEyeLocation().distance(this.location) >= this.range) {
			if (this.progressing) {
				returnWater();
			} else {
				remove();
			}
			return;
		}

		if (!EntityTools.getBendingAbility(this.player).equals(NAME) && this.prepared) {
			remove();
			return;
		}

		if (System.currentTimeMillis() < (this.time + interval)) {
			// Not enough time has passed to progress, just waiting
			return;
		}

		this.time = System.currentTimeMillis();

		if (this.progressing) {
			Vector direction = null;

			if (this.location.getBlockY() == this.firstdestination.getBlockY()) {
				this.settingup = false;
			}

			if (this.location.distance(this.destination) <= 2) {
				returnWater();
				return;
			}

			if (this.settingup) {
				direction = Tools.getDirection(this.location, this.firstdestination).normalize();
			} else {
				direction = Tools.getDirection(this.location, this.destination).normalize();
			}

			this.location.add(direction);

			Block block = this.location.getBlock();

			if (block.equals(this.sourceblock)) {
				return;
			}

			this.source.revertBlock();
			this.source = null;

			if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
				BlockTools.breakBlock(block);
			} else if (!BlockTools.isWater(block)) {
				returnWater();
				return;
			}

			if (ProtectionManager.isRegionProtectedFromBending(this.player, NAME, this.location)) {
				returnWater();
				return;
			}

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, affectingradius)) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (entity.getEntityId() != this.player.getEntityId()) {
					affect(entity);
					this.progressing = false;
					returnWater();
				}
			}

			if (!this.progressing) {
				remove();
				return;
			}

			this.sourceblock = block;
			//this.source = new TempBlock(this.sourceblock, Material.ICE, (byte) 0x0);
			this.source = TempBlock.makeTemporary(sourceblock, Material.ICE);

		} else if (this.prepared) {
			Tools.playFocusWaterEffect(this.sourceblock);
		}
	}

	private void affect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(this.player);
		int mod = (int) PluginTools.waterbendingNightAugment(defaultmod, this.player.getWorld());
		double damage = (int) PluginTools.waterbendingNightAugment(defaultdamage, this.player.getWorld());
		if (entity instanceof Player) {
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
				entity.addPotionEffect(effect);
				bPlayer.slow(slowCooldown);
				entity.damage(damage, this.player);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
			entity.addPotionEffect(effect);
			entity.damage(damage, this.player);
		}
	}

	private static void redirect(Player player) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			IceSpike ice = (IceSpike) ab;

			if (!ice.progressing) {
				continue;
			}

			if (!ice.location.getWorld().equals(player.getWorld())) {
				continue;
			}

			if (ice.player.equals(player)) {
				Location location;
				Entity target = EntityTools.getTargetedEntity(player, defaultrange);
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
			if (ProtectionManager.isRegionProtectedFromBending(player, NAME, mloc)) {
				continue;
			}
			if ((mloc.distance(location) <= defaultrange) && (Tools.getDistanceFromLine(vector, location, ice.location) < deflectrange) && (mloc.distance(location.clone().add(vector)) < mloc.distance(location.clone().add(vector.clone().multiply(-1))))) {
				Location loc;
				Entity target = EntityTools.getTargetedEntity(player, defaultrange);
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

	private void redirect(Location destination, Player player) {
		this.destination = destination;
	}

	/**
	 * Remove cleanly this ability in game, but does not remove it on instances
	 * list; assuming it is done after
	 */
	private void clear() {
		if (this.progressing) {
			if (this.source != null) {
				this.source.revertBlock();
			}
			this.progressing = false;
		}
	}

	@Override
	public void stop() {
		if (this.field != null) {
			this.field.remove();
		}
		if (this.waterReturn != null) {
			this.waterReturn.stop();
		}
		this.clear();
	}

	private void returnWater() {
		this.waterReturn = new WaterReturn(this.player, this.sourceblock, this);
	}

	public static boolean isBending(Player player) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			IceSpike ice = (IceSpike) ab;
			if (ice.player.equals(player)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

}

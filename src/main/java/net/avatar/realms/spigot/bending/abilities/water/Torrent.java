package net.avatar.realms.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPathType;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Torrent", element=BendingType.Water)
public class Torrent extends ActiveAbility {
	private static Map<TempBlock, Player> frozenblocks = new HashMap<TempBlock, Player>();

	static long interval = 30;
	private static int defaultrange = 20;
	private static int selectrange = 10;
	private static double radius = 3;
	private static double RANGE = 25;

	@ConfigurationParameter("Damage")
	private static double DAMAGE = 6;

	@ConfigurationParameter("Throw-Factor")
	private static double DEFLECT_DAMAGE = 2;

	private static double factor = 1;
	private static int maxlayer = 3;
	private static double ylimit = 0.2;

	private static final byte full = 0x0;

	private double startangle = 0;
	private Block sourceblock;
	private TempBlock source;
	private Location location;
	private long time;
	private double angle = 20;
	private int layer = 0;

	private List<TempBlock> blocks = new ArrayList<TempBlock>();
	private List<TempBlock> launchblocks = new ArrayList<TempBlock>();

	private List<Entity> hurtentities = new ArrayList<Entity>();

	private boolean sourceselected = false;
	private boolean settingup = false;
	private boolean forming = false;
	private boolean formed = false;
	private boolean launch = false;
	private boolean launching = false;
	private boolean freeze = false;
	
	private double damage;
	private double range;

	private TorrentBurst burst;

	private WaterReturn waterReturn;

	public Torrent(Player player) {
		super(player, null);
		
		damage = DAMAGE;
		range = RANGE;
		bender = BendingPlayer.getBendingPlayer(player);
		if(bender.hasPath(BendingPathType.Marksman)) {
			range *= 1.4;
			damage *= 0.8;
		}
		
		if(bender.hasPath(BendingPathType.Flowless)) {
			damage *= 1.2;
		}
	}
	
	@Override
	public boolean sneak() {
		this.time = System.currentTimeMillis();
		this.sourceblock = BlockTools.getWaterSourceBlock(player, selectrange,
				EntityTools.canPlantbend(player));
		
		if (this.sourceblock == null && WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (BlockTools.isTransparentToEarthbending(player, block)
					&& BlockTools.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {
				WaterReturn.emptyWaterBottle(player);
			}
		}
		
		if (this.sourceblock != null) {
			this.sourceselected = true;
			AbilityManager.getManager().addInstance(this);
		}
		
		return false;
	}

	private void freeze() {
		if (this.layer == 0) {
			return;
		}
		if (!EntityTools.canBend(this.player, Abilities.PhaseChange)) {
			return;
		}
		List<Block> ice = BlockTools.getBlocksAroundPoint(this.location, this.layer);
		for (Block block : ice) {
			if (BlockTools.isTransparentToEarthbending(this.player, block)
					&& (block.getType() != Material.ICE)) {
				TempBlock tblock = new TempBlock(block, Material.ICE, (byte) 0);
				frozenblocks.put(tblock, this.player);
			}
		}
	}
	
	@Override
	public boolean swing() {
		launch = true;
		if (launching)
			freeze = true;
		
		return false;
	}

	@Override
	public boolean progress() {
		if(!super.progress()) {
			return false;
		}

		if (EntityTools.getBendingAbility(this.player) != Abilities.Torrent) {
			if (this.location != null) {
				returnWater(this.location);
			}
			return false;
		}
		
		if(waterReturn != null) {
			return waterReturn.progress();
		}
		
		if(burst != null) {
			if(!burst.progress()) {
				returnWater(location);
				return false;
			}
			return true;
		}

		if (System.currentTimeMillis() > (this.time + interval)) {
			this.time = System.currentTimeMillis();

			if (this.sourceselected) {
				if (!this.sourceblock.getWorld().equals(this.player.getWorld())) {
					return false;
				}

				if (this.sourceblock.getLocation().distance(this.player.getLocation()) > selectrange) {
					return false;
				}

				if (this.player.isSneaking()) {
					this.sourceselected = false;
					this.settingup = true;				

					if (BlockTools.isPlant(this.sourceblock)) {
						this.sourceblock.setType(Material.AIR);
					} else if (!BlockTools
							.adjacentToThreeOrMoreSources(this.sourceblock)) {
						this.sourceblock.setType(Material.AIR);
					}
					this.source = new TempBlock(this.sourceblock, Material.WATER, full);
					this.location = this.sourceblock.getLocation();
				} else {
					Tools.playFocusWaterEffect(this.sourceblock);
					return true;
				}
			}

			if (this.settingup) {
				if (!this.player.isSneaking()) {
					returnWater(this.source.getLocation());
					return false;
				}
				Location eyeloc = this.player.getEyeLocation();
				double startangle = this.player.getEyeLocation().getDirection()
						.angle(new Vector(1, 0, 0));
				double dx = radius * Math.cos(startangle);
				double dy = 0;
				double dz = radius * Math.sin(startangle);
				Location setup = eyeloc.clone().add(dx, dy, dz);

				if (!this.location.getWorld().equals(this.player.getWorld())) {
					return false;
				}

				if (this.location.distance(setup) > defaultrange) {
					return false;
				}

				if (this.location.getBlockY() > setup.getBlockY()) {
					Vector direction = new Vector(0, -1, 0);
					this.location = this.location.clone().add(direction);
				} else if (this.location.getBlockY() < setup.getBlockY()) {
					Vector direction = new Vector(0, 1, 0);
					this.location = this.location.clone().add(direction);
				} else {
					Vector direction = Tools.getDirection(this.location, setup)
							.normalize();
					this.location = this.location.clone().add(direction);
				}

				if (this.location.distance(setup) <= 1) {
					this.settingup = false;
					this.source.revertBlock();
					this.source = null;
					this.forming = true;
				} else {
					if (!this.location.getBlock().equals(
							this.source.getLocation().getBlock())) {
						this.source.revertBlock();
						this.source = null;
						Block block = this.location.getBlock();
						if (!BlockTools.isTransparentToEarthbending(this.player,
								block) || block.isLiquid()) {
							return false;
						}
						this.source = new TempBlock(this.location.getBlock(),
								Material.WATER, full);
					}
				}
			}

			if (this.forming && !this.player.isSneaking()) {
				returnWater(this.player.getEyeLocation().add(radius, 0, 0));
				return false;
			}

			if (this.forming || this.formed) {
				if (this.angle < 220) {
					this.angle += 20;
				} else {
					this.forming = false;
					this.formed = true;
				}
				formRing();
				if (this.blocks.isEmpty()) {
					return false;
				}
			}

			if (this.formed && !this.player.isSneaking() && !this.launch) {
				burst = new TorrentBurst(this.player, radius, this);
				return true;
			}

			if (this.launch && this.formed) {
				this.launching = true;
				this.launch = false;
				this.formed = false;
				if (!launch()) {
					return false;
				}
			}

			if (this.launching) {
				if (!this.player.isSneaking()) {
					return false;
				}
				if (!launch()) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean launch() {
		if (this.launchblocks.isEmpty() && this.blocks.isEmpty()) {
			return false;
		}

		if (this.launchblocks.isEmpty()) {
			clearRing();

			Location loc = this.player.getEyeLocation();
			ArrayList<Block> doneblocks = new ArrayList<Block>();
			for (double theta = this.startangle; theta < (this.angle + this.startangle); theta += 20) {
				double phi = Math.toRadians(theta);
				double dx = Math.cos(phi) * radius;
				double dy = 0;
				double dz = Math.sin(phi) * radius;
				Location blockloc = loc.clone().add(dx, dy, dz);
				if (Math.abs(theta - this.startangle) < 10) {
					this.location = blockloc.clone();
				}
				Block block = blockloc.getBlock();
				if (!doneblocks.contains(block)
						&& !ProtectionManager.isRegionProtectedFromBending(this.player,
								Abilities.Torrent, blockloc)) {
					if (BlockTools.isTransparentToEarthbending(this.player, block)
							&& !block.isLiquid()) {
						this.launchblocks.add(new TempBlock(block, Material.WATER,
								full));
						doneblocks.add(block);
					} else if (!BlockTools.isTransparentToEarthbending(this.player,
							block)) {
						break;
					}						
				}
			}
			if (this.launchblocks.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}

		Entity target = null;
		if(!bender.hasPath(BendingPathType.Flowless)) {
			target = EntityTools.getTargettedEntity(this.player, range,
					this.hurtentities);
		}
		Location targetloc = EntityTools.getTargetBlock(this.player, range,
				BlockTools.getTransparentEarthbending()).getLocation();

		if ((target != null) && !ProtectionManager.isEntityProtectedByCitizens(target)) {
			targetloc = target.getLocation();
		}

		ArrayList<TempBlock> newblocks = new ArrayList<TempBlock>();

		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(
				this.player.getLocation(), range + 5);
		List<Entity> affectedentities = new ArrayList<Entity>();

		Block realblock = this.launchblocks.get(0).getBlock();

		Vector dir = Tools.getDirection(this.location, targetloc).normalize();

		if (target != null) {
			targetloc = this.location.clone().add(dir.clone().multiply(10));
		}

		// Tools.verbose(layer);
		if (this.layer == 0) {
			this.location = this.location.clone().add(dir);
		}

		Block b = this.location.getBlock();

		// player.sendBlockChange(location, 20, (byte) 0);

		if ((this.location.distance(this.player.getLocation()) > range)
				|| ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.Torrent,
						this.location)) {
			if (this.layer < maxlayer) {
				if (this.freeze || (this.layer < 1)) {
					this.layer++;
				}
			}
			if (this.launchblocks.size() == 1) {
				returnWater(this.location);
				return false;
			}
		} else if (!BlockTools.isTransparentToEarthbending(this.player, b)) {
			// b.setType(Material.GLASS);
			if (this.layer < maxlayer) {
				// Tools.verbose(layer);
				if (this.layer == 0) {
					this.hurtentities.clear();
				}
				if (this.freeze || (this.layer < 1)) {
					this.layer++;
				}
			}
			if (this.freeze) {
				freeze();
			} else if (this.launchblocks.size() == 1) {
				returnWater(realblock.getLocation());
				return false;
			}
		} else {
			if (b.equals(realblock) && (this.layer == 0)) {
				// Tools.verbose(dir);
				return true;
			}
			if (b.getLocation().distance(targetloc) > 1) {
				newblocks.add(new TempBlock(b, Material.WATER, full));
			} else {
				if (this.layer < maxlayer) {
					if (this.layer == 0) {
						this.hurtentities.clear();
					}
					if (this.freeze || (this.layer < 1)) {
						this.layer++;
					}
				}
				if (this.freeze) {
					freeze();
				}
			}
		}

		for (int i = 0; i < this.launchblocks.size(); i++) {
			TempBlock block = this.launchblocks.get(i);
			if (i == (this.launchblocks.size() - 1)) {
				block.revertBlock();
			} else {
				newblocks.add(block);
				for (LivingEntity entity : entities) {
					if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
						continue;
					}
					if (entity.getWorld() != block.getBlock().getWorld()) {
						continue;
					}
					if ((entity.getLocation().distance(block.getLocation()) <= 1.5)
							&& !affectedentities.contains(entity)) {
						if (i == 0) {
							affect(entity, dir);
						} else {
							affect(entity,
									Tools.getDirection(
											block.getLocation(),
											this.launchblocks.get(i - 1)
											.getLocation()).normalize());
						}
						affectedentities.add(entity);
					}
				}
			}
		}

		this.launchblocks.clear();
		this.launchblocks.addAll(newblocks);

		if (this.launchblocks.isEmpty()) {
			return false;
		}

		return true;
	}

	private void formRing() {
		clearRing();
		this.startangle += 30;
		Location loc = this.player.getEyeLocation();
		ArrayList<Block> doneblocks = new ArrayList<Block>();
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(
				loc, radius + 2);
		List<Entity> affectedentities = new ArrayList<Entity>();
		for (double theta = this.startangle; theta < (this.angle + this.startangle); theta += 20) {
			double phi = Math.toRadians(theta);
			double dx = Math.cos(phi) * radius;
			double dy = 0;
			double dz = Math.sin(phi) * radius;
			Location blockloc = loc.clone().add(dx, dy, dz);
			Block block = blockloc.getBlock();
			if (!doneblocks.contains(block)
					&& !ProtectionManager.isRegionProtectedFromBending(this.player,
							Abilities.Torrent, blockloc)) {
				if (BlockTools.isTransparentToEarthbending(this.player, block)
						&& !block.isLiquid()) {
					this.blocks.add(new TempBlock(block, Material.WATER, full));
					doneblocks.add(block);
					for (LivingEntity entity : entities) {
						if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
							continue;
						}
						if (entity.getWorld() != blockloc.getWorld()) {
							continue;
						}
						if (!affectedentities.contains(entity)
								&& (entity.getLocation().distance(blockloc) <= 1.5)) {
							deflect(entity);
						}
					}
				}
			}
		}
	}

	private void clearRing() {
		for (TempBlock block : this.blocks) {
			block.revertBlock();
		}
		this.blocks.clear();
	}

	private void clear() {
		clearRing();
		for (TempBlock block : this.launchblocks) {
			block.revertBlock();
		}
		this.launchblocks.clear();
		for (TempBlock block : this.blocks) {
			block.revertBlock();
		}
		this.blocks.clear();
		if (this.source != null) {
			this.source.revertBlock();
		}
	}

	@Override
	public void remove() {
		this.clear();
		if(waterReturn != null) {
			waterReturn.remove();
		}
		if(burst != null) {
			burst.remove();
		}
		super.remove();
	}

	private void returnWater(Location location) {
		this.clear();
		waterReturn = new WaterReturn(this.player, location.getBlock(), this);
	}

	private void deflect(LivingEntity entity) {
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		if (entity.getEntityId() == this.player.getEntityId()) {
			return;
		}
		double x, z, vx, vz, mag;
		double angle = 50;
		angle = Math.toRadians(angle);

		x = entity.getLocation().getX() - this.player.getLocation().getX();
		z = entity.getLocation().getZ() - this.player.getLocation().getZ();

		mag = Math.sqrt((x * x) + (z * z));

		vx = ((x * Math.cos(angle)) - (z * Math.sin(angle))) / mag;
		vz = ((x * Math.sin(angle)) + (z * Math.cos(angle))) / mag;

		Vector vec = new Vector(vx, 0, vz).normalize().multiply(factor);

		Vector velocity = entity.getVelocity();
		if (AvatarState.isAvatarState(this.player)) {
			velocity.setX(AvatarState.getValue(vec.getX()));
			velocity.setZ(AvatarState.getValue(vec.getZ()));
		} else {
			velocity.setX(vec.getX());
			velocity.setZ(vec.getY());
		}
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if(bender.hasPath(BendingPathType.Marksman)) {
			velocity = velocity.multiply(1.5);
		}
		entity.setVelocity(velocity);
		entity.setFallDistance(0);

		World world = this.player.getWorld();
		
		if(!bender.hasPath(BendingPathType.Marksman)) {
			double damagedealt = DEFLECT_DAMAGE;
			if (Tools.isNight(world)) {
				damagedealt = (PluginTools.getWaterbendingNightAugment(world) * DEFLECT_DAMAGE);
			}
			EntityTools.damageEntity(this.player, entity, damagedealt);
		}

	}

	private void affect(LivingEntity entity, Vector direction) {
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		if (entity.getEntityId() == this.player.getEntityId()) {
			return;
		}

		if (direction.getY() > ylimit) {
			direction.setY(ylimit);
		}
		if (!this.freeze) {
			entity.setVelocity(direction.multiply(factor));
		}

		if (!this.hurtentities.contains(entity)) {
			World world = this.player.getWorld();
			double damagedealt = damage;
			if (Tools.isNight(world)) {
				damagedealt = (PluginTools
						.getWaterbendingNightAugment(world) * damage);
			}

			EntityTools.damageEntity(this.player, entity, damagedealt);
			this.hurtentities.add(entity);

			entity.setNoDamageTicks(0);
		}
	}

	public static void thaw(Block block) {
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			if (frozenblocks.containsKey(tblock)) {
				thaw(tblock);
			}
		}
	}

	public static void thaw(TempBlock block) {
		block.revertBlock();
		frozenblocks.remove(block);
	}

	public static boolean canThaw(Block block) {
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			return !frozenblocks.containsKey(tblock);
		}
		return true;
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (AbilityManager.getManager().getInstances(Abilities.Torrent).containsKey(player)) {
			Torrent torrent = (Torrent) AbilityManager.getManager().getInstances(Abilities.Torrent).get(player);
			if (torrent.sourceblock == null) {
				return false;
			}
			if (torrent.sourceblock.equals(block)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.Torrent;
	}

}

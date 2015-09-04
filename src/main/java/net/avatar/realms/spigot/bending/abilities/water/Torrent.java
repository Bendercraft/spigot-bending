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
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Torrent", element=BendingType.Water)
public class Torrent implements IAbility {
	private static Map<Player, Torrent> instances = new HashMap<Player, Torrent>();
	private static Map<TempBlock, Player> frozenblocks = new HashMap<TempBlock, Player>();

	static long interval = 30;
	private static int defaultrange = 20;
	private static int selectrange = 10;
	private static double radius = 3;
	static double range = 25;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 6;

	@ConfigurationParameter("Throw-Factor")
	private static int DEFLECT_DAMAGE = 2;

	private static double factor = 1;
	private static int maxlayer = 3;
	private static double ylimit = 0.2;

	private static final byte full = 0x0;

	private double startangle = 0;
	private Block sourceblock;
	private TempBlock source;
	private Location location;
	private Player player;
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

	private IAbility parent;

	public Torrent(Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player)) {
			Torrent torrent = instances.get(player);
			if (!torrent.sourceselected) {
				instances.get(player).use();
				return;
			}
		}
		this.player = player;
		this.time = System.currentTimeMillis();
		this.sourceblock = BlockTools.getWaterSourceBlock(player, selectrange,
				EntityTools.canPlantbend(player));
		if (this.sourceblock != null) {
			this.sourceselected = true;
			instances.put(player, this);
		}
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

	private boolean progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			return false;
		}

		if (!EntityTools.canBend(this.player, Abilities.Torrent)) {
			return false;
		}

		if (EntityTools.getBendingAbility(this.player) != Abilities.Torrent) {
			if (this.location != null) {
				returnWater(this.location);
			}
			return false;
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
						new Plantbending(this.sourceblock, this);
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
				new TorrentBurst(this.player, radius, this);
				return false;
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

		Entity target = EntityTools.getTargettedEntity(this.player, range,
				this.hurtentities);
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
		if (this.source != null) {
			this.source.revertBlock();
		}
	}

	private void remove() {
		this.clear();
		instances.remove(this.player);
	}

	private void returnWater(Location location) {
		new WaterReturn(this.player, location.getBlock(), this);
	}

	public static void use(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).use();
		}
	}

	@SuppressWarnings("deprecation")
	public static void create(Player player) {
		if (instances.containsKey(player)) {
			return;
		}

		if (WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (BlockTools.isTransparentToEarthbending(player, block)
					&& BlockTools.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(full);
				Torrent tor = new Torrent(player, null);
				if (tor.sourceselected || tor.settingup) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}
			}
		}
	}

	private void use() {
		this.launch = true;
		if (this.launching) {
			this.freeze = true;
		}
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

		entity.setVelocity(velocity);
		entity.setFallDistance(0);

		World world = this.player.getWorld();
		int damagedealt = DEFLECT_DAMAGE;
		if (Tools.isNight(world)) {
			damagedealt = (int) (PluginTools.getWaterbendingNightAugment(world) * DEFLECT_DAMAGE);
		}
		EntityTools.damageEntity(this.player, entity, damagedealt);

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
			int damagedealt = DAMAGE;
			if (Tools.isNight(world)) {
				damagedealt = (int) (PluginTools
						.getWaterbendingNightAugment(world) * DAMAGE);
			}

			EntityTools.damageEntity(this.player, entity, damagedealt);
			this.hurtentities.add(entity);

			entity.setNoDamageTicks(0);
		}
	}

	public static void progressAll() {
		List<Torrent> toRemove = new LinkedList<Torrent>();
		//Concurrent modification exception here
		for (Torrent torrent : instances.values()) {
			boolean keep = torrent.progress();
			if (!keep) {
				toRemove.add(torrent);
			}
		}

		for (Torrent torrent : toRemove) {
			torrent.remove();
		}

		List<TempBlock> toRemoveIce = new LinkedList<TempBlock>();
		List<TempBlock> toThawIce = new LinkedList<TempBlock>();
		for (TempBlock block : frozenblocks.keySet()) {
			Player player = frozenblocks.get(block);
			if (block.getBlock().getType() != Material.ICE) {
				toRemoveIce.add(block);
				continue;
			}
			if (block.getBlock().getWorld() != player.getWorld()) {
				toThawIce.add(block);
				continue;
			}
			if ((block.getLocation().distance(player.getLocation()) > range)
					|| !EntityTools.canBend(player, Abilities.Torrent)) {
				toThawIce.add(block);
			}
		}
		for (TempBlock block : toRemoveIce) {
			frozenblocks.remove(block);
		}
		for (TempBlock block : toThawIce) {
			thaw(block);
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

	public static void removeAll() {
		for (Torrent torrent : instances.values()) {
			torrent.clear();
		}

		instances.clear();

		for (TempBlock block : frozenblocks.keySet()) {
			block.revertBlock();
		}

		frozenblocks.clear();
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (instances.containsKey(player)) {
			Torrent torrent = instances.get(player);
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
	public IAbility getParent() {
		return this.parent;
	}

}

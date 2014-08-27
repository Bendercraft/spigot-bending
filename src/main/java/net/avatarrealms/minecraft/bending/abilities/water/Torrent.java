package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.model.TempBlock;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Torrent implements IAbility {
	private static Map<Player, Torrent> instances = new HashMap<Player, Torrent>();
	private static Map<TempBlock, Player> frozenblocks = new HashMap<TempBlock, Player>();

	static long interval = 30;
	private static int defaultrange = 20;
	private static int selectrange = 10;
	private static double radius = 3;
	static double range = 25;
	private static int damage = ConfigManager.torrentDamage;
	private static int deflectdamage = ConfigManager.torrentDeflectDamage;
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
		time = System.currentTimeMillis();
		sourceblock = BlockTools.getWaterSourceBlock(player, selectrange,
				EntityTools.canPlantbend(player));
		if (sourceblock != null) {
			sourceselected = true;
			instances.put(player, this);
		}
	}

	private void freeze() {
		if (layer == 0)
			return;
		if (!EntityTools.hasAbility(player, Abilities.PhaseChange))
			return;
		List<Block> ice = BlockTools.getBlocksAroundPoint(location, layer);
		for (Block block : ice) {
			if (BlockTools.isTransparentToEarthbending(player, block)
					&& block.getType() != Material.ICE) {
				TempBlock tblock = new TempBlock(block, Material.ICE, (byte) 0);
				frozenblocks.put(tblock, player);
			}
		}
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (!EntityTools.canBend(player, Abilities.Torrent)) {
			return false;
		}

		if (EntityTools.getBendingAbility(player) != Abilities.Torrent) {
			if (location != null)
				returnWater(location);
			return false;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();

			if (sourceselected) {
				if (!sourceblock.getWorld().equals(player.getWorld())) {
					return false;
				}

				if (sourceblock.getLocation().distance(player.getLocation()) > selectrange) {
					return false;
				}

				if (player.isSneaking()) {
					sourceselected = false;
					settingup = true;
					if (BlockTools.isPlant(sourceblock)) {
						new Plantbending(sourceblock, this);
						sourceblock.setType(Material.AIR);
					} else if (!BlockTools
							.adjacentToThreeOrMoreSources(sourceblock)) {
						sourceblock.setType(Material.AIR);
					}
					source = new TempBlock(sourceblock, Material.WATER, full);
					location = sourceblock.getLocation();
				} else {
					Tools.playFocusWaterEffect(sourceblock);
					return true;
				}
			}

			if (settingup) {
				if (!player.isSneaking()) {
					returnWater(source.getLocation());
					return false;
				}
				Location eyeloc = player.getEyeLocation();
				double startangle = player.getEyeLocation().getDirection()
						.angle(new Vector(1, 0, 0));
				double dx = radius * Math.cos(startangle);
				double dy = 0;
				double dz = radius * Math.sin(startangle);
				Location setup = eyeloc.clone().add(dx, dy, dz);

				if (!location.getWorld().equals(player.getWorld())) {
					return false;
				}

				if (location.distance(setup) > defaultrange) {
					return false;
				}

				if (location.getBlockY() > setup.getBlockY()) {
					Vector direction = new Vector(0, -1, 0);
					location = location.clone().add(direction);
				} else if (location.getBlockY() < setup.getBlockY()) {
					Vector direction = new Vector(0, 1, 0);
					location = location.clone().add(direction);
				} else {
					Vector direction = Tools.getDirection(location, setup)
							.normalize();
					location = location.clone().add(direction);
				}

				if (location.distance(setup) <= 1) {
					settingup = false;
					source.revertBlock();
					source = null;
					forming = true;
				} else {
					if (!location.getBlock().equals(
							source.getLocation().getBlock())) {
						source.revertBlock();
						source = null;
						Block block = location.getBlock();
						if (!BlockTools.isTransparentToEarthbending(player,
								block) || block.isLiquid()) {
							return false;
						}
						source = new TempBlock(location.getBlock(),
								Material.WATER, full);
					}
				}
			}

			if (forming && !player.isSneaking()) {
				returnWater(player.getEyeLocation().add(radius, 0, 0));
				return false;
			}

			if (forming || formed) {
				if (angle < 220) {
					angle += 20;
				} else {
					forming = false;
					formed = true;
				}
				formRing();
				if (blocks.isEmpty()) {
					return false;
				}
			}

			if (formed && !player.isSneaking() && !launch) {
				new TorrentBurst(player, radius, this);
				return false;
			}

			if (launch && formed) {
				launching = true;
				launch = false;
				formed = false;
				if (!launch()) {
					return false;
				}
			}

			if (launching) {
				if (!player.isSneaking()) {
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
		if (launchblocks.isEmpty() && blocks.isEmpty()) {
			return false;
		}

		if (launchblocks.isEmpty()) {
			clearRing();
	
			Location loc = player.getEyeLocation();
			ArrayList<Block> doneblocks = new ArrayList<Block>();
			for (double theta = startangle; theta < angle + startangle; theta += 20) {
				double phi = Math.toRadians(theta);
				double dx = Math.cos(phi) * radius;
				double dy = 0;
				double dz = Math.sin(phi) * radius;
				Location blockloc = loc.clone().add(dx, dy, dz);
				if (Math.abs(theta - startangle) < 10)
					location = blockloc.clone();
				Block block = blockloc.getBlock();
				if (!doneblocks.contains(block)
						&& !Tools.isRegionProtectedFromBuild(player,
								Abilities.Torrent, blockloc)) {
					if (BlockTools.isTransparentToEarthbending(player, block)
							&& !block.isLiquid()) {
						launchblocks.add(new TempBlock(block, Material.WATER,
								full));
						doneblocks.add(block);
					} else if (!BlockTools.isTransparentToEarthbending(player,
							block)) {
						break;
					}						
				}
			}
			if (launchblocks.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}

		Entity target = EntityTools.getTargettedEntity(player, range,
				hurtentities);
		Location targetloc = EntityTools.getTargetBlock(player, range,
				BlockTools.getTransparentEarthbending()).getLocation();

		if (target != null) {
			targetloc = target.getLocation();
		}

		ArrayList<TempBlock> newblocks = new ArrayList<TempBlock>();

		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(
				player.getLocation(), range + 5);
		List<Entity> affectedentities = new ArrayList<Entity>();

		Block realblock = launchblocks.get(0).getBlock();

		Vector dir = Tools.getDirection(location, targetloc).normalize();

		if (target != null) {
			targetloc = location.clone().add(dir.clone().multiply(10));
		}

		// Tools.verbose(layer);
		if (layer == 0)
			location = location.clone().add(dir);

		Block b = location.getBlock();

		// player.sendBlockChange(location, 20, (byte) 0);

		if (location.distance(player.getLocation()) > range
				|| Tools.isRegionProtectedFromBuild(player, Abilities.Torrent,
						location)) {
			if (layer < maxlayer)
				if (freeze || layer < 1)
					layer++;
			if (launchblocks.size() == 1) {
				remove();
				returnWater(location);
				return false;
			}
		} else if (!BlockTools.isTransparentToEarthbending(player, b)) {
			// b.setType(Material.GLASS);
			if (layer < maxlayer) {
				// Tools.verbose(layer);
				if (layer == 0)
					hurtentities.clear();
				if (freeze || layer < 1)
					layer++;
			}
			if (freeze) {
				freeze();
			} else if (launchblocks.size() == 1) {
				remove();
				returnWater(realblock.getLocation());
				return false;
			}
		} else {
			if (b.equals(realblock) && layer == 0) {
				// Tools.verbose(dir);
				return true;
			}
			if (b.getLocation().distance(targetloc) > 1) {
				newblocks.add(new TempBlock(b, Material.WATER, full));
			} else {
				if (layer < maxlayer) {
					if (layer == 0)
						hurtentities.clear();
					if (freeze || layer < 1)
						layer++;
				}
				if (freeze) {
					freeze();
				}
			}
		}

		for (int i = 0; i < launchblocks.size(); i++) {
			TempBlock block = launchblocks.get(i);
			if (i == launchblocks.size() - 1) {
				block.revertBlock();
			} else {
				newblocks.add(block);
				for (LivingEntity entity : entities) {
					if (entity.getWorld() != block.getBlock().getWorld()) {
						continue;
					}
					if (entity.getLocation().distance(block.getLocation()) <= 1.5
							&& !affectedentities.contains(entity)) {
						if (i == 0) {
							affect(entity, dir);
						} else {
							affect(entity,
									Tools.getDirection(
											block.getLocation(),
											launchblocks.get(i - 1)
													.getLocation()).normalize());
						}
						affectedentities.add(entity);
					}
				}
			}
		}

		launchblocks.clear();
		launchblocks.addAll(newblocks);

		if (launchblocks.isEmpty())
			return false;

		return true;
	}

	private void formRing() {
		clearRing();
		startangle += 30;
		Location loc = player.getEyeLocation();
		ArrayList<Block> doneblocks = new ArrayList<Block>();
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(
				loc, radius + 2);
		List<Entity> affectedentities = new ArrayList<Entity>();
		for (double theta = startangle; theta < angle + startangle; theta += 20) {
			double phi = Math.toRadians(theta);
			double dx = Math.cos(phi) * radius;
			double dy = 0;
			double dz = Math.sin(phi) * radius;
			Location blockloc = loc.clone().add(dx, dy, dz);
			Block block = blockloc.getBlock();
			if (!doneblocks.contains(block)
					&& !Tools.isRegionProtectedFromBuild(player,
							Abilities.Torrent, blockloc)) {
				if (BlockTools.isTransparentToEarthbending(player, block)
						&& !block.isLiquid()) {
					blocks.add(new TempBlock(block, Material.WATER, full));
					doneblocks.add(block);
					for (LivingEntity entity : entities) {
						if (entity.getWorld() != blockloc.getWorld()) {
							continue;
						}
						if (!affectedentities.contains(entity)
								&& entity.getLocation().distance(blockloc) <= 1.5) {
							deflect(entity);
						}
					}
				}
			}
		}
	}

	private void clearRing() {
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
		blocks.clear();
	}

	private void clear() {
		clearRing();
		for (TempBlock block : launchblocks)
			block.revertBlock();
		launchblocks.clear();
		if (source != null)
			source.revertBlock();
	}

	private void remove() {
		this.clear();
		instances.remove(player);
	}

	private void returnWater(Location location) {
		new WaterReturn(player, location.getBlock(), this);
	}

	public static void use(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).use();
		}
	}

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
		launch = true;
		if (launching)
			freeze = true;
	}

	private void deflect(LivingEntity entity) {
		if (entity.getEntityId() == player.getEntityId()) {
			return;
		}
		double x, z, vx, vz, mag;
		double angle = 50;
		angle = Math.toRadians(angle);

		x = entity.getLocation().getX() - player.getLocation().getX();
		z = entity.getLocation().getZ() - player.getLocation().getZ();

		mag = Math.sqrt(x * x + z * z);

		vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
		vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

		Vector vec = new Vector(vx, 0, vz).normalize().multiply(factor);

		Vector velocity = entity.getVelocity();
		if (AvatarState.isAvatarState(player)) {
			velocity.setX(AvatarState.getValue(vec.getX()));
			velocity.setZ(AvatarState.getValue(vec.getZ()));
		} else {
			velocity.setX(vec.getX());
			velocity.setZ(vec.getY());
		}

		entity.setVelocity(velocity);
		entity.setFallDistance(0);

		World world = player.getWorld();
		int damagedealt = deflectdamage;
		if (Tools.isNight(world)) {
			damagedealt = (int) (PluginTools.getWaterbendingNightAugment(world) * (double) deflectdamage);
		}
		EntityTools.damageEntity(player, entity, damagedealt);

	}

	private void affect(LivingEntity entity, Vector direction) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (entity.getEntityId() == player.getEntityId()) {
			return;
		}

		if (direction.getY() > ylimit) {
			direction.setY(ylimit);
		}
		if (!freeze) {
			entity.setVelocity(direction.multiply(factor));
		}

		if (!hurtentities.contains(entity)) {
			World world = player.getWorld();
			int damagedealt = damage;
			if (Tools.isNight(world)) {
				damagedealt = (int) (PluginTools
						.getWaterbendingNightAugment(world) * (double) damage);
			}
			if (((entity instanceof Player) || (entity instanceof Monster))
					&& (entity.getEntityId() != player.getEntityId())) {
				if (bPlayer != null) {
					bPlayer.earnXP(BendingType.Water, this);
				}
			}

			EntityTools.damageEntity(player, entity,
					bPlayer.getCriticalHit(BendingType.Water, damagedealt));
			hurtentities.add(entity);

			entity.setNoDamageTicks(0);
		}
	}

	public static void progressAll() {
		List<Torrent> toRemove = new LinkedList<Torrent>();
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
			if (block.getLocation().distance(player.getLocation()) > range
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
			if (frozenblocks.containsKey(tblock))
				thaw(tblock);
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
		for (Torrent torrent : instances.values())
			torrent.clear();

		instances.clear();

		for (TempBlock block : frozenblocks.keySet())
			block.revertBlock();

		frozenblocks.clear();
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (instances.containsKey(player)) {
			Torrent torrent = instances.get(player);
			if (torrent.sourceblock == null)
				return false;
			if (torrent.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	public static String getDescription() {
		return "Torrent is one of the strongest moves in a waterbender's arsenal. To use, first click a source block to select it; then hold shift to begin streaming the water around you. Water flowing around you this way will damage and knock back nearby enemies and projectiles. If you release shift during this, you will create a large wave that expands outwards from you, launching anything in its path back. Instead, if you click you release the water and channel it to flow towards your cursor. Anything caught in the blast will be tossed about violently and take damage. Finally, if you click again when the water is torrenting, it will freeze the area around it when it is obstructed.";
	}

	@Override
	public int getBaseExperience() {
		return 6;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

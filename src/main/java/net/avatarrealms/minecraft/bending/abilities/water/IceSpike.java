package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.model.TempPotionEffect;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class IceSpike implements IAbility {
	private static Map<Integer, IceSpike> instances = new HashMap<Integer, IceSpike>();
	
	private Map<Player, Long> removeTimers = new HashMap<Player, Long>();
	private static Map<Player, Long> cooldowns = new HashMap<Player, Long>();
	private static long removeTimer = 500;
	private static Map<Block, Integer> baseblocks = new HashMap<Block, Integer>();

	private static int ID = Integer.MIN_VALUE;

	private static double range = ConfigManager.icespikeRange;
	private long cooldown = ConfigManager.icespikeCooldown;
	private static double speed = 25;
	private static final Vector direction = new Vector(0, 1, 0);

	private static long interval = (long) (1000. / speed);

	private Location origin;
	private Location location;
	private Block block;
	private Player player;
	private int progress = 0;
	private int damage = ConfigManager.icespikeDamage;
	int id;
	private long time;
	int height = 2;
	private Vector thrown = new Vector(0, ConfigManager.icespikeThrowingMult, 0);
	private Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private List<LivingEntity> damaged = new ArrayList<LivingEntity>();
	private IAbility parent;

	public IceSpike(Player player, IAbility parent) {
		this.parent = parent;
		if (cooldowns.containsKey(player))
			if (cooldowns.get(player) + cooldown >= System.currentTimeMillis())
				return;
		try {
			this.player = player;

			double lowestdistance = range + 1;
			Entity closestentity = null;
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(
					player.getLocation(), range)) {
				if (Tools.getDistanceFromLine(player.getLocation()
						.getDirection(), player.getLocation(), entity
						.getLocation()) <= 2
						&& (entity.getEntityId() != player.getEntityId())) {
					double distance = player.getLocation().distance(
							entity.getLocation());
					if (distance < lowestdistance) {
						closestentity = entity;
						lowestdistance = distance;
					}
				}
			}
			if (closestentity != null) {
				Block temptestingblock = closestentity.getLocation().getBlock()
						.getRelative(BlockFace.DOWN, 1);
				// if (temptestingblock.getType() == Material.ICE){
				this.block = temptestingblock;
				// }
			} else {
				this.block = EntityTools.getTargetBlock(player, range);
			}
			origin = block.getLocation();
			location = origin.clone();

		} catch (IllegalStateException e) {
			return;
		}

		loadAffectedBlocks();

		if (height != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
				cooldowns.put(player, System.currentTimeMillis());
			}
		}
	}

	public IceSpike(Player player, Location origin, int damage,
			Vector throwing, long aoecooldown, IAbility parent) {
		this.parent = parent;
		this.cooldown = aoecooldown;
		this.player = player;
		this.origin = origin;
		location = origin.clone();
		block = location.getBlock();
		this.damage = damage;
		this.thrown = throwing;

		loadAffectedBlocks();

		if (block.getType() == Material.ICE) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	private void loadAffectedBlocks() {
		affectedblocks.clear();
		Block thisblock;
		for (int i = 1; i <= height; i++) {
			thisblock = block.getWorld().getBlockAt(
					location.clone().add(direction.clone().multiply(i)));
			affectedblocks.put(thisblock, thisblock);
		}
	}

	private boolean blockInAffectedBlocks(Block block) {
		return affectedblocks.containsKey(block);
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (IceSpike spike : instances.values()) {
			if (spike.blockInAffectedBlocks(block))
				return true;
		}
		return false;
	}

	public static void revertBlock(Block block) {
		for (IceSpike spike : instances.values()) {
			if (spike.blockInAffectedBlocks(block)) {
				spike.affectedblocks.remove(block);
			}
		}
	}

	private boolean canInstantiate() {
		if (block.getType() != Material.ICE)
			return false;
		for (Block block : affectedblocks.keySet()) {
			if (blockInAllAffectedBlocks(block)
					|| block.getType() != Material.AIR
					|| (block.getX() == player.getEyeLocation().getBlock()
							.getX() && block.getZ() == player.getEyeLocation()
							.getBlock().getZ())) {
				return false;
			}
		}
		return true;
	}
	
	public static void progressAll() {
		List<IceSpike> toRemove = new LinkedList<IceSpike>();
		for(IceSpike spike : instances.values()) {
			boolean keep = spike.progress();
			if(!keep) {
				toRemove.add(spike);
			}
		}
		
		for(IceSpike spike : toRemove) {
			spike.remove();
		}
	}
	
	private void remove() {
		instances.remove(id);
	}

	private boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (progress < height) {
				moveEarth();
				removeTimers.put(player, System.currentTimeMillis());
			} else {
				if (removeTimers.get(player) + removeTimer <= System
						.currentTimeMillis()) {
					baseblocks.put(
							location.clone()
									.add(direction.clone().multiply(
											-1 * (height))).getBlock(),
							(height - 1));
					if (!revertblocks()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean moveEarth() {
		progress++;
		Block affectedblock = location.clone().add(direction).getBlock();
		location = location.add(direction);
		if (Tools.isRegionProtectedFromBuild(player, Abilities.IceSpike,
				location))
			return false;
		for (LivingEntity en : EntityTools.getLivingEntitiesAroundPoint(location, 1.4)) {
			if (en != player && !damaged.contains(((LivingEntity) en))) {
				LivingEntity le = (LivingEntity) en;
				affect(le);
			}
		}
		affectedblock.setType(Material.ICE);
		loadAffectedBlocks();

		if (location.distance(origin) >= height) {
			return false;
		}

		return true;
	}

	private void affect(LivingEntity entity) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		entity.setVelocity(thrown);
		entity.damage(bPlayer.getCriticalHit(BendingType.Water,damage));
		damaged.add(entity);
		long slowCooldown = IceSpike2.slowCooldown;
		int mod = 2;
		if (((entity instanceof Player) ||(entity instanceof Monster)) && (entity.getEntityId() != player.getEntityId())){
			if (bPlayer != null) {
				bPlayer.earnXP(BendingType.Water, this);
			}
		}
		if (entity instanceof Player) {
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW,
						70, mod);
				new TempPotionEffect(entity, effect);
				bPlayer.slow(slowCooldown);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70,
					mod);
			new TempPotionEffect(entity, effect);
		}

	}

	public static boolean blockIsBase(Block block) {
		if (baseblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static void removeBlockBase(Block block) {
		if (baseblocks.containsKey(block)) {
			baseblocks.remove(block);
		}

	}

	public static void removeAll() {
		instances.clear();
	}

	public boolean revertblocks() {
		Vector direction = new Vector(0, -1, 0);
		location.getBlock().setType(Material.AIR);// .clone().add(direction).getBlock().setType(Material.AIR);
		location.add(direction);
		if (blockIsBase(location.getBlock()))
			return false;
		return true;
	}

	public static String getDescription() {
		return "This ability has many functions. Clicking while targetting ice, or an entity over some ice, "
				+ "will raise a spike of ice up, damaging and slowing the target. Tapping sneak (shift) while"
				+ " selecting a water source will select that source that can then be fired with a click. Firing"
				+ " this will launch a spike of ice at your target, dealing a bit of damage and slowing the target. "
				+ "If you sneak (shift) while not selecting a source, many ice spikes will erupt from around you, "
				+ "damaging and slowing those targets.";
	}

	@Override
	public int getBaseExperience() {
		return 4;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

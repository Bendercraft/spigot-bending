package net.avatar.realms.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

public class IceSpikeColumn {
	private static Map<Integer, IceSpikeColumn> instances = new HashMap<Integer, IceSpikeColumn>();
	private Map<Player, Long> removeTimers = new HashMap<Player, Long>();
	private static Map<Player, Long> cooldowns = new HashMap<Player, Long>();
	private static long removeTimer = 500;
	private static Map<Block, Integer> baseblocks = new HashMap<Block, Integer>();

	private static int ID = Integer.MIN_VALUE;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 2000;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 4;

	@ConfigurationParameter("Throw-Mult")
	private static double THROW_MULT = 0.7;

	private static double speed = 25;
	private static final Vector direction = new Vector(0, 1, 0);

	private static long interval = (long) (1000. / speed);

	private Location origin;
	private Location location;
	private Block block;
	private int progress = 0;
	private int damage = DAMAGE;
	int id;
	private long time;
	int height = 2;
	private Vector thrown = new Vector(0, THROW_MULT, 0);
	private Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private List<LivingEntity> damaged = new ArrayList<LivingEntity>();
	private Player player;

	public IceSpikeColumn(Player player, IBendingAbility parent) {
		this.player = player;
		if (cooldowns.containsKey(player)) {
			if (cooldowns.get(player) + COOLDOWN >= System.currentTimeMillis()) {
				return;
			}
		}
		try {
			double lowestdistance = RANGE + 1;
			Entity closestentity = null;
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(player.getLocation(), RANGE)) {
				if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (Tools.getDistanceFromLine(player.getLocation().getDirection(), player.getLocation(), entity.getLocation()) <= 2 && (entity.getEntityId() != player.getEntityId())) {
					double distance = player.getLocation().distance(entity.getLocation());
					if (distance < lowestdistance) {
						closestentity = entity;
						lowestdistance = distance;
					}
				}
			}
			if (closestentity != null) {
				Block temptestingblock = closestentity.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				// if (temptestingblock.getType() == Material.ICE){
				this.block = temptestingblock;
				// }
			} else {
				this.block = EntityTools.getTargetBlock(player, RANGE);
			}
			this.origin = this.block.getLocation();
			this.location = this.origin.clone();

		} catch (IllegalStateException e) {
			return;
		}

		loadAffectedBlocks();

		if (this.height != 0) {
			if (canInstantiate()) {
				this.id = ID;
				instances.put(this.id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				this.time = System.currentTimeMillis() - interval;
				cooldowns.put(player, System.currentTimeMillis());
			}
		}
	}

	public IceSpikeColumn(Player player, Location origin, int damage, Vector throwing, long aoecooldown, SpikeField spikeField) {
		this.player = player;
		this.origin = origin;
		this.location = origin.clone();
		this.block = this.location.getBlock();
		this.damage = damage;
		this.thrown = throwing;

		loadAffectedBlocks();

		if (this.block.getType() == Material.ICE) {
			if (canInstantiate()) {
				this.id = ID;
				instances.put(this.id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				this.time = System.currentTimeMillis() - interval;
			}
		}
	}

	private void loadAffectedBlocks() {
		this.affectedblocks.clear();
		Block thisblock;
		for (int i = 1; i <= this.height; i++) {
			thisblock = this.block.getWorld().getBlockAt(this.location.clone().add(direction.clone().multiply(i)));
			this.affectedblocks.put(thisblock, thisblock);
		}
	}

	private boolean blockInAffectedBlocks(Block block) {
		return this.affectedblocks.containsKey(block);
	}

	private static boolean blockInAllAffectedBlocks(Block block) {
		for (IceSpikeColumn spike : instances.values()) {
			if (spike.blockInAffectedBlocks(block)) {
				return true;
			}
		}
		return false;
	}

	private boolean canInstantiate() {
		if (this.block.getType() != Material.ICE) {
			return false;
		}
		for (Block block : this.affectedblocks.keySet()) {
			if (blockInAllAffectedBlocks(block) || block.getType() != Material.AIR || (block.getX() == this.player.getEyeLocation().getBlock().getX() && block.getZ() == this.player.getEyeLocation().getBlock().getZ())) {
				return false;
			}
		}
		return true;
	}

	public boolean progress() {
		if (System.currentTimeMillis() - this.time >= interval) {
			this.time = System.currentTimeMillis();
			if (this.progress < this.height) {
				moveEarth();
				this.removeTimers.put(this.player, System.currentTimeMillis());
			} else {
				if (this.removeTimers.get(this.player) + removeTimer <= System.currentTimeMillis()) {
					baseblocks.put(this.location.clone().add(direction.clone().multiply(-1 * (this.height))).getBlock(), (this.height - 1));
					if (!revertblocks()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean moveEarth() {
		this.progress++;
		Block affectedblock = this.location.clone().add(direction).getBlock();
		this.location = this.location.add(direction);
		if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.IceSpike, this.location)) {
			return false;
		}
		for (LivingEntity en : EntityTools.getLivingEntitiesAroundPoint(this.location, 1.4)) {
			if (en != this.player && !this.damaged.contains((en))) {
				LivingEntity le = en;
				affect(le);
			}
		}
		affectedblock.setType(Material.ICE);
		loadAffectedBlocks();

		if (this.location.distance(this.origin) >= this.height) {
			return false;
		}

		return true;
	}

	private void affect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(this.player);
		entity.setVelocity(this.thrown);
		entity.damage(this.damage);
		this.damaged.add(entity);
		long slowCooldown = IceSpike.slowCooldown;
		int mod = 2;
		if (entity instanceof Player) {
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
				entity.addPotionEffect(effect);
				bPlayer.slow(slowCooldown);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
			entity.addPotionEffect(effect);
		}

	}

	private static boolean blockIsBase(Block block) {
		if (baseblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public boolean revertblocks() {
		Vector direction = new Vector(0, -1, 0);
		this.location.getBlock().setType(Material.AIR);// .clone().add(direction).getBlock().setType(Material.AIR);
		this.location.add(direction);
		if (blockIsBase(this.location.getBlock())) {
			return false;
		}
		return true;
	}

}

package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Wave implements IAbility {
	private static Map<Integer, Wave> instances = new HashMap<Integer, Wave>();

	private static final long interval = 30;


	private static final byte full = 0x0;
	private static final double defaultmaxradius = ConfigManager.waveRadius;
	private static final double defaultfactor = ConfigManager.waveHorizontalPush;
	private static final double upfactor = ConfigManager.waveVerticalPush;
	private static final double maxfreezeradius = 7;

	static double defaultrange = 20;


	Player player;
	private Location location = null;
	private Block sourceblock = null;
	boolean progressing = false;
	private Location targetdestination = null;
	private Vector targetdirection = null;
	private Map<Block, Block> wave = new HashMap<Block, Block>();
	private Map<Block, Block> frozenblocks = new HashMap<Block, Block>();
	private double radius = 1;
	private long time;
	private double maxradius = defaultmaxradius;
	private boolean freeze = false;
	private boolean activatefreeze = false;
	private Location frozenlocation;
	double range = defaultrange;
	private double factor = defaultfactor;
	boolean canhitself = true;
	private IAbility parent;

	private TempBlock drainedBlock;

	public Wave(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;

		if (instances.containsKey(player.getEntityId())) {
			if (instances.get(player.getEntityId()).progressing
					&& !instances.get(player.getEntityId()).freeze) {
				instances.get(player.getEntityId()).freeze = true;
				return;
			}
		}

		if (AvatarState.isAvatarState(player)) {
			maxradius = AvatarState.getValue(maxradius);
		}
		maxradius = PluginTools.waterbendingNightAugment(maxradius, player.getWorld());
		if (prepare()) {
			if (instances.containsKey(player.getEntityId())) {
				instances.get(player.getEntityId()).remove();
			}
			instances.put(player.getEntityId(), this);
			time = System.currentTimeMillis();
		}

	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockTools.getWaterSourceBlock(player, range,
				EntityTools.canPlantbend(player));
		if (block != null) {
			sourceblock = block;
			focusBlock();
			return true;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if(bPlayer == null) {
			return false;
		}
		//If no block available, check if bender can drainbend !
		if(Drainbending.canDrainBend(player) && !bPlayer.isOnCooldown(Abilities.Drainbending)) {
			Location location = player.getEyeLocation();
			Vector vector = location.getDirection().clone().normalize();
			block = location.clone().add(vector.clone().multiply(2)).getBlock();
			if(block.getType().equals(Material.AIR)) {
				drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0x0);
				sourceblock = block;
				focusBlock();
				//Range and max radius is halfed for Drainbending
				range = range/2;
				maxradius = maxradius/2;
				bPlayer.cooldown(Abilities.Drainbending);
				return true;
			}
		}
		return false;
	}

	private void cancelPrevious() {
		if (instances.containsKey(player.getEntityId())) {
			Wave old = instances.get(player.getEntityId());
			if (old.progressing) {
				old.breakBlock();
				old.thaw();
				old.returnWater();
			} else {
				old.remove();
			}
		}
	}

	public void remove() {
		finalRemoveWater(sourceblock);
		if(drainedBlock != null) {
			drainedBlock.revertBlock();
		}
		instances.remove(player.getEntityId());
	}

	private void focusBlock() {
		location = sourceblock.getLocation();
	}

	public void moveWater() {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Surge))
			return;

		bPlayer.cooldown(Abilities.Surge);
		if (sourceblock != null) {
			if (sourceblock.getWorld() != player.getWorld()) {
				return;
			}
			range = PluginTools.waterbendingNightAugment(range, player.getWorld());
			if (AvatarState.isAvatarState(player))
				factor = AvatarState.getValue(factor);
			Entity target = EntityTools.getTargettedEntity(player, range);
			if (target == null) {
				targetdestination = EntityTools.getTargetBlock(player, range, BlockTools.getTransparentEarthbending()).getLocation();
			} else {
				targetdestination = ((LivingEntity) target).getEyeLocation();
			}
			if (targetdestination.distance(location) <= 1) {
				progressing = false;
				targetdestination = null;
			} else {
				progressing = true;
				targetdirection = getDirection(sourceblock.getLocation(),
						targetdestination).normalize();
				targetdestination = location.clone().add(
						targetdirection.clone().multiply(range));
				if (BlockTools.isPlant(sourceblock))
					new Plantbending(sourceblock, this);
				if (!BlockTools.adjacentToThreeOrMoreSources(sourceblock)) {
					sourceblock.setType(Material.AIR);
				}
				addWater(sourceblock);

			}

		}
	}

	private Vector getDirection(Location location, Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;

		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();

		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();

		return new Vector(x1 - x0, y1 - y0, z1 - z0);

	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()
				|| !EntityTools.canBend(player, Abilities.Surge)) {
			breakBlock();
			thaw();
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (!progressing
					&& EntityTools.getBendingAbility(player) != Abilities.Surge) {
				return false;
			}

			if (!progressing) {
				sourceblock.getWorld().playEffect(location, Effect.SMOKE, 4,
						(int) range);
				return true;
			}

			if (location.getWorld() != player.getWorld()) {
				thaw();
				breakBlock();
				return false;
			}

			if (activatefreeze) {
				if (location.distance(player.getLocation()) > range) {
					progressing = false;
					thaw();
					breakBlock();
					return false;
				}
				if (!EntityTools.hasAbility(player, Abilities.PhaseChange)
						&& EntityTools.getBendingAbility(player) != Abilities.Surge) {
					progressing = false;
					thaw();
					breakBlock();
					returnWater();
					return false;
				}
				if (!EntityTools.canBend(player, Abilities.Surge)) {
					progressing = false;
					thaw();
					breakBlock();
					returnWater();
					return false;
				}

			} else {

				Vector direction = targetdirection;

				location = location.clone().add(direction);
				Block blockl = location.getBlock();

				List<Block> blocks = new LinkedList<Block>();

				if (!Tools.isRegionProtectedFromBuild(player, Abilities.Surge,
						location)
						&& (((blockl.getType() == Material.AIR
								|| blockl.getType() == Material.FIRE
								|| BlockTools.isPlant(blockl)
								|| BlockTools.isWater(blockl) 
								|| BlockTools.isWaterbendable(blockl, player))) && blockl
								.getType() != Material.LEAVES)) {

					for (double i = 0; i <= radius; i += .5) {
						for (double angle = 0; angle < 360; angle += 10) {
							Vector vec = Tools.getOrthogonalVector(
									targetdirection, angle, i);
							Block block = location.clone().add(vec).getBlock();
							if (!blocks.contains(block)
									&& (block.getType() == Material.AIR || block
											.getType() == Material.FIRE)
									|| BlockTools.isWaterbendable(block, player)) {
								blocks.add(block);
								FireBlast.removeFireBlastsAroundPoint(
										block.getLocation(), 2);
							}
						}
					}
				}
				
				List<Block> toRemove = new LinkedList<Block>(wave.keySet());
				for (Block block : toRemove) {
					if (!blocks.contains(block))
						finalRemoveWater(block);
				}

				for (Block block : blocks) {
					if (!wave.containsKey(block))
						addWater(block);
				}

				if (wave.isEmpty()) {
					// blockl.setType(Material.GLOWSTONE);
					breakBlock();
					progressing = false;
					return false;
				}

				for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
						2 * radius)) {

					boolean knockback = false;
	
					List<Block> temp = new LinkedList<Block>(wave.keySet());
					for (Block block : temp) {
						if (entity.getLocation().distance(block.getLocation()) <= 2) {
							if (entity instanceof LivingEntity
									&& freeze
									&& entity.getEntityId() != player
											.getEntityId()) {
								activatefreeze = true;
								frozenlocation = entity.getLocation();
								freeze();
								break;
							}
							if (entity.getEntityId() != player.getEntityId()
									|| canhitself) {
								knockback = true;
							}		
						}
					}
					if (knockback) {
						Vector dir = direction.clone();
						dir.setY(dir.getY() * upfactor);
						entity.setVelocity(entity
								.getVelocity()
								.clone()
								.add(dir.clone().multiply(
										PluginTools.waterbendingNightAugment(factor,
												player.getWorld()))));
						entity.setFallDistance(0);
						if (entity.getFireTicks() > 0)
							entity.getWorld().playEffect(entity.getLocation(),
									Effect.EXTINGUISH, 0);
						entity.setFireTicks(0);
					}
				}

				if (!progressing) {
					breakBlock();
					return false;
				}

				if (location.distance(targetdestination) < 1) {
					progressing = false;
					breakBlock();
					returnWater();
					return false;
				}

				if (radius < maxradius) {
					radius += .5;
				}				
				return true;
			}
		}

		return true;

	}

	private void breakBlock() {
		List<Block> temp = new LinkedList<Block>(wave.keySet());
		for (Block block : temp) {
			finalRemoveWater(block);
		}
	}

	private void finalRemoveWater(Block block) {
		if (wave.containsKey(block)) {
			TempBlock.revertBlock(block, Material.AIR);
			wave.remove(block);
		}
	}

	private void addWater(Block block) {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.Surge,
				block.getLocation()))
			return;
		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.WATER, full);
			// new TempBlock(block, Material.ICE, (byte) 0);
			wave.put(block, block);
		}
	}

	private void clearWave() {
		for (Block block : wave.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
		}
		wave.clear();
	}

	public static void moveWater(Player player) {
		if (instances.containsKey(player.getEntityId())) {
			instances.get(player.getEntityId()).moveWater();
		}
	}

	public static void progressAll() {
		List<Wave> toRemove = new LinkedList<Wave>();
		
		for(Wave wave : instances.values()) {
			boolean keep = wave.progress();
			if(!keep) {
				toRemove.add(wave);
			}
		}
		for(Wave wave : toRemove) {
			wave.remove();
		}
	}

	public static boolean isBlockWave(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).wave.containsKey(block))
				return true;
		}
		return false;
	}

	public static void launch(Player player) {
		moveWater(player);
	}

	public static void removeAll() {
		for (Wave wave : instances.values()) {
			List<Block> waveBlock = new LinkedList<Block>(wave.wave.keySet());
			for (Block block : waveBlock) {
				block.setType(Material.AIR);
				wave.wave.remove(block);
			}
			List<Block> frozenBlock = new LinkedList<Block>(wave.frozenblocks.keySet());
			for (Block block : frozenBlock) {
				block.setType(Material.AIR);
				wave.frozenblocks.remove(block);
			}
		}
	}

	private void freeze() {
		clearWave();

		double freezeradius = radius;
		if (freezeradius > maxfreezeradius) {
			freezeradius = maxfreezeradius;
		}

		for (Block block : BlockTools.getBlocksAroundPoint(frozenlocation,
				freezeradius)) {
			if (Tools.isRegionProtectedFromBuild(player, Abilities.Surge,
					block.getLocation())
					|| Tools.isRegionProtectedFromBuild(player,
							Abilities.PhaseChange, block.getLocation()))
				continue;
			if (TempBlock.isTempBlock(block))
				continue;
			if (block.getType() == Material.AIR
					|| block.getType() == Material.SNOW) {
				// block.setType(Material.ICE);
				new TempBlock(block, Material.ICE, (byte) 0);
				frozenblocks.put(block, block);
			}
			if (BlockTools.isWater(block)) {
				new FreezeMelt(player, this, block);
			}
			if (BlockTools.isPlant(block) && block.getType() != Material.LEAVES) {
				block.breakNaturally();
				// block.setType(Material.ICE);
				new TempBlock(block, Material.ICE, (byte) 0);
				frozenblocks.put(block, block);
			}
		}
	}

	private void thaw() {
		for (Block block : frozenblocks.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
		}
		frozenblocks.clear();
	}

	public static void thaw(Block block) {
		for (Wave wave : instances.values()) {
			if (wave.frozenblocks.containsKey(block)) {
				TempBlock.revertBlock(block, Material.AIR);
				wave.frozenblocks.remove(block);
			}
		}
	}

	public static boolean canThaw(Block block) {
		for (int id : instances.keySet()) {
			if (instances.get(id).frozenblocks.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	private void returnWater() {
		if (location != null) {
			new WaterReturn(player, location.getBlock(), this);
		}
	}
	
	public static boolean isWaving(Player player) {
		return instances.containsKey(player.getEntityId());
	}
	
	public static Wave getWave(Player player) {
		return instances.get(player.getEntityId());
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

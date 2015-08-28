package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Surge", element=BendingType.Water)
public class Wave implements IAbility {
	private static Map<Integer, Wave> instances = new HashMap<Integer, Wave>();

	private static final long interval = 30;


	private static final byte full = 0x0;

	@ConfigurationParameter("Radius")
	private static final double RADIUS = 3;

	@ConfigurationParameter("Horizontal-Factor")
	private static final double HOR_FACTOR = 1;

	@ConfigurationParameter("Vertical-Factor")
	private static final double VERT_FACTOR = 0.2;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

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
	private double maxradius = RADIUS;
	private boolean freeze = false;
	private boolean activatefreeze = false;
	private Location frozenlocation;
	double range = defaultrange;
	private double factor = HOR_FACTOR;
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
			this.maxradius = AvatarState.getValue(this.maxradius);
		}
		this.maxradius = PluginTools.waterbendingNightAugment(this.maxradius, player.getWorld());
		if (prepare()) {
			if (instances.containsKey(player.getEntityId())) {
				instances.get(player.getEntityId()).remove();
			}
			instances.put(player.getEntityId(), this);
			this.time = System.currentTimeMillis();
		}

	}

	public boolean prepare() {
		cancelPrevious();
		Block block = BlockTools.getWaterSourceBlock(this.player, this.range,
				EntityTools.canPlantbend(this.player));
		if (block != null) {
			this.sourceblock = block;
			focusBlock();
			return true;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(this.player);
		if(bPlayer == null) {
			return false;
		}
		//If no block available, check if bender can drainbend !
		if(Drainbending.canDrainBend(this.player) && !bPlayer.isOnCooldown(Abilities.Drainbending)) {
			Location location = this.player.getEyeLocation();
			Vector vector = location.getDirection().clone().normalize();
			block = location.clone().add(vector.clone().multiply(2)).getBlock();
			if(Drainbending.canBeSource(block)) {
				this.drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0x0);
				this.sourceblock = block;
				focusBlock();
				//Range and max radius is halfed for Drainbending
				this.range = this.range/2;
				this.maxradius = this.maxradius/2;
				bPlayer.cooldown(Abilities.Drainbending, Drainbending.COOLDOWN);
				return true;
			}
		}
		return false;
	}

	private void cancelPrevious() {
		if (instances.containsKey(this.player.getEntityId())) {
			Wave old = instances.get(this.player.getEntityId());
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
		finalRemoveWater(this.sourceblock);
		if(this.drainedBlock != null) {
			this.drainedBlock.revertBlock();
		}
		instances.remove(this.player.getEntityId());
	}

	private void focusBlock() {
		this.location = this.sourceblock.getLocation();
	}

	public void moveWater() {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(this.player);

		if (bPlayer.isOnCooldown(Abilities.Surge)) {
			return;
		}

		bPlayer.cooldown(Abilities.Surge, COOLDOWN);
		if (this.sourceblock == null) {
			return;
		}
		if (this.sourceblock.getWorld() != this.player.getWorld()) {
			return;
		}

		this.range = PluginTools.waterbendingNightAugment(this.range, this.player.getWorld());
		if (AvatarState.isAvatarState(this.player)){
			this.factor = AvatarState.getValue(this.factor);
		}			
		Entity target = EntityTools.getTargettedEntity(this.player, this.range);
		if ((target == null) || ProtectionManager.isEntityProtectedByCitizens(target)) {
			this.targetdestination = EntityTools.getTargetBlock(this.player, this.range, BlockTools.getTransparentEarthbending()).getLocation();
		} else {
			this.targetdestination = ((LivingEntity) target).getEyeLocation();
		}
		if (this.targetdestination.distance(this.location) <= 1) {
			this.progressing = false;
			this.targetdestination = null;
		} else {
			this.progressing = true;
			this.targetdirection = getDirection(this.sourceblock.getLocation(),
					this.targetdestination).normalize();
			this.targetdestination = this.location.clone().add(
					this.targetdirection.clone().multiply(this.range));
			if (BlockTools.isPlant(this.sourceblock)) {
				new Plantbending(this.sourceblock, this);
			}
			if (!BlockTools.adjacentToThreeOrMoreSources(this.sourceblock)) {
				this.sourceblock.setType(Material.AIR);
			}
			addWater(this.sourceblock);
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
		if (this.player.isDead() || !this.player.isOnline()
				|| !EntityTools.canBend(this.player, Abilities.Surge)) {
			breakBlock();
			thaw();
			return false;
		}
		if ((System.currentTimeMillis() - this.time) >= interval) {
			this.time = System.currentTimeMillis();

			if (!this.progressing
					&& (EntityTools.getBendingAbility(this.player) != Abilities.Surge)) {
				return false;
			}

			if (!this.progressing) {
				this.sourceblock.getWorld().playEffect(this.location, Effect.SMOKE, 4,
						(int) this.range);
				return true;
			}

			if (this.location.getWorld() != this.player.getWorld()) {
				thaw();
				breakBlock();
				return false;
			}

			if (this.activatefreeze) {
				if (this.location.distance(this.player.getLocation()) > this.range) {
					this.progressing = false;
					thaw();
					breakBlock();
					return false;
				}
				if (!EntityTools.canBend(this.player, Abilities.PhaseChange)
						&& (EntityTools.getBendingAbility(this.player) != Abilities.Surge)) {
					this.progressing = false;
					thaw();
					breakBlock();
					returnWater();
					return false;
				}
				if (!EntityTools.canBend(this.player, Abilities.Surge)) {
					this.progressing = false;
					thaw();
					breakBlock();
					returnWater();
					return false;
				}

			} else {

				Vector direction = this.targetdirection;

				this.location = this.location.clone().add(direction);
				Block blockl = this.location.getBlock();

				List<Block> blocks = new LinkedList<Block>();

				if (!ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.Surge,
						this.location)
						&& ((((blockl.getType() == Material.AIR)
								|| (blockl.getType() == Material.FIRE)
								|| BlockTools.isPlant(blockl)
								|| BlockTools.isWater(blockl) 
								|| BlockTools.isWaterbendable(blockl, this.player))) && (blockl
										.getType() != Material.LEAVES))) {

					for (double i = 0; i <= this.radius; i += .5) {
						for (double angle = 0; angle < 360; angle += 10) {
							Vector vec = Tools.getOrthogonalVector(
									this.targetdirection, angle, i);
							Block block = this.location.clone().add(vec).getBlock();
							if ((!blocks.contains(block)
									&& ((block.getType() == Material.AIR) || (block
											.getType() == Material.FIRE)))
									|| BlockTools.isWaterbendable(block, this.player)) {
								blocks.add(block);
								FireBlast.removeFireBlastsAroundPoint(
										block.getLocation(), 2);
							}
						}
					}
				}

				List<Block> toRemove = new LinkedList<Block>(this.wave.keySet());
				for (Block block : toRemove) {
					if (!blocks.contains(block)) {
						finalRemoveWater(block);
					}
				}

				for (Block block : blocks) {
					if (!this.wave.containsKey(block)) {
						addWater(block);
					}
				}

				if (this.wave.isEmpty()) {
					// blockl.setType(Material.GLOWSTONE);
					breakBlock();
					this.progressing = false;
					return false;
				}

				for (Entity entity : EntityTools.getEntitiesAroundPoint(this.location,
						2 * this.radius)) {
					if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
						continue;
					}
					boolean knockback = false;

					List<Block> temp = new LinkedList<Block>(this.wave.keySet());
					for (Block block : temp) {
						if (entity.getLocation().distance(block.getLocation()) <= 2) {
							if ((entity instanceof LivingEntity)
									&& this.freeze
									&& (entity.getEntityId() != this.player
									.getEntityId())) {
								this.activatefreeze = true;
								this.frozenlocation = entity.getLocation();
								freeze();
								break;
							}
							if ((entity.getEntityId() != this.player.getEntityId())
									|| this.canhitself) {
								knockback = true;
							}		
						}
					}
					if (knockback) {
						Vector dir = direction.clone();
						dir.setY(dir.getY() * VERT_FACTOR);
						if (entity.getEntityId() == this.player.getEntityId()) {
							dir.multiply(2.0/3.0);
						}
						entity.setVelocity(entity
								.getVelocity()
								.clone()
								.add(dir.clone().multiply(
										PluginTools.waterbendingNightAugment(this.factor,
												this.player.getWorld()))));
						entity.setFallDistance(0);
						if (entity.getFireTicks() > 0) {
							entity.getWorld().playEffect(entity.getLocation(),
									Effect.EXTINGUISH, 0);
						}
						entity.setFireTicks(0);
					}
				}

				if (!this.progressing) {
					breakBlock();
					return false;
				}

				if (this.location.distance(this.targetdestination) < 1) {
					this.progressing = false;
					breakBlock();
					returnWater();
					return false;
				}

				if (this.radius < this.maxradius) {
					this.radius += .5;
				}				
				return true;
			}
		}

		return true;

	}

	private void breakBlock() {
		List<Block> temp = new LinkedList<Block>(this.wave.keySet());
		for (Block block : temp) {
			finalRemoveWater(block);
		}
	}

	private void finalRemoveWater(Block block) {
		if (this.wave.containsKey(block)) {
			TempBlock.revertBlock(block);
			this.wave.remove(block);
		}
	}

	private void addWater(Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.Surge,
				block.getLocation())) {
			return;
		}
		if (!TempBlock.isTempBlock(block)) {
			new TempBlock(block, Material.WATER, full);
			// new TempBlock(block, Material.ICE, (byte) 0);
			this.wave.put(block, block);
		}
	}

	private void clearWave() {
		for (Block block : this.wave.keySet()) {
			TempBlock.revertBlock(block);
		}
		this.wave.clear();
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
			if (instances.get(ID).wave.containsKey(block)) {
				return true;
			}
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

		double freezeradius = this.radius;
		if (freezeradius > maxfreezeradius) {
			freezeradius = maxfreezeradius;
		}

		for (Block block : BlockTools.getBlocksAroundPoint(this.frozenlocation,
				freezeradius)) {
			if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.Surge,
					block.getLocation())
					|| ProtectionManager.isRegionProtectedFromBending(this.player,
							Abilities.PhaseChange, block.getLocation())){
				continue;
			}			
			if (TempBlock.isTempBlock(block)){
				continue;
			}		
			if ((block.getType() == Material.AIR)
					|| (block.getType() == Material.SNOW)) {
				// block.setType(Material.ICE);
				new TempBlock(block, Material.ICE, (byte) 0);
				this.frozenblocks.put(block, block);
			}
			if (BlockTools.isWater(block)) {
				new FreezeMelt(this.player, this, block);
			}
			if (BlockTools.isPlant(block) && (block.getType() != Material.LEAVES)) {
				block.breakNaturally();
				// block.setType(Material.ICE);
				new TempBlock(block, Material.ICE, (byte) 0);
				this.frozenblocks.put(block, block);
			}
		}
	}

	private void thaw() {
		for (Block block : this.frozenblocks.keySet()) {
			TempBlock.revertBlock(block);
		}
		this.frozenblocks.clear();
	}

	public static void thaw(Block block) {
		for (Wave wave : instances.values()) {
			if (wave.frozenblocks.containsKey(block)) {
				TempBlock.revertBlock(block);
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
		if (this.location != null) {
			new WaterReturn(this.player, this.location.getBlock(), this);
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
		return this.parent;
	}

}

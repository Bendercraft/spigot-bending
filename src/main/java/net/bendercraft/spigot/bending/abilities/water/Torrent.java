package net.bendercraft.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.abilities.water.WaterBalance.Damage;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

@ABendingAbility(name = Torrent.NAME, element = BendingElement.WATER)
public class Torrent extends BendingActiveAbility {
	public final static String NAME = "Torrent";
	
	static long interval = 30;
	private static int defaultrange = 20;
	private static int selectrange = 10;
	private static double radius = 3;
	private static double RANGE = 25;

	@ConfigurationParameter("Damage")
	private static double DAMAGE = 6;

	@ConfigurationParameter("Throw-Factor")
	private static double DEFLECT_DAMAGE = 2;
	
	@ConfigurationParameter("Iced")
	private static long ICED = 20000;

	private static double factor = 1;
	private static int maxlayer = 3;
	private static double ylimit = 0.2;

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

	private TempBlock drainedBlock;

	public Torrent(RegisteredAbility register, Player player) {
		super(register, player);

		this.damage = DAMAGE;
		this.range = RANGE;
		if (this.bender.hasPath(BendingPath.MARKSMAN)) {
			this.range *= 1.3;
			this.damage *= 0.8;
		}

		if (this.bender.hasPath(BendingPath.FLOWLESS)) {
			this.damage *= 1.2;
		}
	}

	private void freeze() {
		if (layer == 0) {
			return;
		}
		if (!EntityTools.canBend(this.player, AbilityManager.getManager().getRegisteredAbility(PhaseChange.NAME))) {
			return;
		}
		List<Block> ice = BlockTools.getBlocksAroundPoint(location, layer);
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, layer);
		for (Block block : ice) {
			if (BlockTools.isTransparentToEarthbending(player, block) && (block.getType() != Material.ICE)) {
				boolean safe = true;
				for(LivingEntity entity : entities) {
					if(entity.getLocation().getBlock() == block
							|| entity.getLocation().getBlock().getRelative(BlockFace.UP) == block) {
						safe = false;
						break;
					}
				}
				if(safe) {
					Bending.getInstance().getManager().addGlobalTempBlock(ICED, TempBlock.makeTemporary(block, Material.ICE, true));
				}
			}
		}
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			time = System.currentTimeMillis();
			sourceblock = BlockTools.getWaterSourceBlock(player, selectrange, EntityTools.canPlantbend(player));

			if (sourceblock == null && WaterReturn.hasWaterBottle(player)) {
				Location eyeloc = player.getEyeLocation();
				Block block = eyeloc.add(eyeloc.getDirection().normalize()).getBlock();
				if (BlockTools.isTransparentToEarthbending(player, block) 
						&& BlockTools.isTransparentToEarthbending(player, eyeloc.getBlock())
						&& WaterReturn.canBeSource(block)) {
					drainedBlock = TempBlock.makeTemporary(block, Material.STATIONARY_WATER, false);
					sourceblock = block;
					WaterReturn.emptyWaterBottle(player);
				}
			}

			if (sourceblock != null) {
				sourceselected = true;
				setState(BendingAbilityState.PREPARING);
			}

			return false;
		}
		launch = true;
		if (launching && !freeze) {
			freeze = true;
		}

		return false;
	}

	@Override
	public void progress() {
		if (!NAME.equals(EntityTools.getBendingAbility(player))) {
			if (this.location != null) {
				returnWater(this.location);
			}
			remove();
			return;
		}

		if (this.waterReturn != null) {
			if(!this.waterReturn.progress()) {
				remove();
				return;
			}
		}

		if (this.burst != null) {
			if (!this.burst.progress()) {
				returnWater(this.location);
				remove();
			}
			return;
		}

		if (System.currentTimeMillis() > (this.time + interval)) {
			this.time = System.currentTimeMillis();
			
			if (this.sourceselected) {
				if (!this.sourceblock.getWorld().equals(this.player.getWorld()) 
						|| this.sourceblock.getLocation().distance(this.player.getLocation()) > selectrange) {
					remove();
					return;
				}

				if (this.player.isSneaking()) {
					this.sourceselected = false;
					this.settingup = true;
					if (BlockTools.isPlant(this.sourceblock)) {
						this.sourceblock.setType(Material.AIR);
					} else if (!BlockTools.adjacentToThreeOrMoreSources(this.sourceblock)) {
						this.sourceblock.setType(Material.AIR);
					}
					//this.source = new TempBlock(this.sourceblock, Material.WATER, full);
					this.source = TempBlock.makeTemporary(sourceblock, Material.WATER, false);
					this.location = this.sourceblock.getLocation();
				} else {
					sourceblock.getWorld().playEffect(sourceblock.getLocation(), Effect.SMOKE, 4, 20);
					return;
				}
			}

			if (this.settingup) {
				if (!this.player.isSneaking()) {
					returnWater(this.source.getLocation());
					return;
				}
				Location eyeloc = this.player.getEyeLocation();
				double startangle = this.player.getEyeLocation().getDirection().angle(new Vector(1, 0, 0));
				double dx = radius * Math.cos(startangle);
				double dy = 0;
				double dz = radius * Math.sin(startangle);
				Location setup = eyeloc.clone().add(dx, dy, dz);

				if (!this.location.getWorld().equals(this.player.getWorld()) 
						|| this.location.distance(setup) > defaultrange) {
					remove();
					return;
				}

				if (this.location.getBlockY() > setup.getBlockY()) {
					Vector direction = new Vector(0, -1, 0);
					this.location = this.location.clone().add(direction);
				} else if (this.location.getBlockY() < setup.getBlockY()) {
					Vector direction = new Vector(0, 1, 0);
					this.location = this.location.clone().add(direction);
				} else {
					Vector direction = Tools.getDirection(this.location, setup).normalize();
					this.location = this.location.clone().add(direction);
				}

				if (this.location.distance(setup) <= 1) {
					this.settingup = false;
					this.source.revertBlock();
					this.source = null;
					this.forming = true;
				} else {
					if (!this.location.getBlock().equals(this.source.getLocation().getBlock())) {
						this.source.revertBlock();
						this.source = null;
						Block block = this.location.getBlock();
						if (!BlockTools.isTransparentToEarthbending(this.player, block) || block.isLiquid()) {
							remove();
							return;
						}
						//this.source = new TempBlock(this.location.getBlock(), Material.WATER, full);
						this.source = TempBlock.makeTemporary(this.location.getBlock(), Material.WATER, false);
					}
				}
			}

			if (this.forming && !this.player.isSneaking()) {
				returnWater(this.player.getEyeLocation().add(radius, 0, 0));
				return;
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
					remove();
					return;
				}
			}

			if (this.formed && !this.player.isSneaking() && !this.launch) {
				this.burst = new TorrentBurst(this.player, radius, this);
				return;
			}

			if (this.launch && this.formed) {
				this.launching = true;
				this.launch = false;
				this.formed = false;
				if (!launch()) {
					remove();
					return;
				}
			}

			if (this.launching) {
				if (!this.player.isSneaking() 
						|| !launch()) {
					remove();
					return;
				}
			}
		}
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
				if (!doneblocks.contains(block) && !ProtectionManager.isLocationProtectedFromBending(this.player, register, blockloc)) {
					if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
						//this.launchblocks.add(new TempBlock(block, Material.WATER, full));
						this.launchblocks.add(TempBlock.makeTemporary(block, Material.WATER, false));
						doneblocks.add(block);
					} else if (!BlockTools.isTransparentToEarthbending(this.player, block)) {
						break;
					}
				}
			}
			if (this.launchblocks.isEmpty()) {
				return false;
			}
			return true;
		}

		Entity target = null;
		if (!this.bender.hasPath(BendingPath.FLOWLESS)) {
			target = EntityTools.getTargetedEntity(this.player, this.range, this.hurtentities);
		}
		Location targetloc = EntityTools.getTargetBlock(this.player, this.range, BlockTools.getTransparentEarthbending()).getLocation();

		if ((target != null) && !ProtectionManager.isEntityProtected(target)) {
			targetloc = target.getLocation();
		}

		ArrayList<TempBlock> newblocks = new ArrayList<TempBlock>();

		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(this.player.getLocation(), this.range + 5);
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

		if ((this.location.distance(this.player.getLocation()) > this.range) || ProtectionManager.isLocationProtectedFromBending(this.player, register, this.location)) {
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
				//newblocks.add(new TempBlock(b, Material.WATER, full));
				newblocks.add(TempBlock.makeTemporary(b, Material.WATER, false));
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
					if (ProtectionManager.isEntityProtected(entity)) {
						continue;
					}
					if (entity.getWorld() != block.getBlock().getWorld()) {
						continue;
					}
					if ((entity.getLocation().distance(block.getLocation()) <= 1.5) && !affectedentities.contains(entity)) {
						if (i == 0) {
							affect(entity, dir);
						} else {
							affect(entity, Tools.getDirection(block.getLocation(), this.launchblocks.get(i - 1).getLocation()).normalize());
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
		ArrayList<Block> doneBlocks = new ArrayList<Block>();
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(loc, radius + 2);
		List<Entity> affectedEntities = new ArrayList<Entity>();
		for (double theta = this.startangle; theta < (this.angle + this.startangle); theta += 20) {
			double phi = Math.toRadians(theta);
			double dx = Math.cos(phi) * radius;
			double dy = 0;
			double dz = Math.sin(phi) * radius;
			Location blockloc = loc.clone().add(dx, dy, dz);
			Block block = blockloc.getBlock();
			if (!doneBlocks.contains(block) && !ProtectionManager.isLocationProtectedFromBending(this.player, register, blockloc)) {
				if (BlockTools.isTransparentToEarthbending(this.player, block) && !block.isLiquid()) {
					//this.blocks.add(new TempBlock(block, Material.WATER, full));
					this.blocks.add(TempBlock.makeTemporary(block, Material.WATER, false));
					doneBlocks.add(block);
					for (LivingEntity entity : entities) {
						if (ProtectionManager.isEntityProtected(entity)) {
							continue;
						}
						if (entity.getWorld() != blockloc.getWorld()) {
							continue;
						}
						if (!affectedEntities.contains(entity) && (entity.getLocation().distance(blockloc) <= 1.5)) {
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
	public void stop() {
		clear();
		if(drainedBlock != null) {
			drainedBlock.revertBlock();
			drainedBlock = null;
		}
		if (waterReturn != null) {
			waterReturn.stop();
		}
		if (burst != null) {
			burst.remove();
		}
		if(launching && freeze) {
			bender.water.ice();
		} else if(launching || burst != null) {
			bender.water.liquid();
		}
	}

	private void returnWater(Location location) {
		this.clear();
		this.waterReturn = new WaterReturn(this.player, location.getBlock(), this);
	}

	private void deflect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
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
		BendingPlayer bender = BendingPlayer.getBendingPlayer(this.player);
		if (bender.hasPath(BendingPath.MARKSMAN)) {
			velocity = velocity.multiply(1.5);
		}
		entity.setVelocity(velocity);
		entity.setFallDistance(0);

		if (!bender.hasPath(BendingPath.MARKSMAN)) {
			DamageTools.damageEntity(bender, entity, this, bender.water.damage(Damage.LIQUID, DEFLECT_DAMAGE));
		}

	}

	private void affect(LivingEntity entity, Vector direction) {
		if (ProtectionManager.isEntityProtected(entity)) {
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
			DamageTools.damageEntity(bender, entity, this, bender.water.damage(Damage.LIQUID, damage));
			this.hurtentities.add(entity);
			entity.setNoDamageTicks(0);
		}
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (AbilityManager.getManager().getInstances(Torrent.NAME).containsKey(player)) {
			Torrent torrent = (Torrent) AbilityManager.getManager().getInstances(Torrent.NAME).get(player);
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
		return this.player;
	}

}

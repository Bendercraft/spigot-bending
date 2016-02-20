package net.avatar.realms.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.MathUtils;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@ABendingAbility(name = OctopusForm.NAME, element = BendingElement.Water)
public class OctopusForm extends BendingActiveAbility {
	public final static String NAME = "OctopusForm";
	
	private static int range = 10;
	static final double radius = 3;
	private static final byte full = 0x0;
	private static long interval = 50;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 5;

	private Block sourceblock;
	private Location sourcelocation;
	private TempBlock source;
	private long time;
	private double startangle;
	private double angle;
	private double y = 0;
	private int animstep = 1, step = 1, inc = 3;
	private double dta = 45;
	private List<TempBlock> blocks = new LinkedList<TempBlock>();
	private List<TempBlock> newblocks = new LinkedList<TempBlock>();
	private boolean sourceselected = false;
	private boolean settingup = false;
	private boolean forming = false;
	private boolean formed = false;
	private WaterReturn waterReturn;
	private TempBlock drainedBlock;

	public OctopusForm(RegisteredAbility register, Player player) {
		super(register, player);
	}

	private void incrementStep() {
		if (sourceselected) {
			sourceselected = false;
			settingup = true;
		} else if (settingup) {
			settingup = false;
			forming = true;
		} else if (forming) {
			forming = false;
			formed = true;
		}
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.Preparing) {
			return false;
		}

		incrementStep();

		if (BlockTools.isPlant(sourceblock)) {
			sourceblock.setType(Material.AIR);
		} else if (!BlockTools.adjacentToThreeOrMoreSources(sourceblock)) {
			sourceblock.setType(Material.AIR);
		}
		//source = new TempBlock(sourceblock, Material.WATER, full);
		source = TempBlock.makeTemporary(sourceblock, Material.WATER);

		setState(BendingAbilityState.Prepared);
		return false;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.Start) {
			sourceblock = BlockTools.getWaterSourceBlock(player, range, EntityTools.canPlantbend(player));
			if (sourceblock == null && WaterReturn.hasWaterBottle(player)) {
				Location eyeLoc = player.getEyeLocation();
				Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
				if (BlockTools.isTransparentToEarthbending(player, block) && BlockTools.isTransparentToEarthbending(player, eyeLoc.getBlock())) {
					//this.drainedBlock = new TempBlock(block, Material.STATIONARY_WATER, (byte) 0x0);
					this.drainedBlock = TempBlock.makeTemporary(block, Material.STATIONARY_WATER);
					sourceblock = block;
					WaterReturn.emptyWaterBottle(player);
				}
			}

			if (sourceblock == null) {
				return false;
			}

			time = System.currentTimeMillis();

			sourcelocation = sourceblock.getLocation();
			sourceselected = true;
			setState(BendingAbilityState.Preparing);
			return false;
		}

		if (!formed)
			return false;
		double tentacleangle = (new Vector(1, 0, 0)).angle(player.getEyeLocation().getDirection()) + dta / 2;

		for (double tangle = tentacleangle; tangle < tentacleangle + 360; tangle += dta) {
			double phi = Math.toRadians(tangle);
			affect(player.getLocation().clone().add(new Vector(radius * Math.cos(phi), 1, radius * Math.sin(phi))));
		}

		return false;
	}

	private void affect(Location location) {
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2.5)) {
			if (ProtectionManager.isEntityProtected(entity)) {
				continue;
			}
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player, NAME, entity.getLocation())) {
				continue;
			}

			if (BlockTools.isObstructed(location, entity.getLocation())) {
				continue;
			}
			entity.setVelocity(Tools.getDirection(player.getLocation(), location).normalize().multiply(1.75));
			EntityTools.damageEntity(player, entity, DAMAGE);
		}
	}

	@Override
	public void progress() {
		if (waterReturn != null) {
			if(!waterReturn.progress()) {
				remove();
			}
			return;
		}

		if (!EntityTools.canBend(player, NAME) 
				|| (!player.isSneaking() && !sourceselected) 
				|| !EntityTools.getBendingAbility(player).equals(NAME) 
				|| !sourceblock.getWorld().equals(player.getWorld()) 
				|| (sourceblock.getLocation().distance(player.getLocation()) > range && sourceselected)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();

			Location location = player.getLocation();

			if (sourceselected) {
				Tools.playFocusWaterEffect(sourceblock);
			} else if (settingup) {
				if (sourceblock.getY() < location.getBlockY()) {
					source.revertBlock();
					source = null;
					Block newBlock = sourceblock.getRelative(BlockFace.UP);
					sourcelocation = newBlock.getLocation();
					if (!BlockTools.isSolid(newBlock)) {
						//source = new TempBlock(newBlock, Material.WATER, full);
						source = TempBlock.makeTemporary(newBlock, Material.WATER);
						sourceblock = newBlock;
					} else {
						returnWater();
						return;
					}
				} else if (sourceblock.getY() > location.getBlockY()) {
					source.revertBlock();
					source = null;
					Block newBlock = sourceblock.getRelative(BlockFace.DOWN);
					sourcelocation = newBlock.getLocation();
					if (!BlockTools.isSolid(newBlock)) {
						//source = new TempBlock(newBlock, Material.WATER, full);
						source = TempBlock.makeTemporary(newBlock, Material.WATER);
						sourceblock = newBlock;
					} else {
						returnWater();
						return;
					}
				} else if (sourcelocation.distance(location) > radius) {
					Vector vector = Tools.getDirection(sourcelocation, location.getBlock().getLocation()).normalize();
					sourcelocation.add(vector);
					Block newBlock = sourcelocation.getBlock();
					if (!newBlock.equals(sourceblock)) {
						if (source != null) {
							source.revertBlock();
						}
						source = null;
						if (!BlockTools.isSolid(newBlock)) {
							//source = new TempBlock(newBlock, Material.WATER, full);
							source = TempBlock.makeTemporary(newBlock, Material.WATER);
							sourceblock = newBlock;
						}
					}
				} else {
					incrementStep();
					source.revertBlock();
					source = null;
					Vector vector = new Vector(1, 0, 0);
					startangle = vector.angle(Tools.getDirection(sourceblock.getLocation(), location));
					angle = startangle;
				}
			} else if (forming) {

				if (angle - startangle >= 360) {
					y += 1;
				} else {
					angle += 20;
				}
				if(!formOctopus()) {
					remove();
					return;
				}
				if (MathUtils.doubleEquals(y, 2)) {
					incrementStep();
				}
				return;
			} else if (formed) {
				step += 1;
				if (step % inc == 0)
					animstep += 1;
				if (animstep > 8)
					animstep = 1;
				if(!formOctopus()) {
					remove();
				}
			} else {
				remove();
				return;
			}
		}
	}

	private boolean formOctopus() {
		Location location = player.getLocation();
		newblocks.clear();

		List<Block> doneblocks = new LinkedList<Block>();

		for (double theta = startangle; theta < startangle + angle; theta += 10) {
			double rtheta = Math.toRadians(theta);
			Block block = location.clone().add(new Vector(radius * Math.cos(rtheta), 0, radius * Math.sin(rtheta))).getBlock();
			if (!doneblocks.contains(block)) {
				addWater(block);
				doneblocks.add(block);
			}
		}

		Vector eyedir = player.getEyeLocation().getDirection();
		eyedir.setY(0);

		double tentacleangle = Math.toDegrees((new Vector(1, 0, 0)).angle(eyedir)) + dta / 2;

		int astep = animstep;
		for (double tangle = tentacleangle; tangle < tentacleangle + 360; tangle += dta) {
			astep += 1;
			double phi = Math.toRadians(tangle);
			tentacle(location.clone().add(new Vector(radius * Math.cos(phi), 0, radius * Math.sin(phi))), astep);
		}

		for (TempBlock block : blocks) {
			if (!newblocks.contains(block))
				block.revertBlock();
		}

		blocks.clear();

		blocks.addAll(newblocks);

		if (blocks.isEmpty())
			return false;

		return true;
	}

	private void tentacle(Location base, int animationstep) {
		if (!TempBlock.isTempBlock(base.getBlock()))
			return;
		if (!blocks.contains(TempBlock.get(base.getBlock())))
			return;

		Vector direction = Tools.getDirection(player.getLocation(), base);
		direction.setY(0);
		direction.normalize();

		if (animationstep > 8) {
			animationstep = animationstep % 8;
		}

		if (y >= 1) {

			Block baseblock = base.clone().add(0, 1, 0).getBlock();

			if (animationstep == 1) {
				addWater(baseblock);
			} else if (animationstep == 2 || animationstep == 8) {
				addWater(baseblock);
			} else {
				addWater(base.clone().add(direction.getX(), 1, direction.getZ()).getBlock());
			}

		}

		if (MathUtils.doubleEquals(y, 2)) {

			Block baseblock = base.clone().add(0, 2, 0).getBlock();

			if (animationstep == 1) {
				addWater(base.clone().add(-direction.getX(), 2, -direction.getZ()).getBlock());
			} else if (animationstep == 3 || animationstep == 7 || animationstep == 2 || animationstep == 8) {
				addWater(baseblock);
			} else if (animationstep == 4 || animationstep == 6) {
				addWater(base.clone().add(direction.getX(), 2, direction.getZ()).getBlock());
			} else {
				addWater(base.clone().add(2 * direction.getX(), 2, 2 * direction.getZ()).getBlock());
			}

		}
	}

	private void addWater(Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, NAME, block.getLocation())) {
			return;
		}

		if (TempBlock.isTempBlock(block)) {
			TempBlock tBlock = TempBlock.get(block);
			if (!newblocks.contains(tBlock)) {
				if (!blocks.contains(tBlock))
					tBlock.setType(Material.WATER, full);
				newblocks.add(tBlock);
			}
		} else if (BlockTools.isWaterbendable(block, player) || block.getType() == Material.FIRE || block.getType() == Material.AIR) {
			//newblocks.add(new TempBlock(block, Material.WATER, full));
			newblocks.add(TempBlock.makeTemporary(block, Material.WATER));
		}
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (isOctopus(player)) {
			OctopusForm form = (OctopusForm) AbilityManager.getManager().getInstances(NAME).get(player);
			if (form.sourceblock == null)
				return false;
			if (form.sourceblock.equals(block))
				return true;
		}
		return false;
	}

	private void clear() {
		if (source != null)
			source.revertBlock();
		for (TempBlock block : blocks)
			block.revertBlock();
	}

	@Override
	public void stop() {
		this.clear();
		if (this.drainedBlock != null) {
			this.drainedBlock.revertBlock();
			drainedBlock = null;
		}
		if (waterReturn != null) {
			waterReturn.stop();
		}
	}

	private void returnWater() {
		if (source != null) {
			source.revertBlock();
			waterReturn = new WaterReturn(player, source.getLocation().getBlock(), this);
			source = null;
		} else {
			Location location = player.getLocation();
			double rtheta = Math.toRadians(startangle);
			Block block = location.clone().add(new Vector(radius * Math.cos(rtheta), 0, radius * Math.sin(rtheta))).getBlock();
			waterReturn = new WaterReturn(player, block, this);
		}
	}

	public static boolean isOctopus(Player player) {
		return AbilityManager.getManager().getInstances(NAME).containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}

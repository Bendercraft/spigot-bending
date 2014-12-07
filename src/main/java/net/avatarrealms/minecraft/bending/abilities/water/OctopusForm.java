package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class OctopusForm implements IAbility {
	private static Map<Player, OctopusForm> instances = new HashMap<Player, OctopusForm>();

	private static int range = 10;
	static final double radius = 3;
	private static final byte full = 0x0;
	private static long interval = 50;
	private static int damage = ConfigManager.octopusFormDamage;

	private Player player;
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
	private IAbility parent;

	public OctopusForm(Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player)) {
			if (instances.get(player).formed) {
				instances.get(player).attack();
				return;
			} else if (!instances.get(player).sourceselected) {
				return;
			}
		}
		this.player = player;
		time = System.currentTimeMillis();
		sourceblock = BlockTools.getWaterSourceBlock(player, range, EntityTools.canPlantbend(player));
		if (sourceblock != null) {
			sourcelocation = sourceblock.getLocation();
			sourceselected = true;
			instances.put(player, this);
		}
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

	public static void form(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).form();
		} else if (WaterReturn.hasWaterBottle(player)) {
			Location eyeloc = player.getEyeLocation();
			Block block = eyeloc.add(eyeloc.getDirection().normalize())
					.getBlock();
			if (BlockTools.isTransparentToEarthbending(player, block)
					&& BlockTools.isTransparentToEarthbending(player,
							eyeloc.getBlock())) {
				block.setType(Material.WATER);
				block.setData(full);
				OctopusForm form = new OctopusForm(player, null);
				form.form();
				if (form.formed || form.forming || form.settingup) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}
			}
		}
	}

	private void form() {
		incrementStep();
		
		if (BlockTools.isPlant(sourceblock)) {
			new Plantbending(sourceblock, this);
			sourceblock.setType(Material.AIR);
		} else if (!BlockTools.adjacentToThreeOrMoreSources(sourceblock)) {
			sourceblock.setType(Material.AIR);
		}
		source = new TempBlock(sourceblock, Material.WATER, full, player, OctopusForm.class);
	}

	private void attack() {
		if (!formed)
			return;
		double tentacleangle = (new Vector(1, 0, 0)).angle(player
				.getEyeLocation().getDirection()) + dta / 2;

		for (double tangle = tentacleangle; tangle < tentacleangle + 360; tangle += dta) {
			double phi = Math.toRadians(tangle);
			affect(player
					.getLocation()
					.clone()
					.add(new Vector(radius * Math.cos(phi), 1, radius
							* Math.sin(phi))));
		}
	}

	private void affect(Location location) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2.5)) {
			if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
				
			if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.OctopusForm,
					entity.getLocation())) {
				continue;
			}
				
			if (BlockTools.isObstructed(location, entity.getLocation())) {
				continue;
			}
			entity.setVelocity(Tools
					.getDirection(player.getLocation(), location).normalize()
					.multiply(1.75));
			EntityTools.damageEntity(player, entity, damage);
		}
	}

	public static void progressAll() {
		List<OctopusForm> toRemove = new LinkedList<OctopusForm>();
		for (OctopusForm octopus : instances.values()) {
			boolean keep = octopus.progress();
			if(!keep) {
				toRemove.add(octopus);
			}
		}
		for(OctopusForm octopus : toRemove) {
			octopus.remove();
		}
	}

	private boolean progress() {
		if (!EntityTools.canBend(player, Abilities.OctopusForm)
				|| (!player.isSneaking() && !sourceselected)
				|| EntityTools.getBendingAbility(player) != Abilities.OctopusForm) {
			returnWater();
			return false;
		}

		if (!sourceblock.getWorld().equals(player.getWorld())) {
			return false;
		}

		if (sourceblock.getLocation().distance(player.getLocation()) > range
				&& sourceselected) {
			return false;
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
					Block newblock = sourceblock.getRelative(BlockFace.UP);
					sourcelocation = newblock.getLocation();
					if (!BlockTools.isSolid(newblock)) {
						source = new TempBlock(newblock, Material.WATER, full, player, OctopusForm.class);
						sourceblock = newblock;
					} else {
						returnWater();
						return false;
					}
				} else if (sourceblock.getY() > location.getBlockY()) {
					source.revertBlock();
					source = null;
					Block newblock = sourceblock.getRelative(BlockFace.DOWN);
					sourcelocation = newblock.getLocation();
					if (!BlockTools.isSolid(newblock)) {
						source = new TempBlock(newblock, Material.WATER, full, player, OctopusForm.class);
						sourceblock = newblock;
					} else {
						returnWater();
						return false;
					}
				} else if (sourcelocation.distance(location) > radius) {
					Vector vector = Tools.getDirection(sourcelocation,
							location.getBlock().getLocation()).normalize();
					sourcelocation.add(vector);
					Block newblock = sourcelocation.getBlock();
					if (!newblock.equals(sourceblock)) {
						if(source != null) {
							source.revertBlock();
						}
						source = null;
						if (!BlockTools.isSolid(newblock)) {
							source = new TempBlock(newblock, Material.WATER,
									full, player, OctopusForm.class);
							sourceblock = newblock;
						}
					}
				} else {
					incrementStep();
					source.revertBlock();
					source = null;
					Vector vector = new Vector(1, 0, 0);
					startangle = vector.angle(Tools.getDirection(
							sourceblock.getLocation(), location));
					angle = startangle;
				}
			} else if (forming) {

				if (angle - startangle >= 360) {
					y += 1;
				} else {
					angle += 20;
				}
				boolean result = formOctopus();
				if (y == 2) {
					incrementStep();
				}
				return result;
			} else if (formed) {
				step += 1;
				if (step % inc == 0)
					animstep += 1;
				if (animstep > 8)
					animstep = 1;
				return formOctopus();
			} else {
				return false;
			}
		}
		
		return true;
	}

	private boolean formOctopus() {
		Location location = player.getLocation();
		newblocks.clear();

		List<Block> doneblocks = new LinkedList<Block>();

		for (double theta = startangle; theta < startangle + angle; theta += 10) {
			double rtheta = Math.toRadians(theta);
			Block block = location
					.clone()
					.add(new Vector(radius * Math.cos(rtheta), 0, radius
							* Math.sin(rtheta))).getBlock();
			if (!doneblocks.contains(block)) {
				addWater(block);
				doneblocks.add(block);
			}
		}

		Vector eyedir = player.getEyeLocation().getDirection();
		eyedir.setY(0);

		double tentacleangle = Math.toDegrees((new Vector(1, 0, 0))
				.angle(eyedir)) + dta / 2;

		int astep = animstep;
		for (double tangle = tentacleangle; tangle < tentacleangle + 360; tangle += dta) {
			astep += 1;
			double phi = Math.toRadians(tangle);
			tentacle(
					location.clone().add(
							new Vector(radius * Math.cos(phi), 0, radius
									* Math.sin(phi))), astep);
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
				addWater(base.clone()
						.add(direction.getX(), 1, direction.getZ()).getBlock());
			}

		}

		if (y == 2) {

			Block baseblock = base.clone().add(0, 2, 0).getBlock();

			if (animationstep == 1) {
				addWater(base.clone()
						.add(-direction.getX(), 2, -direction.getZ())
						.getBlock());
			} else if (animationstep == 3 || animationstep == 7
					|| animationstep == 2 || animationstep == 8) {
				addWater(baseblock);
			} else if (animationstep == 4 || animationstep == 6) {
				addWater(base.clone()
						.add(direction.getX(), 2, direction.getZ()).getBlock());
			} else {
				addWater(base.clone()
						.add(2 * direction.getX(), 2, 2 * direction.getZ())
						.getBlock());
			}

		}
	}

	private void addWater(Block block) {
		clearNearbyWater(block);
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.OctopusForm,
				block.getLocation()))
			return;
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			if (!newblocks.contains(tblock)) {
				if (!blocks.contains(tblock))
					tblock.setType(Material.WATER, full);
				newblocks.add(tblock);
			}
		} else if (BlockTools.isWaterbendable(block, player)
				|| block.getType() == Material.FIRE
				|| block.getType() == Material.AIR) {
			newblocks.add(new TempBlock(block, Material.WATER, full, player, OctopusForm.class));
		}
	}


	private void clearNearbyWater(Block block) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
				BlockFace.WEST, BlockFace.DOWN };
		for (BlockFace face : faces) {
			Block rel = block.getRelative(face);
			if (BlockTools.isWater(rel) && !TempBlock.isTempBlock(rel)) {
				new FreezeMelt(player, this, rel);
				// water.add(new TempBlock(rel, Material.AIR, (byte) 0));
			}
		}
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		if (instances.containsKey(player)) {
			OctopusForm form = instances.get(player);
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
	
	private void remove() {
		this.clear();
		instances.remove(player);
	}

	private void returnWater() {
		if (source != null) {
			source.revertBlock();
			new WaterReturn(player, source.getLocation().getBlock(), this);
			source = null;
		} else {
			Location location = player.getLocation();
			double rtheta = Math.toRadians(startangle);
			Block block = location
					.clone()
					.add(new Vector(radius * Math.cos(rtheta), 0, radius
							* Math.sin(rtheta))).getBlock();
			new WaterReturn(player, block, this);
		}
	}

	public static void removeAll() {
		for (OctopusForm octopus : instances.values()) {
			octopus.clear();
		}
		
		instances.clear();
	}
	
	public static boolean isOctopus(Player player) {
		return instances.containsKey(player);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

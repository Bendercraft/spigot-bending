package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class WaterSpout implements IAbility {
	//TODO Put this variable into configmanager and config file
	public static int SPEED = 3;
	private static Map<Player, WaterSpout> instances = new HashMap<Player, WaterSpout>();
	private static List<Block> affectedblocks = new LinkedList<Block>();
	private static List<Block> newaffectedblocks = new LinkedList<Block>();
	private static final int defaultheight = ConfigManager.waterSpoutHeight;

	private static final byte full = 0x0;
	private int currentCardinalPoint = 0;
	private Player player;
	private Block base;
	private TempBlock baseblock;
	private IAbility parent;

	public WaterSpout(Player player, IAbility parent) {
		this.parent = parent;
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.WaterSpout))
			return;

		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return;
		}
		this.player = player;
		new Flight(player);
		player.setAllowFlight(true);
		instances.put(player, this);
		BendingPlayer.getBendingPlayer(player).earnXP(BendingType.Water,this);
		spout();
	}

	private void remove() {
		revertBaseBlock(player);
		instances.remove(player);
	}

	public static void progressAll() {
		newaffectedblocks.clear();

		List<WaterSpout> toRemoveSpout = new LinkedList<WaterSpout>();
		for (Entry<Player, WaterSpout> entry : instances.entrySet()) {
			Player player = entry.getKey();
			WaterSpout spout = entry.getValue();
			if (!player.isOnline() || player.isDead()) {
				toRemoveSpout.add(spout);
			} else if (EntityTools.hasAbility(player, Abilities.WaterSpout)
					&& EntityTools.canBend(player, Abilities.WaterSpout)) {
				boolean keep = spout.spout();
				if(!keep) {
					toRemoveSpout.add(spout);
				}
			} else {
				toRemoveSpout.add(spout);
			}
		}
		for (WaterSpout spout : toRemoveSpout) {
			spout.remove();
		}

		List<Block> toRemoveBlock = new LinkedList<Block>();
		for(Block block : affectedblocks) {
			if (!newaffectedblocks.contains(block)) {
				toRemoveBlock.add(block);
			}
		}
		for (Block block : toRemoveBlock) {
			affectedblocks.remove(block);
			TempBlock.revertBlock(block, Material.AIR);
		}
	}

	private boolean spout() {
		player.setFallDistance(0);
		player.setSprinting(false);

		player.removePotionEffect(PotionEffectType.SPEED);
		Location location = player.getLocation().clone().add(0, .2, 0);
		Block block = location.clone().getBlock();
		int height = spoutableWaterHeight(location, player);

		// Tools.verbose(height + " " + WaterSpout.height + " "
		// + affectedblocks.size());
		if (height != -1) {
			location = base.getLocation();
			for (int i = 1, cardinalPoint = (int)(currentCardinalPoint/SPEED); i <= height; i++, cardinalPoint++) {
				if (cardinalPoint == 8) {cardinalPoint = 0;}
				
				block = location.clone().add(0, i, 0).getBlock();
				if (!TempBlock.isTempBlock(block)) {
					new TempBlock(block, Material.WATER, full);
				}
				if (!affectedblocks.contains(block)) {
					affectedblocks.add(block);
				}
				newaffectedblocks.add(block);
				
				switch (cardinalPoint) {
					case 0 : block = location.clone().add(0, i, -1).getBlock(); break;
					case 1 : block = location.clone().add(-1, i, -1).getBlock(); break;
					case 2 : block = location.clone().add(-1, i, 0).getBlock(); break;
					case 3 : block = location.clone().add(-1, i, 1).getBlock(); break;
					case 4 : block = location.clone().add(0, i, 1).getBlock(); break;
					case 5 : block = location.clone().add(1, i, 1).getBlock(); break;
					case 6 : block = location.clone().add(1, i, 0).getBlock(); break;
					case 7 : block = location.clone().add(1, i, -1).getBlock(); break;
					default: break;
				}
				
				if (!TempBlock.isTempBlock(block)) {
					new TempBlock(block, Material.WATER, full);
				}
				if (!affectedblocks.contains(block)) {
					affectedblocks.add(block);
				}
				newaffectedblocks.add(block);	
			}
			currentCardinalPoint ++;
			if (currentCardinalPoint == SPEED*8) {
				currentCardinalPoint = 0;
			}
			if (player.getLocation().getBlockY() > block.getY()) {
				player.setFlying(false);
			} else {
				new Flight(player);
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		} else {
			return false;
		}
		return true;
	}

	private static int spoutableWaterHeight(Location location, Player player) {
		WaterSpout spout = instances.get(player);
		int height = defaultheight;
		if (Tools.isNight(player.getWorld()))
			height = (int) PluginTools.waterbendingNightAugment((double) height,
					player.getWorld());
		int maxheight = (int) ((double) defaultheight * ConfigManager.nightFactor) + 5;
		Block blocki;
		for (int i = 0; i < maxheight; i++) {
			blocki = location.clone().add(0, -i, 0).getBlock();
			if (Tools.isRegionProtectedFromBuild(player, Abilities.WaterSpout,
					blocki.getLocation()))
				return -1;
			if (!affectedblocks.contains(blocki)) {
				if (blocki.getType() == Material.WATER
						|| blocki.getType() == Material.STATIONARY_WATER) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock(player);
					}
					spout.base = blocki;
					if (i > height)
						return height;
					return i;
				}
				if (blocki.getType() == Material.ICE
						|| blocki.getType() == Material.SNOW
						|| blocki.getType() == Material.SNOW_BLOCK) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock(player);
						instances.get(player).baseblock = new TempBlock(blocki,
								Material.WATER, full);
					}
					spout.base = blocki;
					if (i > height)
						return height;
					return i;
				}
				if ((blocki.getType() != Material.AIR && (!BlockTools.isPlant(blocki) 
						|| !EntityTools.canPlantbend(player)))) {
					revertBaseBlock(player);
					return -1;
				}
			}
		}
		revertBaseBlock(player);
		return -1;
	}

	public static void revertBaseBlock(Player player) {
		if (instances.containsKey(player)) {
			if (instances.get(player).baseblock != null) {
				instances.get(player).baseblock.revertBlock();
				instances.get(player).baseblock = null;
			}
		}
	}

	public static void removeAll() {
		instances.clear();
		for (Block block : affectedblocks) {
			TempBlock.revertBlock(block, Material.AIR);
		}
		affectedblocks.clear();
	}

	public static List<Player> getPlayers() {
		return new LinkedList<Player>(instances.keySet());
	}

	public static void removeSpouts(Location loc0, double radius,
			Player sourceplayer) {
		for (Player player : instances.keySet()) {
			if (!player.equals(sourceplayer)) {
				Location loc1 = player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				double dx = loc1.getX() - loc0.getX();
				double dy = loc1.getY() - loc0.getY();
				double dz = loc1.getZ() - loc0.getZ();

				double distance = Math.sqrt(dx * dx + dz * dz);

				if (distance <= radius && dy > 0 && dy < defaultheight)
					instances.get(player).remove();
			}
		}
	}
	
	public static boolean isBending(Player player) {
		return instances.containsKey(player);
	}
	
	public static boolean isAffected(Block block) {
		return affectedblocks.contains(block);
	}

	public static String getDescription() {
		return "To use this ability, click while over or in water. "
				+ "You will spout water up from beneath you to experience controlled levitation. "
				+ "This ability is a toggle, so you can activate it then use other abilities and it "
				+ "will remain on. If you try to spout over an area with no water, snow or ice, "
				+ "the spout will dissipate and you will fall. Click again with this ability selected to deactivate it.";
	}

	@Override
	public int getBaseExperience() {
		return 0;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

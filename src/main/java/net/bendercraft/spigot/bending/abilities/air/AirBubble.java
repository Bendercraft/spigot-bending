package net.bendercraft.spigot.bending.abilities.air;

import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.abilities.Bubble;

import java.util.List;


@ABendingAbility(name = AirBubble.NAME, element = BendingElement.AIR, shift = false, canBeUsedWithTools = true)
public class AirBubble extends Bubble {
	public final static String NAME = "AirBubble";

	@ConfigurationParameter("Radius")
	private static double DEFAULT_RADIUS = 4;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 600000;

	public AirBubble(RegisteredAbility register, Player player) {
		super(register, player);

		this.radius = DEFAULT_RADIUS;

		if(bender.hasPerk(BendingPerk.AIR_AIRBUBBLE_RADIUS)) {
			this.radius += 1;
		}
	}

	public boolean isBlockPushable(Block block) {
		if (this.origins.containsKey(block)) {
			return false;
		}

		if (ProtectionManager.isLocationProtectedFromBending(this.player, register, block.getLocation())) {
			return false;
		}

		Material type = block.getType();

		if (Material.DIRT == type
			|| Material.STONE == type
			|| Material.GRAVEL == type
			|| Material.GRASS == type) {
			return false;
		}
		return true;
	}

	@Override
	protected void pushNewBlocks() {
		final Location center = player.getLocation();
		List<Block> affectedBlocks = BlockTools.getFilteredBlocksAroundPoint(center, this.radius, this::isBlockPushable);

		for (Block block : affectedBlocks) {
			TempBlock tempBlock = TempBlock.get(block);
			if (tempBlock == null || tempBlock.isBendAllowed()) {
				Material type = block.getType();
				if (Material.WATER == type || Material.BUBBLE_COLUMN == type) {
					this.origins.put(block, TempBlock.makeTemporary(this, block, Material.AIR,true));
				}
				else if (Material.KELP == type || Material.KELP_PLANT == type) {
					this.origins.put(block, TempBlock.makeTemporary(this, block, Material.JUNGLE_SAPLING,true));
				}
				else if (Material.SEAGRASS == type) {
					this.origins.put(block, TempBlock.makeTemporary(this, block, Material.GRASS, true));
				}
				else if (Material.TALL_SEAGRASS == type) {
					this.origins.put(block, TempBlock.makeTemporary(this, block, Material.TALL_GRASS, true));
				}
				else {
					BlockData blockData = block.getBlockData();
					if (blockData instanceof Waterlogged) {
						Waterlogged data = (Waterlogged) blockData;
						if (data.isWaterlogged()) {
							data.setWaterlogged(false);
							this.origins.put(block, TempBlock.makeTemporary(this, block, type, data, true));
						}
					}
				}
			}
		}
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

}

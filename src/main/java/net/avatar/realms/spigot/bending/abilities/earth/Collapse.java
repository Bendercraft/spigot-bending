package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name="Collapse", element=BendingType.Earth)
public class Collapse implements IAbility {
	
	@ConfigurationParameter("Range")
	 		static int RANGE = 20;
	
	@ConfigurationParameter("Radius")
	private static double RADIUS = 7;
	
	@ConfigurationParameter("Depth")
	 		static int DEPTH = 6;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;
	
	@ConfigurationParameter("Speed")
	 		static double SPEED = 8;

	//TODO : This map is never cleared of any of its item, strange
	private Map<Block, Block> blocks = new HashMap<Block, Block>();
	private Map<Block, Integer> baseblocks = new HashMap<Block, Integer>();
	private double radius = RADIUS;
	private IAbility parent;
	private Player player;

	public Collapse(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Collapse)) {
			return;
		}	

		this.player = player;
		Block sblock = BlockTools.getEarthSourceBlock(player, Abilities.Collapse, RANGE);
		Location location;
		if (sblock == null) {
			location = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
		} else {
			location = sblock.getLocation();
		}
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (BlockTools.isEarthbendable(player, Abilities.Collapse, block)
					&& !blocks.containsKey(block)
					&& block.getY() >= location.getBlockY()) {
				getAffectedBlocks(block);
			}
		}

		if (!baseblocks.isEmpty()) {
			bPlayer.cooldown(Abilities.Collapse, COOLDOWN);
		}

		for (Block block : baseblocks.keySet()) {
			new CompactColumn(player, block.getLocation(), this);
		}
	}

	private void getAffectedBlocks(Block block) {
		Block baseblock = block;
		int tall = 0;
		List<Block> bendableblocks = new ArrayList<Block>();
		bendableblocks.add(block);
		for (int i = 1; i <= DEPTH; i++) {
			Block blocki = block.getRelative(BlockFace.DOWN, i);
			if (BlockTools.isEarthbendable(player, Abilities.Collapse, blocki)) {
				baseblock = blocki;
				bendableblocks.add(blocki);
				tall++;
			} else {
				break;
			}
		}
		baseblocks.put(baseblock, tall);
		for (Block blocki : bendableblocks) {
			blocks.put(blocki, baseblock);
		}

	}
	
	@Override
	public IAbility getParent() {
		return parent;
	}
}

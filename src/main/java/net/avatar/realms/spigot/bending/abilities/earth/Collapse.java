package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name = "Collapse", bind = BendingAbilities.Collapse, element = BendingElement.Earth)
public class Collapse extends BendingActiveAbility {
	@ConfigurationParameter("Range")
	public static int RANGE = 20;

	@ConfigurationParameter("Radius")
	private static double RADIUS = 7;

	@ConfigurationParameter("Depth")
	public static int DEPTH = 6;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;

	@ConfigurationParameter("Speed")
	public static double SPEED = 8;

	// TODO : This map is never cleared of any of its item, strange
	private Map<Block, Block> blocks = new HashMap<Block, Block>();
	private Map<Block, Integer> baseblocks = new HashMap<Block, Integer>();
	private double radius = RADIUS;

	private List<CompactColumn> columns = new LinkedList<CompactColumn>();

	public Collapse(Player player) {
		super(player, null);

	}

	@Override
	public boolean swing() {
		if (state != BendingAbilityState.CanStart) {
			return false;
		}
		bender.cooldown(BendingAbilities.Collapse, COOLDOWN);
		columns.add(new CompactColumn(player));
		state = BendingAbilityState.Progressing;
		AbilityManager.getManager().addInstance(this);
		return false;
	}

	@Override
	public boolean sneak() {
		if (state != BendingAbilityState.CanStart) {
			return false;
		}

		Block sblock = BlockTools.getEarthSourceBlock(player, BendingAbilities.Collapse, RANGE);
		Location location;
		if (sblock == null) {
			location = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
		} else {
			location = sblock.getLocation();
		}
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (BlockTools.isEarthbendable(player, BendingAbilities.Collapse, block) && !blocks.containsKey(block) && block.getY() >= location.getBlockY()) {
				getAffectedBlocks(block);
			}
		}

		if (!baseblocks.isEmpty()) {
			bender.cooldown(BendingAbilities.Collapse, COOLDOWN);
		}

		for (Block block : baseblocks.keySet()) {
			columns.add(new CompactColumn(player, block.getLocation()));
		}
		state = BendingAbilityState.Progressing;
		AbilityManager.getManager().addInstance(this);
		return false;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		if (state == BendingAbilityState.Progressing && columns.isEmpty()) {
			return false;
		}
		for (CompactColumn column : columns) {
			if (!column.progress()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void remove() {
		for (CompactColumn column : columns) {
			column.remove();
		}
		super.remove();
	}

	private void getAffectedBlocks(Block block) {
		Block baseblock = block;
		int tall = 0;
		List<Block> bendableblocks = new ArrayList<Block>();
		bendableblocks.add(block);
		for (int i = 1; i <= DEPTH; i++) {
			Block blocki = block.getRelative(BlockFace.DOWN, i);
			if (BlockTools.isEarthbendable(player, BendingAbilities.Collapse, blocki)) {
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
	public Object getIdentifier() {
		return player;
	}
}

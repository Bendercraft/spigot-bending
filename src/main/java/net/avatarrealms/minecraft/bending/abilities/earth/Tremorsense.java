package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Tremorsense implements IAbility {
	private static Map<Player, Tremorsense> instances = new HashMap<Player, Tremorsense>();
	private static final int maxdepth = ConfigManager.tremorsenseMaxDepth;
	private static final int radius = ConfigManager.tremorsenseRadius;
	private static final byte lightthreshold = ConfigManager.tremorsenseLightThreshold;

	private Player player;
	private Block block;
	private IAbility parent;

	public Tremorsense(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Tremorsense))
			return;

		if (BlockTools.isEarthbendable(player, Abilities.Tremorsense, player
				.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			this.player = player;
			bPlayer.cooldown(Abilities.Tremorsense);
			activate();
			bPlayer.earnXP(BendingType.Earth, this);
		}
	}

	public Tremorsense(Player player, boolean value, IAbility parent) {
		this.parent = parent;
		this.player = player;
		set();
	}

	private void activate() {
		Block block = player.getLocation().getBlock()
				.getRelative(BlockFace.DOWN);
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				boolean earth = false;
				boolean foundair = false;
				Block smokeblock = null;
				for (int k = 0; k <= maxdepth; k++) {
					Block blocki = block.getRelative(BlockFace.EAST, i)
							.getRelative(BlockFace.NORTH, j)
							.getRelative(BlockFace.DOWN, k);
					if (Tools.isRegionProtectedFromBuild(player,
							Abilities.RaiseEarth, blocki.getLocation()))
						continue;
					if (BlockTools.isEarthbendable(player, Abilities.Tremorsense,
							blocki) && !earth) {
						earth = true;
						smokeblock = blocki;
					} else if (!BlockTools.isEarthbendable(player,
							Abilities.Tremorsense, blocki) && earth) {
						foundair = true;
						break;
					} else if (!BlockTools.isEarthbendable(player,
							Abilities.Tremorsense, blocki)
							&& !earth
							&& blocki.getType() != Material.AIR) {
						break;
					}
				}
				if (foundair) {
					smokeblock.getWorld().playEffect(
							smokeblock.getRelative(BlockFace.UP).getLocation(),
							Effect.SMOKE, 4, radius);
				}
			}
		}

	}

	private void set() {
		Block standblock = player.getLocation().getBlock()
				.getRelative(BlockFace.DOWN);

		if (!BendingPlayer.getBendingPlayer(player).isTremorsensing()) {
			if (block != null)
				revert();
			return;
		}

		if (BlockTools.isEarthbendable(player, Abilities.Tremorsense, standblock)
				&& block == null) {
			block = standblock;
			player.sendBlockChange(block.getLocation(), Material.GLOWSTONE, (byte) 1);
			instances.put(player, this);
		} else if (BlockTools.isEarthbendable(player, Abilities.Tremorsense,
				standblock) && !block.equals(standblock)) {
			revert();
			block = standblock;
			player.sendBlockChange(block.getLocation(), Material.GLOWSTONE, (byte) 1);
			instances.put(player, this);
		} else if (block == null) {
			return;
		} else if (player.getWorld() != block.getWorld()) {
			revert();
		} else if (!BlockTools.isEarthbendable(player, Abilities.Tremorsense,
				standblock)) {
			revert();
		}
	}

	private void revert() {
		if (block != null) {
			player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
			instances.remove(player);
		}
	}

	public static void progressAll() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (instances.containsKey(player)
					&& (!EntityTools.hasAbility(player, Abilities.Tremorsense)
							|| !EntityTools.canBend(player, Abilities.Tremorsense) || player
							.getLocation().getBlock().getLightLevel() > lightthreshold)) {
				instances.get(player).revert();
			} else if (instances.containsKey(player)) {
				instances.get(player).set();
			} else if (EntityTools.hasAbility(player, Abilities.Tremorsense)
					&& EntityTools.canBend(player, Abilities.Tremorsense)
					&& player.getLocation().getBlock().getLightLevel() < lightthreshold) {
				new Tremorsense(player, false, null);
			}
		}
	}

	public static void removeAll() {
		List<Player> temp = new LinkedList<Player>(instances.keySet());
		for (Player player : temp) {
			instances.get(player).revert();
		}

	}

	public static String getDescription() {
		return "This is a pure utility ability for earthbenders. If you have this ability bound to any "
				+ "slot whatsoever, then you are able to 'see' using the earth. If you are in an area of low-light "
				+ "and are standing on top of an earthbendable block, this ability will automatically turn that block into "
				+ "glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out, "
				+ "as you have lost contact with the earth and cannot 'see' until you can touch earth again. "
				+ "Additionally, if you click with this ability selected, smoke will appear above nearby earth "
				+ "with pockets of air beneath them.";
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

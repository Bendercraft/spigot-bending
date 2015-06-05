package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

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
					if (ProtectionManager.isRegionProtectedFromBending(player,
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
				remove();
			return;
		}

		if (BlockTools.isEarthbendable(player, Abilities.Tremorsense, standblock)
				&& block == null) {
			block = standblock;
			player.sendBlockChange(block.getLocation(), Material.GLOWSTONE, (byte) 1);
			instances.put(player, this);
		} else if (BlockTools.isEarthbendable(player, Abilities.Tremorsense,
				standblock) && !block.equals(standblock)) {
			remove();
			block = standblock;
			player.sendBlockChange(block.getLocation(), Material.GLOWSTONE, (byte) 1);
			instances.put(player, this);
		} else if (block == null) {
			return;
		} else if (player.getWorld() != block.getWorld()) {
			remove();
		} else if (!BlockTools.isEarthbendable(player, Abilities.Tremorsense,
				standblock)) {
			remove();
		}
	}

	private void remove() {
		if (block != null) {
			player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
			instances.remove(player);
		}
	}
	
	private boolean progress() {
		if (!EntityTools.hasAbility(player, Abilities.Tremorsense)
						|| !EntityTools.canBend(player, Abilities.Tremorsense) || player
						.getLocation().getBlock().getLightLevel() > lightthreshold) {
			return false;
		}
		this.set();
		return true;
	}

	public static void progressAll() {
		List<Tremorsense> toRemove = new LinkedList<Tremorsense>();
		for(Tremorsense sense : instances.values()) {
			boolean keep = sense.progress();
			if(!keep) {
				toRemove.add(sense);
			}
		}
		for(Tremorsense sense : toRemove) {
			sense.remove();
		}
	}

	public static void removeAll() {
		List<Tremorsense> toRemove = new LinkedList<Tremorsense>(instances.values());
		for(Tremorsense sense : toRemove) {
			sense.remove();
		}

	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

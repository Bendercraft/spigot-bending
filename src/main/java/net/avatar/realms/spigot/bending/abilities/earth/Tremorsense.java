package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

@BendingAbility(name="Tremor Sense", element=BendingType.Earth)
public class Tremorsense implements IAbility {
	private static Map<Player, Tremorsense> instances = new HashMap<Player, Tremorsense>();
	
	@ConfigurationParameter("Max-Depth")
	private static int DEPTH = 10;
	
	@ConfigurationParameter("Radius")
	private static int RADIUS = 5;
	
	@ConfigurationParameter("Light-Threshold")
	private static byte LIGHT_THRESHOLD = 7;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1000;

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
			bPlayer.cooldown(Abilities.Tremorsense, COOLDOWN);
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
		for (int i = -RADIUS; i <= RADIUS; i++) {
			for (int j = -RADIUS; j <= RADIUS; j++) {
				boolean earth = false;
				boolean foundair = false;
				Block smokeblock = null;
				for (int k = 0; k <= DEPTH; k++) {
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
							Effect.SMOKE, 4, RADIUS);
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
	private void remove() {
		if (block != null) {
			player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
			instances.remove(player);
		}
	}
	
	private boolean progress() {
		if (!EntityTools.hasAbility(player, Abilities.Tremorsense)
						|| !EntityTools.canBend(player, Abilities.Tremorsense) || player
						.getLocation().getBlock().getLightLevel() > LIGHT_THRESHOLD) {
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

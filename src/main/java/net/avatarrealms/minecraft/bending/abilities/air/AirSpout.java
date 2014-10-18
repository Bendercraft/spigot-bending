package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class AirSpout implements IAbility {

	private static Map<Player, AirSpout> instances = new HashMap<Player, AirSpout>();

	private static final double height = ConfigManager.airSpoutHeight;
	private static final long interval = 100;
	
	private static final ParticleEffect VISUAL = ParticleEffect.ENCHANTMENT_TABLE;

	private Player player;
	private long time;
	private int angle = 0;
	private IAbility parent;

	public AirSpout(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.AirSpout))
			return;

		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return;
		}
		this.player = player;
		time = System.currentTimeMillis();
		new Flight(player);
		instances.put(player, this);
		bPlayer.cooldown(Abilities.AirSpout);
		boolean keep = spout();
		if(!keep) {
			this.remove();
		}
	}

	public static void spoutAll() {
		List<AirSpout> toRemove = new LinkedList<AirSpout>();
		for (AirSpout spout : instances.values()) {
			boolean keep = spout.spout();
			if(!keep) {
				toRemove.add(spout);
			}
		}
		
		for (AirSpout spout : toRemove) {
			spout.remove();
		}
	}

	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		players.addAll(instances.keySet());
		return players;
	}

	private boolean spout() {
		if (!EntityTools.canBend(player, Abilities.AirSpout)
				|| !EntityTools.hasAbility(player, Abilities.AirSpout)
				|| player.getEyeLocation().getBlock().isLiquid()
				|| BlockTools.isSolid(player.getEyeLocation().getBlock())
				|| player.isDead() || !player.isOnline()) {
			return false;
		}
		player.setFallDistance(0);
		player.setSprinting(false);
		Block block = getGround();
		if (block != null) {
			double dy = player.getLocation().getY() - block.getY();
			if (dy > height) {
				removeFlight();
			} else {
				allowFlight();
			}
			rotateAirColumn(block);
		} else {
			return false;
		}
		
		return true;
	}

	private void allowFlight() {
		player.setAllowFlight(true);
		player.setFlying(true);
		// flight speed too
	}

	private void removeFlight() {
		player.setAllowFlight(false);
		player.setFlying(false);
		// player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE);
		// flight speed too
	}

	private Block getGround() {
		Block standingblock = player.getLocation().getBlock();
		for (int i = 0; i <= height + 5; i++) {
			Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (BlockTools.isSolid(block) || block.isLiquid()) {
				return block;
			}
		}
		return null;
	}

	private void rotateAirColumn(Block block) {

		if (System.currentTimeMillis() >= time + interval) {
			time = System.currentTimeMillis();

			Location location = block.getLocation();
			Location playerloc = player.getLocation();
			location = new Location(location.getWorld(), playerloc.getX(),
					location.getY(), playerloc.getZ());

			double dy = playerloc.getY() - block.getY();
			if (dy > height)
				dy = height;
			Integer[] directions = { 0, 1, 2, 3, 5, 6, 7, 8 };
			int index = angle;

			angle++;
			if (angle >= directions.length)
				angle = 0;
			for (int i = 1; i <= dy; i++) {

				index += 1;
				if (index >= directions.length)
					index = 0;

				Location effectloc2 = new Location(location.getWorld(),
						location.getX(), block.getY() + i, location.getZ());

				//location.getWorld().playEffect(effectloc2, Effect.SMOKE, (int) directions[index], (int) height + 5);
				VISUAL.display(effectloc2, 0, 0, 0, 1, 1);
			}
		}
	}

	public static void removeSpouts(Location loc0, double radius,
			Player sourceplayer) {
		List<AirSpout> toRemove = new LinkedList<AirSpout>();
		for (Player player : instances.keySet()) {
			if (!player.equals(sourceplayer)) {
				Location loc1 = player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				double dx = loc1.getX() - loc0.getX();
				double dy = loc1.getY() - loc0.getY();
				double dz = loc1.getZ() - loc0.getZ();

				double distance = Math.sqrt(dx * dx + dz * dz);

				if (distance <= radius && dy > 0 && dy < height)
					toRemove.add(instances.get(player));
			}
		}
		for(AirSpout spout : toRemove) {
			spout.remove();
		}
	}

	private void clear() {
		removeFlight();
	}
	
	private void remove() {
		clear();
		instances.remove(player);
	}

	public static void removeAll() {
		for (AirSpout spout : instances.values()) {
			spout.clear();
		}
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
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
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@BendingAbility(name="Air Scooter", element=BendingType.Air)
public class AirScooter implements IAbility {
	private static Map<Player, AirScooter> instances = new HashMap<Player, AirScooter>();

	@ConfigurationParameter("Speed")
	private static double SPEED = 0.675;
	
	private static final long INTERVAL = 100;
	private static final double SCOOTER_RADIUS = 1;

	private Player player;
	private Block floorblock;
	private long time;
	private ArrayList<Double> angles = new ArrayList<Double>();
	private IAbility parent;

	public AirScooter(Player player, IAbility parent) {
		this.parent = parent;
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.AirScooter))
			return;

		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return;
		}
		if (!player.isSprinting()
				|| BlockTools.isSolid(player.getEyeLocation().getBlock())
				|| player.getEyeLocation().getBlock().isLiquid())
			return;
		if (BlockTools.isSolid(player.getLocation().add(0, -.5, 0).getBlock()))
			return;
		this.player = player;
		new Flight(player);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setSprinting(false);
		time = System.currentTimeMillis();
		for (int i = 0; i < 5; i++) {
			angles.add((double) (60 * i));
		}
		instances.put(player, this);
		progress();
	}

	private boolean progress() {
		getFloor();
		if (floorblock == null) {
			return false;
		}
		if (!EntityTools.canBend(player, Abilities.AirScooter)
				|| !EntityTools.hasAbility(player, Abilities.AirScooter)) {
			return false;
		}
		if (!player.isOnline() || player.isDead() || !player.isFlying()) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.AirScooter,
				player.getLocation())) {
			return false;
		}

		Vector velocity = player.getEyeLocation().getDirection().clone();
		velocity.setY(0);
		velocity = velocity.clone().normalize().multiply(SPEED);
		if (System.currentTimeMillis() > time + INTERVAL) {
			time = System.currentTimeMillis();
			if (player.getVelocity().length() < SPEED * .5) {
				return false;
			}
			spinScooter();
		}
		double distance = player.getLocation().getY()
				- (double) floorblock.getY();
		double dx = Math.abs(distance - 2.4);
		if (distance > 2.75) {
			velocity.setY(-.25 * dx * dx);
		} else if (distance < 2) {
			velocity.setY(.25 * dx * dx);
		} else {
			velocity.setY(0);
		}
		Location loc = player.getLocation();
		loc.setY((double) floorblock.getY() + 1.5);
		// player.setFlying(true);
		// player.teleport(loc.add(velocity));
		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.setVelocity(velocity);
		
		return true;
	}

	private void spinScooter() {
		Location origin = player.getLocation().clone();
		origin.add(0, -SCOOTER_RADIUS, 0);
		for (int i = 0; i < 5; i++) {
			double x = Math.cos(Math.toRadians(angles.get(i))) * SCOOTER_RADIUS;
			double y = ((double) i) / 2 * SCOOTER_RADIUS - SCOOTER_RADIUS;
			double z = Math.sin(Math.toRadians(angles.get(i))) * SCOOTER_RADIUS;
			player.getWorld().playEffect(origin.clone().add(x, y, z),
					Effect.SMOKE, 4, (int) AirBlast.DEFAULT_RANGE);
		}
		for (int i = 0; i < 5; i++) {
			angles.set(i, angles.get(i) + 10);
		}
	}

	private void getFloor() {
		floorblock = null;
		for (int i = 0; i <= 7; i++) {
			Block block = player.getEyeLocation().getBlock()
					.getRelative(BlockFace.DOWN, i);
			if (BlockTools.isSolid(block) || block.isLiquid()) {
				floorblock = block;
				return;
			}
		}
	}

	private void remove() {
		instances.remove(player);
	}

	public static void check(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).remove();
		}
	}

	public static void progressAll() {
		List<AirScooter> toRemove = new LinkedList<AirScooter>();
		for (AirScooter scooter : instances.values()) {
			boolean keep = scooter.progress();
			if(!keep) {
				toRemove.add(scooter);
			}
		}
		
		for(AirScooter scooter : toRemove) {
			scooter.remove();
		}
	}

	public static void removeAll() {
		instances.clear();
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

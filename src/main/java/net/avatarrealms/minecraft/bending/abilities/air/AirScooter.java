package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class AirScooter implements IAbility {
	private static Map<Player, AirScooter> instances = new HashMap<Player, AirScooter>();

	private static final double speed = ConfigManager.airScooterSpeed;
	private static final long interval = 100;
	private static final double scooterradius = 1;

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

		if (Tools.isRegionProtectedFromBuild(player, Abilities.AirScooter,
				player.getLocation())) {
			return false;
		}

		Vector velocity = player.getEyeLocation().getDirection().clone();
		velocity.setY(0);
		velocity = velocity.clone().normalize().multiply(speed);
		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();
			if (player.getVelocity().length() < speed * .5) {
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
		origin.add(0, -scooterradius, 0);
		for (int i = 0; i < 5; i++) {
			double x = Math.cos(Math.toRadians(angles.get(i))) * scooterradius;
			double y = ((double) i) / 2 * scooterradius - scooterradius;
			double z = Math.sin(Math.toRadians(angles.get(i))) * scooterradius;
			player.getWorld().playEffect(origin.clone().add(x, y, z),
					Effect.SMOKE, 4, (int) AirBlast.defaultrange);
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

	public static String getDescription() {
		return "AirScooter is a fast means of transportation. To use, sprint, jump then click with "
				+ "this ability selected. You will hop on a scooter of air and be propelled forward "
				+ "in the direction you're looking (you don't need to press anything). "
				+ "This ability can be used to levitate above liquids, but it cannot go up steep slopes. "
				+ "Any other actions will deactivate this ability.";
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
	public int getBaseExperience() {
		return 0;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

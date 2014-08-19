package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Catapult implements IAbility {
	private static Map<Integer, Catapult> instances = new HashMap<Integer, Catapult>();

	private static int length = ConfigManager.catapultLength;
	private static double speed = ConfigManager.catapultSpeed;
	private static double push = ConfigManager.catapultPush;

	private static long interval = (long) (1000. / speed);
	// private static long interval = 1500;

	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private int distance;
	private boolean catapult = false;
	private boolean moving = false;
	private boolean flying = false;
	private long time;
	private long starttime;
	private int ticks = 0;
	private IAbility parent;

	public Catapult(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Catapult))
			return;

		this.player = player;
		origin = player.getEyeLocation().clone();
		direction = origin.getDirection().clone().normalize();
		Vector neg = direction.clone().multiply(-1);

		Block block;
		distance = 0;
		for (int i = 0; i <= length; i++) {
			location = origin.clone().add(neg.clone().multiply((double) i));
			block = location.getBlock();
			if (BlockTools.isEarthbendable(player, block)) {
				// block.setType(Material.SANDSTONE);
				distance = BlockTools.getEarthbendableBlocksLength(player, block,
						neg, length - i);
				break;
			} else if (!BlockTools.isTransparentToEarthbending(player, block)) {
				break;
			}
		}

		if (distance != 0) {
			if ((double) distance >= location.distance(origin)) {
				catapult = true;
			}
			time = System.currentTimeMillis() - interval;
			starttime = System.currentTimeMillis();
			moving = true;
			instances.put(player.getEntityId(), this);
			bPlayer.cooldown(Abilities.Catapult);
			bPlayer.earnXP(BendingType.Earth, this);
		}

	}

	public Catapult(Player player, Catapult source, IAbility parent) {
		this.parent = parent;
		flying = true;
		this.player = player;
		moving = false;
		location = source.location.clone();
		starttime = source.starttime;
		direction = source.direction.clone();
		distance = source.distance;
		time = source.time;
		instances.put(player.getEntityId(), this);
		fly();
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (moving)
				if (!moveEarth()) {
					moving = false;
				}
		}

		if (flying)
			return fly();

		if (!flying && !moving && System.currentTimeMillis() > starttime + 1000)
			return false;
		return true;
	}

	private boolean fly() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		// Tools.verbose(player.getLocation().distance(location));
		if (player.getWorld() != location.getWorld()) {
			return false;
		}

		if (player.getLocation().distance(location) < 3) {
			if (!moving && System.currentTimeMillis() > starttime + 1000)
				flying = false;
			return true;
		}

		for (Block block : BlockTools.getBlocksAroundPoint(player.getLocation(), 1.5)) {
			if ((BlockTools.isSolid(block) || block.isLiquid())) {
				flying = false;
				return true;
			}
		}
		Vector vector = direction.clone().multiply(push * distance / length);
		vector.setY(player.getVelocity().getY());
		player.setVelocity(vector);
		return true;
	}

	private void remove() {
		instances.remove(player.getEntityId());
	}

	private boolean moveEarth() {
		if (ticks > distance) {
			return false;
		} else {
			ticks++;
		}

		// Tools.moveEarth(player, location, direction, distance, false);
		location = location.clone().add(direction);

		if (catapult) {
			if (location.distance(origin) < .5) {
				boolean remove = false;
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(origin, 2)) {
					if (entity instanceof Player) {
						Player target = (Player) entity;
						boolean equal = target.getEntityId() == player
								.getEntityId();
						if (equal) {
							remove();
							remove = true;
						}
						if (equal || target.isSneaking()) {
							new Flight(target);
							target.setAllowFlight(true);
							new Catapult(target, this);
						}
					}
					entity.setVelocity(direction.clone().multiply(
							push * distance / length));

				}
				return remove;
			}
		} else {
			if (location.distance(origin) <= length - distance) {
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2)) {
					entity.setVelocity(direction.clone().multiply(
							push * distance / length));
				}
				return false;
			}
		}
		BlockTools.moveEarth(player, location.clone().subtract(direction),
				direction, distance, false);
		return true;
	}

	public static void progressAll() {
		List<Catapult> toRemove = new LinkedList<Catapult>();
		
		for(Catapult catapult : instances.values()) {
			boolean keep = catapult.progress();
			if(!keep) {
				toRemove.add(catapult);
			}
		}
		for(Catapult catapult : toRemove) {
			catapult.remove();
		}
	}

	public static List<Player> getPlayers() {
		List<Player> players = new LinkedList<Player>();
		for (Catapult catapult : instances.values()) {
			Player player = catapult.player;
			if (!players.contains(player))
				players.add(player);
		}
		return players;
	}

	public static void removeAll() {
		instances.clear();
	}

	public static String getDescription() {
		return "To use, left-click while looking in the direction you want to be launched. "
				+ "A pillar of earth will jut up from under you and launch you in that direction - "
				+ "if and only if there is enough earth behind where you're looking to launch you. "
				+ "Skillful use of this ability takes much time and work, and it does result in the "
				+ "death of certain gung-ho earthbenders. If you plan to use this ability, be sure "
				+ "you've read about your passive ability you innately have as an earthbender.";
	}

	@Override
	public int getBaseExperience() {
		return 6;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

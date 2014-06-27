package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class AirBlast implements IAbility {
	private static Map<Integer, AirBlast> instances = new HashMap<Integer, AirBlast>();
	private static Map<Player, Location> origins = new HashMap<Player, Location>();

	private static int ID = Integer.MIN_VALUE;
	static final int maxticks = 10000;

	public static double speed = ConfigManager.airBlastSpeed;
	public static double defaultrange = ConfigManager.airBlastRange;
	public static double affectingradius = ConfigManager.airBlastRadius;
	public static double defaultpushfactor = ConfigManager.airBlastPush;
	private static double originselectrange = 10;
	static final double maxspeed = 1. / defaultpushfactor;
	// public static long interval = 2000;
	public static byte full = 0x0;

	private Location location;
	private Location origin;
	private Vector direction;
	private Player player;
	private int id;
	private double speedfactor;
	private double range = defaultrange;
	private double pushfactor = defaultpushfactor;
	private boolean otherorigin = false;
	private int ticks = 0;
	private int cptEntitiesHit = 0;

	private List<Block> affectedlevers = new ArrayList<Block>();
	private IAbility parent;

	public AirBlast(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.AirBlast))
			return;

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		
		this.player = player;
		if (origins.containsKey(player)) {
			otherorigin = true;
			origin = origins.get(player);
			origins.remove(player);
			Entity entity = EntityTools.getTargettedEntity(player, range);
			if (entity != null) {
				direction = Tools.getDirection(origin, entity.getLocation())
						.normalize();
			} else {
				direction = Tools.getDirection(origin,
						EntityTools.getTargetedLocation(player, range)).normalize();
			}
		} else {
			origin = player.getEyeLocation();
			direction = player.getEyeLocation().getDirection().normalize();
		}
		location = origin.clone();
		id = ID;
		instances.put(id, this);
		cptEntitiesHit = 0;
		bPlayer.cooldown(Abilities.AirBlast);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	public AirBlast(Location location, Vector direction, Player player,
			double factorpush, AirBurst burst) {
		this.parent = burst;
		if (location.getBlock().isLiquid()) {
			return;
		}

		this.player = player;
		origin = location.clone();
		this.direction = direction.clone();
		this.location = location.clone();
		id = ID;
		pushfactor *= factorpush;
		instances.put(id, this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	public static void setOrigin(Player player) {
		Location location = EntityTools.getTargetedLocation(player,
				originselectrange, BlockTools.nonOpaque);
		if (location.getBlock().isLiquid()
				|| BlockTools.isSolid(location.getBlock()))
			return;

		if (Tools.isRegionProtectedFromBuild(player, Abilities.AirBlast,
				location))
			return;

		origins.put(player, location);
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (Tools.isRegionProtectedFromBuild(player, Abilities.AirBlast,
				location)) {
			return false;
		}

		speedfactor = speed * (Bending.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			return false;
		}

		Block block = location.getBlock();
		for (Block testblock : BlockTools.getBlocksAroundPoint(location,
				affectingradius)) {
			if (testblock.getType() == Material.FIRE) {
				testblock.setType(Material.AIR);
				testblock.getWorld().playEffect(testblock.getLocation(),
						Effect.EXTINGUISH, 0);
			}
			if (((block.getType() == Material.LEVER) || (block.getType() == Material.STONE_BUTTON))
					&& !affectedlevers.contains(block)) {

				affectedlevers.add(block);
			}
		}
		if ((BlockTools.isSolid(block) || block.isLiquid())
				&& !affectedlevers.contains(block)) {
			if (block.getType() == Material.LAVA
					|| block.getType() == Material.STATIONARY_LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
			}
			return false;
		}

		if (location.distance(origin) > range) {
			return false;
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
				affectingradius)) {
			affect(entity);
			cptEntitiesHit ++;
		}
		
		advanceLocation();

		return true;
	}

	private void advanceLocation() {
		location.getWorld().playEffect(location, Effect.SMOKE, 4, (int) range);
		location = location.add(direction.clone().multiply(speedfactor));
	}

	private void affect(Entity entity) {
		boolean isUser = entity.getEntityId() == player.getEntityId();

		if (!isUser || otherorigin) {
			Vector velocity = entity.getVelocity();
			// double mag = Math.abs(velocity.getY());
			double max = maxspeed;
			double factor = pushfactor;
			if (AvatarState.isAvatarState(player)) {
				max = AvatarState.getValue(maxspeed);
				factor = AvatarState.getValue(factor);
			}

			Vector push = direction.clone();
			if (Math.abs(push.getY()) > max && !isUser) {
				if (push.getY() < 0)
					push.setY(-max);
				else
					push.setY(max);
			}

			factor *= 1 - location.distance(origin) / (2 * range);

			if (isUser
					&& BlockTools.isSolid(player.getLocation().add(0, -.5, 0)
							.getBlock())) {
				factor *= .5;
			}

			double comp = velocity.dot(push.clone().normalize());
			if (comp > factor) {
				velocity.multiply(.5);
				velocity.add(push
						.clone()
						.normalize()
						.multiply(
								velocity.clone().dot(push.clone().normalize())));
			} else if (comp + factor * .5 > factor) {
				velocity.add(push.clone().multiply(factor - comp));
			} else {
				velocity.add(push.clone().multiply(factor * .5));
			}

			entity.setVelocity(velocity);
			entity.setFallDistance(0);
			if (!isUser && entity instanceof Player) {
				new Flight((Player) entity, player);
			}
			if (entity.getFireTicks() > 0)
				entity.getWorld().playEffect(entity.getLocation(),
						Effect.EXTINGUISH, 0);
			entity.setFireTicks(0);
		}
	}

	public static void progressAll() {
		List<AirBlast> toRemove = new LinkedList<AirBlast>();
		for (AirBlast blast : instances.values()) {
			if (!blast.progress()) {
				toRemove.add(blast);
				if (blast.cptEntitiesHit >=1 && blast.parent == null) {
					BendingPlayer.getBendingPlayer(blast.player).earnXP(BendingType.Air,blast);
				}
			}
		}
		
		for(AirBlast blastTR : toRemove) {
			instances.remove(blastTR);
		}
		toRemove.clear();
			
		for (Player player : origins.keySet()) {
			playOriginEffect(player);
		}
	}

	private static void playOriginEffect(Player player) {
		if (!origins.containsKey(player))
			return;
		Location origin = origins.get(player);
		if (!origin.getWorld().equals(player.getWorld())) {
			origins.remove(player);
			return;
		}

		if (EntityTools.getBendingAbility(player) != Abilities.AirBlast
				|| !EntityTools.canBend(player, Abilities.AirBlast)) {
			origins.remove(player);
			return;
		}

		if (origin.distance(player.getEyeLocation()) > originselectrange) {
			origins.remove(player);
			return;
		}

		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
				(int) originselectrange);
	}

	public static void removeAll() {
		instances.clear();
	}

	public static String getDescription() {
		return "AirBlast is the most fundamental bending technique of an airbender."
				+ " To use, simply left-click in a direction. A gust of wind will be"
				+ " created at your fingertips, launching anything in its path harmlessly back."
				+ " A gust of air can extinguish fires on the ground or on a player, can cool lava, and "
				+ "can flip levers and activate buttons. Additionally, tapping sneak will change the "
				+ "origin of your next AirBlast to your targeted location.";
	}

	@Override
	public int getBaseExperience() {
		return 5;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

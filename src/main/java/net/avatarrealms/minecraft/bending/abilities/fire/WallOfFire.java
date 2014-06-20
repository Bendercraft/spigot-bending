package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WallOfFire {

	private Player player;

	private static double maxangle = 50;

	private static int range = ConfigManager.wallOfFireRange;
	private int height = ConfigManager.wallOfFireHeight;
	private int width = ConfigManager.wallOfFireWidth;
	private long duration = ConfigManager.wallOfFireDuration;
	private int damage = ConfigManager.wallOfFireDamage;
	private static long interval = 250;
	private static long cooldown = ConfigManager.wallOfFireCooldown;
	public static ConcurrentHashMap<Player, WallOfFire> instances = new ConcurrentHashMap<Player, WallOfFire>();
	private static long damageinterval = ConfigManager.wallOfFireInterval;

	private Location origin;
	private long time, starttime;
	private boolean active = true;
	private int damagetick = 0, intervaltick = 0;
	private List<Block> blocks = new ArrayList<Block>();

	public WallOfFire(Player player) {
		if (instances.containsKey(player) && !AvatarState.isAvatarState(player)) {
			return;
		}

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.WallOfFire))
			return;

		this.player = player;

		origin = EntityTools.getTargetedLocation(player, range);

		World world = player.getWorld();

		if (Tools.isDay(player.getWorld())) {
			width = (int) PluginTools.firebendingDayAugment((double) width, world);
			height = (int) PluginTools.firebendingDayAugment((double) height, world);
			duration = (long) PluginTools.firebendingDayAugment((double) duration,
					world);
			damage = (int) PluginTools.firebendingDayAugment((double) damage, world);
		}

		time = System.currentTimeMillis();
		starttime = time;

		Block block = origin.getBlock();

		if (block.isLiquid() || BlockTools.isSolid(block)) {
			return;
		}

		Vector direction = player.getEyeLocation().getDirection();
		Vector compare = direction.clone();
		compare.setY(0);
		// double angle = direction.angle(compare);
		// Tools.verbose(Math.toDegrees(angle));

		if (Math.abs(direction.angle(compare)) > Math.toRadians(maxangle)) {
			return;
		}

		initializeBlocks();

		instances.put(player, this);
	}

	private void progress() {
		time = System.currentTimeMillis();

		if (time - starttime > cooldown) {
			instances.remove(player);
			return;
		}

		if (!active)
			return;

		if (time - starttime > duration) {
			active = false;
			return;
		}

		if (time - starttime > intervaltick * interval) {
			intervaltick++;
			display();
		}

		if (time - starttime > damagetick * damageinterval) {
			damagetick++;
			damage();
		}

	}

	private void initializeBlocks() {
		Vector direction = player.getEyeLocation().getDirection();
		direction = direction.normalize();

		Vector ortholr = Tools.getOrthogonalVector(direction, 0, 1);
		ortholr = ortholr.normalize();

		Vector orthoud = Tools.getOrthogonalVector(direction, 90, 1);
		orthoud = orthoud.normalize();

		double w = (double) width;
		double h = (double) height;

		for (double i = -w; i <= w; i++) {
			for (double j = -h; j <= h; j++) {
				Location location = origin.clone().add(
						orthoud.clone().multiply(j));
				location = location.add(ortholr.clone().multiply(i));
				if (Tools.isRegionProtectedFromBuild(player,
						Abilities.WallOfFire, location))
					continue;
				Block block = location.getBlock();
				if (!blocks.contains(block))
					blocks.add(block);
			}
		}

	}

	private void display() {
		for (Block block : blocks) {
			block.getWorld().playEffect(block.getLocation(),
					Effect.MOBSPAWNER_FLAMES, 0, 15);
		}
	}

	private void damage() {
		double radius = height;
		if (radius < width)
			radius = width;
		radius = radius + 1;
		List<Entity> entities = EntityTools.getEntitiesAroundPoint(origin, radius);
		if (entities.contains(player))
			entities.remove(player);
		for (Entity entity : entities) {
			if (Tools.isRegionProtectedFromBuild(player, Abilities.WallOfFire,
					entity.getLocation()))
				continue;
			for (Block block : blocks) {
				if (entity.getLocation().distance(block.getLocation()) <= 1.5) {
					affect(entity);
					break;
				}			
			}
			if (((entity instanceof Player) ||(entity instanceof Monster)) && (entity.getEntityId() != player.getEntityId())) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer != null) {
					bPlayer.earnXP(BendingType.Fire);
				}
			}
		}
	}

	private void affect(Entity entity) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		entity.setFireTicks(50);
		entity.setVelocity(new Vector(0, 0, 0));
		if (entity instanceof LivingEntity) {
			EntityTools.damageEntity(player, entity, bPlayer.getCriticalHit(BendingType.Fire,damage));
			new Enflamed(entity, player);
		}
	}

	public static String getDescription() {
		return "To use this ability, click at a location. A wall of fire "
				+ "will appear at this location, igniting enemies caught in it "
				+ "and blocking projectiles.";
	}

	public static void manage() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}
}
package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WallOfFire implements IAbility {
	private static Map<Player, WallOfFire> instances = new HashMap<Player, WallOfFire>();

	private static double maxangle = 50;
	private static int range = ConfigManager.wallOfFireRange;
	private static long interval = 250;
	private static long cooldown = ConfigManager.wallOfFireCooldown;
	private static long damageinterval = ConfigManager.wallOfFireInterval;

	private int height = ConfigManager.wallOfFireHeight;
	private int width = ConfigManager.wallOfFireWidth;
	private long duration = ConfigManager.wallOfFireDuration;
	private int damage = ConfigManager.wallOfFireDamage;
	private Player player;
	private Location origin;
	private long time, starttime;
	private boolean active = true;
	private int damagetick = 0, intervaltick = 0;
	private List<Block> blocks = new LinkedList<Block>();
	private IAbility parent;

	public WallOfFire(Player player, IAbility parent) {
		this.parent = parent;
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
			width = (int) PluginTools.firebendingDayAugment((double) width,
					world);
			height = (int) PluginTools.firebendingDayAugment((double) height,
					world);
			duration = (long) PluginTools.firebendingDayAugment(
					(double) duration, world);
			damage = (int) PluginTools.firebendingDayAugment((double) damage,
					world);
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

		if (Math.abs(direction.angle(compare)) > Math.toRadians(maxangle)) {
			return;
		}

		initializeBlocks();

		instances.put(player, this);
	}

	private boolean progress() {
		time = System.currentTimeMillis();

		if (time - starttime > cooldown) {
			return false;
		}

		if (!active)
			return true;

		if (time - starttime > duration) {
			active = false;
			return true;
		}

		if (time - starttime > intervaltick * interval) {
			intervaltick++;
			display();
		}

		if (time - starttime > damagetick * damageinterval) {
			damagetick++;
			damage();
		}
		return true;
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
		//TODO : Make it no longer pass through the walls
		for (double i = -w; i <= w; i++) {
			for (double j = -h; j <= h; j++) {
				Location location = origin.clone().add(
						orthoud.clone().multiply(j));
				location = location.add(ortholr.clone().multiply(i));
				if (PluginTools.isRegionProtectedFromBuild(player,
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
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(
				origin, radius);
		if (entities.contains(player)) {
			entities.remove(player);
		}

		for (LivingEntity entity : entities) {
			if (PluginTools.isRegionProtectedFromBuild(player, Abilities.WallOfFire,
					entity.getLocation())) {
				continue;
			}
			for (Block block : blocks) {
				if (entity.getLocation().distance(block.getLocation()) <= 1.5) {
					affect(entity);
					break;
				}
			}
		}
	}

	private void affect(LivingEntity entity) {
		entity.setFireTicks(50);
		entity.setVelocity(new Vector(0, 0, 0));
		EntityTools.damageEntity(player, entity, damage);
		new Enflamed(entity, player, this);

	}

	public static void progressAll() {
		List<WallOfFire> toRemove = new LinkedList<WallOfFire>();
		for (WallOfFire wall : instances.values()) {
			boolean keep = wall.progress();
			if (!keep) {
				toRemove.add(wall);
			}
		}
		for (WallOfFire wall : toRemove) {
			wall.remove();
		}
	}

	public static void removeAll() {
		instances.clear();
	}

	private void remove() {
		instances.remove(player);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Wall of Fire", element=BendingType.Fire)
public class WallOfFire implements IAbility {
	private static Map<Player, WallOfFire> instances = new HashMap<Player, WallOfFire>();

	private static double maxangle = 50;
	private static long interval = 250;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 4;
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 7500;
	
	@ConfigurationParameter("Interval")
	private static long DAMAGE_INTERVAL = 500;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 4;
	
	@ConfigurationParameter("Width")
	private static int WIDTH = 4;
	
	@ConfigurationParameter("Duration")
	private static long DURATION = 5000;
	
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 9;
	
	private Player player;
	private Location origin;
	private long time, starttime;
	private boolean active = true;
	private int damagetick = 0, intervaltick = 0;
	private List<Block> blocks = new LinkedList<Block>();
	private IAbility parent;
	
	private int damage;
	private int width;
	private int height;
	private long duration;

	public WallOfFire(Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player) && !AvatarState.isAvatarState(player)) {
			return;
		}

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.WallOfFire))
			return;

		this.player = player;

		origin = EntityTools.getTargetedLocation(player, RANGE);

		World world = player.getWorld();

		if (Tools.isDay(player.getWorld())) {
			width = (int) PluginTools.firebendingDayAugment((double) WIDTH,
					world);
			height = (int) PluginTools.firebendingDayAugment((double) HEIGHT,
					world);
			duration = (long) PluginTools.firebendingDayAugment(
					(double) DURATION, world);
			damage = (int) PluginTools.firebendingDayAugment((double) DAMAGE,
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

		if (time - starttime > COOLDOWN) {
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

		if (time - starttime > damagetick * DAMAGE_INTERVAL) {
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
				if (ProtectionManager.isRegionProtectedFromBending(player,
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
		if (radius < width) {
			radius = width;
		}
		radius = radius + 1;
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(
				origin, radius);
		if (entities.contains(player)) {
			entities.remove(player);
		}

		for (LivingEntity entity : entities) {
			if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.WallOfFire,
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
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
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
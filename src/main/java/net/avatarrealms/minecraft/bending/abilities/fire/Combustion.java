package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

public class Combustion implements IAbility {

	private static Map<Player, Combustion> instances = new HashMap<Player, Combustion>();
	//TODO : this variable seems to be never cleared of any of its content, strange
	private static Map<Entity, Combustion> explosions = new HashMap<Entity, Combustion>();

	private static long defaultchargetime = 2000;
	private static long interval = 25;
	private static double radius = 1.5;

	private double range = 20;
	private int maxdamage = 4;
	private double explosionradius = 6;
	private double innerradius = 3;
	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private long starttime;
	private long time;
	private long chargetime = defaultchargetime;
	private boolean charged = false;
	private boolean launched = false;
	private TNTPrimed explosion = null;
	private IAbility parent;

	public Combustion(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		time = System.currentTimeMillis();
		starttime = time;
		if (Tools.isDay(player.getWorld())) {
			chargetime = (long) (chargetime / ConfigManager.dayFactor);
		}
		if (AvatarState.isAvatarState(player)) {
			chargetime = 0;
			maxdamage = AvatarState.getValue(maxdamage);
		}
		range = PluginTools.firebendingDayAugment(range, player.getWorld());
		if (!player.getEyeLocation().getBlock().isLiquid()) {
			instances.put(player, this);
		}

	}

	private boolean progress() {
		if ((!EntityTools.canBend(player, Abilities.Combustion) 
				|| EntityTools.getBendingAbility(player) != Abilities.Combustion) && !launched) {
			return false;
		}

		if (System.currentTimeMillis() > starttime + chargetime) {
			charged = true;
		}

		if (!player.isSneaking() && !charged) {
			new FireBlast(player, this);
			return false;
		}

		if (!player.isSneaking() && !launched) {
			launched = true;
			location = player.getEyeLocation();
			origin = location.clone();
			direction = location.getDirection().normalize().multiply(radius);
		}

		if (System.currentTimeMillis() > time + interval) {
			if (launched)
				if (Tools.isRegionProtectedFromBuild(player, Abilities.Combustion,
						location)) {
					return false;
				}

			time = System.currentTimeMillis();

			if (!launched && !charged)
				return true;
			if (!launched) {
				player.getWorld().playEffect(player.getEyeLocation(),
						Effect.BLAZE_SHOOT, 0, 3);
				return true ;
			}

			location = location.clone().add(direction);
			if (location.distance(origin) > range) {
				return false;
			}

			if (BlockTools.isSolid(location.getBlock())) {
				explode();
				return false;
			} else if (location.getBlock().isLiquid()) {
				return false;
			}

			return fireball();
		}
		return true;
	}

	public static Combustion getFireball(Entity entity) {
		return explosions.get(entity);
	}

	public void dealDamage(Entity entity) {
		if (explosion == null)
			return;
		// if (Tools.isObstructed(explosion.getLocation(),
		// entity.getLocation())) {
		// return 0;
		// }
		double distance = entity.getLocation()
				.distance(explosion.getLocation());
		if (distance > explosionradius)
			return;
		if (distance < innerradius) {
			EntityTools.damageEntity(player, entity, maxdamage);
			return;
		}
		double slope = -(maxdamage * .5) / (explosionradius - innerradius);

		double damage = slope * (distance - innerradius) + maxdamage;
		EntityTools.damageEntity(player, entity, (int) damage);
	}

	private boolean fireball() {
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			block.getWorld().playEffect(block.getLocation(),
					Effect.MOBSPAWNER_FLAMES, 0, 20);
		}

		for (Entity entity : EntityTools.getEntitiesAroundPoint(location, 2 * radius)) {
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			entity.setFireTicks(120);
			if (entity instanceof LivingEntity) {
				explode();
				return false;
			}
		}
		return true;
	}

	public static boolean isCharging(Player player) {
		for (Combustion ball : instances.values()) {
			if (ball.player == player && !ball.launched)
				return true;
		}
		return false;
	}

	private void explode() {
		// List<Block> blocks = Tools.getBlocksAroundPoint(location, 3);
		// List<Block> blocks2 = new ArrayList<Block>();

		// Tools.verbose("Fireball Explode!");
		boolean explode = true;
		for (Block block : BlockTools.getBlocksAroundPoint(location, 3)) {
			if (Tools.isRegionProtectedFromBuild(player, Abilities.Combustion,
					block.getLocation())) {
				explode = false;
				break;
			}
		}
		if (explode) {
			explosion = player.getWorld().spawn(location, TNTPrimed.class);
			explosion.setFuseTicks(0);
			float yield = 1;
			switch (player.getWorld().getDifficulty()) {
			case PEACEFUL:
				yield *= 2.;
				break;
			case EASY:
				yield *= 2.;
				break;
			case NORMAL:
				yield *= 1.;
				break;
			case HARD:
				yield *= 3. / 4.;
				break;
			}
			explosion.setYield(yield);
			explosions.put(explosion, this);
		}
		// location.getWorld().createExplosion(location, 1);
		ignite(location);
	}

	private void ignite(Location location) {
		for (Block block : BlockTools.getBlocksAroundPoint(location,
				FireBlast.affectingradius)) {
			if (FireStream.isIgnitable(player, block)) {
				block.setType(Material.FIRE);
				if (FireBlast.dissipate) {
					FireStream.addIgnitedBlock(block, player, System.currentTimeMillis());
				}
			}
		}
	}

	public static void progressAll() {
		List<Combustion> toRemove = new LinkedList<Combustion>();
		for (Combustion fireball : instances.values()) {
			boolean keep = fireball.progress();
			if(!keep) {
				toRemove.add(fireball);
			}
		}
		for(Combustion fireball : toRemove) {
			fireball.remove();
		}
	}

	public void remove() {
		instances.remove(this.player);
	}

	public static void removeAll() {
		instances.clear();
	}

	public static void removeFireballsAroundPoint(Location location,
			double radius) {
		List<Combustion> toRemove = new LinkedList<Combustion>();
		for (Combustion fireball : instances.values()) {
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					toRemove.add(fireball);
			}
		}
		for(Combustion fireball : toRemove) {
			fireball.remove();
		}
	}

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		boolean broke = false;
		List<Combustion> toRemove = new LinkedList<Combustion>();
		for (Combustion fireball : instances.values()) {
			if (!fireball.launched)
				continue;
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()
					&& !source.equals(fireball.player)) {
				if (location.distance(fireblastlocation) <= radius) {
					fireball.explode();
					toRemove.add(fireball);
					broke = true;
				}
			}
		}
		
		for(Combustion fireball : toRemove) {
			fireball.remove();
		}

		return broke;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

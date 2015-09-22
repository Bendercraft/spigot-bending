package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name = "Combustion", bind = BendingAbilities.Combustion, element = BendingElement.Fire, affinity = BendingAffinity.Combustion)
public class Combustion extends BendingActiveAbility {
	private static long interval = 25;

	@ConfigurationParameter("Radius")
	private static double radius = 2;

	@ConfigurationParameter("Charge-Time")
	private static long chargeTime = 2000;

	@ConfigurationParameter("Explosion-Radius")
	private static double explosionradius = 3.0;

	@ConfigurationParameter("Inner-Radius")
	private static double innerradius = 3.0;

	@ConfigurationParameter("Sound-Radius")
	private static int SOUND_RADIUS = 35;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 9;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 2000;

	@ConfigurationParameter("Max-Move")
	private static float MAX_DISTANCE = 2.5f;

	private static final ParticleEffect CRIT = ParticleEffect.CRIT;
	private static final ParticleEffect EXPLODE = ParticleEffect.EXPLOSION_HUGE;

	private Location origin;
	private Block block;
	private Location location;
	private Vector direction;
	private long time;
	private int progressed = 0;

	private double range = RANGE;
	private double damage = DAMAGE;

	public Combustion(Player player) {
		super(player, null);

		if (state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		time = startedTime;
		if (AvatarState.isAvatarState(player)) {
			damage = AvatarState.getValue(damage);
		}
		range = PluginTools.firebendingDayAugment(range, player.getWorld());
		block = player.getLocation().getBlock();
	}

	@Override
	public boolean sneak() {
		switch (state) {
		case None:
		case CannotStart:
			return false;
		case CanStart:
			if (!player.getEyeLocation().getBlock().isLiquid()) {
				setState(BendingAbilityState.Preparing);
				AbilityManager.getManager().addInstance(this);
			}
			return false;
		case Preparing:
		case Prepared:
		case Progressing:
		case Ending:
		case Ended:
		case Removed:
		default:
			return false;
		}
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if ((EntityTools.getBendingAbility(player) != BendingAbilities.Combustion)) {
			return false;
		}

		if (state == BendingAbilityState.Preparing) {
			if (!player.isSneaking()) {
				return false;
			}

			if (player.getLocation().getBlock().getLocation().distance(block.getLocation()) > MAX_DISTANCE) {
				return false;
			}

			if (System.currentTimeMillis() > time + chargeTime) {
				location = player.getEyeLocation();
				origin = location.clone();
				direction = location.getDirection().normalize().multiply(radius);
				setState(BendingAbilityState.Prepared);
			}
			return true;
		}

		if (System.currentTimeMillis() > time + interval) {
			if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.Combustion, location)) {
				return false;
			}

			time = System.currentTimeMillis();

			location = location.clone().add(direction);
			if (location.distance(origin) > range) {
				explode();
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

	public void dealDamage(Entity entity) {
		double distance = entity.getLocation().distance(location);
		if (distance > explosionradius) {
			return;
		}
		if (distance < innerradius) {
			EntityTools.damageEntity(player, entity, damage);
			return;
		}
		double slope = -(damage * .5) / (explosionradius - innerradius);

		double damage = slope * (distance - innerradius) + this.damage;
		EntityTools.damageEntity(player, entity, (int) damage);
	}

	private void knockBack(Entity entity) {
		double distance = entity.getLocation().distance(location);
		if (distance > explosionradius) {
			return;
		}
		double dx = entity.getLocation().getX() - location.getX();
		double dy = entity.getLocation().getY() - location.getY();
		double dz = entity.getLocation().getZ() - location.getZ();
		Vector v = new Vector(dx, dy, dz);
		v = v.normalize();

		v.multiply(distance);

		entity.setVelocity(v);
	}

	private boolean fireball() {
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 0, 1);
		}
		CRIT.display(0, 0, 0, 1, 1, location, 20);
		if (progressed % 5 == 0) {
			location.getWorld().playSound(location, Sound.SHOOT_ARROW, 5, 1);
		}
		progressed++;
		for (Entity entity : EntityTools.getEntitiesAroundPoint(location, 2 * radius)) {
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			if (entity instanceof LivingEntity) {
				explode();
				return false;
			}
		}
		return true;
	}

	private void explode() {
		boolean obsidian = false;

		List<Block> affecteds = new LinkedList<Block>();
		for (Block block : BlockTools.getBlocksAroundPoint(location, explosionradius)) {
			if (block.getType() == Material.OBSIDIAN) {
				obsidian = true;
			}
			if (!obsidian || (obsidian && location.distance(block.getLocation()) < explosionradius / 2.0)) {
				if (!ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.Combustion, block.getLocation()) && !ProtectionManager.isRegionProtectedFromExplosion(player, BendingAbilities.Combustion, block.getLocation())) {
					affecteds.add(block);
				}
			}
		}
		for (Block block : affecteds) {
			if (!block.getType().equals(Material.BEDROCK)) {
				if (!obsidian || location.distance(block.getLocation()) < 2.0) {
					List<Block> adjacent = new LinkedList<Block>();
					adjacent.add(block.getRelative(BlockFace.NORTH));
					adjacent.add(block.getRelative(BlockFace.SOUTH));
					adjacent.add(block.getRelative(BlockFace.EAST));
					adjacent.add(block.getRelative(BlockFace.WEST));
					adjacent.add(block.getRelative(BlockFace.UP));
					adjacent.add(block.getRelative(BlockFace.DOWN));

					if (affecteds.containsAll(adjacent)) {
						// Explosion ok
						this.removeBlock(block);
					} else {
						double rand = Math.random();
						if (rand < 0.8) {
							this.removeBlock(block);
						}
					}
				}
			}
		}
		location.getWorld().playSound(location, Sound.EXPLODE, SOUND_RADIUS / 16.0f, 1);
		EXPLODE.display(0, 0, 0, 1, 1, location, 20);
		double radius = explosionradius;
		if (obsidian) {
			radius = explosionradius / 2.0;
		}
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, radius);
		for (LivingEntity entity : entities) {
			if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			this.dealDamage(entity);
			this.knockBack(entity);
		}
	}

	@SuppressWarnings("deprecation")
	private void removeBlock(Block block) {
		if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
			CoreProtectAPI cp = CoreProtectAPI.plugin.getAPI();
			cp.logRemoval(player.getName(), block.getLocation(), block.getType(), block.getData());
		}
		block.getDrops().clear();
		block.breakNaturally();
	}

	@Override
	public void remove() {
		BendingPlayer.getBendingPlayer(player).cooldown(BendingAbilities.Combustion, COOLDOWN);
		super.remove();
	}

	public static void removeFireballsAroundPoint(Location location, double radius) {
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.Combustion);
		if (instances == null) {
			return;
		}
		for (IBendingAbility ab : instances.values()) {
			Combustion fireball = ((Combustion) ab);
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius) {
					fireball.consume();
				}
			}
		}
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.Combustion);
		if (instances == null) {
			return broke;
		}
		for (IBendingAbility ab : instances.values()) {
			Combustion fireball = ((Combustion) ab);
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld() && !source.equals(fireball.player)) {
				if (location.distance(fireblastlocation) <= radius) {
					fireball.explode();
					fireball.consume();
					broke = true;
				}
			}
		}

		return broke;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	protected long getMaxMillis() {
		return 1000 * 90;
	}

}

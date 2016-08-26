package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@ABendingAbility(name = Combustion.NAME, affinity = BendingAffinity.COMBUSTION, canBeUsedWithTools = true)
public class Combustion extends BendingActiveAbility {
	public final static String NAME = "Combustion";
	
	private static long interval = 25;

	@ConfigurationParameter("Radius")
	private static double HITBOX = 2;

	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 2000;
	
	@ConfigurationParameter("Explosion-Radius")
	private static double EXPLOSION_RADIUS = 5.0;

	@ConfigurationParameter("Inner-Radius")
	private static double DAMAGE_RADIUS = 5.0;

	@ConfigurationParameter("Sound-Radius")
	private static int SOUND_RADIUS = 35;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;
	
	@ConfigurationParameter("Push-back")
	private static double PUSH_BACK = 5.0;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 9;

	@ConfigurationParameter("Power")
	public static int POWER = 10;

	private static final Particle CRIT = Particle.CRIT;
	private static final Particle EXPLODE = Particle.EXPLOSION_HUGE;

	private Location origin;
	private Location location;
	private Vector direction;
	private long time;
	private int progressed = 0;

	private long chargeTime = CHARGE_TIME;
	private double range = RANGE;
	private double damage = DAMAGE;
	
	private boolean destruction = false; 

	public Combustion(RegisteredAbility register, Player player) {
		super(register, player);

		time = startedTime;
		if (AvatarState.isAvatarState(player)) {
			damage = AvatarState.getValue(damage);
			chargeTime *= 0.60;
		}
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, POWER)) {
			return false;
		}

		return true;
	}
	
	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			if (!player.getEyeLocation().getBlock().isLiquid() && bender.fire.can(NAME, POWER)) {
				destruction = false;
				bender.fire.consume(NAME, POWER);
				damage *= 0.5;
				launch();
				setState(BendingAbilityState.PREPARED);
			}
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			if (!player.getEyeLocation().getBlock().isLiquid()) {
				destruction = true;
				setState(BendingAbilityState.PREPARING);
			}
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!NAME.equals(EntityTools.getBendingAbility(player))) {
			return false;
		}
		return true;
	}
	
	private void launch() {
		location = player.getEyeLocation();
		origin = location.clone();
		direction = location.getDirection().normalize().multiply(HITBOX);
	}

	@Override
	public void progress() {
		if (getState() == BendingAbilityState.PREPARING) {
			if (!player.isSneaking()) {
				remove();
				return;
			}
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.SPELL, loc, 1, 0, 0, 0, 0);
			if (System.currentTimeMillis() > time + chargeTime) {
				launch();
				setState(BendingAbilityState.PREPARED);
			}
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
				remove();
				return;
			}

			time = System.currentTimeMillis();

			location = location.clone().add(direction);
			if (location.distance(origin) > range) {
				explode();
				remove();
				return;
			}
			if (BlockTools.isSolid(location.getBlock()) && !BlockTools.isPlant(location.getBlock())) {
				explode();
				remove();
				return;
			} else if (location.getBlock().isLiquid()) {
				remove();
				return;
			}

			if(!fireball()) {
				remove();
				return;
			}
		}
	}

	public void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		
		double distance = entity.getLocation().distance(location);
		if (distance > EXPLOSION_RADIUS) {
			return;
		}
		double dx = entity.getLocation().getX() - location.getX();
		double dy = entity.getLocation().getY() - location.getY();
		double dz = entity.getLocation().getZ() - location.getZ();
		Vector v = new Vector(dx, dy, dz);
		v = v.normalize();

		v = v.multiply(PUSH_BACK);

		entity.setVelocity(v);
		
		if (distance < DAMAGE_RADIUS) {
			DamageTools.damageEntity(bender, entity, this, damage);
		} else {
			double slope = -(damage * .5) / (EXPLOSION_RADIUS - DAMAGE_RADIUS);
			double damage = slope * (distance - DAMAGE_RADIUS) + this.damage;
			DamageTools.damageEntity(bender, entity, this, damage);
		}
	}

	private boolean fireball() {
		for (Block block : BlockTools.getBlocksAroundPoint(location, HITBOX)) {
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 0, 1);
		}
		location.getWorld().spawnParticle(CRIT, location, 1, 0, 0, 0);
		if (progressed % 5 == 0) {
			location.getWorld().playSound(location, Sound.ENTITY_ARROW_SHOOT, 5, 1);
		}
		progressed++;
		for (Entity entity : EntityTools.getEntitiesAroundPoint(location, 2 * HITBOX)) {
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

		if(destruction) {
			List<Block> affecteds = new LinkedList<Block>();
			for (Block block : BlockTools.getBlocksAroundPoint(location, EXPLOSION_RADIUS)) {
				if (block.getType() == Material.OBSIDIAN) {
					obsidian = true;
				}
				if (!obsidian || (obsidian && location.distance(block.getLocation()) < EXPLOSION_RADIUS / 2.0)) {
					if (!ProtectionManager.isLocationProtectedFromBending(player, register, block.getLocation())
							&& !ProtectionManager.isLocationProtectedFromExplosion(player, NAME, block.getLocation())) {
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
							removeBlock(block);
						} else {
							double rand = Math.random();
							if (rand < 0.8) {
								removeBlock(block);
							}
						}
					}
				}
			}
		}
		location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SOUND_RADIUS / 16.0f, 1);
		location.getWorld().spawnParticle(EXPLODE, location, 1, 0, 0, 0);
		
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, EXPLOSION_RADIUS);
		for (LivingEntity entity : entities) {
			affect(entity);
		}
	}

	@SuppressWarnings("deprecation")
	private void removeBlock(Block block) {
		if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
			CoreProtectAPI cp = CoreProtect.getInstance().getAPI();
			cp.logRemoval(player.getName(), block.getLocation(), block.getType(), block.getData());
		}
		block.getDrops().clear();
		block.breakNaturally();
	}

	@Override
	public void stop() {
		
	}

	public static void removeFireballsAroundPoint(Location location, double radius) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return;
		}
		for (BendingAbility ab : instances.values()) {
			Combustion fireball = ((Combustion) ab);
			Location fireblastlocation = fireball.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius) {
					fireball.remove();
				}
			}
		}
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return broke;
		}
		for (BendingAbility ab : instances.values()) {
			Combustion fireball = ((Combustion) ab);
			Location fireblastLocation = fireball.location;
			if (location.getWorld() == fireblastLocation.getWorld() && !source.equals(fireball.player)) {
				if (location.distance(fireblastLocation) <= radius) {
					fireball.explode();
					fireball.remove();
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

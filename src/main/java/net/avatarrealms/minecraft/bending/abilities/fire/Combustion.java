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
import net.avatarrealms.minecraft.bending.utils.ParticleEffect;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

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

public class Combustion implements IAbility {
	private static Map<Player, Combustion> instances = new HashMap<Player, Combustion>();

	private static long interval = 25;
	private static double radius = ConfigManager.combustionRadius;
	private static long chargeTime = ConfigManager.combustionChargeTime;
	private static double explosionradius = ConfigManager.combustionExplosionRadius;
	private static double innerradius = ConfigManager.combustionInnerRadius;
	
	private static final ParticleEffect CRIT = ParticleEffect.CRIT;
	private static final ParticleEffect EXPLODE = ParticleEffect.HUGE_EXPLOSION;

	private double range = ConfigManager.combustionRange;
	private int maxdamage = ConfigManager.combustionDamage;
	
	private Player player;
	private Location origin;
	private Block block;
	private Location location;
	private Vector direction;
	private long time;
	private IAbility parent;
	private boolean charged = false;
	private int progressed = 0;

	public Combustion(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		time = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player)) {
			maxdamage = AvatarState.getValue(maxdamage);
		}
		range = PluginTools.firebendingDayAugment(range, player.getWorld());
		
		block = player.getLocation().getBlock();
		
		if (!player.getEyeLocation().getBlock().isLiquid()) {
			instances.put(player, this);
		}
		BendingPlayer.getBendingPlayer(player).cooldown(Abilities.Combustion);
	}

	private boolean progress() {
		if ((!EntityTools.canBend(player, Abilities.Combustion) 
				|| EntityTools.getBendingAbility(player) != Abilities.Combustion)) {
			return false;
		}
		
		if(!charged) {
			if(!player.getLocation().getBlock().getLocation().equals(block.getLocation())) {
				return false;
			}
			if(!player.isSneaking()) {
				return false;
			}
			if (System.currentTimeMillis() > time + chargeTime) {
				location = player.getEyeLocation();
				origin = location.clone();
				direction = location.getDirection().normalize().multiply(radius);
				charged = true;
			}
			return true;
		}

		if (System.currentTimeMillis() > time + interval) {
			if (Tools.isRegionProtectedFromBuild(player, Abilities.Combustion,
						location)) {
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
		double distance = entity.getLocation()
				.distance(location);
		if (distance > explosionradius){
			return;
		}	
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
					Effect.SMOKE, 0, 1);
		}
		CRIT.display(location, 0, 0, 0, 1, 3);
		if(progressed % 5 == 0) {
			location.getWorld().playSound(location, Sound.SHOOT_ARROW, 1, 0);
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
		boolean explode = true;
		List<Block> affecteds = BlockTools.getBlocksAroundPoint(location, explosionradius);
		for (Block block : affecteds) {
			if (Tools.isRegionProtectedFromBuild(player, Abilities.Combustion,
					block.getLocation())) {
				explode = false;
				break;
			}
		}
		if (explode) {
			for (Block block : affecteds) {
				if(!block.getType().equals(Material.OBSIDIAN) && !block.getType().equals(Material.BEDROCK)) {
					List<Block> adjacent = new LinkedList<Block>();
					adjacent.add(block.getRelative(BlockFace.NORTH));
					adjacent.add(block.getRelative(BlockFace.SOUTH));
					adjacent.add(block.getRelative(BlockFace.EAST));
					adjacent.add(block.getRelative(BlockFace.WEST));
					adjacent.add(block.getRelative(BlockFace.UP));
					adjacent.add(block.getRelative(BlockFace.DOWN));
					
					if(affecteds.containsAll(adjacent)) {
						//Explosion ok
						block.setType(Material.AIR);
					} else {
						double rand = Math.random();
						if(rand < 0.8) {
							block.setType(Material.AIR);
						}
					}
				}
			}
			location.getWorld().playSound(location, Sound.EXPLODE, 1, 0);
			EXPLODE.display(location, 0, 0, 0, 1, 1);
			List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, explosionradius);
			for(LivingEntity entity : entities) {
				this.dealDamage(entity);
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

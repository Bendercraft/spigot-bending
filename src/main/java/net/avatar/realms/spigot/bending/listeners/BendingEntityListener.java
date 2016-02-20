package net.avatar.realms.spigot.bending.listeners;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.projectiles.ProjectileSource;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.arts.C4;
import net.avatar.realms.spigot.bending.abilities.arts.DirectHit;
import net.avatar.realms.spigot.bending.abilities.fire.Enflamed;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.PhaseChange;
import net.avatar.realms.spigot.bending.abilities.water.WaterWall;
import net.avatar.realms.spigot.bending.abilities.water.Wave;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.MathUtils;
import net.avatar.realms.spigot.bending.utils.TempBlock;

public class BendingEntityListener implements Listener {

	public Bending plugin;

	public BendingEntityListener(Bending bending) {
		this.plugin = bending;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (FireStream.isIgnited(block) && (entity instanceof LivingEntity)) {
			Enflamed.enflame(FireStream.getIgnited(block), entity, 2);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if ((event.getCause() == DamageCause.FIRE) && FireStream.isIgnited(entity.getLocation().getBlock())) {
			Enflamed.enflame(FireStream.getIgnited(entity.getLocation().getBlock()), entity, 2);
		}
		if (Enflamed.isEnflamed(entity) && (event.getCause() == DamageCause.FIRE_TICK)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {

		Entity source = event.getDamager();
		Entity entity = event.getEntity();
		Lightning lightning = Lightning.getLightning(source);

		if (event.getCause() == DamageCause.LIGHTNING) {
			if (Lightning.isNearbyChannel(source.getLocation())) {
				event.setCancelled(true);
				return;
			}
		}

		if (lightning != null) {
			event.setCancelled(true);
			lightning.dealDamage(entity);
			return;
		}

		if ((source instanceof Player) && (entity instanceof Player)) {
			Player sourceplayer = (Player) source;
			Player targetplayer = (Player) entity;
			if (EntityTools.canBendPassive(sourceplayer, BendingElement.Master)
					&& EntityTools.isBender(sourceplayer, BendingElement.Master)
					&& (event.getCause() == DamageCause.ENTITY_ATTACK) && MathUtils.doubleEquals(event.getDamage(), 1)
					&& (sourceplayer.getLocation().distance(targetplayer.getLocation()) <= DirectHit.RANGE)) {
				EntityTools.blockChi(targetplayer, 500);
			}
		}
		if (entity instanceof Player) {
			if (((event.getCause() == DamageCause.ENTITY_ATTACK) || (event.getCause() == DamageCause.ENTITY_EXPLOSION) || (event.getCause() == DamageCause.PROJECTILE)) && EntityTools.isBender(((Player) event.getEntity()), BendingElement.Master) && EntityTools.canBendPassive((Player) event.getEntity(), BendingElement.Master)) {
				double rand = Math.random();

				if (rand <= (Settings.CHI_DODGE_CHANCE / 100.)) {
					event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.SMOKE, 1);
					event.setCancelled(true);
				}
			}
		}
		if (source instanceof Player) {
			if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) source);
				if ((bPlayer.getAbility() != null) && bPlayer.getAbility().equals(BendingAbilities.FireBlade)) {
					// 20ticks per seconds
					entity.setFireTicks(20 * 3);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (PhaseChange.isFrozen(block)) {
				PhaseChange.thawThenRemove(block);
			}

			if (WaterWall.isWaterWallPart(block)) {
				block.setType(Material.AIR);
			}
			if (!Wave.canThaw(block)) {
				Wave.thaw(block);
			}
			if (TempBlock.isTempBlock(block)) {
				TempBlock.get(block).revertBlock();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(EntityTargetLivingEntityEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if (entity != null) {
			if (Bloodbending.isBloodbended(entity)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent(EntityInteractEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(EntityTeleportEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent(ProjectileLaunchEvent event) {
		Projectile pr = event.getEntity();
		Entity entity = null;
		if (pr.getShooter() instanceof LivingEntity) {
			entity = (LivingEntity) pr.getShooter();
		}
		if (entity == null) {
			return;
		}
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (pr instanceof Arrow) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
				if (bPlayer.isBender(BendingElement.Fire)) {
					if (p.isSneaking() && (bPlayer.getAbility() == BendingAbilities.HeatControl)) {
						pr.setFireTicks(200);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile ent = event.getEntity();
		if (ent instanceof Arrow) {
			Arrow arrow = (Arrow) ent;
			ProjectileSource shooter = arrow.getShooter();
			if (shooter instanceof Player) {
				Player player = (Player) shooter;
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer == null) {
					return;
				}
				BendingAbilities ability = bPlayer.getAbility();
				if ((ability == BendingAbilities.PlasticBomb) && EntityTools.canBend(player, ability)) {
					C4 bomb = new C4(player, arrow);
					bomb.swing();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent(SlimeSplitEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}
}

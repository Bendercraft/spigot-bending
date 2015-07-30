package net.avatar.realms.spigot.bending.listeners;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.chi.C4;
import net.avatar.realms.spigot.bending.abilities.chi.Paralyze;
import net.avatar.realms.spigot.bending.abilities.chi.RapidPunch;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.fire.Enflamed;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.FreezeMelt;
import net.avatar.realms.spigot.bending.abilities.water.WaterWall;
import net.avatar.realms.spigot.bending.abilities.water.Wave;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.util.BlockIterator;


public class BendingEntityListener implements Listener {

	public Bending plugin;

	public BendingEntityListener (Bending bending) {
		this.plugin = bending;
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust (EntityCombustEvent event) {
		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (FireStream.isIgnited(block) && (entity instanceof LivingEntity)) {
			// TODO parent is FireStream !
			new Enflamed(entity, FireStream.getIgnited(block), null);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent (EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if ((event.getCause() == DamageCause.FIRE) && FireStream.isIgnited(entity.getLocation().getBlock())) {
			// TODO parent is FireStream
			new Enflamed(entity, FireStream.getIgnited(entity.getLocation().getBlock()), null);
		}
		if (Enflamed.isEnflamed(entity) && (event.getCause() == DamageCause.FIRE_TICK)) {
			event.setCancelled(true);
			Enflamed.dealFlameDamage(entity);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamage (EntityDamageByEntityEvent event) {

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

		if (Paralyze.isParalyzed(event.getDamager())) {
			event.setCancelled(true);
			return;
		}

		boolean dodged = false;

		if ((source instanceof Player) && (entity instanceof Player)) {
			Player sourceplayer = (Player)source;
			Player targetplayer = (Player)entity;
			if (EntityTools.canBendPassive(sourceplayer, BendingType.ChiBlocker)
					&& EntityTools.isBender(sourceplayer, BendingType.ChiBlocker)
					&& (event.getCause() == DamageCause.ENTITY_ATTACK)
					&& (event.getDamage() == 1)
					&& (sourceplayer.getLocation().distance(targetplayer.getLocation()) <= RapidPunch.RANGE)) {
				EntityTools.blockChi(targetplayer, System.currentTimeMillis());
			}
		}
		if (entity instanceof Player) {
			if (((event.getCause() == DamageCause.ENTITY_ATTACK) || (event.getCause() == DamageCause.ENTITY_EXPLOSION) || (event
					.getCause() == DamageCause.PROJECTILE))
					&& EntityTools.isBender(((Player)event.getEntity()), BendingType.ChiBlocker)
					&& EntityTools.canBendPassive((Player)event.getEntity(), BendingType.ChiBlocker)) {
				double rand = Math.random();

				if ((rand <= (Settings.CHI_DODGE_CHANCE / 100.)) && !Paralyze.isParalyzed(event.getEntity())) {
					event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.SMOKE, 1);
					dodged = true;
					event.setCancelled(true);
				}
			}
		}
		if (source instanceof Player) {
			if (!dodged) {
				new Paralyze((Player)source, event.getEntity(), null);
			}
			if (EntityTools.isBender(((Player)event.getDamager()), BendingType.ChiBlocker)
					&& (event.getCause() == DamageCause.ENTITY_ATTACK)
					&& !EntityTools.isWeapon(((Player)event.getDamager()).getItemInHand().getType())) {
			}

			if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player)source);
				if ((bPlayer.getAbility() != null) && bPlayer.getAbility().equals(Abilities.FireBlade)) {
					// 20ticks per seconds
					entity.setFireTicks(20 * 3);
				}
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode (EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			EarthBlast blast = EarthBlast.getBlastFromSource(block);

			if (blast != null) {
				blast.cancel();
			}
			if (FreezeMelt.isFrozen(block)) {
				FreezeMelt.thawThenRemove(block);
			}

			if (WaterWall.isWaterWallPart(block)) {
				block.setType(Material.AIR);
			}
			if (!Wave.canThaw(block)) {
				Wave.thaw(block);
			}
			if (BlockTools.bendedBlocks.containsKey(block)) {
				BlockTools.removeRevertIndex(block);
			}
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTarget (EntityTargetEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving (EntityTargetLivingEntityEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlock (EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplodeEvent (EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if (entity != null) {
			if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent (EntityInteractEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent (EntityShootBowEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent (EntityTeleportEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent (ProjectileLaunchEvent event) {
		Projectile pr = event.getEntity();
		Entity entity = null;
		if (pr.getShooter() instanceof LivingEntity) {
			entity = (LivingEntity)pr.getShooter();
		}
		if (entity == null) {
			return;
		}
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
		if (entity instanceof Player) {
			Player p = (Player)entity;
			if (pr instanceof Arrow) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
				if (bPlayer.isBender(BendingType.Fire)) {
					if (p.isSneaking() && (bPlayer.getAbility() == Abilities.HeatControl)) {
						pr.setFireTicks(200);
					}
				}
			}
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onProjectileHit (ProjectileHitEvent event) {
		Projectile ent = event.getEntity();
		if (ent instanceof Arrow) {
			Arrow arrow = (Arrow)ent;
			ProjectileSource shooter = arrow.getShooter();
			if (shooter instanceof Player) {
				Player player = (Player)shooter;
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer == null) {
					return;
				}
				Abilities ability = bPlayer.getAbility();
				if ((ability == Abilities.PlasticBomb) && EntityTools.canBend(player, ability)) {
					for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(arrow.getLocation(), 1.7)) {
						if (entity instanceof Player) {
							new C4(player, (Player)entity);
							return;
						}
					}
					World world = arrow.getLocation().getWorld();
					Block block = arrow.getLocation().getBlock();
					BlockIterator bi = null;
					Block hitBlock = null;
					if (block.getType() == Material.AIR) {
						bi = new BlockIterator(world, arrow.getLocation().toVector(), arrow.getVelocity().normalize(), 0, 1);
						if (bi.hasNext()) {
							hitBlock = bi.next();
						}
					}
					else {
						bi = new BlockIterator(world, arrow.getLocation().toVector(), arrow.getVelocity().multiply(-1)
								.normalize(), 0, 1);
						if (bi.hasNext()) {
							hitBlock = block;
							block = bi.next();
						}
					}
					if (hitBlock == null) {
						return;
					}

					new C4(player, block, hitBlock.getFace(block));
				}
			}
		}
	}

	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent (SlimeSplitEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}
}

package net.bendercraft.spigot.bending.listeners;

import java.util.logging.Level;

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

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.arts.C4;
import net.bendercraft.spigot.bending.abilities.arts.Concussion;
import net.bendercraft.spigot.bending.abilities.arts.ExplosiveShot;
import net.bendercraft.spigot.bending.abilities.earth.EarthArmor;
import net.bendercraft.spigot.bending.abilities.earth.EarthGrab;
import net.bendercraft.spigot.bending.abilities.fire.Enflamed;
import net.bendercraft.spigot.bending.abilities.fire.FireBlade;
import net.bendercraft.spigot.bending.abilities.fire.HeatControl;
import net.bendercraft.spigot.bending.abilities.water.Bloodbending;
import net.bendercraft.spigot.bending.abilities.water.PhaseChange;
import net.bendercraft.spigot.bending.abilities.water.Wave;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

public class BendingEntityListener implements Listener {

	public Bending plugin;

	public BendingEntityListener(Bending bending) {
		this.plugin = bending;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (Enflamed.isEnflamed(entity) && (event.getCause() == DamageCause.FIRE_TICK)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		
		if(entity instanceof LivingEntity) {
			Concussion concussion = Concussion.getTarget((LivingEntity) entity);
			if(concussion != null) {
				concussion.remove();
			}
		}
		
		
		// FireBlade should not deal any damage at all
		if(event.getCause() == DamageCause.ENTITY_ATTACK 
				&& event.getDamager() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			if(attacker.getInventory().getItemInMainHand().getType() == Material.GOLDEN_SWORD) {
				BendingPlayer bender = BendingPlayer.getBendingPlayer(attacker);
				if(bender != null 
						&& FireBlade.isFireBlading(attacker) 
						&& FireBlade.isFireBlade(attacker.getInventory().getItemInMainHand())) {
					event.setDamage(0);
					event.setCancelled(true);
					FireBlade.getFireBlading(attacker).affect(entity);
				}
			}
		}
		
		// Reduce all damage coming from BOW if not BOWMAN
		if(event.getCause() == DamageCause.PROJECTILE
			&& event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if(arrow.getShooter() instanceof Player) {
				Player shooter = (Player) arrow.getShooter();
				BendingPlayer bender = BendingPlayer.getBendingPlayer(shooter);
				if(bender == null || !bender.hasAffinity(BendingAffinity.BOW)) {
					event.setDamage(event.getDamage()*0.5);
				}
			}
		}
		
		// Reduce all damage coming from SWORD if not SWORDMAN
		if(event.getCause() == DamageCause.ENTITY_ATTACK
			&& event.getDamager() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			if(attacker.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SWORD
					|| attacker.getInventory().getItemInMainHand().getType() == Material.GOLDEN_SWORD
					|| attacker.getInventory().getItemInMainHand().getType() == Material.IRON_SWORD
					|| attacker.getInventory().getItemInMainHand().getType() == Material.STONE_SWORD
					|| attacker.getInventory().getItemInMainHand().getType() == Material.WOODEN_SWORD) {
				BendingPlayer bender = BendingPlayer.getBendingPlayer(attacker);
				if(bender == null || !bender.hasAffinity(BendingAffinity.SWORD)) {
					event.setDamage(event.getDamage()*0.5);
				}
			}
		}
		
		// Reduce all damage coming from diamond TOOLS
		if(event.getCause() == DamageCause.ENTITY_ATTACK
			&& event.getDamager() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			if(attacker.getInventory().getItemInMainHand().getType() == Material.DIAMOND_AXE
					|| attacker.getInventory().getItemInMainHand().getType() == Material.DIAMOND_HOE
					|| attacker.getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE
					|| attacker.getInventory().getItemInMainHand().getType() == Material.DIAMOND_SHOVEL) {
				event.setDamage(event.getDamage()*0.3);
			}
		}
		
		// Check damage cancellation if EarthArmor is on victim
		if(entity instanceof Player && EarthArmor.hasEarthArmor((Player) entity)) {
			EarthArmor ea = (EarthArmor) AbilityManager.getManager().getInstances(EarthArmor.NAME).get(entity);
			if(ea.shouldCancelDamage()) {
				event.setDamage(0);
			}
		}
		
		// Reduce damage if EarthArmor is on attacker
		if(event.getDamager() instanceof Player && EarthArmor.hasEarthArmor((Player) event.getDamager())) {
			EarthArmor ea = (EarthArmor) AbilityManager.getManager().getInstances(EarthArmor.NAME).get(event.getDamager());
			event.setDamage(event.getDamage() * (1-ea.getDamageReduction()));
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			if (PhaseChange.isFrozen(block)) {
				PhaseChange.thawThenRemove(block);
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
		if(EarthGrab.isGrabbed(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(EntityTargetLivingEntityEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
		if(EarthGrab.isGrabbed(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
		if(EarthGrab.isGrabbed(entity)) {
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
			if(EarthGrab.isGrabbed(entity)) {
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
		if(EarthGrab.isGrabbed(entity)) {
			event.setCancelled(true);
		}
		if(entity instanceof LivingEntity && Concussion.getTarget((LivingEntity) entity) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
		if(EarthGrab.isGrabbed(entity)) {
			event.setCancelled(true);
		}
		
		// ExplosiveShot
		if(!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		String ability = EntityTools.getBendingAbility(player);
		if (ability == null || !ability.equals(ExplosiveShot.NAME)) {
			return;
		}

		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ExplosiveShot.NAME);
		if (EntityTools.canBend(player, register)) {
			ExplosiveShot ab = (ExplosiveShot) AbilityManager.getManager().buildAbility(register, player);
			if(ab == null) {
				Bending.getInstance().getLogger().log(Level.SEVERE, "Ability " + ability + " failed to construct with buildAbility for player " + player.getName());
				return;
			}
			if(ab.canBeInitialized()) {
				ab.shot((Arrow) event.getProjectile());
				if(ab.getState() != BendingAbilityState.START && ab.getState() != BendingAbilityState.ENDED) {
					AbilityManager.getManager().addInstance(ab);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(EntityTeleportEvent event) {
		Entity entity = event.getEntity();
		if (Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
		if(EarthGrab.isGrabbed(entity)) {
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
		if(EarthGrab.isGrabbed(entity)) {
			event.setCancelled(true);
		}
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (pr instanceof Arrow) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
				if (bPlayer.isBender(BendingElement.FIRE)) {
					if (p.isSneaking() && bPlayer.getAbility().equals(HeatControl.NAME)) {
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
				
				ExplosiveShot explosiveShot = (ExplosiveShot) AbilityManager.getManager().getInstances(ExplosiveShot.NAME).get(player);
				if(explosiveShot != null) {
					explosiveShot.explode();
				}
				
				String ability = bPlayer.getAbility();
				if (ability != null && ability.equals(C4.NAME) && EntityTools.canBend(player, ability)) {
					C4 bomb = (C4) AbilityManager.getManager().buildAbility(C4.NAME, player);
					bomb.setArrow(arrow);
					if(bomb.canBeInitialized()) {
						bomb.swing();
						if(!bomb.isState(BendingAbilityState.START) && !bomb.isState(BendingAbilityState.ENDED)) {
							AbilityManager.getManager().addInstance(bomb);
						}
					}
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
		if(EarthGrab.isGrabbed(entity)) {
			event.setCancelled(true);
		}
	}
}

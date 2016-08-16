package net.bendercraft.spigot.bending.learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.fire.FireBlade;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.abilities.fire.FireBurst;
import net.bendercraft.spigot.bending.abilities.fire.FireJet;
import net.bendercraft.spigot.bending.abilities.fire.FireShield;
import net.bendercraft.spigot.bending.abilities.fire.FireWall;
import net.bendercraft.spigot.bending.abilities.fire.Illumination;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.event.BendingAbilityEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;

public class FireListener implements Listener {
	private BendingLearning plugin;

	private Map<UUID, Integer> fireBlade = new HashMap<UUID, Integer>();

	private Map<UUID, Integer> fireBlastSucceded = new HashMap<UUID, Integer>();
	private Map<UUID, Long> fireBlastlastTime = new HashMap<UUID, Long>();

	private Map<UUID, Integer> wallOfFire = new HashMap<UUID, Integer>();

	private static ChatColor color = PluginTools.getColor(Settings.getColor(BendingElement.FIRE));

	public FireListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void unlockFireBlade(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) event.getDamager());
			if (bPlayer != null) {
				if (bPlayer.isBender(BendingElement.FIRE) && (bPlayer.getAbility() != null) && bPlayer.getAbility().equalsIgnoreCase(FireBlast.NAME)) {
					boolean ok = false;
					Player p = (Player) event.getDamager();
					if (p.getInventory().getItemInMainHand().getType().equals(Material.WOOD_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInMainHand().getType().equals(Material.STONE_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInMainHand().getType().equals(Material.GOLD_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_SWORD)) {
						ok = true;
					}

					if (ok) {
						int damaged = 0;
						if (this.fireBlade.containsKey(p.getUniqueId())) {
							damaged = this.fireBlade.get(p.getUniqueId());
						}
						damaged = damaged + 1;

						if (damaged >= 50) {
							if (this.plugin.addPermission(p, FireBlade.NAME)) {
								String message = "Your combat experience allowed you to create a sword from your fire bending";
								p.sendMessage(color + message);
								message = "Congratulations, you have unlocked " + FireBlade.NAME;
								p.sendMessage(color + message);
							}

							this.fireBlade.remove(p.getUniqueId());
						} else {
							this.fireBlade.put(p.getUniqueId(), damaged);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockFireBurst(BendingAbilityEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.FIRE) && event.getAbility().equalsIgnoreCase(FireBlast.NAME)) {
				Player p = bPlayer.getPlayer();
				long lastTime = -1;
				long currentTime = System.currentTimeMillis();
				if (this.fireBlastlastTime.containsKey(p.getUniqueId())) {
					lastTime = this.fireBlastlastTime.get(p.getUniqueId());
				}

				if ((currentTime - lastTime) > 10000) {
					this.fireBlastSucceded.remove(p.getUniqueId());
				}

				int success = 0;
				if (this.fireBlastSucceded.containsKey(p.getUniqueId())) {
					success = this.fireBlastSucceded.get(p.getUniqueId());
				}
				success = success + 1;

				if (success > 20) {
					if (this.plugin.addPermission(p, FireBurst.NAME)) {
						String message = "Your skill at fire blasting has improved enough for you to burst 3 in one";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + FireBurst.NAME;
						p.sendMessage(color + message);
					}

					this.fireBlastSucceded.remove(p.getUniqueId());
					this.fireBlastlastTime.remove(p.getUniqueId());
				} else {
					this.fireBlastSucceded.put(p.getUniqueId(), success);
					this.fireBlastlastTime.put(p.getUniqueId(), currentTime);
				}
			}
		}
	}

	@EventHandler
	public void unlockFireShield(BendingAbilityEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.FIRE) && event.getAbility().equalsIgnoreCase(FireShield.NAME)) {
				List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(bPlayer.getPlayer().getLocation(), 10);

				for (Entity entity : entities) {
					if (entity instanceof Player) {
						Player p = (Player) entity;
						BendingPlayer trainee = BendingPlayer.getBendingPlayer(p);
						if (trainee.isBender(BendingElement.FIRE)) {
							if (p.hasLineOfSight(bPlayer.getPlayer())) {
								if (this.plugin.addPermission(p, FireShield.NAME)) {
									String message = "After seeing " + bPlayer.getPlayer().getName() + " doing a fire shield, you are able to copy it for yourself.";
									p.sendMessage(color + message);
									message = "Congratulations, you have unlocked " + FireShield.NAME;
									p.sendMessage(color + message);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockWallOfFire(BendingAbilityEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.FIRE) && event.getAbility().equalsIgnoreCase(FireShield.NAME)) {
				Player p = bPlayer.getPlayer();
				int done = 0;
				if (this.wallOfFire.containsKey(p.getUniqueId())) {
					done = this.wallOfFire.get(p.getUniqueId());
				}
				done = done + 1;

				if (done > 100) {
					if (this.plugin.addPermission(p, FireWall.NAME)) {
						String message = "By doing so much fire shield, you wonder if you could turn this ability into an offensive one";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + FireWall.NAME;
						p.sendMessage(color + message);
					}

					this.wallOfFire.remove(p.getUniqueId());
				} else {
					this.wallOfFire.put(p.getUniqueId(), done);
				}
			}
		}
	}

	@EventHandler
	public void unlockIllumination(BlockPlaceEvent event) {
		if (event.getBlockPlaced().getType().equals(Material.TORCH)) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
			if (bPlayer != null) {
				if (bPlayer.isBender(BendingElement.FIRE)) {
					Player p = event.getPlayer();
					if (this.plugin.addPermission(p, Illumination.NAME)) {
						String message = "As you tried to light up, you realize that your firebending can already sustain that task";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + Illumination.NAME;
						p.sendMessage(color + message);
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockFireJet(EntityDamageEvent event) {
		if (event.getCause().equals(DamageCause.FALL) && (event.getEntity() instanceof Player)) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) event.getEntity());
			if (bPlayer != null) {
				if (bPlayer.isBender(BendingElement.FIRE)) {
					// Check fireburst
					Player p = (Player) event.getEntity();
					if (EntityTools.canBend(p, AbilityManager.getManager().getRegisteredAbility(FireBurst.NAME))) {
						if (EntityTools.canBend(p, AbilityManager.getManager().getRegisteredAbility(FireWall.NAME))) {
							if (this.plugin.addPermission(p, FireJet.NAME)) {
								String message = "An idea came up to you, by combining a fire burst and a wall of fire, you think you will be able to fly temporary into the air";
								p.sendMessage(color + message);
								message = "Congratulations, you have unlocked " + FireJet.NAME;
								p.sendMessage(color + message);
							}
						}
					}
				}
			}
		}
	}
}

package net.avatar.realms.spigot.bending.learning.listeners;

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

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.event.AbilityCooldownEvent;
import net.avatar.realms.spigot.bending.learning.BendingLearning;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class FireListener implements Listener {
	private BendingLearning plugin;

	private Map<UUID, Integer> fireBlade = new HashMap<UUID, Integer>();

	private Map<UUID, Integer> fireBlastSucceded = new HashMap<UUID, Integer>();
	private Map<UUID, Long> fireBlastlastTime = new HashMap<UUID, Long>();

	private Map<UUID, Integer> wallOfFire = new HashMap<UUID, Integer>();

	private static ChatColor color = PluginTools.getColor(Settings.getColorString("Fire"));

	public FireListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void unlockFireBlade(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) event.getDamager());
			if (bPlayer != null) {
				if (bPlayer.isBender(BendingElement.Fire) && !bPlayer.isBender(BendingElement.ChiBlocker) && (bPlayer.getAbility() != null) && bPlayer.getAbility().equals(BendingAbilities.FireBlast)) {
					boolean ok = false;
					Player p = (Player) event.getDamager();
					if (p.getInventory().getItemInHand().getType().equals(Material.WOOD_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInHand().getType().equals(Material.STONE_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInHand().getType().equals(Material.IRON_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInHand().getType().equals(Material.GOLD_SWORD)) {
						ok = true;
					} else if (p.getInventory().getItemInHand().getType().equals(Material.DIAMOND_SWORD)) {
						ok = true;
					}

					if (ok) {
						int damaged = 0;
						if (this.fireBlade.containsKey(p.getUniqueId())) {
							damaged = this.fireBlade.get(p.getUniqueId());
						}
						damaged = damaged + 1;

						if (damaged >= 50) {
							if (this.plugin.addPermission(p, BendingAbilities.FireBlade)) {
								String message = "Your combat experience allowed you to create a sword from your fire bending";
								p.sendMessage(color + message);
								message = "Congratulations, you have unlocked " + BendingAbilities.FireBlade.name();
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
	public void unlockFireBurst(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Fire) && !bPlayer.isBender(BendingElement.ChiBlocker) && event.getAbility().equals(BendingAbilities.FireBlast)) {
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
					if (this.plugin.addPermission(p, BendingAbilities.FireBurst)) {
						String message = "Your skill at fire blasting has improved enough for you to burst 3 in one";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + BendingAbilities.FireBurst.name();
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
	public void unlockFireShield(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Fire) && event.getAbility().equals(BendingAbilities.FireShield)) {
				List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(bPlayer.getPlayer().getLocation(), 10);

				for (Entity entity : entities) {
					if (entity instanceof Player) {
						Player p = (Player) entity;
						BendingPlayer trainee = BendingPlayer.getBendingPlayer(p);
						if (trainee.isBender(BendingElement.Fire) && !trainee.isBender(BendingElement.ChiBlocker)) {
							if (p.hasLineOfSight(bPlayer.getPlayer())) {
								if (this.plugin.addPermission(p, BendingAbilities.FireShield)) {
									String message = "After seeing " + bPlayer.getPlayer().getName() + " doing a fire shield, you are able to copy it for yourself.";
									p.sendMessage(color + message);
									message = "Congratulations, you have unlocked " + BendingAbilities.FireShield.name();
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
	public void unlockWallOfFire(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Fire) && !bPlayer.isBender(BendingElement.ChiBlocker) && event.getAbility().equals(BendingAbilities.FireShield)) {
				Player p = bPlayer.getPlayer();
				int done = 0;
				if (this.wallOfFire.containsKey(p.getUniqueId())) {
					done = this.wallOfFire.get(p.getUniqueId());
				}
				done = done + 1;

				if (done > 100) {
					if (this.plugin.addPermission(p, BendingAbilities.WallOfFire)) {
						String message = "By doing so much fire shield, you wonder if you could turn this ability into an offensive one";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + BendingAbilities.WallOfFire.name();
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
				if (bPlayer.isBender(BendingElement.Fire) && !bPlayer.isBender(BendingElement.ChiBlocker)) {
					Player p = event.getPlayer();
					if (this.plugin.addPermission(p, BendingAbilities.Illumination)) {
						String message = "As you tried to light up, you realize that your firebending can already sustain that task";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + BendingAbilities.Illumination.name();
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
				if (bPlayer.isBender(BendingElement.Fire) && !bPlayer.isBender(BendingElement.ChiBlocker)) {
					// Check fireburst
					Player p = (Player) event.getEntity();
					if (EntityTools.canBend(p, BendingAbilities.FireBurst)) {
						if (EntityTools.canBend(p, BendingAbilities.WallOfFire)) {
							if (this.plugin.addPermission(p, BendingAbilities.FireJet)) {
								String message = "An idea came up to you, by combining a fire burst and a wall of fire, you think you will be able to fly temporary into the air";
								p.sendMessage(color + message);
								message = "Congratulations, you have unlocked " + BendingAbilities.FireJet.name();
								p.sendMessage(color + message);
							}
						}
					}
				}
			}
		}
	}
}

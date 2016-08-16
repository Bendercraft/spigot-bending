package net.bendercraft.spigot.bending.learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.water.IceSpike;
import net.bendercraft.spigot.bending.abilities.water.OctopusForm;
import net.bendercraft.spigot.bending.abilities.water.PhaseChange;
import net.bendercraft.spigot.bending.abilities.water.Torrent;
import net.bendercraft.spigot.bending.abilities.water.WaterBubble;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.abilities.water.WaterWall;
import net.bendercraft.spigot.bending.abilities.water.WaterWhip;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.event.BendingAbilityEvent;
import net.bendercraft.spigot.bending.event.BendingDamageEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;

public class WaterListener implements Listener {
	private BendingLearning plugin;
	private Map<UUID, Long> waterManipulationlastTime = new HashMap<UUID, Long>();
	private Map<UUID, Integer> waterManipulationSucceded = new HashMap<UUID, Integer>();

	private Map<UUID, Integer> surge = new HashMap<UUID, Integer>();
	
	private Map<UUID, Integer> octopusHit = new HashMap<UUID, Integer>();

	private static ChatColor color = PluginTools.getColor(Settings.getColor(BendingElement.WATER));

	public WaterListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void unlockTorrent(BendingAbilityEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.WATER) && event.getAbility().equalsIgnoreCase(Torrent.NAME)) {
				List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(bPlayer.getPlayer().getLocation(), 10);

				for (Entity entity : entities) {
					if (entity instanceof Player) {
						Player p = (Player) entity;
						BendingPlayer trainee = BendingPlayer.getBendingPlayer(p);
						if (trainee.isBender(BendingElement.WATER)) {
							if (p.hasLineOfSight(bPlayer.getPlayer())) {
								if (this.plugin.addPermission(p, Torrent.NAME)) {
									String message = "You saw " + bPlayer.getPlayer().getName() + " bending a continuous torrent of water, and you copied it's mouvement";
									p.sendMessage(color + message);
									message = "Congratulations, you have unlocked " + Torrent.NAME;
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
	public void unlockWaterBubble(EntityDamageEvent event) {
		if ((event.getEntity() instanceof Player) && event.getCause().equals(DamageCause.DROWNING)) {
			Player p = (Player) event.getEntity();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
			if (bPlayer != null) {
				if (bPlayer.isBender(BendingElement.WATER)) {
					if (this.plugin.addPermission(p, WaterBubble.NAME)) {
						String message = "Your suffocation made your realize that you can bend water arround you to prevent your own death";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + WaterBubble.NAME;
						p.sendMessage(color + message);
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockSurge(BendingAbilityEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.WATER) && event.getAbility().equalsIgnoreCase(WaterManipulation.NAME)) {
				int blasted = 0;
				Player p = bPlayer.getPlayer();
				if (this.surge.containsKey(p.getUniqueId())) {
					blasted = this.surge.get(p.getUniqueId());
				}
				blasted = blasted + 1;
				if (blasted >= 250) {
					if (this.plugin.addPermission(p, WaterWall.NAME)) {
						String message = "Your water manipulation has improved enough for you to concentrate multiple water source at the same time";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + WaterWall.NAME;
						p.sendMessage(color + message);
					}
					this.surge.remove(p.getUniqueId());
				} else {
					this.surge.put(p.getUniqueId(), blasted);
				}
			}
		}
	}

	@EventHandler
	public void unlockIceSpike(BendingAbilityEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			// Check if player has unlocked PhaseChange here
			if (bPlayer.isBender(BendingElement.WATER) && event.getAbility().equalsIgnoreCase(WaterManipulation.NAME)) {
				Player p = bPlayer.getPlayer();
				if (EntityTools.canBend(p, PhaseChange.NAME)) {
					if (this.plugin.addPermission(p, IceSpike.NAME)) {
						String message = "Turning water into water also made you realize that you can bend directly ice";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + IceSpike.NAME;
						p.sendMessage(color + message);
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockOctopusForm(BendingAbilityEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			// Check if player knows surge
			Player p = bPlayer.getPlayer();
			if (bPlayer.isBender(BendingElement.WATER) && EntityTools.canBend(p, WaterWall.NAME)) {
				if (event.getAbility().equalsIgnoreCase(WaterManipulation.NAME)) {
					long lastTime = -1;
					long currentTime = System.currentTimeMillis();
					if (this.waterManipulationlastTime.containsKey(p.getUniqueId())) {
						lastTime = this.waterManipulationlastTime.get(p.getUniqueId());
					}

					if ((currentTime - lastTime) > 10000) {
						this.waterManipulationSucceded.remove(p.getUniqueId());
					}

					int success = 0;
					if (this.waterManipulationSucceded.containsKey(p.getUniqueId())) {
						success = this.waterManipulationSucceded.get(p.getUniqueId());
					}
					success = success + 1;

					if (success > 25) {
						if (this.plugin.addPermission(p, OctopusForm.NAME)) {
							String message = "By launching so much water manipulation, you think you are able to manage both defense and attack by combining a water wall on yourself, and multiple water manipulation at the same time";
							p.sendMessage(color + message);
							message = "Congratulations, you have unlocked " + OctopusForm.NAME;
							p.sendMessage(color + message);
						}

						this.waterManipulationSucceded.remove(p.getUniqueId());
						this.waterManipulationlastTime.remove(p.getUniqueId());
					} else {
						this.waterManipulationSucceded.put(p.getUniqueId(), success);
						this.waterManipulationlastTime.put(p.getUniqueId(), currentTime);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void unlockWhip(BendingDamageEvent event) {
		if(!(event.getDamager() instanceof Player)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) event.getDamager());
		if (bPlayer != null) {
			// Check if player knows OctopusForm
			Player p = bPlayer.getPlayer();
			if (bPlayer.isBender(BendingElement.WATER) && EntityTools.canBend(p, OctopusForm.NAME)) {
				if (event.getAbility() != null && event.getAbility().getName().equalsIgnoreCase(OctopusForm.NAME)) {
					if(!octopusHit.containsKey(bPlayer.getPlayerID())) {
						octopusHit.put(bPlayer.getPlayerID(), 0);
					}
					octopusHit.put(bPlayer.getPlayerID(), octopusHit.get(bPlayer.getPlayerID())+1);
					if(octopusHit.get(bPlayer.getPlayerID()) >= 5) {
						if (this.plugin.addPermission(p, WaterWhip.NAME)) {
							String message = "Your skills with constant water streams has improved enough to perform a new move !";
							p.sendMessage(color + message);
							message = "Congratulations, you have unlocked " + WaterWhip.NAME;
							p.sendMessage(color + message);
						}
						octopusHit.remove(bPlayer.getPlayerID());
					}
				}
			}
		}
	}
}

package net.avatar.realms.spigot.bending.learning.listeners;

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

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.event.AbilityCooldownEvent;
import net.avatar.realms.spigot.bending.learning.BendingLearning;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class WaterListener implements Listener {
	private BendingLearning plugin;
	private Map<UUID, Long> waterManipulationlastTime = new HashMap<UUID, Long>();
	private Map<UUID, Integer> waterManipulationSucceded= new HashMap<UUID, Integer>();

	private Map<UUID, Integer> surge = new HashMap<UUID, Integer>();

	private static ChatColor color = PluginTools.getColor(Settings.getColorString("Water"));

	public WaterListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void unlockTorrent(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if(bPlayer != null) {
			if(bPlayer.isBender(BendingType.Water) && event.getAbility().equals(Abilities.Torrent)) {
				List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(bPlayer.getPlayer().getLocation(), 10);

				for(Entity entity : entities) {
					if(entity instanceof Player) {
						Player p = (Player)entity;
						BendingPlayer trainee = BendingPlayer.getBendingPlayer(p);
						if(trainee.isBender(BendingType.Water) && !trainee.isBender(BendingType.ChiBlocker)) {
							if(p.hasLineOfSight(bPlayer.getPlayer())) {
								if(this.plugin.addPermission(p, Abilities.Torrent)) {
									String message = "You saw "+bPlayer.getPlayer().getName()+" bending a continuous torrent of water, and you copied it's mouvement";
									p.sendMessage(color+message);
									message = "Congratulations, you have unlocked "+Abilities.Torrent.name();
									p.sendMessage(color+message);
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
		if((event.getEntity() instanceof Player) && event.getCause().equals(DamageCause.DROWNING)) {
			Player p = (Player) event.getEntity();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
			if(bPlayer != null) {
				if(bPlayer.isBender(BendingType.Water) && !bPlayer.isBender(BendingType.ChiBlocker)) {
					if(this.plugin.addPermission(p, Abilities.WaterBubble)) {
						String message = "Your suffocation made your realize that you can bend water arround you to prevent your own death";
						p.sendMessage(color+message);
						message = "Congratulations, you have unlocked "+Abilities.WaterBubble.name();
						p.sendMessage(color+message);
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockSurge(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if(bPlayer != null) {
			if(bPlayer.isBender(BendingType.Water) && !bPlayer.isBender(BendingType.ChiBlocker) && event.getAbility().equals(Abilities.WaterManipulation)) {
				int blasted = 0;
				Player p = bPlayer.getPlayer();
				if(this.surge.containsKey(p.getUniqueId())) {
					blasted = this.surge.get(p.getUniqueId());
				}
				blasted = blasted + 1;
				if(blasted >= 250) {
					if(this.plugin.addPermission(p, Abilities.Surge)) {
						String message = "Your water manipulation has improved enough for you to concentrate multiple water source at the same time";
						p.sendMessage(color+message);
						message = "Congratulations, you have unlocked "+Abilities.Surge.name();
						p.sendMessage(color+message);
					}
					this.surge.remove(p.getUniqueId());
				} else {
					this.surge.put(p.getUniqueId(), blasted);
				}
			}
		}
	}

	@EventHandler
	public void unlockIceSpike(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if(bPlayer != null) {
			//Check if player has unlocked PhaseChange here
			if(bPlayer.isBender(BendingType.Water) && !bPlayer.isBender(BendingType.ChiBlocker) && event.getAbility().equals(Abilities.WaterManipulation)) {
				Player p = bPlayer.getPlayer();
				if (EntityTools.canBend(p, Abilities.PhaseChange)) {
					if(this.plugin.addPermission(p, Abilities.IceSpike)) {
						String message = "Turning water into water also made you realize that you can bend directly ice";
						p.sendMessage(color+message);
						message = "Congratulations, you have unlocked "+Abilities.IceSpike.name();
						p.sendMessage(color+message);
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockOctopusForm(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if(bPlayer != null) {
			//Check if player knows surge
			Player p = bPlayer.getPlayer();
			if (bPlayer.isBender(BendingType.Water) && !bPlayer.isBender(BendingType.ChiBlocker) && EntityTools.canBend(p, Abilities.Surge)) {
				if(event.getAbility().equals(Abilities.WaterManipulation)) {
					long lastTime = -1;
					long currentTime = System.currentTimeMillis();
					if(this.waterManipulationlastTime .containsKey(p.getUniqueId())) {
						lastTime = this.waterManipulationlastTime.get(p.getUniqueId());
					}

					if((currentTime - lastTime) > 10000) {
						this.waterManipulationSucceded.remove(p.getUniqueId());
					}

					int success = 0;
					if(this.waterManipulationSucceded.containsKey(p.getUniqueId())) {
						success = this.waterManipulationSucceded.get(p.getUniqueId());
					}
					success = success + 1;

					if(success > 25) {
						if(this.plugin.addPermission(p, Abilities.OctopusForm)) {
							String message = "By launching so much water manipulation, you think you are able to manage both defense and attack by combining a water wall on yourself, and multiple water manipulation at the same time";
							p.sendMessage(color+message);
							message = "Congratulations, you have unlocked "+Abilities.OctopusForm.name();
							p.sendMessage(color+message);
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
}

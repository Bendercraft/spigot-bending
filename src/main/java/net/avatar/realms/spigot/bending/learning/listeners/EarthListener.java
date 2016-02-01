package net.avatar.realms.spigot.bending.learning.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.event.AbilityCooldownEvent;
import net.avatar.realms.spigot.bending.learning.BendingLearning;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class EarthListener implements Listener {
	private BendingLearning plugin;

	private Map<UUID, Integer> raiseAmountGrab = new HashMap<UUID, Integer>();
	private static final Integer raiseNeededGrab = 250;

	private Map<UUID, Integer> grabAmountArmor = new HashMap<UUID, Integer>();
	private static final Integer grabNeededArmor = 150;

	private Map<UUID, Integer> compactAmountTunnel = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> dirtAmountTunnel = new HashMap<UUID, Integer>();
	private static final Integer compactNeededTunnel = 250,
			dirtDugNeededTunnel = 3500;

	private Map<UUID, Integer> raiseAmountCatapult = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> jumpAmountCatapult = new HashMap<UUID, Integer>();
	private static final Integer raiseNeededCatapult = 400,
			jumpNeededCatapult = 250;

	private static ChatColor color = PluginTools.getColor(Settings.getColorString("Earth"));

	public EarthListener(BendingLearning plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void unlockEarthGrab(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Earth)) {
				if (event.getAbility().equals(BendingAbilities.RaiseEarth)) {
					int raised = 0;
					Player p = bPlayer.getPlayer();
					UUID id = p.getUniqueId();
					if (raiseAmountGrab.containsKey(id)) {
						raised = raiseAmountGrab.get(id);
					}
					raised++;
					if (raised >= raiseNeededGrab) {
						if (plugin.addPermission(p, BendingAbilities.EarthGrab)) {

							String message = "By raising multiple earth, you somehow learned to do it on a human, grabing it and making him useless";
							p.sendMessage(color + message);
							message = "Congratulations, you have unlocked " + BendingAbilities.EarthGrab.name();
							p.sendMessage(color + message);
						}

						raiseAmountGrab.remove(id);
					} else {
						raiseAmountGrab.put(id, raised);
					}
				} else if (event.getAbility().equals(BendingAbilities.EarthGrab)) {
					List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(bPlayer.getPlayer().getLocation(), 10);

					for (LivingEntity entity : entities) {
						if (entity instanceof Player) {
							Player p = (Player) entity;
							if (p.hasLineOfSight(bPlayer.getPlayer())) {
								BendingPlayer bSeen = BendingPlayer.getBendingPlayer(p);
								if (bSeen.isBender(BendingElement.Earth)) {
									int raised = 0;
									UUID id = bSeen.getPlayer().getUniqueId();
									if (raiseAmountGrab.containsKey(id)) {
										raised = raiseAmountGrab.get(id);
									}
									raised += raiseNeededGrab / 8;
									if (raised >= raiseNeededGrab) {
										if (plugin.addPermission(p, BendingAbilities.EarthGrab)) {
											String message = "You saw " + bPlayer.getPlayer().getName() + " grabing a human with his earthbending, you think you should be able to do the same";
											p.sendMessage(color + message);
											message = "Congratulations, you have unlocked " + BendingAbilities.EarthGrab.name();
											p.sendMessage(color + message);
										}
										raiseAmountGrab.remove(id);
									} else {
										raiseAmountGrab.put(id, raised);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockEarthArmor(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Earth) && event.getAbility().equals(BendingAbilities.EarthGrab)) {
				int grabs = 0;
				Player p = bPlayer.getPlayer();
				UUID id = p.getUniqueId();
				if (grabAmountArmor.containsKey(id)) {
					grabs = grabAmountArmor.get(id);
				}
				grabs++;
				if (grabs >= grabNeededArmor) {
					if (plugin.addPermission(p, BendingAbilities.EarthArmor)) {
						String message = "Your earth grab is now powerfull enough to control it and apply it to yourself without restraining your mouvement.";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + BendingAbilities.EarthArmor.name();
						p.sendMessage(color + message);
					}
					grabAmountArmor.remove(id);
				} else {
					grabAmountArmor.put(id, grabs);
				}
			}
		}
	}

	@EventHandler
	public void unlockEarthTunel(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Earth)) {
				if (event.getAbility().equals(BendingAbilities.Collapse)) {
					int comps = 0;
					Player p = bPlayer.getPlayer();
					UUID id = p.getUniqueId();
					if (compactAmountTunnel.containsKey(id)) {
						comps = compactAmountTunnel.get(id);
					}
					comps++;
					if (comps >= compactNeededTunnel && dirtAmountTunnel.containsKey(id) && dirtAmountTunnel.get(id) >= dirtDugNeededTunnel) {
						if (plugin.addPermission(p, BendingAbilities.EarthTunnel)) {
							String message = "Your skill at digging tunnel is improving, and your bending has been enhanced";
							p.sendMessage(color + message);
							message = "Congratulations, you have unlocked " + BendingAbilities.EarthTunnel.name();
							p.sendMessage(color + message);
						}
						dirtAmountTunnel.remove(id);
						compactAmountTunnel.remove(id);
					} else {
						compactAmountTunnel.put(id, comps);
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockEarthTunnel(BlockBreakEvent event) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Earth)) {
				if (event.getBlock().getType().equals(Material.DIRT) || event.getBlock().getType().equals(Material.GRASS) || event.getBlock().getType().equals(Material.STONE) || event.getBlock().getType().equals(Material.GRAVEL)) {
					int dug = 0;
					Player p = bPlayer.getPlayer();
					UUID id = p.getUniqueId();
					if (dirtAmountTunnel.containsKey(id)) {
						dug = dirtAmountTunnel.get(id);
					}
					dug++;
					if (dug >= dirtDugNeededTunnel && compactAmountTunnel.containsKey(id) && compactAmountTunnel.get(id) >= compactNeededTunnel) {
						if (plugin.addPermission(p, BendingAbilities.EarthTunnel)) {
							String message = "Your skill at digging tunnel is improving, and your bending has been enhanced";
							p.sendMessage(color + message);
							message = "Congratulations, you have unlocked " + BendingAbilities.EarthTunnel.name();
							p.sendMessage(color + message);
						}
						compactAmountTunnel.remove(id);
						dirtAmountTunnel.remove(id);
					} else {
						dirtAmountTunnel.put(id, dug);
					}
				}
			}
		}
	}

	@EventHandler
	public void unlockCatapult(AbilityCooldownEvent event) {
		BendingPlayer bPlayer = event.getBender();
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Earth) && event.getAbility().equals(BendingAbilities.RaiseEarth)) {
				int raised = 0;
				Player p = bPlayer.getPlayer();
				UUID id = p.getUniqueId();
				if (raiseAmountCatapult.containsKey(id)) {
					raised = raiseAmountCatapult.get(id);
				}
				raised++;
				if (raised >= raiseNeededCatapult && jumpAmountCatapult.containsKey(id) && jumpAmountCatapult.get(id) > jumpNeededCatapult) {
					if (plugin.addPermission(p, BendingAbilities.Catapult)) {
						String message = "By raising earth and jumping at the same time, you now know how to bend a human catapult";
						p.sendMessage(color + message);
						message = "Congratulations, you have unlocked " + BendingAbilities.Catapult.name();
						p.sendMessage(color + message);
					}
					jumpAmountCatapult.remove(id);
					raiseAmountCatapult.remove(id);
				} else {
					raiseAmountCatapult.put(id, raised);
				}
			}
		}
	}

	@EventHandler
	public void unlockCatapult(PlayerVelocityEvent event) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.Earth)) {
				Vector down = new Vector(0, 1, 0);
				float angleToDirectFall = event.getVelocity().angle(down);
				if (-Math.PI / 2 < angleToDirectFall && angleToDirectFall < Math.PI / 2) {
					// Player has probably jumped
					int jumped = 0;
					Player p = bPlayer.getPlayer();
					UUID id = p.getUniqueId();
					if (jumpAmountCatapult.containsKey(id)) {
						jumped = jumpAmountCatapult.get(id);
					}
					jumped++;

					if (jumped >= jumpNeededCatapult && raiseAmountCatapult.containsKey(id) && raiseAmountCatapult.get(id) >= raiseNeededCatapult) {
						if (plugin.addPermission(p, BendingAbilities.Catapult)) {
							String message = "By raising earth and jumping at the same time, you now know how to bend a human catapult";
							p.sendMessage(color + message);
							message = "Congratulations, you have unlocked " + BendingAbilities.Catapult.name();
							p.sendMessage(color + message);
						}
						jumpAmountCatapult.remove(id);
						raiseAmountCatapult.remove(id);
					} else {
						jumpAmountCatapult.put(id, jumped);
					}
				}
			}
		}
	}
}

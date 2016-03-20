package net.avatar.realms.spigot.bending.listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPassiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.air.AirBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirSpeed;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.Suffocate;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.arts.Speed;
import net.avatar.realms.spigot.bending.abilities.arts.Dash;
import net.avatar.realms.spigot.bending.abilities.earth.EarthArmor;
import net.avatar.realms.spigot.bending.abilities.earth.EarthPassive;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.abilities.earth.MetalBending;
import net.avatar.realms.spigot.bending.abilities.earth.MetalWire;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.Enflamed;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.FastSwimming;
import net.avatar.realms.spigot.bending.abilities.water.WaterPassive;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.TempBlock;

public class BendingPlayerListener implements Listener {
	private Bending plugin;

	/*
	 * Because PlayerInteract triggers PlayerAnimation if something was hit (like a door or trap).
	 * We need to prevent bending from such case (not really practicable).
	 * It is safe to reset this every tick, because event are sync to main thread and are not spread among 2 ticks.
	 */
	private Set<UUID> interact = new HashSet<UUID>();

	public BendingPlayerListener(Bending bending) {
		this.plugin = bending;
		this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
			@Override
			public void run() {
				BendingPlayerListener.this.interact.clear();
			}
		}, 0, 1);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();

		Bending.getInstance().getBendingDatabase().lease(player.getUniqueId());

		BendingPlayer.getBendingPlayer(player);

		if (!(Settings.CHAT_COMPATIBILITY) && (Settings.CHAT_ENABLED)) {
			player.setDisplayName(player.getName());
		}

		if ((Settings.CHAT_COMPATIBILITY) && (Settings.CHAT_ENABLED)) {
			ChatColor color = ChatColor.WHITE;
			if (Settings.CHAT_COLORED) {
				if (player.hasPermission("bending.avatar")) {
					color = PluginTools.getColor(Settings.getColor(BendingElement.ENERGY));
				} else {
					BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
					List<BendingElement> els = bender.getBendingTypes();
					if ((els != null) && !els.isEmpty()) {
						color = PluginTools.getColor(Settings.getColor(els.get(0)));
					}
				}
			}
			player.setDisplayName("<" + color + player.getName() + ChatColor.WHITE + ">");
		}

		YamlConfiguration dc = new YamlConfiguration();
		File sv = new File(Bukkit.getPluginManager().getPlugin("Bending").getDataFolder(), "Armour.sav");
		if (sv.exists() && (dc.contains("Armors." + player.getName() + ".Boots") && dc.contains("Armors." + player.getName() + ".Leggings") && dc.contains("Armors." + player.getName() + ".Chest") && dc.contains("Armors." + player.getName() + ".Helm"))) {
			ItemStack boots = new ItemStack(Material.matchMaterial(dc.getString("Armors." + player.getName() + ".Boots").split(":")[0]));
			ItemStack leggings = new ItemStack(Material.matchMaterial(dc.getString("Armors." + player.getName() + ".Leggings").split(":")[0]));
			ItemStack chest = new ItemStack(Material.matchMaterial(dc.getString("Armors." + player.getName() + ".Chest").split(":")[0]));
			ItemStack helm = new ItemStack(Material.matchMaterial(dc.getString("Armors." + player.getName() + ".Helm").split(":")[0]));
			boots.setDurability(Short.parseShort(dc.getString("Armors." + player.getName() + ".Boots").split(":")[1]));
			leggings.setDurability(Short.parseShort(dc.getString("Armors." + player.getName() + ".Leggings").split(":")[1]));
			chest.setDurability(Short.parseShort(dc.getString("Armors." + player.getName() + ".Chest").split(":")[1]));
			helm.setDurability(Short.parseShort(dc.getString("Armors." + player.getName() + ".Helm").split(":")[1]));
			ItemStack[] armors = { boots, leggings, chest, helm };
			player.getInventory().setArmorContents(armors);
		}
		try {
			dc.save(sv);
		} catch (IOException e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Failed to save armors file", e);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerScoreboard(PlayerJoinEvent event) {
		if(Settings.USE_SCOREBOARD) {
			BendingPlayer bender = BendingPlayer.getBendingPlayer(event.getPlayer());
			if(bender != null) {
				bender.loadScoreboard();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		this.interact.add(player.getUniqueId());
		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
			return;
		}

		MetalBending.use(player, event.getClickedBlock());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerFish(PlayerFishEvent event) {
		Player player = event.getPlayer();

		String ability = EntityTools.getBendingAbility(player);

		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
			return;
		}

		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);

		if (MetalBending.NAME.equals(ability) && EntityTools.canBend(player, register)) {
			MetalWire.pull(player, event.getHook());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChangeVelocity(PlayerVelocityEvent event) {
		Player player = event.getPlayer();
		// TODO : Check if this is useful ?
		if (EntityTools.isBender(player, BendingElement.WATER) && EntityTools.canBendPassive(player, BendingElement.WATER)) {
			event.setVelocity(WaterPassive.handle(player, event.getVelocity()));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (!(Settings.CHAT_ENABLED)) {
			return;
		}
		if (!(Settings.CHAT_COMPATIBILITY)) {

			Player player = event.getPlayer();
			ChatColor color = ChatColor.WHITE;

			if (Settings.CHAT_COLORED) {
				if (player.hasPermission("bending.admin.avatarstate")) {
					color = PluginTools.getColor(Settings.getColor(BendingElement.ENERGY));
				} else {
					BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
					List<BendingElement> els = bender.getBendingTypes();
					if ((els != null) && els.isEmpty()) {
						color = PluginTools.getColor(Settings.getColor(els.get(0)));
					}
				}
			}
			String format = Settings.CHAT_FORMAT;
			format = format.replace("<message>", "%2$s");
			format = format.replace("<name>", color + player.getDisplayName() + ChatColor.RESET);
			event.setFormat(format);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSwing(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		if(this.interact.contains(player.getUniqueId())) {
			this.interact.remove(player.getUniqueId());
			return;
		}
		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}

		String ability = EntityTools.getBendingAbility(player);
		RegisteredAbility registered = AbilityManager.getManager().getRegisteredAbility(ability);
		if (registered == null) {
			return;
		}

		if (EntityTools.canBend(player, registered) &&
				(registered.canBeUsedWithTools() ||!EntityTools.isTool(player.getInventory().getItemInMainHand().getType()))) {
			Map<Object, BendingAbility> abilities = AbilityManager.getManager().getInstances(ability);
			boolean shouldCreateNew = true;
			for (BendingAbility a : abilities.values()) {
				if (player.equals(a.getPlayer()) && !((BendingActiveAbility) a).swing()) {
					shouldCreateNew = false;
				}
			}
			if (shouldCreateNew) {
				BendingActiveAbility ab = AbilityManager.getManager().buildAbility(ability, player);
				if(ab == null) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Ability " + ability + " failed to construct with buildAbility for player " + player.getName());
					return;
				}
				if(ab.canBeInitialized()) {
					ab.swing();
					if(ab.getState() != BendingAbilityState.START && ab.getState() != BendingAbilityState.ENDED) {
						AbilityManager.getManager().addInstance(ab);
					}
				}
			}
		}
	}


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if (player == null){
			Bending.getInstance().getLogger().warning("Null player in PlayerSneak");
			return;
		}
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}

		String ability = EntityTools.getBendingAbility(player);
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);
		if (!player.isSneaking()
				&& ((ability == null) || (register == null) || !register.isShift())
				&& (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE || !player.isFlying())) {
			if (bender.isBender(BendingElement.WATER)
					&& EntityTools.canBendPassive(player, BendingElement.WATER)
					&& !WaterSpout.isBending(player)) {
				FastSwimming ab = new FastSwimming(AbilityManager.getManager().getRegisteredAbility(FastSwimming.NAME), player);
				if(ab.canBeInitialized()) {
					ab.start();
					if(ab.getState() != BendingAbilityState.START && ab.getState() != BendingAbilityState.ENDED) {
						AbilityManager.getManager().addInstance(ab);
						return;
					}
				}
			}
		}

		if (EntityTools.canBend(player, register) && !player.isSneaking()) {
			Map<Object, BendingAbility> abilities = AbilityManager.getManager().getInstances(ability);
			boolean shouldCreateNew = true;
			for (BendingAbility a : abilities.values()) {
				if (a.getPlayer().equals(player) && !((BendingActiveAbility) a).sneak()) {
					shouldCreateNew = false;
				}
			}
			if (shouldCreateNew) {
				BendingActiveAbility ab = AbilityManager.getManager().buildAbility(ability, player);
				if(ab == null) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Ability "+ability+" failed to construct with buildAbility for player "+player.getName());
					return;
				}
				if(ab.canBeInitialized()) {
					ab.sneak();
					if(ab.getState() != BendingAbilityState.START && ab.getState() != BendingAbilityState.ENDED) {
						AbilityManager.getManager().addInstance(ab);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSprint(PlayerToggleSprintEvent event) {
		Player player = event.getPlayer();
		if (!player.isSprinting()) {
			BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
			if (bender.isBender(BendingElement.AIR) && EntityTools.canBendPassive(player, BendingElement.AIR)) {
				AirSpeed ab = new AirSpeed(AbilityManager.getManager().getRegisteredAbility(AirSpeed.NAME), player);
				if(ab.canBeInitialized()) {
					ab.start();
					if(ab.getState() != BendingAbilityState.START && ab.getState() != BendingAbilityState.ENDED) {
						AbilityManager.getManager().addInstance(ab);
						return;
					}
				}
			}

			if (bender.isBender(BendingElement.MASTER) && EntityTools.canBendPassive(player, BendingElement.MASTER)) {
				Speed ab = new Speed(AbilityManager.getManager().getRegisteredAbility(Speed.NAME), player);
				if(ab.canBeInitialized()) {
					ab.start();
					if(ab.getState() != BendingAbilityState.START && ab.getState() != BendingAbilityState.ENDED) {
						AbilityManager.getManager().addInstance(ab);
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
			String ability = bender.getAbility();

			if (bender != null && bender.hasPath(BendingPath.TOUGH)) {
				event.setDamage(event.getDamage() * 0.9);
			}

			if (event.getCause() == DamageCause.FALL) {
				BendingPassiveAbility ab = null;
				if (bender != null && bender.isBender(BendingElement.EARTH)) {
					ab = new EarthPassive(AbilityManager.getManager().getRegisteredAbility(EarthPassive.NAME), player);
					AbilityManager.getManager().addInstance(ab);
					if (ab.start()) {
						player.setFallDistance(0);
						event.setDamage(0);
						event.setCancelled(true);
						return;
					}

					if (MetalWire.hasNoFallDamage(player)) {
						player.setFallDistance(0);
						event.setDamage(0);
						event.setCancelled(true);
						return;
					}
				}

				if (bender != null 
						&& bender.isBender(BendingElement.AIR) 
						&& EntityTools.canBendPassive(player, BendingElement.AIR)) {
					if (AirBurst.NAME.equals(ability)) {
						BendingActiveAbility burst = AbilityManager.getManager().buildAbility(AirBurst.NAME, player);
						if (burst.canBeInitialized()) {
							burst.fall();
							if(burst.getState() != BendingAbilityState.START && burst.getState() != BendingAbilityState.ENDED) {
								AbilityManager.getManager().addInstance(burst);
							}
						}
					}
					player.setFallDistance(0);
					event.setDamage(0);
					event.setCancelled(true);
					return;
				}

				if (!event.isCancelled() && EntityTools.isBender(player, BendingElement.WATER)) {
					ab = new WaterPassive(AbilityManager.getManager().getRegisteredAbility(WaterPassive.NAME), player);
					AbilityManager.getManager().addInstance(ab);
					if (ab.start()) {
						player.setFallDistance(0);
						event.setDamage(0);
						event.setCancelled(true);
						return;
					}
				}

				if (!event.isCancelled() 
						&& bender.isBender(BendingElement.MASTER)
						&& EntityTools.canBendPassive(player, BendingElement.MASTER)) {
					event.setDamage((int) (event.getDamage() * (Settings.MASTER_FALL_REDUCTION / 100.)));
					if (event.getEntity().getFallDistance() < 10) {
						event.setCancelled(true);
						return;
					}
				}

				if (!event.isCancelled() && EntityTools.isFallImmune(player)) {
					event.setCancelled(true);
					return;
				}
			}
			
			if (EntityTools.canBendPassive(player, BendingElement.FIRE) && EntityTools.isBender(player, BendingElement.FIRE) && ((event.getCause() == DamageCause.FIRE) || (event.getCause() == DamageCause.FIRE_TICK))) {
				event.setCancelled(!Enflamed.canBurn(player));
			}

			if (EntityTools.isBender(player, BendingElement.EARTH) && ((event.getCause() == DamageCause.SUFFOCATION) && TempBlock.isTempBlock(player.getEyeLocation().getBlock()))) {
				event.setDamage(0);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (WaterSpout.isBending(event.getPlayer()) || AirSpout.getPlayers().contains(event.getPlayer())) {
			Vector vel = new Vector();
			vel.setX(event.getTo().getX() - event.getFrom().getX());
			vel.setY(event.getTo().getY() - event.getFrom().getY());
			vel.setZ(event.getTo().getZ() - event.getFrom().getZ());
			// You now know the old velocity. Set to match recommended velocity
			double currspeed = vel.length();
			double maxspeed = .15;
			if (currspeed > maxspeed) {
				vel = vel.normalize().multiply(maxspeed);
				event.getPlayer().setVelocity(vel);
			}
		}
		if (Bloodbending.isBloodbended(player)) {
			double distance1, distance2;
			Location loc = Bloodbending.getBloodbendingLocation(player);
			distance1 = event.getFrom().distance(loc);
			distance2 = event.getTo().distance(loc);
			if (distance2 > distance1) {
				player.setVelocity(new Vector(0, 0, 0));
			}
		}

		if (Dash.isDashing(player)) {
			Vector dir = event.getTo().clone().subtract(event.getFrom()).toVector();
			Dash d = Dash.getDash(player);
			d.setDirection(dir);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		if (Bloodbending.isBloodbended(event.getPlayer())) {
			event.setCancelled(true);
			event.setReason(null);
		}
		if (EntityTools.isFallImmune(event.getPlayer())) {
			event.setCancelled(true);
			event.setReason(null);
		}

		Bending.getInstance().getBendingDatabase().release(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerDropitem(PlayerDropItemEvent event) {
		if (Suffocate.isTempHelmet(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if ((event.getSlotType() == SlotType.ARMOR) && !EarthArmor.canRemoveArmor((Player) event.getWhoClicked())) {
			event.setCancelled(true);
		}

		if (Suffocate.isTempHelmet(event.getCurrentItem())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (EarthArmor.hasEarthArmor(event.getPlayer())) {
			EarthArmor.removeEffect(event.getPlayer());
			event.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		}
		if (Suffocate.isTargeted(event.getPlayer())) {
			Suffocate.getSuffocateByTarget(event.getPlayer()).remove();
		}

		Bending.getInstance().getBendingDatabase().release(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();
		if (Tornado.getPlayers().contains(p) || Bloodbending.isBloodbended(p) || FireJet.getPlayers().contains(p) || AvatarState.getPlayers().contains(p)) {
			event.setCancelled(p.getGameMode() != GameMode.CREATIVE);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {

		EntityDamageEvent ede = event.getEntity().getLastDamageCause();

		if ((ede != null) && (ede.getCause() != null) && (ede.getCause() == DamageCause.LAVA)) {
			Player player = event.getEntity();
			Location loc = player.getLocation();
			LavaTrain lT = LavaTrain.getLavaTrain(loc.getBlock());
			if (lT != null) {
				event.setDeathMessage(player.getName() + " died swimming in " + lT.getPlayer().getName() + "'s lava train");
			}
		}

		if (EarthArmor.hasEarthArmor(event.getEntity())) {
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			EarthArmor armor = EarthArmor.getEarthArmor(event.getEntity());
			for (int i = 0; i < drops.size(); i++) {
				// Remove eartharmor from items drops
				if (!armor.isArmor(drops.get(i))) {
					newdrops.add(drops.get(i));
				}
			}
			// Koudja : Since "EarthArmor.removeEffect" already restore player
			// armor, do not drop it again !
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);
			EarthArmor.removeEffect(event.getEntity());
		}

		// Fireblade & Suffocate
		List<ItemStack> toRemove = new LinkedList<ItemStack>();
		for (ItemStack item : event.getDrops()) {
			if (Suffocate.isTempHelmet(item)) {
				toRemove.add(item);
			}
		}
		event.getDrops().removeAll(toRemove);

		if (EntityTools.isGrabed(event.getEntity())) {
			EntityTools.unGrab(event.getEntity());
		}
	}
}

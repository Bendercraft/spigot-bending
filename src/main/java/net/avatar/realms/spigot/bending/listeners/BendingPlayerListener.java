package net.avatar.realms.spigot.bending.listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.abilities.air.AirBurstCone;
import net.avatar.realms.spigot.bending.abilities.air.AirBurstSphere;
import net.avatar.realms.spigot.bending.abilities.air.AirFallBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.Suffocate;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.chi.Dash;
import net.avatar.realms.spigot.bending.abilities.earth.Catapult;
import net.avatar.realms.spigot.bending.abilities.earth.Collapse;
import net.avatar.realms.spigot.bending.abilities.earth.CompactColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthArmor;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.earth.EarthColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthGrab;
import net.avatar.realms.spigot.bending.abilities.earth.EarthPassive;
import net.avatar.realms.spigot.bending.abilities.earth.EarthShield;
import net.avatar.realms.spigot.bending.abilities.earth.EarthTunnel;
import net.avatar.realms.spigot.bending.abilities.earth.EarthWall;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.abilities.earth.MetalBending;
import net.avatar.realms.spigot.bending.abilities.earth.MetalWire;
import net.avatar.realms.spigot.bending.abilities.earth.Shockwave;
import net.avatar.realms.spigot.bending.abilities.earth.ShockwaveArea;
import net.avatar.realms.spigot.bending.abilities.earth.ShockwaveCone;
import net.avatar.realms.spigot.bending.abilities.earth.ShockwaveFall;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.ArcOfFire;
import net.avatar.realms.spigot.bending.abilities.fire.Combustion;
import net.avatar.realms.spigot.bending.abilities.fire.Cook;
import net.avatar.realms.spigot.bending.abilities.fire.Extinguish;
import net.avatar.realms.spigot.bending.abilities.fire.FireBall;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlade;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurst;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurstCone;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurstSphere;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.fire.FireProtection;
import net.avatar.realms.spigot.bending.abilities.fire.FireShield;
import net.avatar.realms.spigot.bending.abilities.fire.Illumination;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.fire.RingOfFire;
import net.avatar.realms.spigot.bending.abilities.fire.WallOfFire;
import net.avatar.realms.spigot.bending.abilities.multi.Speed;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.FastSwimming;
import net.avatar.realms.spigot.bending.abilities.water.FreezeMelt;
import net.avatar.realms.spigot.bending.abilities.water.HealingWaters;
import net.avatar.realms.spigot.bending.abilities.water.IceSpike2;
import net.avatar.realms.spigot.bending.abilities.water.Melt;
import net.avatar.realms.spigot.bending.abilities.water.OctopusForm;
import net.avatar.realms.spigot.bending.abilities.water.Torrent;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.abilities.water.WaterPassive;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;
import net.avatar.realms.spigot.bending.abilities.water.WaterWall;
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class BendingPlayerListener implements Listener{

	public Bending plugin;

	public BendingPlayerListener(Bending bending) {
		this.plugin = bending;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		BendingPlayer.getBendingPlayer(player);	  

		if (!(Settings.CHAT_COMPATIBILITY) && (Settings.CHAT_ENABLED)) {
			player.setDisplayName(player.getName());
		}	

		if ((Settings.CHAT_COMPATIBILITY) && (Settings.CHAT_ENABLED)) {
			ChatColor color = ChatColor.WHITE;
			if (Settings.CHAT_COLORED) {
				if (player.hasPermission("bending.avatar")) {
					color = PluginTools.getColor(Settings.getColorString("Energy"));
				} else {
					BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
					List<BendingType> els = bender.getBendingTypes();
					if ((els != null) && !els.isEmpty()) {
						color = PluginTools.getColor(Settings.getColorString(els.get(0).name()));
					}
				}
			}
			player.setDisplayName("<" + color + player.getName()
			+ ChatColor.WHITE + ">");
		}

		YamlConfiguration dc = new YamlConfiguration();
		File sv = new File(Bukkit.getPluginManager().getPlugin("Bending")
				.getDataFolder(), "Armour.sav");
		if (sv.exists()
				&& (dc.contains("Armors." + player.getName() + ".Boots")
						&& dc.contains("Armors." + player.getName()
						+ ".Leggings")
						&& dc.contains("Armors." + player.getName() + ".Chest") && dc
						.contains("Armors." + player.getName() + ".Helm"))) {
			ItemStack boots = new ItemStack(Material.matchMaterial(dc
					.getString("Armors." + player.getName() + ".Boots").split(
							":")[0]));
			ItemStack leggings = new ItemStack(Material.matchMaterial(dc
					.getString("Armors." + player.getName() + ".Leggings")
					.split(":")[0]));
			ItemStack chest = new ItemStack(Material.matchMaterial(dc
					.getString("Armors." + player.getName() + ".Chest").split(
							":")[0]));
			ItemStack helm = new ItemStack(Material.matchMaterial(dc.getString(
					"Armors." + player.getName() + ".Helm").split(":")[0]));
			boots.setDurability(Short.parseShort(dc.getString(
					"Armors." + player.getName() + ".Boots").split(":")[1]));
			leggings.setDurability(Short.parseShort(dc.getString(
					"Armors." + player.getName() + ".Leggings").split(":")[1]));
			chest.setDurability(Short.parseShort(dc.getString(
					"Armors." + player.getName() + ".Chest").split(":")[1]));
			helm.setDurability(Short.parseShort(dc.getString(
					"Armors." + player.getName() + ".Helm").split(":")[1]));
			ItemStack[] armors = { boots, leggings, chest, helm };
			player.getInventory().setArmorContents(armors);
		}
		try {
			dc.save(sv);
		} catch (IOException e) {
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();	

		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
			return;
		}

		if (FireBlade.isFireBlading(player) && FireBlade.isFireBlade(player.getItemInHand())) {
			event.setCancelled(true);
		}

		MetalBending.use(player, event.getClickedBlock());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerInteractWithEntity(PlayerInteractEntityEvent e) {
		Entity ent = e.getRightClicked();
		Player p = e.getPlayer();
		if (FireBlade.isFireBlading(p) && FireBlade.isFireBlade(p.getItemInHand())){
			if (ent instanceof ItemFrame) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerFish(PlayerFishEvent event) {
		Player player = event.getPlayer();

		Abilities ability = EntityTools.getBendingAbility(player);

		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
			return;
		}

		if ((ability == Abilities.MetalBending)
				&& EntityTools.canBend(player, ability)) {
			MetalWire.pull(player, event.getHook());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChangeVelocity(PlayerVelocityEvent event) {
		Player player = event.getPlayer();
		if (EntityTools.isBender(player, BendingType.Water)
				&& EntityTools.canBendPassive(player, BendingType.Water)) {

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
					color = PluginTools.getColor(Settings.getColorString("Energy"));
				} else {
					BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
					List<BendingType> els = bender.getBendingTypes();
					if ((els != null) && els.isEmpty()) {
						color = PluginTools.getColor(Settings.getColorString(els.get(0).name()));
					}
				}
			}
			String format = Settings.CHAT_FORMAT;
			format = format.replace("<message>", "%2$s");
			format = format.replace("<name>", color + player.getDisplayName()
			+ ChatColor.RESET);
			event.setFormat(format);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSwing(PlayerAnimationEvent event) {

		Player player = event.getPlayer();
		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}

		Abilities ability = EntityTools.getBendingAbility(player);
		if (ability == null) {
			return;
		}

		if (EntityTools.canBend(player, ability)) {

			if (ability.isAirbending() || ability.isChiblocking() || (ability == Abilities.AvatarState)) {
				Map<Object, IAbility> abilities = AbilityManager.getManager().getInstances(ability);

				if ((abilities == null) || abilities.isEmpty()) {
					ActiveAbility ab = AbilityManager.getManager().buildAbility(ability, player);
					ab.swing();
					return;
				}

				boolean shouldCreateNew = false;
				for (IAbility a : abilities.values()) {
					if (a.getPlayer().equals(player)) {
						if (((ActiveAbility)a).swing()) {
							shouldCreateNew = true;
						}
					}
				}
				if (shouldCreateNew) {
					ActiveAbility ab = AbilityManager.getManager().buildAbility(ability, player);
					ab.swing();
				}
				return;
			}

			if (ability == Abilities.AirBurst) {
				new AirBurstCone(player, null);
				return;
			}

			if (ability == Abilities.Catapult) {
				new Catapult(player, null);
				return;
			}

			if (ability == Abilities.RaiseEarth) {
				new EarthColumn(player, null);
				return;
			}

			if (ability == Abilities.Collapse) {
				new CompactColumn(player, null);
				return;
			}

			if (ability == Abilities.EarthGrab) {
				new EarthGrab(player, false, null);
				return;
			}

			if (ability == Abilities.EarthBlast) {
				EarthBlast.throwEarth(player);
				return;
			}

			if (ability == Abilities.EarthArmor) {
				new EarthArmor(player, null);
				return;
			}

			if (ability == Abilities.Shockwave) {
				new ShockwaveCone(player, null);
				return;
			}

			if ((ability == Abilities.LavaTrain) && player.isSneaking()) {
				new LavaTrain(player, null);
				return;
			}

			if (ability == Abilities.FireBlast) {
				new FireBlast(player, null);
				return;
			}

			if (ability == Abilities.HeatControl) {
				new Extinguish(player, null);
				return;
			}

			if (ability == Abilities.Blaze) {
				new ArcOfFire(player, null);
				return;
			}

			if (ability == Abilities.FireJet) {
				new FireJet(player, null);
				return;
			}

			if (ability == Abilities.Illumination) {
				new Illumination(player, null);
				return;
			}

			if (ability == Abilities.WallOfFire) {
				new WallOfFire(player, null);
				return;
			}

			if (ability == Abilities.FireBurst) {
				new FireBurstCone(player, null);
				return;
			}

			if (ability == Abilities.FireShield) {
				new FireProtection(player, null);
				return;
			}

			if (ability == Abilities.FireBlade) {
				new FireBlade(player);
				return;
			}

			if (ability == Abilities.WaterManipulation) {
				WaterManipulation.moveWater(player);
				return;
			}

			if (ability == Abilities.IceSpike) {
				IceSpike2.activate(player);
				return;
			}

			if (ability == Abilities.PhaseChange) {
				new FreezeMelt(player, null);
				return;
			}

			if (ability == Abilities.Surge) {
				new WaterWall(player, null);
				return;
			}

			if (ability == Abilities.OctopusForm) {
				new OctopusForm(player, null);
				return;
			}

			if (ability == Abilities.Torrent) {
				new Torrent(player, null);
				return;
			}

			if (ability == Abilities.WaterSpout) {
				new WaterSpout(player, null);
				return;
			}

			if (ability == Abilities.Bloodbending) {
				Bloodbending.launch(player);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();

		if (Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}

		Abilities ability = EntityTools.getBendingAbility(player);
		if ((ability == null) || !ability.isShiftAbility()) {
			if (EntityTools.isBender(player,BendingType.Water)
					&& EntityTools.canBendPassive(player, BendingType.Water)){
				new FastSwimming(player);
			}
			return;
		}

		if (EntityTools.canBend(player, ability)) {
			// If the player unsneaks
			if (player.isSneaking()) {
				if (ability == Abilities.AirBurst) {
					new AirBurstSphere(player, null);
				}

				if (ability == Abilities.Shockwave) {
					new ShockwaveArea(player, null);
				}

				if (ability == Abilities.FireBurst) {
					new FireBurstSphere(player, null);
				}
			}

			if (!player.isSneaking()) {
				// If the player sneaks

				if (ability == Abilities.EarthBlast) {
					new EarthBlast(player, null);
					return;
				}

				if (ability == Abilities.EarthGrab) {
					new EarthGrab(player, true, null);
					return;
				}

				if (ability == Abilities.Shockwave) {
					new Shockwave(player, null);
				}

				if (ability == Abilities.Tremorsense) {
					BendingPlayer.getBendingPlayer(player).toggleTremorsense();
				}

				if (ability == Abilities.Collapse) {
					new Collapse(player, null);
				}

				if (ability == Abilities.EarthArmor) {
					new EarthShield(player, null);
				}

				if (ability == Abilities.WaterManipulation) {
					new WaterManipulation(player, null);
					return;
				}

				if (ability == Abilities.HealingWaters) {
					new HealingWaters(player);
				}

				if (ability == Abilities.IceSpike) {
					new IceSpike2(player, null);
				}

				if (ability == Abilities.EarthTunnel) {
					new EarthTunnel(player, null);
				}

				if (ability == Abilities.RaiseEarth) {
					new EarthWall(player, null);
				}

				if (ability == Abilities.Surge) {
					WaterWall.form(player);
				}

				if (ability == Abilities.OctopusForm) {
					OctopusForm.form(player);
				}

				if (ability == Abilities.Torrent) {
					Torrent.create(player);
				}

				if (ability == Abilities.Bloodbending) {
					new Bloodbending(player, null);
				}

				if (ability == Abilities.PhaseChange) {
					new Melt(player, null);
				}

				if (ability == Abilities.Lightning) {
					new Lightning(player, null);
				}

				if (ability == Abilities.Blaze) {
					new RingOfFire(player, null);
				}

				if (ability == Abilities.FireBurst) {
					new FireBurst(player, null);
				}

				if (ability == Abilities.FireBlast) {
					new FireBall(player, null);
				}

				if (ability == Abilities.FireShield) {
					new FireShield(player, null);
				}

				if (ability == Abilities.HeatControl) {
					new Cook(player, null);
				}

				if (ability == Abilities.MetalBending) {
					MetalBending.metalMelt(player);
				}

				if (ability == Abilities.Combustion) {
					new Combustion(player, null);
				}

				if ( ability.isAirbending() || (ability == Abilities.Dash) 
						|| ((ability == Abilities.PlasticBomb)
								|| (ability == Abilities.WaterBubble))) {

					Map<Object, IAbility> abilities = AbilityManager.getManager().getInstances(ability);

					if ((abilities == null) || abilities.isEmpty()) {
						ActiveAbility ab = AbilityManager.getManager().buildAbility(ability, player);
						ab.sneak();
						return;
					}

					boolean shouldCreateNew = false;
					for (IAbility a : abilities.values()) {
						if (a.getPlayer().equals(player)) {
							if (((ActiveAbility)a).sneak()) {
								shouldCreateNew = true;
							}
						}
					}
					if (shouldCreateNew) {
						ActiveAbility ab = AbilityManager.getManager().buildAbility(ability, player);
						ab.sneak();
					}
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSprint(PlayerToggleSprintEvent event) {
		Player player = event.getPlayer();

		if (!player.isSprinting()
				&& EntityTools.isBender(player, BendingType.Air)
				&& EntityTools.canBendPassive(player, BendingType.Air)) {
			Speed sp = new Speed(player);
			sp.start();
		}

		if (!player.isSprinting()
				&& EntityTools.isBender(player, BendingType.ChiBlocker)
				&& EntityTools.canBendPassive(player, BendingType.ChiBlocker)) {
			Speed sp = new Speed(player);
			sp.start();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Abilities ability = EntityTools.getBendingAbility(player);

			if (event.getCause() == DamageCause.FALL) {
				if (EntityTools.isBender(player, BendingType.Earth)) {

					if (ability == Abilities.Shockwave) {
						new ShockwaveFall(player, null);
					}

					if (EarthPassive.softenLanding(player)
							&& EntityTools.canBendPassive(player, BendingType.Earth)) {
						new Flight(player);
						player.setAllowFlight(true);
						player.setFallDistance(0);
						event.setDamage(0);
						event.setCancelled(true);
					}

					if (MetalWire.hasNoFallDamage(player)) {
						player.setFallDistance(0);
						event.setDamage(0);
						event.setCancelled(true);
					}
				}

				if (EntityTools.isBender(player, BendingType.Air)
						&& EntityTools.canBendPassive(player, BendingType.Air)) {
					new Flight(player);
					player.setAllowFlight(true);
					if (ability == Abilities.AirBurst) {
						new AirFallBurst(player, null);
					}
					player.setFallDistance(0);
					event.setDamage(0);
					event.setCancelled(true);
				}

				if (!event.isCancelled()
						&& EntityTools.isBender(player, BendingType.Water)) {
					if (WaterPassive.softenLanding(player)
							&& EntityTools.canBendPassive(player, BendingType.Water)) {
						new Flight(player);
						player.setAllowFlight(true);
						player.setFallDistance(0);
						event.setDamage(0);
						event.setCancelled(true);
					}
				}

				if (!event.isCancelled()
						&& EntityTools.isBender(player, BendingType.ChiBlocker)) {
					if (EntityTools.canBendPassive(player,
							BendingType.ChiBlocker)) {
						event.setDamage((int) (event.getDamage() * (Settings.CHI_FALL_REDUCTION / 100.)));
						if (event.getEntity().getFallDistance() < 10) {
							event.setCancelled(true);
						}
					}
				}

				if(!event.isCancelled()) {
					if(EntityTools.isFallImmune(player)) {
						event.setCancelled(true);
					}
				}
			}

			if (!event.isCancelled() && (event.getCause() == DamageCause.FALL)) {
				Player source = Flight.getLaunchedBy(player);
				if (source != null) {
					event.setCancelled(true);
					EntityTools.damageEntity(source, player, event.getDamage());
				}
			}

			if (EntityTools.canBendPassive(player, BendingType.Fire)
					&& EntityTools.isBender(player, BendingType.Fire)
					&& ((event.getCause() == DamageCause.FIRE) || (event
							.getCause() == DamageCause.FIRE_TICK))) {
				event.setCancelled(!Extinguish.canBurn(player));
			}

			if (EntityTools.isBender(player, BendingType.Earth)
					&& ((event.getCause() == DamageCause.SUFFOCATION) && TempBlock
							.isTempBlock(player.getEyeLocation().getBlock()))) {
				event.setDamage(0);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (WaterSpout.isBending(event.getPlayer())
				|| AirSpout.getPlayers().contains(event.getPlayer())) {
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
			if (distance2 > distance1)
			{
				player.setVelocity(new Vector(0, 0, 0));
				// return;
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
		if(Bloodbending.isBloodbended(event.getPlayer())) {
			event.setCancelled(true);
			event.setReason(null);
		}
		if(EntityTools.isFallImmune(event.getPlayer())) {
			event.setCancelled(true);
			event.setReason(null);
		}
	}

	@EventHandler
	public void onPlayerDropitem(PlayerDropItemEvent event) {
		if(FireBlade.isFireBlade(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
		if(Suffocate.isTempHelmet(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if ((event.getSlotType() == SlotType.ARMOR)
				&& !EarthArmor.canRemoveArmor((Player) event.getWhoClicked())) {
			event.setCancelled(true);
		}

		if (FireBlade.isFireBlade(event.getCurrentItem())) {
			event.setCancelled(true);
		}

		if(Suffocate.isTempHelmet(event.getCurrentItem())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (EarthArmor.hasEarthArmor(event.getPlayer())) {
			EarthArmor.removeEffect(event.getPlayer());
			event.getPlayer().removePotionEffect(
					PotionEffectType.DAMAGE_RESISTANCE);
		}
		if(Suffocate.isTargeted(event.getPlayer())) {
			Suffocate.getSuffocateByTarget(event.getPlayer()).remove();
		}
		if(FireBlade.isFireBlading(event.getPlayer())) {
			FireBlade.getFireBlading(event.getPlayer()).remove();
		}

		if (EntityTools.speToggled(event.getPlayer())) {
			EntityTools.speToggledBenders.remove(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player p = event.getPlayer();
		if (Tornado.getPlayers().contains(p) || Bloodbending.isBloodbended(p)
				|| Speed.getPlayers().contains(p)
				|| FireJet.getPlayers().contains(p)
				|| AvatarState.getPlayers().contains(p)) {
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
				event.setDeathMessage(player.getName() + " died swimming in " 
						+ lT.getPlayer().getName() + "'s lava train");
			}
		}

		if (EarthArmor.hasEarthArmor(event.getEntity())) {
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			EarthArmor armor = EarthArmor.getEarthArmor(event.getEntity());
			for (int i = 0; i < drops.size(); i++) {
				// Remove eartharmor from items drops
				if (!armor.isArmor(drops.get(i))){
					newdrops.add((drops.get(i)));
				}	
			}
			// Koudja : Since "EarthArmor.removeEffect" already restore player
			// armor, do not drop it again !
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);
			EarthArmor.removeEffect(event.getEntity());
		}

		//Fireblade & Suffocate
		List<ItemStack> toRemove = new LinkedList<ItemStack>();
		for(ItemStack item : event.getDrops()) {
			if(FireBlade.isFireBlade(item) || Suffocate.isTempHelmet(item)) {
				toRemove.add(item);
			}
		}
		event.getDrops().removeAll(toRemove);

		if (EntityTools.isGrabed(event.getEntity())) {
			EntityTools.unGrab(event.getEntity());
		}
	}
}

package net.avatarrealms.minecraft.bending;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.abilities.air.*;
import net.avatarrealms.minecraft.bending.abilities.chi.*;
import net.avatarrealms.minecraft.bending.abilities.earth.*;
import net.avatarrealms.minecraft.bending.abilities.fire.*;
import net.avatarrealms.minecraft.bending.abilities.water.*;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempBlock;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

public class BendingListener implements Listener {

	public Bending plugin;

	public BendingListener(Bending bending) {
		this.plugin = bending;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		BendingPlayer.getBendingPlayer(player);
		String append = "";
		if ((player.hasPermission("bending.avatar")) && ConfigManager.enabled) {
			append = ConfigManager.getPrefix("Avatar");
		} else if ((EntityTools.isBender(player, BendingType.Air))
				&& (ConfigManager.enabled)) {
			append = ConfigManager.getPrefix("Air");
		} else if ((EntityTools.isBender(player, BendingType.Earth))
				&& (ConfigManager.enabled)) {
			append = ConfigManager.getPrefix("Earth");
		} else if ((EntityTools.isBender(player, BendingType.Fire))
				&& (ConfigManager.enabled)) {
			append = ConfigManager.getPrefix("Fire");
		} else if ((EntityTools.isBender(player, BendingType.Water))
				&& (ConfigManager.enabled)) {
			append = ConfigManager.getPrefix("Water");
		} else if ((EntityTools.isBender(player, BendingType.ChiBlocker))
				&& (ConfigManager.enabled)) {
			append = ConfigManager.getPrefix("ChiBlocker");
		}

		if (!(ConfigManager.compatibility) && (ConfigManager.enabled))
			player.setDisplayName(append + player.getName());

		if ((ConfigManager.compatibility) && (ConfigManager.enabled)) {
			ChatColor color = ChatColor.WHITE;
			if (ConfigManager.colors) {
				if (player.hasPermission("bending.avatar")) {
					color = PluginTools.getColor(ConfigManager.getColor("Avatar"));
				} else if (EntityTools.isBender(player, BendingType.Air)) {
					color = PluginTools.getColor(ConfigManager.getColor("Air"));
				} else if (EntityTools.isBender(player, BendingType.Earth)) {
					color = PluginTools.getColor(ConfigManager.getColor("Earth"));
				} else if (EntityTools.isBender(player, BendingType.Fire)) {
					color = PluginTools.getColor(ConfigManager.getColor("Fire"));
				} else if (EntityTools.isBender(player, BendingType.Water)) {
					color = PluginTools.getColor(ConfigManager.getColor("Water"));
				} else if (EntityTools.isBender(player,
						BendingType.ChiBlocker)) {
					color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
				}
			}
			player.setDisplayName("<" + color + append + player.getName()
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
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
			BendingPlayer.getBendingPlayer(player).cooldown();
		// Cooldowns.forceCooldown(player);
		if (Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		// Cooldowns.forceCooldown(player);
		BendingPlayer.getBendingPlayer(player).cooldown();
		if (Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
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
		if (!(ConfigManager.enabled))
			return;
		if (!(ConfigManager.compatibility)) {

			Player player = event.getPlayer();
			ChatColor color = ChatColor.WHITE;

			if (ConfigManager.colors) {
				if (player.hasPermission("bending.avatar")) {
					color = PluginTools.getColor(ConfigManager.getColor("Avatar"));
				} else if (EntityTools.isBender(player, BendingType.Air)) {
					color = PluginTools.getColor(ConfigManager.getColor("Air"));
				} else if (EntityTools.isBender(player, BendingType.Earth)) {
					color = PluginTools.getColor(ConfigManager.getColor("Earth"));
				} else if (EntityTools.isBender(player, BendingType.Fire)) {
					color = PluginTools.getColor(ConfigManager.getColor("Fire"));
				} else if (EntityTools.isBender(player, BendingType.Water)) {
					color = PluginTools.getColor(ConfigManager.getColor("Water"));
				} else if (EntityTools.isBender(player,
						BendingType.ChiBlocker)) {
					color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
				}
			}
			String format = ConfigManager.chat;
			format = format.replace("<message>", "%2$s");
			format = format.replace("<name>", color + player.getDisplayName() + ChatColor.RESET);
//			String format2 = format.replace("<name>", color + player.getDisplayName()).replace("<message>", ChatColor.WHITE + event.getMessage());
			event.setFormat(format);
			//				event.setFormat("<" + color + player.getDisplayName()
			//						+ ChatColor.WHITE + "> " + event.getMessage());
			// event.setFormat(format);
			// // event.setMessage(message + "Test");
			// Tools.verbose(event.getFormat());
		}
	}

	// event.setMessage(append + event.getMessage());
	// }

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSwing(PlayerAnimationEvent event) {

		Player player = event.getPlayer();
		if (Bloodbending.isBloodbended(player) || Paralyze.isParalyzed(player)) {
			event.setCancelled(true);
		}

		AirScooter.check(player);

		Abilities ability = EntityTools.getBendingAbility(player);
		if (ability == null)
			return;
		if (EntityTools.canBend(player, ability)) {

			if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Air")) {

				if (ability == Abilities.AirBlast) {
					new AirBlast(player);
				}

				if (ability == Abilities.AirSuction) {
					new AirSuction(player);
				}

				if (ability == Abilities.AirSwipe) {
					new AirSwipe(player);
				}

				if (ability == Abilities.AirScooter) {
					new AirScooter(player);
				}

				if (ability == Abilities.AirSpout) {
					new AirSpout(player);
				}

				if (ability == Abilities.AirBurst) {
					AirBurst.coneBurst(player);
				}

			}

			if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Earth")) {

				if (ability == Abilities.Catapult) {
					new Catapult(player);
				}

				if (ability == Abilities.RaiseEarth) {
					new EarthColumn(player);
				}

				if (ability == Abilities.Collapse) {
					new CompactColumn(player);
				}

				if (ability == Abilities.EarthGrab) {
					new EarthGrab(player,false);
				}

				if (ability == Abilities.EarthBlast) {
					EarthBlast.throwEarth(player);
				}

				if (ability == Abilities.Tremorsense) {
					new Tremorsense(player);
				}

				if (ability == Abilities.EarthArmor) {
					new EarthArmor(player);
				}

				if (ability == Abilities.Shockwave) {
					Shockwave.coneShockwave(player);
				}

			}

			if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Fire")) {

				if (ability == Abilities.FireBlast) {
					new FireBlast(player);
				}

				if (ability == Abilities.HeatControl) {
					new Extinguish(player);
				}

				if (ability == Abilities.Blaze) {
					new ArcOfFire(player);
				}

				if (ability == Abilities.FireJet) {
					new FireJet(player);
				}

				if (ability == Abilities.Illumination) {
					new Illumination(player);
				}

				if (ability == Abilities.WallOfFire) {
					new WallOfFire(player);
				}

				if (ability == Abilities.FireBurst) {
					FireBurst.coneBurst(player);
				}

				if (ability == Abilities.FireShield) {
					new FireShield(player);
				}

			}

			if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Water")) {

				if (ability == Abilities.WaterManipulation) {
					WaterManipulation.moveWater(player);
				}

				if (ability == Abilities.IceSpike) {
					IceSpike2.activate(player);
				}

				if (ability == Abilities.PhaseChange) {
					new FreezeMelt(player);
				}

				if (ability == Abilities.Surge) {
					new WaterWall(player);
				}

				if (ability == Abilities.OctopusForm) {
					new OctopusForm(player);
				}

				if (ability == Abilities.Torrent) {
					new Torrent(player);
				}

				if (ability == Abilities.WaterSpout) {
					new WaterSpout(player);
				}

				if (ability == Abilities.Bloodbending) {
					Bloodbending.launch(player);
				}

			}

			if (ability == Abilities.AvatarState) {
				new AvatarState(player);
			}

			if (ability == Abilities.HighJump) {
				new HighJump(player);
			}

			if (!EntityTools.isWeapon(player.getItemInHand().getType())) {

				if (ability == Abilities.RapidPunch) {
					new RapidPunch(player);
				}

				if (ability == Abilities.Paralyze) {
					Entity t = EntityTools.getTargettedEntity(player, ConfigManager.rapidPunchDistance);
					new Paralyze(player,t);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();

		if (Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}


		AirScooter.check(player);

		Abilities ability = EntityTools.getBendingAbility(player);
		if (ability == null)
			return;
		if (!player.isSneaking() && EntityTools.canBend(player, ability)) {

			if (ability == Abilities.AirShield) {
				new AirShield(player);
			}

			if (!(EntityTools.isWeapon(player.getItemInHand().getType()))
					|| ConfigManager.useWeapon.get("Air")) {

				if (ability == Abilities.AirBlast) {
					//AirBlast.setOrigin(player);
				}

				if (ability == Abilities.AirSuction) {
					AirSuction.setOrigin(player);
				}

				if (ability == Abilities.AirBurst) {
					new AirBurst(player);
				}

				if (ability == Abilities.AirSwipe) {
					Logger.getLogger("Bending").info(player.getName() + (" started to charge an airswipe"));
					AirSwipe.charge(player);
				}
			}

			if (ability == Abilities.Tornado) {
				new Tornado(player);
			}

			if (ability == Abilities.EarthBlast) {
				new EarthBlast(player);
			}

			if (ability == Abilities.EarthGrab) {
				new EarthGrab(player,true);
			}

			if (ability == Abilities.Shockwave) {
				new Shockwave(player);
			}

			if (ability == Abilities.Tremorsense) {
				BendingPlayer.getBendingPlayer(player).toggleTremorsense();
			}

			if (ability == Abilities.Collapse) {
				new Collapse(player);
			}
			
			if (ability == Abilities.EarthArmor) {
				EarthArmor.EarthShield(player);
			}

			if (ability == Abilities.WaterManipulation) {
				new WaterManipulation(player);
			}

			if (ability == Abilities.IceSpike) {
				new IceSpike2(player);
			}

			if (ability == Abilities.EarthTunnel) {
				new EarthTunnel(player);
			}

			if (ability == Abilities.RaiseEarth) {
				new EarthWall(player);
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
				new Bloodbending(player);
			}

			if (ability == Abilities.PhaseChange) {
				new Melt(player);
			}

			if (ability == Abilities.Lightning) {
				new Lightning(player);
			}

			if (ability == Abilities.Blaze) {
				new RingOfFire(player);
			}

			if (ability == Abilities.FireBurst) {
				new FireBurst(player);
			}

			if (ability == Abilities.FireBlast) {
				new Fireball(player);
			}

			if (ability == Abilities.FireShield) {
				FireShield.shield(player);
			}

			if (ability == Abilities.HeatControl) {
				new Cook(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSprint(PlayerToggleSprintEvent event) {
		Player player = event.getPlayer();

		if (!player.isSprinting()
				&& EntityTools.isBender(player, BendingType.Air)
				&& EntityTools.canBendPassive(player, BendingType.Air)) {
			new Speed(player);
		}

		if (!player.isSprinting()
				&& EntityTools.isBender(player, BendingType.ChiBlocker)
				&& EntityTools.canBendPassive(player, BendingType.ChiBlocker)) {
			new Speed(player);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {

		if (event.getEntity() instanceof Player) {
			// Tools.verbose(event.getCause());
			Player player = (Player) event.getEntity();

			if (EntityTools.isBender(player, BendingType.Earth)
					&& event.getCause() == DamageCause.FALL)
				Shockwave.fallShockwave(player);

			if (EntityTools.isBender(player, BendingType.Air)
					&& event.getCause() == DamageCause.FALL
					&& EntityTools.canBendPassive(player, BendingType.Air)) {
				new Flight(player);
				player.setAllowFlight(true);
				AirBurst.fallBurst(player);
				player.setFallDistance(0);
				event.setDamage(0);
				event.setCancelled(true);
			}

			if (!event.isCancelled()
					&& EntityTools.isBender(player, BendingType.Water)
					&& event.getCause() == DamageCause.FALL
					&& EntityTools.canBendPassive(player, BendingType.Water)) {
				if (WaterPassive.softenLanding(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled()
					&& EntityTools.isBender(player, BendingType.Earth)
					&& event.getCause() == DamageCause.FALL
					&& EntityTools.canBendPassive(player, BendingType.Earth)) {
				if (EarthPassive.softenLanding(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0);
					event.setCancelled(true);

				}
			}

			if (!event.isCancelled()
					&& EntityTools.isBender(player, BendingType.ChiBlocker)
					&& event.getCause() == DamageCause.FALL
					&& EntityTools.canBendPassive(player, BendingType.ChiBlocker)) {
				event.setDamage((int) ((double) event.getDamage() * (ConfigManager.falldamagereduction / 100.)));
			}

			if (!event.isCancelled() && event.getCause() == DamageCause.FALL) {
				Player source = Flight.getLaunchedBy(player);
				if (source != null) {
					event.setCancelled(true);
					EntityTools.damageEntity(source, player, event.getDamage());
				}
			}

			if (EntityTools.canBendPassive(player, BendingType.Fire)
					&& EntityTools.isBender(player, BendingType.Fire)
					&& (event.getCause() == DamageCause.FIRE || event
							.getCause() == DamageCause.FIRE_TICK)) {
				event.setCancelled(!Extinguish.canBurn(player));
			}

			if (EntityTools.isBender(player, BendingType.Earth)
					&& (event.getCause() == DamageCause.SUFFOCATION && TempBlock
							.isTempBlock(player.getEyeLocation().getBlock()))) {
				event.setDamage(0);
				event.setCancelled(true);
			}
			
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player)event.getEntity());
			if (bPlayer != null && event.getCause() != DamageCause.STARVATION) {
				int level = bPlayer.getMaxLevel();
				Random rand = new Random();
				if (rand.nextDouble()< ((double)level/2)/100){
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		// Tools.verbose("Caught an ignition");
		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (FireStream.ignitedblocks.containsKey(block)
				&& entity instanceof LivingEntity) {
			// Tools.verbose("Passed it to Enflamed.");
			new Enflamed(entity, FireStream.ignitedblocks.get(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (event.getCause() == DamageCause.FIRE
				&& FireStream.ignitedblocks.containsKey(entity.getLocation()
						.getBlock())) {
			new Enflamed(entity, FireStream.ignitedblocks.get(entity
					.getLocation().getBlock()));
		}
		if (Enflamed.isEnflamed(entity)
				&& event.getCause() == DamageCause.FIRE_TICK) {
			// Tools.verbose("Deal Enflamed damage.");
			event.setCancelled(true);
			Enflamed.dealFlameDamage(entity);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.getCause() == IgniteCause.LIGHTNING) {
			if (Lightning.isNearbyChannel(event.getBlock().getLocation())) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {

		Entity source = event.getDamager();
		Entity entity = event.getEntity();
		Fireball fireball = Fireball.getFireball(source);
		Lightning lightning = Lightning.getLightning(source);

		if (fireball != null) {
			event.setCancelled(true);
			fireball.dealDamage(entity);
			return;
		}

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
		
		if (source instanceof Player && entity instanceof Monster && event.getCause() == DamageCause.CUSTOM) {
			event.setDamage(event.getDamage()*2.5);
		}
		
		if (entity instanceof Player && source instanceof Monster) {
			event.setDamage(event.getDamage()*2/3);
		}

		// Tools.verbose(event.getCause());

		boolean dodged = false;

		if (source instanceof Player
				&& entity instanceof Player) {
			Player sourceplayer = (Player) source;
			Player targetplayer = (Player) entity;
			if (EntityTools.canBendPassive(sourceplayer, BendingType.ChiBlocker)
					&& EntityTools.isBender(sourceplayer,
							BendingType.ChiBlocker)
					&& event.getCause() == DamageCause.ENTITY_ATTACK
					&& event.getDamage() == 1
					&& sourceplayer.getLocation().distance(
							targetplayer.getLocation()) <= ConfigManager.rapidPunchDistance
					&& (!EntityTools.isWeapon(sourceplayer.getItemInHand().getType()) || ConfigManager.useWeapon
							.get("ChiBlocker"))) {
				EntityTools.blockChi(targetplayer, System.currentTimeMillis());
			}
		}
		if (entity instanceof Player) {
			if ((event.getCause() == DamageCause.ENTITY_ATTACK
					|| event.getCause() == DamageCause.ENTITY_EXPLOSION || event
					.getCause() == DamageCause.PROJECTILE)
					&& EntityTools.isBender(((Player) event.getEntity()),
							BendingType.ChiBlocker)
					&& EntityTools.canBendPassive((Player) event.getEntity(),
							BendingType.ChiBlocker)) {
				double rand = Math.random();
				// Tools.verbose(rand + " " + (ConfigManager.dodgechance) /
				// 100.);
				if (rand <= ConfigManager.dodgechance / 100.
						&& !Paralyze.isParalyzed(event.getEntity())) {
					event.getEntity()
							.getWorld()
							.playEffect(event.getEntity().getLocation(),
									Effect.SMOKE, 1);
					dodged = true;
					event.setCancelled(true);
				}
			}
		}
		if (source instanceof Player) {
			
			if (!dodged)
				new Paralyze((Player) event.getDamager(), event.getEntity());
			if (EntityTools.isBender(((Player) event.getDamager()),
					BendingType.ChiBlocker)
					&& event.getCause() == DamageCause.ENTITY_ATTACK
					&& !EntityTools.isWeapon(((Player) event.getDamager())
							.getItemInHand().getType())) {
				// event.setDamage((int) (ConfigManager.punchdamage));
			}
		}

	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(BlockFromToEvent event) {
		Block toblock = event.getToBlock();
		Block fromblock = event.getBlock();
		if (BlockTools.isWater(fromblock)) {
			event.setCancelled(!AirBubble.canFlowTo(toblock));
			if (!event.isCancelled()) {
				event.setCancelled(!WaterManipulation.canFlowFromTo(fromblock,
						toblock));
			}
			if (!event.isCancelled()) {
				if (Illumination.blocks.containsKey(toblock))
					toblock.setType(Material.AIR);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		event.setCancelled(Illumination.blocks.containsKey(block));
		if (!event.isCancelled()) {
			event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(FreezeMelt.isFrozen(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Wave.canThaw(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Torrent.canThaw(block));
		}
		if (FireStream.ignitedblocks.containsKey(block)) {
			FireStream.remove(block);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		if (!event.isCancelled())
			event.setCancelled(Illumination.blocks.containsKey(block));
		if (!event.isCancelled())
			event.setCancelled(BlockTools.tempnophysics.contains(block));
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (WaterWall.wasBrokenFor(player, block)
				|| OctopusForm.wasBrokenFor(player, block)
				|| Torrent.wasBrokenFor(player, block)) {
			event.setCancelled(true);
			return;
		}
		EarthBlast blast = EarthBlast.getBlastFromSource(block);
		if (blast != null) {
			blast.cancel();
		}
		
		Integer id = EarthGrab.blockInEarthGrab(block);
		if (id != null) {
			if (!EarthGrab.revertEarthGrab(id)) {
				Bending.log.info("[Bending] An error occured while removing an earthgrab");
				// Should never happen ?
			}
		}

		if (FreezeMelt.isFrozen(block)) {
			FreezeMelt.thawThenRemove(block);
			event.setCancelled(true);
		} else if (WaterWall.wallblocks.containsKey(block)) {
			WaterWall.thaw(block);
			event.setCancelled(true);
		} else if (Illumination.blocks.containsKey(block)) {
			event.setCancelled(true);
		} else if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			event.setCancelled(true);
		} else if (BlockTools.movedEarth.containsKey(block)) {
			BlockTools.removeRevertIndex(block);
		} else if (TempBlock.isTempBlock(block)) {
			TempBlock.revertBlock(block, Material.AIR);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (Paralyze.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}
		if (WaterSpout.instances.containsKey(event.getPlayer())
				|| AirSpout.getPlayers().contains(event.getPlayer())) {
			Vector vel = new Vector();
			vel.setX(event.getTo().getX() - event.getFrom().getX());
			vel.setY(event.getTo().getY() - event.getFrom().getY());
			vel.setZ(event.getTo().getZ() - event.getFrom().getZ());
			// You now know the old velocity. Set to match recommended velocity
			double currspeed = vel.length();
			double maxspeed = .15;
			if (currspeed > maxspeed) {
				// only if moving set a factor
				// double recspeed = 0.6;
				// vel = vel.ultiply(recspeed * currspeed);
				vel = vel.normalize().multiply(maxspeed);
				// apply the new velocity (MAY REQUIRE A SCHEDULED TASK
				// INSTEAD!)
				event.getPlayer().setVelocity(vel);
			}
		}
		if (Bloodbending.isBloodbended(player)) {
			double distance1, distance2;
			Location loc = Bloodbending.getBloodbendingLocation(player);
			distance1 = event.getFrom().distance(loc);
			distance2 = event.getTo().distance(loc);
			if (distance2 > distance1)
				player.setVelocity(new Vector(0, 0, 0));
			// return;
		}

		// if (Tools.isBender(player, BendingType.Water)
		// && (Tools.getBendingAbility(player) == Abilities.WalkOnWater)) {
		// WalkOnWater.freeze(player);
		// }
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			EarthBlast blast = EarthBlast.getBlastFromSource(block);

			if (blast != null) {
				blast.cancel();
			}
			if (FreezeMelt.isFrozen(block)) {
				FreezeMelt.thawThenRemove(block);
			}
			// if (WalkOnWater.affectedblocks.containsKey(block)) {
			// WalkOnWater.thaw(block);
			// }
			if (WaterWall.wallblocks.containsKey(block)) {
				block.setType(Material.AIR);
			}
			if (!Wave.canThaw(block)) {
				Wave.thaw(block);
			}
			if (BlockTools.movedEarth.containsKey(block)) {
				// Tools.removeEarthbendedBlockIndex(block);
				BlockTools.removeRevertIndex(block);
			}
		}
	}

	// @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	// public void onPlayerKick(PlayerKickEvent event) {
	// Tools.verbose(event.getReason());
	// if (BendingManager.flyingplayers.contains(event.getPlayer())
	// || Bloodbending.isBloodbended(event.getPlayer())) {
	// event.setCancelled(true);
	// event.setReason(null);
	// }
	// }

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (TempBlock.isTempBlock(event.getBlock()))
			event.setCancelled(true);
		if (!WaterManipulation.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(EntityTargetLivingEntityEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if (entity != null)
			if (Paralyze.isParalyzed(entity)
					|| Bloodbending.isBloodbended(entity))
				event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent(EntityInteractEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(EntityTeleportEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent(ProjectileLaunchEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent(SlimeSplitEvent event) {
		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Bloodbending.isBloodbended(entity))
			event.setCancelled(true);
	}

	// @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	// public void onEntityEvent(EntityEvent event) {
	// if (Paralyze.isParalyzed(event.getEntity())
	// || Bloodbending.isBloodbended(event.getEntity()))
	// if ((event instanceof EntityChangeBlockEvent
	// || event instanceof EntityExplodeEvent
	// || event instanceof EntityInteractEvent
	// || event instanceof EntityShootBowEvent
	// || event instanceof EntityTargetEvent
	// || event instanceof EntityTeleportEvent
	// || event instanceof ProjectileLaunchEvent || event instanceof
	// SlimeSplitEvent)
	// && (event instanceof Cancellable)) {
	// ((Cancellable) event).setCancelled(true);
	// }
	// }

	// @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	// public void onPlayerInteract(PlayerInteractEntityEvent event){
	// Entity rightclicked = event.getRightClicked();
	// Player player = event.getPlayer();
	// if (!Tools.isBender(player, BendingType.Air))
	// return;
	// if (!(player.getItemInHand().getType() == Material.AIR))
	// return;
	// EntityType type = event.getRightClicked().getType();
	// if (type == EntityType.COW || type == EntityType.CHICKEN || type ==
	// EntityType.SHEEP
	// || type == EntityType.PIG){
	// rightclicked.setPassenger(player);
	// }
	// if (rightclicked.getPassenger() == player){
	// rightclicked.setPassenger(null);
	// }
	//
	// }

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlotType() == SlotType.ARMOR
				&& !EarthArmor.canRemoveArmor((Player) event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (EarthArmor.instances.containsKey(event.getPlayer())) {
			EarthArmor.removeEffect(event.getPlayer());
			event.getPlayer().removePotionEffect(
					PotionEffectType.DAMAGE_RESISTANCE);
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
		if (EarthArmor.instances.containsKey(event.getEntity())) {
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			for (int i = 0; i < drops.size(); i++) {
				//Remove eartharmor from items drops
				if (!(drops.get(i).getType() == Material.LEATHER_BOOTS
						|| drops.get(i).getType() == Material.LEATHER_CHESTPLATE
						|| drops.get(i).getType() == Material.LEATHER_HELMET
						|| drops.get(i).getType() == Material.LEATHER_LEGGINGS 
						|| drops.get(i).getType() == Material.AIR))
					newdrops.add((drops.get(i)));
			}
			//Koudja : Since "EarthArmor.removeEffect" already restore player armor, do not drop it again !
			/*
			if (EarthArmor.instances.get(event.getEntity()).oldarmor != null) {
				for (ItemStack is : EarthArmor.instances.get(event.getEntity()).oldarmor) {
					if (!(is.getType() == Material.AIR))
						newdrops.add(is);
				}
			}
			*/
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);
			EarthArmor.removeEffect(event.getEntity());
		}
		
		if (EntityTools.isGrabed(event.getEntity())) {
			EntityTools.unGrab(event.getEntity());
		}
	}
}

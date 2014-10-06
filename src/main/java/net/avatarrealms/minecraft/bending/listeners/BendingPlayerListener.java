package net.avatarrealms.minecraft.bending.listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.abilities.air.AirBlast;
import net.avatarrealms.minecraft.bending.abilities.air.AirBurst;
import net.avatarrealms.minecraft.bending.abilities.air.AirBurstCone;
import net.avatarrealms.minecraft.bending.abilities.air.AirBurstSphere;
import net.avatarrealms.minecraft.bending.abilities.air.AirFallBurst;
import net.avatarrealms.minecraft.bending.abilities.air.AirScooter;
import net.avatarrealms.minecraft.bending.abilities.air.AirShield;
import net.avatarrealms.minecraft.bending.abilities.air.AirSpout;
import net.avatarrealms.minecraft.bending.abilities.air.AirSuction;
import net.avatarrealms.minecraft.bending.abilities.air.AirSwipe;
import net.avatarrealms.minecraft.bending.abilities.air.Speed;
import net.avatarrealms.minecraft.bending.abilities.air.Suffocate;
import net.avatarrealms.minecraft.bending.abilities.air.Tornado;
import net.avatarrealms.minecraft.bending.abilities.chi.Dash;
import net.avatarrealms.minecraft.bending.abilities.chi.HighJump;
import net.avatarrealms.minecraft.bending.abilities.chi.Paralyze;
import net.avatarrealms.minecraft.bending.abilities.chi.RapidPunch;
import net.avatarrealms.minecraft.bending.abilities.chi.SmokeBomb;
import net.avatarrealms.minecraft.bending.abilities.earth.Catapult;
import net.avatarrealms.minecraft.bending.abilities.earth.Collapse;
import net.avatarrealms.minecraft.bending.abilities.earth.CompactColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthArmor;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthGrab;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthPassive;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthShield;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthTunnel;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthWall;
import net.avatarrealms.minecraft.bending.abilities.earth.LavaTrain;
import net.avatarrealms.minecraft.bending.abilities.earth.MetalBending;
import net.avatarrealms.minecraft.bending.abilities.earth.MetalWire;
import net.avatarrealms.minecraft.bending.abilities.earth.Shockwave;
import net.avatarrealms.minecraft.bending.abilities.earth.ShockwaveArea;
import net.avatarrealms.minecraft.bending.abilities.earth.ShockwaveCone;
import net.avatarrealms.minecraft.bending.abilities.earth.ShockwaveFall;
import net.avatarrealms.minecraft.bending.abilities.earth.Tremorsense;
import net.avatarrealms.minecraft.bending.abilities.energy.AstralProjection;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.fire.ArcOfFire;
import net.avatarrealms.minecraft.bending.abilities.fire.Combustion;
import net.avatarrealms.minecraft.bending.abilities.fire.Cook;
import net.avatarrealms.minecraft.bending.abilities.fire.Extinguish;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlade;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBurst;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBurstCone;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBurstSphere;
import net.avatarrealms.minecraft.bending.abilities.fire.FireJet;
import net.avatarrealms.minecraft.bending.abilities.fire.FireProtection;
import net.avatarrealms.minecraft.bending.abilities.fire.FireShield;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBall;
import net.avatarrealms.minecraft.bending.abilities.fire.Illumination;
import net.avatarrealms.minecraft.bending.abilities.fire.Lightning;
import net.avatarrealms.minecraft.bending.abilities.fire.RingOfFire;
import net.avatarrealms.minecraft.bending.abilities.fire.WallOfFire;
import net.avatarrealms.minecraft.bending.abilities.water.Bloodbending;
import net.avatarrealms.minecraft.bending.abilities.water.FreezeMelt;
import net.avatarrealms.minecraft.bending.abilities.water.IceSpike2;
import net.avatarrealms.minecraft.bending.abilities.water.Melt;
import net.avatarrealms.minecraft.bending.abilities.water.OctopusForm;
import net.avatarrealms.minecraft.bending.abilities.water.Torrent;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.abilities.water.WaterPassive;
import net.avatarrealms.minecraft.bending.abilities.water.WaterSpout;
import net.avatarrealms.minecraft.bending.abilities.water.WaterWall;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class BendingPlayerListener implements Listener{
	
	public Bending plugin;

	public BendingPlayerListener(Bending bending) {
		this.plugin = bending;
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		BendingPlayer.getBendingPlayer(player);
		
			plugin.ghostManager.addGhost(player);	  
		 
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

		if (!(ConfigManager.compatibility) && (ConfigManager.enabled)) {
			player.setDisplayName(append + player.getName());
		}	

		if ((ConfigManager.compatibility) && (ConfigManager.enabled)) {
			ChatColor color = ChatColor.WHITE;
			if (ConfigManager.colors) {
				if (player.hasPermission("bending.avatar")) {
					color = PluginTools.getColor(ConfigManager.getColor("Avatar"));
				} else if (EntityTools.isBender(player, BendingType.ChiBlocker)) {
					color = PluginTools.getColor(ConfigManager.getColor("ChiBlocker"));
				} else if (EntityTools.isBender(player, BendingType.Earth)) {
					color = PluginTools.getColor(ConfigManager.getColor("Earth"));
				} else if (EntityTools.isBender(player, BendingType.Fire)) {
					color = PluginTools.getColor(ConfigManager.getColor("Fire"));
				} else if (EntityTools.isBender(player, BendingType.Water)) {
					color = PluginTools.getColor(ConfigManager.getColor("Water"));
				} else if (EntityTools.isBender(player, BendingType.Air)) {
					color = PluginTools.getColor(ConfigManager.getColor("Air"));
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
		
		
		if (Paralyze.isParalyzed(player) 
				|| Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
			return;
		}
		
		Block b = event.getClickedBlock();
		if (!event.isCancelled() 
				&& AstralProjection.isAstralProjecting(player)
				&& (b.getType()!= Material.WOOD_DOOR)
				&& (b.getType()!= Material.WOOD_PLATE)
				&& (b.getType()!= Material.GOLD_PLATE)) {
			event.setCancelled(true);;
			return;
			
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			BendingPlayer.getBendingPlayer(player).cooldown();
		}
		
		if (FireBlade.isFireBlading(player) && FireBlade.isFireBlade(player.getItemInHand())) {
			event.setCancelled(true);
		}
			
		MetalBending.use(player, event.getClickedBlock());
		// Cooldowns.forceCooldown(player);
		
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

		if (Paralyze.isParalyzed(player) 
				|| Bloodbending.isBloodbended(player)
				|| AstralProjection.isAstralProjecting(player)) {
			event.setCancelled(true);
			return;
		}

		if (ability == Abilities.MetalBending
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
		if (!(ConfigManager.enabled))
			return;
		if (!(ConfigManager.compatibility)) {

			Player player = event.getPlayer();
			ChatColor color = ChatColor.WHITE;

			if (ConfigManager.colors) {
				if (player.hasPermission("bending.avatar")) {
					color = PluginTools.getColor(ConfigManager
							.getColor("Avatar"));
				} else if (EntityTools.isBender(player, BendingType.Air)) {
					color = PluginTools.getColor(ConfigManager.getColor("Air"));
				} else if (EntityTools.isBender(player, BendingType.Earth)) {
					color = PluginTools.getColor(ConfigManager
							.getColor("Earth"));
				} else if (EntityTools.isBender(player, BendingType.Fire)) {
					color = PluginTools
							.getColor(ConfigManager.getColor("Fire"));
				} else if (EntityTools.isBender(player, BendingType.Water)) {
					color = PluginTools.getColor(ConfigManager
							.getColor("Water"));
				} else if (EntityTools.isBender(player, BendingType.ChiBlocker)) {
					color = PluginTools.getColor(ConfigManager
							.getColor("ChiBlocker"));
				}
			}
			String format = ConfigManager.chat;
			format = format.replace("<message>", "%2$s");
			format = format.replace("<name>", color + player.getDisplayName()
					+ ChatColor.RESET);
			event.setFormat(format);

		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSwing(PlayerAnimationEvent event) {

		Player player = event.getPlayer();
		if (Bloodbending.isBloodbended(player) 
				|| Paralyze.isParalyzed(player)
				|| AstralProjection.isAstralProjecting(player)) {
			event.setCancelled(true);
		}

		AirScooter.check(player);

		Abilities ability = EntityTools.getBendingAbility(player);
		if (ability == null) {
			return;
		}
			
		if (EntityTools.canBend(player, ability)) {
			if (ability == Abilities.AvatarState) {
				new AvatarState(player);
				return;
			}
			
			if (ability == Abilities.AstralProjection) {
				new AstralProjection(player, false);
				return;
			}
			
			if (ability == Abilities.HighJump) {
				new HighJump(player, null);
				return;
			}

			if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Air")) {

				if (ability == Abilities.AirBlast) {
					new AirBlast(player, null);
					return;
				}

				if (ability == Abilities.AirSuction) {
					new AirSuction(player, null);
					return;
				}

				if (ability == Abilities.AirSwipe) {
					new AirSwipe(player, null);
					return;
				}

				if (ability == Abilities.AirScooter) {
					new AirScooter(player, null);
					return;
				}

				if (ability == Abilities.AirSpout) {
					new AirSpout(player, null);
					return;
				}

				if (ability == Abilities.AirBurst) {
					new AirBurstCone(player, null);
					return;
				}
			}

			 if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Earth")) {

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

				if (ability == Abilities.Tremorsense) {
					new Tremorsense(player, null);
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
				
				if (ability == Abilities.LavaTrain && player.isSneaking()) {
					new LavaTrain(player, null);
					return;
				}

			}

			 if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Fire")) {

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
			}

			 if (!EntityTools.isWeapon(player.getItemInHand().getType())
					|| ConfigManager.useWeapon.get("Water")) {

				if (ability == Abilities.WaterManipulation) {
					WaterManipulation.moveWater(player);
					return;
				}

				if (ability == Abilities.IceSpike) {
					IceSpike2.activate(player);
					return;
				}

				/*
				 * if (ability == Abilities.WaterBubble) { new
				 * WaterBubble(player, null); }
				 */

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

			 if (!EntityTools.isWeapon(player.getItemInHand().getType())) {

				if (ability == Abilities.RapidPunch) {
					new RapidPunch(player, null);
					return;
				}

				if (ability == Abilities.Paralyze) {
					Entity t = EntityTools.getTargettedEntity(player,
							ConfigManager.rapidPunchDistance);
					new Paralyze(player, t, null);
					return;
				}

				if (ability == Abilities.SmokeBomb) {
					new SmokeBomb(player, null);
					return;
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
		if (player.isSneaking() && EntityTools.canBend(player, ability)) {
			if (!(EntityTools.isWeapon(player.getItemInHand().getType()))
					|| ConfigManager.useWeapon.get("Air")) {
				if (ability == Abilities.AirBurst) {
					new AirBurstSphere(player, null);
				}
			}
			if (ability == Abilities.Shockwave) {
				new ShockwaveArea(player, null);
			}
			if (ability == Abilities.FireBurst) {
				new FireBurstSphere(player, null);
			}
		}
		if (!player.isSneaking() && EntityTools.canBend(player, ability)) {

			if (ability == Abilities.AirShield) {
				new AirShield(player, null);
				return;
			}

			if (!(EntityTools.isWeapon(player.getItemInHand().getType()))
					|| ConfigManager.useWeapon.get("Air")) {

				if (ability == Abilities.AirSuction) {
					AirSuction.setOrigin(player);
				}

				if (ability == Abilities.AirBurst) {
					new AirBurst(player, null);
				}

				if (ability == Abilities.AirSwipe) {
					AirSwipe.charge(player);
				}
				
				if (ability == Abilities.Suffocate) {
					new Suffocate(player, null);
					return;
				}
			}
			if (ability == Abilities.AstralProjection) {
				new AstralProjection(player, true);
				return;
			}

			if (ability == Abilities.Tornado) {
				new Tornado(player, null);
			}

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

			if (!EntityTools.isWeapon(player.getItemInHand().getType())) {
				if (ability == Abilities.Dash) {
					new Dash(player, null);
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
			
			Player player = (Player) event.getEntity();
			Abilities ability = EntityTools.getBendingAbility(player);
			
			if (AstralProjection.isAstralProjecting(player)) {
				event.setDamage(0);
				event.setCancelled(true);
			}
			
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
						event.setDamage((int) ((double) event.getDamage() * (ConfigManager.falldamagereduction / 100.)));
						if (event.getEntity().getFallDistance() < 10) {
							event.setCancelled(true);
						}
					}
				}
				
				if(!event.isCancelled()) {
					if(Tornado.preventFall(player)) {
						event.setCancelled(true);
					}
				}
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
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (Paralyze.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}
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
				player.setVelocity(new Vector(0, 0, 0));
			// return;
		}
		if (Dash.isDashing(player)) {
			Vector dir = event.getTo().subtract(event.getFrom()).toVector();
			Dash d = Dash.getDash(player);
			d.setDirection(dir);
			d.dash();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		if(Bloodbending.isBloodbended(event.getPlayer())) {
			event.setCancelled(true);
			event.setReason(null);
		}
		if(Tornado.isAffected(event.getPlayer())) {
			event.setCancelled(true);
			event.setReason(null);
		}
	}
	
	@EventHandler
	public void onPlayerDropitem(PlayerDropItemEvent event) {
		if (AstralProjection.isAstralProjecting(event.getPlayer())) {
			event.setCancelled(true);
		}
		if(FireBlade.isFireBlade(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
		if(Suffocate.isTempHelmet(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnPlayerPickUpItem(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		if (AstralProjection.isAstralProjecting(p)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnPlayerEditBook(PlayerEditBookEvent e) {
		Player p = e.getPlayer();
		
		if (AstralProjection.isAstralProjecting(p)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void OnPlayerConsumeItem(PlayerItemConsumeEvent e) {
		Player p = e.getPlayer();
		if (AstralProjection.isAstralProjecting(p)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlotType() == SlotType.ARMOR
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
		
		if (AstralProjection.isAstralProjecting(event.getPlayer())) {
			AstralProjection.getAstralProjection(event.getPlayer()).removeEffect();
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

package net.avatarrealms.minecraft.bending.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {

	public static boolean enabled = true;
	public static boolean bendToItem = false;
	public static boolean colors = true;
	public static boolean compatibility = false;

	public static String chat = "<name>: <message>";

	public static Map<String, String> prefixes = new HashMap<String, String>();
	public static Map<String, String> color = new HashMap<String, String>();
	public static List<String> earthbendable = new LinkedList<String>();
	public static Map<String, Boolean> useWeapon = new HashMap<String, Boolean>();

	public static int seaLevel = 63;

	public static long globalCooldown = 500;

	public static long chiblockduration = 3000;
	public static double dodgechance = 10;
	public static double punchdamage = 2;
	public static double falldamagereduction = 1;

	public static boolean reverseearthbending = true;
	public static boolean safeRevert = true;
	public static long revertchecktime = 120000;

	public static boolean useTagAPI = true;

	private static List<String> defaultearthbendable = new LinkedList<String>();
	static {
		defaultearthbendable.add("STONE");
		defaultearthbendable.add("CLAY");
		defaultearthbendable.add("COAL_ORE");
		defaultearthbendable.add("DIAMOND_ORE");
		defaultearthbendable.add("DIRT");
		defaultearthbendable.add("GOLD_ORE");
		defaultearthbendable.add("EMERALD_ORE");
		defaultearthbendable.add("GRASS");
		defaultearthbendable.add("GRAVEL");
		defaultearthbendable.add("IRON_ORE");
		defaultearthbendable.add("LAPIS_ORE");
		defaultearthbendable.add("REDSTONE_ORE");
		defaultearthbendable.add("SAND");
		defaultearthbendable.add("SANDSTONE");
		defaultearthbendable.add("GLOWING_REDSTONE_ORE");
		defaultearthbendable.add("MYCEL");
	}
	
	// Air
	// AirBlast
	public static double airBlastSpeed = 25;
	public static double airBlastRange = 20;
	public static double airBlastRadius = 2;
	public static double airBlastPush = 3.0;
	
	//AirBurst

	// AirBubble
	public static int airBubbleRadius = 4;

	// AirPassive
	public static float airPassiveFactor = 0.3F;

	// AirScooter
	public static double airScooterSpeed = 0.675;

	// AirShield
	public static double airShieldRadius = 5;

	// AirSpout
	public static double airSpoutHeight = 20;

	// AirSuction
	public static double airSuctionSpeed = 25;
	public static double airSuctionRange = 20;
	public static double airSuctionRadius = 2;
	public static double airSuctionPush = 3.5;

	// AirSwipe
	public static int airSwipeDamage = 4;
	public static double airSwipeRange = 16;
	public static int airSwipeArc = 20;
	public static double airSwipeSpeed = 25;
	public static double airSwipeRadius = 2;
	public static double airSwipePush = 1;
	public static long airSwipeCooldown = 1500;

	// Tornado
	public static double tornadoRadius = 10;
	public static double tornadoHeight = 25;
	public static double tornadoRange = 25;
	public static double tornadoMobPush = 1;
	public static double tornadoPlayerPush = 1;
	
	// Air Manipulation
	public static int airManipulationDamage = 2;
	public static int airManipulationRange = 25;
	
	// Lungs control
	
	// ChiBlocker
	// HighJump
	public static double jumpHeight = 1.5;
	public static long highJumpCooldown = 4000;

	// Paralyze
	public static long paralyzeCooldown = 10000;
	public static long paralyzeDuration = 2500;

	// RapidPunch
	public static int rapidPunchDamage = 2;
	public static int rapidPunchDistance = 1;
	public static long rapidPunchCooldown = 3000;
	public static int rapidPunchPunches = 4;
	
	//Smoke Bomb
	public static int smokeRadius = 6;
	public static int smokeDuration = 10;    // time in secs
	public static int smokeBombCooldown = 6000;
	
	//Poisonned dart
	public static int dartRange = 10;
	public static int dartDamage = 2;
	public static int poisonLevel = 1;
	public static int poisonnedDartCooldown = 1500;
	
	//Dash
	public static int dashCooldown = 6000;
	public static double dashLength = 1.90; // Not really accurate as it's used with the setVelocity()
	public static double dashHeight = 0.70;
	

	// Earth
	// Catapult
	public static int catapultLength = 7;
	public static double catapultSpeed = 12;
	public static double catapultPush = 5;
	
	//ShockWave

	// Collapse
	public static int collapseRange = 20;
	public static double collapseRadius = 7;

	// CompactColumn
	public static double compactColumnRange = 20;
	public static double compactColumnSpeed = 8;

	// EarthArmor
	public static long earthArmorDuration = 60000;
	public static int earthArmorStrength = 2;
	public static long earthArmorCooldown = 60000;

	// EarthBlast
	public static int earthBlastDamage = 3;
	public static int ironBlastDamage = 5;
	public static boolean earthBlastHitSelf = false;
	public static double earthBlastPrepareRange = 10;
	public static double earthBlastRange = 20;
	public static double earthBlastSpeed = 35;
	public static boolean earthBlastRevert = true;
	public static double earthBlastPush = 0.3;

	// EarthColumn
	public static int earthColumnHeight = 6;

	// EarthGrab
	public static double earthGrabRange = 15;
	public static int earthGrabDuration = 150000;
	public static long earthGrabCooldown = 15000;

	// EarthPassive
	public static long earthPassive = 3000;
	public static int passiveResistanceLevel = 0;

	// EarthTunnel
	public static double earthTunnelMaxRadius = 1;
	public static double earthTunnelRange = 10;
	public static double earthTunnelRadius = 0.25;
	public static long earthTunnelInterval = 30;
	public static boolean earthTunnelRevert = true;

	// EarthWall
	public static int earthWallRange = 15;
	public static int earthWallHeight = 6;
	public static int earthWallWidth = 6;

	// Tremorsense
	public static long tremorsenseCooldown = 1000;
	public static int tremorsenseMaxDepth = 10;
	public static int tremorsenseRadius = 5;
	public static byte tremorsenseLightThreshold = 7;
	
	//MetalBending
	
	//EarthMelt
	
	public static long earthMeltCooldown = 15000;
	public static int earthMeltRange = 10;
	public static int earthMeltChargeTime = 3000; 

	//LavaBlast
	public static int lavaBlastDamage = 10;
	public static int lavaBlastRange = 15;
	public static int lavaBlastChargeTime = 1000; // time in millisecs.
	
	
	// Fire
	// ArcOfFire
	public static int arcOfFireArc = 20;
	public static int arcOfFireRange = 9;

	// Extinguish
	public static double extinguishRange = 20;
	public static double extinguishRadius = 7;

	// Fireball
	public static long fireballCooldown = 300000;
	public static double fireballSpeed = 0.3;

	// FireBlast
	public static double fireBlastSpeed = 15;
	public static double fireBlastRange = 25;
	public static double fireBlastRadius = 2;
	public static double fireBlastPush = .3;
	public static int fireBlastDamage = 7;
	public static long fireBlastCooldown = 1500;
	public static boolean fireBlastDissipate = false;
	
	//FireBurst
	
	//FireShield

	// FireJet
	public static double fireJetSpeed = 0.7;
	public static long fireJetDuration = 1500;
	public static long fireJetCooldown = 6000;

	// FireStream
	public static double fireStreamSpeed = 15;
	public static long dissipateAfter = 400;

	// HeatMelt
	public static int heatMeltRange = 15;
	public static int heatMeltRadius = 5;

	// Illumination
	public static int illuminationRange = 5;

	// Lightning
	public static long lightningWarmup = 3500;
	public static int lightningDamage = 6;
	public static int lightningRange = 50;
	public static double lightningMissChance = 5;

	// RingOfFire
	public static int ringOfFireRange = 7;

	// WallOfFire
	public static int wallOfFireRange = 4;
	public static int wallOfFireHeight = 4;
	public static int wallOfFireWidth = 4;
	public static long wallOfFireDuration = 5000;
	public static int wallOfFireDamage = 2;
	public static long wallOfFireInterval = 500;
	public static long wallOfFireCooldown = 7500;
	
	//FireBlade
	public static int fireBladeFireAspectLevel = 2;
	public static int fireBladeSharpnessLevel = 1;
	public static int fireBladeStrengthLevel = 1;
	public static int fireBladeCooldown = 60000;
	public static int fireBladeDuration = 30;
	
	//Combustion
	public static double combustionRadius = 1.5f;
	public static double combustionExplosionRadius = 3;
	public static double combustionInnerRadius = 3;
	public static int combustionDamage = 9;
	public static int combustionRange = 20;
	public static int combustionChargeTime = 2000; //time in millisecs
	public static int combustionCooldown = 2000;
	
	

	// Day
	public static double dayFactor = 1.5;

	// Water
	// Bloodbending
	public static double bloodbendingThrowFactor = 2;
	public static int bloodbendingRange = 10;

	// FastSwimming
	public static double fastSwimmingFactor = 0.7;

	// FreezeMelt
	public static int freezeMeltRange = 20;
	public static int freezeMeltRadius = 3;
	public static int freezeMeltDepth = 1;

	// HealingWaters
	public static double healingWatersRadius = 5;
	public static long healingWatersInterval = 750;

	// IceSpike
	public static long icespikeCooldown = 2000;
	public static int icespikeDamage = 2;
	public static int icespikeRange = 20;
	public static double icespikeThrowingMult = 0.7;

	// Plantbending
	public static long plantbendingRegrowTime = 180000;

	// SpikeField
	public static long icespikeAreaCooldown = 3000;
	public static int icespikeAreaDamage = 2;
	public static int icespikeAreaRadius = 6;
	public static double icespikeAreaThrowingMult = 1;
	
	// WaterBubble
	public static double waterBubbleRadius = airBubbleRadius;

	// WaterManipulation
	public static int waterManipulationDamage = 3;
	public static double waterManipulationRange = 25;
	public static double waterManipulationSpeed = 35;
	public static double WaterManipulationPush = .3;

	// WaterSpout
	public static int waterSpoutHeight = 16;
	public static int waterSpoutRotationSpeed = 4;

	// WaterWall
	public static double waterWallRange = 5;
	public static double waterWallRadius = 2;

	// Wave
	public static double waveRadius = 3;
	public static double waveHorizontalPush = 1;
	public static double waveVerticalPush = 0.2;
	
	//Torrent
	public static int torrentDamage = 2;
	public static int torrentDeflectDamage = 1;
	
	//OctopusForm
	public static int octopusFormDamage = 3;
	
	//IceSwipe
	public static int iceSwipeDamage = 4;
	public static int iceSwipeRange = 25;
	public static double iceSwipeSpeed = 25;
	public static double iceSwipePush = 1;
	public static long iceSwipeCooldown = 1500;

	// Night
	public static double nightFactor = 1.5;

	public void load(File file) {
		FileConfiguration config = new YamlConfiguration();
		try {
			if (file.exists())
				config.load(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		// Chat:
		enabled = config.getBoolean("Chat.Enabled", enabled);
		colors = config.getBoolean("Chat.Colors", colors);
		compatibility = config.getBoolean("Chat.Compatibility", compatibility);

		chat = config.getString("Chat.Format", chat);

		// Prefix
		prefixes.put("Air", config.getString("Chat.Prefix.Air", "[Airbender] "));
		prefixes.put("Avatar",
				config.getString("Chat.Prefix.Avatar", "[Avatar] "));
		prefixes.put("Fire",
				config.getString("Chat.Prefix.Fire", "[Firebender ]"));
		prefixes.put("Water",
				config.getString("Chat.Prefix.Water", "[Waterbender] "));
		prefixes.put("Earth",
				config.getString("Chat.Prefix.Earth", "[Earthbender] "));
		prefixes.put("ChiBlocker",
				config.getString("Chat.Prefix.ChiBlocker", "[Chiblocker] "));

		// Color
		color.put("Avatar",
				config.getString("Chat.Color.Avatar", "DARK_PURPLE"));
		color.put("Air", config.getString("Chat.Color.Air", "GRAY"));
		color.put("Fire", config.getString("Chat.Color.Fire", "RED"));
		color.put("Water", config.getString("Chat.Color.Water", "AQUA"));
		color.put("Earth", config.getString("Chat.Color.Earth", "GREEN"));
		color.put("ChiBlocker",
				config.getString("Chat.Color.ChiBlocker", "GOLD"));

		// Bending

		// Option
		earthbendable = defaultearthbendable;
		if (config.contains("Bending.Option.EarthBendable"))
			earthbendable = config
					.getStringList("Bending.Option.EarthBendable");
		

		// EarthBendable
		useWeapon
				.put("Air", config.getBoolean(
						"Bending.Option.Bend-With-Weapon.Air", false));
		useWeapon.put("Earth", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.Earth", false));
		useWeapon.put("Fire", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.Fire", false));
		useWeapon.put("Water", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.Water", false));
		useWeapon.put("ChiBlocker", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.ChiBlocker", false));

		bendToItem = config.getBoolean("Bending.Option.Bend-To-Item",
				bendToItem);
		useTagAPI = config.getBoolean("Bending.Option.Use-TagAPI", useTagAPI);
		reverseearthbending = config.getBoolean(
				"Bending.Option.Reverse-Earthbending", reverseearthbending);
		revertchecktime = config.getLong(
				"Bending.Option.Reverse-Earthbending-Check-Time",
				revertchecktime);
		safeRevert = config
				.getBoolean("Bending.Option.Safe-Revert", safeRevert);
		dissipateAfter = config.getLong(
				"Bending.Option.Firebending-Dissipate-Time", dissipateAfter);
		seaLevel = config.getInt("Bending.Option.Sea-Level", seaLevel);

		// Properties
		// ChiBlocker
		chiblockduration = config.getLong(
				"Properties.ChiBlocker.ChiBlock-Duration", chiblockduration);
		dodgechance = config.getDouble("Properties.ChiBlocker.Dodge-Chance",
				dodgechance);
		punchdamage = config.getDouble("Properties.ChiBlocker.Punch-Damage",
				punchdamage);
		falldamagereduction = config.getDouble(
				"Properties.ChiBlocker.Fall-Damage-Reduction",
				falldamagereduction);

		globalCooldown = config.getLong("Properties.GlobalCooldown",
				globalCooldown);

		// HighJump
		jumpHeight = config.getDouble("Properties.ChiBlocker.HighJump.Height",
				jumpHeight);
		highJumpCooldown = config.getLong(
				"Properties.ChiBlocker.HighJump.Cooldown", highJumpCooldown);

		// Paralyze
		paralyzeCooldown = config.getLong(
				"Properties.ChiBlocker.Paralyze.Cooldown", paralyzeCooldown);
		paralyzeDuration = config.getLong(
				"Properties.ChiBlocker.Paralyze.Duration", paralyzeDuration);

		// RapidPunch
		rapidPunchDamage = config.getInt(
				"Properties.ChiBlocker.RapidPunch.Damage", rapidPunchDamage);
		rapidPunchDistance = config
				.getInt("Properties.ChiBlocker.RapidPunch.Distance",
						rapidPunchDistance);
		rapidPunchCooldown = config
				.getLong("Properties.ChiBlocker.RapidPunch.Cooldown",
						rapidPunchCooldown);
		rapidPunchPunches = config.getInt(
				"Properties.ChiBlocker.RapidPunch.Punches", rapidPunchPunches);
		
		//Poisonned Dart
		dartRange = config.getInt("Properties.ChiBlocker.PoisonnedDart.Range",dartRange);
		dartDamage = config.getInt("Properties.ChiBlocker.PoisonnedDart.Damage",dartDamage);
		poisonLevel = config.getInt("Properties.ChiBlocker.PoisonnedDart.poison-level",poisonLevel);
		poisonnedDartCooldown = config.getInt("Properties.ChiBlocker.PoisonnedDart.Cooldown",poisonnedDartCooldown);
		
		//Smoke bomb
		smokeRadius = config.getInt("Properties.ChiBlocker.SmokeBomb.Radius",smokeRadius);
		smokeDuration = config.getInt("Properties.ChiBlocker.SmokeBomb.Duration",smokeDuration);
		smokeBombCooldown = config.getInt("Properties.ChiBlocker.SmokeBomb.Cooldown",smokeBombCooldown);
		
		//Dash
		dashCooldown = config.getInt("Properties.ChiBlocker.Dash.Cooldown",dashCooldown);
		dashLength = config.getDouble("Properties.ChiBlocker.Dash.Length", dashLength);
		dashHeight = config.getDouble("Properties.ChiBlocker.Dash.Height",dashHeight);

		// Air
		// AirBlast
		airBlastSpeed = config.getDouble("Properties.Air.AirBlast.Speed",
				airBlastSpeed);
		airBlastRange = config.getDouble("Properties.Air.AirBlast.Range",
				airBlastRange);
		airBlastRadius = config.getDouble(
				"Properties.Air.AirBlast.Affecting-Radius", airBlastRadius);
		airBlastPush = config.getDouble("Properties.Air.AirBlast.Push-Factor",
				airBlastPush);
		
		//AirBurst

		// AirBubble
		airBubbleRadius = config.getInt("Properties.Air.AirBubble.Radius",
				airBubbleRadius);

		// AirPassive
		airPassiveFactor = (float) config.getDouble(
				"Properties.Air.Passive.Factor", airPassiveFactor);

		// AirShield
		airShieldRadius = config.getDouble("Properties.Air.AirShield.Radius",
				airShieldRadius);

		// AirSuction
		airSuctionSpeed = config.getDouble("Properties.Air.AirSuction.Speed",
				airSuctionSpeed);
		airSuctionRange = config.getDouble("Properties.Air.AirSuction.Range",
				airSuctionRange);
		airSuctionRadius = config.getDouble(
				"Properties.Air.AirSuction.Affecting-Radius", airSuctionRadius);
		airSuctionPush = config.getDouble(
				"Properties.Air.AirSuction.Push-Factor", airSuctionPush);

		// AirSwipe
		airSwipeDamage = config.getInt("Properties.Air.AirSwipe.Damage", airSwipeDamage);
		airSwipeRange = config.getDouble("Properties.Air.AirSwipe.Range",
				airSwipeRange);
		airSwipeArc = config.getInt("Properties.Air.AirSwipe.Arc", airSwipeArc);
		airSwipeSpeed = config.getDouble("Properties.Air.AirSwipe.Speed",
				airSwipeSpeed);
		airSwipeRadius = config.getDouble(
				"Properties.Air.AirSwipe.Affecting-Radius", airSwipeRadius);
		airSwipePush = config.getDouble("Properties.Air.AirSwipe.Push-Factor",
				airSwipePush);
		airSwipeCooldown = config.getLong("Properties.Air.AirSwipe.Cooldown",
				airSwipeCooldown);

		// Tornado
		tornadoRadius = config.getDouble("Properties.Air.Tornado.Radius",
				tornadoRadius);
		tornadoHeight = config.getDouble("Properties.Air.Tornado.Height",
				tornadoHeight);
		tornadoRange = config.getDouble("Properties.Air.Tornado.Range",
				tornadoRange);
		tornadoMobPush = config.getDouble(
				"Properties.Air.Tornado.Mob-Push-Factor", tornadoMobPush);
		tornadoPlayerPush = config.getDouble(
				"Properties.Air.Tornado.Player-Push-Factor", tornadoPlayerPush);

		// Air Scooter
		airScooterSpeed = config.getDouble("Properties.Air.AirScooter.Speed",
				airScooterSpeed);

		// Air Spout
		airSpoutHeight = config.getDouble("Properties.Air.AirSpout.Height",
				airSpoutHeight);
		
		//Air Manipulation
		airManipulationDamage = config.getInt("Properties.Air.AirManipulation.Damage",airManipulationDamage);
		airManipulationRange = config.getInt("Properties.Air.AirManipulation.Range",airManipulationRange);
		//Lungs Control
		
		// Earth
		// Catapult
		catapultLength = config.getInt("Properties.Earth.Catapult.Length",
				catapultLength);
		catapultSpeed = config.getDouble("Properties.Earth.Catapult.Speed",
				catapultSpeed);
		catapultPush = config.getDouble("Properties.Earth.Catapult.Push",
				catapultPush);
		
		//ShockWave

		// CompactColumn
		compactColumnRange = config.getDouble(
				"Properties.Earth.CompactColumn.Range", compactColumnRange);
		compactColumnSpeed = config.getDouble(
				"Properties.Earth.CompactColumn.Speed", compactColumnSpeed);

		// EarthBlast
		earthBlastDamage = config.getInt("Properties.Earth.EarthBlast.Damage", earthBlastDamage);
		earthBlastHitSelf = config.getBoolean(
				"Properties.Earth.EarthBlast.Hit-Self", earthBlastHitSelf);
		earthBlastPrepareRange = config.getDouble(
				"Properties.Earth.EarthBlast.Prepare-Range",
				earthBlastPrepareRange);
		earthBlastRange = config.getDouble("Properties.Earth.EarthBlast.Range",
				earthBlastRange);
		earthBlastSpeed = config.getDouble("Properties.Earth.EarthBlast.Speed",
				earthBlastSpeed);
		earthBlastRevert = config.getBoolean(
				"Properties.Earth.EarthBlast.Revert", earthBlastRevert);
		earthBlastPush = config.getDouble("Properties.Earth.EarthBlast.Push", earthBlastPush);
		
		//IronBlast
		ironBlastDamage = config.getInt("Properties.Earth.IronBlast.Damage",ironBlastDamage);
				

		// EarthColumn
		earthColumnHeight = config.getInt(
				"Properties.Earth.EarthColumn.Height", earthColumnHeight);

		// EarthGrab
		earthGrabRange = config.getDouble("Properties.Earth.EarthGrab.Range",
				earthGrabRange);
		earthGrabDuration = config.getInt("Properties.Earth.EarthGrab.Duration",
				earthGrabDuration);
		earthGrabCooldown = config.getLong("Properties.Earth.EarthGrab.Cooldown",
				earthGrabCooldown);

		// EarthPassive
		earthPassive = config.getLong(
				"Properties.Earth.EarthPassive.Wait-Before-Reverse-Changes",
				earthPassive);
		passiveResistanceLevel = config.getInt("Properties.Earth.EarthPassive.resistance-level",passiveResistanceLevel);

		// EarthTunnel
		earthTunnelMaxRadius = config
				.getDouble("Properties.Earth.EarthTunnel.Max-Radius",
						earthTunnelMaxRadius);
		earthTunnelRange = config.getDouble(
				"Properties.Earth.EarthTunnel.Range", earthTunnelRange);
		earthTunnelRadius = config.getDouble(
				"Properties.Earth.EarthTunnel.Radius", earthTunnelRadius);
		earthTunnelInterval = config.getLong(
				"Properties.Earth.EarthTunnel.Interval", earthTunnelInterval);
		earthTunnelRevert = config.getBoolean(
				"Properties.Earth.EarthTunnel.Revert", earthTunnelRevert);

		// EarthWall
		earthWallRange = config.getInt("Properties.Earth.EarthWall.Range",
				earthWallRange);
		earthWallHeight = config.getInt("Properties.Earth.EarthWall.Height",
				earthWallHeight);
		earthWallWidth = config.getInt("Properties.Earth.EarthWall.Width",
				earthWallWidth);

		// Collapse
		collapseRange = config.getInt("Properties.Earth.Collapse.Range",
				collapseRange);
		collapseRadius = config.getDouble("Properties.Earth.Collapse.Radius",
				collapseRadius);

		// Tremorsense
		tremorsenseCooldown = config.getLong(
				"Properties.Earth.Tremorsense.Cooldown", tremorsenseCooldown);
		tremorsenseMaxDepth = config.getInt(
				"Properties.Earth.Tremorsense.Max-Depth", tremorsenseMaxDepth);
		tremorsenseRadius = config.getInt(
				"Properties.Earth.Tremorsense.Radius", tremorsenseRadius);
		tremorsenseLightThreshold = (byte) config.getInt(
				"Properties.Earth.Tremorsense.Light-Threshold",
				tremorsenseLightThreshold);

		// EarthArmor
		earthArmorDuration = config.getLong(
				"Properties.Earth.EarthArmor.Duration", earthArmorDuration);
		earthArmorStrength = config.getInt(
				"Properties.Earth.EarthArmor.Strength", earthArmorStrength);
		earthArmorCooldown = config.getLong(
				"Properties.Earth.EarthArmor.Cooldown", earthArmorCooldown);
		
		//Iron Armor
		
		//MetalBending
		
		//EarthMelt
		earthMeltCooldown = config.getLong("Properties.Earth.EarthMelt.Cooldown",earthMeltCooldown);
		earthMeltRange = config.getInt("Properties.Earth.EarthMelt.Range",earthMeltRange);
		earthMeltChargeTime = config.getInt("Properties.Earth.EarthMelt.ChargeTime",earthMeltChargeTime);
		
		
		//LavaBlast
		lavaBlastRange = config.getInt("Properties.Earth.LavaBlast.Range",lavaBlastRange);
		lavaBlastChargeTime = config.getInt("Properties.Earth.LavaBlast.ChargeTime",lavaBlastChargeTime);
		lavaBlastDamage = config.getInt("Properties.Earth.LavaBlast.Damage",lavaBlastDamage);
		
		// Fire
		// FireBlast
		fireBlastRange = config.getDouble("Properties.Fire.FireBlast.Range",
				fireBlastRange);
		fireBlastSpeed = config.getDouble("Properties.Fire.FireBlast.Speed",
				fireBlastSpeed);
		fireBlastPush = config.getDouble("Properties.Fire.FireBlast.Push",
				fireBlastPush);
		fireBlastRadius = config.getDouble("Properties.Fire.FireBlast.Radius",
				fireBlastRadius);
		fireBlastCooldown = config.getLong(
				"Properties.Fire.FireBlast.Cooldown", fireBlastCooldown);
		fireBlastDamage = config.getInt("Properties.Fire.FireBlast.Damage",
				fireBlastDamage);
		fireBlastDissipate = config.getBoolean(
				"Properties.Fire.FireBlast.Dissipates", fireBlastDissipate);
		
		//FireBurst
		
		//FireShield
		
		// ArcOfFire
		arcOfFireArc = config.getInt("Properties.Fire.ArcOfFire.Arc",
				arcOfFireArc);
		arcOfFireRange = config.getInt("Properties.Fire.ArcOfFire.Range",
				arcOfFireRange);

		// RingOfFire
		ringOfFireRange = config.getInt("Properties.Fire.RingOfFire.Range",
				ringOfFireRange);

		// Extinguish
		extinguishRange = config.getDouble("Properties.Fire.Extinguish.Range",
				extinguishRange);
		extinguishRadius = config.getDouble(
				"Properties.Fire.Extinguish.Radius", extinguishRadius);

		// Fireball
		fireballCooldown = config.getLong("Properties.Fire.Fireball.Cooldown",
				fireballCooldown);
		fireballSpeed = config.getDouble("Properties.Fire.Fireball.Speed",
				fireballSpeed);

		// FireJet
		fireJetSpeed = config.getDouble("Properties.Fire.FireJet.Speed",
				fireJetSpeed);
		fireJetDuration = config.getLong("Properties.Fire.FireJet.Duration",
				fireJetDuration);
		fireJetCooldown = config.getLong("Properties.Fire.FireJet.CoolDown",
				fireJetCooldown);

		// FireStream
		fireStreamSpeed = config.getDouble("Properties.Fire.FireStream.Speed",
				fireStreamSpeed);

		// WallOfFire
		wallOfFireRange = config.getInt("Properties.Fire.WallOfFire.Range",
				wallOfFireRange);
		wallOfFireHeight = config.getInt("Properties.Fire.WallOfFire.Height",
				wallOfFireHeight);
		wallOfFireWidth = config.getInt("Properties.Fire.WallOfFire.Width",
				wallOfFireWidth);
		wallOfFireDuration = config.getLong(
				"Properties.Fire.WallOfFire.Duration", wallOfFireDuration);
		wallOfFireDamage = config.getInt("Properties.Fire.WallOfFire.Damage",
				wallOfFireDamage);
		wallOfFireInterval = config.getLong(
				"Properties.Fire.WallOfFire.Interval", wallOfFireInterval);
		wallOfFireCooldown = config.getLong(
				"Properties.Fire.WallOfFire.Cooldown", wallOfFireCooldown);

		// HeatMelt
		heatMeltRange = config.getInt("Properties.Fire.HeatMelt.Range",
				heatMeltRange);
		heatMeltRadius = config.getInt("Properties.Fire.HeatMelt.Radius",
				heatMeltRadius);

		// Illumination
		illuminationRange = config.getInt("Properties.Fire.Illumination.Range",
				illuminationRange);

		// Lightning
		lightningWarmup = config.getLong("Properties.Fire.Lightning.Warmup",
				lightningWarmup);
		lightningDamage = config.getInt("Properties.Fire.Lightning.Damage",
				lightningDamage);
		lightningRange = config.getInt("Properties.Fire.Lightning.Range",
				lightningRange);
		lightningMissChance = config.getDouble(
				"Properties.Fire.Lightning.Miss-Chance", lightningMissChance);
		
		//FireBlade
		fireBladeSharpnessLevel = config.getInt(
				"Properties.Fire.FireBlade.sharpness-level", fireBladeSharpnessLevel);
		fireBladeFireAspectLevel = config.getInt("Properties.Fire.FireBlade.fireaspect-level",
							fireBladeFireAspectLevel);
		fireBladeDuration = config.getInt("Properties.Fire.FireBlade.Duration", fireBladeDuration);
		fireBladeStrengthLevel = config.getInt("Properties.Fire.FireBlade.strength-level", fireBladeStrengthLevel);
		fireBladeCooldown = config.getInt("Properties.Fire.FireBlade.Cooldown", fireBladeCooldown);
		
		//Combustion
		combustionDamage = config.getInt("Properties.Fire.Combustion.Damage",
							combustionDamage);
		combustionRange = config.getInt("Properties.Fire.Combustion.Range",
							combustionRange);
		combustionRadius = config.getDouble("Properties.Fire.Combustion.Radius", 
							combustionRadius);
		combustionExplosionRadius = config.getDouble("Properties.Fire.Combustion.Explosion-Radius",
							combustionExplosionRadius);
		combustionInnerRadius = config.getDouble("Properties.Fire.Combustion.Inner-Radius",
				combustionInnerRadius);
		combustionChargeTime = config.getInt("Properties.Fire.Combustion.ChargeTime",
							combustionChargeTime);
		combustionCooldown = config.getInt("Properties.Fire.Combustion.Cooldown",
							combustionCooldown);

		// Day
		dayFactor = config.getDouble("Properties.Fire.Day-Power-Factor",
				dayFactor);

		// Water
		// Bloodbending
		bloodbendingThrowFactor = config.getDouble(
				"Properties.Water.Bloodbending.Throw-Factor",
				bloodbendingThrowFactor);
		bloodbendingRange = config.getInt(
				"Properties.Water.Bloodbending.Range", bloodbendingRange);
		
		//WaterBubble

		// FreezeMelt
		freezeMeltRange = config.getInt("Properties.Water.FreezeMelt.Range",
				freezeMeltRange);
		freezeMeltRadius = config.getInt("Properties.Water.FreezeMelt.Radius",
				freezeMeltRadius);
		freezeMeltDepth = config.getInt("Properties.Water.FreezeMelt.Depth",
				freezeMeltDepth);

		// HealingWaters
		healingWatersRadius = config.getDouble(
				"Properties.Water.HealingWaters.Radius", healingWatersRadius);
		healingWatersInterval = config.getLong(
				"Properties.Water.HealingWaters.Interval",
				healingWatersInterval);

		// Plantbending
		plantbendingRegrowTime = config.getLong(
				"Properties.Water.Plantbending.Regrow-Time",
				plantbendingRegrowTime);

		// WaterManipulation
		waterManipulationDamage = config.getInt("Properties.Water.WaterManipulation.Damage", waterManipulationDamage);
		waterManipulationRange = config.getDouble(
				"Properties.Water.WaterManipulation.Range",
				waterManipulationRange);
		waterManipulationSpeed = config.getDouble(
				"Properties.Water.WaterManipulation.Speed",
				waterManipulationSpeed);
		WaterManipulationPush = config.getDouble("Properties.Water.WaterManipulation.Push",
				WaterManipulationPush);

		// WaterSpout
		waterSpoutHeight = config.getInt("Properties.Water.WaterSpout.Height",
				waterSpoutHeight);
		waterSpoutRotationSpeed = config.getInt("Properties.Water.WaterSpout.Rotation-Speed",
				waterSpoutRotationSpeed);

		// WaterWall
		waterWallRange = config.getDouble("Properties.Water.WaterWall.Range",
				waterWallRange);
		waterWallRadius = config.getDouble("Properties.Water.WaterWall.Radius",
				waterWallRadius);

		// Wave
		waveRadius = config.getDouble("Properties.Water.Wave.Radius",
				waveRadius);
		waveHorizontalPush = config.getDouble(
				"Properties.Water.Wave.Horizontal-Push-Force",
				waveHorizontalPush);
		waveVerticalPush = config.getDouble(
				"Properties.Water.Wave.Vertical-Push-Force", waveVerticalPush);

		// Fast Swimming
		fastSwimmingFactor = config.getDouble(
				"Properties.Water.FastSwimming.Factor", fastSwimmingFactor);

		// IceSpike
		icespikeCooldown = config.getLong("Properties.Water.IceSpike.Cooldown",
				icespikeCooldown);
		icespikeDamage = config.getInt("Properties.Water.IceSpike.Damage",
				icespikeDamage);
		icespikeRange = config.getInt("Properties.Water.IceSpike.Range",
				icespikeRange);
		icespikeThrowingMult = config.getDouble(
				"Properties.Water.IceSpike.ThrowingMult", icespikeThrowingMult);
		icespikeAreaCooldown = config.getLong(
				"Properties.Water.IceSpike.AreaCooldown", icespikeAreaCooldown);
		icespikeAreaDamage = config.getInt(
				"Properties.Water.IceSpike.AreaDamage", icespikeAreaDamage);
		icespikeAreaRadius = config.getInt(
				"Properties.Water.IceSpike.AreaRadius", icespikeAreaRadius);
		icespikeAreaThrowingMult = config.getDouble(
				"Properties.Water.IceSpike.AreaThrowingMult",
				icespikeAreaThrowingMult);
		
		//Torrent
		torrentDamage = config.getInt("Properties.Water.Torrent.Damage",torrentDamage);
		torrentDeflectDamage = config.getInt("Properties.Water.Torrent.DeflectDamage",torrentDeflectDamage);

		//OctopusForm
		octopusFormDamage = config.getInt("Properties.Water.OctopusForm.Damage",octopusFormDamage);
		
		//IceSwipe
		iceSwipeDamage = config.getInt("Properties.Water.IceSwipe.Damage", iceSwipeDamage);
		iceSwipeRange = config.getInt("Properties.Water.IceSwipe.Range",
				iceSwipeRange);
		iceSwipeSpeed = config.getDouble("Properties.Water.IceSwipe.Speed",
				iceSwipeSpeed);
		iceSwipePush = config.getDouble("Properties.Water.IceSwipe.Push-Factor",
				iceSwipePush);
		iceSwipeCooldown = config.getLong("Properties.Water.IceSwipe.Cooldown",
				iceSwipeCooldown);
		// Night
		nightFactor = config.getDouble("Properties.Water.Night-Power-Factor",
				nightFactor);

		// set defaults
		config.set("Chat.Enabled", enabled);
		config.set("Chat.Colors", colors);
		config.set("Chat.Compatibility", compatibility);
		config.set("Chat.Format", chat);
		// Prefix
		config.set("Chat.Prefix.Air", prefixes.get("Air"));
		config.set("Chat.Prefix.Fire", prefixes.get("Fire"));
		config.set("Chat.Prefix.Avatar", prefixes.get("Avatar"));
		config.set("Chat.Prefix.Water", prefixes.get("Water"));
		config.set("Chat.Prefix.Earth", prefixes.get("Earth"));
		config.set("Chat.Prefix.ChiBlocker", prefixes.get("ChiBlocker"));
		// Color
		config.set("Chat.Color.Avatar", color.get("Avatar"));
		config.set("Chat.Color.Air", color.get("Air"));
		config.set("Chat.Color.Fire", color.get("Fire"));
		config.set("Chat.Color.Earth", color.get("Earth"));
		config.set("Chat.Color.Water", color.get("Water"));
		config.set("Chat.Color.ChiBlocker", color.get("ChiBlocker"));
		// Bending
		// Option

		config.set("Bending.Option.Bend-To-Item", bendToItem);
		config.set("Bending.Option.Use-TagAPI", useTagAPI);
		config.set("Bending.Option.Reverse-Earthbending", reverseearthbending);
		config.set("Bending.Option.Reverse-Earthbending-Check-Time",
				revertchecktime);
		config.set("Bending.Option.Firebending-Dissipate-Time", dissipateAfter);
		config.set("Bending.Option.Sea-Level", seaLevel);

		// Properties
		// ChiBlocker
		config.set("Properties.ChiBlocker.ChiBlock-Duration", chiblockduration);
		config.set("Properties.ChiBlocker.Dodge-Chance", dodgechance);
		config.set("Properties.ChiBlocker.Punch-Damage", punchdamage);
		config.set("Properties.ChiBlocker.Fall-Damage-Reduction",
				falldamagereduction);
		config.set("Properties.GlobalCooldown", globalCooldown);
		// HighJump
		config.set("Properties.ChiBlocker.HighJump.Height", jumpHeight);
		config.set("Properties.ChiBlocker.HighJump.Cooldown", highJumpCooldown);
		// Paralyze
		config.set("Properties.ChiBlocker.Paralyze.Cooldown", paralyzeCooldown);
		config.set("Properties.ChiBlocker.Paralyze.Duration", paralyzeDuration);
		// RapidPunch
		config.set("Properties.ChiBlocker.RapidPunch.Damage", rapidPunchDamage);
		config.set("Properties.ChiBlocker.RapidPunch.Distance",
				rapidPunchDistance);
		config.set("Properties.ChiBlocker.RapidPunch.Cooldown",
				rapidPunchCooldown);
		config.set("Properties.ChiBlocker.RapidPunch.Punches",
				rapidPunchPunches);
		//SmokeBomb
		config.set("Properties.ChiBlocker.SmokeBomb.Radius", smokeRadius);
		config.set("Properties.ChiBlocker.SmokeBomb.Duration", smokeDuration);
		config.set("Properties.ChiBlocker.SmokeBomb.Cooldown", smokeBombCooldown);
		//Poisonned Dart
		config.set("Properties.ChiBlocker.PoisonnedDart.Range", dartRange);
		config.set("Properties.ChiBlocker.PoisonnedDart.Damage", dartDamage);
		config.set("Properties.ChiBlocker.PoisonnedDart.poison-level", poisonLevel);
		config.set("Properties.ChiBlocker.PoisonnedDart.Cooldown", poisonnedDartCooldown);	
		//Dash
		config.set("Properties.ChiBlocker.Dash.Cooldown", dashCooldown);
		config.set("Properties.ChiBlocker.Dash.Length", dashLength);
		config.set("Properties.ChiBlocker.Dash.Height", dashHeight);
		
		// Air
		// AirBlast
		config.set("Properties.Air.AirBlast.Speed", airBlastSpeed);
		config.set("Properties.Air.AirBlast.Range", airBlastRange);
		config.set("Properties.Air.AirBlast.Affecting-Radius", airBlastRadius);
		config.set("Properties.Air.AirBlast.Push-Factor", airBlastPush);
		//AirBurst
		// AirBubble
		config.set("Properties.Air.AirBubble.Radius", airBubbleRadius);
		// AirPassive
		config.set("Properties.Air.Passive.Factor", airPassiveFactor);
		// AirShield
		config.set("Properties.Air.AirShield.Radius", airShieldRadius);
		// AirSuction
		config.set("Properties.Air.AirSuction.Speed", airSuctionSpeed);
		config.set("Properties.Air.AirSuction.Range", airSuctionRange);
		config.set("Properties.Air.AirSuction.Affecting-Radius",
				airSuctionRadius);
		config.set("Properties.Air.AirSuction.Push-Factor", airSuctionPush);
		// AirSwipe
		config.set("Properties.Air.AirSwipe.Damage", airSwipeDamage);
		config.set("Properties.Air.AirSwipe.Range", airSwipeRange);
		config.set("Properties.Air.AirSwipe.Arc", airSwipeArc);
		config.set("Properties.Air.AirSwipe.Speed", airSwipeSpeed);
		config.set("Properties.Air.AirSwipe.Affecting-Radius", airSwipeRadius);
		config.set("Properties.Air.AirSwipe.Push-Factor", airSwipePush);
		config.set("Properties.Air.AirSwipe.Cooldown", airSwipeCooldown);
		// Tornado
		config.set("Properties.Air.Tornado.Radius", tornadoRadius);
		config.set("Properties.Air.Tornado.Height", tornadoHeight);
		config.set("Properties.Air.Tornado.Range", tornadoRange);
		config.set("Properties.Air.Tornado.Mob-Push-Factor", tornadoMobPush);
		config.set("Properties.Air.Tornado.Player-Push-Factor",
				tornadoPlayerPush);
		// Air Scooter
		config.set("Properties.Air.AirScooter.Speed", airScooterSpeed);
		// Air Spout
		config.set("Properties.Air.AirSpout.Height", airSpoutHeight);
		
		//Air manipulation
		config.set("Properties.Air.AirManipulation.Damage", airManipulationDamage);
		config.set("Properties.Air.AirManipulation.Range", airManipulationRange);
		//LungsControl
		// Earth
		// Catapult
		config.set("Properties.Earth.Catapult.Length", catapultLength);
		config.set("Properties.Earth.Catapult.Speed", catapultSpeed);
		config.set("Properties.Earth.Catapult.Push", catapultPush);
		
		// ShockWave
		// CompactColumn
		config.set("Properties.Earth.CompactColumn.Range", compactColumnRange);
		config.set("Properties.Earth.CompactColumn.Speed", compactColumnSpeed);
		// EarthBlast
		config.set("Properties.Earth.EarthBlast.Damage", earthBlastDamage);
		config.set("Properties.Earth.EarthBlast.Hit-Self", earthBlastHitSelf);
		config.set("Properties.Earth.EarthBlast.Prepare-Range",
				earthBlastPrepareRange);
		config.set("Properties.Earth.EarthBlast.Range", earthBlastRange);
		config.set("Properties.Earth.EarthBlast.Speed", earthBlastSpeed);
		config.set("Properties.Earth.EarthBlast.Revert", earthBlastRevert);
		config.set("Properties.Earth.EarthBlast.Push", earthBlastPush);
		//Iron Armor
		//IronBlast
		config.set("Properties.Earth.IronBlast.Damage", ironBlastDamage);
		// EarthColumn
		config.set("Properties.Earth.EarthColumn.Height", earthColumnHeight);
		// EarthGrab
		config.set("Properties.Earth.EarthGrab.Range", earthGrabRange);
		config.set("Properties.Earth.EarthGrab.Duration", earthGrabDuration);
		config.set("Properties.Earth.EarthGrab.Cooldown", earthGrabCooldown);
		
		// EarthPassive
		config.set("Properties.Earth.EarthPassive.Wait-Before-Reverse-Changes",
				earthPassive);
		config.set("Properties.Earth.EarthPassive.resistance-level", passiveResistanceLevel);
		
		// EarthTunnel
		config.set("Properties.Earth.EarthTunnel.Max-Radius",
				earthTunnelMaxRadius);
		config.set("Properties.Earth.EarthTunnel.Range", earthTunnelRange);
		config.set("Properties.Earth.EarthTunnel.Radius", earthTunnelRadius);
		config.set("Properties.Earth.EarthTunnel.Interval", earthTunnelInterval);
		config.set("Properties.Earth.EarthTunnel.Revert", earthTunnelRevert);
		
		// EarthWall
		config.set("Properties.Earth.EarthWall.Range", earthWallRange);
		config.set("Properties.Earth.EarthWall.Height", earthWallHeight);
		config.set("Properties.Earth.EarthWall.Width", earthWallWidth);
		
		// Collapse
		config.set("Properties.Earth.Collapse.Range", collapseRange);
		config.set("Properties.Earth.Collapse.Radius", collapseRadius);
		// Tremorsense
		config.set("Properties.Earth.Tremorsense.Cooldown", tremorsenseCooldown);
		config.set("Properties.Earth.Tremorsense.Max-Depth",
				tremorsenseMaxDepth);
		config.set("Properties.Earth.Tremorsense.Radius", tremorsenseRadius);
		config.set("Properties.Earth.Tremorsense.Light-Threshold",
				tremorsenseLightThreshold);
		
		// EarthArmor
		config.set("Properties.Earth.EarthArmor.Duration", earthArmorDuration);
		config.set("Properties.Earth.EarthArmor.Strength", earthArmorStrength);
		config.set("Properties.Earth.EarthArmor.Cooldown", earthArmorCooldown);
		
		//MetalBending
		
		
		//EarthMelt
		config.set("Properties.Earth.EarthMelt.Cooldown", earthMeltCooldown);
		config.set("Properties.Earth.EarthMelt.Range", earthMeltRange);
		config.set("Properties.Earth.EarthMelt.ChargeTime", earthMeltChargeTime);
		
		//LavaBlast
		config.set("Properties.Earth.LavaBlast.Range", lavaBlastRange);
		config.set("Properties.Earth.LavaBlast.ChargeTime", lavaBlastChargeTime);
		config.set("Properties.Earth.LavaBlast.Damage", lavaBlastDamage);
		
		
		// Fire
		// FireBlast
		config.set("Properties.Fire.FireBlast.Range", fireBlastRange);
		config.set("Properties.Fire.FireBlast.Speed", fireBlastSpeed);
		config.set("Properties.Fire.FireBlast.Push", fireBlastPush);
		config.set("Properties.Fire.FireBlast.Radius", fireBlastRadius);
		config.set("Properties.Fire.FireBlast.Cooldown", fireBlastCooldown);
		config.set("Properties.Fire.FireBlast.Damage", fireBlastDamage);
		config.set("Properties.Fire.FireBlast.Dissipates", fireBlastDissipate);
		//FireBurst
		// ArcOfFire
		config.set("Properties.Fire.ArcOfFire.Arc", arcOfFireArc);
		config.set("Properties.Fire.ArcOfFire.Range", arcOfFireRange);
		// RingOfFire
		config.set("Properties.Fire.RingOfFire.Range", ringOfFireRange);
		// Extinguish
		config.set("Properties.Fire.Extinguish.Range", extinguishRange);
		config.set("Properties.Fire.Extinguish.Radius", extinguishRadius);
		// Fireball
		config.set("Properties.Fire.Fireball.Cooldown", fireballCooldown);
		config.set("Properties.Fire.Fireball.Speed", fireballSpeed);
		// FireJet
		config.set("Properties.Fire.FireJet.Speed", fireJetSpeed);
		config.set("Properties.Fire.FireJet.Duration", fireJetDuration);
		config.set("Properties.Fire.FireJet.CoolDown", fireJetCooldown);
		// FireStream
		config.set("Properties.Fire.FireStream.Speed", fireStreamSpeed);
		// WallOfFire
		config.set("Properties.Fire.WallOfFire.Range", wallOfFireRange);
		config.set("Properties.Fire.WallOfFire.Height", wallOfFireHeight);
		config.set("Properties.Fire.WallOfFire.Width", wallOfFireWidth);
		config.set("Properties.Fire.WallOfFire.Duration", wallOfFireDuration);
		config.set("Properties.Fire.WallOfFire.Damage", wallOfFireDamage);
		config.set("Properties.Fire.WallOfFire.Interval", wallOfFireInterval);
		config.set("Properties.Fire.WallOfFire.Cooldown", wallOfFireCooldown);
		// HeatMelt
		config.set("Properties.Fire.HeatMelt.Range", heatMeltRange);
		config.set("Properties.Fire.HeatMelt.Radius", heatMeltRadius);
		// Illumination
		config.set("Properties.Fire.Illumination.Range", illuminationRange);
		// Lightning
		config.set("Properties.Fire.Lightning.Damage", lightningDamage);
		config.set("Properties.Fire.Lightning.Range", lightningRange);
		config.set("Properties.Fire.Lightning.Miss-Chance", lightningMissChance);
		//FireBlade
		config.set("Properties.Fire.FireBlade.sharpness-level", fireBladeSharpnessLevel);
		config.set("Properties.Fire.FireBlade.fireaspect-level", fireBladeFireAspectLevel);
		config.set("Properties.Fire.FireBlade.Duration", fireBladeDuration);
		config.set("Properties.Fire.FireBlade.strength-level", fireBladeStrengthLevel);
		config.set("Properties.Fire.FireBlade.Cooldown", fireBladeCooldown);
		//MentalExplosion
		config.set("Properties.Fire.Combustion.Damage", combustionDamage);
		config.set("Properties.Fire.Combustion.Range", combustionRange);
		config.set("Properties.Fire.Combustion.ChargeTime", combustionChargeTime);
		config.set("Properties.Fire.Combustion.Cooldown", combustionCooldown);
		config.set("Properties.Fire.Combustion.Radius", combustionRadius);
		config.set("Properties.Fire.Combustion.Explosion-Radius", combustionExplosionRadius);
		config.set("Properties.Fire.Combustion.Inner-Radius", combustionInnerRadius);
		// Day
		config.set("Properties.Fire.Day-Power-Factor", dayFactor);
		// Water
		// Bloodbending
		config.set("Properties.Water.Bloodbending.Throw-Factor",
				bloodbendingThrowFactor);
		config.set("Properties.Water.Bloodbending.Range", bloodbendingRange);
		//WaterBubble
		// FreezeMelt
		config.set("Properties.Water.FreezeMelt.Range", freezeMeltRange);
		config.set("Properties.Water.FreezeMelt.Radius", freezeMeltRadius);
		config.set("Properties.Water.FreezeMelt.Depth", freezeMeltDepth);
		// HealingWaters
		config.set("Properties.Water.HealingWaters.Radius", healingWatersRadius);
		config.set("Properties.Water.HealingWaters.Interval",
				healingWatersInterval);
		// Plantbending
		config.set("Properties.Water.Plantbending.Regrow-Time",
				plantbendingRegrowTime);
		// WaterManipulation
		config.set("Properties.Water.WaterManipulation.Damage", waterManipulationDamage);
		config.set("Properties.Water.WaterManipulation.Range",
				waterManipulationRange);
		config.set("Properties.Water.WaterManipulation.Speed",
				waterManipulationSpeed);
		config.set("Properties.Water.WaterManipulation.Push", WaterManipulationPush);
		// WaterSpout
		config.set("Properties.Water.WaterSpout.Height", waterSpoutHeight);
		config.set("Properties.Water.WaterSpout.Rotation-Speed", waterSpoutRotationSpeed);
		// WaterWall
		config.set("Properties.Water.WaterWall.Range", waterWallRange);
		config.set("Properties.Water.WaterWall.Radius", waterWallRadius);
		// Wave
		config.set("Properties.Water.Wave.Radius", waveRadius);
		config.set("Properties.Water.Wave.Horizontal-Push-Force",
				waveHorizontalPush);
		config.set("Properties.Water.Wave.Vertical-Push-Force",
				waveVerticalPush);
		// Fast Swimming
		config.set("Properties.Water.FastSwimming.Factor", fastSwimmingFactor);
		// IceSpike
		config.set("Properties.Water.IceSpike.Cooldown", icespikeCooldown);
		config.set("Properties.Water.IceSpike.Damage", icespikeDamage);
		config.set("Properties.Water.IceSpike.Range", icespikeRange);
		config.set("Properties.Water.IceSpike.ThrowingMult",
				icespikeThrowingMult);
		config.set("Properties.Water.IceSpike.AreaCooldown",
				icespikeAreaCooldown);
		config.set("Properties.Water.IceSpike.AreaDamage", icespikeAreaDamage);
		config.set("Properties.Water.IceSpike.AreaRadius", icespikeAreaRadius);
		config.set("Properties.Water.IceSpike.AreaThrowingMult",
				icespikeAreaThrowingMult);	
		//Torrent
		config.set("Properties.Water.Torrent.Damage", torrentDamage);
		config.set("Properties.Water.Torrent.DeflectDamage", torrentDeflectDamage);
		//OctopusForm
		config.set("Properties.Water.OctopusForm.Damage", octopusFormDamage);
		
		//IceSwipe
		config.set("Properties.Water.IceSwipe.Damage", iceSwipeDamage);
		config.set("Properties.Water.IceSwipe.Range", iceSwipeRange);
		config.set("Properties.Water.IceSwipe.Speed", iceSwipeSpeed);
		config.set("Properties.Water.IceSwipe.Push-Factor", iceSwipePush);
		config.set("Properties.Water.IceSwipe.Cooldown", iceSwipeCooldown);
		// Night
		config.set("Properties.Water.Night-Power-Factor", nightFactor);

		// Option
		config.set("Bending.Option.EarthBendable", earthbendable);

		try {
			// config.setDefaults(config);
			// config.options().copyDefaults(true);
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getColor(String element) {
		return color.get(element);
	}

	public static String getPrefix(String element) {
		return prefixes.get(element);
	}

}

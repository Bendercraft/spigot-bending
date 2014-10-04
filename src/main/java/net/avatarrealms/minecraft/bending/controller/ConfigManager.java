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
	
	public static int astralProjectionCooldown = 2000;

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
	
	// Suffocate
	public static int suffocateDistance = 10;
	public static int suffocateBaseDamage = 1;
	
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
	
	public static long lavaTrainDuration = 20000;
	public static int lavaTrainRange = 7;
	public static int lavaTrainWidth = 1;
	public static int lavaTrainRandomWidth = 2;
	public static double lavaTrainRandomChance = 0.25;
	public static int lavaTrainReachWidth = 3;

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
	public static int bloodbendingMaxDuration = 10;
	public static int bloodbendingCooldown = 5;

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
		
		astralProjectionCooldown = config.getInt("Properties.Spirit.AstralProjection.Cooldown");

		// Chat:
		enabled = config.getBoolean("Chat.Enabled");
		colors = config.getBoolean("Chat.Colors");
		compatibility = config.getBoolean("Chat.Compatibility");
		chat = config.getString("Chat.Format");

		// Prefix
		prefixes.put("Air", config.getString("Chat.Prefix.Air"));
		prefixes.put("Avatar", config.getString("Chat.Prefix.Avatar"));
		prefixes.put("Fire", config.getString("Chat.Prefix.Fire"));
		prefixes.put("Water", config.getString("Chat.Prefix.Water"));
		prefixes.put("Earth", config.getString("Chat.Prefix.Earth"));
		prefixes.put("ChiBlocker", config.getString("Chat.Prefix.ChiBlocker"));

		// Color
		color.put("Avatar", config.getString("Chat.Color.Avatar"));
		color.put("Air", config.getString("Chat.Color.Air"));
		color.put("Fire", config.getString("Chat.Color.Fire"));
		color.put("Water", config.getString("Chat.Color.Water"));
		color.put("Earth", config.getString("Chat.Color.Earth"));
		color.put("ChiBlocker", config.getString("Chat.Color.ChiBlocker"));

		// Bending

		// Option
		earthbendable = defaultearthbendable;
		if (config.contains("Bending.Option.EarthBendable")){
			earthbendable = config.getStringList("Bending.Option.EarthBendable");
		}		

		// EarthBendable
		useWeapon
				.put("Air", config.getBoolean(
						"Bending.Option.Bend-With-Weapon.Air"));
		useWeapon.put("Earth", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.Earth"));
		useWeapon.put("Fire", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.Fire"));
		useWeapon.put("Water", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.Water"));
		useWeapon.put("ChiBlocker", config.getBoolean(
				"Bending.Option.Bend-With-Weapon.ChiBlocker"));

		bendToItem = config.getBoolean("Bending.Option.Bend-To-Item");
		useTagAPI = config.getBoolean("Bending.Option.Use-TagAPI");
		reverseearthbending = config.getBoolean("Bending.Option.Reverse-Earthbending");
		revertchecktime = config.getLong("Bending.Option.Reverse-Earthbending-Check-Time");
		safeRevert = config.getBoolean("Bending.Option.Safe-Revert");
		dissipateAfter = config.getLong("Bending.Option.Firebending-Dissipate-Time");
		seaLevel = config.getInt("Bending.Option.Sea-Level");

		// Properties
		// ChiBlocker
		chiblockduration = config.getLong("Properties.ChiBlocker.ChiBlock-Duration");
		dodgechance = config.getDouble("Properties.ChiBlocker.Dodge-Chance");
		punchdamage = config.getDouble("Properties.ChiBlocker.Punch-Damage");
		falldamagereduction = config.getDouble("Properties.ChiBlocker.Fall-Damage-Reduction");

		globalCooldown = config.getLong("Properties.GlobalCooldown");

		// HighJump
		jumpHeight = config.getDouble("Properties.ChiBlocker.HighJump.Height");
		highJumpCooldown = config.getLong("Properties.ChiBlocker.HighJump.Cooldown");

		// Paralyze
		paralyzeCooldown = config.getLong("Properties.ChiBlocker.Paralyze.Cooldown");
		paralyzeDuration = config.getLong("Properties.ChiBlocker.Paralyze.Duration");

		// RapidPunch
		rapidPunchDamage = config.getInt("Properties.ChiBlocker.RapidPunch.Damage");
		rapidPunchDistance = config.getInt("Properties.ChiBlocker.RapidPunch.Distance");
		rapidPunchCooldown = config.getLong("Properties.ChiBlocker.RapidPunch.Cooldown");
		rapidPunchPunches = config.getInt("Properties.ChiBlocker.RapidPunch.Punches");
		
		//Poisonned Dart
		dartRange = config.getInt("Properties.ChiBlocker.PoisonnedDart.Range");
		dartDamage = config.getInt("Properties.ChiBlocker.PoisonnedDart.Damage");
		poisonLevel = config.getInt("Properties.ChiBlocker.PoisonnedDart.poison-level");
		poisonnedDartCooldown = config.getInt("Properties.ChiBlocker.PoisonnedDart.Cooldown");
		
		//Smoke bomb
		smokeRadius = config.getInt("Properties.ChiBlocker.SmokeBomb.Radius");
		smokeDuration = config.getInt("Properties.ChiBlocker.SmokeBomb.Duration");
		smokeBombCooldown = config.getInt("Properties.ChiBlocker.SmokeBomb.Cooldown");
		
		//Dash
		dashCooldown = config.getInt("Properties.ChiBlocker.Dash.Cooldown");
		dashLength = config.getDouble("Properties.ChiBlocker.Dash.Length");
		dashHeight = config.getDouble("Properties.ChiBlocker.Dash.Height");

		// Air
		// AirBlast
		airBlastSpeed = config.getDouble("Properties.Air.AirBlast.Speed");
		airBlastRange = config.getDouble("Properties.Air.AirBlast.Range");
		airBlastRadius = config.getDouble("Properties.Air.AirBlast.Affecting-Radius");
		airBlastPush = config.getDouble("Properties.Air.AirBlast.Push-Factor");
		
		//AirBurst

		// AirBubble
		airBubbleRadius = config.getInt("Properties.Air.AirBubble.Radius");

		// AirPassive
		airPassiveFactor = (float) config.getDouble("Properties.Air.Passive.Factor");

		// AirShield
		airShieldRadius = config.getDouble("Properties.Air.AirShield.Radius");

		// AirSuction
		airSuctionSpeed = config.getDouble("Properties.Air.AirSuction.Speed");
		airSuctionRange = config.getDouble("Properties.Air.AirSuction.Range");
		airSuctionRadius = config.getDouble("Properties.Air.AirSuction.Affecting-Radius");
		airSuctionPush = config.getDouble("Properties.Air.AirSuction.Push-Factor");

		// AirSwipe
		airSwipeDamage = config.getInt("Properties.Air.AirSwipe.Damage");
		airSwipeRange = config.getDouble("Properties.Air.AirSwipe.Range");
		airSwipeArc = config.getInt("Properties.Air.AirSwipe.Arc");
		airSwipeSpeed = config.getDouble("Properties.Air.AirSwipe.Speed");
		airSwipeRadius = config.getDouble("Properties.Air.AirSwipe.Affecting-Radius");
		airSwipePush = config.getDouble("Properties.Air.AirSwipe.Push-Factor");
		airSwipeCooldown = config.getLong("Properties.Air.AirSwipe.Cooldown");

		// Tornado
		tornadoRadius = config.getDouble("Properties.Air.Tornado.Radius");
		tornadoHeight = config.getDouble("Properties.Air.Tornado.Height");
		tornadoRange = config.getDouble("Properties.Air.Tornado.Range");
		tornadoMobPush = config.getDouble("Properties.Air.Tornado.Mob-Push-Factor");
		tornadoPlayerPush = config.getDouble("Properties.Air.Tornado.Player-Push-Factor");

		// Air Scooter
		airScooterSpeed = config.getDouble("Properties.Air.AirScooter.Speed");

		// Air Spout
		airSpoutHeight = config.getDouble("Properties.Air.AirSpout.Height");
		
		//Air Manipulation
		airManipulationDamage = config.getInt("Properties.Air.AirManipulation.Damage");
		airManipulationRange = config.getInt("Properties.Air.AirManipulation.Range");
		
		//Suffocate
		suffocateBaseDamage = config.getInt("Properties.Air.Suffocate.Damage");
		suffocateDistance = config.getInt("Properties.Air.Suffocate.Distance");
		// Earth
		// Catapult
		catapultLength = config.getInt("Properties.Earth.Catapult.Length");
		catapultSpeed = config.getDouble("Properties.Earth.Catapult.Speed");
		catapultPush = config.getDouble("Properties.Earth.Catapult.Push");
		
		//ShockWave

		// CompactColumn
		compactColumnRange = config.getDouble("Properties.Earth.CompactColumn.Range");
		compactColumnSpeed = config.getDouble("Properties.Earth.CompactColumn.Speed");

		// EarthBlast
		earthBlastDamage = config.getInt("Properties.Earth.EarthBlast.Damage");
		earthBlastHitSelf = config.getBoolean("Properties.Earth.EarthBlast.Hit-Self");
		earthBlastPrepareRange = config.getDouble("Properties.Earth.EarthBlast.Prepare-Range");
		earthBlastRange = config.getDouble("Properties.Earth.EarthBlast.Range");
		earthBlastSpeed = config.getDouble("Properties.Earth.EarthBlast.Speed");
		earthBlastRevert = config.getBoolean("Properties.Earth.EarthBlast.Revert");
		earthBlastPush = config.getDouble("Properties.Earth.EarthBlast.Push");
		
		//IronBlast
		ironBlastDamage = config.getInt("Properties.Earth.IronBlast.Damage");				

		// EarthColumn
		earthColumnHeight = config.getInt("Properties.Earth.EarthColumn.Height");

		// EarthGrab
		earthGrabRange = config.getDouble("Properties.Earth.EarthGrab.Range");
		earthGrabDuration = config.getInt("Properties.Earth.EarthGrab.Duration");
		earthGrabCooldown = config.getLong("Properties.Earth.EarthGrab.Cooldown");

		// EarthPassive
		earthPassive = config.getLong("Properties.Earth.EarthPassive.Wait-Before-Reverse-Changes");
		passiveResistanceLevel = config.getInt("Properties.Earth.EarthPassive.resistance-level");

		// EarthTunnel
		earthTunnelMaxRadius = config.getDouble("Properties.Earth.EarthTunnel.Max-Radius");
		earthTunnelRange = config.getDouble("Properties.Earth.EarthTunnel.Range");
		earthTunnelRadius = config.getDouble("Properties.Earth.EarthTunnel.Radius");
		earthTunnelInterval = config.getLong("Properties.Earth.EarthTunnel.Interval");
		earthTunnelRevert = config.getBoolean("Properties.Earth.EarthTunnel.Revert");

		// EarthWall
		earthWallRange = config.getInt("Properties.Earth.EarthWall.Range");
		earthWallHeight = config.getInt("Properties.Earth.EarthWall.Height");
		earthWallWidth = config.getInt("Properties.Earth.EarthWall.Width");

		// Collapse
		collapseRange = config.getInt("Properties.Earth.Collapse.Range");
		collapseRadius = config.getDouble("Properties.Earth.Collapse.Radius");

		// Tremorsense
		tremorsenseCooldown = config.getLong("Properties.Earth.Tremorsense.Cooldown");
		tremorsenseMaxDepth = config.getInt("Properties.Earth.Tremorsense.Max-Depth");
		tremorsenseRadius = config.getInt("Properties.Earth.Tremorsense.Radius");
		tremorsenseLightThreshold = (byte) config.getInt("Properties.Earth.Tremorsense.Light-Threshold");

		// EarthArmor
		earthArmorDuration = config.getLong(
				"Properties.Earth.EarthArmor.Duration");
		earthArmorStrength = config.getInt("Properties.Earth.EarthArmor.Strength");
		earthArmorCooldown = config.getLong("Properties.Earth.EarthArmor.Cooldown");
		
		//Iron Armor
		
		//MetalBending
		
		//LavaTrain
		lavaTrainDuration = config.getLong("Properties.Earth.LavaTrain.Duration");
		lavaTrainRange = config.getInt("Properties.Earth.LavaTrain.Range");
		lavaTrainWidth = config.getInt("Properties.Earth.LavaTrain.Train-Width");
		lavaTrainRandomWidth = config.getInt("Properties.Earth.LavaTrain.Random-Width");
		lavaTrainRandomChance = config.getDouble("Properties.Earth.LavaTrain.Random-Chance");
		lavaTrainReachWidth = config.getInt("Properties.Earth.LavaTrain.Reach-Width");
		
		//LavaBlast
		lavaBlastRange = config.getInt("Properties.Earth.LavaBlast.Range");
		lavaBlastChargeTime = config.getInt("Properties.Earth.LavaBlast.ChargeTime");
		lavaBlastDamage = config.getInt("Properties.Earth.LavaBlast.Damage");
		
		// Fire
		// FireBlast
		fireBlastRange = config.getDouble("Properties.Fire.FireBlast.Range");
		fireBlastSpeed = config.getDouble("Properties.Fire.FireBlast.Speed");
		fireBlastPush = config.getDouble("Properties.Fire.FireBlast.Push");
		fireBlastRadius = config.getDouble("Properties.Fire.FireBlast.Radius");
		fireBlastCooldown = config.getLong("Properties.Fire.FireBlast.Cooldown");
		fireBlastDamage = config.getInt("Properties.Fire.FireBlast.Damage");
		fireBlastDissipate = config.getBoolean("Properties.Fire.FireBlast.Dissipates");
		
		//FireBurst
		
		//FireShield
		
		// ArcOfFire
		arcOfFireArc = config.getInt("Properties.Fire.ArcOfFire.Arc");
		arcOfFireRange = config.getInt("Properties.Fire.ArcOfFire.Range");

		// RingOfFire
		ringOfFireRange = config.getInt("Properties.Fire.RingOfFire.Range");

		// Extinguish
		extinguishRange = config.getDouble("Properties.Fire.Extinguish.Range");
		extinguishRadius = config.getDouble("Properties.Fire.Extinguish.Radius");

		// Fireball
		fireballCooldown = config.getLong("Properties.Fire.Fireball.Cooldown");
		fireballSpeed = config.getDouble("Properties.Fire.Fireball.Speed");

		// FireJet
		fireJetSpeed = config.getDouble("Properties.Fire.FireJet.Speed");
		fireJetDuration = config.getLong("Properties.Fire.FireJet.Duration");
		fireJetCooldown = config.getLong("Properties.Fire.FireJet.CoolDown");

		// FireStream
		fireStreamSpeed = config.getDouble("Properties.Fire.FireStream.Speed");

		// WallOfFire
		wallOfFireRange = config.getInt("Properties.Fire.WallOfFire.Range");
		wallOfFireHeight = config.getInt("Properties.Fire.WallOfFire.Height");
		wallOfFireWidth = config.getInt("Properties.Fire.WallOfFire.Width");
		wallOfFireDuration = config.getLong("Properties.Fire.WallOfFire.Duration");
		wallOfFireDamage = config.getInt("Properties.Fire.WallOfFire.Damage");
		wallOfFireInterval = config.getLong("Properties.Fire.WallOfFire.Interval");
		wallOfFireCooldown = config.getLong("Properties.Fire.WallOfFire.Cooldown");

		// HeatMelt
		heatMeltRange = config.getInt("Properties.Fire.HeatMelt.Range");
		heatMeltRadius = config.getInt("Properties.Fire.HeatMelt.Radius");

		// Illumination
		illuminationRange = config.getInt("Properties.Fire.Illumination.Range");

		// Lightning
		lightningWarmup = config.getLong("Properties.Fire.Lightning.Warmup");
		lightningDamage = config.getInt("Properties.Fire.Lightning.Damage");
		lightningRange = config.getInt("Properties.Fire.Lightning.Range");
		lightningMissChance = config.getDouble("Properties.Fire.Lightning.Miss-Chance");
		
		//FireBlade
		fireBladeSharpnessLevel = config.getInt("Properties.Fire.FireBlade.sharpness-level");
		fireBladeFireAspectLevel = config.getInt("Properties.Fire.FireBlade.fireaspect-level");
		fireBladeDuration = config.getInt("Properties.Fire.FireBlade.Duration");
		fireBladeStrengthLevel = config.getInt("Properties.Fire.FireBlade.strength-level");
		fireBladeCooldown = config.getInt("Properties.Fire.FireBlade.Cooldown");
		
		//Combustion
		combustionDamage = config.getInt("Properties.Fire.Combustion.Damage");
		combustionRange = config.getInt("Properties.Fire.Combustion.Range");
		combustionRadius = config.getDouble("Properties.Fire.Combustion.Radius");
		combustionExplosionRadius = config.getDouble("Properties.Fire.Combustion.Explosion-Radius");
		combustionInnerRadius = config.getDouble("Properties.Fire.Combustion.Inner-Radius");
		combustionChargeTime = config.getInt("Properties.Fire.Combustion.ChargeTime");
		combustionCooldown = config.getInt("Properties.Fire.Combustion.Cooldown");

		// Day
		dayFactor = config.getDouble("Properties.Fire.Day-Power-Factor");

		// Water
		// Bloodbending
		bloodbendingThrowFactor = config.getDouble("Properties.Water.Bloodbending.Throw-Factor");
		bloodbendingRange = config.getInt("Properties.Water.Bloodbending.Range");
		bloodbendingMaxDuration = config.getInt("Properties.Water.Bloodbending.Max-Duration");
		bloodbendingCooldown = config.getInt("Properties.Water.Bloodbending.Cooldown");
		
		//WaterBubble

		// FreezeMelt
		freezeMeltRange = config.getInt("Properties.Water.FreezeMelt.Range");
		freezeMeltRadius = config.getInt("Properties.Water.FreezeMelt.Radius");
		freezeMeltDepth = config.getInt("Properties.Water.FreezeMelt.Depth");

		// HealingWaters
		healingWatersRadius = config.getDouble("Properties.Water.HealingWaters.Radius");
		healingWatersInterval = config.getLong("Properties.Water.HealingWaters.Interval");

		// Plantbending
		plantbendingRegrowTime = config.getLong("Properties.Water.Plantbending.Regrow-Time");

		// WaterManipulation
		waterManipulationDamage = config.getInt("Properties.Water.WaterManipulation.Damage");
		waterManipulationRange = config.getDouble("Properties.Water.WaterManipulation.Range");
		waterManipulationSpeed = config.getDouble("Properties.Water.WaterManipulation.Speed");
		WaterManipulationPush = config.getDouble("Properties.Water.WaterManipulation.Push");

		// WaterSpout
		waterSpoutHeight = config.getInt("Properties.Water.WaterSpout.Height");
		waterSpoutRotationSpeed = config.getInt("Properties.Water.WaterSpout.Rotation-Speed");

		// WaterWall
		waterWallRange = config.getDouble("Properties.Water.WaterWall.Range");
		waterWallRadius = config.getDouble("Properties.Water.WaterWall.Radius");

		// Wave
		waveRadius = config.getDouble("Properties.Water.Wave.Radius");
		waveHorizontalPush = config.getDouble("Properties.Water.Wave.Horizontal-Push-Force");
		waveVerticalPush = config.getDouble("Properties.Water.Wave.Vertical-Push-Force");

		// Fast Swimming
		fastSwimmingFactor = config.getDouble("Properties.Water.FastSwimming.Factor");

		// IceSpike
		icespikeCooldown = config.getLong("Properties.Water.IceSpike.Cooldown");
		icespikeDamage = config.getInt("Properties.Water.IceSpike.Damage");
		icespikeRange = config.getInt("Properties.Water.IceSpike.Range");
		icespikeThrowingMult = config.getDouble("Properties.Water.IceSpike.ThrowingMult");
		icespikeAreaCooldown = config.getLong("Properties.Water.IceSpike.AreaCooldown");
		icespikeAreaDamage = config.getInt("Properties.Water.IceSpike.AreaDamage");
		icespikeAreaRadius = config.getInt("Properties.Water.IceSpike.AreaRadius");
		icespikeAreaThrowingMult = config.getDouble("Properties.Water.IceSpike.AreaThrowingMult");
		
		//Torrent
		torrentDamage = config.getInt("Properties.Water.Torrent.Damage");
		torrentDeflectDamage = config.getInt("Properties.Water.Torrent.DeflectDamage");

		//OctopusForm
		octopusFormDamage = config.getInt("Properties.Water.OctopusForm.Damage");
		
		//IceSwipe
		iceSwipeDamage = config.getInt("Properties.Water.IceSwipe.Damage");
		iceSwipeRange = config.getInt("Properties.Water.IceSwipe.Range");
		iceSwipeSpeed = config.getDouble("Properties.Water.IceSwipe.Speed");
		iceSwipePush = config.getDouble("Properties.Water.IceSwipe.Push-Factor");
		iceSwipeCooldown = config.getLong("Properties.Water.IceSwipe.Cooldown");
		// Night
		nightFactor = config.getDouble("Properties.Water.Night-Power-Factor");
	}

	public static String getColor(String element) {
		return color.get(element);
	}

	public static String getPrefix(String element) {
		return prefixes.get(element);
	}

}

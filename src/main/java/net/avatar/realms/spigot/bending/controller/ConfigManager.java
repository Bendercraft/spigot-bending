package net.avatar.realms.spigot.bending.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class ConfigManager {

	public static boolean enabled = true;
	public static boolean bendToItem = false;
	public static boolean colors = true;
	public static boolean compatibility = false;
	public static String database = "flatfile"; // "flatfile",
												// "mongodb"

	public static String chat = "<name>: <message>";

	public static Map<String, String> prefixes = new HashMap<String, String>();
	public static Map<String, String> color = new HashMap<String, String>();
	public static List<String> earthbendable = new LinkedList<String>();
	public static Map<String, Boolean> useWeapon = new HashMap<String, Boolean>();

	public static int seaLevel;

	public static long globalCooldown;

	public static long chiblockduration;
	public static double dodgechance;
	public static double punchdamage;
	public static double falldamagereduction;

	public static boolean reverseearthbending;
	public static boolean safeRevert;
	public static long revertchecktime;

	public static boolean useTagAPI;

	public static int astralProjectionCooldown;

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
	// Avatar
	// Avatar State
	public static long avatarstateDuration;
	public static long avatarstateCooldown;

	// Air
	// AirBlast
	public static double airBlastSpeed;
	public static double airBlastRange;
	public static double airBlastRadius;
	public static double airBlastPush;

	// AirBurst

	// AirBubble
	public static int airBubbleRadius;

	// AirPassive
	public static float airPassiveFactor;

	// AirScooter
	public static double airScooterSpeed;

	// AirShield
	public static double airShieldRadius;

	// AirSpout
	public static double airSpoutHeight;

	// AirSuction
	public static double airSuctionSpeed;
	public static double airSuctionRange;
	public static double airSuctionRadius;
	public static double airSuctionPush;

	// AirSwipe
	public static int airSwipeDamage;
	public static double airSwipeRange;
	public static int airSwipeArc;
	public static double airSwipeSpeed;
	public static double airSwipeRadius;
	public static double airSwipePush;
	public static long airSwipeCooldown;

	// Tornado
	public static double tornadoRadius;
	public static double tornadoHeight;
	public static double tornadoRange;
	public static double tornadoMobPush;
	public static double tornadoPlayerPush;

	// Air Manipulation
	public static int airManipulationDamage;
	public static int airManipulationRange;

	// Suffocate
	public static int suffocateDistance;
	public static int suffocateBaseDamage;

	// ChiBlocker
	// HighJump
	public static double jumpHeight;
	public static long highJumpCooldown;

	// Paralyze
	public static long paralyzeCooldown;
	public static long paralyzeDuration;

	// RapidPunch
	public static int rapidPunchDamage;
	public static int rapidPunchDistance;
	public static long rapidPunchCooldown;
	public static int rapidPunchPunches;

	// Smoke Bomb
	public static int smokeRadius;
	public static int smokeDuration;    // time in secs
	public static int smokeBombCooldown; // time in ms

	// Poisonned dart
//	public static int dartRange;
//	public static int dartDamage;
//	public static int poisonLevel;
//	public static int poisonnedDartCooldown;

	// Dash
	public static int dashCooldown;
	public static double dashLength;
	public static double dashHeight;

	// PlasticBomb
	public static int plasticCooldown;
	public static double plasticRadius;
	public static int plasticDamage;

	// Earth
	// Catapult
	public static int catapultLength;
	public static double catapultSpeed;
	public static double catapultPush;

	// ShockWave

	// Collapse
	public static int collapseRange;
	public static double collapseRadius;

	// CompactColumn
	public static double compactColumnRange;
	public static double compactColumnSpeed;

	// EarthArmor
	public static long earthArmorDuration;
	public static int earthArmorStrength;
	public static long earthArmorCooldown;

	// EarthBlast
	public static int earthBlastDamage;
	public static int ironBlastDamage;
	public static boolean earthBlastHitSelf;
	public static double earthBlastPrepareRange;
	public static double earthBlastRange;
	public static double earthBlastSpeed;
	public static boolean earthBlastRevert;
	public static double earthBlastPush;

	// EarthColumn
	public static int earthColumnHeight;

	// EarthGrab
	public static double earthGrabRange;
	public static int earthGrabDuration;
	public static long earthGrabCooldown;

	// EarthPassive
	public static long earthPassive;
	public static int passiveResistanceLevel;

	// EarthTunnel
	public static double earthTunnelMaxRadius;
	public static double earthTunnelRange;
	public static double earthTunnelRadius;
	public static long earthTunnelInterval;
	public static boolean earthTunnelRevert;

	// EarthWall
	public static int earthWallRange;
	public static int earthWallHeight;
	public static int earthWallWidth;

	// Tremorsense
	public static long tremorsenseCooldown;
	public static int tremorsenseMaxDepth;
	public static int tremorsenseRadius;
	public static byte tremorsenseLightThreshold;

	// MetalBending

	// EarthMelt

	public static long lavaTrainDuration;
	public static int lavaTrainRange;
	public static int lavaTrainWidth;
	public static int lavaTrainRandomWidth;
	public static double lavaTrainRandomChance;
	public static int lavaTrainReachWidth;

	// LavaBlast
	public static int lavaBlastDamage;
	public static int lavaBlastRange;
	public static int lavaBlastChargeTime; // time in millisecs.

	// Fire
	// ArcOfFire
	public static int arcOfFireArc;
	public static int arcOfFireRange;

	// Extinguish
	public static double extinguishRange;
	public static double extinguishRadius;

	// Fireball
	public static long fireballCooldown;
	public static double fireballSpeed;

	// FireBlast
	public static double fireBlastSpeed;
	public static double fireBlastRange;
	public static double fireBlastRadius;
	public static double fireBlastPush;
	public static int fireBlastDamage;
	public static long fireBlastCooldown;
	public static boolean fireBlastDissipate;

	// FireBurst

	// FireShield

	// FireJet
	public static double fireJetSpeed;
	public static long fireJetDuration;
	public static long fireJetCooldown;

	// FireStream
	public static double fireStreamSpeed;
	public static long dissipateAfter;

	// HeatMelt
	public static int heatMeltRange;
	public static int heatMeltRadius;

	// Illumination
	public static int illuminationRange;

	// Lightning
	public static long lightningWarmup;
	public static int lightningDamage;
	public static int lightningRange;
	public static double lightningMissChance;

	// RingOfFire
	public static int ringOfFireRange;

	// WallOfFire
	public static int wallOfFireRange;
	public static int wallOfFireHeight;
	public static int wallOfFireWidth;
	public static long wallOfFireDuration;
	public static int wallOfFireDamage;
	public static long wallOfFireInterval;
	public static long wallOfFireCooldown;

	// FireBlade
	public static int fireBladeSharpnessLevel;
	public static int fireBladeStrengthLevel;
	public static int fireBladeCooldown;
	public static int fireBladeDuration;

	// Combustion
	public static double combustionRadius;
	public static double combustionExplosionRadius;
	public static double combustionInnerRadius;
	public static int combustionDamage;
	public static int combustionRange;
	public static int combustionChargeTime; // time in millisecs
	public static int combustionCooldown;

	// Day
	public static double dayFactor;

	// Water
	// Bloodbending
	public static double bloodbendingThrowFactor;
	public static int bloodbendingRange;
	public static int bloodbendingMaxDuration;
	public static int bloodbendingCooldown;

	// FastSwimming
	public static double fastSwimmingFactor;

	// FreezeMelt
	public static int freezeMeltRange;
	public static int freezeMeltRadius;
	public static int freezeMeltDepth;

	// HealingWaters
	public static double healingWatersRadius;
	public static long healingWatersInterval;

	// IceSpike
	public static long icespikeCooldown;
	public static int icespikeDamage;
	public static int icespikeRange;
	public static double icespikeThrowingMult;

	// Plantbending
	public static long plantbendingRegrowTime;

	// SpikeField
	public static long icespikeAreaCooldown;
	public static int icespikeAreaDamage;
	public static int icespikeAreaRadius;
	public static double icespikeAreaThrowingMult;

	// WaterBubble
	public static double waterBubbleRadius;

	// WaterManipulation
	public static int waterManipulationDamage;
	public static double waterManipulationRange;
	public static double waterManipulationSpeed;
	public static double WaterManipulationPush;

	// WaterSpout
	public static int waterSpoutHeight;
	public static int waterSpoutRotationSpeed;

	// WaterWall
	public static double waterWallRange;
	public static double waterWallRadius;

	// Wave
	public static double waveRadius;
	public static double waveHorizontalPush;
	public static double waveVerticalPush;

	// Torrent
	public static int torrentDamage;
	public static int torrentDeflectDamage;

	// OctopusForm
	public static int octopusFormDamage;

	// IceSwipe
	public static int iceSwipeDamage;
	public static int iceSwipeRange;
	public static double iceSwipeSpeed;
	public static double iceSwipePush;
	public static long iceSwipeCooldown;

	// Night
	public static double nightFactor;

	public void load (File file) {
		FileConfiguration config = new YamlConfiguration();
		try {
			if (file.exists()) {
				config.load(file);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		astralProjectionCooldown = config.getInt("Properties.Spirit.AstralProjection.Cooldown");
		avatarstateDuration = config.getLong("Properties.Spirit.AvatarState.Duration");
		avatarstateCooldown = config.getLong("Properties.Spirit.AvatarState.Cooldown");

		database = config.getString("Database", "flatfile");

		// Respect plugins
		ProtectionManager.respectWorldGuard = config.getBoolean("respectWorldGuard");
		ProtectionManager.respectFactions = config.getBoolean("respectFactions");
		ProtectionManager.respectCitizens = config.getBoolean("respectCitizens");

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
		if (config.contains("Bending.Option.EarthBendable")) {
			earthbendable = config.getStringList("Bending.Option.EarthBendable");
		}

		// EarthBendable
		useWeapon.put("Air", config.getBoolean("Bending.Option.Bend-With-Weapon.Air"));
		useWeapon.put("Earth", config.getBoolean("Bending.Option.Bend-With-Weapon.Earth"));
		useWeapon.put("Fire", config.getBoolean("Bending.Option.Bend-With-Weapon.Fire"));
		useWeapon.put("Water", config.getBoolean("Bending.Option.Bend-With-Weapon.Water"));
		useWeapon.put("ChiBlocker", config.getBoolean("Bending.Option.Bend-With-Weapon.ChiBlocker"));

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

		// Poisonned Dart
//		dartRange = config.getInt("Properties.ChiBlocker.PoisonnedDart.Range");
//		dartDamage = config.getInt("Properties.ChiBlocker.PoisonnedDart.Damage");
//		poisonLevel = config.getInt("Properties.ChiBlocker.PoisonnedDart.poison-level");
//		poisonnedDartCooldown = config.getInt("Properties.ChiBlocker.PoisonnedDart.Cooldown");

		// Smoke bomb
		smokeRadius = config.getInt("Properties.ChiBlocker.SmokeBomb.Radius");
		smokeDuration = config.getInt("Properties.ChiBlocker.SmokeBomb.Duration");
		smokeBombCooldown = config.getInt("Properties.ChiBlocker.SmokeBomb.Cooldown");

		// Dash
		dashCooldown = config.getInt("Properties.ChiBlocker.Dash.Cooldown");
		dashLength = config.getDouble("Properties.ChiBlocker.Dash.Length");
		dashHeight = config.getDouble("Properties.ChiBlocker.Dash.Height");

		// Plastic bomb
		plasticCooldown = config.getInt("Properties.ChiBlocker.PlasticBomb.Cooldown");
		plasticRadius = config.getDouble("Properties.ChiBlocker.PlasticBomb.Radius");
		plasticDamage = config.getInt("Properties.ChiBlocker.PlasticBomb.Damage");

		// Air
		// AirBlast
		airBlastSpeed = config.getDouble("Properties.Air.AirBlast.Speed");
		airBlastRange = config.getDouble("Properties.Air.AirBlast.Range");
		airBlastRadius = config.getDouble("Properties.Air.AirBlast.Affecting-Radius");
		airBlastPush = config.getDouble("Properties.Air.AirBlast.Push-Factor");

		// AirBurst

		// AirBubble
		airBubbleRadius = config.getInt("Properties.Air.AirBubble.Radius");
		waterBubbleRadius = airBubbleRadius;

		// AirPassive
		airPassiveFactor = (float)config.getDouble("Properties.Air.Passive.Factor");

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

		// Air Manipulation
		airManipulationDamage = config.getInt("Properties.Air.AirManipulation.Damage");
		airManipulationRange = config.getInt("Properties.Air.AirManipulation.Range");

		// Suffocate
		suffocateBaseDamage = config.getInt("Properties.Air.Suffocate.Damage");
		suffocateDistance = config.getInt("Properties.Air.Suffocate.Distance");
		// Earth
		// Catapult
		catapultLength = config.getInt("Properties.Earth.Catapult.Length");
		catapultSpeed = config.getDouble("Properties.Earth.Catapult.Speed");
		catapultPush = config.getDouble("Properties.Earth.Catapult.Push");

		// ShockWave

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

		// IronBlast
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
		tremorsenseLightThreshold = (byte)config.getInt("Properties.Earth.Tremorsense.Light-Threshold");

		// EarthArmor
		earthArmorDuration = config.getLong("Properties.Earth.EarthArmor.Duration");
		earthArmorStrength = config.getInt("Properties.Earth.EarthArmor.Strength");
		earthArmorCooldown = config.getLong("Properties.Earth.EarthArmor.Cooldown");

		// Iron Armor

		// MetalBending

		// LavaTrain
		lavaTrainDuration = config.getLong("Properties.Earth.LavaTrain.Duration");
		lavaTrainRange = config.getInt("Properties.Earth.LavaTrain.Range");
		lavaTrainWidth = config.getInt("Properties.Earth.LavaTrain.Train-Width");
		lavaTrainRandomWidth = config.getInt("Properties.Earth.LavaTrain.Random-Width");
		lavaTrainRandomChance = config.getDouble("Properties.Earth.LavaTrain.Random-Chance");
		lavaTrainReachWidth = config.getInt("Properties.Earth.LavaTrain.Reach-Width");

		// LavaBlast
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

		// FireBurst

		// FireShield

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

		// FireBlade
		fireBladeSharpnessLevel = config.getInt("Properties.Fire.FireBlade.sharpness-level");
		fireBladeDuration = config.getInt("Properties.Fire.FireBlade.Duration");
		fireBladeStrengthLevel = config.getInt("Properties.Fire.FireBlade.strength-level");
		fireBladeCooldown = config.getInt("Properties.Fire.FireBlade.Cooldown");

		// Combustion
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

		// WaterBubble

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

		// Torrent
		torrentDamage = config.getInt("Properties.Water.Torrent.Damage");
		torrentDeflectDamage = config.getInt("Properties.Water.Torrent.DeflectDamage");

		// OctopusForm
		octopusFormDamage = config.getInt("Properties.Water.OctopusForm.Damage");

		// IceSwipe
		iceSwipeDamage = config.getInt("Properties.Water.IceSwipe.Damage");
		iceSwipeRange = config.getInt("Properties.Water.IceSwipe.Range");
		iceSwipeSpeed = config.getDouble("Properties.Water.IceSwipe.Speed");
		iceSwipePush = config.getDouble("Properties.Water.IceSwipe.Push-Factor");
		iceSwipeCooldown = config.getLong("Properties.Water.IceSwipe.Cooldown");
		// Night
		nightFactor = config.getDouble("Properties.Water.Night-Power-Factor");
	}
	
	public String getStringAttribute(String path, String defaultValue) {
		return "";
	}
	
	public String getStringAttribute(String path) {
		return getStringAttribute(path, "none");
	}
	
	public int getIntAttribute (String path, int defaultValue) {
		return 0;
	}
	
	public int getIntAttribute (String path) {
		return getIntAttribute(path, 0);
	}
	
	public long getLongAttribute (String path, long defaultValue) {
		return 0;
	}
	
	public long getLongAttribute (String path) {
		return getLongAttribute(path, 0);
	}
	
	public int getBooleanAttribute (String path, boolean defaultValue) {
		return 0;
	}
	
	public int getBooleanAttribute (String path) {
		return getBooleanAttribute(path, false);
	}
	
	public int getDoubleAttribute (String path, double defaultValue) {
		return 0;
	}
	
	public int getDoubleAttribute (String path) {
		return getDoubleAttribute(path, 0.0);
	}

	public static String getColor (String element) {
		return color.get(element);
	}

	public static String getPrefix (String element) {
		return prefixes.get(element);
	}

}

package net.bendercraft.spigot.bending.abilities;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.db.SkillTree;

public class BendingPerk {
	/* FIRE */
	public static final BendingPerk FIRE_BLAZE_PERMANENT = new BendingPerk("FIRE_BLAZE_PERMANENT", BendingElement.FIRE, Collections.emptyList());
	public static final BendingPerk FIRE_FIREBURST_CONE_DAMAGE = new BendingPerk("FIRE_FIREBURST_CONE_DAMAGE", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_BLAZE_PERMANENT", "FIRE_OVERLOAD")));
	public static final BendingPerk FIRE_OVERLOAD = new BendingPerk("FIRE_OVERLOAD", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_CONE_DAMAGE", "FIRE_BLAZE_ENERGY")));
	public static final BendingPerk FIRE_BLAZE_ENERGY = new BendingPerk("FIRE_BLAZE_ENERGY", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_OVERLOAD", "FIRE_FIREBURST_CONE_RANGE_1")));
	public static final BendingPerk FIRE_FIREBURST_CONE_RANGE_1 = new BendingPerk("FIRE_FIREBURST_CONE_RANGE_1", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_BLAZE_ENERGY", "FIRE_FIREJET_SPEED", "FIRE_FIREFERRET_SPEED")));
	public static final BendingPerk FIRE_FIREJET_SPEED = new BendingPerk("FIRE_FIREJET_SPEED", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_CONE_RANGE_1", "FIRE_FIREFERRET_RANGE", "FIRE_FIREBLAST_PUSHBACK")));
	public static final BendingPerk FIRE_FIREFERRET_RANGE = new BendingPerk("FIRE_FIREFERRET_RANGE", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREJET_SPEED", "FIRE_FIREPROTECTION_DURATION_2")));
	public static final BendingPerk FIRE_FIREPROTECTION_DURATION_1 = new BendingPerk("FIRE_FIREPROTECTION_DURATION_1", BendingElement.FIRE, Collections.emptyList());
	public static final BendingPerk FIRE_FIREBLAST_FLAME = new BendingPerk("FIRE_FIREBLAST_FLAME", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_FLAME", "FIRE_FIREWALL_FLAME")));
	public static final BendingPerk FIRE_FIREWALL_FLAME = new BendingPerk("FIRE_FIREWALL_FLAME", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_FLAME", "FIRE_INNERFIRE")));
	public static final BendingPerk FIRE_INNERFIRE = new BendingPerk("FIRE_INNERFIRE", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREWALL_FLAME")));
	public static final BendingPerk FIRE_FIREBLAST_PUSHBACK = new BendingPerk("FIRE_FIREBLAST_PUSHBACK", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREJET_SPEED", "FIRE_FIREPROTECTION_DURATION_1")));
	public static final BendingPerk FIRE_FIREPROTECTION_DURATION_2 = new BendingPerk("FIRE_FIREPROTECTION_DURATION_2", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREFERRET_RANGE", "FIRE_FIREBURST_FLAME")));
	public static final BendingPerk FIRE_FIREFERRET_SPEED = new BendingPerk("FIRE_FIREFERRET_SPEED", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_CONE_RANGE_1", "FIRE_FIREPROTECTION_BREATH")));
	public static final BendingPerk FIRE_FIREPROTECTION_BREATH = new BendingPerk("FIRE_FIREPROTECTION_BREATH", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREFERRET_SPEED", "FIRE_FIREBURST_CONE_RANGE_2")));
	public static final BendingPerk FIRE_FIREBURST_CONE_RANGE_2 = new BendingPerk("FIRE_FIREBURST_CONE_RANGE_2", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREPROTECTION_BREATH", "FIRE_FIREJET_RANGE")));
	public static final BendingPerk FIRE_FIREJET_RANGE = new BendingPerk("FIRE_FIREJET_RANGE", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_CONE_RANGE_2", "FIRE_FIREWALL_TICK")));
	public static final BendingPerk FIRE_FIREWALL_TICK = new BendingPerk("FIRE_FIREWALL_TICK", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREJET_RANGE", "FIRE_FIREPROTECTION_AREA", "FIRE_FIREBURST_AREA_ZONE")));
	public static final BendingPerk FIRE_FIREPROTECTION_AREA = new BendingPerk("FIRE_FIREPROTECTION_AREA", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREWALL_TICK")));
	public static final BendingPerk FIRE_FIREFERRET_SEARCH_RANGE = new BendingPerk("FIRE_FIREFERRET_SEARCH_RANGE", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREPROTECTION_AREA")));
	public static final BendingPerk FIRE_SCORCH = new BendingPerk("FIRE_SCORCH", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREFERRET_SEARCH_RANGE")));
	public static final BendingPerk FIRE_SCORCH_ENHANCE_1 = new BendingPerk("FIRE_SCORCH_ENHANCE_1", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_SCORCH")));
	public static final BendingPerk FIRE_SCORCH_ENHANCE_2 = new BendingPerk("FIRE_SCORCH_ENHANCE_2", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_SCORCH_ENHANCE_1")));
	public static final BendingPerk FIRE_FIREBURST_AREA_ZONE = new BendingPerk("FIRE_FIREBURST_AREA_ZONE", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREWALL_TICK", "FIRE_SNIPER")));
	public static final BendingPerk FIRE_SNIPER = new BendingPerk("FIRE_SNIPER", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_AREA_ZONE", "FIRE_FIREBURST_DAMAGE_ZONE")));
	public static final BendingPerk FIRE_FIREBURST_DAMAGE_ZONE = new BendingPerk("FIRE_FIREBURST_DAMAGE_ZONE", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_SNIPER", "FIRE_FIREWALL_DURATION")));
	public static final BendingPerk FIRE_FIREWALL_DURATION = new BendingPerk("FIRE_FIREWALL_DURATION", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBURST_DAMAGE_ZONE", "FIRE_FIREPROTECTION_FLAME")));
	public static final BendingPerk FIRE_FIREPROTECTION_FLAME = new BendingPerk("FIRE_FIREPROTECTION_FLAME", BendingElement.FIRE, Collections.emptyList());
	
	public static final BendingPerk FIRE_FIREBLAST_CHARGE_TIME_1 = new BendingPerk("FIRE_FIREBLAST_CHARGE_TIME_1", BendingElement.FIRE, Collections.emptyList());
	public static final BendingPerk FIRE_FIREBLAST_CHARGE_TIME_2 = new BendingPerk("FIRE_FIREBLAST_CHARGE_TIME_2", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBLAST_CHARGE_TIME_1", "FIRE_FIREBLAST_CHARGE_TIME_3")));
	public static final BendingPerk FIRE_FIREBLAST_CHARGE_TIME_3 = new BendingPerk("FIRE_FIREBLAST_CHARGE_TIME_3", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBLAST_CHARGE_TIME_2", "FIRE_FIREBLAST_ENERGY")));
	public static final BendingPerk FIRE_FIREBLAST_ENERGY = new BendingPerk("FIRE_FIREBLAST_ENERGY", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBLAST_CHARGE_DAMAGE_3", "FIRE_FIREBLAST_CHARGE_TIME_3")));
	public static final BendingPerk FIRE_FIREBLAST_CHARGE_DAMAGE_3 = new BendingPerk("FIRE_FIREBLAST_CHARGE_DAMAGE_3", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBLAST_CHARGE_DAMAGE_2", "FIRE_FIREBLAST_ENERGY")));
	public static final BendingPerk FIRE_FIREBLAST_CHARGE_DAMAGE_2 = new BendingPerk("FIRE_FIREBLAST_CHARGE_DAMAGE_2", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREBLAST_CHARGE_DAMAGE_1", "FIRE_FIREBLAST_CHARGE_DAMAGE_3")));
	public static final BendingPerk FIRE_FIREBLAST_CHARGE_DAMAGE_1 = new BendingPerk("FIRE_FIREBLAST_CHARGE_DAMAGE_1", BendingElement.FIRE, Collections.emptyList());
	public static final BendingPerk FIRE_FIREBURST_FLAME = new BendingPerk("FIRE_FIREBURST_FLAME", BendingElement.FIRE, Arrays.asList(Arrays.asList("FIRE_FIREPROTECTION_DURATION_2", "FIRE_FIREBLAST_FLAME")));
	
	/* AIR */
	public static final BendingPerk AIR_MOBILITY = new BendingPerk("AIR_MOBILITY", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSWIPE_SPEED = new BendingPerk("AIR_AIRSWIPE_SPEED", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBURST_SPEED = new BendingPerk("AIR_AIRBURST_SPEED", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSUCTION_SPEED = new BendingPerk("AIR_AIRSUCTION_SPEED", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBLAST_SPEED = new BendingPerk("AIR_AIRBLAST_SPEED", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSLICE_SPEED = new BendingPerk("AIR_AIRSLICE_SPEED", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSWIPE_CHARGE_TIME = new BendingPerk("AIR_AIRSWIPE_CHARGE_TIME", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSWIPE_CHARGE_POWER = new BendingPerk("AIR_AIRSWIPE_CHARGE_POWER", BendingElement.AIR, Collections.emptyList());
	
	public static final BendingPerk AIR_CUT = new BendingPerk("AIR_CUT", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBLAST_RANGE = new BendingPerk("AIR_AIRBLAST_RANGE", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSLICE_RANGE = new BendingPerk("AIR_AIRSLICE_RANGE", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSWIPE_RANGE = new BendingPerk("AIR_AIRSWIPE_RANGE", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSUCTION_RANGE = new BendingPerk("AIR_AIRSUCTION_RANGE", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBURST_RANGE = new BendingPerk("AIR_AIRBURST_RANGE", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBUBBLE_RADIUS = new BendingPerk("AIR_AIRBUBBLE_RADIUS", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSLICE_DISTANCE = new BendingPerk("AIR_AIRSLICE_DISTANCE", BendingElement.AIR, Collections.emptyList());
	
	public static final BendingPerk AIR_PRESSURE = new BendingPerk("AIR_PRESSURE", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSWIPE_COOLDOWN = new BendingPerk("AIR_AIRSWIPE_COOLDOWN", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSLICE_COOLDOWN = new BendingPerk("AIR_AIRSLICE_COOLDOWN", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSUCTION_COOLDOWN = new BendingPerk("AIR_AIRSUCTION_COOLDOWN", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBLAST_PUSH = new BendingPerk("AIR_AIRBLAST_PUSH", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBURST_PUSH = new BendingPerk("AIR_AIRBURST_PUSH", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRSWIPE_PUSH = new BendingPerk("AIR_AIRSWIPE_PUSH", BendingElement.AIR, Collections.emptyList());
	public static final BendingPerk AIR_AIRBURST_COOLDOWN = new BendingPerk("AIR_AIRBURST_COOLDOWN", BendingElement.AIR, Collections.emptyList());
	
	
	/* WATER */
	public static final BendingPerk WATER_WATERMANIPULATION_COOLDOWN = new BendingPerk("WATER_WATERMANIPULATION_COOLDOWN", BendingElement.WATER, Collections.emptyList());
	public static final BendingPerk WATER_WATERWHIP_RANGE = new BendingPerk("WATER_WATERWHIP_RANGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERMANIPULATION_COOLDOWN")));
	public static final BendingPerk WATER_TORRENT_RANGE = new BendingPerk("WATER_TORRENT_RANGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERWHIP_RANGE")));
	public static final BendingPerk WATER_WATERWHIP_COOLDOWN = new BendingPerk("WATER_WATERWHIP_COOLDOWN", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TORRENT_RANGE")));
	public static final BendingPerk WATER_TSUNAMI = new BendingPerk("WATER_TSUNAMI", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERMANIPULATION_HITBOX"), Arrays.asList("WATER_WATERWHIP_COOLDOWN")));
	
	public static final BendingPerk WATER_WATERMANIPULATION_SPEED = new BendingPerk("WATER_WATERMANIPULATION_SPEED", BendingElement.WATER, Collections.emptyList());
	public static final BendingPerk WATER_ICESPIKE_PUSHBACK = new BendingPerk("WATER_ICESPIKE_PUSHBACK", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERMANIPULATION_SPEED")));
	public static final BendingPerk WATER_WATERMANIPULATION_RANGE_1 = new BendingPerk("WATER_WATERMANIPULATION_RANGE_1", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_ICESPIKE_PUSHBACK"), Arrays.asList("WATER_WATERMANIPULATION_HITBOX")));
	public static final BendingPerk WATER_PRESERVE = new BendingPerk("WATER_PRESERVE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERMANIPULATION_RANGE_1"), Arrays.asList("WATER_ICESPIKE_COOLDOWN")));
	
	public static final BendingPerk WATER_WATERMANIPULATION_PUSHBACK = new BendingPerk("WATER_WATERMANIPULATION_PUSHBACK", BendingElement.WATER, Collections.emptyList());
	public static final BendingPerk WATER_ICESPIKE_SPEED = new BendingPerk("WATER_ICESPIKE_SPEED", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERMANIPULATION_PUSHBACK")));
	public static final BendingPerk WATER_WATERWHIP_SPEED = new BendingPerk("WATER_WATERWHIP_SPEED", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_ICESPIKE_SPEED")));
	public static final BendingPerk WATER_WATERMANIPULATION_RANGE_2 = new BendingPerk("WATER_WATERMANIPULATION_RANGE_2", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_ICESPIKE_COOLDOWN"), Arrays.asList("WATER_WATERWHIP_SPEED")));
	public static final BendingPerk WATER_FREEDOM = new BendingPerk("WATER_FREEDOM", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERMANIPULATION_RANGE_2")));
	
	public static final BendingPerk WATER_WATERMANIPULATION_HITBOX = new BendingPerk("WATER_WATERMANIPULATION_HITBOX", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERWHIP_COOLDOWN", "WATER_ICESPIKE_PUSHBACK")));
	public static final BendingPerk WATER_ICESPIKE_COOLDOWN = new BendingPerk("WATER_ICESPIKE_COOLDOWN", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERMANIPULATION_RANGE_1", "WATER_WATERWHIP_SPEED")));
	
	public static final BendingPerk WATER_WAVE_RANGE = new BendingPerk("WATER_WAVE_RANGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_WAVE_COOLDOWN = new BendingPerk("WATER_WAVE_COOLDOWN", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_PHASECHANGE_RADIUS = new BendingPerk("WATER_PHASECHANGE_RADIUS", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_OCTOPUSFORM_WATER_DAMAGE = new BendingPerk("WATER_OCTOPUSFORM_WATER_DAMAGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_WATERWALL_RADIUS_1 = new BendingPerk("WATER_WATERWALL_RADIUS_1", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_WATERBUBBLE_RADIUS = new BendingPerk("WATER_WATERBUBBLE_RADIUS", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_WATERMANIPULATION_DAMAGE = new BendingPerk("WATER_WATERMANIPULATION_DAMAGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_WATERSPOUT_HEIGHT = new BendingPerk("WATER_WATERSPOUT_HEIGHT", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_WATERWALL_RADIUS_2 = new BendingPerk("WATER_WATERWALL_RADIUS_2", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	public static final BendingPerk WATER_OCTOPUSFORM_ICE_DAMAGE = new BendingPerk("WATER_OCTOPUSFORM_ICE_DAMAGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TSUNAMI", "WATER_PRESERVE", "WATER_FREEDOM")));
	
	public static final BendingPerk WATER_TORRENT_WHIRLWIND_DAMAGE = new BendingPerk("WATER_TORRENT_WHIRLWIND_DAMAGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_OCTOPUSFORM_WATER_DAMAGE", "WATER_WAVE_RADIUS_1")));
	public static final BendingPerk WATER_WAVE_RADIUS_1 = new BendingPerk("WATER_WAVE_RADIUS_1", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_TORRENT_WHIRLWIND_DAMAGE", "WATER_EQUILIBIRUM")));
	public static final BendingPerk WATER_EQUILIBIRUM = new BendingPerk("WATER_EQUILIBIRUM", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WAVE_RADIUS_1", "WATER_WAVE_RADIUS_2")));
	public static final BendingPerk WATER_WAVE_RADIUS_2 = new BendingPerk("WATER_WAVE_RADIUS_2", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_EQUILIBIRUM", "WATER_TORRENT_STREAM_DAMAGE")));
	public static final BendingPerk WATER_TORRENT_STREAM_DAMAGE = new BendingPerk("WATER_TORRENT_STREAM_DAMAGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WAVE_RADIUS_2", "WATER_OCTOPUSFORM_ICE_DAMAGE")));
	
	public static final BendingPerk WATER_ICESPIKE_DAMAGE = new BendingPerk("WATER_ICESPIKE_DAMAGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERWALL_RADIUS_1", "WATER_COMMUNION")));
	public static final BendingPerk WATER_COMMUNION = new BendingPerk("WATER_COMMUNION", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_ICESPIKE_DAMAGE", "WATER_ICESPIKE_RANGE")));
	public static final BendingPerk WATER_ICESPIKE_RANGE = new BendingPerk("WATER_ICESPIKE_RANGE", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_COMMUNION", "WATER_WATERWALL_RADIUS_2")));
	
	public static final BendingPerk WATER_BATTERY = new BendingPerk("WATER_BATTERY", BendingElement.WATER, Arrays.asList(Arrays.asList("WATER_WATERBUBBLE_RADIUS", "WATER_WATERSPOUT_HEIGHT")));
	
	/* EARTH */
	public static final BendingPerk EARTH_RAISEEARTH_RANGE = new BendingPerk("EARTH_RAISEEARTH_RANGE", BendingElement.EARTH, Collections.emptyList());
	public static final BendingPerk EARTH_EARTHBLAST_SELECT_RANGE_1 = new BendingPerk("EARTH_EARTHBLAST_SELECT_RANGE_1", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_RAISEEARTH_RANGE")));
	public static final BendingPerk EARTH_SHOCKWAVE_ANGLE = new BendingPerk("EARTH_SHOCKWAVE_ANGLE", BendingElement.EARTH, Collections.emptyList());
	public static final BendingPerk EARTH_EARTHBLAST_DAMAGE = new BendingPerk("EARTH_EARTHBLAST_DAMAGE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHBLAST_SELECT_RANGE_1", "EARTH_SHOCKWAVE_ANGLE")));
	public static final BendingPerk EARTH_EARTHLARIAT_RANGE = new BendingPerk("EARTH_EARTHLARIAT_RANGE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHBLAST_DAMAGE")));
	public static final BendingPerk EARTH_EARTHARMOR_DURATION_1 = new BendingPerk("EARTH_EARTHARMOR_DURATION_1", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHLARIAT_RANGE")));
	public static final BendingPerk EARTH_EARTHBLAST_MULTISELECT = new BendingPerk("EARTH_EARTHBLAST_MULTISELECT", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHARMOR_DURATION_1")));
	public static final BendingPerk EARTH_EARTHBLAST_SELECT_RANGE_2 = new BendingPerk("EARTH_EARTHBLAST_SELECT_RANGE_2", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHBLAST_MULTISELECT")));
	public static final BendingPerk EARTH_EARTHBLAST_SPEED = new BendingPerk("EARTH_EARTHBLAST_SPEED", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHBLAST_SELECT_RANGE_2")));
	public static final BendingPerk EARTH_SHOCKWAVE_DAMAGE = new BendingPerk("EARTH_SHOCKWAVE_DAMAGE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHBLAST_SPEED")));
	public static final BendingPerk EARTH_EARTHGRAB_RANGE = new BendingPerk("EARTH_EARTHGRAB_RANGE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_SHOCKWAVE_DAMAGE")));
	public static final BendingPerk EARTH_EARTHARMOR_PUSHBACK = new BendingPerk("EARTH_EARTHARMOR_PUSHBACK", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHGRAB_RANGE")));
	public static final BendingPerk EARTH_PATIENCE = new BendingPerk("EARTH_PATIENCE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHARMOR_PUSHBACK")));
	
	public static final BendingPerk EARTH_EARTHARMOR_COOLDOWN = new BendingPerk("EARTH_EARTHARMOR_COOLDOWN", BendingElement.EARTH, Collections.emptyList());
	public static final BendingPerk EARTH_EARTHARMOR_DURATION_2 = new BendingPerk("EARTH_EARTHARMOR_DURATION_2", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHARMOR_COOLDOWN")));
	public static final BendingPerk EARTH_SHOCKWAVE_RANGE = new BendingPerk("EARTH_SHOCKWAVE_RANGE", BendingElement.EARTH, Collections.emptyList());
	public static final BendingPerk EARTH_EARTHLARIAT_COOLDOWN = new BendingPerk("EARTH_EARTHLARIAT_COOLDOWN", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHARMOR_DURATION_2", "EARTH_SHOCKWAVE_RANGE")));
	public static final BendingPerk EARTH_EARTHGRAB_COOLDOWN = new BendingPerk("EARTH_EARTHGRAB_COOLDOWN", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHLARIAT_COOLDOWN")));
	public static final BendingPerk EARTH_EARTHBLAST_RANGE = new BendingPerk("EARTH_EARTHBLAST_RANGE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHGRAB_COOLDOWN")));
	public static final BendingPerk EARTH_EARTHWALL_MULTISELECT = new BendingPerk("EARTH_EARTHWALL_MULTISELECT", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHBLAST_RANGE")));
	public static final BendingPerk EARTH_EARTHARMOR_REDUCTION = new BendingPerk("EARTH_EARTHARMOR_REDUCTION", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHWALL_MULTISELECT")));
	public static final BendingPerk EARTH_EARTHLARIAT_STUN_1 = new BendingPerk("EARTH_EARTHLARIAT_STUN_1", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHARMOR_REDUCTION")));
	public static final BendingPerk EARTH_SHOCKWAVE_STUN = new BendingPerk("EARTH_SHOCKWAVE_STUN", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHLARIAT_STUN_1")));
	public static final BendingPerk EARTH_SHOCKWAVE_CHARGETIME = new BendingPerk("EARTH_SHOCKWAVE_CHARGETIME", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_SHOCKWAVE_STUN")));
	public static final BendingPerk EARTH_EARTHARMOR_THICK = new BendingPerk("EARTH_EARTHARMOR_THICK", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_SHOCKWAVE_CHARGETIME")));
	public static final BendingPerk EARTH_RESISTANCE = new BendingPerk("EARTH_RESISTANCE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHARMOR_THICK")));
	
	public static final BendingPerk EARTH_CATAPULT_COOLDOWN = new BendingPerk("EARTH_CATAPULT_COOLDOWN", BendingElement.EARTH, Collections.emptyList());
	public static final BendingPerk EARTH_SHOCKWAVE_POWER = new BendingPerk("EARTH_SHOCKWAVE_POWER", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_CATAPULT_COOLDOWN")));
	public static final BendingPerk EARTH_EARTHBLAST_DEFLECT_RANGE = new BendingPerk("EARTH_EARTHBLAST_DEFLECT_RANGE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_SHOCKWAVE_POWER")));
	public static final BendingPerk EARTH_EARTHLARIAT_STUN_2 = new BendingPerk("EARTH_EARTHLARIAT_STUN_2", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHBLAST_DEFLECT_RANGE")));
	public static final BendingPerk EARTH_SHOCKWAVE_SLOW = new BendingPerk("EARTH_SHOCKWAVE_SLOW", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHLARIAT_STUN_2")));
	public static final BendingPerk EARTH_EARTHARMOR_DAMAGE = new BendingPerk("EARTH_EARTHARMOR_DAMAGE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_SHOCKWAVE_SLOW")));
	public static final BendingPerk EARTH_EARTHGRAB_DURATION = new BendingPerk("EARTH_EARTHGRAB_DURATION", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHARMOR_DAMAGE")));
	public static final BendingPerk EARTH_INNERCORE = new BendingPerk("EARTH_INNERCORE", BendingElement.EARTH,  Arrays.asList(Arrays.asList("EARTH_EARTHGRAB_DURATION")));
	
	/* MASTER */
	public static final BendingPerk MASTER_TRAINING = new BendingPerk("MASTER_TRAINING", BendingElement.MASTER, Collections.emptyList());
	
	public static final BendingPerk MASTER_DASH_RANGE = new BendingPerk("MASTER_DASH_RANGE", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_TRAINING")));
	public static final BendingPerk MASTER_DIRECTHIT_PUSH = new BendingPerk("MASTER_DIRECTHIT_PUSH", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_DASH_RANGE")));
	public static final BendingPerk MASTER_BLANKPOINTPUSH_POISONNEDARTRANGE_NEBULARCHAINRANGE = new BendingPerk("MASTER_BLANKPOINTPUSH_POISONNEDARTRANGE_NEBULARCHAINRANGE", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_DIRECTHIT_PUSH")));
	
	public static final BendingPerk MASTER_DIRECTHIT_COOLDOWN = new BendingPerk("MASTER_DIRECTHIT_COOLDOWN", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_TRAINING")));
	public static final BendingPerk MASTER_AIMCD_C4CD_SLICE_CD = new BendingPerk("MASTER_AIMCD_C4CD_SLICE_CD", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_DIRECTHIT_COOLDOWN")));
	public static final BendingPerk MASTER_STRAIGHTSHOTDAMAGE_SMOKEBOMBDURATION_SLICEBLEEDDAMAGE = new BendingPerk("MASTER_STRAIGHTSHOTDAMAGE_SMOKEBOMBDURATION_SLICEBLEEDDAMAGE", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_AIMCD_C4CD_SLICE_CD")));
	
	public static final BendingPerk MASTER_HIGHJUMP_HEIGHT = new BendingPerk("MASTER_HIGHJUMP_HEIGHT", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_TRAINING")));
	public static final BendingPerk MASTER_AIMCHARGETIME_PARASTICKCD_CONCUSSIONCD = new BendingPerk("MASTER_AIMCHARGETIME_PARASTICKCD_CONCUSSIONCD", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_HIGHJUMP_HEIGHT")));
	public static final BendingPerk MASTER_STRAIGHTSHOTRANGE_SMOKEBOMBRADIUS_SLICEDIRECTDAMAGE = new BendingPerk("MASTER_STRAIGHTSHOTRANGE_SMOKEBOMBRADIUS_SLICEDIRECTDAMAGE", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_AIMCHARGETIME_PARASTICKCD_CONCUSSIONCD")));
	
	public static final BendingPerk MASTER_STRAIGHTSHOTCD_C4RADIUS_NEBULARCD = new BendingPerk("MASTER_STRAIGHTSHOTCD_C4RADIUS_NEBULARCD", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_STRAIGHTSHOTDAMAGE_SMOKEBOMBDURATION_SLICEBLEEDDAMAGE", "MASTER_SMOKE_HIDE_SHIELD")));
	public static final BendingPerk MASTER_HIGHJUMP_COOLDOWN = new BendingPerk("MASTER_HIGHJUMP_COOLDOWN", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_STRAIGHTSHOTCD_C4RADIUS_NEBULARCD")));
	public static final BendingPerk MASTER_EXPLOSIVESHOTRADIUSDAMAGE_SMOKEBOMBPARASTICKDAMAGE_NEBULARCD = new BendingPerk("MASTER_EXPLOSIVESHOTRADIUSDAMAGE_SMOKEBOMBPARASTICKDAMAGE_NEBULARCD", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_HIGHJUMP_COOLDOWN")));
	public static final BendingPerk MASTER_DISENGAGE_PARAPARASTICK_CONSTITUTION = new BendingPerk("MASTER_DISENGAGE_PARAPARASTICK_CONSTITUTION", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_EXPLOSIVESHOTRADIUSDAMAGE_SMOKEBOMBPARASTICKDAMAGE_NEBULARCD")));
	
	public static final BendingPerk MASTER_BLANKPOINTCD_SMOKEBOMBCD_DASHSTUN = new BendingPerk("MASTER_BLANKPOINTCD_SMOKEBOMBCD_DASHSTUN", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_STRAIGHTSHOTRANGE_SMOKEBOMBRADIUS_SLICEDIRECTDAMAGE", "MASTER_DISENGAGE_PARAPARASTICK_CONSTITUTION")));
	public static final BendingPerk MASTER_DASH_CD = new BendingPerk("MASTER_DASH_CD", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_BLANKPOINTCD_SMOKEBOMBCD_DASHSTUN")));
	public static final BendingPerk MASTER_EXPLOSIVESHOTFIRE_POISONNEDDARTDAMAGE_CONCUSSIONDURATION = new BendingPerk("MASTER_EXPLOSIVESHOTFIRE_POISONNEDDARTDAMAGE_CONCUSSIONDURATION", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_DASH_CD")));
	public static final BendingPerk MASTER_SNIPE_PERSIST_CONSTITUTION = new BendingPerk("MASTER_SNIPE_PERSIST_CONSTITUTION", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_EXPLOSIVESHOTFIRE_POISONNEDDARTDAMAGE_CONCUSSIONDURATION")));
	
	public static final BendingPerk MASTER_EXPLOSIVESHOTCD_POISONNEDDARTCD_SLICEDURATION = new BendingPerk("MASTER_EXPLOSIVESHOTCD_POISONNEDDARTCD_SLICEDURATION", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_BLANKPOINTPUSH_POISONNEDARTRANGE_NEBULARCHAINRANGE", "MASTER_SNIPE_PERSIST_CONSTITUTION")));
	public static final BendingPerk MASTER_AIMRANGE_VITALPOINTCHI_SLICEINTERVAL = new BendingPerk("MASTER_AIMRANGE_VITALPOINTCHI_SLICEINTERVAL", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_EXPLOSIVESHOTCD_POISONNEDDARTCD_SLICEDURATION")));
	public static final BendingPerk MASTER_BLANKPOINTDAMAGE_PARASTICKCD_NEBULARRANGE = new BendingPerk("MASTER_BLANKPOINTDAMAGE_PARASTICKCD_NEBULARRANGE", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_AIMRANGE_VITALPOINTCHI_SLICEINTERVAL")));
	public static final BendingPerk MASTER_SMOKE_HIDE_SHIELD = new BendingPerk("MASTER_SMOKE_HIDE_SHIELD", BendingElement.MASTER,  Arrays.asList(Arrays.asList("MASTER_BLANKPOINTDAMAGE_PARASTICKCD_NEBULARRANGE")));
	

	public static final Map<BendingElement, Integer> POINTS = new HashMap<>();
	static {
		POINTS.put(BendingElement.FIRE, 20);
		POINTS.put(BendingElement.AIR, 16);
		POINTS.put(BendingElement.WATER, 14);
		POINTS.put(BendingElement.EARTH, 20);
		POINTS.put(BendingElement.MASTER, 12);
		POINTS.put(BendingElement.ENERGY, 0);
	}
	
	private static final Map<String, BendingPerk> all = new HashMap<>();
	private static final Gson mapper = new Gson();
	
	public List<List<String>> requires;
	
	public final String name;
	public final BendingElement element;
	
	public BendingPerk(String name, BendingElement element, List<List<String>> requires) {
		this.name = name.toLowerCase();
		this.element = element;
		List<List<String>> temp = new LinkedList<>();
		requires.forEach(l -> temp.add(ImmutableList.copyOf(l)));
		this.requires = ImmutableList.copyOf(temp);
	}
	
	public static void reset(UUID player) {
		File folder = new File(Bending.getInstance().getDataFolder(), "skills");
		File data = new File(folder, player.toString()+".json");
		data.delete();
	}
	
	public static void collect() {
		all.clear();
		
		// Collect !
		for(Field field : BendingPerk.class.getDeclaredFields()) {
			if(Modifier.isPublic(field.getModifiers()) 
					&& Modifier.isStatic(field.getModifiers()) 
					&& Modifier.isFinal(field.getModifiers()) 
					&& field.getType().isAssignableFrom(BendingPerk.class)) {
				field.setAccessible(true);
				try {
					BendingPerk perk = (BendingPerk) field.get(null);
					if(all.containsKey(perk.name.toLowerCase())) {
						Bending.getInstance().getLogger().info("Cannot collect '"+perk.name+"' because it already exists.");
						continue;
					}
					all.put(perk.name.toLowerCase(), perk);
					Bending.getInstance().getLogger().info("Collected "+perk.name);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// Quiet !
				}
				
			}
		}
		
		// Check integrity of dependencies
		for(BendingPerk perk : all.values()) {
			for(List<String> dependencies : perk.requires) {
				for(String dependency : dependencies) {
					if(!all.containsKey(dependency.toLowerCase())) {
						Bending.getInstance().getLogger().severe("Perk "+perk.name+" has unknown dependency "+dependency);
					}
				}
			}
		}
		
		// Compute json model
		SkillTree tree = new SkillTree();
		tree.setPoints(POINTS);
		List<BendingPerk> skills = new ArrayList<>(all.values());
		tree.setSkills(skills);
		

		File model = new File(Bending.getInstance().getDataFolder(), "skills.json");
		try (FileWriter writer = new FileWriter(model, false)){
			mapper.toJson(tree, writer);
		}
		catch (Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Could not save skills model ", e);
		}
	}

	public static BendingPerk valueOf(String string) {
		return all.get(string);
	}
	
}

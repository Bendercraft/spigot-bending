package net.avatar.realms.spigot.bending.abilities;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

import com.sun.org.apache.xml.internal.utils.FastStringBuffer;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.air.AirBlast;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;
import net.avatar.realms.spigot.bending.abilities.air.AirBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirBurstCone;
import net.avatar.realms.spigot.bending.abilities.air.AirBurstSphere;
import net.avatar.realms.spigot.bending.abilities.air.AirFallBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirManipulation;
import net.avatar.realms.spigot.bending.abilities.air.AirPassive;
import net.avatar.realms.spigot.bending.abilities.air.AirScooter;
import net.avatar.realms.spigot.bending.abilities.air.AirShield;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.AirSwipe;
import net.avatar.realms.spigot.bending.abilities.air.Speed;
import net.avatar.realms.spigot.bending.abilities.air.Suffocate;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.chi.C4;
import net.avatar.realms.spigot.bending.abilities.chi.Dash;
import net.avatar.realms.spigot.bending.abilities.chi.HighJump;
import net.avatar.realms.spigot.bending.abilities.chi.Paralyze;
import net.avatar.realms.spigot.bending.abilities.chi.PoisonnedDart;
import net.avatar.realms.spigot.bending.abilities.chi.PowerfulHit;
import net.avatar.realms.spigot.bending.abilities.chi.RapidPunch;
import net.avatar.realms.spigot.bending.abilities.chi.SmokeBomb;
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
import net.avatar.realms.spigot.bending.abilities.earth.Ripple;
import net.avatar.realms.spigot.bending.abilities.earth.Shockwave;
import net.avatar.realms.spigot.bending.abilities.earth.ShockwaveArea;
import net.avatar.realms.spigot.bending.abilities.earth.ShockwaveCone;
import net.avatar.realms.spigot.bending.abilities.earth.ShockwaveFall;
import net.avatar.realms.spigot.bending.abilities.earth.Tremorsense;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.ArcOfFire;
import net.avatar.realms.spigot.bending.abilities.fire.Combustion;
import net.avatar.realms.spigot.bending.abilities.fire.Cook;
import net.avatar.realms.spigot.bending.abilities.fire.Enflamed;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlade;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurst;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurstCone;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurstSphere;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.fire.FireProtection;
import net.avatar.realms.spigot.bending.abilities.fire.FireShield;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.abilities.fire.HeatMelt;
import net.avatar.realms.spigot.bending.abilities.fire.Illumination;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.fire.RingOfFire;
import net.avatar.realms.spigot.bending.abilities.fire.WallOfFire;
import net.avatar.realms.spigot.bending.abilities.water.WaterBubble;
import net.avatar.realms.spigot.bending.abilities.water.WaterPassive;
import net.avatar.realms.spigot.bending.controller.ConfigurationManager;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;


public class AbilityManager {
	
	private static AbilityManager manager =  null;

	private Map<String, RegisteredAbility> availableAbilities;
	private Map<Abilities, Map<Object, Ability>> abilities;
	
	public static AbilityManager getManager() {
		if (manager == null) {
			manager = new AbilityManager();
		}
		return manager;
	}
	
	private AbilityManager() {
		abilities = new HashMap<Abilities, Map<Object, Ability>>();
		availableAbilities = new HashMap<String, RegisteredAbility>();
	}
	
	public void progressAllAbilities() {
		List<Ability> toRemove = new LinkedList<Ability>();
		for (Abilities key : abilities.keySet()) {
			for (Ability ability : abilities.get(key).values()) {
				boolean canKeep = ability.progress();
				if (!canKeep) {
					toRemove.add(ability);
				}
			}
		}
		
		for (Ability ability : toRemove) {
			ability.stop();
			ability.remove();
		}
	}
	
	public void stopAllAbilities() {
		for (Map<Object, Ability> instances : abilities.values()) {
			for (Ability ability : instances.values()) {
				ability.stop();
			}
		}
		
		clearAllAbilities();
	}
	
	private void clearAllAbilities() {
		for (Map<Object, Ability> instances : abilities.values()) {
			instances.clear();
		}
	}
	
	public Ability buildAbility (Abilities abilityType, Player player) {
		switch (abilityType) {
			case AvatarState : return new AvatarState(player); 
			
			case PoisonnedDart : return new PoisonnedDart(player);
			case HighJump : return new HighJump(player);
			case SmokeBomb : return new SmokeBomb(player);
			case PowerfulHit : return new PowerfulHit(player);
			case Dash :return new Dash(player);
			
			case AirBlast : return new AirBlast(player);
			
			default : return null;
		}
	}
	
	public void addInstance(Ability instance) {
		Map<Object, Ability> map = abilities.get(instance.getAbilityType());
		if (map == null) {
			map = new HashMap<Object, Ability>();
			abilities.put(instance.getAbilityType(), map);
		}
		map.put(instance.getIdentifier(), instance);
	}
	
	public Map<Object, Ability> getInstances(Abilities type) {
		return abilities.get(type);
	}
	
	public void registerAllAbilities() {
		
		register(AvatarState.class);
		
		register(PoisonnedDart.class);
//		register(C4.class);
		register(Dash.class);
		register(HighJump.class);
//		register(Paralyze.class);
		register(PowerfulHit.class);
//		register(RapidPunch.class);
		register(SmokeBomb.class);
		
		register(AirBlast.class);
//		register(AirBubble.class);
//		register(AirBurst.class);
		register(AirBurstCone.class);
		register(AirBurstSphere.class);
		register(AirFallBurst.class);
		register(AirManipulation.class);
//		register(AirPassive.class);
//		register(AirScooter.class);
//		register(AirShield.class);
//		register(AirSpout.class);
//		register(AirSwipe.class);
//		register(Speed.class);
//		register(Suffocate.class);
//		register(Tornado.class);
		
//		register(Catapult.class);
//		register(Collapse.class);
//		register(CompactColumn.class);
//		register(EarthArmor.class);
//		register(EarthBlast.class);
//		register(EarthColumn.class);
//		register(EarthGrab.class);
//		register(EarthPassive.class);
//		register(EarthShield.class);
//		register(EarthTunnel.class);
//		register(EarthWall.class);
//		register(LavaTrain.class);
//		register(MetalBending.class);
//		register(Ripple.class);
//		register(Shockwave.class);
//		register(ShockwaveArea.class);
//		register(ShockwaveCone.class);
//		register(ShockwaveFall.class);
//		register(Tremorsense.class);
		
//		register(ArcOfFire.class);
//		register(Combustion.class);
//		register(Cook.class);
//		register(Enflamed.class);
//		register(Fireball.class);
//		register(FireBlade.class);
//		register(FireBlast.class);
//		register(FireBurst.class);
//		register(FireBurstCone.class);
//		register(FireBurstSphere.class);
//		register(FireJet.class);
//		register(FireProtection.class);
//		register(FireShield.class);
//		register(FireStream.class);
//		register(HeatMelt.class);
//		register(Illumination.class);
//		register(Lightning.class);
//		register(RingOfFire.class);
//		register(WallOfFire.class);
		
//		register(Bloodbending.class);
//		register(Drainbending.class);
//		register(FastSwmimming.class);
//		register(FreezeMelt.class);
//		register(HealingWaters.class);
//		register(IceSpike.class);
//		register(IceSpike2.class);
//		register(IceSwipe.class);
//		register(Melt.class);
//		register(OctopusForm.class);
//		register(Plantbending.class);
//		register(SpikeField.class);
//		register(Torrent.class);
//		register(TorrentBurst.class);
//		register(WaterBubble.class);
//		register(WaterManipulation.class);
//		register(WaterPassive.class);
//		register(WaterReturn.class);
//		register(WaterSpout.class);
//		register(WaterWall.class);
//		register(Wave.class);
	}
	
	protected void register(Class<? extends Ability> ability) {
		BendingAbility annotation = ability.getAnnotation(BendingAbility.class);
		if(annotation == null) {
			Bending.plugin.getLogger().severe("Trying to register ability : "+ability+" but not annoted ! Aborting this registration...");
			return;
		}
		if(annotation.name() == null || annotation.name().equals("")) {
			Bending.plugin.getLogger().severe("Trying to register ability : "+ability+" but name is null or empty ! Aborting this registration...");
			return;
		}
		if(annotation.specialization() != null) {
			_register(annotation.name(), ability, annotation.specialization().getElement(), annotation.specialization());
		} else {
			if(annotation.element() == null) {
				Bending.plugin.getLogger().severe("Trying to register ability : "+ability+" but element&specilization are not set ! Aborting this registration...");
				return;
			}
			_register(annotation.name(), ability, annotation.element(), null);
		}
	}
	
	private void _register(String name, Class<? extends Ability> ability, BendingType element, BendingSpecializationType specialization) {
		if(availableAbilities.containsKey(name)) {
			//Nope !
			Bending.plugin.getLogger().severe("Ability "+name+" is already register with class "+availableAbilities.get(name)+" ! Aborting registration...");
			return;
		}
		RegisteredAbility ra = new RegisteredAbility(name, ability, element, specialization);
		availableAbilities.put(name, ra);
	}
	
	public void applyConfiguration(File configDir) {
		configDir.mkdirs();
		File configFile = new File(configDir, "abilities_config.json");
		
		Map<String, Field> fields = new HashMap<String, Field>();
		for(RegisteredAbility ab : availableAbilities.values()) {
			for(Field f : ab.getAbility().getDeclaredFields()) {
				if (Modifier.isStatic(f.getModifiers())) {
					ConfigurationParameter an = f.getAnnotation(ConfigurationParameter.class);
					if (an != null) {
						fields.put(ab.getConfigPath()+"."+an.value().toLowerCase(), f);
					}
				}
			}
		}
		ConfigurationManager.applyConfiguration(configFile, fields);
	}

	
}

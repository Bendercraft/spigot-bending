package net.avatar.realms.spigot.bending.abilities;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.air.AirBlast;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;
import net.avatar.realms.spigot.bending.abilities.air.AirBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirManipulation;
import net.avatar.realms.spigot.bending.abilities.air.AirScooter;
import net.avatar.realms.spigot.bending.abilities.air.AirShield;
import net.avatar.realms.spigot.bending.abilities.air.AirSpeed;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.AirSuction;
import net.avatar.realms.spigot.bending.abilities.air.AirSwipe;
import net.avatar.realms.spigot.bending.abilities.air.Suffocate;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.chi.C4;
import net.avatar.realms.spigot.bending.abilities.chi.ChiSpeed;
import net.avatar.realms.spigot.bending.abilities.chi.Dash;
import net.avatar.realms.spigot.bending.abilities.chi.HighJump;
import net.avatar.realms.spigot.bending.abilities.chi.PoisonnedDart;
import net.avatar.realms.spigot.bending.abilities.chi.PowerfulHit;
import net.avatar.realms.spigot.bending.abilities.chi.RapidPunch;
import net.avatar.realms.spigot.bending.abilities.chi.SmokeBomb;
import net.avatar.realms.spigot.bending.abilities.chi.VitalPoint;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.earth.EarthPassive;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.abilities.earth.Tremorsense;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.Blaze;
import net.avatar.realms.spigot.bending.abilities.fire.Combustion;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlade;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.fire.HeatControl;
import net.avatar.realms.spigot.bending.abilities.fire.Illumination;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.water.HealingWaters;
import net.avatar.realms.spigot.bending.abilities.water.WaterBubble;
import net.avatar.realms.spigot.bending.abilities.water.WaterPassive;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;
import net.avatar.realms.spigot.bending.controller.ConfigurationManager;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;


public class AbilityManager {
	
	private static AbilityManager manager =  null;
	
	private Map<String, RegisteredAbility> availableAbilities;
	private Map<Abilities, Map<Object, IAbility>> abilities;
	
	public static AbilityManager getManager() {
		if (manager == null) {
			manager = new AbilityManager();
		}
		return manager;
	}
	
	private AbilityManager() {
		this.abilities = new HashMap<Abilities, Map<Object, IAbility>>();
		this.availableAbilities = new HashMap<String, RegisteredAbility>();
	}
	
	public void progressAllAbilities() {
		List<IAbility> toRemove = new LinkedList<IAbility>();
		for (Abilities key : this.abilities.keySet()) {
			for (IAbility ability : this.abilities.get(key).values()) {
				boolean canKeep = ability.progress();
				if (!canKeep) {
					toRemove.add(ability);
				}
			}
		}
		
		for (IAbility ability : toRemove) {
			ability.stop();
			ability.remove();
		}
	}
	
	public void stopAllAbilities() {
		for (Map<Object, IAbility> instances : this.abilities.values()) {
			for (IAbility ability : instances.values()) {
				ability.stop();
			}
		}
		
		clearAllAbilities();
	}
	
	private void clearAllAbilities() {
		for (Map<Object, IAbility> instances : this.abilities.values()) {
			instances.clear();
		}
	}
	
	public ActiveAbility buildAbility (Abilities abilityType, Player player) {
		switch (abilityType) {
			case AvatarState:
				return new AvatarState(player);
				
			case PlasticBomb:
				return new C4(player);
			case PoisonnedDart:
				return new PoisonnedDart(player);
			case HighJump:
				return new HighJump(player);
			case SmokeBomb:
				return new SmokeBomb(player);
			case PowerfulHit:
				return new PowerfulHit(player);
			case Dash:
				return new Dash(player);
			case Paralyze:
				return new VitalPoint(player);
			case RapidPunch:
				return new RapidPunch(player);
				
			case AirBlast:
				return new AirBlast(player);
			case AirBubble:
				return new AirBubble(player);
			case AirBurst:
				return new AirBurst(player);
			case AirManipulation:
				return new AirManipulation(player);
			case AirScooter:
				return new AirScooter(player);
			case AirShield:
				return new AirShield(player);
			case AirSpout:
				return new AirSpout(player);
			case AirSuction:
				return new AirSuction(player);
			case AirSwipe:
				return new AirSwipe(player);
			case Suffocate:
				return new Suffocate(player);
			case Tornado:
				return new Tornado(player);

			case Blaze:
				return new Blaze(player);
			case Combustion:
				return new Combustion(player);
			case FireJet:
				return new FireJet(player);
			case Illumination:
				return new Illumination(player);
			case Lightning:
				return new Lightning(player);
			case FireBlade:
				return new FireBlade(player);
			case HeatControl:
				return new HeatControl(player);
				
			case WaterBubble:
				return new WaterBubble(player);
			case WaterSpout:
				return new WaterSpout(player);
			case HealingWaters:
				return new HealingWaters(player);
				
			case Tremorsense:
				return new Tremorsense(player);
			case EarthBlast:
				return new EarthBlast(player);
			case LavaTrain:
				return new LavaTrain(player);
				
			default : return null;
		}
	}
	
	public void addInstance(IAbility instance) {
		Map<Object, IAbility> map = this.abilities.get(instance.getAbilityType());
		if (map == null) {
			map = new HashMap<Object, IAbility>();
			this.abilities.put(instance.getAbilityType(), map);
		}
		map.put(instance.getIdentifier(), instance);
	}
	
	public Map<Object, IAbility> getInstances(Abilities type) {
		if(this.abilities.containsKey(type)) {
			return this.abilities.get(type);
		}
		return null;
	}
	
	public boolean isUsingAbility(Player player, Abilities ability) {
		if (player == null) {
			return false;
		}
		
		if (ability == null) {
			return false;
		}
		
		Map<Object, IAbility> instances = getInstances(ability);
		if (instances == null) {
			return false;
		}
		
		for (IAbility ab : instances.values()) {
			if (ab.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				return true;
			}
		}
		
		return false;
	}
	
	public void registerAllAbilities() {
		
		register(AvatarState.class);
		
		register(PoisonnedDart.class);
		register(C4.class);
		register(Dash.class);
		register(HighJump.class);
		register(VitalPoint.class);
		register(PowerfulHit.class);
		register(RapidPunch.class);
		register(SmokeBomb.class);
		register(ChiSpeed.class);
		
		register(AirBlast.class);
		register(AirBubble.class);
		register(AirBurst.class);
		register(AirManipulation.class);
		register(AirScooter.class);
		register(AirShield.class);
		register(AirSpout.class);
		register(AirSuction.class);
		register(AirSwipe.class);
		register(AirSpeed.class);
		register(Suffocate.class);
		register(Tornado.class);
		
		//		register(Catapult.class);
		//		register(Collapse.class);
		//		register(CompactColumn.class);
		//		register(EarthArmor.class);
		register(EarthBlast.class);
		//		register(EarthColumn.class);
		//		register(EarthGrab.class);
		register(EarthPassive.class);
		//		register(EarthShield.class);
		//		register(EarthTunnel.class);
		//		register(EarthWall.class);
		register(LavaTrain.class);
		//		register(MetalBending.class);
		//		register(Ripple.class);
		//		register(Shockwave.class);
		//		register(ShockwaveArea.class);
		//		register(ShockwaveCone.class);
		//		register(ShockwaveFall.class);
		register(Tremorsense.class);
		
		register(HeatControl.class);
		register(Blaze.class);
		register(Combustion.class);
		//		register(Enflamed.class);
		register(FireBlade.class);
		register(FireBlast.class);
		//		register(FireBurst.class);
		//		register(FireBurstCone.class);
		//		register(FireBurstSphere.class);
		register(FireJet.class);
		//		register(FireProtection.class);
		//		register(FireShield.class);
		//		register(FireStream.class);
		register(Illumination.class);
		register(Lightning.class);
		//		register(WallOfFire.class);
		
		//		register(Bloodbending.class);
		//		register(Drainbending.class);
		//		register(FastSwmimming.class);
		//		register(FreezeMelt.class);
		register(HealingWaters.class);
		//		register(IceSpike.class);
		//		register(IceSpike2.class);
		//		register(IceSwipe.class);
		//		register(Melt.class);
		//		register(OctopusForm.class);
		//		register(Plantbending.class);
		//		register(SpikeField.class);
		//		register(Torrent.class);
		//		register(TorrentBurst.class);
		register(WaterBubble.class);
		//		register(WaterManipulation.class);
		register(WaterPassive.class);
		//		register(WaterReturn.class);
		register(WaterSpout.class);
		//		register(WaterWall.class);
		//		register(Wave.class);
	}
	
	protected void register(Class<? extends IAbility> ability) {
		BendingAbility annotation = ability.getAnnotation(BendingAbility.class);
		if(annotation == null) {
			Bending.plugin.getLogger().severe("Trying to register ability : "+ability+" but not annoted ! Aborting this registration...");
			return;
		}
		if((annotation.name() == null) || annotation.name().equals("")) {
			Bending.plugin.getLogger().severe("Trying to register ability : "+ability+" but name is null or empty ! Aborting this registration...");
			return;
		}
		if(annotation.specialization() != BendingSpecializationType.None) {
			_register(annotation.name(), ability, annotation.specialization().getElement(), annotation.specialization());
		} else {
			if(annotation.element() == BendingType.None) {
				Bending.plugin.getLogger().severe("Trying to register ability : "+ability+" but element&specilization are not set ! Aborting this registration...");
				return;
			}
			_register(annotation.name(), ability, annotation.element(), null);
		}
	}
	
	private void _register(String name, Class<? extends IAbility> ability, BendingType element, BendingSpecializationType specialization) {
		if(this.availableAbilities.containsKey(name)) {
			//Nope !
			Bending.plugin.getLogger().severe("Ability "+name+" is already register with class "+this.availableAbilities.get(name)+" ! Aborting registration...");
			return;
		}
		RegisteredAbility ra = new RegisteredAbility(name, ability, element, specialization);
		this.availableAbilities.put(name, ra);
	}
	
	public void applyConfiguration(File configDir) {
		configDir.mkdirs();
		File configFile = new File(configDir, "abilities_config.json");
		
		Map<String, Field> fields = new TreeMap<String, Field>();
		for(RegisteredAbility ab : this.availableAbilities.values()) {
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

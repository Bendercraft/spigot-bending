package net.avatar.realms.spigot.bending.abilities;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import net.avatar.realms.spigot.bending.abilities.earth.*;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.air.AirBlast;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;
import net.avatar.realms.spigot.bending.abilities.air.AirBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirGlide;
import net.avatar.realms.spigot.bending.abilities.air.AirScooter;
import net.avatar.realms.spigot.bending.abilities.air.AirShield;
import net.avatar.realms.spigot.bending.abilities.air.AirSlice;
import net.avatar.realms.spigot.bending.abilities.air.AirSpeed;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.AirSuction;
import net.avatar.realms.spigot.bending.abilities.air.AirSwipe;
import net.avatar.realms.spigot.bending.abilities.air.Suffocate;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.arts.Aim;
import net.avatar.realms.spigot.bending.abilities.arts.NebularChain;
import net.avatar.realms.spigot.bending.abilities.arts.C4;
import net.avatar.realms.spigot.bending.abilities.arts.Speed;
import net.avatar.realms.spigot.bending.abilities.arts.StraightShot;
import net.avatar.realms.spigot.bending.abilities.arts.Dash;
import net.avatar.realms.spigot.bending.abilities.arts.DirectHit;
import net.avatar.realms.spigot.bending.abilities.arts.ExplosiveShot;
import net.avatar.realms.spigot.bending.abilities.arts.HighJump;
import net.avatar.realms.spigot.bending.abilities.arts.Mark;
import net.avatar.realms.spigot.bending.abilities.arts.DaggerFall;
import net.avatar.realms.spigot.bending.abilities.arts.PoisonnedDart;
import net.avatar.realms.spigot.bending.abilities.arts.BlankPoint;
import net.avatar.realms.spigot.bending.abilities.arts.Slice;
import net.avatar.realms.spigot.bending.abilities.arts.SmokeBomb;
import net.avatar.realms.spigot.bending.abilities.arts.VitalPoint;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.Blaze;
import net.avatar.realms.spigot.bending.abilities.fire.Combustion;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlade;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurst;
import net.avatar.realms.spigot.bending.abilities.fire.FireFerret;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.fire.FireShield;
import net.avatar.realms.spigot.bending.abilities.fire.HeatControl;
import net.avatar.realms.spigot.bending.abilities.fire.Illumination;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.fire.FireWall;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.FastSwimming;
import net.avatar.realms.spigot.bending.abilities.water.HealingWaters;
import net.avatar.realms.spigot.bending.abilities.water.IceSpike;
import net.avatar.realms.spigot.bending.abilities.water.OctopusForm;
import net.avatar.realms.spigot.bending.abilities.water.PhaseChange;
import net.avatar.realms.spigot.bending.abilities.water.Torrent;
import net.avatar.realms.spigot.bending.abilities.water.WaterBubble;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.abilities.water.WaterPassive;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;
import net.avatar.realms.spigot.bending.abilities.water.WaterTurret;
import net.avatar.realms.spigot.bending.abilities.water.WaterWall;
import net.avatar.realms.spigot.bending.controller.ConfigurationManager;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.event.BendingRegisterEvent;

public class AbilityManager {

	//Singleton
	private static AbilityManager manager = null;

	private Map<String, RegisteredAbility> binds;
	private Map<Class<? extends BendingAbility>, String> reverseBinds;
	
	private Map<String, Map<Object, BendingAbility>> runnings;

	private AbilityManager() {
		this.runnings = new HashMap<String, Map<Object, BendingAbility>>();
		this.binds = new HashMap<String, RegisteredAbility>();
		this.reverseBinds = new HashMap<Class<? extends BendingAbility>, String>();
	}
	
	public static AbilityManager getManager() {
		if (manager == null) {
			manager = new AbilityManager();
		}
		return manager;
	}

	public void progressAllAbilities() {
		for (Map<Object, BendingAbility> abilities : this.runnings.values()) {
			Map<Object, BendingAbility> pendings = new HashMap<Object, BendingAbility>(abilities);
			for (Entry<Object, BendingAbility> entry : pendings.entrySet()) {
				if(entry.getValue().getState().equals(BendingAbilityState.ENDED)) {
					abilities.remove(entry.getKey());
				} else {
					entry.getValue().tick();
				}
			}
		}
	}

	public void stopAllAbilities() {
		for (Map<Object, BendingAbility> instances : this.runnings.values()) {
			for (BendingAbility ability : instances.values()) {
				ability.remove();
			}
		}

		clearAllAbilities();
	}

	private void clearAllAbilities() {
		for (Map<Object, BendingAbility> instances : this.runnings.values()) {
			instances.clear();
		}
	}

	public BendingActiveAbility buildAbility(String name, Player player) {
		RegisteredAbility registered = this.binds.get(name.toLowerCase());
		if (registered == null) {
			return null; // Invalid bind
		}
		Constructor<? extends BendingAbility> contructor = registered.getConstructor();
		if (contructor == null) {
			return null; // Invalid bind
		}
		try {
			BendingActiveAbility ab = (BendingActiveAbility) contructor.newInstance(registered, player);
			if (ab == null) {
				Bending.getInstance().getLogger().warning("Invalid class for ability " + name);
			}
			return ab;
		} catch (Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Invalid constructor for ability " + name, e);
		}
		return null;
	}

	public String getName(BendingAbility instance) {
		return reverseBinds.get(instance.getClass());
	}

	public void addInstance(BendingAbility instance) {
		String name = getName(instance);
		Map<Object, BendingAbility> map = runnings.get(name);
		if(map == null) {
			map = new HashMap<Object, BendingAbility>();
			runnings.put(name, map);
		}
		map.put(instance.getIdentifier(), instance);
	}

	public Map<Object, BendingAbility> getInstances(String name) {
		Map<Object, BendingAbility> result = runnings.get(name.toLowerCase());
		if(result == null) {
			result = new HashMap<Object, BendingAbility>();
		}
		return result;
	}

	public boolean isUsingAbility(Player player, String name) {
		if (player == null || name == null) {
			return false;
		}

		Map<Object, BendingAbility> instances = getInstances(name);
		if (instances == null || instances.isEmpty()) {
			return false;
		}

		for (BendingAbility ab : instances.values()) {
			if (ab.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				return true;
			}
		}

		return false;
	}

	public void registerAllAbilities() {

		register(AvatarState.class);
		
		register(Dash.class);
		register(HighJump.class);
		register(DirectHit.class);
		register(Speed.class);
		register(Aim.class);
		register(ExplosiveShot.class);
		register(StraightShot.class);
		register(BlankPoint.class);
		register(Mark.class);
		register(NebularChain.class);
		register(Slice.class);
		register(DaggerFall.class);
		register(PoisonnedDart.class);
		register(C4.class);
		register(SmokeBomb.class);
		register(VitalPoint.class);

		register(AirBlast.class);
		register(AirBubble.class);
		register(AirBurst.class);
		register(AirScooter.class);
		register(AirShield.class);
		register(AirSpout.class);
		register(AirGlide.class);
		register(AirSuction.class);
		register(AirSwipe.class);
		register(AirSpeed.class);
		register(AirSlice.class);
		register(Suffocate.class);
		register(Tornado.class);

		register(Catapult.class);
		register(Collapse.class);
		register(EarthArmor.class);
		register(EarthBlast.class);
		register(EarthGrab.class);
		register(EarthPassive.class);
		register(EarthTunnel.class);
		register(EarthWall.class);
		register(EarthLariat.class);
		register(LavaTrain.class);
		register(LavaFlow.class);
		register(LavaSpin.class);
		register(MetalBending.class);
		register(Shockwave.class);
		register(TremorSense.class);

		register(HeatControl.class);
		register(Blaze.class);
		register(Combustion.class);
		register(FireBlade.class);
		register(FireBlast.class);
		register(FireBurst.class);
		register(FireJet.class);
		register(FireShield.class);
		register(Illumination.class);
		register(FireFerret.class);
		register(Lightning.class);
		register(FireWall.class);

		register(Bloodbending.class);
		register(FastSwimming.class);
		register(PhaseChange.class);
		register(HealingWaters.class);
		register(IceSpike.class);
		register(OctopusForm.class);
		register(Torrent.class);
		register(WaterBubble.class);
		register(WaterManipulation.class);
		register(WaterPassive.class);
		register(WaterTurret.class);
		register(WaterSpout.class);
		register(WaterWall.class);
		
		Bending.callEvent(new BendingRegisterEvent());
	}

	protected void register(Class<? extends BendingAbility> ability) {
		ABendingAbility annotation = ability.getAnnotation(ABendingAbility.class);
		if (annotation == null) {
			Bending.getInstance().getLogger().severe("Trying to register ability : " + ability + " but not annoted ! Aborting this registration...");
			return;
		}
		if ((annotation.name() == null) || annotation.name().isEmpty()) {
			Bending.getInstance().getLogger().severe("Trying to register ability : " + ability + " but name is null or empty ! Aborting this registration...");
			return;
		}
		if (annotation.affinity() != BendingAffinity.NONE) {
			if (annotation.element() != BendingElement.NONE) {
				Bending.getInstance().getLogger().warning("Register ability : " + ability + " affinity is set to "+annotation.affinity()+"("+annotation.affinity().getElement()+") and element is set as well to "+annotation.element()+". Affinity's element will prevail in case of difference.");
			}
			register(annotation.name(), ability, annotation.affinity().getElement(), annotation.affinity(), annotation.shift(), annotation.passive());
		} else {
			if (annotation.element() == BendingElement.NONE) {
				Bending.getInstance().getLogger().severe("Trying to register ability : " + ability + " but element & affinity are not set ! Aborting this registration...");
				return;
			}
			register(annotation.name(), ability, annotation.element(), BendingAffinity.NONE, annotation.shift(), annotation.passive());
		}
	}

	private void register(String name, Class<? extends BendingAbility> ability, BendingElement element, BendingAffinity affinity, boolean shift, boolean passive) {
		if(!name.matches("^[a-zA-Z0-9_]*$")) {
			// Nope !
			Bending.getInstance().getLogger().severe("Ability " + name + " is not a valid name ([a-zA-Z0-9_]) ! Aborting registration...");
			return;
		}
		
		if (this.binds.containsKey(name.toLowerCase())) {
			// Nope !
			Bending.getInstance().getLogger().severe("Ability " + name + " is already register with class " + this.binds.get(name) + " ! Aborting registration...");
			return;
		}
		
		if(element == null || element == BendingElement.NONE) {
			// Nope !
			Bending.getInstance().getLogger().severe("Ability " + name + " has no element on its annotation ! Aborting registration...");
			return;
		}
		
		if(affinity != null && affinity != BendingAffinity.NONE && affinity.getElement() != element) {
			// Nope !
			Bending.getInstance().getLogger().severe("Ability " + name + " affinity and element are not matching (element is "+ element +" - affinity element is "+ affinity.getElement() +") ! Aborting registration...");
			return;
		}

		try {
			Constructor<? extends BendingAbility> constructor = ability.getConstructor(RegisteredAbility.class, Player.class);
			if (constructor == null) {
				Bending.getInstance().getLogger().severe("Bind " + name + " associated with class " + ability + " has no valid constructor <RegisteredAbility, Player>.");
				return;
			}
			RegisteredAbility ra = new RegisteredAbility(name, ability, element, affinity, shift, passive, constructor);
			this.binds.put(name.toLowerCase(), ra);
			this.reverseBinds.put(ra.getAbility(), name.toLowerCase());
		} catch (Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Bind " + name + " associated with class " + ability + " threw exception when getting constructor", e);
		}

	}

	public void applyConfiguration(File configDir) {
		configDir.mkdirs();
		File configFile = new File(configDir, "abilities_config.json");

		Map<String, Field> fields = new TreeMap<String, Field>();
		for (RegisteredAbility ab : this.binds.values()) {
			for (Field f : ab.getAbility().getDeclaredFields()) {
				if (Modifier.isStatic(f.getModifiers())) {
					ConfigurationParameter an = f.getAnnotation(ConfigurationParameter.class);
					if (an != null) {
						fields.put(ab.getConfigPath() + "." + an.value().toLowerCase(), f);
					}
				}
			}
		}
		ConfigurationManager.applyConfiguration(configFile, fields);
	}
	
	public RegisteredAbility getRegisteredAbility(String name) {
		return binds.get(name.toLowerCase());
	}
	
	public Collection<RegisteredAbility> getRegisteredAbilities() {
		return Collections.unmodifiableCollection(binds.values());
	}
}

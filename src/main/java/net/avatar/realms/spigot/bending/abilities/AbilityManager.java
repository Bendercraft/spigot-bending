package net.avatar.realms.spigot.bending.abilities;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.air.AirBlast;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;
import net.avatar.realms.spigot.bending.abilities.air.AirBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirGlide;
import net.avatar.realms.spigot.bending.abilities.air.AirScooter;
import net.avatar.realms.spigot.bending.abilities.air.AirShield;
import net.avatar.realms.spigot.bending.abilities.air.AirSpeed;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.AirSuction;
import net.avatar.realms.spigot.bending.abilities.air.AirSwipe;
import net.avatar.realms.spigot.bending.abilities.air.Suffocate;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.abilities.chi.AirSlice;
import net.avatar.realms.spigot.bending.abilities.chi.C4;
import net.avatar.realms.spigot.bending.abilities.chi.ChiSpeed;
import net.avatar.realms.spigot.bending.abilities.chi.Count;
import net.avatar.realms.spigot.bending.abilities.chi.Dash;
import net.avatar.realms.spigot.bending.abilities.chi.DirectHit;
import net.avatar.realms.spigot.bending.abilities.chi.EarthLariat;
import net.avatar.realms.spigot.bending.abilities.chi.FireFerret;
import net.avatar.realms.spigot.bending.abilities.chi.HighJump;
import net.avatar.realms.spigot.bending.abilities.chi.PoisonnedDart;
import net.avatar.realms.spigot.bending.abilities.chi.Release;
import net.avatar.realms.spigot.bending.abilities.chi.SmokeBomb;
import net.avatar.realms.spigot.bending.abilities.chi.VitalPoint;
import net.avatar.realms.spigot.bending.abilities.chi.WaterTurret;
import net.avatar.realms.spigot.bending.abilities.earth.Catapult;
import net.avatar.realms.spigot.bending.abilities.earth.Collapse;
import net.avatar.realms.spigot.bending.abilities.earth.EarthArmor;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.earth.EarthGrab;
import net.avatar.realms.spigot.bending.abilities.earth.EarthPassive;
import net.avatar.realms.spigot.bending.abilities.earth.EarthTunnel;
import net.avatar.realms.spigot.bending.abilities.earth.EarthWall;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.abilities.earth.MetalBending;
import net.avatar.realms.spigot.bending.abilities.earth.Shockwave;
import net.avatar.realms.spigot.bending.abilities.earth.Tremorsense;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.fire.Blaze;
import net.avatar.realms.spigot.bending.abilities.fire.Combustion;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlade;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurst;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.fire.FireShield;
import net.avatar.realms.spigot.bending.abilities.fire.HeatControl;
import net.avatar.realms.spigot.bending.abilities.fire.Illumination;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.fire.WallOfFire;
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
import net.avatar.realms.spigot.bending.abilities.water.WaterWall;
import net.avatar.realms.spigot.bending.controller.ConfigurationManager;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

public class AbilityManager {

	private static AbilityManager manager = null;

	private Map<BendingAbilities, RegisteredAbility> binds;
	private Map<BendingAbilities, Map<Object, IBendingAbility>> runnings;
	private Map<Class<? extends IBendingAbility>, BendingAbilities> reverseBinds;

	public static AbilityManager getManager() {
		if (manager == null) {
			manager = new AbilityManager();
		}
		return manager;
	}

	private AbilityManager() {
		this.runnings = new HashMap<BendingAbilities, Map<Object, IBendingAbility>>();
		this.binds = new HashMap<BendingAbilities, RegisteredAbility>();
		this.reverseBinds = new HashMap<Class<? extends IBendingAbility>, BendingAbilities>();
	}

	public void progressAllAbilities() {
		List<IBendingAbility> toRemove = new LinkedList<IBendingAbility>();
		for (Map<Object, IBendingAbility> abilities : this.runnings.values()) {
			for (IBendingAbility ability : abilities.values()) {
				if (!ability.progress()) {
					toRemove.add(ability);
				}
			}
		}

		for (IBendingAbility ability : toRemove) {
			ability.remove();
		}
	}

	public void stopAllAbilities() {
		for (Map<Object, IBendingAbility> instances : this.runnings.values()) {
			for (IBendingAbility ability : instances.values()) {
				ability.stop();
			}
		}

		clearAllAbilities();
	}

	private void clearAllAbilities() {
		for (Map<Object, IBendingAbility> instances : this.runnings.values()) {
			instances.clear();
		}
	}

	public BendingActiveAbility buildAbility(BendingAbilities abilityType, Player player) {
		RegisteredAbility registered = this.binds.get(abilityType);
		if (registered == null) {
			return null; // Invalid bind
		}
		Constructor<? extends IBendingAbility> contructor = registered.getConstructor();
		if (contructor == null) {
			return null; // Invalid bind
		}
		try {
			BendingActiveAbility ab = (BendingActiveAbility) contructor.newInstance(player);
			if (ab == null) {
				Bending.getInstance().getLogger().warning("Invalid class for ability " + abilityType);
			}
			return ab;
		} catch (Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Invalid constructor for ability " + abilityType, e);
		}
		return null;
	}

	public BendingAbilities getAbilityType(IBendingAbility instance) {
		return this.reverseBinds.get(instance.getClass());
	}

	public void addInstance(IBendingAbility instance) {
		BendingAbilities bind = getAbilityType(instance);
		Map<Object, IBendingAbility> map = this.runnings.get(bind);
		if (map == null) {
			map = new HashMap<Object, IBendingAbility>();
			this.runnings.put(bind, map);
		}
		map.put(instance.getIdentifier(), instance);
	}

	public Map<Object, IBendingAbility> getInstances(BendingAbilities type) {
		if (this.runnings.containsKey(type)) {
			return this.runnings.get(type);
		}
		return new HashMap<Object, IBendingAbility>();
	}

	public boolean isUsingAbility(Player player, BendingAbilities ability) {
		if (player == null) {
			return false;
		}

		if (ability == null) {
			return false;
		}

		Map<Object, IBendingAbility> instances = getInstances(ability);
		if (instances == null) {
			return false;
		}

		for (IBendingAbility ab : instances.values()) {
			if (ab.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				return true;
			}
		}

		return false;
	}

	public void registerAllAbilities() {

		register(AvatarState.class);

		register(Release.class);
		register(Count.class);
		register(PoisonnedDart.class);
		register(C4.class);
		register(Dash.class);
		register(HighJump.class);
		register(VitalPoint.class);
		register(DirectHit.class);
		register(SmokeBomb.class);
		register(ChiSpeed.class);
		register(AirSlice.class);
		register(WaterTurret.class);
		register(EarthLariat.class);
		register(FireFerret.class);

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
		register(LavaTrain.class);
		register(MetalBending.class);
		register(Shockwave.class);
		register(Tremorsense.class);

		register(HeatControl.class);
		register(Blaze.class);
		register(Combustion.class);
		register(FireBlade.class);
		register(FireBlast.class);
		register(FireBurst.class);
		register(FireJet.class);
		register(FireShield.class);
		register(Illumination.class);
		register(Lightning.class);
		register(WallOfFire.class);

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
		register(WaterSpout.class);
		register(WaterWall.class);
	}

	protected void register(Class<? extends IBendingAbility> ability) {
		BendingAbility annotation = ability.getAnnotation(BendingAbility.class);
		if (annotation == null) {
			Bending.getInstance().getLogger().severe("Trying to register ability : " + ability + " but not annoted ! Aborting this registration...");
			return;
		}
		if ((annotation.name() == null) || annotation.name().isEmpty()) {
			Bending.getInstance().getLogger().severe("Trying to register ability : " + ability + " but name is null or empty ! Aborting this registration...");
			return;
		}
		if (annotation.affinity() != BendingAffinity.None) {
			_register(annotation.name(), annotation.bind(), ability, annotation.affinity().getElement(), annotation.affinity());
		} else {
			if (annotation.element() == BendingElement.None) {
				Bending.getInstance().getLogger().severe("Trying to register ability : " + ability + " but element&specilization are not set ! Aborting this registration...");
				return;
			}
			_register(annotation.name(), annotation.bind(), ability, annotation.element(), null);
		}
	}

	private void _register(String name, BendingAbilities bind, Class<? extends IBendingAbility> ability, BendingElement element, BendingAffinity specialization) {
		if (this.binds.containsKey(name)) {
			// Nope !
			Bending.getInstance().getLogger().severe("Ability " + bind + "(" + name + ") is already register with class " + this.binds.get(name) + " ! Aborting registration...");
			return;
		}

		try {
			Constructor<? extends IBendingAbility> constructor = ability.getConstructor(Player.class);
			if (constructor == null) {
				Bending.getInstance().getLogger().severe("Bind " + bind + " associated with class " + ability + " has no valid constructor with just one player.");
				return;
			}
			RegisteredAbility ra = new RegisteredAbility(name, ability, element, specialization, constructor);
			this.binds.put(bind, ra);
			this.reverseBinds.put(ra.getAbility(), bind);
		} catch (Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Bind " + bind + " associated with class " + ability + " threw exception when getting constructor", e);
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
}

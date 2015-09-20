package net.avatar.realms.spigot.bending.abilities;

import java.util.LinkedList;
import java.util.List;

/**
 * This class list all abilities
 */
public enum Abilities {
	
	/*
	 * AIR Abilities
	 */
	AirBlast (BendingType.Air, true),
	AirBubble (BendingType.Air, true),
	AirShield (BendingType.Air, true),
	AirSuction (BendingType.Air, true),
	AirSwipe (BendingType.Air, true),
	AirScooter (BendingType.Air, false),
	AirSpout (BendingType.Air, false),
	AirBurst (BendingType.Air, true),
	AirSpeed (BendingType.Air, true),
	AirManipulation (BendingType.Air, true),
	Tornado (BendingSpecializationType.Tornado, true),
	Suffocate (BendingSpecializationType.Suffocate, true),
	
	/*
	 * EARTH Abilities
	 */
	
	Catapult (BendingType.Earth, true),
	RaiseEarth (BendingType.Earth, true),
	EarthGrab (BendingType.Earth, true),
	EarthTunnel (BendingType.Earth, true),
	EarthBlast (BendingType.Earth, true),
	Collapse (BendingType.Earth, true),
	Tremorsense (BendingType.Earth, true),
	EarthArmor (BendingType.Earth, true),
	Shockwave (BendingType.Earth, true),
	LavaTrain (BendingSpecializationType.Lavabend, true),
	MetalBending (BendingSpecializationType.Metalbend, true),
	
	/*
	 * FIRE Abilities
	 */
	
	HeatControl (BendingType.Fire, true),
	Blaze (BendingType.Fire, true),
	FireJet (BendingType.Fire, false),
	Illumination (BendingType.Fire, false),
	WallOfFire (BendingType.Fire, false),
	FireBlast (BendingType.Fire, true),
	FireBurst (BendingType.Fire, true),
	FireShield (BendingType.Fire, true),
	FireBlade (BendingType.Fire, false),
	Combustion (BendingSpecializationType.Combustion, true),
	Lightning (BendingSpecializationType.Lightning, true),
	
	/*
	 * WATER Abilities
	 */
	
	WaterBubble (BendingType.Water, true),
	PhaseChange (BendingType.Water, true),
	HealingWaters (BendingType.Water, true),
	WaterManipulation (BendingType.Water, true),
	Surge (BendingType.Water, true),
	WaterSpout (BendingType.Water, false),
	IceSpike (BendingType.Water, true),
	OctopusForm (BendingType.Water, true),
	Torrent (BendingType.Water, true),
	IceSwipe (BendingType.Water, true),
	Bloodbending (BendingSpecializationType.Bloodbend, true),
	Drainbending (BendingSpecializationType.DrainBend, false),
	
	/*
	 * CHI-BLOCKERS Abilities
	 */
	
	HighJump (BendingType.ChiBlocker, false),
	ChiSpeed (BendingType.ChiBlocker, false),
	RapidPunch (BendingType.ChiBlocker, false),
	Paralyze (BendingType.ChiBlocker, false),
	SmokeBomb (BendingType.ChiBlocker, false),
	Dash (BendingType.ChiBlocker, true),
	PowerfulHit (BendingType.ChiBlocker, false),
	PoisonnedDart (BendingSpecializationType.Inventor, false),
	PlasticBomb (BendingSpecializationType.Inventor, true),
	
	/*
	 * AVATAR Abilities
	 */
	
	AvatarState (BendingType.Energy, false),
	
	/*
	 * PASSIVE Abilities
	 */
	
	AirPassive (BendingType.Air, false),
	ChiPassive (BendingType.ChiBlocker, false),
	EarthPassive (BendingType.Earth, false),
	FirePassive (BendingType.Fire, false),
	FastSwimming (BendingType.Water, true),
	WaterPassive (BendingType.Water, false),
	
	/*
	 * UTILITY Abilities
	 */
	
	FireStream (BendingType.Fire, false);
	
	/*
	 *
	 */
	
	private BendingType element;
	private BendingSpecializationType specialization;
	private boolean shift;
	
	Abilities (BendingType element, boolean shift) {
		this.element = element;
		this.specialization = null;
		this.shift = shift;
	}
	
	Abilities (BendingSpecializationType specialization, boolean shift) {
		this.element = specialization.getElement();
		this.specialization = specialization;
		this.shift = shift;
	}
	
	public BendingType getElement () {
		return this.element;
	}
	
	public BendingSpecializationType getSpecialization () {
		return this.specialization;
	}
	
	public boolean isSpecialization () {
		if (this.specialization != null) {
			return true;
		}
		return false;
	}
	
	// Static methods
	public static Abilities getAbility (String ability) {
		for (Abilities a : Abilities.values()) {
			if (ability.equalsIgnoreCase(a.name())) {
				return a;
			}
		}
		return null;
	}
	
	public static boolean isAirbending (Abilities ability) {
		return ability.isAirbending();
	}
	
	public boolean isAirbending () {
		return getElement().equals(BendingType.Air);
	}
	
	public static List<Abilities> getAirbendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (a.isAirbending()) {
				result.add(a);
			}
		}
		return result;
	}
	
	public static boolean isWaterbending (Abilities ability) {
		return ability.isWaterbending();
	}
	
	public boolean isWaterbending () {
		return getElement().equals(BendingType.Water);
	}
	
	public static List<Abilities> getWaterbendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (a.isWaterbending()) {
				result.add(a);
			}
		}
		return result;
	}
	
	public static boolean isEarthbending (Abilities ability) {
		return ability.isEarthbending();
	}
	
	public boolean isEarthbending () {
		return getElement().equals(BendingType.Earth);
	}
	
	public static List<Abilities> getEarthbendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (a.isEarthbending()) {
				result.add(a);
			}
		}
		return result;
	}
	
	public static boolean isFirebending (Abilities ability) {
		return ability.isFirebending();
	}
	
	public boolean isFirebending () {
		return getElement().equals(BendingType.Fire);
	}
	
	public static List<Abilities> getFirebendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (a.isFirebending()) {
				result.add(a);
			}
		}
		return result;
	}
	
	public static boolean isChiBlocking (Abilities ability) {
		return ability.isChiblocking();
	}
	
	public boolean isChiblocking () {
		return getElement().equals(BendingType.ChiBlocker);
	}
	
	public static List<Abilities> getChiBlockingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (a.isChiblocking()) {
				result.add(a);
			}
		}
		return result;
	}
	
	public boolean isShiftAbility () {
		return this.shift;
	}
	
	public static boolean isEnergyAbility (Abilities ab) {
		if (ab.getElement().equals(BendingType.Energy)) {
			return true;
		}
		return false;
	}
	
	public boolean isEnergyAbility () {
		if (getElement().equals(BendingType.Energy)) {
			return true;
		}
		return false;
	}
	
	// TODO : Make sure passive abilities cannot be bound
	public boolean isPassiveAbility () {
		switch (this) {
			case AirPassive:
			case AirSpeed:
			case ChiPassive:
			case ChiSpeed:
			case EarthPassive:
			case FirePassive:
			case FastSwimming:
			case WaterPassive:
				return true;
			default:
				return false;
				
		}
	}
	
	public boolean isUtilityAbility () {
		switch (this) {
			case FireStream:
				return true;
			default:
				return false;
		}
	}

	public String getPermission () {
		return "bending." + getElement().toString().toLowerCase() + name().toLowerCase();
	}
}

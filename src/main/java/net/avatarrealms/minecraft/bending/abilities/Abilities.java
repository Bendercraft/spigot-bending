package net.avatarrealms.minecraft.bending.abilities;

import java.util.LinkedList;
import java.util.List;

/**
 * This class list all bindable abilities
 *
 */
public enum Abilities {
	AirBlast(BendingType.Air), 
	AirBubble(BendingType.Air), 
	AirShield(BendingType.Air), 
	AirSuction(BendingType.Air), 
	AirSwipe(BendingType.Air), 
	AirScooter(BendingType.Air),
	AirSpout(BendingType.Air), 
	AirBurst(BendingType.Air), 
	AirManipulation(BendingType.Air), 
	Tornado(BendingSpecializationType.Tornado), 
	Suffocate(BendingSpecializationType.Suffocate),

	Catapult(BendingType.Earth), 
	RaiseEarth(BendingType.Earth), 
	EarthGrab(BendingType.Earth), 
	EarthTunnel(BendingType.Earth), 
	EarthBlast(BendingType.Earth), 
	Collapse(BendingType.Earth), 
	Tremorsense(BendingType.Earth),
	EarthArmor(BendingType.Earth), 
	Shockwave(BendingType.Earth), 
	EarthMelt(BendingSpecializationType.Lavabend), 
	LavaTrain(BendingSpecializationType.Lavabend), 
	MetalBending(BendingSpecializationType.Metalbend),

	HeatControl(BendingType.Fire), 
	Blaze(BendingType.Fire), 
	FireJet(BendingType.Fire), 
	Illumination(BendingType.Fire), 
	WallOfFire(BendingType.Fire), 
	FireBlast(BendingType.Fire), 
	FireBurst(BendingType.Fire), 
	FireShield(BendingType.Fire), 
	FireBlade(BendingType.Fire),
	Combustion(BendingSpecializationType.Combustion),
	Lightning(BendingSpecializationType.Lightning),

	WaterBubble(BendingType.Water), 
	PhaseChange(BendingType.Water), 
	HealingWaters(BendingType.Water), 
	WaterManipulation(BendingType.Water), 
	Surge(BendingType.Water),
	WaterSpout(BendingType.Water),
	IceSpike(BendingType.Water), 
	OctopusForm(BendingType.Water), 
	Torrent(BendingType.Water), 
	IceSwipe(BendingType.Water),
	Bloodbending(BendingSpecializationType.Bloodbend), 
	Drainbending(BendingSpecializationType.DrainBend), 

	HighJump(BendingType.ChiBlocker), 
	RapidPunch(BendingType.ChiBlocker), 
	Paralyze(BendingType.ChiBlocker), 
	SmokeBomb(BendingType.ChiBlocker), 
	PoisonnedDart(BendingType.ChiBlocker), 
	Dash(BendingType.ChiBlocker),

	AvatarState(BendingType.Energy);
	
	private BendingType element;
	private BendingSpecializationType specialization;
	
	Abilities(BendingType element) {
		this.element = element;
		this.specialization = null;
	}
	
	Abilities(BendingSpecializationType specialization) {
		this.element = specialization.getElement();
		this.specialization = specialization;
	}
	
	public BendingType getElement() {
		return element;
	}
	public BendingSpecializationType getSpecialization() {
		return specialization;
	}
	public boolean isSpecialization() {
		if(specialization != null) {
			return true;
		}
		return false;
	}
	
	
	
	
	//Statci methods

	public static Abilities getAbility(String ability) {
		for (Abilities a : Abilities.values()) {
			if (ability.equalsIgnoreCase(a.name())) {
				return a;
			}
		}
		return null;
	}

	public static boolean isAirbending(Abilities ability) {
		if(ability.getElement().equals(BendingType.Air)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getAirbendingAbilities() {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isAirbending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isWaterbending(Abilities ability) {
		if(ability.getElement().equals(BendingType.Water)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getWaterbendingAbilities() {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isWaterbending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isEarthbending(Abilities ability) {
		if(ability.getElement().equals(BendingType.Earth)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getEarthbendingAbilities() {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isEarthbending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isFirebending(Abilities ability) {
		if(ability.getElement().equals(BendingType.Fire)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getFirebendingAbilities() {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isFirebending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isChiBlocking(Abilities ability) {
		if(ability.getElement().equals(BendingType.ChiBlocker)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getChiBlockingAbilities() {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isChiBlocking(a)) {
				result.add(a);
			}
		}
		return result;
	}
}

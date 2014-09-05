package net.avatarrealms.minecraft.bending.abilities;

import java.util.ArrayList;
import java.util.Arrays;

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
	LungsControl(BendingSpecializationType.Suffocate),

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
	LavaBlast(BendingSpecializationType.Lavabend), 
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

	public static String[] getAirbendingAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		for (Abilities a : Abilities.values()) {
			if (isAirbending(a) && !isSecret(a)) {
				list.add(a.name());
			}
		}
		String[] abilities = list.toArray(new String[list.size()]);
		Arrays.sort(abilities);
		return abilities;
	}

	public static boolean isWaterbending(Abilities ability) {
		if(ability.getElement().equals(BendingType.Water)) {
			return true;
		}
		return false;
	}

	public static String[] getWaterbendingAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		for (Abilities a : Abilities.values()) {
			if (isWaterbending(a) && !isSecret(a)) {
				list.add(a.name());
			}
		}
		String[] abilities = list.toArray(new String[list.size()]);
		Arrays.sort(abilities);
		return abilities;
	}

	public static boolean isEarthbending(Abilities ability) {
		if(ability.getElement().equals(BendingType.Earth)) {
			return true;
		}
		return false;
	}

	public static String[] getEarthbendingAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		for (Abilities a : Abilities.values()) {
			if (isEarthbending(a) && !isSecret(a)) {
				list.add(a.name());
			}
		}
		String[] abilities = list.toArray(new String[list.size()]);
		Arrays.sort(abilities);
		return abilities;
	}

	public static boolean isFirebending(Abilities ability) {
		if(ability.getElement().equals(BendingType.Fire)) {
			return true;
		}
		return false;
	}

	public static String[] getFirebendingAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		for (Abilities a : Abilities.values()) {
			if (isFirebending(a) && !isSecret(a)) {
				list.add(a.name());
			}
		}
		String[] abilities = list.toArray(new String[list.size()]);
		Arrays.sort(abilities);
		return abilities;
	}

	public static boolean isChiBlocking(Abilities ability) {
		if(ability.getElement().equals(BendingType.ChiBlocker)) {
			return true;
		}
		return false;
	}

	public static String[] getChiBlockingAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		for (Abilities a : Abilities.values()) {
			if (isChiBlocking(a) && !isSecret(a)) {
				list.add(a.name());
			}
		}
		String[] abilities = list.toArray(new String[list.size()]);
		Arrays.sort(abilities);
		return abilities;
	}

	public static boolean isSecret(Abilities ability) {
		// Temp method to make players not see new abilities
		switch (ability) {
		case AirManipulation :
		case LungsControl :
		case EarthMelt :
		case LavaBlast :
		case MetalBending :
		case Combustion :
		case FireBlade :
		case IceSwipe :
		case Dash :
		case PoisonnedDart: return true;			
		default : return false;
		}
	}
}

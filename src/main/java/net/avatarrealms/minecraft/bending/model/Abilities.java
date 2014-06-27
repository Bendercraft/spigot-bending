package net.avatarrealms.minecraft.bending.model;

import java.util.ArrayList;
import java.util.Arrays;

public enum Abilities {

	AirBlast, AirBubble, AirShield, AirSuction, AirSwipe, Tornado, AirScooter,
	AirSpout, AirBurst, AirManipulation, LungsControl,

	Catapult, RaiseEarth, EarthGrab, EarthTunnel, EarthBlast, Collapse, Tremorsense,
	EarthArmor, Shockwave, EarthMelt,

	HeatControl, Blaze, FireJet, Illumination, WallOfFire, FireBlast, Lightning,
	FireBurst, FireShield, MentalExplosion, ThunderArmor,

	WaterBubble, PhaseChange, HealingWaters, WaterManipulation, Surge, Bloodbending, WaterSpout,
	IceSpike, OctopusForm, Torrent, IceSwipe,

	HighJump, RapidPunch, Paralyze, SmokeBomb, PoisonnedDart,

	AvatarState;

	public static Abilities getAbility(String ability) {
		for (Abilities a : Abilities.values()) {
			if (ability.equalsIgnoreCase(a.name())) {
				return a;
			}
		}
		return null;
	}

	public static boolean isAirbending(Abilities ability) {
		switch (ability) {
		case AirBlast:
		case AirBubble :
		case AirShield : 
		case AirSuction : 
		case AirSwipe : 
		case Tornado : 
		case AirScooter : 
		case AirSpout : 
		case AirBurst :
		case AirManipulation :
		case LungsControl : return true;
		default : return false;
		}
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
		switch (ability) {
		case WaterBubble : 
		case PhaseChange :
		case HealingWaters : 
		case WaterManipulation : 
		case Surge : 
		case Bloodbending :
		case WaterSpout : 
		case IceSpike : 
		case OctopusForm : 
		case Torrent : 
		case IceSwipe : return true;		
		default : return false;
		}

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
		switch (ability) {
		case Catapult :
		case RaiseEarth :
		case EarthGrab :
		case EarthTunnel :
		case EarthBlast : 
		case Collapse: 
		case Tremorsense :
		case Shockwave :
		case EarthArmor :
		case EarthMelt : return true;
		default : return false;
		}
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
		switch (ability) {
		case HeatControl :
		case Blaze :
		case FireJet:
		case Illumination:
		case WallOfFire :
		case FireBlast :
		case Lightning:
		case FireBurst :
		case FireShield:
		case MentalExplosion :
		case ThunderArmor : return true;
		default : return false;
		}
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
		switch (ability) {
		case HighJump : 
		case RapidPunch :
		case Paralyze :
		case SmokeBomb :
		case PoisonnedDart : return true;
		default : return false;
		}
		
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
		case MentalExplosion :
		case ThunderArmor :
		case IceSwipe :
		case PoisonnedDart: return true;			
		default : return false;
		}
	}
}

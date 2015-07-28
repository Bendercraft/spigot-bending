package net.avatar.realms.spigot.bending.abilities;

import java.util.LinkedList;
import java.util.List;


/**
 * This class list all bindable abilities
 *
 */
public enum Abilities {

	/*
	 * AIR Abilities
	 */
	AirBlast(BendingType.Air, true),
	AirBubble(BendingType.Air, false),
	AirShield(BendingType.Air, true),
	AirSuction(BendingType.Air, true),
	AirSwipe(BendingType.Air, true),
	AirScooter(BendingType.Air, false),
	AirSpout(BendingType.Air, false),
	AirBurst(BendingType.Air, true),
	AirManipulation(BendingType.Air, true),
	Tornado(BendingSpecializationType.Tornado, true),
	Suffocate(BendingSpecializationType.Suffocate, true),

	/*
	 * 
	 * EARTH Abilities
	 */

	Catapult(BendingType.Earth, true),
	RaiseEarth(BendingType.Earth, true),
	EarthGrab(BendingType.Earth, true),
	EarthTunnel(BendingType.Earth, true),
	EarthBlast(BendingType.Earth, true),
	Collapse(BendingType.Earth, true),
	Tremorsense(BendingType.Earth, true),
	EarthArmor(BendingType.Earth, true),
	Shockwave(BendingType.Earth, true),
	LavaTrain(BendingSpecializationType.Lavabend, true),
	MetalBending(BendingSpecializationType.Metalbend, true),

	/*
	 * 
	 * FIRE Abilities
	 */

	HeatControl(BendingType.Fire, true),
	Blaze(BendingType.Fire, true),
	FireJet(BendingType.Fire, false),
	Illumination(BendingType.Fire, false),
	WallOfFire(BendingType.Fire, false),
	FireBlast(BendingType.Fire, true),
	FireBurst(BendingType.Fire, true),
	FireShield(BendingType.Fire, true),
	FireBlade(BendingType.Fire, false),
	Combustion(BendingSpecializationType.Combustion, true),
	Lightning(BendingSpecializationType.Lightning, true),

	/*
	 * 
	 * WATER Abilities
	 */

	WaterBubble(BendingType.Water, true),
	PhaseChange(BendingType.Water, true),
	HealingWaters(BendingType.Water, true),
	WaterManipulation(BendingType.Water, true),
	Surge(BendingType.Water, true),
	WaterSpout(BendingType.Water, false),
	IceSpike(BendingType.Water, true),
	OctopusForm(BendingType.Water, true),
	Torrent(BendingType.Water, true),
	IceSwipe(BendingType.Water, true),
	Bloodbending(BendingSpecializationType.Bloodbend, true),
	Drainbending(BendingSpecializationType.DrainBend, false),

	/*
	 * 
	 * CHI-BLOCKERS Abilities
	 */

	HighJump(BendingType.ChiBlocker, false),
	RapidPunch(BendingType.ChiBlocker, false),
	Paralyze(BendingType.ChiBlocker, false),
	SmokeBomb(BendingType.ChiBlocker, false),
	Dash(BendingType.ChiBlocker, true),
	PowerfulHit(BendingType.ChiBlocker, false),
	PoisonnedDart(BendingSpecializationType.Inventor, false),
	PlasticBomb(BendingSpecializationType.Inventor, true),

	/*
	 * 
	 * AVATAR Abilities
	 */

	AvatarState(BendingType.Energy, false);

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
		if (ability.getElement().equals(BendingType.Air)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getAirbendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isAirbending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isWaterbending (Abilities ability) {
		if (ability.getElement().equals(BendingType.Water)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getWaterbendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isWaterbending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isEarthbending (Abilities ability) {
		if (ability.getElement().equals(BendingType.Earth)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getEarthbendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isEarthbending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isFirebending (Abilities ability) {
		if (ability.getElement().equals(BendingType.Fire)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getFirebendingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isFirebending(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isChiBlocking (Abilities ability) {
		if (ability.getElement().equals(BendingType.ChiBlocker)) {
			return true;
		}
		return false;
	}

	public static List<Abilities> getChiBlockingAbilities () {
		List<Abilities> result = new LinkedList<Abilities>();
		for (Abilities a : Abilities.values()) {
			if (isChiBlocking(a)) {
				result.add(a);
			}
		}
		return result;
	}

	public boolean isShiftAbility () {
		return this.shift;
	}
}

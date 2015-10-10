package net.avatar.realms.spigot.bending.abilities;

import java.util.LinkedList;
import java.util.List;

/**
 * This class list all abilities
 */
public enum BendingAbilities {
	/*
	 * AIR Abilities
	 */
	AirBlast(BendingElement.Air, true), 
	AirBubble(BendingElement.Air, false),
	AirShield(BendingElement.Air, true), 
	AirSuction(BendingElement.Air, true), 
	AirSwipe(BendingElement.Air, true), 
	AirScooter(BendingElement.Air, false), 
	AirSpout(BendingElement.Air, false), 
	AirBurst(BendingElement.Air, true), 
	AirSpeed(BendingElement.Air, true), 
	AirGlide(BendingElement.Air, true),
	AirManipulation(BendingElement.Air, true), 
	Tornado(BendingAffinity.Tornado, true), 
	Suffocate(BendingAffinity.Suffocate, false),

	/*
	 * EARTH Abilities
	 */
	Catapult(BendingElement.Earth, true), 
	RaiseEarth(BendingElement.Earth, true), 
	EarthGrab(BendingElement.Earth, true), 
	EarthTunnel(BendingElement.Earth, true), 
	EarthBlast(BendingElement.Earth, true), 
	Collapse(BendingElement.Earth, true), 
	Tremorsense(BendingElement.Earth, true), 
	EarthArmor(BendingElement.Earth, true), 
	Shockwave(BendingElement.Earth, true), 
	LavaTrain(BendingAffinity.Lavabend, true), 
	MetalBending(BendingAffinity.Metalbend, true),

	/*
	 * FIRE Abilities
	 */
	HeatControl(BendingElement.Fire, true), 
	Blaze(BendingElement.Fire, true), 
	FireJet(BendingElement.Fire, false), 
	Illumination(BendingElement.Fire, false), 
	WallOfFire(BendingElement.Fire, false), 
	FireBlast(BendingElement.Fire, true), 
	FireBurst(BendingElement.Fire, true), 
	FireShield(BendingElement.Fire, true), 
	FireBlade(BendingElement.Fire, false), 
	Combustion(BendingAffinity.Combustion, true), 
	Lightning(BendingAffinity.Lightning, true),

	/*
	 * WATER Abilities
	 */
	WaterBubble(BendingElement.Water, false),
	PhaseChange(BendingElement.Water, true), 
	HealingWaters(BendingElement.Water, true), 
	WaterManipulation(BendingElement.Water, true), 
	Surge(BendingElement.Water, true), 
	WaterSpout(BendingElement.Water, false), 
	IceSpike(BendingElement.Water, true), 
	OctopusForm(BendingElement.Water, true), 
	Torrent(BendingElement.Water, true), 
	IceSwipe(BendingElement.Water, true), 
	Bloodbending(BendingAffinity.Bloodbend, true), 
	Drainbending(BendingAffinity.DrainBend, false),

	/*
	 * CHI-BLOCKERS Abilities
	 */
	Release(BendingElement.ChiBlocker, false), 
	Count(BendingElement.ChiBlocker, false), 
	HighJump(BendingElement.ChiBlocker, false), 
	ChiSpeed(BendingElement.ChiBlocker, false), 
	VitalPoint(BendingElement.ChiBlocker, false), 
	SmokeBomb(BendingElement.ChiBlocker, false), 
	Dash(BendingElement.ChiBlocker, true), 
	DirectHit(BendingElement.ChiBlocker, false), 
	PoisonnedDart(BendingAffinity.Inventor, false), 
	PlasticBomb(BendingAffinity.Inventor, true),
	AirSlice(BendingAffinity.ChiAir, true),
	WaterTurret(BendingAffinity.ChiWater, true),
	EarthLariat(BendingAffinity.ChiEarth, true),
	FireFerret(BendingAffinity.ChiFire, true),

	/*
	 * AVATAR Abilities
	 */
	AvatarState(BendingElement.Energy, false),

	/*
	 * PASSIVE Abilities
	 */
	AirPassive(BendingElement.Air, false), 
	ChiPassive(BendingElement.ChiBlocker, false), 
	EarthPassive(BendingElement.Earth, false), 
	FirePassive(BendingElement.Fire, false), 
	FastSwimming(BendingElement.Water, true), 
	WaterPassive(BendingElement.Water, false);

	private BendingElement element;
	private BendingAffinity affinity;
	private boolean shift;

	BendingAbilities(BendingElement element, boolean shift) {
		this.element = element;
		this.affinity = null;
		this.shift = shift;
	}

	BendingAbilities(BendingAffinity specialization, boolean shift) {
		this.element = specialization.getElement();
		this.affinity = specialization;
		this.shift = shift;
	}

	public BendingElement getElement() {
		return this.element;
	}

	public BendingAffinity getAffinity() {
		return this.affinity;
	}

	public boolean isAffinity() {
		if (this.affinity != null) {
			return true;
		}
		return false;
	}

	// Static methods
	public static BendingAbilities getAbility(String ability) {
		for (BendingAbilities a : BendingAbilities.values()) {
			if (ability.equalsIgnoreCase(a.name())) {
				return a;
			}
		}
		return null;
	}

	public static boolean isAirbending(BendingAbilities ability) {
		return ability.isAirbending();
	}

	public boolean isAirbending() {
		return getElement().equals(BendingElement.Air);
	}

	public static List<BendingAbilities> getAirbendingAbilities() {
		List<BendingAbilities> result = new LinkedList<BendingAbilities>();
		for (BendingAbilities a : BendingAbilities.values()) {
			if (a.isAirbending()) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isWaterbending(BendingAbilities ability) {
		return ability.isWaterbending();
	}

	public boolean isWaterbending() {
		return getElement().equals(BendingElement.Water);
	}

	public static List<BendingAbilities> getWaterbendingAbilities() {
		List<BendingAbilities> result = new LinkedList<BendingAbilities>();
		for (BendingAbilities a : BendingAbilities.values()) {
			if (a.isWaterbending()) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isEarthbending(BendingAbilities ability) {
		return ability.isEarthbending();
	}

	public boolean isEarthbending() {
		return getElement().equals(BendingElement.Earth);
	}

	public static List<BendingAbilities> getEarthbendingAbilities() {
		List<BendingAbilities> result = new LinkedList<BendingAbilities>();
		for (BendingAbilities a : BendingAbilities.values()) {
			if (a.isEarthbending()) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isFirebending(BendingAbilities ability) {
		return ability.isFirebending();
	}

	public boolean isFirebending() {
		return getElement().equals(BendingElement.Fire);
	}

	public static List<BendingAbilities> getFirebendingAbilities() {
		List<BendingAbilities> result = new LinkedList<BendingAbilities>();
		for (BendingAbilities a : BendingAbilities.values()) {
			if (a.isFirebending()) {
				result.add(a);
			}
		}
		return result;
	}

	public static boolean isChiBlocking(BendingAbilities ability) {
		return ability.isChiblocking();
	}

	public boolean isChiblocking() {
		return getElement().equals(BendingElement.ChiBlocker);
	}

	public static List<BendingAbilities> getChiBlockingAbilities() {
		List<BendingAbilities> result = new LinkedList<BendingAbilities>();
		for (BendingAbilities a : BendingAbilities.values()) {
			if (a.isChiblocking()) {
				result.add(a);
			}
		}
		return result;
	}

	public boolean isShiftAbility() {
		return this.shift;
	}

	public static boolean isEnergyAbility(BendingAbilities ab) {
		if (ab.getElement().equals(BendingElement.Energy)) {
			return true;
		}
		return false;
	}

	public boolean isEnergyAbility() {
		if (getElement().equals(BendingElement.Energy)) {
			return true;
		}
		return false;
	}

	public boolean isPassiveAbility() {
		switch (this) {
			case AirPassive:
			case AirGlide:
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

	public boolean isBindable() {
		if (isPassiveAbility()) {
			return false;
		}

		if(this == Drainbending) {
			return false;
		}
		return true;
	}

	public String getPermission() {
		return "bending." + this.element.name().toLowerCase() + "." + name().toLowerCase();
	}

	public static List<BendingAbilities> getElementAbilities(BendingElement el) {
		List<BendingAbilities> abilities = new LinkedList<BendingAbilities>();
		for (BendingAbilities ability : values()) {
			if ((ability.element == el) && ability.isBindable()) {
				abilities.add(ability);
			}
		}
		return abilities;
	}
}

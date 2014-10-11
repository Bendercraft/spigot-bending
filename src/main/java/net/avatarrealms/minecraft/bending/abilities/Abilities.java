package net.avatarrealms.minecraft.bending.abilities;

import java.util.LinkedList;
import java.util.List;

/**
 * This class list all bindable abilities
 *
 */
public enum Abilities {
	
	/*
	 *  AIR Abilities
	 */
	AirBlast(BendingType.Air) {
		public String getDescription() {
			return "AirBlast is the most fundamental bending technique of an airbender."
					+ " To use, simply left-click in a direction. A gust of wind will be"
					+ " created at your fingertips, launching anything in its path harmlessly back."
					+ " A gust of air can extinguish fires on the ground or on a player, can cool lava, and "
					+ "can flip levers and activate buttons. Additionally, tapping sneak will change the "
					+ "origin of your next AirBlast to your targeted location.";
		}
	}, 
	AirBubble(BendingType.Air){
		public String getDescription() {
			return "To use, the bender must merely have the ability selected."
					+ " All water around the user in a small bubble will vanish,"
					+ " replacing itself once the user either gets too far away or selects a different ability.";
		}
	}, 
	AirShield(BendingType.Air){
		public String getDescription() {
			return "Air Shield is one of the most powerful defensive techniques in existence. "
					+ "To use, simply sneak (default: shift). "
					+ "This will create a whirlwind of air around the user, "
					+ "with a small pocket of safe space in the center. "
					+ "This wind will deflect all projectiles and will prevent any creature from "
					+ "entering it for as long as its maintained. ";
		}
	}, 
	AirSuction(BendingType.Air){
		public String getDescription() {
			return "To use, simply left-click in a direction. "
					+ "A gust of wind will originate as far as it can in that direction"
					+ " and flow towards you, sucking anything in its path harmlessly with it."
					+ " Skilled benders can use this technique to pull items from precarious locations. "
					+ "Additionally, tapping sneak will change the origin of your next "
					+ "AirSuction to your targeted location.";
		}
	}, 
	AirSwipe(BendingType.Air){
		public String getDescription() {
			return "To use, simply left-click in a direction. "
					+ "An arc of air will flow from you towards that direction, "
					+ "cutting and pushing back anything in its path. "
					+ "Its damage is minimal, but it still sends the message. "
					+ "This ability will extinguish fires, cool lava, and cut things like grass, "
					+ "mushrooms and flowers. Additionally, you can charge it by holding sneak. "
					+ "Charging before attacking will increase damage and knockback, up to a maximum.";
		}
	}, 
	AirScooter(BendingType.Air){
		public String getDescription() {
			return "AirScooter is a fast means of transportation. To use, sprint, jump then click with "
					+ "this ability selected. You will hop on a scooter of air and be propelled forward "
					+ "in the direction you're looking (you don't need to press anything). "
					+ "This ability can be used to levitate above liquids, but it cannot go up steep slopes. "
					+ "Any other actions will deactivate this ability.";
		}
	},
	AirSpout(BendingType.Air){
		public String getDescription() {
			return "This ability gives the airbender limited sustained levitation. It is a "
					+ "toggle - click to activate and form a whirling spout of air "
					+ "beneath you, lifting you up. You can bend other abilities while using AirSpout. "
					+ "Click again to deactivate this ability.";
		}
	}, 
	AirBurst(BendingType.Air){
		public String getDescription() {
			return "AirBurst is one of the most powerful abilities in the airbender's arsenal. "
					+ "To use, press and hold sneak to charge your burst. "
					+ "Once charged, you can either release sneak to launch a cone-shaped burst "
					+ "of air in front of you, or click to release the burst in a sphere around you. "
					+ "Additionally, having this ability selected when you land on the ground from a "
					+ "large enough fall will create a burst of air around you.";
		}
	}, 
	AirManipulation(BendingType.Air){
		public String getDescription() {
			return " Not ready yet";
		}
	}, 
	Tornado(BendingSpecializationType.Tornado){
		public String getDescription() {
			return "To use, simply sneak (default: shift). "
					+ "This will create a swirling vortex at the targeted location. "
					+ "Any creature or object caught in the vortex will be launched up "
					+ "and out in some random direction. If another player gets caught "
					+ "in the vortex, the launching effect is minimal. Tornado can "
					+ "also be used to transport the user. If the user gets caught in his/her "
					+ "own tornado, his movements are much more manageable. Provided the user doesn't "
					+ "fall out of the vortex, it will take him to a maximum height and move him in "
					+ "the general direction he's looking. Skilled airbenders can scale anything "
					+ "with this ability.";
		}
	}, 
	Suffocate(BendingSpecializationType.Suffocate){
		public String getDescription() {
			return "To use, keep sneaking while watching a player "
					+ "and the target while slowly lose his health."
					+ " The target will also be slower and weaker than before.";
		}
	},
	
	/*
	 * 
	 * EARTH Abilities
	 */

	Catapult(BendingType.Earth){
		public String getDescription() {
			return "To use, left-click while looking in the direction you want to be launched. "
					+ "A pillar of earth will jut up from under you and launch you in that direction - "
					+ "if and only if there is enough earth behind where you're looking to launch you. "
					+ "Skillful use of this ability takes much time and work, and it does result in the "
					+ "death of certain gung-ho earthbenders. If you plan to use this ability, be sure "
					+ "you've read about your passive ability you innately have as an earthbender.";
		}
	}, 
	RaiseEarth(BendingType.Earth){
		public String getDescription() {
			return "To use, simply left-click on an earthbendable block. "
					+ "A column of earth will shoot upwards from that location. "
					+ "Anything in the way of the column will be brought up with it, "
					+ "leaving talented benders the ability to trap brainless entities up there. "
					+ "Additionally, simply sneak (default shift) looking at an earthbendable block. "
					+ "A wall of earth will shoot upwards from that location. "
					+ "Anything in the way of the wall will be brought up with it. ";
		}
	}, 
	EarthGrab(BendingType.Earth){
		public String getDescription() {
			return "To use, simply left-click while targeting a creature within range. "
					+ "Four earthbendables blocks will come from the ground to trap the creature in.";
		}
	}, 
	EarthTunnel(BendingType.Earth){
		public String getDescription() {
			return "Earth Tunnel is a completely utility ability for earthbenders. "
			+ "To use, simply sneak (default: shift) in the direction you want to tunnel. "
			+ "You will slowly begin tunneling in the direction you're facing for as long as you "
			+ "sneak or if the tunnel has been dug long enough. This ability will be interupted "
			+ "if it hits a block that cannot be earthbent.";
			}
	}, 
	EarthBlast(BendingType.Earth){
		public String getDescription() {
			return "To use, place your cursor over an earthbendable object (dirt, rock, ores, etc) "
			+ "and tap sneak (default: shift). The object will temporarily turn to stone, "
			+ "indicating that you have it focused as the source for your ability. "
			+ "After you have selected an origin (you no longer need to be sneaking), "
			+ "simply left-click in any direction and you will see your object launch "
			+ "off in that direction, smashing into any creature in its path. If you look "
			+ "towards a creature when you use this ability, it will target that creature. "
			+ "A collision from Earth Blast both knocks the target back and deals some damage. "
			+ "You cannot have multiple of these abilities flying at the same time.";
			}
	}, 
	Collapse(BendingType.Earth){
		public String getDescription() {
			return " To use, simply left-click on an earthbendable block. "
					+ "That block and the earthbendable blocks above it will be shoved "
					+ "back into the earth below them, if they can. "
					+ "This ability does have the capacity to trap something inside of it, "
					+ "although it is incredibly difficult to do so. "
					+ "Additionally, press sneak with this ability to affect an area around your targetted location - "
					+ "all earth that can be moved downwards will be moved downwards. "
					+ "This ability is especially risky or deadly in caves, depending on the "
					+ "earthbender's goal and technique.";
		}
	}, 
	Tremorsense(BendingType.Earth){
		public String getDescription() {
			return "This is a pure utility ability for earthbenders. If you have this ability bound to any "
					+ "slot whatsoever, then you are able to 'see' using the earth. If you are in an area of low-light "
					+ "and are standing on top of an earthbendable block, this ability will automatically turn that block into "
					+ "glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out, "
					+ "as you have lost contact with the earth and cannot 'see' until you can touch earth again. "
					+ "Additionally, if you click with this ability selected, smoke will appear above nearby earth "
					+ "with pockets of air beneath them.";
		}
	},
	EarthArmor(BendingType.Earth){
		public String getDescription() {
			return "This ability encases the earthbender in temporary armor. To use, click on a block that is earthbendable. If there is another block under"
					+ " it that is earthbendable, the block will fly to you and grant you temporary armor and damage reduction. This ability has a long cooldown.";
		}
	}, 
	Shockwave(BendingType.Earth){
		public String getDescription() {
			return "This is one of the most powerful moves in the earthbender's arsenal. "
					+ "To use, you must first charge it by holding sneak (default: shift). "
					+ "Once charged, you can release sneak to create an enormous shockwave of earth, "
					+ "disturbing all earth around you and expanding radially outwards. "
					+ "Anything caught in the shockwave will be blasted back and dealt damage. "
					+ "If you instead click while charged, the disruption is focused in a cone in front of you. "
					+ "Lastly, if you fall from a great enough height with this ability selected, you will automatically create a shockwave.";
		}
	}, 
	LavaTrain(BendingSpecializationType.Lavabend){
		public String getDescription() {
			return "Simply sneaking + clic and a lava lake will form under your eyes.";
		}
	}, 
	MetalBending(BendingSpecializationType.Metalbend){
		public String getDescription() {
			return "The MetalBending skill is one of the most useful earthbender technique."
					+ " You can open iron door by clicking while you're on this slot. "
					+ "You can transform most of iron items into iron ingots. "
					+ "You can travel as you were spider-man by using a fishing rod.";
		}
	},
	
	/*
	 * 
	 * FIRE Abilities
	 */

	HeatControl(BendingType.Fire){
		public String getDescription() {
			return "While this ability is selected, the firebender becomes impervious "
					+ "to fire damage and cannot be ignited. "
					+ "If the user left-clicks with this ability, the targeted area will be "
					+ "extinguished, although it will leave any creature burning engulfed in flames. "
					+ "This ability can also cool lava. If this ability is used while targetting ice or snow, it"
					+ " will instead melt blocks in that area. Finally, sneaking with this ability will cook any food in your hand.";
		}
	}, 
	Blaze(BendingType.Fire){
		public String getDescription() {
			return "To use, simply left-click in any direction. "
					+ "An arc of fire will flow from your location, "
					+ "igniting anything in its path."
					+ " Additionally, tap sneak to engulf the area around you "
					+ "in roaring flames.";
		}
	}, 
	FireJet(BendingType.Fire){
		public String getDescription() {
			return "This ability is used for a limited burst of flight for firebenders. Clicking with this "
					+ "ability selected will launch you in the direction you're looking, granting you "
					+ "controlled flight for a short time. This ability can be used mid-air to prevent falling "
					+ "to your death, but on the ground it can only be used if standing on a block that's "
					+ "ignitable (e.g. not snow or water).";
		}
	}, 
	Illumination(BendingType.Fire){
		public String getDescription() {
			return "This ability gives firebenders a means of illuminating the area. It is a toggle - clicking "
					+ "will create a torch that follows you around. The torch will only appear on objects that are "
					+ "ignitable and can hold a torch (e.g. not leaves or ice). If you get too far away from the torch, "
					+ "it will disappear, but will reappear when you get on another ignitable block. Clicking again "
					+ "dismisses this torch.";
		}
	}, 
	WallOfFire(BendingType.Fire){
		public String getDescription() {
			return "To use this ability, click at a location. A wall of fire "
					+ "will appear at this location, igniting enemies caught in it "
					+ "and blocking projectiles.";
		}
	}, 
	FireBlast(BendingType.Fire){
		public String getDescription() {
			return "FireBlast is the most fundamental bending technique of a firebender. "
					+ "To use, simply left-click in a direction. A blast of fire will be created at your fingertips. "
					+ "If this blast contacts an enemy, it will dissipate and engulf them in flames, "
					+ "doing additional damage and knocking them back slightly. "
					+ "If the blast hits terrain, it will ignite the nearby area. "
					+ "Additionally, if you hold sneak, you will charge up the fireblast. "
					+ "If you release it when it's charged, it will instead launch a powerful "
					+ "fireball that explodes on contact.";
		}
	}, 
	FireBurst(BendingType.Fire){
		public String getDescription() {
			return "FireBurst is a very powerful firebending ability. "
					+ "To use, press and hold sneak to charge your burst. "
					+ "Once charged, you can either release sneak to launch a cone-shaped burst "
					+ "of flames in front of you, or click to release the burst in a sphere around you. ";
		}
	}, 
	FireShield(BendingType.Fire){
		public String getDescription() {
			return "FireShield is a basic defensive ability. "
					+ "Clicking with this ability selected will create a "
					+ "small disc of fire in front of you, which will block most "
					+ "attacks and bending. Alternatively, pressing and holding "
					+ "sneak creates a very small shield of fire, blocking most attacks. "
					+ "Creatures that contact this fire are ignited.";
		}
	}, 
	FireBlade(BendingType.Fire){
		public String getDescription() {
			return "To use, simply click and a powerful blade will appear in your hand.";
		}
	},
	Combustion(BendingSpecializationType.Combustion){
		public String getDescription() {
			return "Combustion is a powerful Firebender skill."
					+ " You just need to sneak and aim what you want."
					+ "Then, when the skill will be ready,  the air"
					+ " around the target will explode";
		}
	},
	Lightning(BendingSpecializationType.Lightning){
		public String getDescription() {
			return "Hold sneak while selecting this ability to charge up a lightning strike. Once "
					+ "charged, release sneak to discharge the lightning to the targetted location.";
		}
	},

	/* 
	 * 
	 * WATER Abilities
	 */
	
	WaterBubble(BendingType.Water){
		public String getDescription() {
			return "To use, the bender must merely have the ability selected."
					+ " All water around the user in a small bubble will vanish,"
					+ " replacing itself once the user either gets too far away or selects a different ability.";
		}
	}, 
	PhaseChange(BendingType.Water){
		public String getDescription() {
			return "To use, simply left-click. "
					+ "Any water you are looking at within range will instantly freeze over into solid ice. "
					+ "Provided you stay within range of the ice and do not unbind FreezeMelt, "
					+ "that ice will not thaw. If, however, you do either of those the ice will instantly thaw. "
					+ "If you sneak (default: shift), anything around where you are looking at will instantly melt. "
					+ "Since this is a more favorable state for these things, they will never re-freeze unless they "
					+ "would otherwise by nature or some other bending ability. Additionally, if you tap sneak while "
					+ "targetting water with FreezeMelt, it will evaporate water around that block that is above "
					+ "sea level. ";
		}
	}, 
	HealingWaters(BendingType.Water){
		public String getDescription() {
			return "To use, the bender must be at least partially submerged in water. "
					+ "If the user is not sneaking, this ability will automatically begin "
					+ "working provided the user has it selected. If the user is sneaking, "
					+ "he/she is channeling the healing to their target in front of them. "
					+ "In order for this channel to be successful, the user and the target must "
					+ "be at least partially submerged in water.";
		}
	}, 
	WaterManipulation(BendingType.Water){
		public String getDescription() {
			return "To use, place your cursor over a waterbendable object and tap sneak (default: shift). "
					+ "Smoke will appear where you've selected, indicating the origin of your ability. "
					+ "After you have selected an origin, simply left-click in any direction and you will "
					+ "see your water spout off in that direction, slicing any creature in its path. "
					+ "If you look towards a creature when you use this ability, it will target that creature. "
					+ "A collision from Water Manipulation both knocks the target back and deals some damage. "
					+ "Alternatively, if you have source selected and tap shift again, "
					+ "you will be able to control the water more directly.";
		}
	}, 
	Surge(BendingType.Water){
		public String getDescription() {
			return "This ability has two distinct features. If you sneak to select a source block, "
					+ "you can then click in a direction and a large wave will be launched in that direction. "
					+ "If you sneak again while the wave is en route, the wave will freeze the next target it hits. "
					+ "If, instead, you click to select a source block, you can hold sneak to form a wall of water at "
					+ "your cursor location. Click to shift between a water wall and an ice wall. "
					+ "Release sneak to dissipate it.";
		}
	},
	WaterSpout(BendingType.Water){
		public String getDescription() {
			return "To use this ability, click while over or in water. "
					+ "You will spout water up from beneath you to experience controlled levitation. "
					+ "This ability is a toggle, so you can activate it then use other abilities and it "
					+ "will remain on. If you try to spout over an area with no water, snow or ice, "
					+ "the spout will dissipate and you will fall. Click again with this ability selected to deactivate it.";
		}
	},
	IceSpike(BendingType.Water){
		public String getDescription() {
			return "This ability has many functions. Clicking while targetting ice, or an entity over some ice, "
					+ "will raise a spike of ice up, damaging and slowing the target. Tapping sneak (shift) while"
					+ " selecting a water source will select that source that can then be fired with a click. Firing"
					+ " this will launch a spike of ice at your target, dealing a bit of damage and slowing the target. "
					+ "If you sneak (shift) while not selecting a source, many ice spikes will erupt from around you, "
					+ "damaging and slowing those targets.";
		}
	}, 
	OctopusForm(BendingType.Water){
		public String getDescription() {
			return "This ability allows the waterbender to manipulate a large quantity of water into a form resembling that of an octopus. "
					+ "To use, click to select a water source. Then, hold sneak to channel this ability. "
					+ "While channeling, the water will form itself around you and has a chance to block incoming attacks. "
					+ "Additionally, you can click while channeling to attack things near you, dealing damage and knocking them back. "
					+ "Releasing shift at any time will dissipate the form.";
		}
	}, 
	Torrent(BendingType.Water){
		public String getDescription() {
			return "Torrent is one of the strongest moves in a waterbender's arsenal."
					+ " To use, first click a source block to select it; "
					+ "then hold shift to begin streaming the water around you."
					+ " Water flowing around you this way will damage and knock back nearby enemies and projectiles. "
					+ "If you release shift during this, you will create a large wave "
					+ "that expands outwards from you, launching anything in its path back. "
					+ "Instead, if you click you release the water and channel it "
					+ "to flow towards your cursor. "
					+ "Anything caught in the blast will be tossed about violently and take damage. "
					+ "Finally, if you click again when the water is torrenting, "
					+ "it will freeze the area around it when it is obstructed.";
		}
	}, 
	IceSwipe(BendingType.Water){
		public String getDescription() {
			return " Not ready yet";
		}
	},
	Bloodbending(BendingSpecializationType.Bloodbend){
		public String getDescription() {
			return "This ability was made illegal for a reason. With this ability selected, sneak while "
					+ "targetting something and you will bloodbend that target. Bloodbent targets cannot move, "
					+ "bend or attack. You are free to control their actions by looking elsewhere - they will "
					+ "be forced to move in that direction. Additionally, clicking while bloodbending will "
					+ "launch that target off in the direction you're looking. "
					+ "People who are capable of bloodbending are immune to your technique, and you are immune to theirs.";
		}
	}, 
	Drainbending(BendingSpecializationType.DrainBend){
		public String getDescription() {
			return " The drainbending skill allows the water bender using it to take water"
					+ " from plants and even from the air.";
		}
	}, 
	
	/*
	 * 
	 * CHI-BLOCKERS Abilities
	 */

	HighJump(BendingType.ChiBlocker){
		public String getDescription() {
			return "To use this ability, simply click. "
					+ "You will jump quite high. "
					+ "This ability has a short cooldown.";
		}
	}, 
	RapidPunch(BendingType.ChiBlocker){
		public String getDescription() {
			return "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch."
					+ " This has a short cooldown.";
		}
	}, 
	Paralyze(BendingType.ChiBlocker){
		public String getDescription() {
			return "Paralyzes the target, making them unable to do anything for a short "
					+ "period of time. This ability has a long cooldown.";
		}
	}, 
	SmokeBomb(BendingType.ChiBlocker){
		public String getDescription() {
			return " To use, simply click and a smokebomb will make every player around you blind."
					+ "While you're in the smokebomb area, you also are invisible.";
		}
	}, 
	PoisonnedDart(BendingType.ChiBlocker){
		public String getDescription() {
			return " Not ready yet.";
		}
	}, 
	Dash(BendingType.ChiBlocker){
		public String getDescription() {
			return "This ChiBlocker skill is very useful when you want to travel faster"
					+ " or if you want to avoid some dangerous technics ";
		}
	},
	PlasticBomb(BendingType.ChiBlocker) {
		@Override
		public String getDescription() {
			return " Not ready yet.";
		}
		
	},
	
	/*
	 * 
	 * AVATAR Abilities
	 */

	AvatarState(BendingType.Energy){
		public String getDescription() {
			return "The signature ability of the Avatar, this is a toggle. Click to activate to become "
					+ "nearly unstoppable. While in the Avatar State, the user takes severely reduced damage from "
					+ "all sources, regenerates health rapidly, and is granted extreme speed. Nearly all abilities "
					+ "are incredibly amplified in this state. Additionally, AirShield and FireJet become toggle-able "
					+ "abilities and last until you deactivate them or the Avatar State. Click again with the Avatar "
					+ "State selected to deactivate it.";
		}
	},
	AstralProjection(BendingType.Energy) {
		@Override
		public String getDescription() {
			return "";
		}
		
	};
	
	/*
	 * 
	 */
	
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
	
	public abstract String getDescription();
	
	//Static methods
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

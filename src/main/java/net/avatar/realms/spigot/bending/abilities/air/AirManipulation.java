package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;

@BendingAbility(name="Air Manipulation", element=BendingType.Air)
public class AirManipulation extends ActiveAbility{

	public AirManipulation(Player player) {
		super(player, null);
		// TODO Auto-generated constructor stub
	}
	
	public boolean swing() {
		switch (state) {
		case None:
		case CannotStart:
			return true;
			
		case CanStart:
		case Preparing:
		case Prepared:
		case Progressing:
		case Ending:
		case Ended: 
		case Removed:
			default: return false;
		}
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.AirManipulation;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	//TODO : Redirectable airblast that makes damages
}

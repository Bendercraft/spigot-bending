package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;

@BendingAbility(name="Air Manipulation", element=BendingElement.Air)
public class AirManipulation extends BendingActiveAbility{

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
	public BendingAbilities getAbilityType() {
		return BendingAbilities.AirManipulation;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	//TODO : Redirectable airblast that makes damages
}

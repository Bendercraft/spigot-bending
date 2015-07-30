package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;

@BendingAbility(name="Air Manipulation", element=BendingType.Air)
public class AirManipulation extends Ability{

	public AirManipulation(Player player) {
		super(player, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.AirManipulation;
	}

	@Override
	public Object getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}
	//TODO : Redirectable airblast that makes damages
}

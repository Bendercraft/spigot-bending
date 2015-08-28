package net.avatar.realms.spigot.bending.abilities.earth;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;

@BendingAbility(name="Tremor Sense", element=BendingType.Earth)
public class Tremorsense extends ActiveAbility {

	public Tremorsense (Player player) {
		super(player, null);
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.Tremorsense;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

}

package net.avatar.realms.spigot.bending.abilities.earth;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;

@BendingAbility(name = "Tremor Sense", bind = BendingAbilities.Tremorsense, element = BendingElement.Earth)
public class Tremorsense extends BendingActiveAbility {

	public Tremorsense(Player player) {
		super(player, null);
	}

	@Override
	public boolean sneak() {
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

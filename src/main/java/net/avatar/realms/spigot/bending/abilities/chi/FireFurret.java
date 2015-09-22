package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;

@BendingAbility(name = "Fire Furret", bind = BendingAbilities.FireFurret, element = BendingElement.ChiBlocker, affinity = BendingAffinity.ChiFire)
public class FireFurret extends BendingActiveAbility {

	public FireFurret(Player player) {
		super(player, null);
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}

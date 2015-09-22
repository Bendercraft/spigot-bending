package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;

@BendingAbility(name = "Earth Lariat", bind = BendingAbilities.EarthLariat, element = BendingElement.ChiBlocker, affinity = BendingAffinity.ChiEarth)
public class EarthLariat extends BendingActiveAbility {

	public EarthLariat(Player player) {
		super(player, null);
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}

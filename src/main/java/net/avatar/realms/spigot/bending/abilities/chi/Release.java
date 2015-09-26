package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.entity.Player;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;

@BendingAbility(name = "Release", bind = BendingAbilities.Release, element = BendingElement.ChiBlocker)
public class Release extends BendingActiveAbility {

	public Release(Player player) {
		super(player, null);
	}

	@Override
	public boolean sneak() {
		ComboPoints.consume(player);
		return false;
	}

	@Override
	public boolean progress() {
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

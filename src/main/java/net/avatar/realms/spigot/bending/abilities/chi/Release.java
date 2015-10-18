package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.entity.Player;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;

@ABendingAbility(name = "Release", bind = BendingAbilities.Release, element = BendingElement.ChiBlocker)
public class Release extends BendingActiveAbility {

	public Release(Player player) {
		super(player);
	}

	@Override
	public boolean sneak() {
		ComboPoints.consume(player);
		return false;
	}

	@Override
	public void progress() {
		
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}

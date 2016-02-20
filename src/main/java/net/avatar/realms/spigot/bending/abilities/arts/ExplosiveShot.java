package net.avatar.realms.spigot.bending.abilities.arts;

import org.bukkit.entity.Player;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;

@ABendingAbility(name = "ExplosiveShot", bind = BendingAbilities.AirSlice, element = BendingElement.Master, affinity = BendingAffinity.Bowman)
public class ExplosiveShot extends BendingActiveAbility {

	public ExplosiveShot(Player player) {
		super(player);
	}

	@Override
	public boolean swing() {
		
		return false;
	}

	@Override
	public boolean sneak() {
		
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

package net.avatar.realms.spigot.bending.abilities.arts;

import org.bukkit.entity.Player;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;

@ABendingAbility(name = BlankPoint.NAME, affinity = BendingAffinity.SWORD)
public class BlankPoint extends BendingActiveAbility {
	public final static String NAME = "BlankPoint";

	public BlankPoint(RegisteredAbility register, Player player) {
		super(register, player);
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

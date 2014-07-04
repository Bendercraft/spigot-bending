package net.avatarrealms.minecraft.bending.abilities.chi;

import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;

public class Dash implements IAbility{
	
	private Player player;
	
	public Dash (Player player) {
		this.player = player;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (!bPlayer.isOnCooldown(Abilities.Dash)) {
			
			bPlayer.cooldown(Abilities.Dash);
		}
		
	}

	@Override
	public int getBaseExperience() {
		return 2;
	}

	@Override
	public IAbility getParent() {
		return null;
	}

}

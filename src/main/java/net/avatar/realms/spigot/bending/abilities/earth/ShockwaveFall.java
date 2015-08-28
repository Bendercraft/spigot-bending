package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.entity.Player;

@BendingAbility(name="Shockwave", element=BendingType.Earth)
public class ShockwaveFall implements IAbility {
	
	@ConfigurationParameter("Fall-Threshold")
	private static double THRESHOLD = 9;

	private IAbility parent;

	public ShockwaveFall(Player player, IAbility parent) {
		this.parent = parent;
		
		if (!EntityTools.canBend(player, Abilities.Shockwave)
				|| EntityTools.getBendingAbility(player) != Abilities.Shockwave
				|| player.getFallDistance() < THRESHOLD
				|| !BlockTools.isEarthbendable(player,
						player.getLocation().add(0, -1, 0).getBlock())) {
			return;
		}
		
		new ShockwaveArea(player, this);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

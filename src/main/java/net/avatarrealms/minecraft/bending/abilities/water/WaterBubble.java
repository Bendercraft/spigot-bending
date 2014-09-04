package net.avatarrealms.minecraft.bending.abilities.water;

import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.air.AirBubble;

import org.bukkit.entity.Player;

public class WaterBubble implements IAbility {
	private IAbility parent;

	public WaterBubble(Player player, IAbility parent) {
		this.parent = parent;
		new AirBubble(player, this);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

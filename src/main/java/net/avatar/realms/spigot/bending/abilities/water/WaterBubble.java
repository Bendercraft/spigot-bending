package net.avatar.realms.spigot.bending.abilities.water;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;

import org.bukkit.entity.Player;

@BendingAbility(name="Water Bubble", element=BendingType.Water)
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

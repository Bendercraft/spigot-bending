package net.avatar.realms.spigot.bending.abilities.water;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

import org.bukkit.entity.Player;

@BendingAbility(name="Water Bubble", element=BendingType.Water)
public class WaterBubble implements IAbility {
	private IAbility parent;

	@ConfigurationParameter("Radius")
	public static double DEFAULT_RADIUS;
	
	public WaterBubble(Player player, IAbility parent) {
		this.parent = parent;
		new AirBubble(player, this);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

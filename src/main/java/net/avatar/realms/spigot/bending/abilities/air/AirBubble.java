package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.multi.Bubble;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

@ABendingAbility(name = "Air Bubble", bind = BendingAbilities.AirBubble, element = BendingElement.Air)
public class AirBubble extends Bubble {

	@ConfigurationParameter("Radius")
	private static double DEFAULT_RADIUS = 4;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 60 * 10 * 1000L;

	public AirBubble(Player player) {
		super(player);

		this.radius = DEFAULT_RADIUS;

		if (AvatarState.isAvatarState(player)) {
			this.radius = AvatarState.getValue(this.radius);
		}

		this.pushedMaterials.add(Material.WATER);
		this.pushedMaterials.add(Material.STATIONARY_WATER);
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

}

package net.bendercraft.spigot.bending.abilities.air;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.abilities.Bubble;

@ABendingAbility(name = AirBubble.NAME, element = BendingElement.AIR, shift = false, canBeUsedWithTools = true)
public class AirBubble extends Bubble {
	public final static String NAME = "AirBubble";

	@ConfigurationParameter("Radius")
	private static double DEFAULT_RADIUS = 4;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 60 * 10 * 1000L;

	public AirBubble(RegisteredAbility register, Player player) {
		super(register, player);

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

package net.avatar.realms.spigot.bending.abilities.air;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.multi.Bubble;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

@ABendingAbility(name = AirBubble.NAME, element = BendingElement.AIR, shift=false)
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

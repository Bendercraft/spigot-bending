package net.bendercraft.spigot.bending.abilities.water;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.abilities.Bubble;

@ABendingAbility(name = WaterBubble.NAME, element = BendingElement.WATER, shift=false, canBeUsedWithTools = true)
public class WaterBubble extends Bubble {
	public final static String NAME = "WaterBubble";

	@ConfigurationParameter("Radius")
	public static double DEFAULT_RADIUS = 4;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 600000;

	public WaterBubble(RegisteredAbility register, Player player) {
		super(register, player);

		this.radius = DEFAULT_RADIUS;
		if(bender.hasPerk(BendingPerk.WATER_WATERBUBBLE_RADIUS)) {
			this.radius += 1;
		}

		if (AvatarState.isAvatarState(player)) {
			this.radius = AvatarState.getValue(this.radius);
		}

		this.pushedMaterials.add(Material.WATER);

	}

	@Override
	public long getMaxMillis() {
		return MAX_DURATION;
	}
}

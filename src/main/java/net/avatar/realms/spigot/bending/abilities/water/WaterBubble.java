package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.multi.Bubble;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.Tools;

@ABendingAbility(name = WaterBubble.NAME, element = BendingElement.WATER, shift=false, canBeUsedWithTools = true)
public class WaterBubble extends Bubble {
	public final static String NAME = "WaterBubble";

	@ConfigurationParameter("Radius")
	public static double DEFAULT_RADIUS = 4;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 60 * 10 * 1000;

	public WaterBubble(RegisteredAbility register, Player player) {
		super(register, player);

		if (Tools.isNight(this.player.getWorld())) {
			this.radius = PluginTools.waterbendingNightAugment(WaterBubble.DEFAULT_RADIUS, this.player.getWorld());
		} else {
			this.radius = DEFAULT_RADIUS;
		}

		if (AvatarState.isAvatarState(player)) {
			this.radius = AvatarState.getValue(this.radius);
		}

		this.pushedMaterials.add(Material.WATER);
		this.pushedMaterials.add(Material.STATIONARY_WATER);

	}

	@Override
	public long getMaxMillis() {
		return MAX_DURATION;
	}
}

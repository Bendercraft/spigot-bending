package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.multi.Bubble;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Water Bubble", element=BendingType.Water)
public class WaterBubble extends Bubble {

	@ConfigurationParameter("Radius")
	public static double DEFAULT_RADIUS = 4;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 60 * 10 * 1000;

	public WaterBubble (Player player) {
		super(player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (Tools.isNight(this.player.getWorld())) {
			this.radius = PluginTools.waterbendingNightAugment(WaterBubble.DEFAULT_RADIUS, this.player.getWorld());
		}
		else {
			this.radius = DEFAULT_RADIUS;
		}

		if (AvatarState.isAvatarState(player)) {
			this.radius = AvatarState.getValue(this.radius);
		}

		this.pushedMaterials.add(Material.WATER);
		this.pushedMaterials.add(Material.STATIONARY_WATER);

	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.WaterBubble;
	}

	@Override
	protected long getMaxMillis () {
		return MAX_DURATION;
	}
}

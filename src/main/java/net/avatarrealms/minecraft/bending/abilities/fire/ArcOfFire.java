package net.avatarrealms.minecraft.bending.abilities.fire;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ArcOfFire implements IAbility {

	private static int defaultarc = ConfigManager.arcOfFireArc;
	private static int defaultrange = ConfigManager.arcOfFireRange;
	private static int stepsize = 2;
	
	private IAbility parent;

	public ArcOfFire(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Blaze))
			return;

		Location location = player.getLocation();

		int arc = (int) PluginTools.firebendingDayAugment(defaultarc,
				player.getWorld());

		for (int i = -arc; i <= arc; i += stepsize) {
			double angle = Math.toRadians((double) i);
			Vector direction = player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			int range = defaultrange;
			if (AvatarState.isAvatarState(player))
				range = AvatarState.getValue(range);

			new FireStream(location, direction, player, range, this);
			bPlayer.cooldown(Abilities.Blaze);
			bPlayer.earnXP(BendingType.Fire, this);
		}
	}

	public static String getDescription() {
		return "To use, simply left-click in any direction. "
				+ "An arc of fire will flow from your location, "
				+ "igniting anything in its path."
				+ " Additionally, tap sneak to engulf the area around you "
				+ "in roaring flames.";
	}

	@Override
	public int getBaseExperience() {
		return 3;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

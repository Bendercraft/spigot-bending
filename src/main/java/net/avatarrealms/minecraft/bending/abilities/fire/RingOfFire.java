package net.avatarrealms.minecraft.bending.abilities.fire;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RingOfFire implements IAbility {

	static final int defaultrange = ConfigManager.ringOfFireRange;
	private IAbility parent;

	public RingOfFire(Player player, IAbility parent) {
		this.parent = parent;

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Blaze))
			return;

		Location location = player.getLocation();

		for (double degrees = 0; degrees < 360; degrees += 10) {
			double angle = Math.toRadians(degrees);
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
		}

		bPlayer.cooldown(Abilities.Blaze);
	}

	public static String getDescription() {
		return "To use, simply left-click. "
				+ "A circle of fire will emanate from you, "
				+ "engulfing everything around you. Use with extreme caution.";
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

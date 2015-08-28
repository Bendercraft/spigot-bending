package net.avatar.realms.spigot.bending.abilities.fire;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Blaze Ring", element=BendingType.Fire)
public class RingOfFire implements IAbility {

	@ConfigurationParameter("Range")
	private static int RANGE = 7;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 2000;
	
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

			int range = RANGE;
			if (AvatarState.isAvatarState(player))
				range = AvatarState.getValue(range);

			new FireStream(location, direction, player, range, this);
		}

		bPlayer.cooldown(Abilities.Blaze, COOLDOWN);
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

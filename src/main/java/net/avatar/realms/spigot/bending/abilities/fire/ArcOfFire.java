package net.avatar.realms.spigot.bending.abilities.fire;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Blaze Arc", element=BendingType.Fire)
public class ArcOfFire implements IAbility {

	@ConfigurationParameter("Arc")
	private static int defaultarc = 20;
	
	@ConfigurationParameter("Range")
	private static int defaultrange = 20;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1000;
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
		}
		bPlayer.cooldown(Abilities.Blaze, COOLDOWN);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

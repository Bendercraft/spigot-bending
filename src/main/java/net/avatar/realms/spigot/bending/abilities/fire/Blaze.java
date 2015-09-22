package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.PluginTools;

@BendingAbility(name = "Blaze", bind = BendingAbilities.Blaze, element = BendingElement.Fire)
public class Blaze extends BendingActiveAbility {

	@ConfigurationParameter("Arc")
	private static int DEFAULT_ARC = 20;

	@ConfigurationParameter("Range")
	private static int DEFAULT_RANGE = 20;

	@ConfigurationParameter("Arc-Cooldown")
	public static long ARC_COOLDOWN = 1000;

	@ConfigurationParameter("Range")
	private static int RANGE = 7;

	@ConfigurationParameter("Ring-Cooldown")
	public static long RING_COOLDOWN = 2000;

	private static int stepsize = 2;

	private List<FireStream> firestreams = new LinkedList<FireStream>();

	public Blaze(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		switch (this.state) {
		case None:
		case CannotStart:
		case Progressing:
			return false;
		case Ended:
		case Removed:
			return true;
		default:
			Location location = this.player.getLocation();

			int arc = (int) PluginTools.firebendingDayAugment(DEFAULT_ARC, this.player.getWorld());

			for (int i = -arc; i <= arc; i += stepsize) {
				double angle = Math.toRadians(i);
				Vector direction = this.player.getEyeLocation().getDirection().clone();

				double x, z, vx, vz;
				x = direction.getX();
				z = direction.getZ();

				vx = (x * Math.cos(angle)) - (z * Math.sin(angle));
				vz = (x * Math.sin(angle)) + (z * Math.cos(angle));

				direction.setX(vx);
				direction.setZ(vz);

				int range = DEFAULT_RANGE;
				if (AvatarState.isAvatarState(this.player)) {
					range = AvatarState.getValue(range);
				}

				firestreams.add(new FireStream(location, direction, this.player, range));
			}
			this.bender.cooldown(BendingAbilities.Blaze, ARC_COOLDOWN);
			AbilityManager.getManager().addInstance(this);
			setState(BendingAbilityState.Progressing);
			return false;
		}
	}

	@Override
	public boolean sneak() {
		switch (this.state) {
		case None:
		case CannotStart:
		case Progressing:
			return false;
		case Ended:
		case Removed:
			return true;
		default:
			Location location = this.player.getLocation();

			for (double degrees = 0; degrees < 360; degrees += 10) {
				double angle = Math.toRadians(degrees);
				Vector direction = this.player.getEyeLocation().getDirection().clone();

				double x, z, vx, vz;
				x = direction.getX();
				z = direction.getZ();

				vx = (x * Math.cos(angle)) - (z * Math.sin(angle));
				vz = (x * Math.sin(angle)) + (z * Math.cos(angle));

				direction.setX(vx);
				direction.setZ(vz);

				int range = RANGE;
				if (AvatarState.isAvatarState(this.player)) {
					range = AvatarState.getValue(range);
				}

				firestreams.add(new FireStream(location, direction, this.player, range));
			}

			this.bender.cooldown(BendingAbilities.Blaze, RING_COOLDOWN);
			AbilityManager.getManager().addInstance(this);
			setState(BendingAbilityState.Progressing);
			return false;
		}
	}

	@Override
	public boolean progress() {
		if(!super.progress()) {
			return false;
		}
		
		if(state == BendingAbilityState.Progressing) {
			List<FireStream> test = new LinkedList<FireStream>(firestreams);
			for(FireStream stream : test) {
				if(!stream.progress()) {
					firestreams.remove(stream);
				}
			}
			return !firestreams.isEmpty();
		}
		
		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

package net.avatar.realms.spigot.bending.abilities.fire;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.PluginTools;

@BendingAbility (name = "Blaze", element = BendingElement.Fire)
public class Blaze extends BendingActiveAbility {

	@ConfigurationParameter ("Arc")
	private static int DEFAULT_ARC = 20;

	@ConfigurationParameter ("Range")
	private static int DEFAULT_RANGE = 20;

	@ConfigurationParameter ("Arc-Cooldown")
	public static long ARC_COOLDOWN = 1000;

	@ConfigurationParameter ("Range")
	private static int RANGE = 7;

	@ConfigurationParameter ("Ring-Cooldown")
	public static long RING_COOLDOWN = 2000;

	private static int stepsize = 2;
	
	public Blaze (Player player) {
		super(player, null);
	}

	@Override
	public boolean swing () {
		switch (this.state) {
			case None:
			case CannotStart:
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
					
					new FireStream(location, direction, this.player, range, this);
				}
				this.bender.cooldown(BendingAbilities.Blaze, ARC_COOLDOWN);
				return false;
		}
	}

	@Override
	public boolean sneak () {
		switch (this.state) {
			case None:
			case CannotStart:
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
					
					new FireStream(location, direction, this.player, range, this);
				}
				
				this.bender.cooldown(BendingAbilities.Blaze, RING_COOLDOWN);
				return false;
		}
	}
	
	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public BendingAbilities getAbilityType () {
		return BendingAbilities.Blaze;
	}
	
}

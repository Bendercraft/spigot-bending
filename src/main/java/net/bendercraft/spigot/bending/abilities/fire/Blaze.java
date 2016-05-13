package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.PluginTools;

@ABendingAbility(name = Blaze.NAME, element = BendingElement.FIRE)
public class Blaze extends BendingActiveAbility {
	public final static String NAME = "Blaze";
	
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

	public Blaze(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if(getState() != BendingAbilityState.START) {
			return false;
		}
		
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
		this.bender.cooldown(NAME, ARC_COOLDOWN);
		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() != BendingAbilityState.START) {
			return false;
		}
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

		this.bender.cooldown(NAME, RING_COOLDOWN);
		
		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PROGRESSING) {
			List<FireStream> test = new LinkedList<FireStream>(firestreams);
			for(FireStream stream : test) {
				if(!stream.progress()) {
					firestreams.remove(stream);
				}
			}
			if(firestreams.isEmpty()) {
				remove();
			}
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	public List<FireStream> getFirestreams() {
		return firestreams;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	

}

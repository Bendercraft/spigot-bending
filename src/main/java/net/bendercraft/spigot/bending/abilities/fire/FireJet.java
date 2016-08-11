package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.controller.FlyingPlayer;
import net.bendercraft.spigot.bending.utils.BlockTools;

@ABendingAbility(name = FireJet.NAME, element = BendingElement.FIRE, shift=false)
public class FireJet extends BendingActiveAbility {
	public final static String NAME = "FireJet";

	@ConfigurationParameter("Speed")
	private static double FACTOR = 1.0;

	@ConfigurationParameter("Duration")
	private static long DURATION = 1600;

	@ConfigurationParameter("Power")
	public static int POWER = 1;
	
	@ConfigurationParameter("Power-Activation")
	public static int POWER_ACTIVATION = 5;
	
	@ConfigurationParameter("Tick")
	public static long TICK = 320;

	private long duration = DURATION;
	private double factor = FACTOR;
	private long time;

	public FireJet(RegisteredAbility register, Player player) {
		super(register, player);
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, POWER_ACTIVATION)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			Block block = player.getLocation().getBlock();
			if (FireStream.isIgnitable(player, block) || (block.getType() == Material.AIR) || AvatarState.isAvatarState(player)) {
				FlyingPlayer.addFlyingPlayer(player, this, getMaxMillis(), false);
				player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(factor));
				bender.fire.consume(NAME, POWER_ACTIVATION);
				bender.fire.halt();
				time = System.currentTimeMillis();
				setState(BendingAbilityState.PROGRESSING);
			}
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			remove();
		}
		return false;
	}

	public static boolean checkTemporaryImmunity(Player player) {
		if (getPlayers().contains(player)) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if ((BlockTools.isWater(player.getLocation().getBlock()) 
				|| (System.currentTimeMillis() > (startedTime + duration)))) {
			return false;
		}
		
		if(!bender.fire.can(NAME, POWER)) {
			return false;
		}
		
		return true;
	}

	@Override
	public void progress() {
		player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
		double timefactor;
		if (AvatarState.isAvatarState(player)) {
			timefactor = 1;
		} else {
			timefactor = 1 - ((System.currentTimeMillis() - startedTime) / (2.0 * duration));
		}
		Vector velocity = player.getEyeLocation().getDirection().clone().normalize().multiply(factor * timefactor);
		player.setVelocity(velocity);
		player.setFallDistance(0);
		long now = System.currentTimeMillis();
		if(time + TICK < now) {
			time = now;
			bender.fire.consume(NAME, POWER, false);
		}
	}

	public static List<Player> getPlayers() {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(FireJet.NAME);
		LinkedList<Player> players = new LinkedList<Player>();
		if (instances == null) {
			return players;
		}

		for (Object o : instances.keySet()) {
			players.add((Player) o);
		}

		return players;
	}

	@Override
	public void stop() {
		FlyingPlayer.removeFlyingPlayer(this.player, this);
		bender.fire.resume();
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

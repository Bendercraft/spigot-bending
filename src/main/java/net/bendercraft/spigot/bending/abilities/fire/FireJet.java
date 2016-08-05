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
	private static double FACTOR = 0.7;

	@ConfigurationParameter("Duration")
	private static long DURATION = 1550;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 4000;

	private long duration = DURATION;
	private double factor = FACTOR;

	public FireJet(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			Block block = this.player.getLocation().getBlock();
			if (FireStream.isIgnitable(this.player, block) || (block.getType() == Material.AIR) || AvatarState.isAvatarState(this.player)) {
				FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis(), false);
				this.player.setVelocity(this.player.getEyeLocation().getDirection().clone().normalize().multiply(this.factor));
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
		if ((BlockTools.isWater(this.player.getLocation().getBlock()) 
				|| (System.currentTimeMillis() > (this.startedTime + this.duration)))) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		this.player.getWorld().playEffect(this.player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
		double timefactor;
		if (AvatarState.isAvatarState(this.player)) {
			timefactor = 1;
		} else {
			timefactor = 1 - ((System.currentTimeMillis() - this.startedTime) / (2.0 * this.duration));
		}
		Vector velocity = this.player.getEyeLocation().getDirection().clone().normalize().multiply(this.factor * timefactor);
		this.player.setVelocity(velocity);
		this.player.setFallDistance(0);
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
		this.bender.cooldown(FireJet.NAME, COOLDOWN);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

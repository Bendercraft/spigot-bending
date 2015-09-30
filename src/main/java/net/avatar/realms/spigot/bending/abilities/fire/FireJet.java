package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name = "Fire Jet", bind = BendingAbilities.FireJet, element = BendingElement.Fire)
public class FireJet extends BendingActiveAbility {

	@ConfigurationParameter("Speed")
	private static double FACTOR = 0.7;

	@ConfigurationParameter("Duration")
	private static long DURATION = 1550;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 6000;

	private long duration = DURATION;
	private double factor = FACTOR;

	public FireJet(Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}
		this.factor = PluginTools.firebendingDayAugment(FACTOR, player.getWorld());
	}

	@Override
	public boolean swing() {
		switch (this.state) {
		case None:
		case CannotStart:
			return false;
		case CanStart:
			Block block = this.player.getLocation().getBlock();
			if (FireStream.isIgnitable(this.player, block) || (block.getType() == Material.AIR) || AvatarState.isAvatarState(this.player)) {
				FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
				this.player.setVelocity(this.player.getEyeLocation().getDirection().clone().normalize().multiply(this.factor));
				setState(BendingAbilityState.Progressing);
				AbilityManager.getManager().addInstance(this);
			}
			return false;
		case Preparing:
		case Prepared:
		case Progressing:
			setState(BendingAbilityState.Ended);
			return false;
		case Ending:
		case Ended:
		case Removed:
		default:
			return false;
		}
	}

	public static boolean checkTemporaryImmunity(Player player) {
		if (getPlayers().contains(player)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		long now = System.currentTimeMillis();
		if ((BlockTools.isWater(this.player.getLocation().getBlock()) || (now > (this.startedTime + this.duration))) && !AvatarState.isAvatarState(this.player)) {
			return false;
		} else {
			this.player.getWorld().playEffect(this.player.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
			double timefactor;
			if (AvatarState.isAvatarState(this.player)) {
				timefactor = 1;
			} else {
				timefactor = 1 - ((now - this.startedTime) / (2.0 * this.duration));
			}
			Vector velocity = this.player.getEyeLocation().getDirection().clone().normalize().multiply(this.factor * timefactor);
			this.player.setVelocity(velocity);
			this.player.setFallDistance(0);
		}
		return true;
	}

	public static List<Player> getPlayers() {
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireJet);
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
	}

	@Override
	public void remove() {
		this.bender.cooldown(BendingAbilities.FireJet, COOLDOWN);
		super.remove();
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

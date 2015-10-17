package net.avatar.realms.spigot.bending.abilities;

import java.util.Map;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * Represent the base class for bending abilities
 */
public abstract class BendingAbility {

	private BendingAbility parent;

	protected BendingPlayer bender;
	protected Player player;

	protected long startedTime;

	private BendingAbilityState state = BendingAbilityState.Start;

	/**
	 * Construct the bases of a new ability instance
	 * 
	 * @param player
	 *            The player that launches this ability
	 * @param parent
	 *            The ability that generates this ability. null if none
	 */
	public BendingAbility(Player player, BendingAbility parent) {
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);
		this.parent = parent;
		this.startedTime = System.currentTimeMillis();
	}

	/**
	 * Set the state in which the ability is currently in
	 * 
	 * @param newState
	 * 
	 *            <pre>
	 * The new state
	 * </pre>
	 */
	protected final void setState(BendingAbilityState newState) {
		if(newState == BendingAbilityState.Ended) {
			Bending.getInstance().getLogger().warning(this+" tried to change its state to ended directly");
			return;
		}
		this.state = newState;
	}

	/**
	 * This method is used by the Ability Constructor to determine if the
	 * ability can be launched or not The ability state will then be changed to
	 * CanStart or CannotStart according to this result
	 * 
	 * @return <code>true</code> if the ability can be launched
	 *         <code>false</code> if the ability cannot be launched
	 */
	public boolean canBeInitialized() {
		if (this.player == null) {
			return false;
		}

		if (this.bender == null) {
			return false;
		}
		
		if (this.bender.isOnCooldown(AbilityManager.getManager().getAbilityType(this))) {
			return false;
		}

		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}

		if (!EntityTools.canBend(this.player, AbilityManager.getManager().getAbilityType(this))) {
			// Also check region protection at player location
			return false;
		}
		
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(AbilityManager.getManager().getAbilityType(this));
		if(instances != null && instances.containsKey(this.getIdentifier())) {
			return false;
		}

		return true;
	}
	
	public boolean canTick() {
		if (!this.player.isOnline()
				|| this.player.isDead()
				|| this.state.equals(BendingAbilityState.Ended)
				|| (getMaxMillis() > 0 && System.currentTimeMillis() > (this.startedTime + getMaxMillis()))
				|| !EntityTools.canBend(this.player, AbilityManager.getManager().getAbilityType(this))) {
			return false;
		}
		return true;
	}

	public final void tick() {
		if (canTick()) {
			progress();
		} else {
			remove();
		}
	}
	
	public final void remove() {
		stop();
		state = BendingAbilityState.Ended;
	}

	/**
	 * @return
	 * 
	 *         <pre>
	 * Max time in millisecond the ability can keep running.
	 * 0 if unlimited
	 * </pre>
	 */
	protected long getMaxMillis() {
		return 60 * 1000;
	}

	/**
	 * @return The object that identifies this ability instance among the others
	 */
	public abstract Object getIdentifier();
	
	public abstract void progress();

	public abstract void stop();
	

	public final Player getPlayer() {
		return this.player;
	}

	public final BendingPlayer getBender() {
		return this.bender;
	}

	public final BendingAbility getParent() {
		return this.parent;
	}
	
	public final BendingAbilityState getState() {
		return state;
	}
}

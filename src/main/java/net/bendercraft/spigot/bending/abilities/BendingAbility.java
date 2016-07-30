package net.bendercraft.spigot.bending.abilities;

import java.util.Map;

import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.utils.EntityTools;

/**
 * Represent the base class for bending abilities
 */
public abstract class BendingAbility {
	protected final BendingPlayer bender;
	protected final Player player;

	protected long startedTime;
	
	// For performance
	protected final RegisteredAbility register;

	private BendingAbilityState state = BendingAbilityState.START;

	/**
	 * Construct the bases of a new ability instance
	 * 
	 * @param player
	 *            The player that launches this ability
	 */
	public BendingAbility(RegisteredAbility register, Player player) {
		this.register = register;
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);
		this.startedTime = System.currentTimeMillis();
	}

	public long getStartedTime() {
		return startedTime;
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
		if(newState == BendingAbilityState.ENDED) {
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
		if (player == null) {
			return false;
		}

		if (bender == null) {
			return false;
		}
		
		if (bender.isOnCooldown(getName())) {
			return false;
		}

		if (!player.isOnline() || player.isDead()) {
			return false;
		}

		if (!EntityTools.canBend(player, register)) {
			// Also check region protection at player location
			return false;
		}
		
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(getName());
		if(instances != null && instances.containsKey(this.getIdentifier())) {
			return false;
		}

		return true;
	}
	
	public boolean canTick() {
		if (!player.isOnline()
				|| player.isDead()
				|| isState(BendingAbilityState.ENDED)
				|| (getMaxMillis() > 0 && System.currentTimeMillis() > (startedTime + getMaxMillis()))
				|| !EntityTools.canBend(player, register)) {
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
		state = BendingAbilityState.ENDED;
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
		return 60000;
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
	
	public final BendingAbilityState getState() {
		return state;
	}
	
	public boolean isState(BendingAbilityState test) {
		return state != null && state.equals(test);
	}
	
	public String getName() {
		return register.getName();
	}
	
	public BendingElement getElement() {
		return register.getElement();
	}
}

package net.avatar.realms.spigot.bending.abilities.base;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * Represent the base class for bending abilities
 */
public abstract class Ability implements IAbility {
	
	private IAbility parent;
	
	protected BendingPlayer bender;
	protected Player player;
	
	protected long startedTime;
	
	protected AbilityState state = AbilityState.None;
	
	/**
	 * Construct the bases of a new ability instance
	 * 
	 * @param player
	 *        The player that launches this ability
	 * @param parent
	 *        The ability that generates this ability. null if none
	 */
	public Ability (Player player, IAbility parent) {
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);
		this.parent = parent;
	}
	
	@Override
	public final Player getPlayer () {
		return this.player;
	}
	
	@Override
	public final BendingPlayer getBender () {
		return this.bender;
	}
	
	@Override
	public final IAbility getParent () {
		return this.parent;
	}
	
	/**
	 * Set the state in which the ability is currently in
	 * 
	 * @param newState
	 * 		
	 *        <pre>
	 * The new state
	 *        </pre>
	 */
	protected final void setState (AbilityState newState) {
		Bending.plugin.getLogger().info(newState.name());
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
	public boolean canBeInitialized () {
		if (this.player == null) {
			return false;
		}
		
		if (this.bender == null) {
			return false;
		}
		
		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}
		
		if (!EntityTools.canBend(this.player, this.getAbilityType())) {
			// Also check region protection at player location
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean progress () {
		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}
		
		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}
		
		if (this.state.equals(AbilityState.Ended) || this.state.equals(AbilityState.Removed)) {
			return false;
		}
		
		long now = System.currentTimeMillis();
		if ((getMaxMillis() > 0) && (now > (this.startedTime + getMaxMillis()))) {
			return false;
		}
		
		if (!EntityTools.canBend(this.player, getAbilityType())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public final void consume () {
		this.setState(AbilityState.Ended);
	}
	
	@Override
	public void stop () {
	
	}
	
	/**
	 * @return
	 * 		
	 * 		<pre>
	 * Max time in millisecond the ability can keep running.
	 * 0 if unlimited
	 *         </pre>
	 */
	protected long getMaxMillis () {
		return 60 * 1000;
	}
	
	@Override
	public void remove () {
		AbilityManager.getManager().getInstances(this.getAbilityType()).remove(this.getIdentifier());
		setState(AbilityState.Removed);
	}
	
	@Override
	public boolean equals (Object object) {
		
		if (this == object) {
			return true;
		}
		
		if (object == null) {
			return false;
		}
		
		if (!(getClass() == object.getClass())) {
			return false;
		}
		
		Ability ab = (Ability) object;
		
		if (this.getIdentifier().equals(ab.getIdentifier())) {
			return true;
		}
		
		return false;
	}
}

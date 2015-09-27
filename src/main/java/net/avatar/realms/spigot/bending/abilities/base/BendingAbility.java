package net.avatar.realms.spigot.bending.abilities.base;

import java.util.Map;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * Represent the base class for bending abilities
 */
public abstract class BendingAbility implements IBendingAbility {

	private IBendingAbility parent;

	protected BendingPlayer bender;
	protected Player player;

	protected long startedTime;

	protected BendingAbilityState state = BendingAbilityState.None;

	/**
	 * Construct the bases of a new ability instance
	 * 
	 * @param player
	 *            The player that launches this ability
	 * @param parent
	 *            The ability that generates this ability. null if none
	 */
	public BendingAbility(Player player, IBendingAbility parent) {
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);
		this.parent = parent;
	}

	@Override
	public final Player getPlayer() {
		return this.player;
	}

	@Override
	public final BendingPlayer getBender() {
		return this.bender;
	}

	@Override
	public final IBendingAbility getParent() {
		return this.parent;
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
		//Bending.log.info("Player "+player.getName()+" - "+AbilityManager.getManager().getAbilityType(this)+" - "+newState);
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

		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}

		if (!EntityTools.canBend(this.player, AbilityManager.getManager().getAbilityType(this))) {
			// Also check region protection at player location
			return false;
		}
		
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(AbilityManager.getManager().getAbilityType(this));
		if(instances != null && instances.containsKey(this.getIdentifier())) {
			//Bending.log.info("Player "+player.getName()+" - "+AbilityManager.getManager().getAbilityType(this)+" - tried to start twice same ability");
			return false;
		}

		return true;
	}

	@Override
	public boolean progress() {
		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return false;
		}

		if (this.state.equals(BendingAbilityState.Ended) || this.state.equals(BendingAbilityState.Removed)) {
			return false;
		}

		long now = System.currentTimeMillis();
		if ((getMaxMillis() > 0) && (now > (this.startedTime + getMaxMillis()))) {
			return false;
		}

		if (!EntityTools.canBend(this.player, AbilityManager.getManager().getAbilityType(this))) {
			return false;
		}

		return true;
	}

	@Override
	public final void consume() {
		this.setState(BendingAbilityState.Ended);
	}

	@Override
	public void stop() {

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

	@Override
	public void remove() {
		AbilityManager.getManager().getInstances(AbilityManager.getManager().getAbilityType(this)).remove(this.getIdentifier());
		setState(BendingAbilityState.Removed);
	}

	@Override
	public boolean equals(Object object) {

		if (this == object) {
			return true;
		}

		if (object == null) {
			return false;
		}

		if (!(getClass() == object.getClass())) {
			return false;
		}

		BendingAbility ab = (BendingAbility) object;

		if (this.getIdentifier().equals(ab.getIdentifier())) {
			return true;
		}

		return false;
	}
}

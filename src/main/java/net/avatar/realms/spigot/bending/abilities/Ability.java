package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

/**
 * 
 * Represent the base class for bending abilities
 *
 */
public abstract class Ability {

	private Ability parent;

	protected BendingPlayer bender;
	protected Player player;

	protected long startedTime;

	protected AbilityState state = AbilityState.None;
	/**
	 * Construct the bases of a new ability instance
	 * @param player The player that launches this ability
	 * @param parent The ability that generates this ability. null if none
	 */
	public Ability(Player player, Ability parent) {
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);

		if (canBeInitialized()) {
			this.startedTime = System.currentTimeMillis();	
			this.parent = parent;
			setState(AbilityState.CanStart);
		}
		else {
			setState(AbilityState.CannotStart);
		}
	}

	/**
	 * What should the ability do when the player click
	 * @return <code>true</code> if we should create a new version of the ability
	 *  <code>false</code> otherwise
	 */
	public boolean swing() {
		return false;
	}

	/**
	 * What should the ability do when the player jump. Not used for the moment as no way to detect player jump.
	 * @return <code>true</code> if we should create a new version of the ability
	 *  <code>false</code> otherwise
	 */
	public boolean jump() {
		return false;
	}

	/**
	 * What should the ability do when the player sneaks.
	 * @return <code>true</code> if we should create a new version of the ability
	 *  <code>false</code> otherwise
	 */
	public boolean sneak() {
		return false;
	}

	/**
	 * What should the ability do when the player falls.
	 * @return <code>true</code> if we should create a new version of the ability
	 *  <code>false</code> otherwise
	 */
	public boolean fall() {
		return false;
	}

	/**
	 * The logic that the ability must follow over the time.
	 * @return <code>false</code> if the ability must be stopped
	 * <code>true</code> if the ability can continue 
	 */
	public boolean progress() {
		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}

		if (!EntityTools.canBend(this.player, getAbilityType())) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBending(this.player, this.getAbilityType(), this.player.getLocation())) {
			return false;
		}

		long now = System.currentTimeMillis();
		if ((getMaxMillis() > 0) && (now > (this.startedTime + getMaxMillis()))) {
			return false;
		}

		if (this.state.isBefore(AbilityState.CanStart)) {
			return false;
		}

		if (this.state.equals(AbilityState.Ended)) {
			return false;
		}

		return true;
	}

	/**
	 * What should the ability do when it's over.
	 */
	public void stop() {

	}

	/**
	 * Remove the ability from all instances
	 */
	public void remove() {
		AbilityManager.getManager().getInstances(this.getAbilityType()).remove(this.getIdentifier());
		setState(AbilityState.Removed);
	}

	/**
	 * @return <pre>Max time in millisecond the ability can keep running.
	 * 0 if unlimited </pre>
	 */
	protected long getMaxMillis() {
		return 25000;
	}

	/**
	 * <pre>Sometimes, an ability is the logical sequence of another ability.
	 * For example, FireBurst generates multiples FireBlast,
	 * AirBurst can generate an AirFallBurst that generates multiple AirBlast </pre>
	 * @return The ability that generated this ability
	 */
	public final Ability getParent() {
		return this.parent;
	}

	/**
	 * 
	 * @return The player that launch this ability
	 */
	public final Player getPlayer() {
		return this.player;
	}

	/**
	 * 
	 * @return The BendingPlayer(bender) that launch this ability
	 */
	public final BendingPlayer getBender() {
		return this.bender;
	}

	/**
	 * Set state to Ended so that it will be deleted at next tick
	 */
	public final void consume () {
		this.setState(AbilityState.Ended);
	}

	/**
	 * This method is used by the Ability Constructor to determine if the
	 * ability can be launched or not The ability state will then be changed to
	 * CanStart or CannotStart according to this result
	 * 
	 * @return <code>true</code> if the ability can be launched
	 *         <code>false</code> if the ability cannot be launched
	 * 
	 */
	public boolean canBeInitialized() {
		if (this.player == null) {
			return false;
		}

		if (this.bender == null) {
			return false;
		}

		if (this.bender.isOnCooldown(this.getAbilityType())) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBending(this.player, this.getAbilityType(), this.player.getLocation())) {
			return false;
		}

		return true;
	}

	protected final void setState(AbilityState newState) {
		Bending.plugin.getLogger().info(newState.name());
		this.state = newState;
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

	public abstract Abilities getAbilityType();

	public abstract Object getIdentifier();

}

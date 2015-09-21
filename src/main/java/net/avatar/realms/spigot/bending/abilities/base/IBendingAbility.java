package net.avatar.realms.spigot.bending.abilities.base;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingPlayer;

public interface IBendingAbility {

	/**
	 * The logic that the ability must follow over the time.
	 * 
	 * @return <code>false</code> if the ability must be stopped
	 *         <code>true</code> if the ability can continue
	 */
	public boolean progress();

	/**
	 * What should the ability do when it's over.
	 */
	public void stop();

	/**
	 * Remove the ability from all instances
	 */
	public void remove();

	/**
	 * <pre>
	 * Sometimes, an ability is the logical sequence of another ability.
	 * For example, FireBurst generates multiples FireBlast,
	 * AirBurst can generate an AirFallBurst that generates multiple AirBlast
	 * </pre>
	 * 
	 * @return The ability that generated this ability
	 */
	public IBendingAbility getParent();

	/**
	 * @return The player that launch this ability
	 */
	public Player getPlayer();

	/**
	 * @return The BendingPlayer(bender) that launch this ability
	 */
	public BendingPlayer getBender();

	/**
	 * Set state to Ended so that it will be deleted at next tick
	 */
	public void consume();

	/**
	 * @return The object that identifies this ability instance among the others
	 */
	public Object getIdentifier();
}

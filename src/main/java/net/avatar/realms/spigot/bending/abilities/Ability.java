package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

public abstract class Ability {
	
	private Ability parent;
	
	protected BendingPlayer bender;
	protected Player player;
	
	protected long startedTime;
	/**
	 * Construct the bases of a new ability instance
	 * @param player The player that launches this ability
	 * @param parent The ability that generates this ability. null if none
	 */
	public Ability(Player player, Ability parent) {
		startedTime = System.currentTimeMillis();	
		this.parent = parent;
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);	
	}
	
	/**
	 * What should the ability do when the player click
	 * @return <code>true</code> if we should create a new version of the ability
	 *  <code>false</code> otherwise
	 */
	public boolean click() {
		return false;
	}
	
	/**
	 * What should the ability do when the player jump. Not used for the moment.
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
	 * The logic that the ability must follow over the time.
	 * @return false if 
	 */
	public abstract boolean progress();
	
	/**
	 * What should the ability do when it's over.
	 */
	public void remove() {
		
	}
	
	protected int getMaxMillis() {
		return 10000;
	}
	
	/**
	 * <pre>Sometimes, an ability is the logical sequence of another ability.
	 * For example, FireBurst generates multiples FireBlast,
	 * AirBurst can generate an AirFallBurst that generates multiple AirBlast </pre>
	 * @return The ability that generated this ability
	 */
	public Ability getParent() {
		return parent;
	}

}

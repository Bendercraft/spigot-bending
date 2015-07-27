package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.entity.Player;

public abstract class Ability {
	
	private Ability parent;
	
	protected BendingPlayer bender;
	protected Player player;
	
	protected long startedTime;
	
	public Ability(Player player, Ability parent) {
		startedTime = System.currentTimeMillis();	
		this.parent = parent;
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);	
	}
	
	/**
	 * What should the ability do when the player click
	 */
	public void click() {
		
	}
	
	/**
	 * What should the ability do when the player jump. Not used for the moment.
	 */
	public void jump() {
		
	}
	
	/**
	 * What should the ability do when the player sneaks.
	 */
	public void sneak() {
		
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

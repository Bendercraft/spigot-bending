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
	
	protected static final String configPrefix = "Properties.";
	
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
		startedTime = System.currentTimeMillis();	
		this.parent = parent;
		this.player = player;
		this.bender = BendingPlayer.getBendingPlayer(player);
		
		if (canBeInitialized()) {
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
		if (!player.isOnline() || player.isDead()) {
			return false;
		}
		
		if (!EntityTools.canBend(this.player, getAbilityType())) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(player, this.getAbilityType(), player.getLocation())) {
			return false;
		}
		
		long now = System.currentTimeMillis();
		if (getMaxMillis() > 0 && now > startedTime + getMaxMillis()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * What should the ability do when it's over.
	 */
	public void stop() {
		
	}
	
	public void remove() {
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
		return parent;
	}
	
	public final Player getPlayer() {
		return player;
	}
	
	public final BendingPlayer getBender() {
		return bender;
	}
	
	public boolean canBeInitialized() {
		if (bender.isOnCooldown(this.getAbilityType())) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(player, this.getAbilityType(), player.getLocation())) {
			return false;
		}
		
		return true;
	}
	
	protected final void setState(AbilityState newState) {
		Bending.plugin.getLogger().info(newState.name());
		this.state = newState;
	}
	
	public abstract Abilities getAbilityType();
	
	public abstract Object getIdentifier();
	
}

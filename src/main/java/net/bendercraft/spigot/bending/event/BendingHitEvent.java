package net.bendercraft.spigot.bending.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.bendercraft.spigot.bending.abilities.BendingAbility;

/**
 * Unlike BendingDamageEvent, this one is fired when something affect an entity other than damage.
 * Change in velocity, potions effects and everything is do not damage directly an entity.
 * @author Koudja
 *
 */
public class BendingHitEvent extends Event implements Cancellable {
	protected static final HandlerList handlers = new HandlerList();
	
	private final BendingAbility ability;
	private final Entity target;
	private boolean cancelled;

	public BendingHitEvent(BendingAbility ability, Entity target) {
		this.ability = ability;
		this.target = target;
	}

	public BendingAbility getAbility() {
		return ability;
	}

	public Entity getTarget() {
		return target;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

package net.avatar.realms.spigot.bending.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BendingEvent extends Event{
	protected static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers () {
		return handlers;
	}
	
	public static HandlerList getHandlerList () {
		return handlers;
	}
}

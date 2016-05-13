package net.bendercraft.spigot.bending.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BendingRegisterEvent extends Event {
	protected static final HandlerList handlers = new HandlerList();

	public BendingRegisterEvent() {
		
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

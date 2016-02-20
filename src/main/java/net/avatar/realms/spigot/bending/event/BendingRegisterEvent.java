package net.avatar.realms.spigot.bending.event;

import org.bukkit.event.HandlerList;

public class BendingRegisterEvent extends BendingEvent {

	public BendingRegisterEvent() {
		super();
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}

package main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

import tools.BendingType;
import tools.ConfigManager;
import tools.Tools;

public class TagAPIListener implements Listener {
	@EventHandler
	public void onNameTag(PlayerReceiveNameTagEvent event) {
		if (!event.isModified())

			if (event.getNamedPlayer().hasPermission("bending.avatar")) {
				event.setTag(Tools.getColor(ConfigManager.getColor("Avatar"))
						+ event.getNamedPlayer().getName());
			}

			else if (Tools.isBender(event.getNamedPlayer().getName(),
					BendingType.Air)) {
				event.setTag(Tools.getColor(ConfigManager.getColor("Air"))
						+ event.getNamedPlayer().getName());
			} else if (Tools.isBender(event.getNamedPlayer().getName(),
					BendingType.Earth)) {
				event.setTag(Tools.getColor(ConfigManager.getColor("Earth"))
						+ event.getNamedPlayer().getName());
			} else if (Tools.isBender(event.getNamedPlayer().getName(),
					BendingType.Fire)) {
				event.setTag(Tools.getColor(ConfigManager.getColor("Fire"))
						+ event.getNamedPlayer().getName());
			} else if (Tools.isBender(event.getNamedPlayer().getName(),
					BendingType.ChiBlocker)) {
				event.setTag(Tools.getColor(ConfigManager
						.getColor("ChiBlocker"))
						+ event.getNamedPlayer().getName());
			} else if (Tools.isBender(event.getNamedPlayer().getName(),
					BendingType.Water)) {
				event.setTag(Tools.getColor(ConfigManager.getColor("Water"))
						+ event.getNamedPlayer().getName());
			}
		// Tools.verbose("'" + event.getTag() + "'");
	}
}

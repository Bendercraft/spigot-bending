package net.bendercraft.spigot.bending.listeners;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthGrab;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * This class should only be instantiate if Factions is detected on the server.
 * Created by Nokorbis on 27/05/2016.
 */
public class BendingFactionsBlockListener implements Listener{

    //If factions prevented the destruction of a block, we might need to allow it. (#Earthgrab)
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            for (BendingAbility eGrab : AbilityManager.getManager().getInstances(EarthGrab.NAME).values()) {
                EarthGrab grab = (EarthGrab) eGrab;
                if (grab.isEarthGrabBlock(event.getBlock())) {
                    event.setCancelled(false);
                    return;
                }
            }
        }
    }
}

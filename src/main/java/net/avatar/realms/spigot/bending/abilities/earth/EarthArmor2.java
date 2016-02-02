package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import org.bukkit.entity.Player;

/**
 * Class that will replace the current eartharmor.
 * Duplicated until the refactor is done
 */
@ABendingAbility(name = "Earth Armor", bind = BendingAbilities.EarthArmor, element = BendingElement.Earth)
public class EarthArmor2 extends BendingActiveAbility{

    @ConfigurationParameter("Duration")
    private static long DURATION = 60000;

    @ConfigurationParameter("Strength")
    private static int STRENGTH = 2;

    @ConfigurationParameter("Cooldown")
    private static long COOLDOWN = 60000;

    @ConfigurationParameter("Range")
    private static int RANGE = 7;

    public EarthArmor2(Player player) {
        super(player);
    }

    @Override
    public Object getIdentifier() {
        return this.player;
    }

    @Override
    public void progress() {

    }

    @Override
    public void stop() {

    }
}

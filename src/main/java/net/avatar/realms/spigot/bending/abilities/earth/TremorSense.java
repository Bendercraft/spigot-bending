package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.*;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.integrations.ProtocolLib.CustomPacket;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Nokorbis on 02/03/2016.
 */
@ABendingAbility(name = TremorSense.NAME, element = BendingElement.EARTH, shift = true)
public class TremorSense extends BendingActiveAbility {

    public static final String NAME = "TremorSense";

    private static final PotionEffect BLIND = new PotionEffect(PotionEffectType.BLINDNESS, 20, 0);
    private static final PotionEffect GLOW = new PotionEffect(PotionEffectType.GLOWING, 20, 2);

    @ConfigurationParameter("Base-Distance")
    private static int BASE_DISTANCE = 5;

    @ConfigurationParameter("Max-Distance")
    private static int MAX_DISTANCE = 50;

    @ConfigurationParameter("Distance-Increment")
    private static int DISTANCE_INC = 5;

    private int currentDistance;

    private long lastIncrementTime;

    public TremorSense(RegisteredAbility register, Player player) {
        super(register, player);
    }

    @Override
    public boolean sneak() {
        if (getState().equals(BendingAbilityState.ENDED)) {
            return false;
        }

        if (!isOnEarth(player)) {
            return false;
        }

        if (bender.isOnCooldown(NAME)) {
            return false;
        }

        currentDistance = BASE_DISTANCE;
        lastIncrementTime = startedTime;

        setState(BendingAbilityState.PROGRESSING);
        return true;
    }

    @Override
    public boolean canTick() {
        return super.canTick();
    }

    @Override
    protected long getMaxMillis() {
        return 1000 * 60 * 5; // 5 minutes
    }

    @Override
    public boolean canBeUsedWithTools() {
        return true;
    }

    @Override
    public Object getIdentifier() {
        return player;
    }

    @Override
    public void progress() {
        if (!getState().equals(BendingAbilityState.PROGRESSING)) {
            return;
        }
        if (!player.isSneaking()) {
            remove();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastIncrementTime >= 2000) {
            if (currentDistance < MAX_DISTANCE) {
                currentDistance += DISTANCE_INC;
                if (currentDistance > MAX_DISTANCE) {
                    currentDistance = MAX_DISTANCE;
                }
            }
            lastIncrementTime = now;
            CustomPacket.sendAddPotionEffect(player, BLIND, player);
        }

        for (LivingEntity livingEntity : EntityTools.getLivingEntitiesAroundPoint(player.getLocation(), currentDistance)) {
            if (isOnEarth(livingEntity)) {
                livingEntity.addPotionEffect(GLOW);
                CustomPacket.sendAddPotionEffect(player, GLOW, livingEntity);
            }
        }
    }

    @Override
    public void stop() {
        //TODO : Cooldown
    }

    public boolean isOnEarth(LivingEntity entity) {
        Block block = entity.getLocation().clone().add(0, -1, 0).getBlock();
        return BlockTools.isEarthbendable(player, block);
    }
}

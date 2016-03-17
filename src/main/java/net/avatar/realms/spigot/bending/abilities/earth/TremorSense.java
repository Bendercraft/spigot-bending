package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.*;
import net.avatar.realms.spigot.bending.abilities.arts.Mark;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Nokorbis on 02/03/2016.
 */
@ABendingAbility(name = TremorSense.NAME, element = BendingElement.EARTH, shift = true, canBeUsedWithTools = true)
public class TremorSense extends BendingActiveAbility {

    public static final String NAME = "TremorSense";

    private static final PotionEffect BLIND = new PotionEffect(PotionEffectType.BLINDNESS, 20, 0);
    private static final PotionEffect GLOW = new PotionEffect(PotionEffectType.GLOWING, 5*20, 2);
    private static final int[][] RELATIVES = {{1,0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
    @ConfigurationParameter("Base-Distance")
    private static int BASE_DISTANCE = 5;

    @ConfigurationParameter("Max-Distance")
    private static int MAX_DISTANCE = 50;

    @ConfigurationParameter("Distance-Increment")
    private static int DISTANCE_INC = 5;

    private int currentDistance;

    private long lastIncrementTime;
    private long lastBlindTime;

    private Map<Integer, LivingEntity> entities;

    public TremorSense(RegisteredAbility register, Player player) {
        super(register, player);
        entities = new HashMap<Integer, LivingEntity>();
    }

	@Override
    public boolean sneak() {
        currentDistance = BASE_DISTANCE;
        lastBlindTime = lastIncrementTime = startedTime;

        setState(BendingAbilityState.PROGRESSING);
        return true;
    }

    @Override
    public boolean canTick() {
        if(!super.canTick()) {
        	return false;
        }

        if (!isOnEarth(player)) {
            return false;
        }

        if (bender.isOnCooldown(NAME)) {
            return false;
        }

        if (!getState().equals(BendingAbilityState.PROGRESSING)) {
            return false;
        }
        return true;
    }

    @Override
    protected long getMaxMillis() {
        return 1000 * 60 * 5; // 5 minutes
    }

    @Override
    public Object getIdentifier() {
        return player;
    }

    @Override
    public void progress() {
        if (!player.isSneaking()) {
            remove();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastIncrementTime >= 500) {
            if (currentDistance < MAX_DISTANCE) {
                currentDistance += DISTANCE_INC;
                if (currentDistance > MAX_DISTANCE) {
                    currentDistance = MAX_DISTANCE;
                }
            }
            lastIncrementTime = now;
        }

        if (now - lastBlindTime >= 2000) {
            player.addPotionEffect(BLIND);
            lastBlindTime = now;
        }

        Collection<LivingEntity> oldEntities = entities.values();
        entities.clear();
        for (LivingEntity livingEntity : EntityTools.getLivingEntitiesAroundPoint(player.getLocation(), currentDistance)) {
            if (player.getEntityId() != livingEntity.getEntityId() && isOnEarth(livingEntity)) {
                entities.put(livingEntity.getEntityId(), livingEntity);
                livingEntity.addPotionEffect(GLOW);
                player.sendMessage("Glow effect added");
            }
            oldEntities.remove(livingEntity);
        }
        for (LivingEntity oldEntity : oldEntities) {
            if (!Mark.isMarked(oldEntity)) {
                player.sendMessage("Glow effect removed");
                oldEntity.removePotionEffect(PotionEffectType.GLOWING);
            }
        }
    }

    @Override
    public void stop() {
    	for(LivingEntity entity : entities.values()) {
    		entity.removePotionEffect(PotionEffectType.GLOWING);
    	}
    	entities.clear();
        bender.cooldown(NAME, (System.currentTimeMillis() - startedTime) / 10);
    }

    public boolean isOnEarth(LivingEntity entity) {
        if (!entity.isOnGround()) {
            return false;
        }
        Block block = entity.getLocation().clone().add(0, -1, 0).getBlock();
        if (BlockTools.isEarthbendable(player, block)) {
            return true;
        }
        for (int[] rel : RELATIVES) {
            Block relative = block.getRelative(rel[0], 0, rel[1]);
            if (BlockTools.isEarthbendable(player, relative)) {
                return true;
            }
        }
        return false;
    }

	public Map<Integer, LivingEntity> getEntities() {
		return entities;
	}

    public static boolean isEntityTremorsensedByPlayer(LivingEntity target, Player player) {
        return isEntityTremorsensedByPlayer(target.getEntityId(), player);
    }

    public static boolean isEntityTremorsensedByPlayer(int entityID, Player player) {
        Map<Object, BendingAbility> tremorsenses = AbilityManager.getManager().getInstances(NAME);
        if (tremorsenses == null || tremorsenses.isEmpty()) {
            return false;
        }
        if (!tremorsenses.containsKey(player)) {
            player.sendMessage("No tremorsense for you");
            return false;
        }
        TremorSense sense = (TremorSense) tremorsenses.get(player);
        return sense.entities.containsKey(entityID);
    }
    
}

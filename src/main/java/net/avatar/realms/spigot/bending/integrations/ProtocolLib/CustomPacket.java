package net.avatar.realms.spigot.bending.integrations.ProtocolLib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.avatar.realms.spigot.bending.Bending;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EFFECT;
import static com.comphenix.protocol.PacketType.Play.Server.REMOVE_ENTITY_EFFECT;

/**
 * Created by Nokorbis on 02/03/2016.
 */
public abstract  class CustomPacket {

    public static void sendAddPotionEffect(Player p, PotionEffect effect, LivingEntity target) {
        PacketContainer packet = new PacketContainer(ENTITY_EFFECT);
        @SuppressWarnings("deprecation")
        int effectID = effect.getType().getId();
        int amplifier = effect.getAmplifier();
        int duration = effect.getDuration();
        int entityID = target.getEntityId();
        packet.getIntegers().write(0, entityID);
        packet.getBytes().write(0, (byte) effectID);
        packet.getBytes().write(1, (byte) amplifier);
        packet.getIntegers().write(1, duration);
        // use this to hide particles in 1.8
        packet.getBytes().write(2, (byte) 0);
        //
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            Bending.getInstance().getLogger().log(Level.WARNING, "Was not able to send a potion adding packet", e);
        }
    }

    public static void sendRemovePotionEffect(Player p, PotionEffectType type) {
        PacketContainer packet = new PacketContainer(REMOVE_ENTITY_EFFECT);
        int entityID = p.getEntityId();
        @SuppressWarnings("deprecation")
        int effectID = type.getId();
        packet.getIntegers().write(0, entityID);
        packet.getIntegers().write(1, effectID);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            Bending.getInstance().getLogger().log(Level.WARNING, "Was not able to send a potion adding packet", e);
        }
    }
}

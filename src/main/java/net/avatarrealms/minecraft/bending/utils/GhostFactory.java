package net.avatarrealms.minecraft.bending.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
 
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/* @author : Lenis0012 */
 
public class GhostFactory {
    private static final String MC_VERSION;
 
    static {
        String version = "";
        if(!checkVersion(version)) {
            StringBuilder builder = new StringBuilder();
            for(int a = 0; a < 10; a++) {
                for(int b = 0; b < 10; b++) {
                    for(int c = 0; c < 10; c++) {
                        builder.setLength(0);
                        builder.append('v').append(a).append('_').append(b).append("_R").append(c).append('.');
                        version = builder.toString();
                        if(checkVersion(version))
                            a = b = c = 10;
                    }
                }
            }
        }
 
        MC_VERSION = version;
    }
 
    private static final String NMS_ROOT = "net.minecraft.server." + MC_VERSION;
 
    private static boolean checkVersion(String version) {
        try {
            Class.forName("net.minecraft.server." + version + "World");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
 
    private List<String> players = new ArrayList<String>();
    private boolean created = false;
 
    public GhostFactory(Plugin plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new GhostListener(this), plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for(String user : players) {
                    Player player = Bukkit.getPlayer(user);
                    if(!player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
                }
            }
        }, 5L, 5L);
    }
 
    public void create() {
        if(created)
            return;
 
        this.update(0);
        this.created = true;
    }
 
    public void remove() {
        if(!created)
            return;
 
        this.update(1);
        this.created = false;
    }
 
    public boolean isCreated() {
        return this.created;
    }
 
    public void addGhost(Player player) {
        if(players.add(player.getName())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
            this.update(3);
        }
    }
 
    public void removeGhost(Player player) {
        if(players.remove(player.getName())) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            this.update(4);
        }
    }
 
    public String[] getGhosts() {
        return players.toArray(new String[0]);
    }
 
    public void clearGhosts() {
        players.clear();
    }
 
    private void update(int action) {
        Object packet = this.createPacket("Packet209SetScoreboardTeam");
        this.setValue(packet, "a", "ghosts");
        this.setValue(packet, "f", action);
        this.setValue(packet, "b", "Ghosts");
        this.setValue(packet, "c", "");
        this.setValue(packet, "d", "");
        this.setValue(packet, "g", 2);
        this.setValue(packet, "e", players);
 
        for(Player player : Bukkit.getOnlinePlayers()) {
            this.sendPacket(player, packet);
        }
    }
 
    private void sendTo(Player player) {
        Object packet = this.createPacket("Packet209SetScoreboardTeam");
        this.setValue(packet, "a", "ghosts");
        this.setValue(packet, "f", 0);
        this.setValue(packet, "b", "Ghosts");
        this.setValue(packet, "c", "");
        this.setValue(packet, "d", "");
        this.setValue(packet, "g", 2);
        this.setValue(packet, "e", players);
        this.sendPacket(player, packet);
    }
 
    private void setValue(Object instance, String fieldName, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
 
    private Object getValue(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    private Object invoke(Object instance, String methodName, Class<?>[] fields, Object[] values) {
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, fields);
            method.setAccessible(true);
            return method.invoke(instance, values);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    private Object createPacket(String name) {
        try {
            Class<?> clazz = Class.forName(NMS_ROOT + name);
            return clazz.newInstance();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    private void sendPacket(Player player, Object packet) {
        try {
            Class<?> Packet = Class.forName(NMS_ROOT + "Packet");
            Object playerHandle = this.invoke(player, "getHandle", new Class<?>[0], new Class<?>[0]);
            Object playerConnection = this.getValue(playerHandle, "playerConnection");
            this.invoke(playerConnection, "sendPacket", new Class<?>[] {Packet}, new Object[] {packet});
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
 
    private static final class GhostListener implements Listener {
        private GhostFactory factory;
 
        public GhostListener(GhostFactory factory) {
            this.factory = factory;
        }
 
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            factory.removeGhost(player);
        }
 
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            if(factory.isCreated())
                factory.sendTo(player);
        }
    }
}
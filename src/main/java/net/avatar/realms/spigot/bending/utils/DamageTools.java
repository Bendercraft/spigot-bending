package net.avatar.realms.spigot.bending.utils;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.event.BendingDamageEvent;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.EntityLiving;

public class DamageTools {
	private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);
	private static Field FIELD_ENTITYPLAYER_lastDamageByPlayerTime = null;
	
	private static Field getField() throws NoSuchFieldException, SecurityException {
		if(FIELD_ENTITYPLAYER_lastDamageByPlayerTime == null) {
			FIELD_ENTITYPLAYER_lastDamageByPlayerTime = EntityLiving.class.getDeclaredField("lastDamageByPlayerTime");
			FIELD_ENTITYPLAYER_lastDamageByPlayerTime.setAccessible(true);
		}
		return FIELD_ENTITYPLAYER_lastDamageByPlayerTime;
	}
	
	public static void damageEntity(BendingPlayer attacker, Entity damagee, double damage) {
		if (ProtectionManager.isEntityProtected(damagee)) {
			return;
		}
		if (damagee instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) damagee;
			if (AvatarState.isAvatarState(attacker.getPlayer())) {
				damage = AvatarState.getValue(damage);
			}

			//((LivingEntity) entity).damage(damage, player); // This is garbish - spigot do not let us decide damage in the end
			Map<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
	        Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<DamageModifier, Function<? super Double, Double>>(DamageModifier.class);
	        
	        modifiers.put(DamageModifier.BASE, damage);
	        modifierFunctions.put(DamageModifier.BASE, ZERO);
			
			BendingDamageEvent event = new BendingDamageEvent(attacker, damagee, modifiers, modifierFunctions);
			Bending.callEvent(event);
			
			
			CraftLivingEntity t = (CraftLivingEntity) damagee;
			if(!event.isCancelled() && !living.isDead() && t.getHandle().noDamageTicks == 0) {
				living.setLastDamageCause(event);
				
				// Make sure we do not set negative health or too much
				double health = living.getHealth() - event.getDamage();
				if(health < 0) {
					health = 0;
				}
				if(health > living.getMaxHealth()) {
					health = living.getMaxHealth();
				}
				living.setHealth(health);
				
				
				
				// See EntityLiving#damageEntity from NMS to get effect ID and standard hurt ticks
				t.getHandle().lastDamage = (float) event.getDamage();
				t.getHandle().lastDamager = ((CraftPlayer)(attacker.getPlayer())).getHandle();
				try {
					getField().setInt(t.getHandle(), 100);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Could not set lastDamageByPlayerTime on "+t.getName(), e);
				}
				t.getHandle().killer = ((CraftPlayer)(attacker.getPlayer())).getHandle();
				t.getHandle().hurtTicks = t.getHandle().ay = 10;
				t.getHandle().noDamageTicks = t.getHandle().maxNoDamageTicks;
				t.getHandle().world.broadcastEntityEffect(t.getHandle(), (byte)33);
				
				if(living.getHealth() <= 0) {
					t.getHandle().die(DamageSource.GENERIC);
				}
			}
		}
	}

}

package net.bendercraft.spigot.bending.utils;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.event.BendingDamageEvent;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.MathHelper;

@SuppressWarnings("deprecation")
public class DamageTools {
	private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);
	private static Field FIELD_ENTITYPLAYER_lastDamageByPlayerTime = null;
	
	public static final int DEFAULT_NODAMAGETICKS = 5;
	public static final float DEFAULT_KNOCKBACK = 0.4F;
	
	private static Field getField() throws NoSuchFieldException, SecurityException {
		if(FIELD_ENTITYPLAYER_lastDamageByPlayerTime == null) {
			FIELD_ENTITYPLAYER_lastDamageByPlayerTime = EntityLiving.class.getDeclaredField("lastDamageByPlayerTime");
			FIELD_ENTITYPLAYER_lastDamageByPlayerTime.setAccessible(true);
		}
		return FIELD_ENTITYPLAYER_lastDamageByPlayerTime;
	}
	
	public static void damageEntity(BendingPlayer attacker, Entity damagee, BendingAbility ability, double damage) {
		damageEntity(attacker, damagee, ability, damage, false, DEFAULT_NODAMAGETICKS, DEFAULT_KNOCKBACK, false);
	}
	
	/**
	 * 
	 * @param attacker : thug life
	 * @param damagee : who's the victim here ?!
	 * @param damage : amount of damage to apply
	 * @param ghost : if true, this damage won't change "noDamageTicks" on damagee, usefull to get "passive damage" done
	 * @param noDamageTicks : how many ticks damagee gets invulnerability after being touch by this damage (have no effect if 'ghost' is set to true)
	 * @param knockback : knockback push to apply
	 * @param bypassImmunity : set to true if this damage should bypass "noDamageTicks" 
	 */
	public static void damageEntity(BendingPlayer attacker, Entity damagee, BendingAbility ability, double damage, boolean ghost, int noDamageTicks, float knockback, boolean bypassImmunity) {
		if (ProtectionManager.isEntityProtected(damagee) 
				|| ProtectionManager.isLocationProtectedFromBending(attacker.getPlayer(), ability.getRegister(), damagee.getLocation())) {
			return;
		}
		if (damagee instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) damagee;
			if(living.isDead()) {
				return;
			}
			if (AvatarState.isAvatarState(attacker.getPlayer())) {
				damage = AvatarState.getValue(damage);
			}

			//((LivingEntity) entity).damage(damage, player); // This is garbish - spigot do not let us decide damage in the end
			Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
	        Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<>(DamageModifier.class);
	        
	        modifiers.put(DamageModifier.BASE, damage);
	        modifierFunctions.put(DamageModifier.BASE, ZERO);
	        for(DamageModifier modifier : DamageModifier.values()) {
	        	if(modifier != DamageModifier.BASE) {
		        	modifiers.put(modifier, 0.0);
		        	modifierFunctions.put(modifier, ZERO);
	        	}
	        }
			
			BendingDamageEvent event = new BendingDamageEvent(attacker, damagee, ability, modifiers, modifierFunctions);
			Bending.callEvent(event);
			
			if(event.isCancelled()) {
				return;
			}
			
			double finalDamage = event.getFinalDamage();
			
			CraftLivingEntity t = (CraftLivingEntity) damagee;
			if(!event.isCancelled() && !living.isDead() && (bypassImmunity || t.getHandle().noDamageTicks == 0)) {
				living.setLastDamageCause(event);
				
				float previousHealth = t.getHandle().getHealth();
				t.getHandle().setHealth((float) (previousHealth - finalDamage));
				t.getHandle().getCombatTracker().trackDamage(DamageSource.GENERIC, previousHealth, (float) finalDamage);
				
				// See EntityLiving#damageEntity(DamageSource damagesource, float f) from NMS to get effect ID and standard hurt ticks
				t.getHandle().lastDamage = (float) finalDamage;
				t.getHandle().lastDamager = ((CraftPlayer)(attacker.getPlayer())).getHandle();
				try {
					getField().setInt(t.getHandle(), 100);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					Bending.getInstance().getLogger().log(Level.SEVERE, "Could not set lastDamageByPlayerTime on "+t.getName(), e);
				}
				t.getHandle().killer = ((CraftPlayer)(attacker.getPlayer())).getHandle();
				t.getHandle().hurtDuration = 10;
				t.getHandle().hurtTicks = t.getHandle().hurtDuration;
				if(!ghost) {
					t.getHandle().noDamageTicks = noDamageTicks;
				}
				t.getHandle().world.broadcastEntityEffect(t.getHandle(), (byte)33);
				// Knockback
				if (t.getHandle().lastDamager != null) {
                    double d0 = t.getHandle().lastDamager.getPositionVector().getX() - t.getHandle().getPositionVector().getX();

                    double d1;
                    for (d1 = t.getHandle().lastDamager.getPositionVector().getZ() - t.getHandle().getPositionVector().getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                        d0 = (Math.random() - Math.random()) * 0.01D;
                    }

                    t.getHandle().az = (float) (MathHelper.d(d1, d0) * 57.2957763671875D - (double) t.getHandle().yaw);
                    t.getHandle().a(t.getHandle().lastDamager, knockback, d0, d1);
				}
				
				// Should entity die ?
				if(living.getHealth() <= 0) {
					t.getHandle().die(DamageSource.GENERIC);
				}
			}
		}
	}

}

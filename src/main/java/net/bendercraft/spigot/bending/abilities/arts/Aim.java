package net.bendercraft.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;


@ABendingAbility(name = Aim.NAME, affinity = BendingAffinity.BOW)
public class Aim extends BendingActiveAbility {
	public final static String NAME = "Aim";

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 6;
	
	@ConfigurationParameter("Focus-Damage")
	private static int FOCUS_DAMAGE = 6;

	@ConfigurationParameter("Range")
	private static double RANGE = 40;
	
	@ConfigurationParameter("Range-Effective")
	private static int EFFECTIVE_RANGE = 30;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 3000;
	
	@ConfigurationParameter("Charge")
	private static long CHARGE = 2000;

	private static final Particle VISUAL = Particle.SPELL_INSTANT;

	private Location origin;
	private Location location;
	private Vector direction;
	private int damage;

	private long cooldown;
	private long charge;
	private double range;

	public Aim(RegisteredAbility register, Player player) {
		super(register, player);
		this.damage = DAMAGE;
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_AIMCD_C4CD_SLICE_CD)) {
			this.cooldown -= 500;
		}
		
		this.charge = CHARGE;
		if(bender.hasPerk(BendingPerk.MASTER_AIMCHARGETIME_PARASTICKCD_CONCUSSIONCD)) {
			this.charge -= 500;
		}
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.MASTER_AIMRANGE_VITALPOINTCHI_SLICEINTERVAL)) {
			this.range += 5;
		}
	}
	
	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.PREPARING || getState() == BendingAbilityState.PREPARED) {
			origin = player.getEyeLocation();
			location = origin.clone();
			direction = origin.getDirection().normalize();

			setState(BendingAbilityState.PROGRESSING);

			origin.getWorld().playSound(origin, Sound.ENTITY_ARROW_SHOOT, 10, 1);
			bender.cooldown(NAME, cooldown);
		}

		return false;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PREPARING || getState() == BendingAbilityState.PREPARED) {
			if(!player.isSneaking()) {
				remove();
				return;
			}
			if(getState() == BendingAbilityState.PREPARING && startedTime + charge < System.currentTimeMillis()) {
				damage += FOCUS_DAMAGE;
				setState(BendingAbilityState.PREPARED);
			}
			if(getState() == BendingAbilityState.PREPARED) {
				Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
				player.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
			} else {
				Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
				player.getWorld().spawnParticle(Particle.SPELL, loc, 1, 0, 0, 0, 0);
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
			if (!player.getWorld().equals(location.getWorld()) 
					|| location.distance(origin) > range 
					|| BlockTools.isSolid(location.getBlock())) {
				remove();
				return;
			}

			advanceLocation();
			if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
				remove();
				return;
			}
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2.1)) {
				if(affect(entity)) {
					remove();
				}
			}
		}
	}
	
	private boolean affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		
		if (entity == player) {
			return false;
		}
		double factor = location.distance(origin) / EFFECTIVE_RANGE;
		if(!bender.hasPerk(BendingPerk.MASTER_SNIPE_PERSIST_CONSTITUTION)) {
			if(factor > 1) {
				factor = 1;
			}
		}
		DamageTools.damageEntity(bender, entity, this, damage*factor);
		return true;
	}

	private void advanceLocation() {
		location.getWorld().spawnParticle(VISUAL, location, 1, 0, 0, 0);
		location = location.add(direction.clone().multiply(1.5));
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.holdsTool(player)) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		if (instances.containsKey(player)) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public void stop() {
		
	}

}

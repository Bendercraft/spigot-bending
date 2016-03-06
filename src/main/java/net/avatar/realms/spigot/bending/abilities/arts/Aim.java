package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;


@ABendingAbility(name = Aim.NAME, affinity = BendingAffinity.BOW)
public class Aim extends BendingActiveAbility {
	public final static String NAME = "Aim";

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 8;
	
	@ConfigurationParameter("Focus-Damage")
	private static int FOCUS_DAMAGE = 4;

	@ConfigurationParameter("Range")
	private static int RANGE = 40;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 3000;
	
	@ConfigurationParameter("Charge")
	private static long CHARGE = 2000;

	private static final Particle VISUAL = Particle.SPELL_INSTANT;

	private Location origin;
	private Location location;
	private Vector direction;
	private int damage;

	public Aim(RegisteredAbility register, Player player) {
		super(register, player);
		damage = DAMAGE;
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
			bender.cooldown(NAME, COOLDOWN);
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
			if(getState() == BendingAbilityState.PREPARING && startedTime + CHARGE < System.currentTimeMillis()) {
				damage += FOCUS_DAMAGE;
				setState(BendingAbilityState.PREPARED);
			}
			if(getState() == BendingAbilityState.PREPARED) {
				player.getWorld().playEffect(player.getEyeLocation(), 
						Effect.SMOKE, 
						Tools.getIntCardinalDirection(player.getEyeLocation().getDirection()), 3);
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
			if (!player.getWorld().equals(location.getWorld()) 
					|| location.distance(origin) > RANGE 
					|| BlockTools.isSolid(location.getBlock())) {
				remove();
				return;
			}

			advanceLocation();
			if (!affectAround()) {
				remove();
			}
		}
	}

	private boolean affectAround() {
		if (ProtectionManager.isLocationProtectedFromBending(player, NAME, location)) {
			return false;
		}
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2.1)) {
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			EntityTools.damageEntity(player, entity, damage);
			return false;
		}
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

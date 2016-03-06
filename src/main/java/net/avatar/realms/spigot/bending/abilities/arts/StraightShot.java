package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

/**
 * 
 * This ability throws a poisonned dart to straight foward. If the dart hit an
 * entity, this entity gets poisonned. The type of poisonned can change if
 * specifics items are hold in hand.
 *
 */

@ABendingAbility(name = StraightShot.NAME, affinity = BendingAffinity.BOW)
public class StraightShot extends BendingActiveAbility {
	public final static String NAME = "StraightShot";

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;

	@ConfigurationParameter("Range")
	private static int RANGE = 20;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;
	
	private static final Particle VISUAL = Particle.SPELL_WITCH;

	public StraightShot(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		Location origin = player.getEyeLocation();
		Location current = origin.clone();
		Vector direction = origin.getDirection().normalize();
		
		while(affectAround(current) 
				&& player.getWorld().equals(current.getWorld()) 
				&& current.distance(origin) < RANGE 
				&& !BlockTools.isSolid(current.getBlock())) {
			current = current.add(direction.multiply(1.1));
			current.getWorld().spawnParticle(VISUAL, current, 1, 0, 0, 0);
		}

		origin.getWorld().playSound(origin, Sound.ENTITY_ARROW_SHOOT, 10, 1);
		bender.cooldown(NAME, COOLDOWN);

		return false;
	}
	
	private boolean affectAround(Location location) {
		if (ProtectionManager.isLocationProtectedFromBending(player, NAME, location)) {
			return false;
		}
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2.1)) {
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			EntityTools.damageEntity(player, entity, DAMAGE);
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		
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

package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

/**
 * 
 * This ability throws a poisonned dart to straight foward. If the dart hit an
 * entity, this entity gets poisonned. The type of poisonned can change if
 * specifics items are hold in hand.
 *
 */

@ABendingAbility(name = "Aim", bind = BendingAbilities.Aim, element = BendingElement.Master, affinity = BendingAffinity.Bowman)
public class Aim extends BendingActiveAbility {

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;

	@ConfigurationParameter("Range")
	private static int RANGE = 20;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 2000;

	private static final ParticleEffect VISUAL = ParticleEffect.VILLAGER_HAPPY;

	private Location origin;
	private Location location;
	private Vector direction;

	public Aim(Player player) {
		super(player);
	}

	@Override
	public boolean swing() {
		if (getState().equals(BendingAbilityState.Preparing)) {
			return true;
		}

		if (!getState().equals(BendingAbilityState.Start)) {
			return false;
		}

		this.origin = this.player.getEyeLocation();
		this.location = this.origin.clone();
		this.direction = this.origin.getDirection().normalize();

		setState(BendingAbilityState.Preparing);

		this.origin.getWorld().playSound(this.origin, Sound.SHOOT_ARROW, 10, 1);
		this.bender.cooldown(BendingAbilities.PoisonnedDart, COOLDOWN);

		return false;
	}

	@Override
	public void progress() {
		if (getState() == BendingAbilityState.Preparing) {
			setState(BendingAbilityState.Progressing);
		}

		if (getState() != BendingAbilityState.Progressing) {
			return;
		}

		if (!this.player.getWorld().equals(this.location.getWorld()) 
				|| this.location.distance(this.origin) > RANGE 
				|| BlockTools.isSolid(this.location.getBlock())) {
			remove();
			return;
		}

		advanceLocation();
		if (!affectAround()) {
			remove();
		}
	}

	private boolean affectAround() {
		if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.PoisonnedDart, this.location)) {
			return false;
		}
		int cptEnt = 0;
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, 2.1)) {
			if (entity.getEntityId() == this.player.getEntityId()) {
				continue;
			}
			EntityTools.damageEntity(this.player, entity, DAMAGE);
			cptEnt++;
			break;
		}

		if (cptEnt > 0) {
			return false;
		}
		return true;
	}

	private void advanceLocation() {
		VISUAL.display(0, 0, 0, 1, 1, this.location, 20);
		this.location = this.location.add(this.direction.clone().multiply(1.5));
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.isTool(this.player.getItemInHand().getType())) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.PoisonnedDart);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		if (instances.containsKey(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}

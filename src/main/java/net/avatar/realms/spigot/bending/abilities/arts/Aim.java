package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
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
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * 
 * This ability throws a poisonned dart to straight foward. If the dart hit an
 * entity, this entity gets poisonned. The type of poisonned can change if
 * specifics items are hold in hand.
 *
 */

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

	private static final ParticleEffect VISUAL = ParticleEffect.SPELL_INSTANT;

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
			this.origin = this.player.getEyeLocation();
			this.location = this.origin.clone();
			this.direction = this.origin.getDirection().normalize();

			setState(BendingAbilityState.PROGRESSING);

			this.origin.getWorld().playSound(this.origin, Sound.ENTITY_ARROW_SHOOT, 10, 1);
			this.bender.cooldown(NAME, COOLDOWN);
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
						Tools.getIntCardinalDirection(this.player.getEyeLocation().getDirection()), 3);
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
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
	}

	private boolean affectAround() {
		if (ProtectionManager.isLocationProtectedFromBending(this.player, NAME, this.location)) {
			return false;
		}
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, 2.1)) {
			if (entity.getEntityId() == this.player.getEntityId()) {
				continue;
			}
			EntityTools.damageEntity(this.player, entity, damage);
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

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
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

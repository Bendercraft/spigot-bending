package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

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
	private static long COOLDOWN = 2000;

	public StraightShot(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if (getState().equals(BendingAbilityState.PREPARING)) {
			return true;
		}

		if (!getState().equals(BendingAbilityState.START)) {
			return false;
		}

		Location origin = this.player.getEyeLocation();

		setState(BendingAbilityState.PREPARING);
		
		LivingEntity entity = EntityTools.getTargetedEntity(player, RANGE);
		
		if(entity != null) {
			EntityTools.damageEntity(this.player, entity, DAMAGE);
		}

		origin.getWorld().playSound(origin, Sound.SHOOT_ARROW, 10, 1);
		bender.cooldown(NAME, COOLDOWN);

		return false;
	}

	@Override
	public void progress() {
		
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

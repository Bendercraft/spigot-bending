package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.*;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;

import java.util.Map;

/**
 * 
 * This ability throws a poisonned dart to straight foward. If the dart hit an
 * entity, this entity gets poisonned. The type of poisonned can change if
 * specifics items are hold in hand.
 *
 */

@ABendingAbility(name = Mark.NAME, affinity = BendingAffinity.BOW)
public class Mark extends BendingActiveAbility {
	public final static String NAME = "Mark";

	@ConfigurationParameter("Range")
	private static int RANGE = 50;
	
	@ConfigurationParameter("Duration")
	private static int DURATION = 10000;
	
	@ConfigurationParameter("Amplifier")
	private static int AMPLIFIER = 1;

	private LivingEntity target;

	public Mark(RegisteredAbility register, Player player) {
		super(register, player);
	}
	
	@Override
	public boolean sneak() {
		target = EntityTools.getTargetedEntity(player, RANGE);
		if(target != null) {
			if(affect(target)) {
				bender.cooldown(this, DURATION);
				setState(BendingAbilityState.PROGRESSING);
			}
		}
		return false;
	}


	@Override
	public void progress() {
		
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		if (target != null) {
			target.removePotionEffect(PotionEffectType.GLOWING);
		}
	}

	public LivingEntity getTarget() {
		return target;
	}

	@Override
	protected long getMaxMillis() {
		return DURATION;
	}
	
	private boolean affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		target.addPotionEffect(PotionEffectType.GLOWING.createEffect(DURATION*20/1000, 1));
		return true;
	}

	public static boolean isMarked(LivingEntity target) {
		return isMarked(target.getEntityId());
	}

	public static boolean isMarked(int entityID) {
		Map<Object, BendingAbility> marks = AbilityManager.getManager().getInstances(NAME);
		if (marks == null || marks.isEmpty()) {
			return false;
		}
		for(BendingAbility raw : marks.values()) {
			Mark ability = (Mark) raw;
			if(ability.getTarget() != null
					&& ability.getTarget().getEntityId() == entityID) {
				return true;
			}
		}
		return false;
	}
}

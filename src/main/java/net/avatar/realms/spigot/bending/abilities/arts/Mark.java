package net.avatar.realms.spigot.bending.abilities.arts;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
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
			target.addPotionEffect(PotionEffectType.GLOWING.createEffect(DURATION*20/1000, 1));
			bender.cooldown(this, DURATION);
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
		target.removePotionEffect(PotionEffectType.GLOWING);
	}

}

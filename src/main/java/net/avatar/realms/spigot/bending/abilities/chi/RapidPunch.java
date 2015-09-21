package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * 
 * This ability will be modified : When you hit an entity, you deal some damage
 * to it. The more you hit, the more you deal damage. (But the more big the
 * cooldown will be)
 *
 */
@BendingAbility(name = "Rapid Punch", bind = BendingAbilities.RapidPunch, element = BendingElement.ChiBlocker)
public class RapidPunch extends BendingActiveAbility {

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 7;

	@ConfigurationParameter("Range")
	public static int RANGE = 4;

	@ConfigurationParameter("Punches")
	private static int punches = 4;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 60 * 1000;

	public RapidPunch(Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		Entity t = EntityTools.getTargettedEntity(player, RANGE);

		if (t == null) {
			setState(BendingAbilityState.CannotStart);
			return;
		}
	}

	@Override
	public boolean swing() {
		switch (this.state) {
		case None:
		case CannotStart:
			return true;

		case CanStart:
		case Preparing:
		case Prepared:
		case Progressing:
		case Ended:
		case Removed:
		default:
			return false;
		}
	}

	@Override
	public void remove() {
		this.bender.cooldown(BendingAbilities.RapidPunch, COOLDOWN);
		super.remove();
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.isWeapon(this.player.getItemInHand().getType())) {
			return false;
		}

		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.RapidPunch);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		if (instances.containsKey(this.player)) {
			return false;
		}

		return true;
	}

}
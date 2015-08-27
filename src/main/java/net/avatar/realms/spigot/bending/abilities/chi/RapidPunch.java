package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * 
 * This ability will be modified : 
 * When you hit an entity, you deal some damage to it.
 * The more you hit, the more you deal damage. (But the more big the cooldown will be)
 *
 */
@BendingAbility(name="Rapid Punch", element=BendingType.ChiBlocker)
public class RapidPunch extends Ability {

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

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		Entity t = EntityTools.getTargettedEntity(player, RANGE);

		if (t == null) {
			setState(AbilityState.CannotStart);
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
			default :
				return false;
		}
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}


		return true;
	}

	@Override
	public void remove() {
		this.bender.cooldown(Abilities.RapidPunch, COOLDOWN);
		super.remove();
	}


	@Override
	public Abilities getAbilityType () {
		return Abilities.RapidPunch;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	protected long getMaxMillis () {
		return MAX_DURATION;
	}

	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.isWeapon(this.player.getItemInHand().getType())) {
			return false;
		}

		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.RapidPunch);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		if (instances.containsKey(this.player)) {
			return false;
		}

		return true;
	}

}
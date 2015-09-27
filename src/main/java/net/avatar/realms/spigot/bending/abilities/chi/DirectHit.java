package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * 
 * This ability hit the first entity in front of you powerfully driving to a
 * knockback You must be sneaking when clicking to activate this technique.
 *
 */
@BendingAbility(name = "Direct Hit", bind = BendingAbilities.DirectHit, element = BendingElement.ChiBlocker)
public class DirectHit extends BendingActiveAbility {

	@ConfigurationParameter("Damage")
	public static long DAMAGE = 5;

	@ConfigurationParameter("Knockback")
	public static long KNOCKBACK = 1;

	@ConfigurationParameter("Range")
	public static long RANGE = 4;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;

	public DirectHit(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		if(state == BendingAbilityState.CanStart) {
			if(player.isSneaking()) {
				LivingEntity target = EntityTools.getTargettedEntity(player, RANGE);
				if(target == null) {
					setState(BendingAbilityState.Ended);
					return false;
				}
				EntityTools.damageEntity(player, target, DAMAGE);
				target.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(KNOCKBACK));
				bender.cooldown(this, COOLDOWN);
				if(ComboPoints.getComboPointAmount(player) == 0) {
					ComboPoints.addComboPoint(player, target);
					ComboPoints.addComboPoint(player, target);
				}
				AbilityManager.getManager().addInstance(this);
				setState(BendingAbilityState.Progressing);
			}
		}
		return false;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		return false;
	}

	@Override
	protected long getMaxMillis() {
		return 1;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.isWeapon(this.player.getItemInHand().getType())) {
			return false;
		}

		return true;
	}

}

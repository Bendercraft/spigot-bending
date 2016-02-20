package net.avatar.realms.spigot.bending.abilities.arts;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 *
 * This ability hit the first entity in front of you powerfully driving to a
 * knockback You must be sneaking when clicking to activate this technique.
 *
 */
@ABendingAbility(name = PowerShot.NAME, affinity=BendingAffinity.BOW)
public class PowerShot extends BendingActiveAbility {
	public final static String NAME = "PowerShot";

	@ConfigurationParameter("Damage")
	public static long DAMAGE = 5;

	@ConfigurationParameter("Knockback")
	public static long KNOCKBACK = 2;

	@ConfigurationParameter("Range")
	public static long RANGE = 4;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

	public PowerShot(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		LivingEntity target = EntityTools.getTargetedEntity(this.player, RANGE);
		if(target == null) {
			remove();
			return false;
		}
		if(this.player.isSneaking()) {
			EntityTools.damageEntity(this.player, target, DAMAGE);
			target.setVelocity(this.player.getEyeLocation().getDirection().clone().normalize()
					.multiply((0.5 + this.player.getVelocity().length()) * KNOCKBACK));

			this.bender.cooldown(this, COOLDOWN * 2);
		} else {
			this.bender.cooldown(this, COOLDOWN);
		}
		return false;
	}

	@Override
	public void progress() {
		
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

		if (EntityTools.isTool(this.player.getItemInHand().getType())) {
			return false;
		}

		return true;
	}

	@Override
	public void stop() {
		
	}

}

package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

/**
 *
 * This ability hit the first entity in front of you powerfully driving to a
 * knockback You must be sneaking when clicking to activate this technique.
 *
 */
@ABendingAbility(name = BlankPoint.NAME, affinity=BendingAffinity.BOW)
public class BlankPoint extends BendingActiveAbility {
	public final static String NAME = "BlankPoint";

	@ConfigurationParameter("Damage")
	public static long DAMAGE = 5;

	@ConfigurationParameter("Knockback")
	public static long KNOCKBACK = 4;

	@ConfigurationParameter("Range")
	public static long RANGE = 4;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;

	public BlankPoint(RegisteredAbility register, Player player) {
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
			DamageTools.damageEntity(bender, target, this, DAMAGE);
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

		if (EntityTools.holdsTool(player)) {
			return false;
		}

		return true;
	}

	@Override
	public void stop() {
		
	}

}

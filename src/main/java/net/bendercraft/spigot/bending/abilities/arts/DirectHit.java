package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

/**
 *
 * This ability hit the first entity in front of you powerfully driving to a
 * knockback You must be sneaking when clicking to activate this technique.
 *
 */
@ABendingAbility(name = DirectHit.NAME, element = BendingElement.MASTER, shift=false)
public class DirectHit extends BendingActiveAbility {
	public final static String NAME = "DirectHit";

	@ConfigurationParameter("Damage")
	public static long DAMAGE = 5;

	@ConfigurationParameter("Knockback")
	public static long KNOCKBACK = 2;

	@ConfigurationParameter("Range")
	public static long RANGE = 4;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

	private long knockback;
	private long cooldown;

	public DirectHit(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.knockback = KNOCKBACK;
		if(bender.hasPerk(BendingPerk.MASTER_DIRECTHIT_PUSH)) {
			this.knockback += 1;
		}
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_DIRECTHIT_COOLDOWN)) {
			this.cooldown -= 500;
		}
	}

	@Override
	public boolean swing() {
		LivingEntity target = EntityTools.getTargetedEntity(this.player, RANGE);
		if(target == null) {
			remove();
			return false;
		}
		if(this.player.isSneaking()) {
			if(affect(target)) {
				this.bender.cooldown(this, cooldown);
			}
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

	private boolean affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		DamageTools.damageEntity(bender, entity, this, DAMAGE, false, DamageTools.DEFAULT_NODAMAGETICKS, 0.0f, true);
		Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
		entity.setVelocity(direction.multiply(knockback));
		
		return true;
	}
}

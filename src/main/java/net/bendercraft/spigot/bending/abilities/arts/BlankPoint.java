package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
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
@ABendingAbility(name = BlankPoint.NAME, affinity=BendingAffinity.BOW)
public class BlankPoint extends BendingActiveAbility {
	public final static String NAME = "BlankPoint";

	@ConfigurationParameter("Damage")
	public static long DAMAGE = 5;

	@ConfigurationParameter("Knockback")
	public static double KNOCKBACK = 4;

	@ConfigurationParameter("Range")
	public static long RANGE = 4;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;
	
	private double knockback;
	private long cooldown;
	private long damage;

	public BlankPoint(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.knockback = KNOCKBACK;
		if(bender.hasPerk(BendingPerk.MASTER_BLANKPOINTPUSH_POISONNEDARTRANGE_NEBULARCHAINRANGE)) {
			this.knockback += 0.5;
		}
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_BLANKPOINTCD_SMOKEBOMBCD_DASHSTUN)) {
			this.cooldown -= 500;
		}
		
		this.damage = DAMAGE;
		if(bender.hasPerk(BendingPerk.MASTER_BLANKPOINTDAMAGE_PARASTICKCD_NEBULARRANGE)) {
			this.damage += 1;
		}
	}

	@Override
	public boolean swing() {
		LivingEntity target = EntityTools.getTargetedEntity(this.player, RANGE);
		if(target == null) {
			remove();
			return false;
		}
		affect(target);
		if(bender.hasPerk(BendingPerk.MASTER_DISENGAGE_PARAPARASTICK_CONSTITUTION)) {
			Vector push = player.getEyeLocation().getDirection().multiply(-1).normalize();
			push.setY(0.2);
			push = push.normalize().multiply(knockback);
			
			player.setVelocity(push);
		}
		this.bender.cooldown(this, cooldown);
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
	
	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		DamageTools.damageEntity(bender, entity, this, damage);
		Vector push = player.getEyeLocation().getDirection().normalize();
		push.setY(0.2);
		push = push.normalize().multiply(knockback);
		
		entity.setVelocity(push);
	}

}

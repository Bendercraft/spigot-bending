package net.bendercraft.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

/**
 *
 * This ability will be modified : When you hit an entity, you deal a small
 * amount of damage to it and it gets slown. The more you hit it, the more it
 * get slown.
 *
 */
@ABendingAbility(name = VitalPoint.NAME, affinity=BendingAffinity.CHI, shift=false)
public class VitalPoint extends BendingActiveAbility {
	public final static String NAME = "VitalPoint";

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 1;

	@ConfigurationParameter("Damage-Increment")
	private static int DAMAGE_INCREMENT = 1;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;

	@ConfigurationParameter("Duration")
	private static long DURATION = 2500;

	@ConfigurationParameter("Slown-Duration")
	private static int SLOW_DURATION = 5; // In seconds

	@ConfigurationParameter("Chiblock-Duration")
	private static long CHIBLOCK_DURATION = 500;

	@ConfigurationParameter("Max-Range")
	private static float MAX_RANGE = 3.5f;

	private static final PotionEffectType TYPE = PotionEffectType.SLOW;

	private int damage;
	private long cooldown;
	private LivingEntity target;
	private int amplifier;

	public VitalPoint(RegisteredAbility register, Player player) {
		super(register, player);

		this.amplifier = 0;
		this.damage = DAMAGE;
		this.cooldown = COOLDOWN;
	}

	@Override
	public boolean swing() {
		if(isState(BendingAbilityState.START)) {
			target = EntityTools.getTargetedEntity(player, MAX_RANGE);
			if (target == null) {
				return false;
			}

			cooldown = 1000;
			damage += DAMAGE_INCREMENT;
			affect(target);
		}
		return true;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.holdsTool(player)) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);

		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		return !instances.containsKey(this.player);
	}

	@Override
	protected long getMaxMillis() {
		return 2000;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void progress() {
		
	}

	@Override
	public void stop() {
		
	}
	
	private void affect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		if (ParaStick.hasParaStick(player)) {
			ParaStick stick = ParaStick.getParaStick(player);
			stick.consume();
			
			DamageTools.damageEntity(bender, target, this, damage, true, 0, 0.0f, true);
			if (this.target instanceof Player) {
				EntityTools.blockChi((Player) this.target, CHIBLOCK_DURATION);
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*4, 1));
			}
			this.target.addPotionEffect(new PotionEffect(TYPE, (int) (DURATION / 20), 130));
			this.cooldown += COOLDOWN / (6);
		} else {
			DamageTools.damageEntity(bender, target, this, damage, true, 0, 0.0f, true);
			this.target.addPotionEffect(new PotionEffect(TYPE, (int) (DURATION / 20), this.amplifier));
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*3, 1));
		}
		this.bender.cooldown(NAME, this.cooldown);
	}
}

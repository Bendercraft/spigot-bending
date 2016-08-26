package net.bendercraft.spigot.bending.abilities.arts;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;


@ABendingAbility(name = PoisonnedDart.NAME, affinity = BendingAffinity.CHI, shift=false)
public class PoisonnedDart extends BendingActiveAbility {
	public final static String NAME = "PoisonnedDart";

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;

	@ConfigurationParameter("Range")
	private static int RANGE = 20;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 4000;
	
	@ConfigurationParameter("Radius")
	private static double RADIUS = 2.1;
	
	@ConfigurationParameter("Parastick-Chiblock-Duration")
	private static long PARASTICK_CHIBLOCK_DURATION = 3000;

	private static final Particle VISUAL = Particle.VILLAGER_HAPPY;

	private Location origin;
	private Location location;
	private Vector direction;
	private Effect effect = Effect.POISON;

	public PoisonnedDart(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if(isState(BendingAbilityState.START) || isState(BendingAbilityState.PREPARED)) {
			if(player.isSneaking()) {
				effect = effect.next;
				if(effect == null) {
					effect = Effect.POISON;
				}
				setState(BendingAbilityState.PREPARED);
				return false;
			} else {
				origin = player.getEyeLocation();
				location = origin.clone();
				direction = origin.getDirection().normalize();
				origin.getWorld().playSound(this.origin, Sound.ENTITY_ARROW_SHOOT, 10, 1);
				bender.cooldown(NAME, COOLDOWN);
				setState(BendingAbilityState.PROGRESSING);
			}
		}
		return false;
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

		if (instances.containsKey(this.player)) {
			return false;
		}
		
		if(isState(BendingAbilityState.PREPARED) && !NAME.equals(bender.getAbility())) {
			return false;
		}

		return true;
	}

	@Override
	public void progress() {
		if(isState(BendingAbilityState.PREPARED)) {
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(effect.visual, loc, 1, 0, 0, 0, 0);
		}
		
		if (!isState(BendingAbilityState.PROGRESSING)) {
			return;
		}

		if (!player.getWorld().equals(location.getWorld()) 
				|| location.distance(origin) > RANGE 
				|| BlockTools.isSolid(location.getBlock())) {
			remove();
			return;
		}

		location.getWorld().spawnParticle(VISUAL, location, 1, 0, 0, 0);
		location = location.add(direction.clone().normalize());
		
		if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
			remove();
			return;
		}
		
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, RADIUS)) {
			if(affect(entity)) {
				remove();
				return;
			}
		}
	}
	
	@Override
	public void stop() {
		
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	private boolean affect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		if (entity == player) {
			return false;
		}

		if (effect.type == null) {
			for (PotionEffect effect : entity.getActivePotionEffects()) {
				entity.removePotionEffect(effect.getType());
			}
			entity.getActivePotionEffects().clear();
		} else {
			entity.addPotionEffect(new PotionEffect(effect.type, 20*2, 1));
			DamageTools.damageEntity(bender, entity, this, DAMAGE);
		}
		if(ParaStick.hasParaStick(player) && entity instanceof Player) {
			ParaStick stick = ParaStick.getParaStick(player);
			stick.consume();
			EntityTools.blockChi((Player) entity, PARASTICK_CHIBLOCK_DURATION);
		}
		return true;
	}
	
	private enum Effect {
		PURGE(null, null, Particle.HEART), 
		CONFUSION(PotionEffectType.POISON, PURGE, Particle.CRIT), 
		BLINDNESS(PotionEffectType.POISON, CONFUSION, Particle.DRAGON_BREATH), 
		POISON(PotionEffectType.POISON, BLINDNESS, Particle.SPELL);
		
		public final PotionEffectType type;
		public final Effect next;
		public final Particle visual;
		
		private Effect(PotionEffectType type, Effect next, Particle visual) {
			this.type = type;
			this.next = next;
			this.visual = visual;
		}
	}

}

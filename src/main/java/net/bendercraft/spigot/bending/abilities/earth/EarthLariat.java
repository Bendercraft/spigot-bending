package net.bendercraft.spigot.bending.abilities.earth;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = EarthLariat.NAME, element = BendingElement.EARTH)
public class EarthLariat extends BendingActiveAbility {
	public final static String NAME = "EarthLariat";
	
	@ConfigurationParameter("Range")
	private static double RANGE = 15;
	
	@ConfigurationParameter("Max-live")
	private static long MAX_LIVE = 3; // In seconds
	
	@ConfigurationParameter("Distance")
	private static long DISTANCE = 2;
	
	@ConfigurationParameter("Confusion-Duration")
	private static int CONFUSION_DURATION = 5; // In seconds
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;
	
	private LivingEntity target;
	
	private double range;
	private int confusionDuration;
	private long cooldown;

	public EarthLariat(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHLARIAT_RANGE)) {
			this.range += 2;
		}
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHLARIAT_COOLDOWN)) {
			this.cooldown -= 2000;
		}
		
		this.confusionDuration = CONFUSION_DURATION;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHLARIAT_STUN_1)) {
			this.confusionDuration += 1;
		}
		if(bender.hasPerk(BendingPerk.EARTH_EARTHLARIAT_STUN_2)) {
			this.confusionDuration += 1;
		}
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			if(!player.isSneaking()) {
				target = EntityTools.getTargetedEntity(player, range);
				if(target == null) {
					remove();
					return false;
				}
				
				if(BlockTools.isEarthbendable(player, register, player.getLocation().getBlock().getRelative(BlockFace.DOWN))
						&& BlockTools.isEarthbendable(player, register, target.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
					if(affect(target)) {
						setState(BendingAbilityState.PROGRESSING);
						bender.cooldown(this, cooldown);
					}
				}
			}
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			if(player.getLocation().distance(target.getLocation()) < DISTANCE) {
				target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, confusionDuration, 1));
			}
			remove();
		}
		return false;
	}

	@Override
	public boolean sneak() {
		return false;
	}

	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if(now - startedTime > MAX_LIVE*1000) {
			remove();
		}
	}

	@Override
	public Object getIdentifier() {
		return player;
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
		
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
		
		Location middle = player.getLocation().clone().add(target.getLocation()).multiply(0.5);
		
		player.setVelocity(middle.toVector().clone().subtract(player.getLocation().toVector()).multiply(0.5));
		target.setVelocity(middle.toVector().clone().subtract(target.getLocation().toVector()).multiply(0.5));
		return true;
	}

}

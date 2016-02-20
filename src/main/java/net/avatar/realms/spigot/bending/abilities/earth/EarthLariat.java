package net.avatar.realms.spigot.bending.abilities.earth;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

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
	
	private LivingEntity target;

	public EarthLariat(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			if(!player.isSneaking()) {
				target = EntityTools.getTargetedEntity(player, RANGE);
				if(target == null) {
					remove();
					return false;
				}
				
				if(BlockTools.isEarthbendable(player, NAME, player.getLocation().getBlock().getRelative(BlockFace.DOWN))
						&& BlockTools.isEarthbendable(player, NAME, target.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
					player.getWorld().playEffect(player.getLocation(), Effect.GHAST_SHOOT, 0, 4);
					target.getWorld().playEffect(target.getLocation(), Effect.GHAST_SHOOT, 0, 4);
					
					Location middle = player.getLocation().clone().add(target.getLocation()).multiply(0.5);
					
					player.setVelocity(middle.toVector().clone().subtract(player.getLocation().toVector()).multiply(0.5));
					target.setVelocity(middle.toVector().clone().subtract(target.getLocation().toVector()).multiply(0.5));
					
					setState(BendingAbilityState.PROGRESSING);
				}
			}
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			if(player.getLocation().distance(target.getLocation()) < DISTANCE) {
				target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CONFUSION_DURATION, 1));
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

}

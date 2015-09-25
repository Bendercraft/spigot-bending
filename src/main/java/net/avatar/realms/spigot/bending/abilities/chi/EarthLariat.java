package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@BendingAbility(name = "Earth Lariat", bind = BendingAbilities.EarthLariat, element = BendingElement.ChiBlocker, affinity = BendingAffinity.ChiEarth)
public class EarthLariat extends BendingActiveAbility {
	@ConfigurationParameter("Range")
	private static double RANGE = 15;
	
	@ConfigurationParameter("Max-live")
	private static long MAX_LIVE = 3; // In seconds
	
	@ConfigurationParameter("Distance")
	private static long DISTANCE = 2;
	
	@ConfigurationParameter("Confusion-Duration")
	private static int CONFUSION_DURATION = 5; // In seconds
	
	private LivingEntity target;

	public EarthLariat(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		if(state == BendingAbilityState.CanStart) {
			if(!player.isSneaking()) {
				if(!ComboPoints.consume(player,1)) {
					setState(BendingAbilityState.Ended);
					return false;
				}
				
				target = EntityTools.getTargettedEntity(player, RANGE);
				if(target == null) {
					setState(BendingAbilityState.Ended);
					return false;
				}
				
				if(BlockTools.isEarthbendable(player, AbilityManager.getManager().getAbilityType(this), player.getLocation().getBlock().getRelative(BlockFace.DOWN))
						&& BlockTools.isEarthbendable(player, AbilityManager.getManager().getAbilityType(this), target.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
					//BlockTools.moveEarth(player, player.getLocation().getBlock().getRelative(BlockFace.DOWN), new Vector(0,1,0), 1);
					//BlockTools.moveEarth(player, target.getLocation().getBlock().getRelative(BlockFace.DOWN), new Vector(0,1,0), 1);
					player.getWorld().playEffect(player.getLocation(), Effect.GHAST_SHOOT, 0, 4);
					target.getWorld().playEffect(target.getLocation(), Effect.GHAST_SHOOT, 0, 4);
					
					Location middle = player.getLocation().clone().add(target.getLocation()).multiply(0.5);
					
					player.setVelocity(middle.toVector().clone().subtract(player.getLocation().toVector()).multiply(0.5));
					target.setVelocity(middle.toVector().clone().subtract(target.getLocation().toVector()).multiply(0.5));
					
					setState(BendingAbilityState.Progressing);
					AbilityManager.getManager().addInstance(this);
				}
			}
		} else if(state == BendingAbilityState.Progressing) {
			if(player.getLocation().distance(target.getLocation()) < DISTANCE) {
				target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, CONFUSION_DURATION, 1));
				Bending.log.info("Confused");
			}
			setState(BendingAbilityState.Ended);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		return false;
	}

	@Override
	public boolean progress() {
		if(!super.progress()) {
			return false;
		}
		
		long now = System.currentTimeMillis();
		if(now - startedTime > MAX_LIVE*1000) {
			return false;
		}
		
		return true;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}

package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Fire Shield", element=BendingType.Fire)
public class FireProtection extends ActiveAbility {

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1000;

	@ConfigurationParameter("Duration")
	private static long DURATION = 1200;
	
	private static long interval = 100;
	private static double radius = 3;
	private static double discradius = 1.5;
	private static boolean ignite = true;
	
	private long time;
	
	public FireProtection(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;
			case CanStart:
				if (!this.player.getEyeLocation().getBlock().isLiquid()) {
					this.time = this.startedTime;
					AbilityManager.getManager().addInstance(this);
				}
				return false;
			case Preparing:
			case Prepared:
			case Progressing:
			case Ending:
			case Ended:
			case Removed:
			default:
				return true;
		}
	}
	
	@Override
	public void remove() {
		this.bender.cooldown(Abilities.FireShield, COOLDOWN);
		super.remove();
	}
	
	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		
		if (System.currentTimeMillis() > (this.time + interval)) {
			this.time = System.currentTimeMillis();

			List<Block> blocks = new LinkedList<Block>();
			Location location = this.player.getEyeLocation().clone();
			Vector direction = location.getDirection();
			location = location.clone().add(direction.multiply(radius));
			
			if (ProtectionManager.isRegionProtectedFromBending(this.player,
					Abilities.FireShield, location)) {
				return false;
			}
			
			for (double theta = 0; theta < 360; theta += 20) {
				Vector vector = Tools.getOrthogonalVector(direction, theta,
						discradius);
				Block block = location.clone().add(vector).getBlock();
				if (!blocks.contains(block) && !BlockTools.isSolid(block)
						&& !block.isLiquid()) {
					blocks.add(block);
				}
			}
			
			for (Block block : blocks) {
				if (!ProtectionManager.isRegionProtectedFromBending(this.player,
						Abilities.FireShield, block.getLocation())) {
					block.getWorld().playEffect(block.getLocation(),
							Effect.MOBSPAWNER_FLAMES, 0, 20);
				}
			}
			
			for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
					discradius)) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(this.player,
						Abilities.FireShield, entity.getLocation())) {
					continue;
				}

				if ((this.player.getEntityId() != entity.getEntityId()) && ignite) {
					entity.setFireTicks(120);
					if (!(entity instanceof LivingEntity)) {
						entity.remove();
					}
				}
			}
			
			FireBlast.removeFireBlastsAroundPoint(location, discradius);
			WaterManipulation.removeAroundPoint(location, discradius);
			EarthBlast.removeAroundPoint(location, discradius);
			FireStream.removeAroundPoint(location, discradius);

		}
		return true;
	}
	
	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.FireShield);
		if (instances == null) {
			return true;
		}

		return !instances.containsKey(this.player);
	}
	
	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.FireShield;
	}
}

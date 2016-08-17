package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.air.AirBlast;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.Tools;

@ABendingAbility(name = FireShield.NAME, element = BendingElement.FIRE)
public class FireShield extends BendingActiveAbility {
	public final static String NAME = "FireShield";
	
	@ConfigurationParameter("Power")
	public static int POWER = 1;

	@ConfigurationParameter("Tick")
	public static long TICK = 1000;
	
	private static long interval = 100;
	private static double radius = 2;

	private long time;
	private long timePower;
	private FireProtection protect;

	public FireShield(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			time = System.currentTimeMillis();
			timePower = time;
			setState(BendingAbilityState.PROGRESSING);
			
		}
		return false;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			if(!bender.fire.can(NAME, POWER)) {
				return false;
			}
			protect = new FireProtection(this.player);
			bender.fire.consume(NAME, POWER);
			setState(BendingAbilityState.PROGRESSING);
			
		}
		return false;
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void progress() {
		if(protect != null) {
			if(!protect.progress()) {
				remove();
			}
			return;
		}
		if (!this.player.isSneaking()) {
			remove();
			return;
		}

		long now = System.currentTimeMillis();
		if (now > (time + interval)) {
			time = now;

			List<Block> blocks = new LinkedList<Block>();
			Location location = this.player.getEyeLocation().clone();

			for (double theta = 0; theta < 180; theta += 20) {
				for (double phi = 0; phi < 360; phi += 20) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					Block block = location.clone().add(radius * Math.cos(rphi) * Math.sin(rtheta), radius * Math.cos(rtheta), radius * Math.sin(rphi) * Math.sin(rtheta)).getBlock();
					if (!blocks.contains(block) && !BlockTools.isSolid(block) && !block.isLiquid()) {
						blocks.add(block);
					}
				}
			}

			for (Block block : blocks) {
				if (!ProtectionManager.isLocationProtectedFromBending(this.player, register, block.getLocation())) {
					block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
				}
			}
		}
		
		if(now > timePower+TICK) {
			timePower = now;
			bender.fire.grant(POWER);
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	public static class FireProtection {

		@ConfigurationParameter("Cooldown")
		public static long COOLDOWN = 1000;

		@ConfigurationParameter("Duration")
		private static long DURATION = 1200;

		private static long interval = 100;
		private static double radius = 4;
		private static double discradius = 1.5;

		private long time;
		private long startedTime;

		private Player player;

		public FireProtection(Player player) {
			this.player = player;
			this.time = System.currentTimeMillis();
			this.startedTime = this.time;
		}

		public boolean progress() {
			if (System.currentTimeMillis() > (time + interval)) {
				time = System.currentTimeMillis();
				
				if(startedTime + DURATION < time) {
					return false;
				}
				
				if (player.getEyeLocation().getBlock().isLiquid()) {
					return false;
				}

				Location location = player.getEyeLocation().clone();
				Vector direction = location.getDirection();
				location = location.clone().add(direction.multiply(radius));

				RegisteredAbility ability = AbilityManager.getManager().getRegisteredAbility(FireShield.NAME);
				if (ProtectionManager.isLocationProtectedFromBending(player, ability, location)) {
					return false;
				}

				location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
				for (double theta = 0; theta < 360; theta += 20) {
					Vector vector = Tools.getOrthogonalVector(direction, theta, discradius);
					Location temp = location.clone().add(vector);
					temp.getWorld().spawnParticle(Particle.FLAME, temp, 1, 0, 0, 0, 0);
				}

				for (Entity entity : EntityTools.getEntitiesAroundPoint(location, discradius)) {
					if (ProtectionManager.isEntityProtected(entity)) {
						continue;
					}
					if (ProtectionManager.isLocationProtectedFromBending(player, ability, entity.getLocation())) {
						continue;
					}

					if (player.getEntityId() != entity.getEntityId()) {
						if (!(entity instanceof LivingEntity)) {
							entity.remove();
						} else {
							Enflamed.enflame(player, entity, 5);
						}
					}
					if(entity instanceof Arrow) {
						entity.setVelocity(entity.getVelocity().multiply(-1));
						return false;
					}
				}
			}
			Location location = player.getEyeLocation().clone();
			Vector direction = location.getDirection();
			location = location.clone().add(direction.multiply(radius));
			
			if(FireBlast.removeOneAroundPoint(location, player, discradius)
					|| WaterManipulation.removeOneAroundPoint(location, player, discradius)
					|| EarthBlast.removeOneAroundPoint(location, player, discradius)
					|| AirBlast.removeOneBlastAroundPoint(location, discradius)) {
				return false;
			}
			return true;
		}
	}

}

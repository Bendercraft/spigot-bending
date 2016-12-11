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

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.air.AirBlast;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.Tools;

@ABendingAbility(name = FireShield.NAME, element = BendingElement.FIRE)
public class FireShield extends BendingActiveAbility {
	public final static String NAME = "FireShield";
	
	// FireShield
	@ConfigurationParameter("Restore")
	public static int RESTORE = 1;
	@ConfigurationParameter("Tick")
	public static long TICK = 1000;
	@ConfigurationParameter("Flame")
	public static int FLAME = 5;
	@ConfigurationParameter("Radius-Regen")
	public static double RADIUS_REGEN = 1;
	
	// FireProtection
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1000;
	@ConfigurationParameter("Duration")
	private static long DURATION = 1200;
	@ConfigurationParameter("Power")
	public static int POWER = 1;
	@ConfigurationParameter("Radius-Protection")
	public static double RADIUS_PROTECTION = 4;
	
	
	
	private static long interval = 100;
	

	private long time;
	private long timePower;
	private FireProtection protect;

	private long tick;

	public FireShield(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.tick = TICK;
		if(bender.hasPerk(BendingPerk.FIRE_FIREPROTECTION_BREATH)) {
			this.tick *= 0.85;
		}
		
		
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
			protect = new FireProtection(this, player);
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
					Block block = location.clone().add(RADIUS_REGEN * Math.cos(rphi) * Math.sin(rtheta), RADIUS_REGEN * Math.cos(rtheta), RADIUS_REGEN * Math.sin(rphi) * Math.sin(rtheta)).getBlock();
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
		
		if(now > timePower+tick) {
			timePower = now;
			bender.fire.grant(RESTORE);
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	public static class FireProtection {
		private static long interval = 100;
		private static double discradius = 1.5;

		private long time;
		private long startedTime;

		private Player player;
		private FireShield parent;
		
		private double radius;
		private long duration;
		private int flame;

		public FireProtection(FireShield parent, Player player) {
			this.parent = parent;
			this.player = player;
			this.time = System.currentTimeMillis();
			this.startedTime = this.time;
			this.duration = DURATION;
			if(parent.getBender().hasPerk(BendingPerk.FIRE_FIREPROTECTION_DURATION_1)) {
				this.duration += 500;
			}
			if(parent.getBender().hasPerk(BendingPerk.FIRE_FIREPROTECTION_DURATION_2)) {
				this.duration += 500;
			}
			this.flame= FLAME;
			if(parent.getBender().hasPerk(BendingPerk.FIRE_FIREPROTECTION_FLAME)) {
				this.flame += 1;
			}
			
			this.radius = RADIUS_PROTECTION;
			if(parent.getBender().hasPerk(BendingPerk.FIRE_FIREPROTECTION_AREA)) {
				this.radius += 0.5;
			}
		}

		public boolean progress() {
			if (System.currentTimeMillis() > (time + interval)) {
				time = System.currentTimeMillis();
				
				if(startedTime + duration < time) {
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
					if(entity instanceof Arrow) {
						entity.setVelocity(entity.getVelocity().multiply(-1));
						return false;
					} else {
						affect(entity);
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
		
		private void affect(Entity entity) {
			BendingHitEvent event = new BendingHitEvent(parent, entity);
			Bending.callEvent(event);
			if(event.isCancelled()) {
				return;
			}

			if (player == entity) {
				return;
			}
			if (!(entity instanceof LivingEntity)) {
				entity.remove();
			} else {
				Enflamed.enflame(player, entity, flame, parent);
			}
			
		}
	}
}

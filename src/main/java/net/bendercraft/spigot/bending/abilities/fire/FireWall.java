package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = FireWall.NAME, element = BendingElement.FIRE, shift=false)
public class FireWall extends BendingActiveAbility {
	public final static String NAME = "FireWall";

	private static final double PARTICLE_SPEED   = 1.0 / 32.0;
	private static final double MAX_ANGLE        = 50.0;
	private static       long   DISPLAY_INTERVAL = 450L;

	@ConfigurationParameter("Range")
	private static int RANGE = 4;

	@ConfigurationParameter("Power")
	private static int POWER = 4;

	@ConfigurationParameter("Interval")
	private static long DAMAGE_INTERVAL = 450;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 4;

	@ConfigurationParameter("Width")
	private static int WIDTH = 4;

	@ConfigurationParameter("Duration")
	private static long DURATION = 5000;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 7;
	
	@ConfigurationParameter("Flame-Time")
	private static int FLAME_TIME = 1;

	private Location origin;
	private long time;
	private int damagetick = 0, intervaltick = 0;
	private List<Block> blocks = new LinkedList<>();

	private int damage;
	private long duration;

	private int flameTime;
	private long damageInterval;

	public FireWall(RegisteredAbility register, Player player) {
		super(register, player);
		this.duration = DURATION;
		if(bender.hasPerk(BendingPerk.FIRE_FIREWALL_DURATION)) {
			this.duration += 1000;
		}
		
		this.damage = DAMAGE;
		
		this.flameTime = FLAME_TIME;
		if(bender.hasPerk(BendingPerk.FIRE_FIREWALL_FLAME)) {
			this.flameTime += 1;
		}
		
		this.damageInterval = DAMAGE_INTERVAL;
		if(bender.hasPerk(BendingPerk.FIRE_FIREWALL_TICK)) {
			this.damageInterval -= 50;
		}
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, POWER)) {
			return false;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			origin = EntityTools.getTargetedLocation(player, RANGE);
			Block block = origin.getBlock();
			if(block.isLiquid() || BlockTools.isSolid(block)) {
				return false;
			}
			
			time = this.startedTime;

			Vector direction = this.player.getEyeLocation().getDirection();
			Vector compare = direction.clone();
			compare.setY(0);

			if (Math.abs(direction.angle(compare)) > Math.toRadians(MAX_ANGLE)) {
				return false;
			}

			initializeBlocks();
			bender.fire.consume(NAME, POWER);
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public void progress() {
		this.time = System.currentTimeMillis();

		if ((this.time - this.startedTime) > this.duration) {
			remove();
			return;
		}

		if ((this.time - this.startedTime) > (this.intervaltick * DISPLAY_INTERVAL)) {
			this.intervaltick++;
			display();
		}

		if ((this.time - this.startedTime) > (this.damagetick * damageInterval)) {
			this.damagetick++;
			damage();
		}
	}
	
	private void initializeBlocks() {
		Vector direction = player.getEyeLocation().getDirection().setY(0).normalize();

		// A vector orthogonal to (a, b, c) is (-b, a, 0), or (-c, 0, a) or (0, -c, b)
		Vector orth = new Vector(-direction.getZ(), 0, direction.getX());
		orth = orth.normalize();

		Vector orth2 = new Vector(0,1,0);
		
		int i = 0;
		boolean expandLeft = true;
		while(-WIDTH <= i && i <= WIDTH) {
			int j = 0;
			boolean expandUp = true;
			boolean carryOn = false;
			while(-HEIGHT <= j && j <= HEIGHT) {
				Location location = origin.clone().add(orth.clone().multiply(i));
				location = location.add(orth2.clone().multiply(j));
				if (!ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
					Block block = location.getBlock();
					if(block.getType() != Material.AIR) {
						if(expandUp) {
							expandUp = false;
							j = 0;
							continue;
						} else {
							break;
						}
					}
					carryOn = true;
					if (!blocks.contains(block)) {
						blocks.add(block);
					}
				}
				if(expandUp) {
					j++;
					if(j > HEIGHT) {
						expandUp = false;
						j = 0;
					}
				} else {
					j--;
				}
			}
			if(!carryOn) {
				if(expandLeft) {
					expandLeft = false;
					i = 0;
					continue;
				} else {
					break;
				}
			}
			if(expandLeft) {
				i--;
				if(i < -WIDTH) {
					expandLeft = false;
					i = 0;
				}
			} else {
				i++;
			}
		}
	}

	private void display() {
		for (Block block : this.blocks) {
			Location location = block.getLocation();
			location.getWorld().spawnParticle(Particle.FLAME, location, 7, 0.25, 0.25, 0.25, PARTICLE_SPEED, null, true);
		}
	}

	private void damage() {
		double radius = HEIGHT;
		if (radius < WIDTH) {
			radius = WIDTH;
		}
		radius = radius + 1;
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(this.origin, radius);
		if (entities.contains(this.player)) {
			entities.remove(this.player);
		}

		for (LivingEntity entity : entities) {
			if (ProtectionManager.isEntityProtected(entity)) {
				continue;
			}
			if (ProtectionManager.isLocationProtectedFromBending(this.player, register, entity.getLocation())) {
				continue;
			}
			for (Block block : this.blocks) {
				if (entity.getLocation().distance(block.getLocation()) <= 1.5) {
					affect(entity);
					break;
				}
			}
		}
	}

	private void affect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		entity.setVelocity(new Vector(0, 0, 0));
		DamageTools.damageEntity(bender, entity, this, this.damage, false, 1, 0.2f, true);
		Enflamed.enflame(this.player, entity, flameTime, this);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}
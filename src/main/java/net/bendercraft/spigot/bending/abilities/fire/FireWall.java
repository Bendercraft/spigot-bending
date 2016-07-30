package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = FireWall.NAME, element = BendingElement.FIRE, shift=false)
public class FireWall extends BendingActiveAbility {
	public final static String NAME = "FireWall";

	private static double maxangle = 50;
	private static long interval = 250;

	@ConfigurationParameter("Range")
	private static int RANGE = 4;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 7500;

	@ConfigurationParameter("Interval")
	private static long DAMAGE_INTERVAL = 500;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 4;

	@ConfigurationParameter("Width")
	private static int WIDTH = 4;

	@ConfigurationParameter("Duration")
	private static long DURATION = 5000;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 9;

	private Location origin;
	private long time;
	private int damagetick = 0, intervaltick = 0;
	private List<Block> blocks = new LinkedList<Block>();

	private int damage;
	private long duration;

	public FireWall(RegisteredAbility register, Player player) {
		super(register, player);
		this.duration = DURATION;
		this.damage = DAMAGE;
		
		if (this.bender.hasPath(BendingPath.NURTURE)) {
			this.damage *= 0.8;
		}
		if (this.bender.hasPath(BendingPath.LIFELESS)) {
			this.damage *= 1.1;
		}
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

			if (Math.abs(direction.angle(compare)) > Math.toRadians(maxangle)) {
				return false;
			}

			initializeBlocks();
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

		if ((this.time - this.startedTime) > (this.intervaltick * interval)) {
			this.intervaltick++;
			display();
		}

		if ((this.time - this.startedTime) > (this.damagetick * DAMAGE_INTERVAL)) {
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
		
		double i = 0;
		boolean expandLeft = true;
		while(-WIDTH <= i && i <= WIDTH) {
			double j = 0;
			boolean expandUp = true;
			boolean carryOn = false;
			while(-HEIGHT <= j && j <= HEIGHT) {
				Location location = origin.clone().add(orth.clone().multiply(i));
				location = location.add(orth2.clone().multiply(j));
				if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
					continue;
				}
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
				if(expandUp) {
					j = j + 0.8;
					if(j > HEIGHT) {
						expandUp = false;
						j = 0;
					}
				} else {
					j = j - 0.8;
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
				i = i - 0.8;
				if(i < -WIDTH) {
					expandLeft = false;
					i = 0;
				}
			} else {
				i = i + 0.8;
			}
		}
	}

	private void display() {
		for (Block block : this.blocks) {
			block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 15);
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
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}
		entity.setVelocity(new Vector(0, 0, 0));
		DamageTools.damageEntity(bender, entity, this, this.damage, false, 1, 0.2f, true);
		Enflamed.enflame(this.player, entity, 1);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}
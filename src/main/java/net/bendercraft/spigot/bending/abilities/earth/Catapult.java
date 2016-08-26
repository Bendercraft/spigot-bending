package net.bendercraft.spigot.bending.abilities.earth;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Catapult.NAME, element = BendingElement.EARTH)
public class Catapult extends BendingActiveAbility {
	public final static String NAME = "Catapult";
	
	@ConfigurationParameter("Length")
	private static int length = 6;

	@ConfigurationParameter("Speed")
	private static double speed = 10.0;

	@ConfigurationParameter("Push")
	private static double push = 4.0;

	@ConfigurationParameter("Catapult")
	public static long COOLDOWN = 5000;

	private long interval = (long) (1000. / speed);

	private Location origin;
	private Location location;
	private Vector direction;
	private int distance;
	private boolean catapult = false;
	private boolean moving = false;
	private boolean flying = false;
	private long time;
	private long starttime;
	private int ticks = 0;

	public Catapult(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if (getState() != BendingAbilityState.START) {
			return false;
		}
		setState(BendingAbilityState.PROGRESSING);
		origin = player.getEyeLocation().clone();
		direction = origin.getDirection().clone().normalize();
		Vector neg = direction.clone().multiply(-1);

		Block block;
		distance = 0;
		for (int i = 0; i <= length; i++) {
			location = origin.clone().add(neg.clone().multiply((double) i));
			block = location.getBlock();
			if (BlockTools.isEarthbendable(player, register, block)) {
				distance = BlockTools.getEarthbendableBlocksLength(player, block, neg, length - i);
				break;
			} else if (!BlockTools.isTransparentToEarthbending(player, block)) {
				break;
			}
		}

		if (distance != 0) {
			if ((double) distance >= location.distance(origin)) {
				catapult = true;
			}
			time = System.currentTimeMillis() - interval;
			starttime = System.currentTimeMillis();
			moving = true;
			
			bender.cooldown(NAME, COOLDOWN);
		}

		return false;
	}

	public void progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (moving)
				if (!moveEarth()) {
					moving = false;
				}
		}

		if (flying) {
			if(!fly()) {
				remove();
				return;
			}
		}

		if (!flying && !moving && System.currentTimeMillis() > starttime + 1000) {
			remove();
		}
	}

	private boolean fly() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		// Tools.verbose(player.getLocation().distance(location));
		if (player.getWorld() != location.getWorld()) {
			return false;
		}

		if (player.getLocation().distance(location) < 3) {
			if (!moving && System.currentTimeMillis() > starttime + 1000)
				flying = false;
			return true;
		}

		for (Block block : BlockTools.getBlocksAroundPoint(player.getLocation(), 1.5)) {
			if ((BlockTools.isSolid(block) || block.isLiquid())) {
				flying = false;
				return true;
			}
		}
		Vector vector = direction.clone().multiply(push * distance / length);
		vector.setY(player.getVelocity().getY());
		player.setVelocity(vector);
		return true;
	}

	private boolean moveEarth() {
		if (ticks > distance) {
			return false;
		} else {
			ticks++;
		}

		// Tools.moveEarth(player, location, direction, distance, false);
		location = location.clone().add(direction);

		if (catapult) {
			if (location.distance(origin) < .5) {
				boolean remove = false;
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(origin, 2)) {
					affect(entity);
				}
				return remove;
			}
		} else {
			if (location.distance(origin) <= length - distance) {
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2)) {
					affect(entity);
				}
				return false;
			}
		}
		BlockTools.moveEarth(player, location.clone().subtract(direction), direction, distance, false);
		return true;
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
		boolean remove = true;
		if(catapult) {
			remove = entity == player;
		}
		entity.setVelocity(direction.clone().multiply(push * distance / length));
		return remove;
	}
}

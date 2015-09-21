package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Catapult", element=BendingType.Earth)
public class Catapult extends ActiveAbility {
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

	public Catapult(Player player) {
		super(player, null);
	}
	
	@Override
	public boolean swing() {
		if(state != AbilityState.CanStart) {
			return false;
		}
		state = AbilityState.Progressing;
		origin = player.getEyeLocation().clone();
		direction = origin.getDirection().clone().normalize();
		Vector neg = direction.clone().multiply(-1);

		Block block;
		distance = 0;
		for (int i = 0; i <= length; i++) {
			location = origin.clone().add(neg.clone().multiply((double) i));
			block = location.getBlock();
			if (BlockTools.isEarthbendable(player, Abilities.Catapult, block)) {
				distance = BlockTools.getEarthbendableBlocksLength(player, block,
						neg, length - i);
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
			AbilityManager.getManager().addInstance(this);
			bender.cooldown(Abilities.Catapult, COOLDOWN);
		}
		
		return false;
	}

	public boolean progress() {
		if(!super.progress()) {
			return false;
		}
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (moving)
				if (!moveEarth()) {
					moving = false;
				}
		}

		if (flying)
			return fly();

		if (!flying && !moving && System.currentTimeMillis() > starttime + 1000)
			return false;
		return true;
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
					if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
						continue;
					}
					if (entity instanceof Player) {
						Player target = (Player) entity;
						boolean equal = target.getEntityId() == player.getEntityId();
						if (equal) {
							remove = true;
						}
					}
					entity.setVelocity(direction.clone().multiply(
							push * distance / length));
				}
				return remove;
			}
		} else {
			if (location.distance(origin) <= length - distance) {
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 2)) {
					entity.setVelocity(direction.clone().multiply(
							push * distance / length));
				}
				return false;
			}
		}
		BlockTools.moveEarth(player, location.clone().subtract(direction),
				direction, distance, false);
		return true;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.Catapult;
	}
}

package net.avatar.realms.spigot.bending.abilities.air;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.BlockTools;

@ABendingAbility(name = "Air Scooter", bind = BendingAbilities.AirScooter, element = BendingElement.Air)
public class AirScooter extends BendingActiveAbility {

	@ConfigurationParameter("Speed")
	private static double SPEED = 0.675;

	private static final long INTERVAL = 100;
	private static final double SCOOTER_RADIUS = 1;

	private Block floorblock;
	private long time;
	private List<Double> angles = new LinkedList<Double>();

	private double speed;

	public AirScooter(Player player) {
		super(player);

		this.time = this.startedTime;
		for (int i = 0; i < 5; i++) {
			this.angles.add((double) (60 * i));
		}

		this.speed = SPEED;

		if (this.bender.hasPath(BendingPath.Mobile)) {
			this.speed *= 1.2;
		}
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.Start) {
			FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
			this.player.setAllowFlight(true);
			this.player.setFlying(true);
			this.player.setSprinting(false);

			setState(BendingAbilityState.Progressing);

			return false;
		}

		if (getState() == BendingAbilityState.Progressing) {
			remove();
			return false;
		}

		return true;
	}

	@Override
	public boolean sneak() {
		remove();
		return false;
	}

	@Override
	public void stop() {
		FlyingPlayer.removeFlyingPlayer(this.player, this);
	}

	@Override
	public boolean canTick() {
		if(!super.canTick() 
				|| this.bender.getAbility() != AbilityManager.getManager().getAbilityType(this)
				// TODO: Remove Scooter when using another ability instead of this
				|| this.player.isSneaking() 
				|| !this.player.isFlying()) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		getFloor();
		if (this.floorblock == null) {
			remove();
			return;
		}

		Vector velocity = this.player.getEyeLocation().getDirection().clone();
		velocity.setY(0);
		velocity = velocity.clone().normalize().multiply(this.speed);
		if (System.currentTimeMillis() > (this.time + INTERVAL)) {
			this.time = System.currentTimeMillis();
			if (this.player.getVelocity().length() < (this.speed * .5)) {
				remove();
				return;
			}
			spinScooter();
		}
		double distance = this.player.getLocation().getY() - this.floorblock.getY();
		double dx = Math.abs(distance - 2.4);
		if (distance > 2.75) {
			velocity.setY(-.25 * dx * dx);
		} else if (distance < 2) {
			velocity.setY(.25 * dx * dx);
		} else {
			velocity.setY(0);
		}
		Location loc = this.player.getLocation();
		loc.setY(this.floorblock.getY() + 1.5);
		this.player.setSprinting(false);
		this.player.removePotionEffect(PotionEffectType.SPEED);
		this.player.setVelocity(velocity);
	}

	private void spinScooter() {
		Location origin = this.player.getLocation().clone();
		origin.add(0, -SCOOTER_RADIUS, 0);
		for (int i = 0; i < 5; i++) {
			double x = Math.cos(Math.toRadians(this.angles.get(i))) * SCOOTER_RADIUS;
			double y = ((((double) i) / 2) * SCOOTER_RADIUS) - SCOOTER_RADIUS;
			double z = Math.sin(Math.toRadians(this.angles.get(i))) * SCOOTER_RADIUS;
			this.player.getWorld().playEffect(origin.clone().add(x, y, z), Effect.SMOKE, 4, (int) AirBlast.DEFAULT_RANGE);
		}
		for (int i = 0; i < 5; i++) {
			this.angles.set(i, this.angles.get(i) + 10);
		}
	}

	private void getFloor() {
		this.floorblock = null;
		for (int i = 0; i <= 7; i++) {
			Block block = this.player.getEyeLocation().getBlock().getRelative(BlockFace.DOWN, i);
			if (BlockTools.isSolid(block) || block.isLiquid()) {
				this.floorblock = block;
				return;
			}
		}
	}

	public static List<Player> getPlayers() {
		List<Player> players = new LinkedList<Player>();

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AirScooter);
		if ((instances == null) || instances.isEmpty()) {
			return players;
		}

		for (Object player : instances.keySet()) {
			players.add((Player) player);
		}
		return players;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	protected long getMaxMillis() {
		return 1000L * 60 * 10;
	}

	public static boolean isOnScooter(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AirScooter);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		return instances.containsKey(player);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (isOnScooter(this.player)) {
			return false;
		}

		if (!this.player.isSprinting() || BlockTools.isSolid(this.player.getEyeLocation().getBlock()) || this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		if (BlockTools.isSolid(this.player.getLocation().add(0, -0.5, 0).getBlock())) {
			return false;
		}

		return true;
	}

}

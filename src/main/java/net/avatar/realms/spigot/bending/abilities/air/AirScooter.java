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
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = AirScooter.NAME, element = BendingElement.Air, shift=false)
public class AirScooter extends BendingActiveAbility {
	public final static String NAME = "AirScooter";

	@ConfigurationParameter("Speed")
	private static double SPEED = 0.675;

	private static final long INTERVAL = 100;
	private static final double SCOOTER_RADIUS = 1;

	private Block floorBlock;
	private long time;
	private List<Double> angles = new LinkedList<Double>();

	private double speed;

	public AirScooter(RegisteredAbility register, Player player) {
		super(register, player);

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
				|| this.player.isSneaking() 
				|| !this.player.isFlying()) {
			return false;
		}

		for (String ability : this.bender.getAbilities().values()) {
			if (!ability.equals(AirScooter.NAME) && AbilityManager.getManager().isUsingAbility(this.player, ability)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void progress() {
		getFloor();
		if (this.floorBlock == null) {
			remove();
			return;
		}
		
		if(EntityTools.isTool(player.getItemInHand().getType())) {
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
		double distance = this.player.getLocation().getY() - this.floorBlock.getY();
		double dx = Math.abs(distance - 2.4);
		if (distance > 2.75) {
			velocity.setY(-.25 * dx * dx);
		} else if (distance < 2) {
			velocity.setY(.25 * dx * dx);
		} else {
			velocity.setY(0);
		}
		Location loc = this.player.getLocation();
		loc.setY(this.floorBlock.getY() + 1.5);
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
		this.floorBlock = null;
		for (int i = 0; i <= 7; i++) {
			Block block = this.player.getEyeLocation().getBlock().getRelative(BlockFace.DOWN, i);
			if (BlockTools.isSolid(block) || block.isLiquid()) {
				this.floorBlock = block;
				return;
			}
		}
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
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(AirScooter.NAME);
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

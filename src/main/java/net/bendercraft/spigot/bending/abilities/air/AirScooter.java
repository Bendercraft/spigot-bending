package net.bendercraft.spigot.bending.abilities.air;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.doubles.DoubleList;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.controller.FlyingPlayer;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = AirScooter.NAME, element = BendingElement.AIR, shift=false)
public class AirScooter extends BendingActiveAbility {
	public final static String NAME = "AirScooter";

	private static final int PARTICLE_AMOUNT = 5;

	@ConfigurationParameter("Speed")
	private static double SPEED = 0.675;

	private static final long INTERVAL = 100;
	private static final double SCOOTER_RADIUS = 1;

	private Block      floorBlock;
	private long       time;
	private DoubleList angles = new DoubleArrayList(PARTICLE_AMOUNT);

	private double speed;

	public AirScooter(RegisteredAbility register, Player player) {
		super(register, player);

		this.time = this.startedTime;
		for (int i = 0; i < PARTICLE_AMOUNT; i++) {
			this.angles.add((double) (60 * i));
		}

		this.speed = SPEED;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis(), true);
			this.player.setSprinting(false);

			setState(BendingAbilityState.PROGRESSING);

			return false;
		}

		if (getState() == BendingAbilityState.PROGRESSING) {
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
		
		if(EntityTools.holdsTool(player)) {
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

	private static final double RED_COMPONENT = 200/255.0;
	private static final double GREEN_COMPONENT = 250/255.0;
	private static final double BLUE_COMPONENT = 250/255.0;
	private void spinScooter() {
		Location origin = this.player.getLocation().clone();
		origin.add(0, -SCOOTER_RADIUS, 0);

		final World world = origin.getWorld();
		for (int i = 0; i < PARTICLE_AMOUNT; i++) {
			double angle = this.angles.getDouble(i);
			double x = Math.cos(Math.toRadians(angle)) * SCOOTER_RADIUS;
			double y = ((((double) i) / 2) * SCOOTER_RADIUS) - SCOOTER_RADIUS;
			double z = Math.sin(Math.toRadians(angle)) * SCOOTER_RADIUS;
			world.spawnParticle(Particle.SPELL_MOB, origin.clone().add(x, y, z), 0, RED_COMPONENT, GREEN_COMPONENT, BLUE_COMPONENT, 1, null, false);
			this.angles.set(i, angle + 10.0);
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

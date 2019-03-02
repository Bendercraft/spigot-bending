package net.bendercraft.spigot.bending.abilities.air;

import java.util.Map;

import org.bukkit.*;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2IntMaps;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = AirShield.NAME, element = BendingElement.AIR)
public class AirShield extends BendingActiveAbility {
	public final static String NAME = "AirShield";

	@ConfigurationParameter("Max-Radius")
	private static double MAX_RADIUS = 5.0;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 3000;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 300000;

	private int        numberOfStreams = (int) (1.20 * MAX_RADIUS);
	private double     radius          = 2;
	private double     maxRadius       = MAX_RADIUS;
	private double     speedfactor;
	private Int2IntMap angles          = new Int2IntOpenHashMap();
	
	private long cooldown;
	private long duration;

	public AirShield(RegisteredAbility register, Player player) {
		super(register, player);

		this.cooldown = COOLDOWN;
		this.duration = MAX_DURATION;
		if(bender.hasPerk(BendingPerk.AIR_MOBILITY)) {
			this.cooldown *= 3;
			this.duration = 6000;
		}
		
		int angle = 0;
		int di = (int) ((maxRadius * 2) / numberOfStreams);
		for (int i = -(int) maxRadius + di; i < (int) maxRadius; i += di) {
			this.angles.put(i, angle);
			angle += 90;
			if (angle == 360) {
				angle = 0;
			}
		}
		this.speedfactor = 1;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (isShielded(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean swing() {
		if (AvatarState.isAvatarState(this.player)) {
			if (getState() == BendingAbilityState.START) {
				setState(BendingAbilityState.PROGRESSING);
			}
			else if (getState() == BendingAbilityState.PROGRESSING) {
				remove();
			}
		}

		return false;
	}

	@Override
	public boolean sneak() {
		if (isState(BendingAbilityState.START)) {
			setState(BendingAbilityState.PROGRESSING);
			return false;
		}

		if (isState(BendingAbilityState.PROGRESSING)) {
			return false;
		}

		return true;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if(this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		if(!NAME.equals(EntityTools.getBendingAbility(player))) {
			return false;
		}
		if(!this.player.isSneaking() && !bender.hasPerk(BendingPerk.AIR_MOBILITY)) {
			return false;
		}
		return true;
	}

	private static final double RED_COMPONENT = 200/255.0;
	private static final double GREEN_COMPONENT = 250/255.0;
	private static final double BLUE_COMPONENT = 250/255.0;

	@Override
	public void progress() {
		Location origin = this.player.getLocation();

		FireBlast.removeFireBlastsAroundPoint(origin, this.radius);

		for (Entity entity : EntityTools.getEntitiesAroundPoint(origin, this.radius)) {
			affect(entity);
		}

		World world = origin.getWorld();
		for (Int2IntMap.Entry entry : Int2IntMaps.fastIterable(this.angles)) {
			final int i = entry.getIntKey();
			double x, y, z;
			double angle = entry.getIntValue();
			angle = Math.toRadians(angle);

			double factor = this.radius / maxRadius;

			y = origin.getY() + (factor * i);

			double f = Math.sqrt(1 - (factor * factor * (i / this.radius) * (i / this.radius)));

			x = origin.getX() + (this.radius * Math.cos(angle) * f);
			z = origin.getZ() + (this.radius * Math.sin(angle) * f);

			Location effect = new Location(world, x, y, z);
			if (!ProtectionManager.isLocationProtectedFromBending(this.player, register, effect)) {
				world.spawnParticle(Particle.SPELL_MOB, effect, 0, RED_COMPONENT, GREEN_COMPONENT, BLUE_COMPONENT, 1, null, false);
			}

			this.angles.put(i, entry.getIntValue() + (int) (10 * this.speedfactor));
		}

		if (this.radius < maxRadius) {
			this.radius += .3;
		}

		if (this.radius > maxRadius) {
			this.radius = maxRadius;
		}
	}

	@Override
	public void stop() {
		this.bender.cooldown(NAME, cooldown);
	}


	@Override
	protected long getMaxMillis() {
		return duration;
	}
	
	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		
		if (entity instanceof ExperienceOrb
				|| entity instanceof FallingBlock
				|| entity instanceof ItemFrame
				|| entity instanceof Item) {
			return;
		}
		Location origin = this.player.getLocation();
		if (entity instanceof Player) {
			entity.setFireTicks(0);
		}

		if (origin.distance(entity.getLocation()) > 2) {
			double x, z, vx, vz, mag;
			double angle = 50;
			angle = Math.toRadians(angle);

			x = entity.getLocation().getX() - origin.getX();
			z = entity.getLocation().getZ() - origin.getZ();

			mag = Math.sqrt((x * x) + (z * z));

			vx = ((x * Math.cos(angle)) - (z * Math.sin(angle))) / mag;
			vz = ((x * Math.sin(angle)) + (z * Math.cos(angle))) / mag;

			Vector velocity = entity.getVelocity();
			if (AvatarState.isAvatarState(this.player)) {
				velocity.setX(AvatarState.getValue(vx));
				velocity.setZ(AvatarState.getValue(vz));
			} else {
				velocity.setX(vx);
				velocity.setZ(vz);
			}

			velocity.multiply(this.radius / maxRadius);

			entity.setVelocity(velocity);
			entity.setFallDistance(0);
		}
	}

	public static boolean isShielded(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		return instances.containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

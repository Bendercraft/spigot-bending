package net.bendercraft.spigot.bending.abilities.air;

import org.bukkit.*;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2IntMaps;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.controller.FlyingPlayer;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = Tornado.NAME, affinity = BendingAffinity.TORNADO, canBeUsedWithTools = true)
public class Tornado extends BendingActiveAbility {
	public final static String NAME = "Tornado";

	@ConfigurationParameter("Fall-Imunity")
	private static long FALL_IMMUNITY = 5000;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;

	@ConfigurationParameter("Radius")
	private static double RADIUS = 20;

	@ConfigurationParameter("Height")
	private static double HEIGHT = 40;

	@ConfigurationParameter("Range")
	private static double RANGE = 25;

	@ConfigurationParameter("Mob-Push-Factor")
	private static double NPC_PUSH = 3.0;

	@ConfigurationParameter("Player-Push-Factor")
	private static double PC_PUSH = 3.0;

	private static int numberOfStreams = (int) (0.375 * HEIGHT);

	private static double speedfactor = 1;

	private double height = 2;
	private double radius = (this.height / HEIGHT) * RADIUS;

	private Int2IntMap angles = new Int2IntOpenHashMap();
	private Location   origin;
	private Location   base;

	private FlyingPlayer flying;

	public Tornado(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			this.origin = EntityTools.getTargetBlock(player, RANGE).getLocation();
			base = origin.clone();
			this.origin.setY(this.origin.getY() - ((1. / 10.) * this.height));

			int angle = 0;
			for (int i = 0; i <= HEIGHT; i += (int) HEIGHT / numberOfStreams) {
				this.angles.put(i, angle);
				angle += 90;
				if (angle == 360) {
					angle = 0;
				}
			}
			
			this.flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis(), false);
			if (this.flying != null) {
				setState(BendingAbilityState.PROGRESSING);
			}
		}
		return false;
	}

	@Override
	public void stop() {
		FlyingPlayer.removeFlyingPlayer(this.player, this);
		bender.cooldown(this, COOLDOWN);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isLocationProtectedFromBending(this.player, register, this.origin)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		origin = base.clone();

		double timefactor = this.height / HEIGHT;
		this.radius = timefactor * RADIUS;

		if (this.origin.getBlock().getType() != Material.AIR) {
			this.origin.setY(this.origin.getY() - ((1. / 10.) * this.height));

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.origin, this.height)) {
				affect(entity, timefactor);
			}

			World world = this.origin.getWorld();
			Int2IntMap toAdd = new Int2IntOpenHashMap();
			for (Int2IntMap.Entry entry : Int2IntMaps.fastIterable(this.angles)) {
				int i = entry.getIntKey();
				double x, y, z;
				double angle = entry.getIntValue();
				angle = Math.toRadians(angle);
				double factor;

				y = this.origin.getY() + (timefactor * i);
				factor = i / this.height;

				x = this.origin.getX() + (timefactor * factor * this.radius * Math.cos(angle));
				z = this.origin.getZ() + (timefactor * factor * this.radius * Math.sin(angle));

				Location effectLocation = new Location(world, x, y, z);
				if (!ProtectionManager.isLocationProtectedFromBending(this.player, register, effectLocation)) {
					world.spawnParticle(Particle.CLOUD, effectLocation, 1, 0.25, 0.25, 0.25, 0, null, true);
				}

				toAdd.put(i, entry.getIntValue() + (25 * (int) speedfactor));
			}
			this.angles.putAll(toAdd);
		}

		if (this.height < HEIGHT) {
			this.height += 1;
		}

		if (this.height > HEIGHT) {
			this.height = HEIGHT;
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public long getMaxMillis() {
		return 60 * 1000;
	}
	
	private void affect(Entity entity, double timefactor) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}

		double y = entity.getLocation().getY();
		double factor;
		if ((y > this.origin.getY()) && (y < (this.origin.getY() + this.height))) {
			factor = (y - this.origin.getY()) / this.height;
			Location testloc = new Location(this.origin.getWorld(), this.origin.getX(), y, this.origin.getZ());
			if (testloc.distance(entity.getLocation()) < (this.radius * factor)) {
				double x, z, vx, vz, mag;
				double angle = 100;
				double vy = NPC_PUSH;
				angle = Math.toRadians(angle);

				x = entity.getLocation().getX() - this.origin.getX();
				z = entity.getLocation().getZ() - this.origin.getZ();

				mag = Math.sqrt((x * x) + (z * z));

				vx = ((x * Math.cos(angle)) - (z * Math.sin(angle))) / mag;
				vz = ((x * Math.sin(angle)) + (z * Math.cos(angle))) / mag;

				if (entity instanceof Player) {
					double dy = y - this.origin.getY();
					if (dy >= (this.height * .95)) {
						vy = 0;
					} else if (dy >= (this.height * .85)) {
						vy = 6.0 * (.95 - (dy / this.height));
					} else {
						vy = PC_PUSH;
					}
				}

				if (entity.getEntityId() == this.player.getEntityId()) {
					Vector direction = this.player.getEyeLocation().getDirection().clone().normalize();
					vx = direction.getX();
					vz = direction.getZ();
					Location playerloc = this.player.getLocation();
					double py = playerloc.getY();
					double oy = this.origin.getY();
					double dy = py - oy;
					if (dy >= (this.height * .95)) {
						vy = 0;
					} else if (dy >= (this.height * .85)) {
						vy = 6.0 * (.95 - (dy / this.height));
					} else {
						vy = .6;
					}
				} else {
					((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 10, 1));
				}

				Vector velocity = entity.getVelocity();
				velocity.setX(vx);
				velocity.setZ(vz);
				velocity.setY(vy);
				velocity.multiply(timefactor * 0.75);
				entity.setVelocity(velocity);
				entity.setFallDistance((float) (entity.getFallDistance() / 3.0));
			}
		}
		EntityTools.fallImmunity.put(entity.getUniqueId(), System.currentTimeMillis() + FALL_IMMUNITY);
	}

}

package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.MathUtils;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@ABendingAbility(name = Lightning.NAME, affinity = BendingAffinity.LIGHTNING)
public class Lightning extends BendingActiveAbility {
	public final static String NAME = "Lightning";
	
	private static Map<Entity, Lightning> strikes = new HashMap<Entity, Lightning>();

	@ConfigurationParameter("Range")
	public static int RANGE = 50;

	@ConfigurationParameter("Charge-Time")
	private static long WARMUP = 4000;

	@ConfigurationParameter("Miss-Chance")
	private static double MISS_CHANCE = 5.0;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 10;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 0;

	private static double threshold = 0.1;
	private static double blockdistance = 4;

	private int damage = DAMAGE;
	private double strikeradius = 4;

	private long warmup;
	private LightningStrike strike = null;
	private List<Entity> hitentities = new LinkedList<Entity>();

	public Lightning(RegisteredAbility register, Player player) {
		super(register, player);

		this.warmup = WARMUP;
		if (AvatarState.isAvatarState(this.player)) {
			this.warmup *= 0.5;
		} else if (Tools.isDay(this.player.getWorld())) {
			this.warmup /= Settings.NIGHT_FACTOR;
		}
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}

	public static Lightning getLightning(Entity entity) {
		return strikes.get(entity);
	}

	private void strike() {
		Location targetlocation = getTargetLocation();

		if (!ProtectionManager.isRegionProtectedFromBending(this.player, NAME, targetlocation)) {
			this.strike = this.player.getWorld().strikeLightning(targetlocation);
			strikes.put(this.strike, this);
		}
	}

	private Location getTargetLocation() {
		int distance = (int) PluginTools.firebendingDayAugment(RANGE, this.player.getWorld());

		Location targetLocation;
		targetLocation = EntityTools.getTargetedLocation(this.player, distance);
		LivingEntity target = EntityTools.getTargetedEntity(this.player, distance);
		if (target != null) {
			if ((this.player.getLocation().distance(targetLocation) > target.getLocation().distance(this.player.getLocation()))) {
				// Check redirection
				if (target instanceof Player) {
					BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) target);
					if ((bPlayer != null) && (bPlayer.getAbility() != null) && bPlayer.getAbility().equals(NAME)) {
						// Redirection !
						targetLocation = EntityTools.getTargetedLocation((Player) target, distance);
					} else {
						targetLocation = target.getLocation();
						if (target.getVelocity().length() < threshold) {
							MISS_CHANCE = 0;
						}
					}
				} else {
					targetLocation = target.getLocation();
					if (target.getVelocity().length() < threshold) {
						MISS_CHANCE = 0;
					}
				}

			}
		} else {
			MISS_CHANCE = 0;
		}

		if (targetLocation.getBlock().getType() == Material.AIR) {
			targetLocation.add(0, -1, 0);
		}
		if (targetLocation.getBlock().getType() == Material.AIR) {
			targetLocation.add(0, -1, 0);
		}

		if (!MathUtils.doubleEquals(MISS_CHANCE, 0) && !AvatarState.isAvatarState(this.player)) {
			double A = Math.random() * Math.PI * MISS_CHANCE * MISS_CHANCE;
			double theta = Math.random() * Math.PI * 2;
			double r = Math.sqrt(A) / Math.PI;
			double x = r * Math.cos(theta);
			double z = r * Math.sin(theta);

			targetLocation = targetLocation.add(x, 0, z);
		}

		return targetLocation;
	}

	@Override
	public void stop() {
		this.bender.cooldown(NAME, COOLDOWN);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!EntityTools.getBendingAbility(this.player).equals(NAME)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		int distance = (int) PluginTools.firebendingDayAugment(RANGE, this.player.getWorld());

		if (System.currentTimeMillis() > (this.startedTime + this.warmup) && getState() == BendingAbilityState.PREPARING) {
			setState(BendingAbilityState.PREPARED);
		}

		if (getState().equals(BendingAbilityState.PREPARED)) {
			if (this.player.isSneaking()) {
				this.player.getWorld().playEffect(this.player.getEyeLocation(), Effect.SMOKE, Tools.getIntCardinalDirection(this.player.getEyeLocation().getDirection()), distance);
			} else {
				strike();
				remove();
			}
		} else {
			if (!this.player.isSneaking()) {
				remove();
			}
		}
	}

	public void dealDamage(Entity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}
		if (this.strike == null) {
			return;
		}
		if (this.hitentities.contains(entity)) {
			return;
		}
		double distance = entity.getLocation().distance(this.strike.getLocation());
		if (distance > this.strikeradius) {
			return;
		}
		double dmg = this.damage - ((distance / this.strikeradius) * .5);
		this.hitentities.add(entity);
		EntityTools.damageEntity(this.player, entity, (int) dmg);
	}

	public static boolean isNearbyChannel(Location location) {
		boolean isNearby = false;
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);

		for (Object obj : instances.keySet()) {
			if (!instances.get(obj).getPlayer().getWorld().equals(location.getWorld())) {
				continue;
			}
			if (instances.get(obj).getPlayer().getLocation().distance(location) <= blockdistance) {
				isNearby = true;
				((Lightning) instances.get(obj)).startedTime = 0;
			}
		}
		return isNearby;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);

		if (instances == null) {
			return true;
		}
		return !instances.containsKey(this.player.getUniqueId());
	}
}

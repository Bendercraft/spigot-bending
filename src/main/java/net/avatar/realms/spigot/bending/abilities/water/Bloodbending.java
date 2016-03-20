package net.avatar.realms.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = Bloodbending.NAME, affinity = BendingAffinity.BLOOD)
public class Bloodbending extends BendingActiveAbility {
	public final static String NAME = "BloodBend";
	
	
	private Map<Entity, Location> targetEntities = new HashMap<Entity, Location>();
	

	@ConfigurationParameter("Throw-Factor")
	private static double FACTOR = 1.0;

	@ConfigurationParameter("Max-Duration")
	private static int MAX_DURATION = 10000;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 6000;

	@ConfigurationParameter("Range")
	public static int RANGE = 8;

	private int range;
	private Long time;

	public Bloodbending(RegisteredAbility register, Player player) {
		super(register, player);

		range = (int) PluginTools.waterbendingNightAugment(RANGE, player.getWorld());
	}

	@Override
	public boolean sneak() {
		if (AvatarState.isAvatarState(this.player)) {
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.player.getLocation(), this.range)) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (entity instanceof Player) {
					if (ProtectionManager.isLocationProtectedFromBending(this.player, register, entity.getLocation())
							|| AvatarState.isAvatarState((Player) entity) 
							|| (entity.getEntityId() == this.player.getEntityId()) 
							|| EntityTools.canBend((Player) entity, register)) {
						continue;
					}
				}
				EntityTools.damageEntity(bender, entity, 0);
				this.targetEntities.put(entity, entity.getLocation().clone());
			}
		} else {
			if (BendingPlayer.getBendingPlayer(this.player).isOnCooldown(NAME)) {
				return false;
			}
			LivingEntity target = EntityTools.getTargetedEntity(this.player, this.range);
			if (target == null
					|| ProtectionManager.isEntityProtected(target)
					|| ProtectionManager.isLocationProtectedFromBending(this.player, register, target.getLocation())) {
				return false;
			}
			if (target instanceof Player) {
				if (EntityTools.canBend((Player) target, register) || AvatarState.isAvatarState((Player) target) || target.isOp()) {
					return false;
				}

			}
			EntityTools.damageEntity(bender, target, 0);
			this.targetEntities.put(target, target.getLocation().clone());
		}
		this.time = System.currentTimeMillis();
		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public boolean swing() {
		Location location = this.player.getLocation();
		for (Entity entity : this.targetEntities.keySet()) {
			double dx, dy, dz;
			Location target = entity.getLocation().clone();
			dx = target.getX() - location.getX();
			dy = target.getY() - location.getY();
			dz = target.getZ() - location.getZ();
			Vector vector = new Vector(dx, dy, dz);
			vector.normalize();
			entity.setVelocity(vector.multiply(FACTOR));
		}
		remove();
		return false;
	}
	

	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!this.player.isSneaking() 
				|| !NAME.equals(EntityTools.getBendingAbility(player))
				|| !EntityTools.canBend(this.player, register)
				|| System.currentTimeMillis() - this.time > MAX_DURATION) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 60, 1);
		
		if (AvatarState.isAvatarState(this.player)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.player.getLocation(), this.range)) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (ProtectionManager.isLocationProtectedFromBending(this.player, register, entity.getLocation())) {
					continue;
				}
				if (entity instanceof Player) {
					if (!EntityTools.canBeBloodbent((Player) entity)) {
						continue;
					}
				}
				entities.add(entity);
				if (!this.targetEntities.containsKey(entity)) {
					EntityTools.damageEntity(bender, entity, 0);
					this.targetEntities.put(entity, entity.getLocation().clone());
				}
				Location newlocation = entity.getLocation().clone();
				Location location = this.targetEntities.get(entity);
				double distance = location.distance(newlocation);
				double dx, dy, dz;
				dx = location.getX() - newlocation.getX();
				dy = location.getY() - newlocation.getY();
				dz = location.getZ() - newlocation.getZ();
				Vector vector = new Vector(dx, dy, dz);
				if (distance > .5) {
					entity.setVelocity(vector.normalize().multiply(.5));
				} else {
					entity.setVelocity(new Vector(0, 0, 0));
				}
				entity.addPotionEffect(effect);
				entity.setFallDistance(0);
				if (entity instanceof Creature) {
					((Creature) entity).setTarget(null);
				}

			}
			List<Entity> toRemove = new LinkedList<Entity>();
			for (Entity entity : this.targetEntities.keySet()) {
				if (!entities.contains(entity)) {
					toRemove.add(entity);
				}
			}
			for (Entity entity : toRemove) {
				this.targetEntities.remove(entity);
			}
		} else {
			List<Entity> toRemove = new LinkedList<Entity>();
			for (Entry<Entity, Location> entry : this.targetEntities.entrySet()) {
				Entity entity = entry.getKey();
				if (entity instanceof Player) {
					if (!EntityTools.canBeBloodbent((Player) entity)) {
						toRemove.add(entity);
						continue;
					}
				}
				Location newlocation = entity.getLocation();
				Location location = EntityTools.getTargetedLocation(this.player, (int) entry.getValue().distance(this.player.getLocation()));
				double distance = location.distance(newlocation);
				double dx, dy, dz;
				dx = location.getX() - newlocation.getX();
				dy = location.getY() - newlocation.getY();
				dz = location.getZ() - newlocation.getZ();
				Vector vector = new Vector(dx, dy / 3, dz);
				if (distance > .5) {
					entity.setVelocity(vector.normalize().multiply(.5));
				} else {
					entity.setVelocity(new Vector(0, 0, 0));
				}
				((LivingEntity) entity).addPotionEffect(effect);
				entity.setFallDistance(0);
				if (entity instanceof Creature) {
					((Creature) entity).setTarget(null);
				}
			}
			for (Entity entity : toRemove) {
				this.targetEntities.remove(entity);
			}
		}
	}

	public static boolean isBloodbended(Entity entity) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			Bloodbending bloodBend = (Bloodbending) ab;
			if (bloodBend.getTargetEntities().containsKey(entity)) {
				return true;
			}
		}
		return false;
	}

	public static Location getBloodbendingLocation(Entity entity) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			Bloodbending bloodBend = (Bloodbending) ab;
			if (bloodBend.getTargetEntities().containsKey(entity)) {
				return bloodBend.getTargetEntities().get(entity);
			}
		}
		return null;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.holdsTool(player)) {
			return false;
		}

		return true;
	}

	public Map<Entity, Location> getTargetEntities() {
		return this.targetEntities;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		bender.cooldown(this, COOLDOWN);
	}
}

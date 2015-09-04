package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Tornado", element=BendingType.Air, specialization=BendingSpecializationType.Tornado)
public class Tornado extends ActiveAbility {
	private static Map<Integer, Tornado> instances = new HashMap<Integer, Tornado>();
	
	@ConfigurationParameter("Fall-Imunity")
	private static long FALL_IMMUNITY = 5000;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;
	
	@ConfigurationParameter("Radius")
	private static double RADIUS = 10;
	
	@ConfigurationParameter("Height")
	private static double HEIGHT = 25;
	
	@ConfigurationParameter("Range")
	private static double RANGE = 25;
	
	@ConfigurationParameter("Mob-Push-Factor")
	private static double NPC_PUSH = 1.0;
	
	@ConfigurationParameter("Player-Push-Factor")
	private static double PC_PUSH = 1.0;
	
	private static int numberOfStreams = (int) (.3 * HEIGHT);
	
	private static double speedfactor = 1;
	
	private double height = 2;
	private double radius = (this.height / HEIGHT) * RADIUS;
	
	private Map<Integer, Integer> angles = new HashMap<Integer, Integer>();
	private Location origin;

	private FlyingPlayer flying;
	
	public Tornado(Player player) {
		super(player, null);
		
		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}
		
		this.origin = EntityTools.getTargetBlock(player, RANGE).getLocation();
		this.origin.setY(this.origin.getY() - ((1. / 10.) * this.height));
		
		int angle = 0;
		for (int i = 0; i <= HEIGHT; i += (int) HEIGHT / numberOfStreams) {
			this.angles.put(i, angle);
			angle += 90;
			if (angle == 360) {
				angle = 0;
			}
		}
	}
	
	@Override
	public boolean sneak() {
		switch (this.state) {
			case None:
			case CannotStart:
				return true;
			case CanStart:
				this.flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
				if (this.flying != null) {
					setState(AbilityState.Progressing);
					AbilityManager.getManager().addInstance(this);
				}
				return false;

			case Preparing:
			case Prepared:
			case Progressing:
			case Ending:
			case Ended:
			case Removed:
			default:
				return false;
		}
	}
	
	@Override
	public void stop () {
		FlyingPlayer.removeFlyingPlayer(this.player, this);
	}
	
	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		
		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		
		if (!this.player.isSneaking()) {
			return false;
		}
		
		if ((EntityTools.getBendingAbility(this.player) != Abilities.Tornado)) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.AirBlast, this.origin)) {
			return false;
		}
		
		rotateTornado();
		return true;
	}
	
	private void rotateTornado() {
		this.origin = EntityTools.getTargetBlock(this.player, RANGE).getLocation();
		
		double timefactor = this.height / HEIGHT;
		this.radius = timefactor * RADIUS;
		
		if (this.origin.getBlock().getType() != Material.AIR) {
			this.origin.setY(this.origin.getY() - ((1. / 10.) * this.height));
			
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.origin, this.height)) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(this.player,
						Abilities.AirBlast, entity.getLocation())) {
					continue;
				}
				
				double y = entity.getLocation().getY();
				double factor;
				if ((y > this.origin.getY()) && (y < (this.origin.getY() + this.height))) {
					factor = (y - this.origin.getY()) / this.height;
					Location testloc = new Location(this.origin.getWorld(),
							this.origin.getX(), y, this.origin.getZ());
					if (testloc.distance(entity.getLocation()) < (this.radius
							* factor)) {
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
							Vector direction = this.player.getEyeLocation()
									.getDirection().clone().normalize();
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
						}
						else {
							entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20*10, 1));
						}
						
						Vector velocity = entity.getVelocity();
						velocity.setX(vx);
						velocity.setZ(vz);
						velocity.setY(vy);
						velocity.multiply(timefactor*0.75);
						entity.setVelocity(velocity);
						entity.setFallDistance((float) (entity.getFallDistance()/3.0));
						
						//						if (entity instanceof Player) {
						//							new Flight((Player) entity);
						//						}
					}
				}
				EntityTools.fallImmunity.put(entity.getUniqueId(), System.currentTimeMillis() + FALL_IMMUNITY);
			}
			
			
			Map<Integer, Integer> toAdd = new HashMap<Integer, Integer>();
			for (Entry<Integer, Integer> entry : this.angles.entrySet()) {
				int i = entry.getKey();
				double x, y, z;
				double angle = entry.getValue();
				angle = Math.toRadians(angle);
				double factor;
				
				y = this.origin.getY() + (timefactor * i);
				factor = i / this.height;
				
				x = this.origin.getX() + (timefactor * factor * this.radius
						* Math.cos(angle));
				z = this.origin.getZ() + (timefactor * factor * this.radius
						* Math.sin(angle));
				
				Location effect = new Location(this.origin.getWorld(), x, y, z);
				if (!ProtectionManager.isRegionProtectedFromBending(this.player,
						Abilities.AirBlast, effect)) {
					this.origin.getWorld().playEffect(effect, Effect.SMOKE, 4,
							(int) AirBlast.DEFAULT_RANGE);
				}
				
				toAdd.put(i, this.angles.get(i) + (25 * (int) speedfactor));
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
	
	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for (Tornado tornado : instances.values()) {
			players.add(tornado.player);
		}
		return players;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.Tornado;
	}
	
	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public long getMaxMillis () {
		return 60 * 1000;
	}
	
}

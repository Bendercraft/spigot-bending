package net.bendercraft.spigot.bending.abilities.energy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.fire.FireBurst;
import net.bendercraft.spigot.bending.abilities.fire.FireBurst.BurstBlast;
import net.bendercraft.spigot.bending.abilities.water.TorrentBurst;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.controller.FlyingPlayer;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = AvatarShield.NAME, element = BendingElement.ENERGY)
public class AvatarShield extends BendingActiveAbility {
	public final static String NAME = "AvatarShield";

	@ConfigurationParameter("Factor")
	public static double FACTOR = 4.5;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 300000;

	@ConfigurationParameter("Cooldown-Factor")
	private static int COOLDOWN_FACTOR = 4;
	
	@ConfigurationParameter("Throw-Factor")
	private static double DEFLECT_DAMAGE = 2;

	@ConfigurationParameter("Immunity-Hit")
	private static int IMMUNITY_HIT = 3;
	
	@ConfigurationParameter("Fire-Range")
	private static int FIRE_RANGE = 25;
	
	@ConfigurationParameter("Del-Phi")
	static double DELPHI = 10;
	
	@ConfigurationParameter("Del-Theta")
	static double DELTHETA = 10;
	
	@ConfigurationParameter("Damage-Fire")
	public static double DAMAGE_FIRE = 4.5;
	
	private long realDuration;
	static long interval = 40;
	private static double radius = 3;
	private long time;
	private int earthHit = 0;
	private List<BurstBlast> blasts = new LinkedList<BurstBlast>();
	private TorrentBurst burst;
	
	private Ring water; // launch a water torrent
	private Ring fire; // expand a fire burst
	private Ring air; // allow to fly
	private Ring earth; // offer protection against damage

	public AvatarShield(RegisteredAbility register, Player player) {
		super(register, player);
	}
	
	public boolean hit() {
		if(earth != null) {
			earthHit++;
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
			if(earthHit > IMMUNITY_HIT) {
				earth.clearRing();
				earth = null;
			}
		} else if(fire != null) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);
			fire.clearRing();
			fire = null;
		} else if(water != null) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
			water.clearRing();
			water = null;
		} else if(air != null) {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1.0f, 1.0f);
			air.clearRing();
			air = null;
		} else {
			return false;
		}
		return true;
	}
	
	private void fireBurst() {
		Location location = this.player.getEyeLocation();
		List<Block> safeblocks = BlockTools.getBlocksAroundPoint(this.player.getLocation(), 2);
		Vector vector = location.getDirection();
		double angle = Math.toRadians(30);
		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += DELTHETA) {
			double dphi = DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				if (direction.angle(vector) <= angle) {
					blasts.add(new FireBurst.BurstBlast(this.player, this.bender, this, location, direction.normalize(), FIRE_RANGE, DAMAGE_FIRE, safeblocks));
				}
			}
		}
		
		fire.clearRing();
		fire = null;
	}

	@Override
	public boolean swing() {
		if (isState(BendingAbilityState.START)) {
			setState(BendingAbilityState.PROGRESSING);
			//fire = new Ring(bender, new Vector(-1,1,1), Material.MAGMA);
			fire = new Ring(bender, new Vector(-1,1,1), Effect.MOBSPAWNER_FLAMES);
			//air = new Ring(bender, new Vector(1,-1,1), Material.GLASS);
			air = new Ring(bender, new Vector(1,-1,1), Effect.SMOKE);
			water = new Ring(bender, new Vector(1,0,-1), Material.WATER);
			earth = new Ring(bender, new Vector(1,1,-1), Material.STONE);
			FlyingPlayer.addFlyingPlayer(player, this, MAX_DURATION, true);
		} else if (isState(BendingAbilityState.PROGRESSING)) {
			if(player.isSneaking()) {
				if(fire != null) {
					// Burst !
					this.fireBurst();
				}
			} else {
				if(water != null) {
					// Torrent !
					burst = new TorrentBurst(this.player, radius, this);
					water.clearRing();
					water = null;
				}
			}
		}

		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!NAME.equals(EntityTools.getBendingAbility(player))) {
			return false;
		}
		if(!AvatarState.isAvatarState(getPlayer())) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if(burst != null) {
			if(!burst.progress()) {
				burst.remove();
				burst = null;
			}
		}
		if(!blasts.isEmpty()) {
			List<BurstBlast> toRemove = new LinkedList<BurstBlast>();
			for(BurstBlast blast : blasts) {
				if(!blast.progress()) {
					toRemove.add(blast);
				}
			}
			blasts.removeAll(toRemove);
		}
		if(burst == null 
				&& blasts.isEmpty() 
				&& fire == null 
				&& air == null 
				&& water == null 
				&& earth == null) {
			remove();
			return;
		}
		if (getState() == BendingAbilityState.PROGRESSING) {
			player.setFireTicks(0);
			if (System.currentTimeMillis() > (time + interval)) {
				time = System.currentTimeMillis();
				if(fire != null) {
					fire.formRing();
				}
				if(air != null) {
					air.formRing();
				}
				if(water != null) {
					water.formRing();
				}
				if(earth != null) {
					earth.formRing();
				}
			}
		}
	}

	@Override
	public void stop() {
		if(fire != null) {
			fire.clearRing();
		}
		if(air != null) {
			air.clearRing();
		}
		if(water != null) {
			water.clearRing();
		}
		if(earth != null) {
			earth.clearRing();
		}
		
		if(burst != null) {
			burst.remove();
		}
		blasts.clear();
		
		long now = System.currentTimeMillis();
		realDuration = now - startedTime;
		bender.cooldown(NAME, realDuration * COOLDOWN_FACTOR);
		FlyingPlayer.removeFlyingPlayer(player, this);
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	private static class Ring {
		private double startangle = 0;
		private List<TempBlock> blocks = new ArrayList<TempBlock>();
		private double angle = 180;
		protected BendingPlayer bender;
		private Vector axis;
		private Material material;
		private Effect effect;
		private double radius = 4;
		
		
		public Ring(BendingPlayer bender, Vector axis, Material material) {
			this(bender, axis, material, null);
		}
		
		public Ring(BendingPlayer bender, Vector axis, Effect effect) {
			this(bender, axis, null, effect);
		}
		
		private Ring(BendingPlayer bender, Vector axis, Material material, Effect effect) {
			this.bender = bender;
			this.axis = axis;
			this.material = material;
			this.effect = effect;
		}
		
		public void clearRing() {
			for (TempBlock block : blocks) {
				block.revertBlock();
			}
			blocks.clear();
		}
		
		private double compute(double phi, double axis) {
			if(axis < 0) {
				return Math.sin(phi) * (-axis);
			}
			return Math.cos(phi) * axis;
		}
		
		private void formRing() {
			clearRing();
			
			startangle += 30;
			Location loc = bender.getPlayer().getLocation();
			
			List<Block> doneBlocks = new ArrayList<Block>();
			List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(loc, radius + 2);
			List<Entity> affectedEntities = new ArrayList<Entity>();
			
			for (double theta = startangle; theta < (angle + startangle); theta += 20) {
				double phi = Math.toRadians(theta);
				
				double dx = compute(phi, axis.getX()) * radius;
				double dy = compute(phi, axis.getY()) * radius;
				double dz = compute(phi, axis.getZ()) * radius;
				Location blockloc = loc.clone().add(dx, dy, dz);
				Block block = blockloc.getBlock();
				if (!doneBlocks.contains(block)) {
					if (BlockTools.isTransparentToEarthbending(bender.getPlayer(), block) && !block.isLiquid()) {
						if(material != null) {
							blocks.add(TempBlock.makeTemporary(block, material, false));
							doneBlocks.add(block);
						} else {
							block.getLocation().getWorld().playEffect(block.getLocation(), effect, 0, 25);
						}
						
						for (LivingEntity entity : entities) {
							if (ProtectionManager.isEntityProtected(entity)) {
								continue;
							}
							if (entity.getWorld() != blockloc.getWorld()) {
								continue;
							}
							if (!affectedEntities.contains(entity) && (entity.getLocation().distance(blockloc) <= 1.5)) {
								deflect(entity);
							}
						}
					}
				}
			}
		}
		
		private void deflect(LivingEntity entity) {
			if (ProtectionManager.isEntityProtected(entity)) {
				return;
			}
			if (entity.getEntityId() == bender.getPlayer().getEntityId()) {
				return;
			}
			double x, z, vx, vz, mag;
			double angle = 50;
			angle = Math.toRadians(angle);

			x = entity.getLocation().getX() - bender.getPlayer().getLocation().getX();
			z = entity.getLocation().getZ() - bender.getPlayer().getLocation().getZ();

			mag = Math.sqrt((x * x) + (z * z));

			vx = ((x * Math.cos(angle)) - (z * Math.sin(angle))) / mag;
			vz = ((x * Math.sin(angle)) + (z * Math.cos(angle))) / mag;

			Vector vec = new Vector(vx, 0, vz).normalize().multiply(FACTOR);

			Vector velocity = entity.getVelocity();
			velocity.setX(vec.getX());
			velocity.setZ(vec.getY());
			entity.setVelocity(velocity);
			entity.setFallDistance(0);
			
			DamageTools.damageEntity(bender, entity, DEFLECT_DAMAGE);
		}
	}
}

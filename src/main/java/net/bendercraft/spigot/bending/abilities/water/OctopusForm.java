package net.bendercraft.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.water.WaterBalance.Damage;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

@ABendingAbility(name = OctopusForm.NAME, element = BendingElement.WATER)
public class OctopusForm extends BendingActiveAbility {
	public final static String NAME = "OctopusForm";
	

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 5;
	
	@ConfigurationParameter("Damage-Ice")
	private static int DAMAGE_ICE = 7;
	
	@ConfigurationParameter("Range")
	private static double RANGE = 30;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 12;
	
	@ConfigurationParameter("Radius-Affecting")
	public static double AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Radius")
	public static double RADIUS = 3;
	
	
	private long time;
	private long interval;
	
	// Those tell in which stage this ability is
	// PREPARING = source selected
	// PREPARED = source is coming towards bender OR ring is forming
	// PROGRESSING = ring is formed and everything's good
	
	
	// PREPARED
	private Block source; // Not nullable
	private Location location = null;
	private TempBlock coming = null; // Used when a source is coming towards player
	private boolean settingup = false; // Source is coming towards bender but have yet to get to eye level
	private boolean forming = false; // Ring is forming but not yet complete if true
	
	// PREPARING
	private double startangle;
	private double angle;
	private int y = 0;
	
	// PROGRESSING
	private int animstep = 1, step = 1, inc = 3;
	private double dta = 45;
	private List<WaterTentacle> tentacles = new ArrayList<WaterTentacle>();
	
	private List<TempBlock> blocks = new LinkedList<TempBlock>(); // Both PREPARED & PROGRESSING, holds ring base
	

	public OctopusForm(RegisteredAbility register, Player player) {
		super(register, player);
		this.interval = (long) (1000. / SPEED);
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.PREPARING) {
			return false;
		}
		settingup = true;
		forming = false;
		setState(BendingAbilityState.PREPARED);
		return false;
	}

	@Override
	public boolean swing() {
		if (isState(BendingAbilityState.START)) {
			Block target = BlockTools.getWaterSourceBlock(player, RANGE, false);
			if (target == null || !allow(target)) {
				return false;
			}
			time = System.currentTimeMillis();
			source = target;
			location = source.getLocation();
			coming = TempBlock.makeTemporary(source, Material.WATER, false);
			setState(BendingAbilityState.PREPARING);
			return false;
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			// TODO throw tentacle !
			LivingEntity target = EntityTools.getTargetedEntity(getPlayer(), RANGE);
			if(target != null) {
				WaterTentacle closest = tentacles.get(0);
				for(WaterTentacle tentacle : tentacles) {
					if(closest.origin == null 
							|| (tentacle.origin != null && target.getLocation().distance(closest.origin) > target.getLocation().distance(tentacle.origin))) {
						closest = tentacle;
					}
				}
				if(closest.hasTarget() && closest.isTarget(target)) {
					closest.freeze();
				} else {
					closest.target(target);
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
		if(isState(BendingAbilityState.PREPARED) || isState(BendingAbilityState.PROGRESSING)) {
			if (!player.isSneaking()
					|| !source.getWorld().equals(player.getWorld())) {
				return false;
			}
		}
		return true;
	}
	
	private boolean allow(Block block) {
		if(ProtectionManager.isLocationProtectedFromBending(player, register, block.getLocation())) {
			return false;
		}
		if(block.getType() != Material.AIR && !BlockTools.isWaterBased(block)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if (now > time + interval) {
			time = now;

			if(isState(BendingAbilityState.PREPARING)) {
				source.getWorld().playEffect(source.getLocation(), Effect.SMOKE, 4, 20);
			} else if(isState(BendingAbilityState.PREPARED)) {
				// Unlike Blast, here it does not calculate path before but adjust it as it goes along
				if(settingup) {
					if(coming.getBlock().getY() < player.getLocation().getBlockY()) {
						Block newBlock = coming.getBlock().getRelative(BlockFace.UP);
						if(!allow(newBlock)) {
							remove();
							return;
						}
						coming.revertBlock();
						coming = TempBlock.makeTemporary(newBlock, Material.WATER, false);
						location = coming.getLocation();
					} else if(coming.getBlock().getY() > player.getLocation().getBlockY()) {
						Block newBlock = coming.getBlock().getRelative(BlockFace.DOWN);
						if(!allow(newBlock)) {
							remove();
							return;
						}
						coming.revertBlock();
						coming = TempBlock.makeTemporary(newBlock, Material.WATER, false);
						location = coming.getLocation();
					} else {
						settingup = false; // Block is OK for Y, so let it come !
					}
				} else if(forming) {
					if (angle - startangle >= 360) {
						y += 1;
					} else {
						angle += 20;
					}
					if(!formRing()) {
						remove();
						return;
					}
					if (y == 2) {
						forming = false;
						// Spawn tentacle !
						for(int angle = 0 ; angle < 360 ; angle += dta) {
							tentacles.add(new WaterTentacle(this));
						}
						setState(BendingAbilityState.PROGRESSING);
					}
				} else {
					// Block is coming towards bender
					if(location.distance(player.getLocation()) > RADIUS) {
						Vector vector = Tools.getDirection(coming.getBlock().getLocation(), player.getLocation()).normalize();
						location = location.add(vector);
						Block newBlock = location.getBlock();
						if(!allow(newBlock)) {
							remove();
							return;
						}
						if(location.getBlock() != coming.getBlock()) {
							coming.revertBlock();
							coming = TempBlock.makeTemporary(newBlock, Material.WATER, false);
						}
					} else {
						// Block has reached player, now we need to form ring !
						forming = true;
						startangle = new Vector(1, 0, 0).angle(Tools.getDirection(coming.getLocation(), player.getLocation()));
						angle = startangle;
						coming.revertBlock();
						coming = null;
					}
				}
			} else if(isState(BendingAbilityState.PROGRESSING)) {
				step += 1;
				if (step % inc == 0)
					animstep += 1;
				if (animstep > 8)
					animstep = 1;
				if(!formOctopus()) {
					remove();
					return;
				}
			}
		}
	}

	private boolean formRing() {
		blocks.forEach(b -> b.revertBlock());
		blocks.clear();
		
		List<Block> doneblocks = new LinkedList<Block>();

		for (double theta = startangle; theta < startangle + angle; theta += 10) {
			double rtheta = Math.toRadians(theta);
			Block block = player.getLocation().add(new Vector(RADIUS * Math.cos(rtheta), 0, RADIUS * Math.sin(rtheta))).getBlock();
			if (!doneblocks.contains(block)) {
				if(allow(block)) {
					blocks.add(TempBlock.makeTemporary(block, Material.WATER, false));
				}
				doneblocks.add(block);
			}
		}

		if (blocks.isEmpty()) {
			return false;
		}

		return true;
	}
	
	private boolean formOctopus() {
		if(!formRing()) {
			return false;
		}
		
		Vector eyedir = player.getEyeLocation().getDirection();
		eyedir.setY(0);

		int current = 0;
		int astep = animstep;
		for (int i = 0; i < 360; i += dta) {
			astep += 1;
			double phi = Math.toRadians(i);
			Location loc = player.getLocation().add(new Vector(RADIUS * Math.cos(phi), 0, RADIUS * Math.sin(phi)));
			if(current < tentacles.size()) {
				if(!tentacles.get(current).progress(loc, astep)) {
					return false;
				}
				current++;
			}
		}
		
		return true;
	}

	@Override
	public void stop() {
		if(coming != null) {
			coming.revertBlock();
		}
		blocks.forEach(b -> b.revertBlock());
		tentacles.forEach(t -> t.remove());
	}

	@Override
	public Object getIdentifier() {
		return player;
	}
	
	public static class WaterTentacle {
		private Location origin;
		private LivingEntity target = null;
		private int distance;
		private long time;
		private List<TempBlock> blocks = new LinkedList<TempBlock>();
		private long interval;

		private OctopusForm parent;
		private boolean freeze;
		
		public WaterTentacle(OctopusForm parent) {
			this.parent = parent;
			this.interval = (long) (1000. / SPEED);
			this.time = System.currentTimeMillis();
		}
		
		private boolean addWater(Block block) {
			if (ProtectionManager.isLocationProtectedFromBending(parent.getPlayer(), parent.register, block.getLocation())) {
				return false;
			}
			if(block.getType() != Material.AIR && !BlockTools.isWaterBased(block)) {
				return false;
			}
			Material material = Material.WATER;
			if(freeze) {
				material = Material.ICE;
			}
			blocks.add(TempBlock.makeTemporary(block, material, false));
			return true;
		}
		
		public void target(LivingEntity target) {
			if(!freeze) {
				this.target = target;
				distance = 0;
			}
		}

		public boolean progress(Location location, int animation) {
			blocks.forEach(b -> b.revertBlock());
			blocks.clear();
			origin = location.clone();
			
			// Tentacle at rest
			if(target == null) {
				Vector direction = Tools.getDirection(parent.getPlayer().getLocation(), origin);
				direction.setY(0);
				direction.normalize();

				if(animation > 8) {
					animation = animation % 8;
				}

				if(parent.y >= 1) {
					Block baseblock = origin.clone().add(0, 1, 0).getBlock();
					if (animation == 1 || animation == 2 || animation == 8) {
						addWater(baseblock);
					} else {
						addWater(origin.clone().add(direction.getX(), 1, direction.getZ()).getBlock());
					}
				}

				if(parent.y == 2) {
					Block baseblock = origin.clone().add(0, 2, 0).getBlock();
					if (animation == 1) {
						addWater(origin.clone().add(-direction.getX(), 2, -direction.getZ()).getBlock());
					} else if (animation == 3 || animation == 7 || animation == 2 || animation == 8) {
						addWater(baseblock);
					} else if (animation == 4 || animation == 6) {
						addWater(origin.clone().add(direction.getX(), 2, direction.getZ()).getBlock());
					} else {
						addWater(origin.clone().add(2 * direction.getX(), 2, 2 * direction.getZ()).getBlock());
					}
				}
				return true;
			}
			
			// Tentacle at work (targetting someone)
			if(target.isDead()) {
				target = null;
				return true;
			}
			long now = System.currentTimeMillis();
			if ((now - time) >= interval) {
				time = now;
				Vector dir = target.getEyeLocation().toVector().clone().subtract(origin.toVector()).normalize();
				distance++;
				if(distance > RANGE) {
					target(null);
				}
				
				for(int i=0 ; i <= distance ; i++) {
					Location current = origin.clone().add(dir.clone().multiply(i));
					Block block = current.getBlock();
					if(!blocks.stream().anyMatch(t -> t.getBlock() == block)) {
						if(!addWater(block)) {
							target(null);
							parent.getBender().water.liquid();
							return true;
						}
					}
					
					boolean consumed = false;
					for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(current, AFFECTING_RADIUS)) {
						if(affect(entity)) {
							consumed = true;
						}
					}
					if(freeze && consumed) {
						blocks.forEach(b -> Bending.getInstance().getManager().addGlobalTempBlock(5000, b));
						blocks.clear();
						parent.getBender().cooldown(parent, 5000);
						parent.getBender().water.ice();
						return false;
					} else if(consumed) {
						target(null);
						parent.getBender().water.liquid();
						break;
					}
				}
			}
			return true;
		}
		
		private boolean affect(Entity entity) {
			BendingHitEvent event = new BendingHitEvent(parent, entity);
			Bending.callEvent(event);
			if(event.isCancelled()) {
				return false;
			}
			
			if(entity == parent.getPlayer()) {
				return false;
			}
			if(freeze) {
				DamageTools.damageEntity(parent.getBender(), entity, parent, parent.getBender().water.damage(Damage.ICE, DAMAGE_ICE));
				Frozen.freeze(parent.getPlayer(), entity.getLocation(), 3);
			} else {
				DamageTools.damageEntity(parent.getBender(), entity, parent, parent.getBender().water.damage(Damage.LIQUID, DAMAGE));
				entity.setVelocity(new Vector(0,0,0));
			}		
			return true;
		}

		public void remove() {
			blocks.forEach(b -> b.revertBlock());
			blocks.clear();
		}
		
		public boolean hasTarget() {
			return target != null;
		}
		
		public boolean isTarget(LivingEntity entity) {
			return entity == target;
		}
		
		public void freeze() {
			freeze = true;
		}
	}

}

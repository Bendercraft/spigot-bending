package net.bendercraft.spigot.bending.abilities.water;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterBalance.Damage;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = IceSpike.NAME, element = BendingElement.WATER)
public class IceSpike extends BendingActiveAbility {
	public final static String NAME = "IceSpike";
	
	@ConfigurationParameter("Range-select")
	private static double RANGE_SELECT = 15;
	
	@ConfigurationParameter("Range")
	private static double RANGE = 10;
	
	@ConfigurationParameter("Radius-Launch")
	private static double RADIUS = 4;
	
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 4;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 35;
	
	@ConfigurationParameter("Radius-Affecting")
	public static double AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Push")
	private static double PUSHFACTOR = 0.3;
	
	@ConfigurationParameter("Cooldown")
	private static int COOLDOWN = 5000;
	
	@ConfigurationParameter("Iced")
	private static long ICED = 5000;
	
	private List<IceSpikeColumn> columns = new LinkedList<IceSpikeColumn>();

	private double push;
	private int cooldown;
	private int damage;
	private double range;
	private double speed;
	
	public IceSpike(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.push = PUSHFACTOR;
		if(bender.hasPerk(BendingPerk.WATER_ICESPIKE_PUSHBACK)) {
			this.push *= 1.5;
		}
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.WATER_ICESPIKE_COOLDOWN)) {
			this.cooldown -= 1500;
		}
		
		this.damage = DAMAGE;
		if(bender.hasPerk(BendingPerk.WATER_ICESPIKE_DAMAGE)) {
			this.damage += 1;
		}
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.WATER_ICESPIKE_RANGE)) {
			this.range *= 1.1;
		}
		
		this.speed = SPEED;
		if(bender.hasPerk(BendingPerk.WATER_ICESPIKE_SPEED)) {
			this.speed *= 1.1;
		}
	}

	@Override
	public boolean swing() {
		if(!isState(BendingAbilityState.START)) {
			return false;
		}
		Location target = EntityTools.getTargetedLocation(player, RANGE_SELECT, BlockTools.getTransparentEarthBending());
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(target, range);
		entities.removeIf(e -> e == player);
		List<Block> blocks = BlockTools.getBlocksAroundPoint(target, RADIUS);
		blocks.removeIf(b -> b.getType() != Material.ICE || (TempBlock.isTempBlock(b) && !TempBlock.get(b).isBendAllowed()));
		Collections.shuffle(blocks);
		for(Block block : blocks) {
			Vector direction = null;
			if(!entities.isEmpty()) {
				direction = entities.remove(0).getLocation().subtract(block.getLocation()).toVector();
			} else {
				double[] randoms = new Random().doubles(3, -1, 1).toArray();
				direction = new Vector(randoms[0], randoms[1], randoms[2]);
			}
			IceSpikeColumn column = new IceSpikeColumn(this, bender.water.damage(Damage.ICE, damage), range, speed, AFFECTING_RADIUS, push, block.getLocation(), direction);
			columns.add(column);
			if(TempBlock.isTempBlock(block)) {
				TempBlock.revertBlock(block);
			} else {
				block.setType(Material.AIR);
			}
		}
		if(columns.isEmpty()) {
			return false;
		}
		target.getWorld().playSound(target, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.0f);
		setState(BendingAbilityState.PROGRESSING);
		bender.cooldown(this, cooldown);
		bender.water.ice();
		return false;
	}

	@Override
	public void progress() {
		Iterator<IceSpikeColumn> it = columns.iterator();
		while(it.hasNext()) {
			IceSpikeColumn blast = it.next();
			if(!blast.progress()) {
				blast.remove();
				it.remove();
			}
		}
		if(columns.isEmpty()) {
			remove();
		}
	}

	@Override
	public void stop() {
		columns.forEach(b -> b.remove());
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	
	public static class IceSpikeColumn {
		private Vector direction;
		private Location source;
		private Location current;
		private TempBlock previous;
		
		private List<TempBlock> column = new LinkedList<TempBlock>();
		
		private long time;
		private double damage;
		private long interval;
		private double range;
		private double radius;
		private double push;
		private BendingAbility parent;

		public IceSpikeColumn(BendingAbility parent, double damage, double range, double speed, double radius, double push, Location source, Vector direction) {
			this.damage = damage;
			this.range = range;
			this.interval = (long) (1000. / speed);
			this.radius = radius;
			this.push = push;
			this.parent = parent;
			this.direction = direction.clone().normalize();
			this.source = source.clone();
			this.current = source.clone().add(direction);
		}
		
		public boolean progress() {
			long now = System.currentTimeMillis();
			if (now < (time + interval)) {
				return true;
			}
			time = System.currentTimeMillis();
			
			if (source.distance(current) > range) {
				return false;
			}
			
			current = current.add(direction);
			Block block = current.getBlock();
			if (BlockTools.isTransparentToEarthbending(parent.getPlayer(), block) && !block.isLiquid()) {
				BlockTools.breakBlock(block); // DESTROY FLOWERSSSS
			} else if (!(BlockTools.isAir(block) || BlockTools.isWaterBased(block))) {
				return false;
			}
			
			PluginTools.removeSpouts(current, parent.getPlayer());
			
			if (EarthBlast.removeOneAroundPoint(current, parent.getPlayer(), radius)
					|| WaterManipulation.removeOneAroundPoint(current, parent.getPlayer(), radius) 
					|| FireBlast.removeOneAroundPoint(current, parent.getPlayer(), radius)) {
				return false;
			}
			
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(current, radius)) {
				if(affect(entity)) {
					return false;
				}
			}
			
			
			if(parent.getBender().hasPerk(BendingPerk.WATER_PRESERVE)) {
				if(previous != null) {
					previous.revertBlock();
				}
				previous = TempBlock.makeTemporary(parent, current.getBlock(), Material.ICE, false);
				column.add(previous);
			} else {
				TempBlock.makeGlobal(ICED, current.getBlock(), Material.ICE, false);
			}
			
			
			return true;
		}
		
		public void remove() {
			if(previous != null) {
				previous.revertBlock();
			}
			column.forEach(t -> t.revertBlock());
		}
		
		private boolean affect(Entity entity) {
			BendingHitEvent event = new BendingHitEvent(parent, entity);
			Bending.callEvent(event);
			if(event.isCancelled()) {
				return false;
			}
			
			if (entity == parent.getPlayer()) {
				return false;
			}
			Vector vector = parent.getPlayer().getEyeLocation().getDirection();
			entity.setVelocity(vector.normalize().multiply(push));

			DamageTools.damageEntity(parent.getBender(), entity, parent, damage);
			return true;
		}
		
	}
}

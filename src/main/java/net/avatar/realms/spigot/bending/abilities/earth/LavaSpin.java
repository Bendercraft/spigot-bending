package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;

@ABendingAbility(name = "Lavaspin", bind = BendingAbilities.LavaSpin, element = BendingElement.Earth, affinity = BendingAffinity.Lavabend)
public class LavaSpin extends BendingActiveAbility {
	@ConfigurationParameter("Speed")
	public static double SPEED = 14;
	
	@ConfigurationParameter("Damage")
	public static double DAMAGE = 4;
	
	@ConfigurationParameter("Radius")
	public static double AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Rest-Distance")
	public static int REST_DISTANCE = 3;
	
	@ConfigurationParameter("Cooldown")
	public static int COOLDOWN = 5000;
	
	@ConfigurationParameter("Select-Range")
	public static double SELECT_RANGE = 10;
	
	@ConfigurationParameter("Reach")
	public static double REACH = 15;
	
	
	private static byte level = 0x0;
	private static Material MATERIAL_REST = Material.COBBLE_WALL;
	private static Material MATERIAL_RESTLESS = Material.LAVA;
	
	private List<LivingEntity> affecteds;
	private Location current;
	private long interval;
	private long time;
	
	private TempBlock block;
	private boolean lava;
	public LavaSpin(Player player) {
		super(player);
		
		interval = (long) (1000. / SPEED);
		time = this.startedTime;
		lava = false;
		affecteds = new LinkedList<LivingEntity>();
	}
	
	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.Prepared) {
			//Send
			lava = true;
			setState(BendingAbilityState.Progressing);
		} else if(getState() == BendingAbilityState.Progressing) {
			lava = true;
			setState(BendingAbilityState.Prepared);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.Start) {
			Block target = EntityTools.getTargetBlock(player, SELECT_RANGE, BlockTools.getTransparentEarthBending());
			if(target == null) {
				return false;
			}
			
			if(BlockTools.isEarthbendable(player, target)) {
				current = target.getRelative(BlockFace.UP).getLocation();
				block = new TempBlock(current.getBlock(), MATERIAL_REST);
				Vector direction = player.getEyeLocation().subtract(current).toVector().normalize();
				if(move(direction)) {
					setState(BendingAbilityState.Prepared);
					bender.cooldown(this, COOLDOWN);
				}
			}
		} else {
			remove();
		}
		return false;
	}
	
	private boolean move(Vector direction) {
		current = current.add(direction.normalize());
		block.revertBlock();
		if(current.getBlock().getType() == Material.AIR || BlockTools.isPlant(current.getBlock())) {
			if(lava) {
				block = new TempBlock(current.getBlock(), MATERIAL_RESTLESS, level);
			} else {
				block = new TempBlock(current.getBlock(), MATERIAL_REST);
			}
		} else {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.LavaSpin, this.current)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if (now - time >= interval) {
			time = now;
			if(getState() == BendingAbilityState.Prepared) {
				if(current.distance(player.getEyeLocation()) >= REST_DISTANCE) {
					Vector direction = player.getEyeLocation().subtract(current).toVector().normalize();
					if(!move(direction)) {
						remove();
						return;
					}
				} else {
					affecteds.clear();
					lava = false;
					if(block.getBlock().getType() != MATERIAL_REST) {
						block = new TempBlock(current.getBlock(), MATERIAL_REST);
					}
				}
			} else if(getState() == BendingAbilityState.Progressing) {
				Vector direction = EntityTools.getTargetedLocation(player, REACH+4, BlockTools.getNonOpaque()).subtract(current).toVector();
				if(!move(direction)) {
					remove();
					return;
				}
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(current, AFFECTING_RADIUS)) {
					affect(entity);
				}
				if(current.distance(player.getEyeLocation()) >= REACH) {
					setState(BendingAbilityState.Prepared);
				}
			}
		}
	}
	
	private void affect(LivingEntity entity) {
		entity.setFireTicks(0);
		if (!ProtectionManager.isEntityProtected(entity) 
				&& entity.getEntityId() != player.getEntityId()
				&& !affecteds.contains(entity)) {
			EntityTools.damageEntity(this.player, entity, DAMAGE);
			affecteds.add(entity);
		}
	}

	@Override
	public void stop() {
		if(block != null) {
			block.revertBlock();
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

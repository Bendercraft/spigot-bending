package net.avatar.realms.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.DamageTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.TempBlock;

@ABendingAbility(name = WaterTurret.NAME, element = BendingElement.WATER)
public class WaterTurret extends BendingActiveAbility {
	public final static String NAME = "WaterTurret";
	
	
	@ConfigurationParameter("Select-Range")
	private static double SELECT_RANGE = 15;
	
	@ConfigurationParameter("Activation-Range")
	private static double ACTIVATION_RANGE = 10;
	
	@ConfigurationParameter("Hit-Range")
	private static double HIT_RANGE = 30;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 12;
	
	@ConfigurationParameter("Radius")
	public static double AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 5000;
	
	@ConfigurationParameter("Damage-Per-Combo")
	private static int DAMAGE = 1;
	
	private Block origin;
	private LivingEntity target = null;
	private long time;
	private List<TempBlock> turrets = new LinkedList<TempBlock>();
	private Location head;
	private long interval;
	public WaterTurret(RegisteredAbility register, Player player) {
		super(register, player);
		
		interval = (long) (1000. / SPEED);
	}

	@Override
	public boolean swing() {
		remove();
		return false;
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			origin = BlockTools.getWaterSourceBlock(this.player, SELECT_RANGE, EntityTools.canPlantbend(this.player));
			if(origin == null) {
				return false;
			}
			setState(BendingAbilityState.PREPARED);
			time = System.currentTimeMillis();
			bender.cooldown(this, COOLDOWN);
			
		}
		return false;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.PREPARED) {
			long now = System.currentTimeMillis();
			if(now - time > 100) {
				time = now;
				target = EntityTools.getNearestLivingEntity(origin.getLocation(), ACTIVATION_RANGE, player);
				if(target != null) {
					setState(BendingAbilityState.PROGRESSING);
				}
			}
			origin.getWorld().playEffect(origin.getLocation(), Effect.SMOKE, 4, (int) ACTIVATION_RANGE);
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			if(target.isDead()) {
				remove();
				return;
			}
			if ((System.currentTimeMillis() - this.time) >= interval) {
				this.time = System.currentTimeMillis();
				if(turrets.size() < 2) {
					Block test = origin;
					if(!turrets.isEmpty()) {
						test = turrets.get(turrets.size()-1).getBlock();
					}
					if(test.getRelative(BlockFace.UP).getType() != Material.AIR
							&& !BlockTools.isWater(test.getRelative(BlockFace.UP))) {
						remove();
						return;
					}
					head = test.getRelative(BlockFace.UP).getLocation();
					if(!TempBlock.isTempBlock(head.getBlock()) || turrets.contains(TempBlock.get(head.getBlock()))) {
						//turrets.add(turrets.size(), new TempBlock(head.getBlock(), Material.WATER, FULL));
						turrets.add(turrets.size(), TempBlock.makeTemporary(head.getBlock(), Material.WATER, false));
					}
				} else {
					Vector dir = target.getEyeLocation().toVector().clone().subtract(head.toVector()).normalize();
					head = head.add(dir);
					if(head.getBlock().getType() != Material.AIR 
							&& !BlockTools.isWater(head.getBlock())) {
						remove();
						return;
					}
					
					if(!TempBlock.isTempBlock(head.getBlock()) || turrets.contains(TempBlock.get(head.getBlock()))) {
						//turrets.add(turrets.size(), new TempBlock(head.getBlock(), Material.WATER, FULL));
						turrets.add(turrets.size(), TempBlock.makeTemporary(head.getBlock(), Material.WATER, false));
					}
				}
				
				boolean consumed = false;
				for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(head, AFFECTING_RADIUS)) {
					if(entity == player) {
						continue;
					}
					DamageTools.damageEntity(bender, entity, DAMAGE);
					entity.setVelocity(new Vector(0,0,0));
					consumed = true;
				}
				if(consumed) {
					remove();
				}
			}
		}
	}

	@Override
	public void stop() {
		for(TempBlock block : turrets) {
			block.revertBlock();
		}
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}

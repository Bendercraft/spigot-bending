package net.bendercraft.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = EarthGrab.NAME, element = BendingElement.EARTH)
public class EarthGrab extends BendingActiveAbility {
	public final static String NAME = "EarthGrab";
	
	@ConfigurationParameter("Range")
	private static double RANGE = 15;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 15000;

	@ConfigurationParameter("Other-Duration")
	public static long OTHER_DURATION = 2500;
	
	@ConfigurationParameter("Speed")
	private static double SPEED = 15;
	

	private Location origin;
	private Location current;
	private Vector direction;
	private long preparedTime = 0;
	private List<TempBlock> preparedBlock = new LinkedList<TempBlock>();
	
	private LivingEntity target;
	private long grabbedTime = 0;
	private List<TempBlock> affectedBlocks = new ArrayList<TempBlock>(8);
	
	private double range;
	private long duration;
	private long cooldown;
	
	private long interval;

	public EarthGrab(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHGRAB_RANGE)) {
			this.range += 2;
		}
		this.duration = OTHER_DURATION;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHGRAB_DURATION)) {
			this.duration += 1;
		}
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHGRAB_COOLDOWN)) {
			this.cooldown -= 3000;
		}
		
		double speed = SPEED;
		if(bender.hasPerk(BendingPerk.EARTH_EARTHBLAST_SPEED)) {
			speed *= 1.15;
		}
		this.interval = (long) (1000. / speed);
	}

	@Override
	public boolean swing() {
		if (!isState(BendingAbilityState.START)) {
			return false;
		}
		
		if(player.isSneaking()) {
			direction = player.getEyeLocation().getDirection().setY(0).normalize();
			origin = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().add(direction);
			current = origin.clone();
			if(BlockTools.isEarthbendable(player, current.getBlock())) {
				setState(BendingAbilityState.PREPARED);
			}
		}
		bender.cooldown(this, cooldown);
		return false;
	}
	
	@Override
	public void progress() {
		if(isState(BendingAbilityState.PREPARED)) {
			if ((System.currentTimeMillis() - preparedTime) >= interval) {
				preparedTime = System.currentTimeMillis();
				
				current = current.add(direction);
				
				if(current.distanceSquared(origin) > range*range) {
					remove();
					return;
				}
				
				Block candidate = current.getBlock();
				for(TempBlock temp : preparedBlock) {
					if(temp.getBlock() == candidate) {
						return;
					}
				}
				
				// Going up ?
				for(int i=0 ; i < 3 ; i++) {
					if(BlockTools.isEarthbendable(player, candidate.getRelative(BlockFace.UP))) {
						candidate = candidate.getRelative(BlockFace.UP);
					}
				}
				// Going down ?
				if(!BlockTools.isEarthbendable(player, candidate)) {
					candidate = current.getBlock();
					for(int i=0 ; i < 3 ; i++) {
						if(BlockTools.isEarthbendable(player, candidate.getRelative(BlockFace.DOWN))) {
							candidate = candidate.getRelative(BlockFace.DOWN);
							break;
						}
					}
				}
				if(!BlockTools.isEarthbendable(player, candidate)) {
					remove();
					return;
				}
				
				preparedBlock.add(TempBlock.makeTemporary(this, candidate, Material.COBBLESTONE, false));
				
				// Touch anything ?
				Location check = candidate.getRelative(BlockFace.UP).getLocation().add(0.5, 0.5, 0.5);
				for(LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(check, 2.5)) {
					if(affect(entity)) {
						setState(BendingAbilityState.PROGRESSING);
						grabbedTime = System.currentTimeMillis();
						break;
					}
				}
			}
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			if(System.currentTimeMillis() - grabbedTime > duration) {
				remove();
			}
		} else {
			remove();
		}
	}

	public boolean affect(LivingEntity entity) {
		if(entity == player) {
        	return false;
        }
		
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
        if (ProtectionManager.isEntityProtected(entity)) {
            return false;
        }
        
        this.target = entity;

        double x = (int) this.target.getLocation().getX();
        double y = (int) this.target.getLocation().getY();
        double z = (int) this.target.getLocation().getZ();

        x = (x < 0) ? x - 0.5 : x + 0.5;
        z = (z < 0) ? z - 0.5 : z + 0.5;

        this.origin = new Location(this.target.getLocation().getWorld(), x, y, z, this.target.getLocation().getYaw(), this.target.getLocation().getPitch());

        if (ProtectionManager.isLocationProtectedFromBending(player, register, this.origin)) {
            return false;
        }

        List<Location> locs = new LinkedList<Location>();

        locs.add(this.origin.clone().add(0, 0, -1));
        locs.add(this.origin.clone().add(0, 0, 1));
        locs.add(this.origin.clone().add(-1, 0, 0));
        locs.add(this.origin.clone().add(1, 0, 0));

        //Check if we can earthgrab the player at this position
        Iterator<Location> it = locs.iterator();
        while (it.hasNext()) {
            Location loc = it.next();
            if (BlockTools.isEarthbendable(player, loc.getBlock())) {
                it.remove(); //We don't need to bend it to lock the target
            } else if (BlockTools.isFluid(loc.getBlock()) || BlockTools.isPlant(loc.getBlock())) {
                //Check if block under this location is bendable
                loc.add(0, -1, 0);
                if (!BlockTools.isEarthbendable(player, loc.getBlock())) {
                    if (BlockTools.isFluid(loc.getBlock()) || BlockTools.isPlant(loc.getBlock())) {

                        loc.add(0, -1, 0);
                        if (!BlockTools.isEarthbendable(player, loc.getBlock())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }

        this.target.teleport(this.origin); // To be sure the guy is locked in the grab

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
        for (Location loc : locs) {
            int h = 1;
            if (this.origin.getY() - loc.getY() >= 2) {
                h = 2;
            }
            Material t = loc.getBlock().getType();
            for (int i = 0; i < h; i++) {
                this.affectedBlocks.add(TempBlock.makeTemporary(this, loc.add(0, 1, 0).getBlock(), t, false));
            }
        }

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		preparedBlock.forEach(t -> t.revertBlock());
		affectedBlocks.forEach(t -> t.revertBlock());
	}
	
	public static boolean isGrabbed(Entity entity) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthGrab grab = (EarthGrab) ab;
			if (grab.target == entity) {
				return true;
			}
		}
		return false;
	}
}

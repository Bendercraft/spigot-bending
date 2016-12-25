package net.bendercraft.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
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
	private static double range = 15;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 15000;

	@ConfigurationParameter("Other-Duration")
	public static int OTHER_DURATION = 300;

	@ConfigurationParameter("Self-Duration")
	private static int SELF_DURATION = 5;

	private static Integer ID = Integer.MIN_VALUE;
	private int id;
	private boolean self;
	private LivingEntity target;
	private Location origin;
	private long time = 0;
	private boolean toKeep = true;
	private List<TempBlock> affectedBlocks = new ArrayList<TempBlock>(8);

	public EarthGrab(RegisteredAbility register, Player player) {
		super(register, player);
		this.id = ID++;
	}

	@Override
	public boolean swing() {
		if (!isState(BendingAbilityState.START)) {
			return false;
		}
		if (bender.isOnCooldown(NAME)) {
			return false;
		}
		this.self = false;
		this.target = EntityTools.getTargetedEntity(this.player, range);
		if(this.target != null && affect()) {
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.START) {
			return false;
		}
		if (this.bender.isOnCooldown(NAME)) {
			return false;
		}
		this.self = true;
		this.target = player;
		if (this.target != null && affect()) {
			this.id = ID++;
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	public boolean affect() {
		BendingHitEvent event = new BendingHitEvent(this, this.target);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
        if (ProtectionManager.isEntityProtected(this.target)) {
            return false;
        }

        this.time = System.currentTimeMillis();
        this.toKeep = true;

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
            }
            else if (BlockTools.isFluid(loc.getBlock()) || BlockTools.isPlant(loc.getBlock())) {
                //Check if block under this location is bendable
                loc.add(0, -1, 0);
                if (!BlockTools.isEarthbendable(player, loc.getBlock())) {
                    if (BlockTools.isFluid(loc.getBlock()) || BlockTools.isPlant(loc.getBlock())) {

                        loc.add(0, -1, 0);
                        if (!BlockTools.isEarthbendable(player, loc.getBlock())) {
                            return false;
                        }
                    }
                    else {
                        return false;
                    }
                }
            }
            else {
                return false;
            }
        }

        this.target.teleport(this.origin); // To be sure the guy is locked in the grab

        int duration = 20 * ((this.self)? SELF_DURATION : OTHER_DURATION);

        PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, duration, 150); // The entity cannot move
        PotionEffect jumpless = new PotionEffect(PotionEffectType.JUMP, duration, 150); // The entity cannot jump
        this.target.addPotionEffect(slowness);
        this.target.addPotionEffect(jumpless);

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
        for (Location loc : locs) {
            int h = 1;
            if (this.origin.getY() - loc.getY() >= 2) {
                h = 2;
            }
            Material t = loc.getBlock().getType();
            for (int i = 0; i < h; i++) {
                //this.affectedBlocks.add(new TempBlock(loc.add(0, 1, 0).getBlock(), t, full));
                this.affectedBlocks.add(TempBlock.makeTemporary(loc.add(0, 1, 0).getBlock(), t, false));
            }
        }

        if (this.target instanceof Player && this.target.getEntityId() != player.getEntityId()) {
            EntityTools.grab((Player) this.target, this.time);
        }

        this.bender.cooldown(NAME, COOLDOWN);
		return true;
	}

	public void setToKeep(boolean k) {
		this.toKeep = k;
	}

	@Override
	public void progress() {
		Location loc = this.target.getLocation();

		if (!this.toKeep) {
			remove();
			return;
		}

		if (this.target == null) {
			remove();
			return;
		}
		if (this.player.getEntityId() == this.target.getEntityId()) {
			if (System.currentTimeMillis() > this.time + (SELF_DURATION * 1000)) {
				remove();
				return;
			}
		}

		if (loc.getWorld() != this.origin.getWorld()
				|| (int) loc.getX() != (int) this.origin.getX()
				|| (int) loc.getZ() != (int) this.origin.getZ()
				|| (int) loc.getY() != (int) this.origin.getY()
				|| !BlockTools.isEarthbendable(this.player, loc.add(0, 0, -1).getBlock()) 
				|| !BlockTools.isEarthbendable(this.player, loc.add(0, 0, +2).getBlock()) 
				|| !BlockTools.isEarthbendable(this.player, loc.add(-1, 0, -1).getBlock()) 
				|| !BlockTools.isEarthbendable(this.player, loc.add(+2, 0, 0).getBlock())) {
			remove();
			return;
		}
	}

	public boolean revertEarthGrab() {
		for (TempBlock tb : this.affectedBlocks) {
			tb.revertBlock();
		}

		if (this.target != null) {
			this.target.removePotionEffect(PotionEffectType.SLOW);
			this.target.removePotionEffect(PotionEffectType.JUMP);
			if (this.target instanceof Player) {
				EntityTools.unGrab((Player) this.target);
			}
		}
		return true;
	}

	public static EarthGrab blockInEarthGrab(Block block) {
		for (BendingAbility ab : AbilityManager.getManager().getInstances(NAME).values()) {
			EarthGrab grab = (EarthGrab) ab;
			if (grab.locInEarthGrab(block.getLocation())) {
				return grab;
			}
		}
		return null;
	}

	public boolean locInEarthGrab(Location loc) {
		int x = (int) loc.getX();
		int y = (int) loc.getY();
		int z = (int) loc.getZ();

		if (z < 0) {
			z++;
		}
		if (x < 0) {
			x++;
		}

		if (this.origin.getWorld() == loc.getWorld()) {
			int lY = (int) this.origin.getY();
			int lX = (int) this.origin.getX();
			int lZ = (int) this.origin.getZ();

			lZ++; // South
			if (lX == x && lY == y && lZ == z) {
				return true;
			}

			lZ -= 2; // North
			if (lX == x && lY == y && lZ == z) {
				return true;
			}

			lZ++;
			lX++; // East
			if (lX == x && lY == y && lZ == z) {
				return true;
			}

			lX -= 2; // West
			if (lX == x && lY == y && lZ == z) {
				return true;
			}
		}

		return false;
	}

	public boolean isEarthGrabBlock(Block block) {
		for (TempBlock tempBlock : affectedBlocks) {
			if (block.getLocation().equals(tempBlock.getLocation())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public void stop() {
		revertEarthGrab();
	}
}

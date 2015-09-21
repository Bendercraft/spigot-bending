package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@BendingAbility(name="Earth Grab", element=BendingElement.Earth)
public class EarthGrab extends BendingActiveAbility {
	@ConfigurationParameter("Range")
	private static double range = 15;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 15000;
	
	@ConfigurationParameter("Other-Duration")
	public static int OTHER_DURATION = 300;
	
	@ConfigurationParameter("Self-Duration")
	private static int SELF_DURATION = 5;

	private static final byte full = 0x0;

	private static Integer ID = Integer.MIN_VALUE;
	private int id;
	private boolean self;
	private LivingEntity target;
	private IBendingAbility parent;
	private Location origin;
	private long time = 0;
	private boolean toKeep = true;
	private List<TempBlock> affectedBlocks = new ArrayList<TempBlock>(8);

	public EarthGrab(Player player) {
		super(player, null);
	}
	
	@Override
	public boolean swing() {
		if(state != BendingAbilityState.CanStart) {
			return false;
		}
		if (bender.isOnCooldown(BendingAbilities.EarthGrab)) {
			return false;
		}
		this.self = false;
		Entity closestentity = EntityTools.getTargettedEntity(player, range);
		boolean done = grabEntity(player, closestentity);
		if (target != null && done == true) {
			if (ID == Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			}
			id = ID++;
			AbilityManager.getManager().addInstance(this);
			state = BendingAbilityState.Progressing;
		}
		return false;
	}
	
	@Override
	public boolean sneak() {
		if(state != BendingAbilityState.CanStart) {
			return false;
		}
		if (bender.isOnCooldown(BendingAbilities.EarthGrab)) {
			return false;
		}
		this.self = true;
		boolean done = grabEntity(player, player);
		if (target != null && done == true) {
			if (ID == Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			}
			id = ID++;
			AbilityManager.getManager().addInstance(this);
			state = BendingAbilityState.Progressing;
		}
		return false;
	}

	public boolean grabEntity(Player player, Entity entity) {
		if (entity != null) {
			if (entity instanceof LivingEntity) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					return false;
				}
				
				target = (LivingEntity) entity;
				time = System.currentTimeMillis();
				toKeep = true;

				double x = (int) target.getLocation().getX();
				double y = (int) target.getLocation().getY();
				double z = (int) target.getLocation().getZ();

				x = (x < 0) ? x - 0.5 : x + 0.5;
				z = (z < 0) ? z - 0.5 : z + 0.5;

				origin = new Location(entity.getLocation().getWorld(), x, y, z,
						entity.getLocation().getYaw(),
						entity.getLocation().getPitch());
				
				if (ProtectionManager.isRegionProtectedFromBending(player, BendingAbilities.RaiseEarth, origin)) {
					return false;
				}

				List<Location> locs = new LinkedList<Location>();
				List<Location> toRemove = new LinkedList<Location>();

				locs.add(origin.clone().add(0, 0, -1));
				locs.add(origin.clone().add(0, 0, 1));
				locs.add(origin.clone().add(-1, 0, 0));
				locs.add(origin.clone().add(1, 0, 0));

				int cpt = 0;
				
				for (Location loc : locs) {
					if (BlockTools.isFluid(loc.getBlock())
							|| BlockTools.isPlant(loc.getBlock())) {
						
						loc.add(0 ,-1, 0);
						if (BlockTools.isEarthbendable(player,loc.getBlock())) {
							cpt++;
						}
						else if (BlockTools.isFluid(loc.getBlock()) 
								|| BlockTools.isPlant(loc.getBlock())){
							
							loc.add(0, -1, 0);
							if (BlockTools.isEarthbendable(player,loc.getBlock())) {	
								cpt++;
							}
							else {
								return false;
							}				
						}
						else {
							return false;
						}	
					}
					else if (BlockTools.isEarthbendable(player, loc.getBlock())) {
						cpt ++;
						toRemove.add(loc);			
					}
					else {
						return false;
					}
				}
				
				for (Location tr : toRemove) {
					locs.remove(tr);
				}

				if (cpt >= 4) {			
					target.teleport(origin);
					// To be sure the guy is locked in the grab
					
					int duration;
					if (self) {
						duration = SELF_DURATION * 20;
					} else {
						duration = OTHER_DURATION * 20;
					}
					PotionEffect slowness = new PotionEffect(
							PotionEffectType.SLOW, duration, 150); // The entity
																	// cannot
																	// move
					PotionEffect jumpless = new PotionEffect(
							PotionEffectType.JUMP, duration, 150); // The entity
																	// cannot
																	// jump
					target.addPotionEffect(slowness);
					target.addPotionEffect(jumpless);
					target.getWorld().playEffect(target.getLocation(),Effect.GHAST_SHOOT, 0, 4);
					for (Location loc : locs) {
						int h = 1;
						if (origin.getY() - loc.getY() >= 2) {
							h = 2;
						}
						Material t = loc.getBlock().getType();
						for (int i = 0; i < h; i++) {
							affectedBlocks.add(new TempBlock(loc.add(0,1,0).getBlock(), t, full));
						}
					}

					if (target instanceof Player
							&& target.getEntityId() != player.getEntityId()) {
						EntityTools.grab((Player) target, time);
					}
					bender.cooldown(BendingAbilities.EarthGrab, COOLDOWN);
				}
			}
		}
		return true;
	}

	public void setToKeep(boolean k) {
		toKeep = k;
	}

	public boolean progress() {
		Location loc = target.getLocation();

		if (!toKeep) {
			return false;
		}

		if (target == null) {
			return false;
		}
		if (player.getEntityId() == target.getEntityId()) {
			if (System.currentTimeMillis() > time
					+ (SELF_DURATION * 1000)) {
				return false;
			}
		}

		if (loc.getWorld() != origin.getWorld()) {
			return false;
		}
		if ((int) loc.getX() != (int) origin.getX()) {
			return false;
		}
		if ((int) loc.getZ() != (int) origin.getZ()) {
			return false;
		}
		if ((int) loc.getY() != (int) origin.getY()) {
			return false;
		}

		if (!BlockTools.isEarthbendable(player, loc.add(0, 0, -1).getBlock())) {
			return false;
		}
		if (!BlockTools.isEarthbendable(player, loc.add(0, 0, +2).getBlock())) {
			return false;
		}
		if (!BlockTools.isEarthbendable(player, loc.add(-1, 0, -1).getBlock())) {
			return false;
		}
		if (!BlockTools.isEarthbendable(player, loc.add(+2, 0, 0).getBlock())) {
			return false;
		}
		return true;
	}

	public boolean revertEarthGrab() {
		for (TempBlock tb : affectedBlocks)  {
			tb.revertBlock();
		}
		
		if (target != null) {
			target.removePotionEffect(PotionEffectType.SLOW);
			target.removePotionEffect(PotionEffectType.JUMP);
			if (target instanceof Player) {
				EntityTools.unGrab((Player) target);
			}
		}
		return true;
	}

	public static EarthGrab blockInEarthGrab(Block block) {
		for (IBendingAbility ab : AbilityManager.getManager().getInstances(BendingAbilities.EarthGrab).values()) {
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

		if (origin.getWorld() == loc.getWorld()) {
			int lY = (int) origin.getY();
			int lX = (int) origin.getX();
			int lZ = (int) origin.getZ();

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

	@Override
	public Object getIdentifier() {
		return id;
	}

	@Override
	public BendingAbilities getAbilityType() {
		return BendingAbilities.EarthGrab;
	}
}

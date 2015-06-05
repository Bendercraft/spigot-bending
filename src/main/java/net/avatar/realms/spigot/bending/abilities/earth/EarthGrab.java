package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
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

public class EarthGrab implements IAbility {

	private static double range = ConfigManager.earthGrabRange;
	private static Map<Integer, EarthGrab> instances = new HashMap<Integer, EarthGrab>();
	private static Integer ID = Integer.MIN_VALUE;
	private static int benderTargettedDuration = 100; // 5 secs
	private static int otherTargettedDuration = 6000; // 5 minutes
	private static final byte full = 0x0;

	private int id;
	private boolean self;
	private BendingPlayer bPlayer;
	private Player bender;
	private LivingEntity target;
	private IAbility parent;
	private Location origin;
	private long time = 0;
	private boolean toKeep = true;
	private List<TempBlock> affectedBlocks = new ArrayList<TempBlock>(8);

	public EarthGrab(Player player, boolean self, IAbility parent) {
		this.parent = parent;
		this.self = self;
		this.bender = player;
		bPlayer = BendingPlayer.getBendingPlayer(bender);
		if (bPlayer.isOnCooldown(Abilities.EarthGrab)) {
			return;
		}		
		boolean done;
		if (self) {
			done = grabEntity(bender, bender);
		} else {
			Entity closestentity = EntityTools.getTargettedEntity(player, range);
			done = grabEntity(bender, closestentity);
		}
		if (target != null && done == true) {
			if (ID == Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			}
			id = ID++;
			instances.put(id, this);
		}

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
				
				if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.RaiseEarth, origin)) {
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
						duration = benderTargettedDuration;
					} else {
						duration = otherTargettedDuration;
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
							affectedBlocks.add(new TempBlock(loc.add(0,1,0).getBlock(), t, full, player, EarthGrab.class));
						}
					}

					if (target instanceof Player
							&& target.getEntityId() != bender.getEntityId()) {
						EntityTools.grab((Player) target, time);
					}
					bPlayer.cooldown(Abilities.EarthGrab);
				}
			}
		}
		return true;
	}
	
	public Player getBender() {
		return bender;
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
		if (bender.getEntityId() == target.getEntityId()) {
			if (System.currentTimeMillis() > time
					+ ((benderTargettedDuration / 20) * 1000)) {
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

		if (!BlockTools.isEarthbendable(bender, loc.add(0, 0, -1).getBlock())) {
			return false;
		}
		if (!BlockTools.isEarthbendable(bender, loc.add(0, 0, +2).getBlock())) {
			return false;
		}
		if (!BlockTools.isEarthbendable(bender, loc.add(-1, 0, -1).getBlock())) {
			return false;
		}
		if (!BlockTools.isEarthbendable(bender, loc.add(+2, 0, 0).getBlock())) {
			return false;
		}
		return true;
	}

	public static void progressAll() {
		List<Integer> toRemove = new LinkedList<Integer>();
		boolean keep;
		for (Integer iD : instances.keySet()) {
			keep = instances.get(iD).progress();
			if (!keep) {
				toRemove.add(iD);
			}
		}

		for (Integer iD : toRemove) {
			instances.get(iD).revertEarthGrab();
			instances.remove(iD);
		}
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
		for (EarthGrab grab : instances.values()) {
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
	public IAbility getParent() {
		return parent;
	}
}

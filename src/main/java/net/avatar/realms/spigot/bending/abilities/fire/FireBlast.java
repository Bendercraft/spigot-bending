package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.water.Plantbending;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@BendingAbility(name="Fire Blast", element=BendingType.Fire)
public class FireBlast implements IAbility {
	private static Map<Integer, FireBlast> instances = new HashMap<Integer, FireBlast>();

	private static int ID = Integer.MIN_VALUE;
	static final int maxticks = 10000;

	@ConfigurationParameter("Speed")
	private static double SPEED = 15;
	
	@ConfigurationParameter("Radius")
	public static double AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Push")
	private static double PUSH_FACTOR = 0.3;
	
	@ConfigurationParameter("Can-Power-Furnace")
	private static boolean POWER_FURNACE = true;
	
	@ConfigurationParameter("Dissipates")
	static boolean DISSIPATES = false;
	
	@ConfigurationParameter("Damage")
	private static int DAMAGE = 6;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 25;
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 1000;
	
	public static byte full = 0x0;

	private Location location;
	private List<Block> safe = new LinkedList<Block>();
	private Location origin;
	private Vector direction;
	private Player player;
	private int id;
	private double speedfactor;
	private int ticks = 0;
	private int damage = DAMAGE;
	double range = RANGE;
	private IAbility parent;

	public FireBlast(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.FireBlast))
			return;

		if (player.getEyeLocation().getBlock().isLiquid()
				|| FireBall.isCharging(player)) {
			return;
		}
		range = PluginTools.firebendingDayAugment(range, player.getWorld());
		this.player = player;
		location = player.getEyeLocation();
		origin = player.getEyeLocation();
		direction = player.getEyeLocation().getDirection().normalize();
		location = location.add(direction.clone());
		id = ID;
		instances.put(id, this);
		bPlayer.cooldown(Abilities.FireBlast, COOLDOWN);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	public FireBlast(Location location, Vector direction, Player player,
			int damage, List<Block> safeblocks, IAbility parent) {
		this.parent = parent;
		if (location.getBlock().isLiquid()) {
			return;
		}
		safe = safeblocks;
		range = PluginTools.firebendingDayAugment(range, player.getWorld());
		this.player = player;
		this.location = location.clone();
		origin = location.clone();
		this.direction = direction.clone().normalize();
		this.damage *= 1.5;
		id = ID;
		instances.put(id, this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.Blaze, location)) {
			return false;
		}

		speedfactor = SPEED * (Bending.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			return false;
		}

		Block block = location.getBlock();
		if (BlockTools.isSolid(block) || block.isLiquid()) {
			if ((block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
					&& POWER_FURNACE) {
				BlockState state = block.getState();
				Furnace furnace = (Furnace) state;
				FurnaceInventory inv = furnace.getInventory();
				if (inv.getFuel() == null) {
					ItemStack temp = inv.getSmelting();
					ItemStack tempfuel = new ItemStack(Material.SAPLING, 1);
					ItemStack tempsmelt = new ItemStack(Material.COBBLESTONE);
					inv.setFuel(tempfuel);
					inv.setSmelting(tempsmelt);
					state.update(true);
					inv.setSmelting(temp);
					state.update(true);
				}
			} else if (FireStream.isIgnitable(player,
					block.getRelative(BlockFace.UP))) {
				ignite(location);
			}
			return false;
		}

		if (location.distance(origin) > range) {
			return false;
		}

		PluginTools.removeSpouts(location, player);

		double radius = FireBlast.AFFECTING_RADIUS;
		Player source = player;
		if (EarthBlast.annihilateBlasts(location, radius, source)
				|| WaterManipulation.annihilateBlasts(location, radius, source)
				|| FireBlast.shouldAnnihilateBlasts(location, radius, source,
						false)) {
			return false;
		}

		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(
				location, AFFECTING_RADIUS)) {
			boolean result = affect(entity);
			// If result is true, do not return here ! we need to iterate fully !
			if (result == false) {
				return false;
			}
		}

		// Advance location
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0,
				(int) range);
		location = location.add(direction.clone().multiply(speedfactor));

		return true;
	}

	private void ignite(Location location) {
		for (Block block : BlockTools.getBlocksAroundPoint(location,
				AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(player, block) && !safe.contains(block)) {
				if (BlockTools.isPlant(block))
					new Plantbending(block, this);
				block.setType(Material.FIRE);
				if (DISSIPATES) {
					FireStream.addIgnitedBlock(block, player,
							System.currentTimeMillis());
				}
			}
		}
	}

	public static void progressAll() {
		List<FireBlast> toRemove = new LinkedList<FireBlast>();
		for (FireBlast fireblast : instances.values()) {
			boolean keep = fireblast.progress();
			if (!keep) {
				toRemove.add(fireblast);
			}
		}
		for (FireBlast fireblast : toRemove) {
			fireblast.remove();
		}
	}

	private void remove() {
		instances.remove(this.id);
	}

	private boolean affect(LivingEntity entity) {
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return false;
		}
		if (entity.getEntityId() != player.getEntityId()) {
			if (AvatarState.isAvatarState(player)) {
				entity.setVelocity(direction.clone().multiply(
						AvatarState.getValue(PUSH_FACTOR)));
			} else {
				entity.setVelocity(direction.clone().multiply(PUSH_FACTOR));
			}
			entity.setFireTicks(50);
			EntityTools.damageEntity(player, entity, PluginTools.firebendingDayAugment((double) damage,
							entity.getWorld()));
			new Enflamed(entity, player, this);
			return false;
		}
		return true;
	}

	public static void removeFireBlastsAroundPoint(Location location,
			double radius) {
		List<Integer> toRemove = new ArrayList<Integer>();
		for (int id : instances.keySet()) {
			Location fireblastlocation = instances.get(id).location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					toRemove.add(id);
			}
		}
		FireBall.removeFireballsAroundPoint(location, radius);
	}

	private static boolean shouldAnnihilateBlasts(Location location,
			double radius, Player source, boolean remove) {
		boolean broke = false;
		List<FireBlast> toRemove = new ArrayList<FireBlast>();
		for (int id : instances.keySet()) {
			FireBlast blast = instances.get(id);
			Location fireblastlocation = blast.location;
			if (location.getWorld() == fireblastlocation.getWorld()
					&& !blast.player.equals(source)) {
				if (location.distance(fireblastlocation) <= radius) {
					toRemove.add(blast);
					broke = true;
				}
			}
		}
		if (FireBall.annihilateBlasts(location, radius, source))
			broke = true;
		if (remove) {
			for (FireBlast fireblast : toRemove) {
				fireblast.remove();
			}
		}
		return broke;
	}

	public static boolean annihilateBlasts(Location location, double radius,
			Player source) {
		return shouldAnnihilateBlasts(location, radius, source, true);
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

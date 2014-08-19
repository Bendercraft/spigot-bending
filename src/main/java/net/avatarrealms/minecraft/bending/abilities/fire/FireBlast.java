package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.water.Plantbending;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FireBlast implements IAbility {
	private static Map<Integer, FireBlast> instances = new HashMap<Integer, FireBlast>();

	private static int ID = Integer.MIN_VALUE;
	static final int maxticks = 10000;

	private static double speed = ConfigManager.fireBlastSpeed;
	public static double affectingradius = 2;
	private static double pushfactor = ConfigManager.fireBlastPush;
	private static boolean canPowerFurnace = true;
	static boolean dissipate = ConfigManager.fireBlastDissipate;
	public static byte full = 0x0;

	private Location location;
	private List<Block> safe = new LinkedList<Block>();
	private Location origin;
	private Vector direction;
	private Player player;
	private int id;
	private double speedfactor;
	private int ticks = 0;
	private int damage = ConfigManager.fireBlastDamage;
	double range = ConfigManager.fireBlastRange;
	private IAbility parent;

	public FireBlast(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.FireBlast))
			return;

		if (player.getEyeLocation().getBlock().isLiquid()
				|| Fireball.isCharging(player)) {
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
		bPlayer.cooldown(Abilities.FireBlast);
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
		// TODO : Make it redirectable EDIT: Nope, will make another skill
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (Tools.isRegionProtectedFromBuild(player, Abilities.Blaze, location)) {
			return false;
		}

		speedfactor = speed * (Bending.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			return false;
		}

		Block block = location.getBlock();
		if (BlockTools.isSolid(block) || block.isLiquid()) {
			if ((block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
					&& canPowerFurnace) {
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

		double radius = FireBlast.affectingradius;
		Player source = player;
		if (EarthBlast.annihilateBlasts(location, radius, source)
				|| WaterManipulation.annihilateBlasts(location, radius, source)
				|| FireBlast.shouldAnnihilateBlasts(location, radius, source,
						false)) {
			return false;
		}

		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(
				location, affectingradius)) {
			boolean result = affect(entity);

			if (((entity instanceof Player) || (entity instanceof Monster))
					&& (entity.getEntityId() != player.getEntityId())) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer != null) {
					bPlayer.earnXP(BendingType.Fire, this);
				}
			}
			// If result is true, do not return here ! we need to iterate fully
			// !
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
				affectingradius)) {
			if (FireStream.isIgnitable(player, block) && !safe.contains(block)) {
				if (BlockTools.isPlant(block))
					new Plantbending(block, this);
				block.setType(Material.FIRE);
				if (dissipate) {
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
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (entity.getEntityId() != player.getEntityId()) {
			if (AvatarState.isAvatarState(player)) {
				entity.setVelocity(direction.clone().multiply(
						AvatarState.getValue(pushfactor)));
			} else {
				entity.setVelocity(direction.clone().multiply(pushfactor));
			}
			entity.setFireTicks(50);
			EntityTools.damageEntity(player, entity, bPlayer.getCriticalHit(
					BendingType.Fire,
					PluginTools.firebendingDayAugment((double) damage,
							entity.getWorld())));
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
		Fireball.removeFireballsAroundPoint(location, radius);
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
		if (Fireball.annihilateBlasts(location, radius, source))
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

	public static String getDescription() {
		return "FireBlast is the most fundamental bending technique of a firebender. "
				+ "To use, simply left-click in a direction. A blast of fire will be created at your fingertips. "
				+ "If this blast contacts an enemy, it will dissipate and engulf them in flames, "
				+ "doing additional damage and knocking them back slightly. "
				+ "If the blast hits terrain, it will ignite the nearby area. "
				+ "Additionally, if you hold sneak, you will charge up the fireblast. "
				+ "If you release it when it's charged, it will instead launch a powerful "
				+ "fireball that explodes on contact.";
	}

	@Override
	public int getBaseExperience() {
		return 5;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

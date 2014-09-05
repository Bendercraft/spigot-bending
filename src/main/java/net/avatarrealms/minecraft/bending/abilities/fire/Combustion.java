package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.water.Plantbending;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Combustion implements IAbility {
	private static Map<Player, Combustion> instances = new HashMap<Player, Combustion>();

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
	private double speedfactor;
	private int ticks = 0;
	private int damage = ConfigManager.fireBlastDamage;
	double range = ConfigManager.fireBlastRange;
	private IAbility parent;

	public Combustion(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Combustion))
			return;
		
		range = PluginTools.firebendingDayAugment(range, player.getWorld());
		this.player = player;
		location = player.getEyeLocation();
		origin = player.getEyeLocation();
		direction = player.getEyeLocation().getDirection().normalize();
		location = location.add(direction.clone());
		instances.put(player, this);
		bPlayer.cooldown(Abilities.Combustion);
	}

	private boolean progress() {
		// TODO : Make it redirectable EDIT: Nope, will make another skill
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (Tools.isRegionProtectedFromBuild(player, Abilities.Combustion, location)) {
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

		double radius = Combustion.affectingradius;
		Player source = player;
		if (EarthBlast.annihilateBlasts(location, radius, source)
				|| WaterManipulation.annihilateBlasts(location, radius, source)
				|| Combustion.shouldAnnihilateBlasts(location, radius, source,
						false)) {
			return false;
		}

		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(
				location, affectingradius)) {
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
		List<Combustion> toRemove = new LinkedList<Combustion>();
		for (Combustion fireblast : instances.values()) {
			boolean keep = fireblast.progress();
			if (!keep) {
				toRemove.add(fireblast);
			}
		}
		for (Combustion fireblast : toRemove) {
			fireblast.remove();
		}
	}

	private void remove() {
		instances.remove(this.player);
	}

	private boolean affect(LivingEntity entity) {
		if (entity.getEntityId() != player.getEntityId()) {
			if (AvatarState.isAvatarState(player)) {
				entity.setVelocity(direction.clone().multiply(
						AvatarState.getValue(pushfactor)));
			} else {
				entity.setVelocity(direction.clone().multiply(pushfactor));
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
		List<Combustion> toRemove = new ArrayList<Combustion>();
		for (Combustion combustion : instances.values()) {
			Location fireblastlocation = combustion.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					toRemove.add(combustion);
			}
		}
		Fireball.removeFireballsAroundPoint(location, radius);
	}

	private static boolean shouldAnnihilateBlasts(Location location,
			double radius, Player source, boolean remove) {
		boolean broke = false;
		List<Combustion> toRemove = new ArrayList<Combustion>();
		for (Combustion combustion : instances.values()) {
			Location fireblastlocation = combustion.location;
			if (location.getWorld() == fireblastlocation.getWorld()
					&& !combustion.player.equals(source)) {
				if (location.distance(fireblastlocation) <= radius) {
					toRemove.add(combustion);
					broke = true;
				}
			}
		}
		if (Fireball.annihilateBlasts(location, radius, source))
			broke = true;
		if (remove) {
			for (Combustion fireblast : toRemove) {
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

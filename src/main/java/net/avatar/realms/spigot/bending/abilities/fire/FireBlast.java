package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPathType;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Fire Blast", element=BendingType.Fire)
public class FireBlast extends ActiveAbility {
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
	
	public FireBlast(Player player) {
		super (player, null);
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		
		if (bender.isOnCooldown(Abilities.FireBlast)) {
			return;
		}
		
		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		this.range = PluginTools.firebendingDayAugment(this.range, player.getWorld());
		this.player = player;
		this.location = player.getEyeLocation();
		this.origin = player.getEyeLocation();
		this.direction = player.getEyeLocation().getDirection().normalize();
		this.location = this.location.add(this.direction.clone());
		this.id = ID;
		instances.put(this.id, this);
		bender.cooldown(Abilities.FireBlast, COOLDOWN);
		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;

		if(bender.hasPath(BendingPathType.Nurture)) {
			this.damage *= 0.8;
		}
		if(bender.hasPath(BendingPathType.Lifeless)) {
			this.damage *= 1.1;
		}
	}
	
	public FireBlast(Player player, IAbility parent, Location location, Vector direction, int damage, List<Block> safeblocks) {
		super(player, parent);
		if (location.getBlock().isLiquid()) {
			return;
		}
		this.safe = safeblocks;
		this.range = PluginTools.firebendingDayAugment(this.range, player.getWorld());
		this.player = player;
		this.location = location.clone();
		this.origin = location.clone();
		this.direction = direction.clone().normalize();
		this.damage *= 1.5;
		this.id = ID;
		instances.put(this.id, this);
		if (ID == Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;
	}
	
	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.Blaze, this.location)) {
			return false;
		}
		
		this.speedfactor = SPEED * (Bending.time_step / 1000.);
		
		this.ticks++;
		
		if (this.ticks > maxticks) {
			return false;
		}
		
		Block block = this.location.getBlock();
		if (BlockTools.isSolid(block) || block.isLiquid()) {
			if (((block.getType() == Material.FURNACE) || (block.getType() == Material.BURNING_FURNACE))
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
			} else if (FireStream.isIgnitable(this.player,
					block.getRelative(BlockFace.UP))) {
				ignite(this.location);
			}
			return false;
		}
		
		if (this.location.distance(this.origin) > this.range) {
			return false;
		}
		
		PluginTools.removeSpouts(this.location, this.player);
		
		double radius = FireBlast.AFFECTING_RADIUS;
		Player source = this.player;
		if (EarthBlast.annihilateBlasts(this.location, radius, source)
				|| WaterManipulation.annihilateBlasts(this.location, radius, source)
				|| FireBlast.shouldAnnihilateBlasts(this.location, radius, source,
						false)) {
			return false;
		}
		
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(
				this.location, AFFECTING_RADIUS)) {
			boolean result = affect(entity);
			// If result is true, do not return here ! we need to iterate fully !
			if (result == false) {
				return false;
			}
		}
		
		// Advance location
		this.location.getWorld().playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 0,
				(int) this.range);
		this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
		
		return true;
	}
	
	private void ignite(Location location) {
		for (Block block : BlockTools.getBlocksAroundPoint(location, AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(this.player, block) && !this.safe.contains(block)) {
				if (BlockTools.isPlant(block)) {
					//TODO : new Plantbending(block, this);
				}
				block.setType(Material.FIRE);
				if (DISSIPATES) {
					FireStream.addIgnitedBlock(block, this.player,
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
	
	@Override
	public void remove() {
		this.bender.cooldown(Abilities.FireBlast, COOLDOWN);
		super.remove();
	}
	
	private boolean affect(LivingEntity entity) {
		if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return false;
		}
		if (entity.getEntityId() != this.player.getEntityId()) {
			if (AvatarState.isAvatarState(this.player)) {
				entity.setVelocity(this.direction.clone().multiply(
						AvatarState.getValue(PUSH_FACTOR)));
			} else {
				entity.setVelocity(this.direction.clone().multiply(PUSH_FACTOR));
			}
			EntityTools.damageEntity(this.player, entity, PluginTools.firebendingDayAugment(this.damage,
					entity.getWorld()));
			new Enflamed(this.player, entity, 1, this);
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
				if (location.distance(fireblastlocation) <= radius) {
					toRemove.add(id);
				}
			}
		}
	}
	
	private static boolean shouldAnnihilateBlasts(Location location,
			double radius, Player source, boolean remove) {
		boolean broke = false;
		List<FireBlast> toRemove = new ArrayList<FireBlast>();
		for (int id : instances.keySet()) {
			FireBlast blast = instances.get(id);
			Location fireblastlocation = blast.location;
			if ((location.getWorld() == fireblastlocation.getWorld())
					&& !blast.player.equals(source)) {
				if (location.distance(fireblastlocation) <= radius) {
					toRemove.add(blast);
					broke = true;
				}
			}
		}
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
	public Object getIdentifier () {
		return this.id;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.FireBlast;
	}
	
}

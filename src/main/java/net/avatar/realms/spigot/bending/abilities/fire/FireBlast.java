package net.avatar.realms.spigot.bending.abilities.fire;

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
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = FireBlast.NAME, element = BendingElement.FIRE)
public class FireBlast extends BendingActiveAbility {
	public final static String NAME = "FireBlast";
	
	private static int ID = Integer.MIN_VALUE;

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
	private static long COOLDOWN = 950;

	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 3500;

	@ConfigurationParameter("Charged-Cooldown")
	private static long CHARGED_COOLDOWN = 1250;

	private Location location;
	private List<Block> safe;
	private Location origin;
	private Vector direction;
	private int id;
	private double speedfactor;
	private int damage = DAMAGE;
	double range = RANGE;

	public FireBlast(RegisteredAbility register, Player player) {
		super(register, player);

		if (this.bender.hasPath(BendingPath.NURTURE)) {
			this.damage *= 0.8;
		}
		if (this.bender.hasPath(BendingPath.LIFELESS)) {
			this.damage *= 1.1;
		}

		this.safe = new LinkedList<Block>();
		this.speedfactor = SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);
		this.range = PluginTools.firebendingDayAugment(this.range, player.getWorld());
		this.location = this.player.getEyeLocation();
		this.id = ID++;
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PREPARING);
		}
		return false;
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			launchSingle();
			this.bender.cooldown(NAME, COOLDOWN);
			return false;
		} else if(getState() == BendingAbilityState.PREPARING) {
			launchSingle();
			this.bender.cooldown(NAME, COOLDOWN);
			return false;
		} else if(getState() == BendingAbilityState.PREPARED) {
			this.damage *= 1.30;
			this.range *= 1.20;
			launchSingle();
			Vector perpDir = this.direction.clone();
			double tempZ = -perpDir.getZ();
			perpDir.setZ(perpDir.getX());
			perpDir.setY(0);
			perpDir.setX(tempZ);
			perpDir.multiply(1.5);
			//List<Block> safes = new LinkedList<Block>();
			//new FireBlast(this.player, this, this.location.clone().add(perpDir), this.direction, this.damage, safes);
			//new FireBlast(this.player, this, this.location.clone().subtract(perpDir), this.direction, this.damage, safes);
			this.bender.cooldown(NAME, CHARGED_COOLDOWN);
			return false;
		}
		return true;
	}

	private void launchSingle() {
		this.location = this.player.getEyeLocation();
		this.origin = this.player.getEyeLocation();
		this.direction = this.player.getEyeLocation().getDirection().normalize();
		this.location = this.location.add(this.direction.clone());
		setState(BendingAbilityState.PROGRESSING);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isLocationProtectedFromBending(this.player, NAME, this.location)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if (getState() == BendingAbilityState.PREPARING) {
			if ((now - this.startedTime) > CHARGE_TIME) {
				setState(BendingAbilityState.PREPARED);
			}
			return;
		} else if (getState() == BendingAbilityState.PREPARED) {
			Location location = this.player.getEyeLocation();
			//location.getWorld().playEffect(location, Effect.FLAME, Tools.getIntCardinalDirection(location.getDirection()), 3);
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4, 3);
			if(!this.player.isSneaking() || !this.bender.getAbility().equals(NAME)) {
				remove();
				return;
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
			if (this.location.distance(this.origin) > this.range) {
				remove();
				return;
			}
			Block block = this.location.getBlock();
			if (BlockTools.isSolid(block) || block.isLiquid()) {
				if (((block.getType() == Material.FURNACE) || (block.getType() == Material.BURNING_FURNACE)) && POWER_FURNACE) {
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
				} else if (FireStream.isIgnitable(this.player, block.getRelative(BlockFace.UP))) {
					ignite(this.location);
				}
				remove();
				return;
			}

			PluginTools.removeSpouts(this.location, this.player);

			double radius = FireBlast.AFFECTING_RADIUS;
			Player source = this.player;
			if (EarthBlast.annihilateBlasts(this.location, radius, source) || WaterManipulation.annihilateBlasts(this.location, radius, source) || FireBlast.shouldAnnihilateBlasts(this.location, radius, source, false)) {
				remove();
				return;
			}

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, AFFECTING_RADIUS)) {
				boolean result = affect(entity);
				// If result is true, do not return here ! we need to iterate
				// fully !
				if (result == false) {
					remove();
					return;
				}
			}

			this.location.getWorld().playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 0, (int) this.range);
			this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
		}
	}

	private void ignite(Location location) {
		for (Block block : BlockTools.getBlocksAroundPoint(location, AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(this.player, block) && !this.safe.contains(block)) {
				block.setType(Material.FIRE);
				if (DISSIPATES) {
					FireStream.addIgnitedBlock(block, this.player, System.currentTimeMillis());
				}
			}
		}
	}

	private boolean affect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return false;
		}
		if (entity.getEntityId() != this.player.getEntityId()) {
			if (AvatarState.isAvatarState(this.player)) {
				entity.setVelocity(this.direction.clone().multiply(AvatarState.getValue(PUSH_FACTOR)));
			} else {
				entity.setVelocity(this.direction.clone().multiply(PUSH_FACTOR));
			}
			Enflamed.enflame(this.player, entity, 1);
			EntityTools.damageEntity(this.player, entity, PluginTools.firebendingDayAugment(this.damage, entity.getWorld()));
			return false;
		}
		return true;
	}

	public static void removeFireBlastsAroundPoint(Location location, double radius) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		for (BendingAbility ability : instances.values()) {
			FireBlast blast = (FireBlast) ability;
			Location loc = blast.location;
			if (location.getWorld() == loc.getWorld()) {
				if (location.distance(loc) <= radius) {
					blast.remove();
				}
			}
		}
	}

	public static boolean shouldAnnihilateBlasts(Location location, double radius, Player source, boolean remove) {
		boolean broke = false;
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		for (BendingAbility ability : instances.values()) {
			FireBlast blast = (FireBlast) ability;
			Location loc = blast.location;
			if ((location.getWorld() == loc.getWorld()) && !blast.player.equals(source)) {
				if (location.distance(loc) <= radius) {
					blast.remove();
					broke = true;
				}
			}
		}
		return broke;
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		return shouldAnnihilateBlasts(location, radius, source, true);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (this.player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public void stop() {
		
	}

}

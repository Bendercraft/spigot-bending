package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPath;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

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
	static boolean DISSIPATES = true;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 5;

	@ConfigurationParameter("Range")
	private static int RANGE = 25;
	
	@ConfigurationParameter("Power")
	private static int POWER = 1;

	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 3500;
	
	@ConfigurationParameter("Dissipate")
	private static long DISSIPATE = 1000;

	private Location location;
	private List<Block> safe;
	private Location origin;
	private Vector direction;
	private int id;
	private double speedfactor;
	private double damage = DAMAGE;
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
		this.location = this.player.getEyeLocation();
		this.id = ID++;
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, POWER)) {
			return false;
		}

		return true;
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
		if(isState(BendingAbilityState.START) || isState(BendingAbilityState.PREPARING)) {
			launch();
			return false;
		} else if(getState() == BendingAbilityState.PREPARED) {
			this.damage *= 1.30;
			this.range *= 1.20;
			launch();
			return false;
		}
		return true;
	}

	private void launch() {
		this.location = this.player.getEyeLocation();
		this.origin = this.player.getEyeLocation();
		this.direction = this.player.getEyeLocation().getDirection().normalize();
		this.location = this.location.add(this.direction.clone());
		bender.fire.consume(NAME, POWER);
		setState(BendingAbilityState.PROGRESSING);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (ProtectionManager.isLocationProtectedFromBending(this.player, register, this.location)) {
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
			Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(0, 0.5, 0);
			player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
			if(!this.player.isSneaking() || !NAME.equals(bender.getAbility())) {
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
				if (affect(entity)) {
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
					FireStream.addIgnitedBlock(block, this.player, DISSIPATE);
				}
			}
		}
	}

	private boolean affect(LivingEntity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return false;
		}
		if (entity == player) {
			return false;
		}
		if (AvatarState.isAvatarState(this.player)) {
			entity.setVelocity(this.direction.clone().multiply(AvatarState.getValue(PUSH_FACTOR)));
		} else {
			entity.setVelocity(this.direction.clone().multiply(PUSH_FACTOR));
		}
		DamageTools.damageEntity(bender, entity, this, damage);
		Enflamed.enflame(this.player, entity, 2, this);
		return true;
	}
	
	public static boolean removeOneAroundPoint(Location location, Player player, double radius) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		for (BendingAbility ability : instances.values()) {
			FireBlast blast = (FireBlast) ability;
			Location loc = blast.location;
			if (location.getWorld() == loc.getWorld()) {
				if (location.distance(loc) <= radius && blast.getPlayer() != player) {
					blast.remove();
					return true;
				}
			}
		}
		return false;
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
	public Object getIdentifier() {
		return this.id;
	}

	@Override
	public void stop() {
		
	}

	public static boolean removeOneAroundPoint(Location location2, double aFFECTING_RADIUS2, Player player) {
		return false;
	}

}

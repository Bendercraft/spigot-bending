package net.bendercraft.spigot.bending.abilities.fire;

import java.util.Map;

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
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
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

	private static final double PARTICLE_SPEED   = 1.0 / 48.0;
	
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
	private static int POWER = 2;

	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 3500;
	
	@ConfigurationParameter("Dissipate")
	private static long DISSIPATE = 1000;
	
	@ConfigurationParameter("Flame-Time")
	private static int FLAME_TIME = 2;
	
	@ConfigurationParameter("Charge-Damage-Factor")
	private static double CHARGE_DAMAGE_FACTOR = 1.3;
	
	@ConfigurationParameter("Charge-Range-Factor")
	private static double CHARGE_RANGE_FACTOR = 1.2;

	private Location location;
	private Location origin;
	private Vector direction;
	private int id;
	private double speedfactor;
	
	
	private double damage;
	private double range;
	private double pushFactor;
	private int flameTime;
	private long chargeTime;
	private int power;

	private double chargeDamageFactor;

	public FireBlast(RegisteredAbility register, Player player) {
		super(register, player);

		this.speedfactor = SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);
		this.location = this.player.getEyeLocation();
		this.id = ID++;
		
		this.power = POWER;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_ENERGY)) {
			this.power -= 1;
		}
		
		this.pushFactor = PUSH_FACTOR;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_PUSHBACK)) {
			this.pushFactor *= 1.3;
		}
		this.flameTime = FLAME_TIME;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_FLAME)) {
			this.flameTime += 1;
		}
		this.chargeTime = CHARGE_TIME;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_CHARGE_TIME_1)) {
			this.chargeTime *= 0.85;
		}
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_CHARGE_TIME_2)) {
			this.chargeTime *= 0.85;
		}
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_CHARGE_TIME_3)) {
			this.chargeTime *= 0.85;
		}
		
		this.chargeDamageFactor = CHARGE_DAMAGE_FACTOR;
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_CHARGE_DAMAGE_1)) {
			this.chargeDamageFactor += 0.15;
		}
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_CHARGE_DAMAGE_2)) {
			this.chargeDamageFactor += 0.15;
		}
		if(bender.hasPerk(BendingPerk.FIRE_FIREBLAST_CHARGE_DAMAGE_3)) {
			this.chargeDamageFactor += 0.15;
		}
		
		this.damage = DAMAGE;
		this.range = RANGE;
		if(bender.hasPerk(BendingPerk.FIRE_OVERLOAD)) {
			this.damage *= 1.1;
			this.range *= 0.95;
		}
		if(bender.hasPerk(BendingPerk.FIRE_SNIPER)) {
			this.damage *= 0.95;
			this.range *= 1.1;
		}
	}
	
	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}
		
		if(!bender.fire.can(NAME, power)) {
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
			this.damage *= chargeDamageFactor;
			this.range *= CHARGE_RANGE_FACTOR;
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
		bender.fire.consume(NAME, power);
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
			if ((now - this.startedTime) > chargeTime) {
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
				if ((block.getType() == Material.FURNACE || block.getType() == Material.SMOKER || block.getType() == Material.BLAST_FURNACE) && POWER_FURNACE) {
					Furnace furnace = (Furnace) block.getState();
					FurnaceInventory inv = furnace.getInventory();
					if (inv.getFuel() == null) {
						inv.setFuel(new ItemStack(Material.OAK_DOOR, 1));
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

			location.getWorld().spawnParticle(Particle.FLAME, location, 7, 0.25, 0.25, 0.25, PARTICLE_SPEED, null, true);
			//this.location.getWorld().playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 0, (int) this.range);
			this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
		}
	}

	private void ignite(Location location) {
		for (Block block : BlockTools.getBlocksAroundPoint(location, AFFECTING_RADIUS)) {
			if (FireStream.isIgnitable(this.player, block)) {
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
		entity.setVelocity(this.direction.clone().multiply(pushFactor));
		DamageTools.damageEntity(bender, entity, this, damage);
		Enflamed.enflame(this.player, entity, flameTime, this);
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

}

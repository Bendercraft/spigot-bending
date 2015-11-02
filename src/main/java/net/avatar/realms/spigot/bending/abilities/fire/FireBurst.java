package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * State Preparing : Player is sneaking but burst is not ready yet State
 * Prepared : Player is sneaking and burst is ready
 *
 * @author Noko
 */
@ABendingAbility(name = "Fire Burst", bind = BendingAbilities.FireBurst, element = BendingElement.Fire)
public class FireBurst extends BendingActiveAbility {
	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 2500;

	@ConfigurationParameter("Damage")
	static int DAMAGE = 3;

	@ConfigurationParameter("Del-Theta")
	static double DELTHETA = 10;

	@ConfigurationParameter("Del-Phi")
	static double DELPHI = 10;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 2500;
	
	@ConfigurationParameter("Blast-Range")
	private static int BLAST_RANGE = 25;
	
	@ConfigurationParameter("Blast-Speed")
	private static double BLAST_SPEED = 15;
	
	@ConfigurationParameter("Blast-Radius")
	public static double BLAST_AFFECTING_RADIUS = 2;
	
	@ConfigurationParameter("Blast-Push")
	private static double BLAST_PUSH_FACTOR = 0.3;

	private long chargetime = CHARGE_TIME;
	private int damage = DAMAGE;
	
	private List<BurstBlast> blasts;

	public FireBurst(Player player) {
		super(player);

		if (Tools.isDay(player.getWorld())) {
			this.chargetime /= Settings.DAY_FACTOR;
		}
		if (AvatarState.isAvatarState(player)) {
			this.chargetime = 0;
		}
		
		blasts = new LinkedList<BurstBlast>();
	}

	@Override
	public boolean sneak() {
		if (getState().equals(BendingAbilityState.Start)) {
			setState(BendingAbilityState.Preparing);
		}
		return false;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.Prepared) {
			coneBurst();
		}
		return false;
	}

	private void coneBurst() {
		Location location = this.player.getEyeLocation();
		List<Block> safeblocks = BlockTools.getBlocksAroundPoint(this.player.getLocation(), 2);
		Vector vector = location.getDirection();
		double angle = Math.toRadians(30);
		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += DELTHETA) {
			double dphi = DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				if (direction.angle(vector) <= angle) {
					blasts.add(new BurstBlast(this.player, this.bender, this, location, direction.normalize(), this.damage, safeblocks));
				}
			}
		}
		setState(BendingAbilityState.Progressing);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if ((EntityTools.getBendingAbility(this.player) != BendingAbilities.FireBurst)) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		if(getState() == BendingAbilityState.Progressing) {
			List<BurstBlast> toRemove = new LinkedList<BurstBlast>();
			for(BurstBlast blast : blasts) {
				if(!blast.progress()) {
					toRemove.add(blast);
				}
			}
			blasts.removeAll(toRemove);
			if(blasts.isEmpty()) {
				remove();
			}
			return;
		}
		
		if (!this.player.isSneaking()) {
			if (getState().equals(BendingAbilityState.Prepared)) {
				sphereBurst();
			}
			return;
		}

		if (getState() != BendingAbilityState.Prepared) {
			if (System.currentTimeMillis() > (this.startedTime + this.chargetime)) {
				setState(BendingAbilityState.Prepared);
			}
		}

		if (getState() == BendingAbilityState.Prepared) {
			Location location = this.player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4, 3);
		}
	}

	private void sphereBurst() {
		Location location = this.player.getEyeLocation();
		List<Block> safeblocks = BlockTools.getBlocksAroundPoint(this.player.getLocation(), 2);

		double x, y, z;
		double r = 1;
		for (double theta = 0; theta <= 180; theta += DELTHETA) {
			double dphi = DELPHI / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				blasts.add(new BurstBlast(this.player, this.bender, this, location, direction.normalize(), this.damage, safeblocks));
			}
		}
		
		setState(BendingAbilityState.Progressing);
	}

	@Override
	public void stop() {
		this.bender.cooldown(BendingAbilities.FireBurst, COOLDOWN);
	}

	public boolean isCharged() {
		return getState() == BendingAbilityState.Prepared;
	}

	public static boolean isFireBursting(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireBurst);
		return instances.containsKey(player);
	}

	public static FireBurst getFireBurst(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireBurst);
		return (FireBurst) instances.get(player);
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireBurst);
		if (instances.containsKey(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
	
	private class BurstBlast {

		private Player player;
		private List<Block> safe;
		private Location location;
		private Location origin;
		private Vector direction;
		private BendingPlayer bender;
		private double range;
		private double damage;
		private double speedfactor;

		public BurstBlast(Player player, BendingPlayer bender, BendingAbility parent, Location location, Vector direction, int damage, List<Block> safeblocks) {
			this.player = player;
			this.bender = bender;
			this.safe = safeblocks;
			this.range = PluginTools.firebendingDayAugment(BLAST_RANGE, player.getWorld());
			this.location = location.clone();
			this.origin = location.clone();
			this.direction = direction.clone().normalize();
			this.damage = damage*1.5;
			this.speedfactor = BLAST_SPEED * (Bending.getInstance().getManager().getTimestep() / 1000.);
			if (this.bender.hasPath(BendingPath.Nurture)) {
				this.damage *= 0.8;
			}
			if (this.bender.hasPath(BendingPath.Lifeless)) {
				this.damage *= 1.1;
			}
		}
		
		public boolean progress() {
			if (this.location.distance(this.origin) > this.range) {
				return false;
			}
			Block block = this.location.getBlock();
			if (BlockTools.isSolid(block) || block.isLiquid()) {
				if (FireStream.isIgnitable(this.player, block.getRelative(BlockFace.UP))) {
					ignite(this.location);
				}
				return false;
			}

			PluginTools.removeSpouts(this.location, this.player);

			double radius = FireBlast.AFFECTING_RADIUS;
			Player source = this.player;
			if (EarthBlast.annihilateBlasts(this.location, radius, source) || WaterManipulation.annihilateBlasts(this.location, radius, source) || FireBlast.shouldAnnihilateBlasts(this.location, radius, source, false)) {
				return false;
			}

			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(this.location, BLAST_AFFECTING_RADIUS)) {
				boolean result = affect(entity);
				// If result is true, do not return here ! we need to iterate
				// fully !
				if (result == false) {
					return false;
				}
			}

			this.location.getWorld().playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 0, (int) this.range);
			this.location = this.location.add(this.direction.clone().multiply(this.speedfactor));
			
			return true;
		}
		
		private void ignite(Location location) {
			for (Block block : BlockTools.getBlocksAroundPoint(location, BLAST_AFFECTING_RADIUS)) {
				if (FireStream.isIgnitable(this.player, block) && !this.safe.contains(block)) {
					block.setType(Material.FIRE);
				}
			}
		}

		private boolean affect(LivingEntity entity) {
			if (ProtectionManager.isEntityProtected(entity)) {
				return false;
			}
			if (entity.getEntityId() != this.player.getEntityId()) {
				if (AvatarState.isAvatarState(this.player)) {
					entity.setVelocity(this.direction.clone().multiply(AvatarState.getValue(BLAST_PUSH_FACTOR)));
				} else {
					entity.setVelocity(this.direction.clone().multiply(BLAST_PUSH_FACTOR));
				}
				new Enflamed(this.player, entity, 1);
				EntityTools.damageEntity(this.player, entity, PluginTools.firebendingDayAugment(this.damage, entity.getWorld()));
				return false;
			}
			return true;
		}
	}
}

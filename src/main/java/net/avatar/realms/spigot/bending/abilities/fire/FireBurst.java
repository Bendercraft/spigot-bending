package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.Tools;

/**
 * State Preparing : Player is sneaking but burst is not ready yet
 * State Prepared : Player is sneaking and burst is ready
 *
 * @author Noko
 */
@BendingAbility(name="Fire Burst", element=BendingType.Fire)
public class FireBurst extends ActiveAbility {
	@ConfigurationParameter("Charge-Time")
	private static long CHARGE_TIME = 2500;

	@ConfigurationParameter("Damage")
	static int DAMAGE = 3;

	@ConfigurationParameter("Del-Theta")
	static double DELTHETA = 10;

	@ConfigurationParameter("Del-Phi")
	static double DELPHI = 10;

	@ConfigurationParameter ("Cooldown")
	private static long COOLDOWN = 2500;
	
	private long chargetime = CHARGE_TIME;
	private int damage = DAMAGE;
	
	public FireBurst (Player player) {
		super(player, null);
		
		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (Tools.isDay(player.getWorld())) {
			this.chargetime /= Settings.DAY_FACTOR;
		}
		if (AvatarState.isAvatarState(player)) {
			this.chargetime = 0;
		}
	}
	
	@Override
	public boolean sneak () {
		if (this.state.equals(AbilityState.CanStart)) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Preparing);
			return false;
		}

		return false;
	}

	@Override
	public boolean swing () {
		if (this.state == AbilityState.Prepared) {
			coneBurst();
			return false;
		}

		return true;
	}
	
	private void coneBurst () {
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
					new FireBlast(this.player, this, location, direction.normalize(), this.damage, safeblocks);
				}
			}
		}
		setState(AbilityState.Ended);
	}

	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}
		
		if ((EntityTools.getBendingAbility(this.player) != Abilities.FireBurst)) {
			return false;
		}

		if (!this.player.isSneaking()) {
			if (this.state.equals(AbilityState.Prepared)) {
				sphereBurst();
			}
			return false;
		}

		if (this.state != AbilityState.Prepared) {
			if (System.currentTimeMillis() > (this.startedTime + this.chargetime)) {
				setState(AbilityState.Prepared);
			}
		}

		if (this.state == AbilityState.Prepared) {
			Location location = this.player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4, 3);
		}
		
		return true;
	}
	
	private void sphereBurst () {
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
				new FireBlast(this.player, this, location, direction.normalize(), this.damage, safeblocks);
			}
		}
		setState(AbilityState.Ended);
	}

	@Override
	public void remove () {
		this.bender.cooldown(Abilities.FireBurst, COOLDOWN);
		super.remove();
	}

	public boolean isCharged() {
		return this.state == AbilityState.Prepared;
	}

	public static boolean isFireBursting(Player player) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.FireBurst);
		return instances.containsKey(player);
	}
	public static FireBurst getFireBurst(Player player) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.FireBurst);
		return (FireBurst) instances.get(player);
	}
	
	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.FireBurst);
		if (instances.containsKey(this.player)) {
			return false;
		}
		
		return true;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.FireBurst;
	}
}

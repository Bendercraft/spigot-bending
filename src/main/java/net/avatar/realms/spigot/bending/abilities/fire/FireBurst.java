package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
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
@BendingAbility(name="Fire Burst", element=BendingElement.Fire)
public class FireBurst extends BendingActiveAbility {
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
		
		if (this.state.isBefore(BendingAbilityState.CanStart)) {
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
		if (this.state.equals(BendingAbilityState.CanStart)) {
			AbilityManager.getManager().addInstance(this);
			setState(BendingAbilityState.Preparing);
			return false;
		}

		return false;
	}

	@Override
	public boolean swing () {
		if (this.state == BendingAbilityState.Prepared) {
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
		setState(BendingAbilityState.Ended);
	}

	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}
		
		if ((EntityTools.getBendingAbility(this.player) != BendingAbilities.FireBurst)) {
			return false;
		}

		if (!this.player.isSneaking()) {
			if (this.state.equals(BendingAbilityState.Prepared)) {
				sphereBurst();
			}
			return false;
		}

		if (this.state != BendingAbilityState.Prepared) {
			if (System.currentTimeMillis() > (this.startedTime + this.chargetime)) {
				setState(BendingAbilityState.Prepared);
			}
		}

		if (this.state == BendingAbilityState.Prepared) {
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
		setState(BendingAbilityState.Ended);
	}

	@Override
	public void remove () {
		this.bender.cooldown(BendingAbilities.FireBurst, COOLDOWN);
		super.remove();
	}

	public boolean isCharged() {
		return this.state == BendingAbilityState.Prepared;
	}

	public static boolean isFireBursting(Player player) {
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireBurst);
		return instances.containsKey(player);
	}
	public static FireBurst getFireBurst(Player player) {
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireBurst);
		return (FireBurst) instances.get(player);
	}
	
	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireBurst);
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
	public BendingAbilities getAbilityType () {
		return BendingAbilities.FireBurst;
	}
}

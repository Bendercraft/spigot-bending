package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;

@BendingAbility(name="Fire Burst", element=BendingType.Fire)
public class FireBurstCone extends ActiveAbility {

	private int damage = FireBurst.DAMAGE;
	private double deltheta = FireBurst.DELTHETA;
	private double delphi = FireBurst.DELPHI;
	
	public FireBurstCone(Player player) {
		super (player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.FireBurst)) {
			return;
		}

		if (!FireBurst.isFireBursting(player)) {
			return;
		}

		FireBurst burst = FireBurst.getFireBurst(player);

		if (burst.isCharged()) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = BlockTools.getBlocksAroundPoint(
					player.getLocation(), 2);
			Vector vector = location.getDirection();
			double angle = Math.toRadians(30);
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += this.deltheta) {
				double dphi = this.delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					if (direction.angle(vector) <= angle) {
						new FireBlast(player, this, location,
								direction.normalize(), this.damage, safeblocks);
					}
				}
			}
			burst.remove();
		}
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

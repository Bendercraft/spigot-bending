package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.List;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="Fire Burst", element=BendingType.Fire)
public class FireBurstSphere implements IAbility {
	
	private int damage = FireBurst.DAMAGE;
	private double deltheta = FireBurst.DELTHETA;
	private double delphi = FireBurst.DELPHI;
	
	private IAbility parent;

	public FireBurstSphere(Player player, IAbility parent) {
		this.parent = parent;
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
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					new FireBlast(location, direction.normalize(), player,
							damage, safeblocks, this);
				}
			}
			burst.remove();
		}
	}
	
	@Override
	public IAbility getParent() {
		return parent;
	}
}

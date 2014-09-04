package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.List;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FireBurstCone implements IAbility {
	private int damage = 3;
	private double deltheta = 10;
	private double delphi = 10;
	private IAbility parent;

	public FireBurstCone(Player player, IAbility parent) {
		this.parent = parent;
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.FireBurst))
			return;
		
		if (!FireBurst.isFireBursting(player))
			return;
		
		FireBurst burst = FireBurst.getFireBurst(player);
		
		if (burst.isCharged()) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = BlockTools.getBlocksAroundPoint(
					player.getLocation(), 2);
			Vector vector = location.getDirection();
			double angle = Math.toRadians(30);
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
					if (direction.angle(vector) <= angle) {
						new FireBlast(location, direction.normalize(), player,
								damage, safeblocks, this);
					}
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

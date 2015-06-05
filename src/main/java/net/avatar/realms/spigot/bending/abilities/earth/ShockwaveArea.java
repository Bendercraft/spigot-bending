package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.IAbility;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ShockwaveArea implements IAbility {
	private IAbility parent;

	public ShockwaveArea(Player player, IAbility parent) {
		this.parent = parent;
		
		if (Shockwave.isShockwaving(player)) {
			Shockwave wave = Shockwave.getShockwave(player);
			if (wave.isCharged()) {
				double dtheta = 360. / (2 * Math.PI * Ripple.radius) - 1;
				for (double theta = 0; theta < 360; theta += dtheta) {
					double rtheta = Math.toRadians(theta);
					Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
					new Ripple(player, vector.normalize(), this);
				}
				wave.remove();
			}
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

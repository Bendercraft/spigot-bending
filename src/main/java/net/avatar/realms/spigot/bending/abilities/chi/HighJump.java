package net.avatar.realms.spigot.bending.abilities.chi;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class HighJump implements IAbility {

	private double jumpheight = ConfigManager.jumpHeight;
	private IAbility parent;

	public HighJump (Player p, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);

		if (bPlayer.isOnCooldown(Abilities.HighJump)) {
			return;
		}
		jump(p);
		bPlayer.cooldown(Abilities.HighJump);
	}

	private void jump (Player p) {
		if (!BlockTools.isSolid(p.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return;
		}
		Vector vec = p.getVelocity();
		vec.setY(this.jumpheight);
		p.setVelocity(vec);
		return;
	}

	@Override
	public IAbility getParent () {
		return this.parent;
	}
}

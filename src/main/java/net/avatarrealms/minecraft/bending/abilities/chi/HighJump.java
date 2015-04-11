package net.avatarrealms.minecraft.bending.abilities.chi;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;

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

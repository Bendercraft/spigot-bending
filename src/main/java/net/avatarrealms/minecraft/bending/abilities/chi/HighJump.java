package net.avatarrealms.minecraft.bending.abilities.chi;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HighJump implements IAbility {

	private double jumpheight = ConfigManager.jumpHeight;
	private IAbility parent;

	public HighJump(Player p, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);

		if (bPlayer.isOnCooldown(Abilities.HighJump))
			return;
		jump(p);
		bPlayer.cooldown(Abilities.HighJump);
		bPlayer.earnXP(BendingType.ChiBlocker,this);
	}

	private void jump(Player p) {
		if (!BlockTools.isSolid(p.getLocation().getBlock()
				.getRelative(BlockFace.DOWN)))
			return;
		Vector vec = p.getVelocity();
		vec.setY(jumpheight);
		p.setVelocity(vec);
		// cooldowns.put(p.getName(), System.currentTimeMillis());	
		return;
	}

	public static String getDescription() {
		return "To use this ability, simply click. You will jump quite high. This ability has a short cooldown.";
	}

	@Override
	public int getBaseExperience() {
		return 1;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

package net.avatar.realms.spigot.bending.abilities.chi;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name = "High Jump", bind = BendingAbilities.HighJump, element = BendingElement.ChiBlocker)
public class HighJump extends BendingActiveAbility {

	@ConfigurationParameter("Height")
	private static final int JUMP_HEIGHT = 7;

	@ConfigurationParameter("Cooldown")
	private static final long COOLDOWN = 1500;

	public HighJump(Player p) {
		super(p, null);
	}

	public boolean swing() {
		if (state == BendingAbilityState.CannotStart) {
			return true;
		}
		if (makeJump()) {
			bender.cooldown(BendingAbilities.HighJump, COOLDOWN);
		}
		return true;
	}

	private boolean makeJump() {
		if (!BlockTools.isSolid(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return false;
		}
		Vector vec = Tools.getVectorForPoints(player.getLocation(), player.getLocation().add(player.getVelocity()).add(0, JUMP_HEIGHT, 0));
		player.setVelocity(vec);
		return true;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}
}

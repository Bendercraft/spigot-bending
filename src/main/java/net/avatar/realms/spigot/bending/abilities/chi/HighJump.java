package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name = "High Jump", bind = BendingAbilities.HighJump, element = BendingElement.ChiBlocker)
public class HighJump extends BendingActiveAbility {

	@ConfigurationParameter("Height")
	private static final int JUMP_HEIGHT = 7;

	@ConfigurationParameter("Cooldown")
	private static final long COOLDOWN = 1500;

	public HighJump(Player p) {
		super(p, null);
	}

	@Override
	public boolean swing() {
		if (this.state == BendingAbilityState.CannotStart) {
			return true;
		}
		if (makeJump()) {
			this.bender.cooldown(BendingAbilities.HighJump, COOLDOWN);
		}
		return true;
	}

	private boolean makeJump() {
		if (!BlockTools.isSolid(this.player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return false;
		}
		int height = JUMP_HEIGHT;
		if (ComboPoints.getComboPointAmount(this.player) >= 2) {
			height++;
		}
		Vector vec = Tools.getVectorForPoints(this.player.getLocation(), this.player.getLocation().add(this.player.getVelocity()).add(0, height, 0));
		this.player.setVelocity(vec);
		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}

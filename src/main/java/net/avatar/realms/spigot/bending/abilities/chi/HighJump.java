package net.avatar.realms.spigot.bending.abilities.chi;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name="High Jump", element=BendingType.ChiBlocker)
public class HighJump extends Ability {

	@ConfigurationParameter("Height")
	private static final int JUMP_HEIGHT = 7;
	@ConfigurationParameter("Cooldown")
	private static final long COOLDOWN = Bending.plugin.configuration.getLongAttribute(configPrefix + "Chi.HighJump.Cooldown", 5);

	public HighJump (Player p) {
		super(p, null);	
	}
	
	public boolean swing() {
		if (state == AbilityState.CannotStart) {
			return true;
		}
		if (makeJump()) {
			bender.cooldown(Abilities.HighJump, COOLDOWN);
		}
		return true;
	}

	private boolean makeJump () {
		if (!BlockTools.isSolid(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return false;
		}
		Vector vec = Tools.getVectorForPoints(player.getLocation(), player.getLocation().add(player.getVelocity()).add(0,JUMP_HEIGHT, 0));
		player.setVelocity(vec);
		return true;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.HighJump;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}
}

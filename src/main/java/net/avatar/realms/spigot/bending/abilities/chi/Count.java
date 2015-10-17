package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;

@ABendingAbility(name = "Release", bind = BendingAbilities.Count, element = BendingElement.ChiBlocker)
public class Count extends BendingActiveAbility {

	public Count(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		LivingEntity target = ComboPoints.getComboPointTarget(player);
		String targetName = "nobody";
		if(target != null) {
			targetName = target.getName();
		}
		player.sendMessage(ChatColor.GOLD+"Bending"+ChatColor.GRAY+" - You have "+ChatColor.AQUA+ComboPoints.getComboPointAmount(player)+ChatColor.GRAY+" combo points on "+ChatColor.RED+targetName);
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void progress() {
		
	}

	@Override
	public void stop() {
		
	}

}

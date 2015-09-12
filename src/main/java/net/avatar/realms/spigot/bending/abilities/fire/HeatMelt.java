package net.avatar.realms.spigot.bending.abilities.fire;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.water.Melt;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

@BendingAbility(name="Heat Melt", element=BendingType.Fire)
public class HeatMelt extends ActiveAbility {

	@ConfigurationParameter("Range")
	private static final int RANGE = 15;

	@ConfigurationParameter("Radius")
	private static final int RADIUS = 5;

	public HeatMelt (Player player, IAbility parent) {
		super(player, parent);
		Location location = EntityTools.getTargetedLocation(player,
				(int) PluginTools.firebendingDayAugment(RANGE, player.getWorld()));
		for (Block block : BlockTools.getBlocksAroundPoint(location,
				(int) PluginTools.firebendingDayAugment(RADIUS, player.getWorld()))) {
			if (BlockTools.isMeltable(block)) {
				Melt.melt(player, block);
			} else if (isHeatable(block)) {
				heat(block);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void heat(Block block) {
		if (block.getType() == Material.OBSIDIAN) {
			block.setType(Material.LAVA);
			block.setData((byte) 0x0);
		}
	}
	
	private static boolean isHeatable(Block block) {
		return false;
	}
	
	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.HeatControl;
	}
	
}

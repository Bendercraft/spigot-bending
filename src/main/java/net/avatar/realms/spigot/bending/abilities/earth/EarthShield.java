package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@BendingAbility(name="Earth Shield", element=BendingType.Earth)
public class EarthShield implements IAbility {
	private static int range = 7;

	private IAbility parent;

	private Block base;

	public EarthShield(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.EarthArmor))
			return;

		base = EntityTools.getTargetBlock(player, range, BlockTools.getTransparentEarthbending());
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		Location location = base.getLocation();
		Location loc1 = location.clone();
		Location loc2 = location.clone();
		Location testloc, testloc2;
		double factor = 3;
		double factor2 = 4;
		int height1 = 3;
		int height2 = 2;
		for (double angle = 0; angle <= 360; angle += 20) {
			testloc = loc1.clone().add(
					factor * Math.cos(Math.toRadians(angle)), 1,
					factor * Math.sin(Math.toRadians(angle)));
			testloc2 = loc2.clone().add(
					factor2 * Math.cos(Math.toRadians(angle)), 1,
					factor2 * Math.sin(Math.toRadians(angle)));
			for (int y = 0; y < EarthColumn.standardheight - height1; y++) {
				testloc = testloc.clone().add(0, -1, 0);
				if (BlockTools.isEarthbendable(player, testloc.getBlock())) {
					if (!blocks.contains(testloc.getBlock())) {
						new EarthColumn(player, testloc, height1 + y - 1, null);
					}
					blocks.add(testloc.getBlock());
					break;
				}
			}
			for (int y = 0; y < EarthColumn.standardheight - height2; y++) {
				testloc2 = testloc2.clone().add(0, -1, 0);
				if (BlockTools.isEarthbendable(player, testloc2.getBlock())) {
					if (!blocks.contains(testloc2.getBlock())) {
						new EarthColumn(player, testloc2, height2 + y - 1, null);
					}
					blocks.add(testloc2.getBlock());
					break;
				}
			}
		}

		if (!blocks.isEmpty()) {
			bPlayer.cooldown(Abilities.EarthArmor);
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

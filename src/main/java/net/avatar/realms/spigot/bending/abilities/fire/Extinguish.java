package net.avatar.realms.spigot.bending.abilities.fire;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.air.AirBlast;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@BendingAbility(name="Extinguish", element=BendingType.Fire)
public class Extinguish implements IAbility {


	@ConfigurationParameter("Range")
	private static double RANGE = 20;
	
	@ConfigurationParameter("Radius")
	private static double RADIUS = 7;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1000;
	private static byte full = AirBlast.full;
	
	private IAbility parent;

	@SuppressWarnings("deprecation")
	public Extinguish(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.HeatControl))
			return;

		double range = PluginTools.firebendingDayAugment(RANGE,
				player.getWorld());
		if (BlockTools.isMeltable(EntityTools.getTargetBlock(player, range))) {
			new HeatMelt(player, this);
			return;
		}
		double radius = PluginTools.firebendingDayAugment(RADIUS,
				player.getWorld());
		for (Block block : BlockTools.getBlocksAroundPoint(
				EntityTools.getTargetBlock(player, range).getLocation(), radius)) {
			if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.Blaze,
					block.getLocation()))
				continue;
			//Do not allow firebender to completly negate lavabend
			if(LavaTrain.isLavaPart(block)) {
				continue;
			}
			if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.STATIONARY_LAVA) {
				block.setType(Material.OBSIDIAN);
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			} else if (block.getType() == Material.LAVA) {
				if (block.getData() == full) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
				block.getWorld().playEffect(block.getLocation(),
						Effect.EXTINGUISH, 0);
			}
		}

		bPlayer.cooldown(Abilities.HeatControl, COOLDOWN);
	}

	public static boolean canBurn(Player player) {
		if (EntityTools.getBendingAbility(player) == Abilities.HeatControl
				|| FireJet.checkTemporaryImmunity(player)) {
			player.setFireTicks(0);
			return false;
		}

		if (player.getFireTicks() > 80
				&& EntityTools.canBendPassive(player, BendingType.Fire)) {
			player.setFireTicks(80);
		}

		return true;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

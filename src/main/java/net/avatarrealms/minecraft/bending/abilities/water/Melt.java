package net.avatarrealms.minecraft.bending.abilities.water;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Melt implements IAbility {

	private static final int defaultrange = FreezeMelt.defaultrange;
	private static final int defaultradius = FreezeMelt.defaultradius;
	private static final int defaultevaporateradius = 3;
	private static final int seaLevel = ConfigManager.seaLevel;

	private static final byte full = 0x0;
	private IAbility parent;

	public Melt(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.PhaseChange))
			return;

		int range = (int) PluginTools.waterbendingNightAugment(defaultrange,
				player.getWorld());
		int radius = (int) PluginTools.waterbendingNightAugment(defaultradius,
				player.getWorld());

		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			radius = AvatarState.getValue(radius);
		}
		boolean evaporate = false;
		Location location = EntityTools.getTargetedLocation(player, range);
		if (BlockTools.isWater(EntityTools.getTargetBlock(player, range))
				&& !(player.getEyeLocation().getBlockY() <= 62)) {
			evaporate = true;
			radius = (int) PluginTools.waterbendingNightAugment(
					defaultevaporateradius, player.getWorld());
		}
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (evaporate) {
				if (block.getY() > seaLevel)
					evaporate(player, block);
			} else {
				melt(player, block);
			}
		}

		bPlayer.cooldown(Abilities.PhaseChange);
	}

	public static void melt(Player player, Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PhaseChange,
				block.getLocation()))
			return;
		if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			return;
		}
		if (!Torrent.canThaw(block)) {
			Torrent.thaw(block);
			return;
		}
		if (BlockTools.isMeltable(block) && !TempBlock.isTempBlock(block)
				&& WaterManipulation.canPhysicsChange(block)) {
			if (block.getType() == Material.SNOW) {
				block.setType(Material.AIR);
				return;
			}
			if (FreezeMelt.isFrozen(block)) {
				FreezeMelt.thawThenRemove(block);
			} else {
				block.setType(Material.WATER);
				block.setData(full);
			}
		}
	}

	public static void evaporate(Player player, Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PhaseChange,
				block.getLocation()))
			return;
		if (BlockTools.isWater(block) && !TempBlock.isTempBlock(block)
				&& WaterManipulation.canPhysicsChange(block)) {
			block.setType(Material.AIR);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

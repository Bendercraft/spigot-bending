package net.avatarrealms.minecraft.bending.abilities.earth;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EarthWall implements IAbility {

	private static final int range = ConfigManager.earthWallRange;
	private static final int defaultheight = ConfigManager.earthWallHeight;
	private static final int defaulthalfwidth = ConfigManager.earthWallWidth / 2;

	private int height = defaultheight;
	private int halfwidth = defaulthalfwidth;
	
	private IAbility parent;

	public EarthWall(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.RaiseEarth))
			return;

		if (AvatarState.isAvatarState(player)) {
			height = (int) (2. / 5. * (double) AvatarState.getValue(height));
			halfwidth = AvatarState.getValue(halfwidth);
		}

		Vector direction = player.getEyeLocation().getDirection().normalize();

		double ox, oy, oz;
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();

		Block sblock = BlockTools.getEarthSourceBlock(player, Abilities.RaiseEarth, range);
		Location origin;
		if (sblock == null) {
			origin = EntityTools.getTargetBlock(player, range, BlockTools.getTransparentEarthbending()).getLocation();
		} else {
			origin = sblock.getLocation();
		}
		World world = origin.getWorld();

		boolean cooldown = false;
		for (int i = -halfwidth; i <= halfwidth; i++) {
			Block block = world.getBlockAt(origin.clone().add(
					orth.clone().multiply((double) i)));

			if (BlockTools.isTransparentToEarthbending(player, block)) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.DOWN);
					if (BlockTools.isEarthbendable(player, Abilities.RaiseEarth, block)) {
						cooldown = true;
						new EarthColumn(player, block.getLocation(), height, this);
						// } else if (block.getType() != Material.AIR
						// && !block.isLiquid()) {
					} else if (!BlockTools.isTransparentToEarthbending(player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(player, Abilities.RaiseEarth,
						block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.UP);
					
					if (BlockTools.isTransparentToEarthbending(player, block)) {
						cooldown = true;
						new EarthColumn(player, block.getRelative(
								BlockFace.DOWN).getLocation(), height, this);
					} else if (!BlockTools.isEarthbendable(player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(player, block)) {
				cooldown = true;
				new EarthColumn(player, block.getLocation(), height, this);
			}
		}
		if (cooldown) {
			bPlayer.cooldown(Abilities.RaiseEarth);
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

package earthbending;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tools.Abilities;
import tools.AvatarState;
import tools.BendingPlayer;
import tools.ConfigManager;
import tools.Tools;

public class EarthWall {

	private static final int range = ConfigManager.earthWallRange;
	private static final int defaultheight = ConfigManager.earthWallHeight;
	private static final int defaulthalfwidth = ConfigManager.earthWallWidth / 2;

	private int height = defaultheight;
	private int halfwidth = defaulthalfwidth;

	public EarthWall(Player player) {
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

		Block sblock = Tools.getEarthSourceBlock(player, range);
		Location origin;
		if (sblock == null) {
			origin = player.getTargetBlock(Tools.getTransparentEarthbending(),
					range).getLocation();
		} else {
			origin = sblock.getLocation();
		}
		World world = origin.getWorld();

		boolean cooldown = false;

		for (int i = -halfwidth; i <= halfwidth; i++) {
			Block block = world.getBlockAt(origin.clone().add(
					orth.clone().multiply((double) i)));
			// if (block.getType() == Material.AIR || block.isLiquid()) {
			if (Tools.isTransparentToEarthbending(player, block)) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.DOWN);
					if (Tools.isEarthbendable(player, block)) {
						cooldown = true;
						new EarthColumn(player, block.getLocation(), height);
						// } else if (block.getType() != Material.AIR
						// && !block.isLiquid()) {
					} else if (!Tools
							.isTransparentToEarthbending(player, block)) {
						break;
					}
				}
			} else if (Tools.isEarthbendable(player,
					block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.UP);
					// if (block.getType() == Material.AIR || block.isLiquid())
					// {
					if (Tools.isTransparentToEarthbending(player, block)) {
						cooldown = true;
						new EarthColumn(player, block.getRelative(
								BlockFace.DOWN).getLocation(), height);
					} else if (!Tools.isEarthbendable(player, block)) {
						break;
					}
				}
			} else if (Tools.isEarthbendable(player, block)) {
				cooldown = true;
				new EarthColumn(player, block.getLocation(), height);
			}
		}

		if (cooldown)
			bPlayer.cooldown(Abilities.RaiseEarth);

	}

}

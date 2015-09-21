package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@BendingAbility(name = "Raise Earth", bind = BendingAbilities.RaiseEarth, element = BendingElement.Earth)
public class EarthWall extends BendingActiveAbility {
	@ConfigurationParameter("Range")
	private static int RANGE = 15;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 6;

	@ConfigurationParameter("Width")
	private static int WIDTH = 6;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

	private int height = HEIGHT;
	private int halfwidth = WIDTH / 2;

	private List<EarthColumn> columns = new LinkedList<EarthColumn>();

	public EarthWall(Player player) {
		super(player, null);

		if (AvatarState.isAvatarState(player)) {
			height = (int) (2. / 5. * (double) AvatarState.getValue(height));
			halfwidth = AvatarState.getValue(halfwidth);
		}
	}

	@Override
	public boolean swing() {
		// One column
		if (state != BendingAbilityState.CanStart) {
			return false;
		}

		if (bender.isOnCooldown(BendingAbilities.RaiseEarth)) {
			return false;
		}

		columns.add(new EarthColumn(player));
		state = BendingAbilityState.Progressing;
		return false;
	}

	@Override
	public boolean sneak() {
		if (state != BendingAbilityState.CanStart) {
			return false;
		}

		// Wall
		if (bender.isOnCooldown(BendingAbilities.RaiseEarth))
			return false;

		Vector direction = player.getEyeLocation().getDirection().normalize();

		double ox, oy, oz;
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();

		Block sblock = BlockTools.getEarthSourceBlock(player, BendingAbilities.RaiseEarth, RANGE);
		Location origin;
		if (sblock == null) {
			origin = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
		} else {
			origin = sblock.getLocation();
		}
		World world = origin.getWorld();

		boolean cooldown = false;
		for (int i = -halfwidth; i <= halfwidth; i++) {
			Block block = world.getBlockAt(origin.clone().add(orth.clone().multiply((double) i)));

			if (BlockTools.isTransparentToEarthbending(player, block)) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.DOWN);
					if (BlockTools.isEarthbendable(player, BendingAbilities.RaiseEarth, block)) {
						cooldown = true;
						columns.add(new EarthColumn(player, block.getLocation(), height));
						// } else if (block.getType() != Material.AIR
						// && !block.isLiquid()) {
					} else if (!BlockTools.isTransparentToEarthbending(player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(player, BendingAbilities.RaiseEarth, block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.UP);

					if (BlockTools.isTransparentToEarthbending(player, block)) {
						cooldown = true;
						columns.add(new EarthColumn(player, block.getRelative(BlockFace.DOWN).getLocation(), height));
					} else if (!BlockTools.isEarthbendable(player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(player, block)) {
				cooldown = true;
				columns.add(new EarthColumn(player, block.getLocation(), height));
			}
		}
		if (cooldown) {
			bender.cooldown(BendingAbilities.RaiseEarth, COOLDOWN);
		}
		state = BendingAbilityState.Progressing;
		return false;
	}

	@Override
	public boolean progress() {
		if (super.progress()) {
			return false;
		}

		if (state == BendingAbilityState.Progressing && columns.isEmpty()) {
			return false;
		}

		for (EarthColumn column : columns) {
			if (!column.progress()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void remove() {
		for (EarthColumn column : columns) {
			column.remove();
		}
		super.remove();
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}

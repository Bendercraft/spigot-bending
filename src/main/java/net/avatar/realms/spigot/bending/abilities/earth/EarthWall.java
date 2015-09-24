package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

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
			this.height = (int) (2. / 5. * AvatarState.getValue(this.height));
			this.halfwidth = AvatarState.getValue(this.halfwidth);
		}
	}

	@Override
	public boolean swing() {
		// One column
		if (this.state != BendingAbilityState.CanStart) {
			return false;
		}

		if (this.bender.isOnCooldown(BendingAbilities.RaiseEarth)) {
			return false;
		}

		this.columns.add(new EarthColumn(this.player));
		this.state = BendingAbilityState.Progressing;
		AbilityManager.getManager().addInstance(this);
		return false;
	}

	@Override
	public boolean sneak() {
		if (this.state != BendingAbilityState.CanStart) {
			return false;
		}

		// Wall
		if (this.bender.isOnCooldown(BendingAbilities.RaiseEarth)) {
			return false;
		}

		Vector direction = this.player.getEyeLocation().getDirection().normalize();

		double ox, oy, oz;
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();

		Block sblock = BlockTools.getEarthSourceBlock(this.player, BendingAbilities.RaiseEarth, RANGE);
		Location origin;
		if (sblock == null) {
			origin = EntityTools.getTargetBlock(this.player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();
		} else {
			origin = sblock.getLocation();
		}
		World world = origin.getWorld();

		boolean cooldown = false;
		for (int i = -this.halfwidth; i <= this.halfwidth; i++) {
			Block block = world.getBlockAt(origin.clone().add(orth.clone().multiply((double) i)));

			if (BlockTools.isTransparentToEarthbending(this.player, block)) {
				for (int j = 1; j < this.height; j++) {
					block = block.getRelative(BlockFace.DOWN);
					if (BlockTools.isEarthbendable(this.player, BendingAbilities.RaiseEarth, block)) {
						cooldown = true;
						this.columns.add(new EarthColumn(this.player, block.getLocation(), this.height));
						// } else if (block.getType() != Material.AIR
						// && !block.isLiquid()) {
					} else if (!BlockTools.isTransparentToEarthbending(this.player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(this.player, BendingAbilities.RaiseEarth, block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < this.height; j++) {
					block = block.getRelative(BlockFace.UP);

					if (BlockTools.isTransparentToEarthbending(this.player, block)) {
						cooldown = true;
						this.columns.add(new EarthColumn(this.player, block.getRelative(BlockFace.DOWN).getLocation(), this.height));
					} else if (!BlockTools.isEarthbendable(this.player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(this.player, block)) {
				cooldown = true;
				this.columns.add(new EarthColumn(this.player, block.getLocation(), this.height));
			}
		}
		if (cooldown) {
			this.bender.cooldown(BendingAbilities.RaiseEarth, COOLDOWN);
		}
		this.state = BendingAbilityState.Progressing;
		AbilityManager.getManager().addInstance(this);
		return false;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (this.state == BendingAbilityState.Progressing && this.columns.isEmpty()) {
			return false;
		}

		LinkedList<EarthColumn> test = new LinkedList<EarthColumn>(this.columns);
		for (EarthColumn column : test) {
			if (!column.progress()) {
				this.columns.remove(column);
			}
		}

		return true;
	}

	@Override
	public void remove() {
		for (EarthColumn column : this.columns) {
			column.remove();
		}
		super.remove();
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

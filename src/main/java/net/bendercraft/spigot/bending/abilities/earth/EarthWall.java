package net.bendercraft.spigot.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.energy.AvatarState;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = EarthWall.NAME, element = BendingElement.EARTH)
public class EarthWall extends BendingActiveAbility {
	public final static String NAME = "RaiseEarth";
	
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

	public EarthWall(RegisteredAbility register, Player player) {
		super(register, player);

		if (AvatarState.isAvatarState(player)) {
			this.height = (int) (2. / 5. * AvatarState.getValue(this.height));
			this.halfwidth = AvatarState.getValue(this.halfwidth);
		}
	}

	@Override
	public boolean swing() {
		// One column
		if (getState() != BendingAbilityState.START) {
			return false;
		}

		if (this.bender.isOnCooldown(NAME)) {
			return false;
		}
		EarthColumn ec = new EarthColumn();
		if(ec.init(player)) {
			this.columns.add(ec);
		}
		setState(BendingAbilityState.PROGRESSING);
		
		return false;
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.START) {
			return false;
		}

		// Wall
		if (this.bender.isOnCooldown(NAME)) {
			return false;
		}

		Vector direction = this.player.getEyeLocation().getDirection().normalize();

		double ox, oy, oz;
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();

		Block sblock = BlockTools.getEarthSourceBlock(this.player, register, RANGE);
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
					if (BlockTools.isEarthbendable(this.player, register, block)) {
						cooldown = true;
						EarthColumn ec = new EarthColumn();
						if(ec.init(this.player, block.getLocation(), this.height)) {
							this.columns.add(ec);
						}
						// } else if (block.getType() != Material.AIR
						// && !block.isLiquid()) {
					} else if (!BlockTools.isTransparentToEarthbending(this.player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(this.player, register, block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < this.height; j++) {
					block = block.getRelative(BlockFace.UP);

					if (BlockTools.isTransparentToEarthbending(this.player, block)) {
						cooldown = true;
						EarthColumn ec = new EarthColumn();
						if(ec.init(this.player, block.getRelative(BlockFace.DOWN).getLocation(), this.height)) {
							this.columns.add(ec);
						}
					} else if (!BlockTools.isEarthbendable(this.player, block)) {
						break;
					}
				}
			} else if (BlockTools.isEarthbendable(this.player, block)) {
				cooldown = true;
				EarthColumn ec = new EarthColumn();
				if(ec.init(this.player, block.getLocation(), this.height)) {
					this.columns.add(ec);
				}
			}
		}
		if (cooldown) {
			this.bender.cooldown(NAME, COOLDOWN);
		}
		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public void progress() {
		if (getState() == BendingAbilityState.PROGRESSING && this.columns.isEmpty()) {
			remove();
			return;
		}

		LinkedList<EarthColumn> test = new LinkedList<EarthColumn>(this.columns);
		for (EarthColumn column : test) {
			if (!column.progress()) {
				this.columns.remove(column);
			}
		}
	}

	@Override
	public void stop() {
		for (EarthColumn column : this.columns) {
			column.remove();
		}
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

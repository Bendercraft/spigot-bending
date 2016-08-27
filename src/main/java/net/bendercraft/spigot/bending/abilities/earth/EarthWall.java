package net.bendercraft.spigot.bending.abilities.earth;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
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
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = EarthWall.NAME, element = BendingElement.EARTH)
public class EarthWall extends BendingActiveAbility {
	public final static String NAME = "RaiseEarth";
	
	@ConfigurationParameter("Range")
	private static int RANGE = 15;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 6;

	@ConfigurationParameter("Width")
	private static int WIDTH = 6;
	
	@ConfigurationParameter("Selection-Time")
	public static long SELECTION_TIME = 3000;
	
	@ConfigurationParameter("Selection-Max")
	private static int SELECTION_MAX = 30;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1500;

	private int height = HEIGHT;
	private int halfwidth = WIDTH / 2;

	private List<EarthColumn> columns = new LinkedList<EarthColumn>();
	private List<Block> selection = new LinkedList<Block>();
	private List<TempBlock> selected = new LinkedList<TempBlock>();

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
		if (!isState(BendingAbilityState.START)) {
			return false;
		}

		if (bender.isOnCooldown(NAME)) {
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
		if (!isState(BendingAbilityState.START)) {
			return false;
		}

		// Wall
		if (bender.isOnCooldown(NAME)) {
			return false;
		}
		
		addSelection();
		
		setState(BendingAbilityState.PREPARING);
		return false;
	}
	
	private void addSelection() {
		Block target = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending());
		if(target != null && !TempBlock.isTempBlock(target) && !selection.contains(target) && BlockTools.isEarthbendable(player, target)) {
			selection.add(target);
			selected.add(TempBlock.makeTemporary(target, Material.COBBLESTONE, false));
		}
	}

	@Override
	public void progress() {
		if(isState(BendingAbilityState.PREPARING)) {
			if(player.isSneaking() 
					&& System.currentTimeMillis() < getStartedTime() + SELECTION_TIME 
					&& selection.size() < SELECTION_MAX) {
				addSelection();
			} else {
				setState(BendingAbilityState.PREPARED);
			}
		} else if(isState(BendingAbilityState.PREPARED)) {
			selected.forEach(t -> t.revertBlock());
			selected.clear();
			
			// If only one block has been selected, then keep legacy behavior and expand selection
			if(selection.size() <= 3) {
				Vector direction = player.getEyeLocation().getDirection().normalize();

				double ox, oy, oz;
				ox = -direction.getZ();
				oy = 0;
				oz = direction.getX();
				
				Vector orth = new Vector(ox, oy, oz);
				orth = orth.normalize();
				
				World world = selection.get(0).getWorld();
				for (int i = -halfwidth; i <= halfwidth; i++) {
					Block block = world.getBlockAt(selection.get(0).getLocation().add(orth.clone().multiply((double) i)));
					selection.add(block);
				}
			}
			
			boolean cooldown = false;
			for(Block block : selection) {
				if (BlockTools.isTransparentToEarthbending(player, block)) {
					for (int j = 1; j < height; j++) {
						block = block.getRelative(BlockFace.DOWN);
						if (BlockTools.isEarthbendable(player, register, block)) {
							cooldown = true;
							EarthColumn ec = new EarthColumn();
							if(ec.init(player, block.getLocation(), height)) {
								columns.add(ec);
							}
						} else if (!BlockTools.isTransparentToEarthbending(player, block)) {
							break;
						}
					}
				} else if (BlockTools.isEarthbendable(player, register, block.getRelative(BlockFace.UP))) {
					for (int j = 1; j < height; j++) {
						block = block.getRelative(BlockFace.UP);

						if (BlockTools.isTransparentToEarthbending(player, block)) {
							cooldown = true;
							EarthColumn ec = new EarthColumn();
							if(ec.init(player, block.getRelative(BlockFace.DOWN).getLocation(), height)) {
								columns.add(ec);
							}
						} else if (!BlockTools.isEarthbendable(player, block)) {
							break;
						}
					}
				} else if (BlockTools.isEarthbendable(player, block)) {
					cooldown = true;
					EarthColumn ec = new EarthColumn();
					if(ec.init(player, block.getLocation(), height)) {
						columns.add(ec);
					}
				}
			}
			if(cooldown) {
				bender.cooldown(NAME, COOLDOWN * (columns.size() / WIDTH));
			}
			setState(BendingAbilityState.PROGRESSING);
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			Iterator<EarthColumn> it = columns.iterator();
			while(it.hasNext()) {
				EarthColumn column = it.next();
				if (!column.progress()) {
					it.remove();
				}
			}
			if(columns.isEmpty()) {
				remove();
				return;
			}
		}
	}

	@Override
	public void stop() {
		selected.forEach(t -> t.revertBlock());
		selected.clear();
		columns.forEach(c -> c.remove());
		columns.clear();
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

package net.bendercraft.spigot.bending.abilities.water;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = Frozen.NAME, element = BendingElement.WATER)
public class Frozen extends BendingActiveAbility {
	public final static String NAME = "Frozen";
	
	private Map<Integer, List<Block>> expands = new HashMap<Integer, List<Block>>();
	private int iteration = 0;
	private long time;
	private long interval = (long) (1000. / 8);
	
	public Frozen(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if(isState(BendingAbilityState.PROGRESSING)) {
			return true;
		}
		for(Block block : BlockTools.getBlocksAroundPoint(player.getLocation(), 32)) {
			if(block.getLocation().getBlockY() < player.getLocation().getBlockY() - 8) {
				continue;
			}
			if(block.getType() == Material.CHEST 
					|| block.getType() == Material.LOG 
					|| block.getType() == Material.LOG_2 
					|| block.getType() == Material.BANNER
					|| block.getType() == Material.STANDING_BANNER
					|| block.getType() == Material.WALL_BANNER) {
				continue;
			}
			int distance = (int) player.getLocation().distance(block.getLocation());
			if(!expands.containsKey(distance)) {
				expands.put(distance, new LinkedList<Block>());
			}
			expands.get(distance).add(block);
		}
		for(List<Block> blocks : expands.values()) {
			Collections.shuffle(blocks);
		}
		setState(BendingAbilityState.PROGRESSING);
		time = System.currentTimeMillis();
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if(expands.containsKey(iteration)) {
				int done = 0;
				Iterator<Block> it = expands.get(iteration).iterator();
				while(it.hasNext()) {
					Block block = it.next();
					if(block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2) {
						Material mat = Material.STAINED_GLASS;
						double rand = Math.random();
						if(rand < 0.5) {
							mat = Material.STAINED_GLASS_PANE;
						}
						
						DyeColor color = DyeColor.BLUE;
						rand = Math.random();
						if(rand < 0.5) {
							color = DyeColor.LIGHT_BLUE;
						}
						Bending.getInstance().getManager().addGlobalTempBlock(180000, TempBlock.makeTemporary(block, mat, color.getData(), true));
						block.getWorld().playSound(block.getLocation(), Sound.BLOCK_SNOW_PLACE, 1.0f, 1.0f);
					} else if(block.getType().isSolid() || block.isLiquid()) {
						Bending.getInstance().getManager().addGlobalTempBlock(180000, TempBlock.makeTemporary(block, Math.random() < 0.5 ? Material.ICE : Material.PACKED_ICE, true));
						block.getWorld().playSound(block.getLocation(), Sound.BLOCK_SNOW_PLACE, 1.0f, 1.0f);
					}
					it.remove();
					done++;
					if(done > 200) {
						break;
					}
				}
				if(expands.get(iteration).isEmpty()) {
					iteration++;
				}
			} else {
				remove();
				return;
			}
		}
	}

	@Override
	protected long getMaxMillis() {
		return 120000;
	}

	@Override
	public void stop() {
		
	}
	
	@Override
	public Object getIdentifier() {
		return player;
	}
}

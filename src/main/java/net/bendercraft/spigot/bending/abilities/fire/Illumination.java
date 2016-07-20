package net.bendercraft.spigot.bending.abilities.fire;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = Illumination.NAME, element = BendingElement.FIRE, shift=false)
public class Illumination extends BendingActiveAbility {
	public final static String NAME = "Illumination";

	@ConfigurationParameter("Range")
	private static int RANGE = 5;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 0;

	private TempBlock block;

	public Illumination(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if(isState(BendingAbilityState.START)) {
			setState(BendingAbilityState.PROGRESSING);
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			remove();
		}
		return false;
	}

	private void set() {
		Block standingblock = player.getLocation().getBlock();
		Block standblock = standingblock.getRelative(BlockFace.DOWN);
		
		if ((FireStream.isIgnitable(player, standingblock) 
				&& (standblock.getType() != Material.LEAVES)) 
				&& (block == null) && !isIlluminated(standblock)) {
			block = TempBlock.makeTemporary(standingblock, Material.TORCH, false);
		} else if ((FireStream.isIgnitable(this.player, standingblock) 
				&& (standblock.getType() != Material.LEAVES)) 
				&& !block.equals(standblock) 
				&& !isIlluminated(standblock) 
				&& BlockTools.isSolid(standblock)) {
			if(block != null) {
				block.revertBlock();
			}
			block = TempBlock.makeTemporary(standingblock, Material.TORCH, false);
		}
	}

	@Override
	public void progress() {
		set();
	}

	@Override
	public void stop() {
		if(block != null) {
			block.revertBlock();
		}
		bender.cooldown(NAME, COOLDOWN);
	}

	@Override
	protected long getMaxMillis() {
		return 1000 * 60 * 15;
	}

	public static boolean isIlluminated(Block block) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return false;
		}
		for (Object o : instances.keySet()) {
			Illumination ill = (Illumination) instances.get(o);
			if (ill.block != null && ill.block.equals(block)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

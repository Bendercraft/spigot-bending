package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.HashMap;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class CFour {
	private static Map<Player,CFour> instances = new HashMap<Player, CFour>();
	
	private Player player;
	private Block bomb;
	
	public CFour (Player player, Block block, BlockFace face){
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.isOnCooldown(Abilities.PlasticBomb)) {
			return;
		}
		this.player = player;
		this.generateCFour();
	}
	
	public static void activate(Player player) {
		if (instances.containsKey(player)) {
			CFour bomb = instances.get(player);
			bomb.activate();
		}
	}
	
	public void activate() {
		
	}
	
	public void generateCFour() {
		
	}

}

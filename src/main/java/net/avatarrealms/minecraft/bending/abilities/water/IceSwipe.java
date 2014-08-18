package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.IAbility;

public class IceSwipe implements IAbility{
	
	
	private static int range = ConfigManager.iceSwipeRange;
	private static int damage = ConfigManager.iceSwipeDamage;
	private static Map<Player, IceSwipe> instances = new HashMap<Player, IceSwipe>();
	private LinkedList<Block> blocks;
	
	private IAbility parent;
	
	//TODO : Not to forget to check for the protected region
	//TODO : As Kya against Zaheer
	
	public IceSwipe(Player player, Block sourceblock) {
		
	}
	
	public static void prepare(Player player) {
		
		
	}
	
	public static void progressAll() {
		
	}
	
	public boolean progress() {
		
		return true;
	}

	
	@Override
	public int getBaseExperience() {
		return 4;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

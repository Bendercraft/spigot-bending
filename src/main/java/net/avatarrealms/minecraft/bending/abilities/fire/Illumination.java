package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Illumination implements IAbility {
	private static Map<Player, Illumination> instances = new HashMap<Player, Illumination>();
	private static Map<Block, Player> blocks = new HashMap<Block, Player>();

	private static final int range = ConfigManager.illuminationRange;

	private Player player;
	private Block block;
	private Material normaltype;
	private byte normaldata;
	private IAbility parent;

	public Illumination(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Illumination))
			return;
		if (instances.containsKey(player)) {
			instances.get(player).revert();
			instances.remove(player);
		} else {
			this.player = player;
			set();
			instances.put(player, this);
			bPlayer.cooldown(Abilities.Illumination);
		}
	}

	private void set() {
		Block standingblock = player.getLocation().getBlock();
		Block standblock = standingblock.getRelative(BlockFace.DOWN);
		if ((FireStream.isIgnitable(player, standingblock) && standblock
				.getType() != Material.LEAVES)
				&& block == null
				&& !blocks.containsKey(standblock)) {
			block = standingblock;
			normaltype = block.getType();
			normaldata = block.getData();
			block.setType(Material.TORCH);
			blocks.put(block, player);
		} else if ((FireStream.isIgnitable(player, standingblock) && standblock
				.getType() != Material.LEAVES)
				&& !block.equals(standblock)
				&& !blocks.containsKey(standblock) && BlockTools.isSolid(standblock)) {
			revert();
			block = standingblock;
			normaltype = block.getType();
			normaldata = block.getData();
			block.setType(Material.TORCH);
			blocks.put(block, player);
		} else if (block == null) {
			return;
		} else if (player.getWorld() != block.getWorld()) {
			revert();
		} else if (player.getLocation().distance(block.getLocation()) > 
		PluginTools.firebendingDayAugment(range, player.getWorld())) {
			revert();
		}
	}

	private void revert() {
		if (block != null) {
			blocks.remove(block);
			block.setType(normaltype);
			block.setData(normaldata);
		}
	}

	public static void revert(Block block) {
		Player player = blocks.get(block);
		instances.get(player).revert();
	}

	public static void progressAll() {
		List<Illumination> toRemove = new LinkedList<Illumination>();
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (instances.containsKey(player)
					&& (!EntityTools.hasAbility(player, Abilities.Illumination) || !EntityTools
							.canBend(player, Abilities.Illumination))) {
				instances.get(player).revert();
				toRemove.add(instances.get(player));
			} else if (instances.containsKey(player)) {
				instances.get(player).set();
			}
		}
		for(Illumination illumination : toRemove) {
			illumination.remove();
		}
		
		toRemove.clear();
		for (Entry<Player, Illumination> entry : instances.entrySet()) {
			Player player = entry.getKey();
			Illumination illumination = entry.getValue();
			if (!player.isOnline() || player.isDead()) {
				illumination.revert();
				toRemove.add(illumination);
			}
		}
		for(Illumination illumination : toRemove) {
			illumination.remove();
		}
	}

	private void remove() {
		instances.remove(player);
	}

	public static void removeAll() {
		for (Illumination illumination : instances.values()) {
			illumination.revert();
		}
		instances.clear();
	}

	public static String getDescription() {
		return "This ability gives firebenders a means of illuminating the area. It is a toggle - clicking "
				+ "will create a torch that follows you around. The torch will only appear on objects that are "
				+ "ignitable and can hold a torch (e.g. not leaves or ice). If you get too far away from the torch, "
				+ "it will disappear, but will reappear when you get on another ignitable block. Clicking again "
				+ "dismisses this torch.";
	}

	public static boolean isIlluminated(Block block) {
		return blocks.containsKey(block);
	}

	@Override
	public int getBaseExperience() {
		return 0;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

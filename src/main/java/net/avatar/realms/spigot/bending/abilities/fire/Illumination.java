package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

@BendingAbility(name="Illumination", element=BendingType.Fire)
public class Illumination implements IAbility {
	private static Map<Player, Illumination> instances = new HashMap<Player, Illumination>();
	private static Map<Block, Player> blocks = new HashMap<Block, Player>();

	
	@ConfigurationParameter("Range")
	private static final int RANGE = 5;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 0;

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
			bPlayer.cooldown(Abilities.Illumination, COOLDOWN);
		}
	}

	@SuppressWarnings("deprecation")
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
		PluginTools.firebendingDayAugment(RANGE, player.getWorld())) {
			revert();
		}
	}

	@SuppressWarnings("deprecation")
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

	public static boolean isIlluminated(Block block) {
		return blocks.containsKey(block);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}

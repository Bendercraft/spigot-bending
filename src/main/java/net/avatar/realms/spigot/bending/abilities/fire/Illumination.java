package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

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

		if (bPlayer.isOnCooldown(Abilities.Illumination)) {
			return;
		}
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
		Block standingblock = this.player.getLocation().getBlock();
		Block standblock = standingblock.getRelative(BlockFace.DOWN);
		if ((FireStream.isIgnitable(this.player, standingblock) && (standblock
				.getType() != Material.LEAVES))
				&& (this.block == null)
				&& !blocks.containsKey(standblock)) {
			this.block = standingblock;
			this.normaltype = this.block.getType();
			this.normaldata = this.block.getData();
			this.block.setType(Material.TORCH);
			blocks.put(this.block, this.player);
		} else if ((FireStream.isIgnitable(this.player, standingblock) && (standblock
				.getType() != Material.LEAVES))
				&& !this.block.equals(standblock)
				&& !blocks.containsKey(standblock) && BlockTools.isSolid(standblock)) {
			revert();
			this.block = standingblock;
			this.normaltype = this.block.getType();
			this.normaldata = this.block.getData();
			this.block.setType(Material.TORCH);
			blocks.put(this.block, this.player);
		} else if (this.block == null) {
			return;
		} else if (this.player.getWorld() != this.block.getWorld()) {
			revert();
		} else if (this.player.getLocation().distance(this.block.getLocation()) > 
		PluginTools.firebendingDayAugment(RANGE, this.player.getWorld())) {
			revert();
		}
	}

	@SuppressWarnings("deprecation")
	private void revert() {
		if (this.block != null) {
			blocks.remove(this.block);
			this.block.setType(this.normaltype);
			this.block.setData(this.normaldata);
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
 && (!EntityTools.canBend(player, Abilities.Illumination))) {
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
		instances.remove(this.player);
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
		return this.parent;
	}

}

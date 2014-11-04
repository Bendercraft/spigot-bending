package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.abilities.TempPotionEffect;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EarthArmor implements IAbility {
	private static Map<Player, EarthArmor> instances = new HashMap<Player, EarthArmor>();
	
	private static long duration = ConfigManager.earthArmorDuration;
	private static int strength = ConfigManager.earthArmorStrength;
	private static long cooldown = ConfigManager.earthArmorCooldown;
	private static int range = 7;

	private Player player;
	private Block headblock, legsblock;
	private Location headblocklocation, legsblocklocation;
	private Material headtype, legstype;
	private byte headdata, legsdata;
	private long time, starttime;
	private boolean formed = false;
	private boolean complete = false;
	public ItemStack[] oldarmor;
	public List<ItemStack> armors = new ArrayList<ItemStack>(4);
	private IAbility parent;

	private static long interval = 2000;

	public EarthArmor(Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player)) {
			return;
		}

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.EarthArmor))
			return;

		this.player = player;
		
		headblock = EntityTools.getTargetBlock(player, range, BlockTools.getTransparentEarthbending());
		if (BlockTools.getEarthbendableBlocksLength(player, headblock, new Vector(0,
				-1, 0), 2) >= 2) {
			legsblock = headblock.getRelative(BlockFace.DOWN);
			headtype = headblock.getType();
			legstype = legsblock.getType();
			headdata = headblock.getData();
			legsdata = legsblock.getData();
			headblocklocation = headblock.getLocation();
			legsblocklocation = legsblock.getLocation();
			Block oldheadblock, oldlegsblock;
			oldheadblock = headblock;
			oldlegsblock = legsblock;
			if (!moveBlocks()) {
				return;
			}	
			if (ConfigManager.reverseearthbending) {
				BlockTools.addTempAirBlock(oldheadblock);
				BlockTools.addTempAirBlock(oldlegsblock);
			} else {
				BlockTools.removeBlock(oldheadblock);
				BlockTools.removeBlock(oldlegsblock);
			}
			instances.put(player, this);
		}
	}

	private boolean moveBlocks() {
		if (player.getWorld() != headblock.getWorld()) {
			cancel();
			return false;
		}

		Location headlocation = player.getEyeLocation();
		Location legslocation = player.getLocation();
		Vector headdirection = headlocation.toVector()
				.subtract(headblocklocation.toVector()).normalize()
				.multiply(.5);
		Vector legsdirection = legslocation.toVector()
				.subtract(legsblocklocation.toVector()).normalize()
				.multiply(.5);

		Block newheadblock = headblock;
		Block newlegsblock = legsblock;

		if (!headlocation.getBlock().equals(headblock)) {
			headblocklocation = headblocklocation.clone().add(headdirection);
			newheadblock = headblocklocation.getBlock();
		}
		if (!legslocation.getBlock().equals(legsblock)) {
			legsblocklocation = legsblocklocation.clone().add(legsdirection);
			newlegsblock = legsblocklocation.getBlock();
		}

		if (BlockTools.isTransparentToEarthbending(player, newheadblock)
				&& !newheadblock.isLiquid()) {
			BlockTools.breakBlock(newheadblock);
		} else if (!BlockTools.isEarthbendable(player, Abilities.EarthArmor, newheadblock)
				&& !newheadblock.isLiquid()
				&& newheadblock.getType() != Material.AIR) {
			cancel();
			return false;
		}

		if (BlockTools.isTransparentToEarthbending(player, newlegsblock)
				&& !newlegsblock.isLiquid()) {
			BlockTools.breakBlock(newlegsblock);
		} else if (!BlockTools.isEarthbendable(player, Abilities.EarthArmor, newlegsblock)
				&& !newlegsblock.isLiquid()
				&& newlegsblock.getType() != Material.AIR) {
			cancel();
			return false;
		}

		if (headblock.getLocation().distance(player.getEyeLocation()) > range
				|| legsblock.getLocation().distance(player.getLocation()) > range) {
			cancel();
			return false;
		}

		if (!newheadblock.equals(headblock)) {
			new TempBlock(newheadblock, headtype, headdata);
			if (TempBlock.isTempBlock(headblock))
				TempBlock.revertBlock(headblock, Material.AIR);
		}

		if (!newlegsblock.equals(legsblock)) {
			new TempBlock(newlegsblock, legstype, legsdata);
			if (TempBlock.isTempBlock(legsblock))
				TempBlock.revertBlock(legsblock, Material.AIR);
		}

		headblock = newheadblock;
		legsblock = newlegsblock;

		return true;
	}

	private void cancel() {
		if (ConfigManager.reverseearthbending) {
			if (TempBlock.isTempBlock(headblock))
				TempBlock.revertBlock(headblock, Material.AIR);
			if (TempBlock.isTempBlock(legsblock))
				TempBlock.revertBlock(legsblock, Material.AIR);
		} else {
			headblock.breakNaturally();
			legsblock.breakNaturally();
		}
	}

	private boolean inPosition() {
		if (headblock.equals(player.getEyeLocation().getBlock())
				&& legsblock.equals(player.getLocation().getBlock())) {
			return true;
		}
		return false;
	}

	private void formArmor() {
		if (TempBlock.isTempBlock(headblock)) {
			TempBlock.revertBlock(headblock, Material.AIR);
		}
			
		if (TempBlock.isTempBlock(legsblock)) {
			TempBlock.revertBlock(legsblock, Material.AIR);
		}

		oldarmor = player.getInventory().getArmorContents();
		if (BlockTools.isIronBendable(player, legstype)) {
			ItemStack is = new ItemStack(Material.IRON_BOOTS, 1);
			//is.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
			armors.add(is);
			is = new ItemStack(Material.IRON_LEGGINGS, 1);
			//is.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
			armors.add(is);
		}
		else {
			armors.add(new ItemStack(Material.LEATHER_BOOTS, 1));
			armors.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
		}
		
		if (BlockTools.isIronBendable(player, headtype)) {
			ItemStack is = new ItemStack(Material.IRON_CHESTPLATE, 1);
			//is.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
			armors.add(is);
			is = new ItemStack(Material.IRON_HELMET, 1);
			//is.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
			armors.add(is);
		}
		else {
			armors.add(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
			armors.add(new ItemStack(Material.LEATHER_HELMET, 1));
		}			
				
		ItemStack[] ar = armors.toArray(new ItemStack[armors.size()]);
		player.getInventory().setArmorContents(ar);
		PotionEffect resistance = new PotionEffect(
				PotionEffectType.DAMAGE_RESISTANCE, (int) duration / 50,
				strength - 1);
		new TempPotionEffect(player, resistance);
		formed = true;
		starttime = System.currentTimeMillis();
	}
	
	private void remove() {
		instances.remove(player);
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			cancel();
			removeEffect();
			return false;
		}

		if (formed) {
			if (System.currentTimeMillis() > starttime + duration
					&& !complete) {
				complete = true;
				removeEffect();
				return true;
			}
			if (System.currentTimeMillis() > starttime + cooldown) {
				return false;
			}
		} else if (System.currentTimeMillis() > time + interval) {
			if (inPosition()) {
				formArmor();
			} else {
				return moveBlocks();
			}
		}
		return true;
	}
	
	public static void progressAll() {
		List<EarthArmor> toRemove = new LinkedList<EarthArmor>();
		for(EarthArmor armor : instances.values()) {
			boolean keep = armor.progress();
			if(!keep) {
				toRemove.add(armor);
			}
		}
		for(EarthArmor armor : toRemove) {
			armor.remove();
		}
	}
	
	public static boolean hasEarthArmor(Player player) {
		return instances.containsKey(player);
	}
	
	public static EarthArmor getEarthArmor(Player pl) {
		return instances.get(pl);
	}
	
	public boolean isArmor(ItemStack is) {
		for (ItemStack part : armors) {
			if (part.getType().equals(is.getType())) {
				return true;
			}
		}
		return false;
	}

	private void removeEffect() {
		player.getInventory().setArmorContents(oldarmor);
	}

	public static void removeEffect(Player player) {
		if (!instances.containsKey(player))
			return;
		instances.get(player).removeEffect();
	}

	public static void removeAll() {
		for (EarthArmor eartharmor : instances.values()) {
			eartharmor.cancel();
			eartharmor.removeEffect();
		}
		instances.clear();
	}

	public static boolean canRemoveArmor(Player player) {
		if (instances.containsKey(player)) {
			EarthArmor eartharmor = instances.get(player);
			if (System.currentTimeMillis() < eartharmor.starttime + duration)
				return false;
		}
		return true;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

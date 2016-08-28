package net.bendercraft.spigot.bending.abilities.earth;

import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = EarthArmor.NAME, element = BendingElement.EARTH)
public class EarthArmor extends BendingActiveAbility {
	public final static String NAME = "EarthArmor";
	
	@ConfigurationParameter("Duration")
	private static long DURATION = 20000;

	@ConfigurationParameter("Strength")
	private static int STRENGTH = 3;
	
	@ConfigurationParameter("Strength-Bonus-Iron")
	private static int STRENGTH_BONUS_IRON = 2;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;

	@ConfigurationParameter("Range")
	private static int RANGE = 7;
	
	@ConfigurationParameter("Damage-Reduction")
	private static double DAMAGE_REDUCTION = 0.15;
	@ConfigurationParameter("Damage-Reduction-Boost")
	private static double DAMAGE_REDUCTION_BOOST = 0.60;

	private TempBlock block;
	private Location location;
	private Material material;
	private int strength;
	
	private boolean boost = false;
	private boolean consumed = false;

	public EarthArmor(RegisteredAbility register, Player player) {
		super(register, player);
		this.strength = STRENGTH;
	}

	@Override
	public boolean swing() {
		if(isState(BendingAbilityState.START)) {
			Block target = EntityTools.getTargetBlock(player, (int) RANGE, BlockTools.getTransparentEarthbending());
			if(target != null 
					&& BlockTools.isEarthbendable(player, target)
					&& BlockTools.isTransparentToEarthbending(player, target.getRelative(BlockFace.UP))
					&& !TempBlock.isTempBlock(target.getRelative(BlockFace.UP))) {
				location = target.getRelative(BlockFace.UP).getLocation();
				material = target.getType();
				block = TempBlock.makeTemporary(location.getBlock(), material, false);
				if(BlockTools.isIronBendable(player, material)) {
					strength += STRENGTH_BONUS_IRON;
				}
				setState(BendingAbilityState.PREPARING);
			}
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			remove();
		}
		
		return false;
	}
	
	@Override
	public boolean sneak() {
		if(isState(BendingAbilityState.PROGRESSING) && !consumed) {
			consumed = true;
			boost = true;
		}
		return false;
	}
	
	@Override
	public void progress() {
		if(isState(BendingAbilityState.PREPARING)) {
			if(!moveBlocks()) {
				remove();
				return;
			}
		} else if(isState(BendingAbilityState.PROGRESSING)) {
			if(boost && !player.isSneaking()) {
				boost = false;
			}
		}
	}

	public boolean shouldCancelDamage() {
		if(isState(BendingAbilityState.PROGRESSING)) {
			if(!boost) {
				strength--;
				if(strength <= 0) {
					remove();
				}
			}
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 2.0f, 0.0f);
			return true;
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		if(block != null) {
			block.revertBlock();
		}
		player.removePotionEffect(PotionEffectType.SLOW);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_METAL_BREAK, 2.0f, 0.0f);
		bender.cooldown(this, COOLDOWN);
	}
	
	@Override
	protected long getMaxMillis() {
		return DURATION;
	}
	
	public double getDamageReduction() {
		if(isState(BendingAbilityState.PROGRESSING)) {
			if(boost) {
				return DAMAGE_REDUCTION_BOOST;
			} else {
				return DAMAGE_REDUCTION;
			}
		}
		return 0.0;
	}

	private boolean moveBlocks() {
		if (location == null || player.getWorld() != location.getWorld()) {
			return false;
		}

		if(block != null) {
			block.revertBlock();
		}
		
		Vector direction = player.getEyeLocation().subtract(location).toVector().normalize();
		location = location.add(direction);
		
		if(block == null || location.getBlock() != block.getBlock()) {
			if(!BlockTools.isTransparentToEarthbending(player, location.getBlock())
					|| TempBlock.isTempBlock(location.getBlock())) {
				return false;
			}
			block = TempBlock.makeTemporary(location.getBlock(), material, false);
		}
		
		if(location.distance(player.getEyeLocation()) < 1) {
			if(block != null) {
				block.revertBlock();
			}
			formArmor();
		}
		return true;
	}

	private void formArmor() {
		// Save current player's armor into inventory
		for(ItemStack is : player.getInventory().getArmorContents()) {
			if(is != null) {
				player.getInventory().addItem(is);
			}
		}
		player.getInventory().setArmorContents(null);
		
		ItemStack[] armors = new ItemStack[4];
		if (BlockTools.isIronBendable(player, material)) {
			armors[0] = sign(new ItemStack(Material.IRON_BOOTS, 1));
			armors[1] = sign(new ItemStack(Material.IRON_LEGGINGS, 1));
			armors[2] = sign(new ItemStack(Material.IRON_CHESTPLATE, 1));
			armors[3] = sign(new ItemStack(Material.IRON_HELMET, 1));
		} else {
			armors[0] = sign(new ItemStack(Material.LEATHER_BOOTS, 1));
			armors[1] = sign(new ItemStack(Material.LEATHER_LEGGINGS, 1));
			armors[2] = sign(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
			armors[3] = sign(new ItemStack(Material.LEATHER_HELMET, 1));
		}
		player.getInventory().setArmorContents(armors);
		PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, (int) DURATION / 50, 0);
		player.addPotionEffect(slowness);
		setState(BendingAbilityState.PROGRESSING);
	}

	private static ItemStack sign(ItemStack is) {
		ItemMeta meta = is.getItemMeta();
		meta.setLore(Arrays.asList(NAME));
		is.setItemMeta(meta);
		return is;
	}
	
	public static boolean isArmor(ItemStack is) {
		return is.hasItemMeta() && is.getItemMeta().hasLore() && is.getItemMeta().getLore().contains(NAME);
	}
	
	public static boolean hasEarthArmor(Player player) {
		return AbilityManager.getManager().getInstances(NAME).containsKey(player);
	}
}

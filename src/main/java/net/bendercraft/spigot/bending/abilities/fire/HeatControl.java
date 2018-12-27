package net.bendercraft.spigot.bending.abilities.fire;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.bendercraft.spigot.bending.abilities.*;
import net.bendercraft.spigot.bending.abilities.water.PhaseChange;
import net.bendercraft.spigot.bending.abilities.water.Wave;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;


@ABendingAbility(name = HeatControl.NAME, element = BendingElement.FIRE)
public class HeatControl extends BendingActiveAbility {
	public final static String NAME = "HeatControl";

	@ConfigurationParameter("Extinguish-Range")
	private static double EXT_RANGE = 20;

	@ConfigurationParameter("Melt-Range")
	private static double MELT_RANGE = 17;

	@ConfigurationParameter("Extinguish-Radius")
	private static double RADIUS = 7;

	@ConfigurationParameter("Melt-Radius")
	private static double MELT_RADIUS = 5;

	@ConfigurationParameter("Cook-Time")
	private static long COOK_TIME = 2000;

	private static final Material[] cookables = {
		Material.BEEF,
		Material.CHICKEN,
		Material.COD,
		Material.SALMON,
		Material.PORKCHOP,
		Material.POTATO,
		Material.POISONOUS_POTATO,
		Material.STICK,
		Material.RABBIT,
		Material.MUTTON
	};

	public static final byte FULL = 0x0;

	// In case of cooking some stuff
	private ItemStack items;
	private long time;

	public HeatControl(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			Block block = EntityTools.getTargetBlock(this.player, EXT_RANGE);
			if (BlockTools.isMeltable(block)) {
				melt();
			} else {
				extinguish(EXT_RANGE);
			}
		}
		return false;
	}

	private void melt() {
		Location location = EntityTools.getTargetedLocation(this.player, MELT_RANGE);
		for (Block block : BlockTools.getBlocksAroundPoint(location, MELT_RADIUS)) {
			if (BlockTools.isMeltable(block)) {
				melt(this.player, block); // Don't really like the idea
			}
		}
	}

	private void extinguish(double range) {
		for (Block block : BlockTools.getBlocksAroundPoint(EntityTools.getTargetBlock(this.player, range).getLocation(), RADIUS)) {
			if (ProtectionManager.isLocationProtectedFromBending(this.player, register, block.getLocation())) {
				continue;
			}
			// Do not allow firebender to completly negate lavabend
			if (block.getType() == Material.LAVA && TempBlock.isTempBlock(block)) {
				continue;
			}
			if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
			} else if (block.getType() == Material.LAVA) {
				Levelled data = (Levelled) block.getBlockData();
				if (data.getLevel() == data.getMaximumLevel()) {
					block.setType(Material.OBSIDIAN);
				} else {
					block.setType(Material.COBBLESTONE);
				}
				
			}
		}
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.0f);
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			this.items = this.player.getInventory().getItemInMainHand();
			if (isCookable(this.items.getType())) {
				this.time = this.startedTime;
				
				setState(BendingAbilityState.PROGRESSING);
			}
		}
		return false;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!this.player.isSneaking()) {
			return false;
		}
		return true;
	}
	
	@Override
	public void progress() {
		long now = System.currentTimeMillis();
		if (!this.items.equals(this.player.getInventory().getItemInMainHand())) {
			this.time = now;
			this.items = this.player.getInventory().getItemInMainHand();
		}

		if (!isCookable(this.items.getType())) {
			remove();
			return;
		}

		if (now > (this.time + COOK_TIME)) {
			cook();
			this.time = now;
		}

		this.player.getWorld().playEffect(this.player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 0, 10);
	}

	private static boolean isCookable(Material material) {
		return Arrays.asList(cookables).contains(material);
	}

	private void cook() {
		ItemStack newitem = getCooked(this.items);

		HashMap<Integer, ItemStack> cantfit = this.player.getInventory().addItem(newitem);
		for (int id : cantfit.keySet()) {
			this.player.getWorld().dropItem(this.player.getEyeLocation(), cantfit.get(id));
		}
		int amount = this.items.getAmount();
		if (amount == 1) {
			this.player.getInventory().clear(this.player.getInventory().getHeldItemSlot());
		} else {
			this.items.setAmount(amount - 1);
		}
	}

	private ItemStack getCooked(ItemStack in) {
		ItemStack cooked = new ItemStack(Material.AIR);
		switch (in.getType()) {
			case BEEF:
				cooked.setType(Material.COOKED_BEEF);
				cooked.setAmount(1);
				break;
			case SALMON:
				cooked.setType(Material.COOKED_SALMON);
				cooked.setAmount(1);
				break;
			case COD:
				cooked.setType(Material.COOKED_COD);
				cooked.setAmount(1);
				break;
			case CHICKEN:
				cooked.setType(Material.COOKED_CHICKEN);
				cooked.setAmount(1);
				break;
			case PORKCHOP:
				cooked.setType(Material.COOKED_PORKCHOP);
				cooked.setAmount(1);
				break;
			case POTATO:
			case POISONOUS_POTATO:
				cooked.setType(Material.BAKED_POTATO);
				cooked.setAmount(1);
				break;
			case STICK:
				cooked.setType(Material.TORCH);
				cooked.setAmount(4);
				break;
			case RABBIT:
				cooked.setType(Material.COOKED_RABBIT);
				cooked.setAmount(1);
				break;
			case MUTTON:
				cooked.setType(Material.COOKED_MUTTON);
				cooked.setAmount(1);
				break;
			default:
				break;
		}
		return cooked;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	// Copy from Melt (now deleted)
	public static void melt(Player player, Block block) {
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(PhaseChange.NAME);
		if (ProtectionManager.isLocationProtectedFromBending(player, register, block.getLocation())) {
			return;
		}

		if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			return;
		}
		
		//Either block is a tempblock and a global one, or not at all
		if(BlockTools.isMeltable(block) && TempBlock.isTempBlock(block) && TempBlock.isGlobalTempBlock(TempBlock.get(block))) {
			TempBlock.revertBlock(block);
		} else if (BlockTools.isMeltable(block) && !TempBlock.isTempBlock(block) && !TempBlock.isTempBlock(block) && ! TempBlock.isTouchingTempBlock(block)) {
			if (block.getType() == Material.SNOW) {
				block.setType(Material.AIR);
				return;
			}
			if (PhaseChange.isFrozen(block)) {
				PhaseChange.thawThenRemove(block);
			} else {
				block.setType(Material.WATER);
			}
		}
	}

	public static void evaporate(Player player, Block block) {
		RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(NAME);
		if (ProtectionManager.isLocationProtectedFromBending(player, register, block.getLocation())) {
			return;
		}
		if (block.getType() == Material.WATER && !TempBlock.isTempBlock(block) && !TempBlock.isTempBlock(block) && ! TempBlock.isTouchingTempBlock(block)) {
			block.setType(Material.AIR);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
		}
	}

	@Override
	public void stop() {
		
	}
}

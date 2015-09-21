package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.abilities.water.Melt;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

/**
 * State progressing means that the player is cooking something. Other stuff don't need a progress
 *
 * @author Noko
 */
public class HeatControl extends BendingActiveAbility {
	
	@ConfigurationParameter ("Extinguish-Range")
	private static double EXT_RANGE = 20;
	
	@ConfigurationParameter ("Melt-Range")
	private static double MELT_RANGE = 17;

	@ConfigurationParameter ("Extinguish-Radius")
	private static double RADIUS = 7;
	
	@ConfigurationParameter ("Melt-Radius")
	private static double MELT_RADIUS = 5;

	@ConfigurationParameter ("Extinguish-Cooldown")
	public static long EXT_COOLDOWN = 1000;
	
	@ConfigurationParameter ("Cook-Time")
	private static long COOK_TIME = 2000;

	@ConfigurationParameter ("Cook-Cooldown")
	private static long COOK_COOLDOWN = 0;

	private static final Material[] cookables = { Material.RAW_BEEF, Material.RAW_CHICKEN, Material.RAW_FISH, Material.PORK,
			Material.POTATO_ITEM, Material.POISONOUS_POTATO, Material.STICK, Material.RABBIT, Material.MUTTON };

	public static final byte FULL = 0x0;

	// In case of cooking some stuff
	private ItemStack items;
	private long time;
	
	public HeatControl (Player player) {
		super(player, null);
	}

	@Override
	public boolean swing () {
		switch (this.state) {
			case None:
			case CannotStart:
			case Ended:
			case Removed:
				return false;
			default:
				double range = PluginTools.firebendingDayAugment(EXT_RANGE, this.player.getWorld());
				Block block = EntityTools.getTargetBlock(this.player, range);
				if (BlockTools.isMeltable(block)) {
					melt();
				}
				else {
					extinguish(range);
				}
				
				this.bender.cooldown(BendingAbilities.HeatControl, EXT_COOLDOWN);
				return false;
		}
	}
	
	private void melt () {
		Location location = EntityTools.getTargetedLocation(this.player,
				(int) PluginTools.firebendingDayAugment(MELT_RANGE, this.player.getWorld()));
		for (Block block : BlockTools.getBlocksAroundPoint(location,
				(int) PluginTools.firebendingDayAugment(MELT_RADIUS, this.player.getWorld()))) {
			if (BlockTools.isMeltable(block)) {
				Melt.melt(this.player, block); // Don't really like the idea
			}
		}
	}

	@SuppressWarnings ("deprecation")
	private void extinguish (double range) {
		double radius = PluginTools.firebendingDayAugment(RADIUS, this.player.getWorld());
		
		for (Block block : BlockTools.getBlocksAroundPoint(EntityTools.getTargetBlock(this.player, range).getLocation(),
				radius)) {
			if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.Blaze, block.getLocation())) {
				continue;
			}
			//Do not allow firebender to completly negate lavabend
			if (LavaTrain.isLavaPart(block)) {
				continue;
			}
			if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}
			else if (block.getType() == Material.STATIONARY_LAVA) {
				block.setType(Material.OBSIDIAN);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}
			else if (block.getType() == Material.LAVA) {
				if (block.getData() == FULL) {
					block.setType(Material.OBSIDIAN);
				}
				else {
					block.setType(Material.COBBLESTONE);
				}
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}
		}
	}

	@Override
	public boolean sneak () {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;
			case CanStart:
				this.items = this.player.getItemInHand();
				if (isCookable(this.items.getType())) {
					this.time = this.startedTime;
					AbilityManager.getManager().addInstance(this);
					setState(BendingAbilityState.Progressing);
				}
				return false;
			default:
				return false;
		}
	}
	
	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}
		
		if (!this.player.isSneaking()) {
			return false;
		}

		long now = System.currentTimeMillis();
		if (!this.items.equals(this.player.getItemInHand())) {
			this.time = now;
			this.items = this.player.getItemInHand();
		}
		
		if (!isCookable(this.items.getType())) {
			return false;
		}
		
		if (now > (this.time + COOK_TIME)) {
			cook();
			this.time = now;
		}
		
		this.player.getWorld().playEffect(this.player.getEyeLocation(), Effect.MOBSPAWNER_FLAMES, 0, 10);
		return true;
	}
	
	private static boolean isCookable (Material material) {
		return Arrays.asList(cookables).contains(material);
	}
	
	private void cook () {
		ItemStack newitem = getCooked(this.items.getType());
		
		HashMap<Integer, ItemStack> cantfit = this.player.getInventory().addItem(newitem);
		for (int id : cantfit.keySet()) {
			this.player.getWorld().dropItem(this.player.getEyeLocation(), cantfit.get(id));
		}
		int amount = this.items.getAmount();
		if (amount == 1) {
			this.player.getInventory().clear(this.player.getInventory().getHeldItemSlot());
		}
		else {
			this.items.setAmount(amount - 1);
		}
	}
	
	private ItemStack getCooked (Material material) {
		ItemStack cooked = new ItemStack(Material.AIR);
		switch (material) {
			case RAW_BEEF:
				cooked.setType(Material.COOKED_BEEF);
				cooked.setAmount(1);
				break;
			case RAW_FISH:
				cooked.setType(Material.COOKED_FISH);
				cooked.setAmount(1);
			case RAW_CHICKEN:
				cooked.setType(Material.COOKED_CHICKEN);
				cooked.setAmount(1);
				break;
			case PORK:
				cooked.setType(Material.GRILLED_PORK);
				cooked.setAmount(1);
				break;
			case POTATO_ITEM:
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
			default:
				break;
		}
		return cooked;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public BendingAbilities getAbilityType () {
		return BendingAbilities.HeatControl;
	}
}

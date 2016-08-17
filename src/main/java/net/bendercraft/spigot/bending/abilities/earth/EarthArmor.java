package net.bendercraft.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
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
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = EarthArmor.NAME, element = BendingElement.EARTH)
public class EarthArmor extends BendingActiveAbility {
	public final static String NAME = "EarthArmor";
	
	@ConfigurationParameter("Duration")
	private static long DURATION = 60000;

	@ConfigurationParameter("Strength")
	private static int STRENGTH = 2;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 60000;

	@ConfigurationParameter("Range")
	private static int RANGE = 7;

	private Block headblock, legsblock;
	private Location headblocklocation, legsblocklocation;
	
	private long time;
	
	private boolean formed = false;
	private boolean complete = false;

	private static long interval = 2000;

	private List<EarthColumn> columns = new LinkedList<EarthColumn>();

	public EarthArmor(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		//EarthArmor
		headblock = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending());
		if (BlockTools.getEarthbendableBlocksLength(player, headblock, new Vector(0, -1, 0), 2) >= 2) {
			legsblock = headblock.getRelative(BlockFace.DOWN);
			headblocklocation = this.headblock.getLocation();
			legsblocklocation = this.legsblock.getLocation();
			Block oldheadblock, oldlegsblock;
			oldheadblock = headblock;
			oldlegsblock = legsblock;
			if (!moveBlocks()) {
				return false;
			}
			if (Settings.REVERSE_BENDING) {
				BlockTools.addTempAirBlock(oldheadblock);
				BlockTools.addTempAirBlock(oldlegsblock);
			} else {
				oldheadblock.setType(Material.AIR);
				oldlegsblock.setType(Material.AIR);
			}
			
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		// EarthShield
		Block base = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending());

		List<Block> blocks = new ArrayList<Block>();
		Location location = base.getLocation();
		Location loc1 = location.clone();
		Location loc2 = location.clone();
		Location testloc, testloc2;
		double factor = 3;
		double factor2 = 4;
		int height1 = 3;
		int height2 = 2;
		for (double angle = 0; angle <= 360; angle += 20) {
			testloc = loc1.clone().add(factor * Math.cos(Math.toRadians(angle)), 1, factor * Math.sin(Math.toRadians(angle)));
			for (int y = 0; y < EarthColumn.HEIGHT - height1; y++) {
				testloc = testloc.clone().add(0, -1, 0);
				if (BlockTools.isEarthbendable(player, testloc.getBlock())) {
					if (!blocks.contains(testloc.getBlock())) {
						EarthColumn ec = new EarthColumn();
						if(ec.init(this.player, testloc, height1+y-1)) {
							columns.add(ec);
						}
					}
					blocks.add(testloc.getBlock());
					break;
				}
			}

			testloc2 = loc2.clone().add(factor2 * Math.cos(Math.toRadians(angle)), 1, factor2 * Math.sin(Math.toRadians(angle)));
			for (int y = 0; y < EarthColumn.HEIGHT - height2; y++) {
				testloc2 = testloc2.clone().add(0, -1, 0);
				if (BlockTools.isEarthbendable(player, testloc2.getBlock())) {
					if (!blocks.contains(testloc2.getBlock())) {
						EarthColumn ec = new EarthColumn();
						if(ec.init(player, testloc2, height2+y-1)) {
							columns.add(ec);
						}
					}
					blocks.add(testloc2.getBlock());
					break;
				}
			}
		}

		if (!columns.isEmpty()) {
			bender.cooldown(NAME, COOLDOWN);
			setState(BendingAbilityState.PROGRESSING);
		}

		return false;
	}

	private boolean moveBlocks() {
		if (headblock == null || player.getWorld() != headblock.getWorld()) {
			cancel();
			return false;
		}

		Location headlocation = player.getEyeLocation();
		Location legslocation = player.getLocation();
		Vector headdirection = headlocation.toVector().subtract(headblocklocation.toVector()).normalize().multiply(.5);
		Vector legsdirection = legslocation.toVector().subtract(legsblocklocation.toVector()).normalize().multiply(.5);

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

		if (BlockTools.isTransparentToEarthbending(player, newheadblock) && !newheadblock.isLiquid()) {
			BlockTools.breakBlock(newheadblock);
		} else if (!BlockTools.isEarthbendable(player, register, newheadblock) 
				&& !newheadblock.isLiquid() 
				&& newheadblock.getType() != Material.AIR) {
			cancel();
			return false;
		}

		if (BlockTools.isTransparentToEarthbending(player, newlegsblock) && !newlegsblock.isLiquid()) {
			BlockTools.breakBlock(newlegsblock);
		} else if (!BlockTools.isEarthbendable(player, register, newlegsblock) 
				&& !newlegsblock.isLiquid() 
				&& newlegsblock.getType() != Material.AIR) {
			cancel();
			return false;
		}

		if (headblock.getLocation().distance(player.getEyeLocation()) > RANGE 
				|| legsblock.getLocation().distance(player.getLocation()) > RANGE) {
			cancel();
			return false;
		}

		if (!newheadblock.equals(headblock)) {
			TempBlock.makeTemporary(newheadblock, headblock.getType(), false);
			TempBlock.revertBlock(headblock);
		}

		if (!newlegsblock.equals(legsblock)) {
			TempBlock.makeTemporary(newlegsblock, legsblock.getType(), false);
			TempBlock.revertBlock(legsblock);
		}

		headblock = newheadblock;
		legsblock = newlegsblock;

		return true;
	}

	private void cancel() {
		if (Settings.REVERSE_BENDING) {
			TempBlock.revertBlock(headblock);
			TempBlock.revertBlock(legsblock);
		} else {
			headblock.breakNaturally();
			legsblock.breakNaturally();
		}
	}

	private boolean inPosition() {
		if(headblock == null || legsblock == null) {
			return false;
		}
		if (headblock.equals(player.getEyeLocation().getBlock()) && legsblock.equals(player.getLocation().getBlock())) {
			return true;
		}
		return false;
	}

	private void formArmor() {
		// Save current player's armor into inventory
		for(ItemStack is : player.getInventory().getArmorContents()) {
			if(is != null) {
				player.getInventory().addItem(is);
			}
		}
		player.getInventory().setArmorContents(null);
		
		boolean iron = false;
		ItemStack[] armors = new ItemStack[4];
		if (BlockTools.isIronBendable(player, legsblock.getType())) {
			armors[0] = sign(new ItemStack(Material.IRON_BOOTS, 1));
			armors[1] = sign(new ItemStack(Material.IRON_LEGGINGS, 1));
			iron = true;
		} else {
			armors[0] = sign(new ItemStack(Material.LEATHER_BOOTS, 1));
			armors[1] = sign(new ItemStack(Material.LEATHER_LEGGINGS, 1));
		}

		if (BlockTools.isIronBendable(player, headblock.getType())) {
			armors[2] = sign(new ItemStack(Material.IRON_CHESTPLATE, 1));
			armors[3] = sign(new ItemStack(Material.IRON_HELMET, 1));
			iron = true;
		} else {
			armors[2] = sign(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
			armors[3] = sign(new ItemStack(Material.LEATHER_HELMET, 1));
		}

		if (!iron) {
			//PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) DURATION / 50, STRENGTH - 1);
			//player.addPotionEffect(resistance);
			PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, (int) DURATION / 50, 0);
			player.addPotionEffect(slowness);
		}
		player.getInventory().setArmorContents(armors);

		formed = true;
		
		TempBlock.revertBlock(headblock);
		TempBlock.revertBlock(legsblock);
	}

	@Override
	public void progress() {
		if(getState() != BendingAbilityState.PROGRESSING) {
			remove();
			return;
		}

		if(!columns.isEmpty()) {
			List<EarthColumn> test = new LinkedList<EarthColumn>(columns);
			for(EarthColumn column : test) {
				if(!column.progress()) {
					columns.remove(column);
				}
			}
			if(columns.isEmpty()) {
				remove();
				return;
			}
		}

		if (formed) {
			if ((System.currentTimeMillis() > (startedTime + DURATION)) && !complete) {
				complete = true;
				return;
			}
			if (System.currentTimeMillis() > (startedTime + COOLDOWN)) {
				remove();
				return;
			}
		} else if (System.currentTimeMillis() > (time + interval)) {
			if (inPosition()) {
				formArmor();
			} else {
				if(!moveBlocks()) {
					remove();
					return;
				}
			}
		}
	}

	public static boolean hasEarthArmor(Player player) {
		return AbilityManager.getManager().getInstances(NAME).containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		for(EarthColumn column : columns) {
			column.remove();
		}
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
}

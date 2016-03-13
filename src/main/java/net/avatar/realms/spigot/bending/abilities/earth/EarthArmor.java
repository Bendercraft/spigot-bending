package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.TempBlock;

@ABendingAbility(name = EarthArmor.NAME, element = BendingElement.EARTH)
public class EarthArmor extends BendingActiveAbility {
	public final static String NAME = "EarthArmor";
	
	@ConfigurationParameter("Duration")
	private static long DURATION = 59000;

	@ConfigurationParameter("Strength")
	private static int STRENGTH = 2;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 60000;

	@ConfigurationParameter("Range")
	private static int RANGE = 7;

	private Block headblock, legsblock;
	private Location headblocklocation, legsblocklocation;
	private Material headtype, legstype;
	private byte headdata, legsdata;
	private long time, starttime;
	private boolean formed = false;
	private boolean complete = false;
	public ItemStack[] oldarmor;
	public List<ItemStack> armors = new ArrayList<ItemStack>(4);

	private static long interval = 2000;

	private List<EarthColumn> columns = new LinkedList<EarthColumn>();

	public EarthArmor(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean swing() {

		//EarthArmor
		
		if (this.bender.isOnCooldown(NAME)) {
			return false;
		}

		this.headblock = EntityTools.getTargetBlock(this.player, RANGE, BlockTools.getTransparentEarthbending());
		if (BlockTools.getEarthbendableBlocksLength(this.player, this.headblock, new Vector(0, -1, 0), 2) >= 2) {
			this.legsblock = this.headblock.getRelative(BlockFace.DOWN);
			this.headtype = this.headblock.getType();
			this.legstype = this.legsblock.getType();
			this.headdata = this.headblock.getData();
			this.legsdata = this.legsblock.getData();
			this.headblocklocation = this.headblock.getLocation();
			this.legsblocklocation = this.legsblock.getLocation();
			Block oldheadblock, oldlegsblock;
			oldheadblock = this.headblock;
			oldlegsblock = this.legsblock;
			if (!moveBlocks()) {
				return false;
			}
			if (Settings.REVERSE_BENDING) {
				BlockTools.addTempAirBlock(oldheadblock);
				BlockTools.addTempAirBlock(oldlegsblock);
			} else {
				BlockTools.removeBlock(oldheadblock);
				BlockTools.removeBlock(oldlegsblock);
			}
			
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		// EarthShield

		if (getState() != BendingAbilityState.START) {
			return false;
		}

		if (this.bender.isOnCooldown(NAME)) {
			return false;
		}

		Block base = EntityTools.getTargetBlock(this.player, RANGE, BlockTools.getTransparentEarthbending());

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
				if (BlockTools.isEarthbendable(this.player, testloc.getBlock())) {
					if (!blocks.contains(testloc.getBlock())) {
						EarthColumn ec = new EarthColumn();
						if(ec.init(this.player, testloc, height1+y-1)) {
							this.columns.add(ec);
						}
					}
					blocks.add(testloc.getBlock());
					break;
				}
			}

			testloc2 = loc2.clone().add(factor2 * Math.cos(Math.toRadians(angle)), 1, factor2 * Math.sin(Math.toRadians(angle)));
			for (int y = 0; y < EarthColumn.HEIGHT - height2; y++) {
				testloc2 = testloc2.clone().add(0, -1, 0);
				if (BlockTools.isEarthbendable(this.player, testloc2.getBlock())) {
					if (!blocks.contains(testloc2.getBlock())) {
						EarthColumn ec = new EarthColumn();
						if(ec.init(this.player, testloc2, height2+y-1)) {
							this.columns.add(ec);
						}
					}
					blocks.add(testloc2.getBlock());
					break;
				}
			}
		}

		if (!this.columns.isEmpty()) {
			this.bender.cooldown(NAME, COOLDOWN);
			setState(BendingAbilityState.PROGRESSING);
		}

		return false;
	}

	private boolean moveBlocks() {
		if (headblock == null || player.getWorld() != this.headblock.getWorld()) {
			cancel();
			return false;
		}

		Location headlocation = this.player.getEyeLocation();
		Location legslocation = this.player.getLocation();
		Vector headdirection = headlocation.toVector().subtract(this.headblocklocation.toVector()).normalize().multiply(.5);
		Vector legsdirection = legslocation.toVector().subtract(this.legsblocklocation.toVector()).normalize().multiply(.5);

		Block newheadblock = this.headblock;
		Block newlegsblock = this.legsblock;

		if (!headlocation.getBlock().equals(this.headblock)) {
			this.headblocklocation = this.headblocklocation.clone().add(headdirection);
			newheadblock = this.headblocklocation.getBlock();
		}
		if (!legslocation.getBlock().equals(this.legsblock)) {
			this.legsblocklocation = this.legsblocklocation.clone().add(legsdirection);
			newlegsblock = this.legsblocklocation.getBlock();
		}

		if (BlockTools.isTransparentToEarthbending(this.player, newheadblock) && !newheadblock.isLiquid()) {
			BlockTools.breakBlock(newheadblock);
		} else if (!BlockTools.isEarthbendable(this.player, NAME, newheadblock) && !newheadblock.isLiquid() && (newheadblock.getType() != Material.AIR)) {
			cancel();
			return false;
		}

		if (BlockTools.isTransparentToEarthbending(this.player, newlegsblock) && !newlegsblock.isLiquid()) {
			BlockTools.breakBlock(newlegsblock);
		} else if (!BlockTools.isEarthbendable(this.player, NAME, newlegsblock) && !newlegsblock.isLiquid() && (newlegsblock.getType() != Material.AIR)) {
			cancel();
			return false;
		}

		if ((this.headblock.getLocation().distance(this.player.getEyeLocation()) > RANGE) || (this.legsblock.getLocation().distance(this.player.getLocation()) > RANGE)) {
			cancel();
			return false;
		}

		if (!newheadblock.equals(this.headblock)) {
			//new TempBlock(newheadblock, this.headtype, this.headdata);
			TempBlock.makeTemporary(newheadblock, headtype, headdata);
			TempBlock.revertBlock(this.headblock);
		}

		if (!newlegsblock.equals(this.legsblock)) {
			//new TempBlock(newlegsblock, this.legstype, this.legsdata);
			TempBlock.makeTemporary(newlegsblock, this.legstype, this.legsdata);
			TempBlock.revertBlock(this.legsblock);
		}

		this.headblock = newheadblock;
		this.legsblock = newlegsblock;

		return true;
	}

	private void cancel() {
		if (Settings.REVERSE_BENDING) {
			TempBlock.revertBlock(this.headblock);
			TempBlock.revertBlock(this.legsblock);
		} else {
			this.headblock.breakNaturally();
			this.legsblock.breakNaturally();
		}
	}

	private boolean inPosition() {
		if(this.headblock == null || this.legsblock == null) {
			return false;
		}
		if (this.headblock.equals(this.player.getEyeLocation().getBlock()) && this.legsblock.equals(this.player.getLocation().getBlock())) {
			return true;
		}
		return false;
	}

	private void formArmor() {
		TempBlock.revertBlock(this.headblock);
		TempBlock.revertBlock(this.legsblock);
		short cptIroned = 0;
		this.oldarmor = this.player.getInventory().getArmorContents();
		if (BlockTools.isIronBendable(this.player, this.legstype)) {
			ItemStack is = new ItemStack(Material.IRON_BOOTS, 1);
			this.armors.add(is);
			is = new ItemStack(Material.IRON_LEGGINGS, 1);
			this.armors.add(is);
			cptIroned++;
		} else {
			this.armors.add(new ItemStack(Material.LEATHER_BOOTS, 1));
			this.armors.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
		}

		if (BlockTools.isIronBendable(this.player, this.headtype)) {
			ItemStack is = new ItemStack(Material.IRON_CHESTPLATE, 1);
			this.armors.add(is);
			is = new ItemStack(Material.IRON_HELMET, 1);
			this.armors.add(is);
			cptIroned++;
		} else {
			this.armors.add(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
			this.armors.add(new ItemStack(Material.LEATHER_HELMET, 1));
		}

		if (cptIroned == 0) {
			PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) DURATION / 50, STRENGTH - 1);
			this.player.addPotionEffect(resistance);
			PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, (int) DURATION / 50, 0);
			this.player.addPotionEffect(slowness);
		}
		ItemStack[] ar = this.armors.toArray(new ItemStack[this.armors.size()]);
		this.player.getInventory().setArmorContents(ar);

		this.formed = true;
		this.starttime = System.currentTimeMillis();
	}

	@Override
	public void progress() {
		if(getState() != BendingAbilityState.PROGRESSING) {
			remove();
			return;
		}

		if(!this.columns.isEmpty()) {
			List<EarthColumn> test = new LinkedList<EarthColumn>(this.columns);
			for(EarthColumn column : test) {
				if(!column.progress()) {
					this.columns.remove(column);
				}
			}
			if(this.columns.isEmpty()) {
				remove();
				return;
			}
		}

		if (this.formed) {

			long testInt = 0;
			testInt = this.starttime + DURATION;

			if (System.currentTimeMillis() > testInt && !this.complete) {
				this.complete = true;
				removeEffect();
				return;
			}
			if (System.currentTimeMillis() > (this.starttime + COOLDOWN)) {
				remove();
				return;
			}
		} else if (System.currentTimeMillis() > (this.time + interval)) {
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

	public static EarthArmor getEarthArmor(Player pl) {
		return (EarthArmor) AbilityManager.getManager().getInstances(NAME).get(pl);
	}

	public boolean isArmor(ItemStack is) {
		for (ItemStack part : this.armors) {
			if (part.getType().equals(is.getType())) {
				return true;
			}
		}
		return false;
	}

	private void removeEffect() {
		if (this.oldarmor != null) {
			this.player.getInventory().setArmorContents(this.oldarmor);
		}
	}

	public static void removeEffect(Player player) {
		EarthArmor earthArmor = (EarthArmor) AbilityManager.getManager().getInstances(NAME).get(player);
		if (earthArmor != null) {
			earthArmor.removeEffect();
		}
	}

	public static boolean canRemoveArmor(Player player) {
		if (AbilityManager.getManager().getInstances(NAME).containsKey(player)) {
			EarthArmor earthArmor = (EarthArmor) AbilityManager.getManager().getInstances(NAME).get(player);
			if (System.currentTimeMillis() < (earthArmor.starttime + DURATION)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		for(EarthColumn column : this.columns) {
			column.remove();
		}
	}
}

package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.List;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@BendingAbility(name = "Earth Armor", bind = BendingAbilities.EarthArmor, element = BendingElement.Earth)
public class EarthArmor extends BendingActiveAbility {
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
	private Material headtype, legstype;
	private byte headdata, legsdata;
	private long time, starttime;
	private boolean formed = false;
	private boolean complete = false;
	public ItemStack[] oldarmor;
	public List<ItemStack> armors = new ArrayList<ItemStack>(4);

	private static long interval = 2000;

	public EarthArmor(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		// EarthArmor

		if (state != BendingAbilityState.CanStart) {
			return false;
		}

		if (bender.isOnCooldown(BendingAbilities.EarthArmor)) {
			return false;
		}

		this.headblock = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending());
		if (BlockTools.getEarthbendableBlocksLength(player, this.headblock, new Vector(0, -1, 0), 2) >= 2) {
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
			AbilityManager.getManager().addInstance(this);
			state = BendingAbilityState.Progressing;
		}
		return false;
	}

	@Override
	public boolean sneak() {
		// EarthShield

		if (state != BendingAbilityState.CanStart) {
			return false;
		}

		if (bender.isOnCooldown(BendingAbilities.EarthArmor)) {
			return false;
		}

		Block base = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending());

		ArrayList<Block> blocks = new ArrayList<Block>();
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
			testloc2 = loc2.clone().add(factor2 * Math.cos(Math.toRadians(angle)), 1, factor2 * Math.sin(Math.toRadians(angle)));
			for (int y = 0; y < EarthColumn.HEIGHT - height1; y++) {
				testloc = testloc.clone().add(0, -1, 0);
				if (BlockTools.isEarthbendable(player, testloc.getBlock())) {
					if (!blocks.contains(testloc.getBlock())) {
						new EarthColumn(player, testloc, height1 + y - 1, null);
					}
					blocks.add(testloc.getBlock());
					break;
				}
			}
			for (int y = 0; y < EarthColumn.HEIGHT - height2; y++) {
				testloc2 = testloc2.clone().add(0, -1, 0);
				if (BlockTools.isEarthbendable(player, testloc2.getBlock())) {
					if (!blocks.contains(testloc2.getBlock())) {
						new EarthColumn(player, testloc2, height2 + y - 1, null);
					}
					blocks.add(testloc2.getBlock());
					break;
				}
			}
		}

		if (!blocks.isEmpty()) {
			bender.cooldown(BendingAbilities.EarthArmor, COOLDOWN);
			AbilityManager.getManager().addInstance(this);
			state = BendingAbilityState.Progressing;
		}

		return false;
	}

	private boolean moveBlocks() {
		if (this.player.getWorld() != this.headblock.getWorld()) {
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
		} else if (!BlockTools.isEarthbendable(this.player, BendingAbilities.EarthArmor, newheadblock) && !newheadblock.isLiquid() && (newheadblock.getType() != Material.AIR)) {
			cancel();
			return false;
		}

		if (BlockTools.isTransparentToEarthbending(this.player, newlegsblock) && !newlegsblock.isLiquid()) {
			BlockTools.breakBlock(newlegsblock);
		} else if (!BlockTools.isEarthbendable(this.player, BendingAbilities.EarthArmor, newlegsblock) && !newlegsblock.isLiquid() && (newlegsblock.getType() != Material.AIR)) {
			cancel();
			return false;
		}

		if ((this.headblock.getLocation().distance(this.player.getEyeLocation()) > RANGE) || (this.legsblock.getLocation().distance(this.player.getLocation()) > RANGE)) {
			cancel();
			return false;
		}

		if (!newheadblock.equals(this.headblock)) {
			new TempBlock(newheadblock, this.headtype, this.headdata);
			TempBlock.revertBlock(this.headblock);
		}

		if (!newlegsblock.equals(this.legsblock)) {
			new TempBlock(newlegsblock, this.legstype, this.legsdata);
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
			new TempPotionEffect(this.player, resistance);
			PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, (int) DURATION / 50, 0);
			new TempPotionEffect(this.player, slowness);
		}
		ItemStack[] ar = this.armors.toArray(new ItemStack[this.armors.size()]);
		this.player.getInventory().setArmorContents(ar);

		this.formed = true;
		this.starttime = System.currentTimeMillis();
	}

	@Override
	public boolean progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			cancel();
			removeEffect();
			return false;
		}

		if (this.formed) {
			if ((System.currentTimeMillis() > (this.starttime + DURATION)) && !this.complete) {
				this.complete = true;
				removeEffect();
				return true;
			}
			if (System.currentTimeMillis() > (this.starttime + COOLDOWN)) {
				return false;
			}
		} else if (System.currentTimeMillis() > (this.time + interval)) {
			if (inPosition()) {
				formArmor();
			} else {
				return moveBlocks();
			}
		}
		return true;
	}

	public static boolean hasEarthArmor(Player player) {
		return AbilityManager.getManager().getInstances(BendingAbilities.EarthArmor).containsKey(player);
	}

	public static EarthArmor getEarthArmor(Player pl) {
		return (EarthArmor) AbilityManager.getManager().getInstances(BendingAbilities.EarthArmor).get(pl);
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
		this.player.getInventory().setArmorContents(this.oldarmor);
	}

	public static void removeEffect(Player player) {
		if (!AbilityManager.getManager().getInstances(BendingAbilities.EarthArmor).containsKey(player)) {
			return;
		}
		EarthArmor earthArmor = (EarthArmor) AbilityManager.getManager().getInstances(BendingAbilities.EarthArmor);
		earthArmor.removeEffect();
	}

	public static boolean canRemoveArmor(Player player) {
		if (AbilityManager.getManager().getInstances(BendingAbilities.EarthArmor).containsKey(player)) {
			EarthArmor earthArmor = (EarthArmor) AbilityManager.getManager().getInstances(BendingAbilities.EarthArmor).get(player);
			if (System.currentTimeMillis() < (earthArmor.starttime + DURATION)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}
}

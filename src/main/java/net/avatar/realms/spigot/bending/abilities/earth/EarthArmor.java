package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
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

@BendingAbility(name="Earth Armor", element=BendingType.Earth)
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

	@SuppressWarnings("deprecation")
	public EarthArmor (Player player, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player)) {
			return;
		}

		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(Abilities.EarthArmor)) {
			return;
		}

		this.player = player;

		this.headblock = EntityTools.getTargetBlock(player, range, BlockTools.getTransparentEarthbending());
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
				return;
			}
			if (ConfigManager.reverseearthbending) {
				BlockTools.addTempAirBlock(oldheadblock);
				BlockTools.addTempAirBlock(oldlegsblock);
			}
			else {
				BlockTools.removeBlock(oldheadblock);
				BlockTools.removeBlock(oldlegsblock);
			}
			instances.put(player, this);
		}
	}

	private boolean moveBlocks () {
		if (this.player.getWorld() != this.headblock.getWorld()) {
			cancel();
			return false;
		}

		Location headlocation = this.player.getEyeLocation();
		Location legslocation = this.player.getLocation();
		Vector headdirection = headlocation.toVector().subtract(this.headblocklocation.toVector())
				.normalize().multiply(.5);
		Vector legsdirection = legslocation.toVector().subtract(this.legsblocklocation.toVector())
				.normalize().multiply(.5);

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
		}
		else if (!BlockTools.isEarthbendable(this.player, Abilities.EarthArmor, newheadblock)
				&& !newheadblock.isLiquid() && (newheadblock.getType() != Material.AIR)) {
			cancel();
			return false;
		}

		if (BlockTools.isTransparentToEarthbending(this.player, newlegsblock) && !newlegsblock.isLiquid()) {
			BlockTools.breakBlock(newlegsblock);
		}
		else if (!BlockTools.isEarthbendable(this.player, Abilities.EarthArmor, newlegsblock)
				&& !newlegsblock.isLiquid() && (newlegsblock.getType() != Material.AIR)) {
			cancel();
			return false;
		}

		if ((this.headblock.getLocation().distance(this.player.getEyeLocation()) > range)
				|| (this.legsblock.getLocation().distance(this.player.getLocation()) > range)) {
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

	private void cancel () {
		if (ConfigManager.reverseearthbending) {
			TempBlock.revertBlock(this.headblock);
			TempBlock.revertBlock(this.legsblock);
		}
		else {
			this.headblock.breakNaturally();
			this.legsblock.breakNaturally();
		}
	}

	private boolean inPosition () {
		if (this.headblock.equals(this.player.getEyeLocation().getBlock())
				&& this.legsblock.equals(this.player.getLocation().getBlock())) {
			return true;
		}
		return false;
	}

	private void formArmor () {
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
		}
		else {
			this.armors.add(new ItemStack(Material.LEATHER_BOOTS, 1));
			this.armors.add(new ItemStack(Material.LEATHER_LEGGINGS, 1));
		}

		if (BlockTools.isIronBendable(this.player, this.headtype)) {
			ItemStack is = new ItemStack(Material.IRON_CHESTPLATE, 1);
			this.armors.add(is);
			is = new ItemStack(Material.IRON_HELMET, 1);
			this.armors.add(is);
			cptIroned++;
		}
		else {
			this.armors.add(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
			this.armors.add(new ItemStack(Material.LEATHER_HELMET, 1));
		}

		if (cptIroned == 0) {
			PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int)duration / 50, strength - 1);
			new TempPotionEffect(this.player, resistance);
			PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, (int)duration / 50, 0);
			new TempPotionEffect(this.player, slowness);
		}
		ItemStack[] ar = this.armors.toArray(new ItemStack[this.armors.size()]);
		this.player.getInventory().setArmorContents(ar);

		this.formed = true;
		this.starttime = System.currentTimeMillis();
	}

	private void remove () {
		instances.remove(this.player);
	}

	private boolean progress () {
		if (this.player.isDead() || !this.player.isOnline()) {
			cancel();
			removeEffect();
			return false;
		}

		if (this.formed) {
			if ((System.currentTimeMillis() > (this.starttime + duration)) && !this.complete) {
				this.complete = true;
				removeEffect();
				return true;
			}
			if (System.currentTimeMillis() > (this.starttime + cooldown)) {
				return false;
			}
		}
		else if (System.currentTimeMillis() > (this.time + interval)) {
			if (inPosition()) {
				formArmor();
			}
			else {
				return moveBlocks();
			}
		}
		return true;
	}

	public static void progressAll () {
		List<EarthArmor> toRemove = new LinkedList<EarthArmor>();
		for (EarthArmor armor : instances.values()) {
			boolean keep = armor.progress();
			if (!keep) {
				toRemove.add(armor);
			}
		}
		for (EarthArmor armor : toRemove) {
			armor.remove();
		}
	}

	public static boolean hasEarthArmor (Player player) {
		return instances.containsKey(player);
	}

	public static EarthArmor getEarthArmor (Player pl) {
		return instances.get(pl);
	}

	public boolean isArmor (ItemStack is) {
		for (ItemStack part : this.armors) {
			if (part.getType().equals(is.getType())) {
				return true;
			}
		}
		return false;
	}

	private void removeEffect () {
		this.player.getInventory().setArmorContents(this.oldarmor);
	}

	public static void removeEffect (Player player) {
		if (!instances.containsKey(player)) {
			return;
		}
		instances.get(player).removeEffect();
	}

	public static void removeAll () {
		for (EarthArmor eartharmor : instances.values()) {
			eartharmor.cancel();
			eartharmor.removeEffect();
		}
		instances.clear();
	}

	public static boolean canRemoveArmor (Player player) {
		if (instances.containsKey(player)) {
			EarthArmor eartharmor = instances.get(player);
			if (System.currentTimeMillis() < (eartharmor.starttime + duration)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IAbility getParent () {
		return this.parent;
	}
}

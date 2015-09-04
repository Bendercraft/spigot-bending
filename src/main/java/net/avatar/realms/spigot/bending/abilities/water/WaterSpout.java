package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempBlock;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name="Water Spout", element=BendingType.Water)
public class WaterSpout extends ActiveAbility {
	
	@ConfigurationParameter ("Rotation-Speed")
	public static int SPEED = 4;
	
	@ConfigurationParameter ("Height")
	private static int HEIGHT = 18;
	
	@ConfigurationParameter ("Cooldown")
	private static long COOLDOWN = 0;
	
	//	private static final byte full = 0x0;
	private int currentCardinalPoint = 0;
	private Block base;
	private BlockState baseState;
	private Map<Block, BlockState> blocks;
	private FlyingPlayer flying;
	
	public WaterSpout (Player player) {
		super(player, null);
	}
	
	@Override
	public boolean swing () {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;
				
			case CanStart:
				if (canWaterSpout(this.player)) {
					this.blocks = new HashMap<Block, BlockState>();
					this.flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
					if (this.flying != null) {
						spout();
						setState(AbilityState.Progressing);
						AbilityManager.getManager().addInstance(this);
					}
				}
				return false;
				
			case Preparing:
			case Prepared:
			case Progressing:
				setState(AbilityState.Ended);
				return false;

			case Ending:
			case Ended:
			case Removed:
			default:
				return false;
		}
	}

	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}
		revertSpout();
		return spout();
	}

	@Override
	public void stop () {
		revertSpout();
		FlyingPlayer.removeFlyingPlayer(this.player, this);
	}
	
	private void revertSpout () {
		revertBaseBlock();
		for (Block b : this.blocks.keySet()) {
			this.blocks.get(b).update(true);
		}
		this.blocks.clear();
	}
	
	@Override
	public void remove () {
		this.bender.cooldown(Abilities.WaterSpout, COOLDOWN);
		super.remove();
	}
	
	private boolean spout () {
		this.player.setFallDistance(0);
		this.player.setSprinting(false);
		this.player.removePotionEffect(PotionEffectType.SPEED);
		
		Location location = this.player.getLocation().clone().add(0, 0.2, 0);
		Block block = location.clone().getBlock();
		int height = spoutableWaterHeight(location);
		
		if (height == -1) {
			return false;
		}
		
		location = this.base.getLocation();
		for (int i = 1, cardinalPoint = this.currentCardinalPoint / SPEED; i <= height; i++, cardinalPoint++) {
			if (cardinalPoint == 8) {
				cardinalPoint = 0;
			}
			
			block = location.clone().add(0, i, 0).getBlock();
			if (!BlockTools.isTempBlock(block)) {
				if (!isWaterSpoutBlock(block)) {
					this.blocks.put(block, block.getState());
				}
				block.setType(Material.WATER);
			}
			
			switch (cardinalPoint) {
				case 0:
					block = location.clone().add(0, i, -1).getBlock();
					break;
				case 1:
					block = location.clone().add(-1, i, -1).getBlock();
					break;
				case 2:
					block = location.clone().add(-1, i, 0).getBlock();
					break;
				case 3:
					block = location.clone().add(-1, i, 1).getBlock();
					break;
				case 4:
					block = location.clone().add(0, i, 1).getBlock();
					break;
				case 5:
					block = location.clone().add(1, i, 1).getBlock();
					break;
				case 6:
					block = location.clone().add(1, i, 0).getBlock();
					break;
				case 7:
					block = location.clone().add(1, i, -1).getBlock();
					break;
				default:
					break;
			}
			
			if (block.getType().equals(Material.AIR) || isWaterSpoutBlock(block)) {
				if (!BlockTools.isTempBlock(block)) {
					if (!isWaterSpoutBlock(block)) {
						this.blocks.put(block, block.getState());
					}
					block.setType(Material.WATER);
				}
			}
		}
		this.currentCardinalPoint++;
		if (this.currentCardinalPoint == (SPEED * 8)) {
			this.currentCardinalPoint = 0;
		}
		
		if (this.player.getLocation().getBlockY() > block.getY()) {
			this.flying.resetState();
		} else {
			this.flying.fly();
		}
		return true;
	}
	
	private int spoutableWaterHeight (Location location) {
		
		int height = HEIGHT;
		if (Tools.isNight(this.player.getWorld())) {
			height = (int) PluginTools.waterbendingNightAugment(
					height, this.player.getWorld());
		}
		int maxheight = (int) (HEIGHT * Settings.NIGHT_FACTOR) + 5;
		Block blocki;
		for (int i = 0; i < maxheight; i++) {
			blocki = location.clone().add(0, -i, 0).getBlock();
			if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.WaterSpout,
					blocki.getLocation())) {
				return -1;
			}
			if (!isWaterSpoutBlock(blocki)) {
				if ((blocki.getType() == Material.WATER)
						|| (blocki.getType() == Material.STATIONARY_WATER)) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock();
					}
					this.base = blocki;
					if (i > height) {
						return height;
					}
					return i;
				}
				if ((blocki.getType() == Material.ICE)
						|| (blocki.getType() == Material.SNOW)
						|| (blocki.getType() == Material.SNOW_BLOCK)) {
					if (!BlockTools.isTempBlock(blocki)) {
						revertBaseBlock();
						this.baseState = blocki.getState();
						blocki.setType(Material.WATER);
					}
					this.base = blocki;
					if (i > height) {
						return height;
					}
					return i;
				}
				if (((blocki.getType() != Material.AIR)
						&& (!BlockTools.isPlant(blocki) || !EntityTools.canPlantbend(this.player)))) {
					revertBaseBlock();
					return -1;
				}
			}
		}
		revertBaseBlock();
		return -1;
	}

	private void revertBaseBlock () {
		if (this.baseState != null) {
			this.baseState.update(true);
			this.baseState = null;
		}
	}

	public static boolean isWaterSpoutBlock (Block block) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.WaterSpout);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		for (IAbility ab : instances.values()) {
			WaterSpout spout = (WaterSpout) ab;
			if (spout.blocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}
	
	public static List<Player> getPlayers() {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.WaterSpout);
		LinkedList<Player> players = new LinkedList<Player>();
		if ((instances == null) || instances.isEmpty()) {
			return players;
		}
		for (Object o : instances.keySet()) {
			players.add((Player) o);
		}
		return players;
	}
	
	public static void removeSpouts (Location loc0, double radius, Player sourceplayer) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.WaterSpout);
		if (instances == null) {
			return;
		}

		for (Object obj : instances.keySet()) {
			Player player = (Player) obj;
			if (!player.equals(sourceplayer)) {
				Location loc1 = player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				double dx = loc1.getX() - loc0.getX();
				double dy = loc1.getY() - loc0.getY();
				double dz = loc1.getZ() - loc0.getZ();
				
				double distance = Math.sqrt((dx * dx) + (dz * dz));
				
				if ((distance <= radius) && (dy > 0) && (dy < HEIGHT)) {
					instances.get(player).consume();
				}
			}
		}
	}
	
	public static boolean isBending(Player player) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.WaterSpout);
		if (instances == null) {
			return false;
		}
		return instances.containsKey(player);
	}
	
	public static boolean canWaterSpout(Player player) {
		Location loc = player.getLocation();
		if (BlockTools.isWaterBased(loc.getBlock())){
			return true;
		}
		int cpt = 0;
		while ((loc.getBlock().getType() == Material.AIR) && (loc.getBlockY() > 0) && (cpt <= HEIGHT)) {
			loc = loc.add(0, -1, 0);
			if (BlockTools.isWaterBased(loc.getBlock())) {
				return true;
			}
			cpt++;
		}
		return false;
	}
	
	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.WaterSpout;
	}
	
	@Override
	protected long getMaxMillis () {
		return 1000 * 60 * 20;
	}
	
}

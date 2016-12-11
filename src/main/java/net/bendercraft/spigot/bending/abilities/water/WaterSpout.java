package net.bendercraft.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.controller.FlyingPlayer;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

@ABendingAbility(name = WaterSpout.NAME, element = BendingElement.WATER, shift=false)
public class WaterSpout extends BendingActiveAbility {
	public final static String NAME = "WaterSpout";

	private static final Vector[] vectors = { new Vector(0, 0, -1), new Vector(1, 0, -1), new Vector(1, 0, 0), new Vector(1, 0, 1), new Vector(0, 0, 1), new Vector(-1, 0, 1), new Vector(-1, 0, 0), new Vector(-1, 0, -1) };

	@ConfigurationParameter("Rotation-Speed")
	public static int SPEED = 4;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 18;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 0;

	private int currentCardinalPoint = 0;
	private List<TempBlock> blocks = new LinkedList<TempBlock>();
	private FlyingPlayer flying;

	private int height;

	public WaterSpout(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.height = HEIGHT;
		if(bender.hasPerk(BendingPerk.WATER_WATERSPOUT_HEIGHT)) {
			this.height += 1;
		}
	}

	@Override
	public boolean swing() {
		if(getState() == BendingAbilityState.START) {
			if (canWaterSpout(this.player)) {
				this.blocks = new LinkedList<TempBlock>();
				this.flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis(), true);
				if (this.flying != null) {
					spout();
					setState(BendingAbilityState.PROGRESSING);
				}
			}
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			remove();
		}
		return false;
	}

	@Override
	public void progress() {
		revertSpout();
		if(!spout()) {
			remove();
		}
	}

	@Override
	public void stop() {
		revertSpout();
		FlyingPlayer.removeFlyingPlayer(this.player, this);
		this.bender.cooldown(NAME, COOLDOWN);
	}

	private void revertSpout() {
		for (TempBlock b : this.blocks) {
			b.revertBlock();
		}
		this.blocks.clear();
	}

	private boolean spout() {
		player.setFallDistance(0);
		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);

		Location location = player.getLocation().clone().add(0, 0.2, 0);
		int height = spoutableWaterHeight(location);

		if (height == -1) {
			return false;
		} else if (height == -2) {
			FlyingPlayer.removeFlyingPlayer(player, this);
			flying = null;
		} else {
			if(flying == null) {
				flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis(), true);
			}
		}
		Block block = location.getBlock();
		location = block.getLocation();
		for (int i = 0, cardinalPoint = currentCardinalPoint / SPEED; i < (height + 1); i++, cardinalPoint--) {
			Location loc = location.clone().add(0, -i, 0);
			if (!TempBlock.isTempBlock(loc.getBlock()) && loc.getBlock().getType().equals(Material.AIR)) {
				blocks.add(TempBlock.makeTemporary(this, loc.getBlock(), Material.STATIONARY_WATER, false));
			}

			if (cardinalPoint == -1) {
				cardinalPoint = 7;
			}

			loc = loc.add(vectors[cardinalPoint]);
			if (loc.getBlock().getType().equals(Material.AIR)) {
				blocks.add(TempBlock.makeTemporary(this, loc.getBlock(), Material.WATER, false));
			}

		}

		currentCardinalPoint++;
		if (currentCardinalPoint == (SPEED * 8)) {
			currentCardinalPoint = 0;
		}
		return true;
	}

	private int spoutableWaterHeight(Location location) {
		Location loc = location.clone();
		if (loc.getBlock().getType() != Material.AIR && !BlockTools.isWaterBased(loc.getBlock())) {
			return -1;
		}
		loc = loc.add(0, 1, 0);
		for (int i = 0; i < height; i++) {
			Location locToTest = loc.add(0, -1, 0);
			if (ProtectionManager.isLocationProtectedFromBending(player, register, locToTest)) {
				return -1;
			}
			Block block = locToTest.getBlock();
			if (!block.getType().equals(Material.AIR)) {
				if (BlockTools.isWaterBased(block) && (!TempBlock.isTempBlock(block) || TempBlock.get(block).isBendAllowed())) {
					return i + 1; // Valid source !
				} else {
					return -1; // Cannot waterspout
				}
			}
		}
		// If we are here, only AIR block has been traversed : check one more time if we should just stop or deny lift
		Location locToTest = loc.add(0, -1, 0);
		if (ProtectionManager.isLocationProtectedFromBending(player, register, locToTest)) {
			return -1; // Cannot waterspout
		}
		Block block = locToTest.getBlock();
		if (BlockTools.isWaterBased(block) && (!TempBlock.isTempBlock(block) || TempBlock.get(block).isBendAllowed())) {
			return -2; // Can waterspout but too high
		}
		
		return -1; // Cannot waterspout
	}

	public static boolean isWaterSpoutBlock(Block block) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		for (BendingAbility ab : instances.values()) {
			WaterSpout spout = (WaterSpout) ab;
			for(TempBlock b : spout.blocks) {
				if(b.getBlock() == block) {
					return true;
				}
			}
		}
		return false;
	}

	public static void removeSpouts(Location loc0, double radius, Player sourceplayer) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
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
					instances.get(player).remove();
				}
			}
		}
	}

	public static boolean isBending(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return false;
		}
		return instances.containsKey(player);
	}

	public boolean canWaterSpout(Player player) {
		Location loc = player.getLocation();
		if (BlockTools.isWaterBased(loc.getBlock())) {
			return true;
		}
		int cpt = 0;
		while ((loc.getBlock().getType() == Material.AIR 
					|| loc.getBlock().getType() == Material.WATER 
					|| loc.getBlock().getType() ==Material.STATIONARY_WATER) 
				&& (loc.getBlockY() > 0) 
				&& (cpt <= height)) {
			loc = loc.add(0, -1, 0);
			if (BlockTools.isWaterBased(loc.getBlock())) {
				return true;
			}
			cpt++;
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	protected long getMaxMillis() {
		return 1000 * 60 * 20;
	}

}

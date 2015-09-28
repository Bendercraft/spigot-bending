package net.avatar.realms.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name = "Water Spout", bind = BendingAbilities.WaterSpout, element = BendingElement.Water)
public class WaterSpout extends BendingActiveAbility {

	private static final Vector[] vectors = { new Vector(0, 0, -1), new Vector(1, 0, -1), new Vector(1, 0, 0), new Vector(1, 0, 1), new Vector(0, 0, 1), new Vector(-1, 0, 1), new Vector(-1, 0, 0), new Vector(-1, 0, -1) };

	@ConfigurationParameter("Rotation-Speed")
	public static int SPEED = 4;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 18;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 0;

	private int currentCardinalPoint = 0;
	private BlockState baseState;
	private List<TempBlock> blocks = new LinkedList<TempBlock>();
	private FlyingPlayer flying;
	private int height;

	public WaterSpout(Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {
		switch (this.state) {
		case None:
		case CannotStart:
			return false;

		case CanStart:
			if (canWaterSpout(this.player)) {
				this.height = 0;
				this.blocks = new LinkedList<TempBlock>();
				this.flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
				if (this.flying != null) {
					spout();
					setState(BendingAbilityState.Progressing);
					AbilityManager.getManager().addInstance(this);
				}
			}
			return false;

		case Preparing:
		case Prepared:
		case Progressing:
			setState(BendingAbilityState.Ended);
			return false;

		case Ending:
		case Ended:
		case Removed:
		default:
			return false;
		}
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		revertSpout();
		return spout();
	}

	@Override
	public void stop() {
		revertSpout();
		FlyingPlayer.removeFlyingPlayer(this.player, this);
	}

	private void revertSpout() {
		revertBaseBlock();
		for (TempBlock b : this.blocks) {
			b.revertBlock();
		}
		this.blocks.clear();
	}

	@Override
	public void remove() {
		this.bender.cooldown(BendingAbilities.WaterSpout, COOLDOWN);
		super.remove();
	}

	private boolean spout() {
		this.player.setFallDistance(0);
		this.player.setSprinting(false);
		this.player.removePotionEffect(PotionEffectType.SPEED);

		Location location = this.player.getLocation().clone().add(0, 0.2, 0);
		int result = spoutableWaterHeight(location);

		if (result == -1) {
			return false;
		} else if (result == -2) {
			this.flying.resetState();
		} else {
			this.flying.fly();
		}
		Block block = location.getBlock();
		location = block.getLocation();
		for (int i = 0, cardinalPoint = this.currentCardinalPoint / SPEED; i < (this.height + 1); i++, cardinalPoint--) {
			Location loc = location.clone().add(0, -i, 0);
			if (!BlockTools.isTempBlock(loc.getBlock())) {
				this.blocks.add(new TempBlock(loc.getBlock(), Material.STATIONARY_WATER, (byte) 0x0));
			}

			if (cardinalPoint == -1) {
				cardinalPoint = 7;
			}

			loc = loc.add(vectors[cardinalPoint]);
			if (loc.getBlock().getType().equals(Material.AIR)) {
				this.blocks.add(new TempBlock(loc.getBlock(), Material.STATIONARY_WATER, (byte) 0x0));
			}

		}

		this.currentCardinalPoint++;
		if (this.currentCardinalPoint == (SPEED * 8)) {
			this.currentCardinalPoint = 0;
		}
		return true;
	}

	private int spoutableWaterHeight(Location location) {

		Location loc = location.clone();
		int height = HEIGHT;
		if (Tools.isNight(loc.getWorld())) {
			height = (int) (Settings.NIGHT_FACTOR * HEIGHT) + 1;
		}
		for (int i = 0; i <= (height + 1); i++) {
			Location locToTest = loc.add(0, -1, 0);
			if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.WaterSpout, locToTest)) {
				return -1;
			}
			Block block = locToTest.getBlock();
			if (block.getType().equals(Material.AIR)) {
				this.height = i + 1;
				continue;
			}
			if (BlockTools.isWaterBased(block)) {
				// Valid source !
				return i + 1;
			} else {
				return -1;
				// Cannot waterspout
			}
		}
		return -2;
		// Can waterspout but too high
	}

	private void revertBaseBlock() {
		if (this.baseState != null) {
			this.baseState.update(true);
			this.baseState = null;
		}
	}

	public static boolean isWaterSpoutBlock(Block block) {
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.WaterSpout);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		for (IBendingAbility ab : instances.values()) {
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
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.WaterSpout);
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
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.WaterSpout);
		if (instances == null) {
			return false;
		}
		return instances.containsKey(player);
	}

	public static boolean canWaterSpout(Player player) {
		Location loc = player.getLocation();
		if (BlockTools.isWaterBased(loc.getBlock())) {
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
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	protected long getMaxMillis() {
		return 1000 * 60 * 20;
	}

}

package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;

@ABendingAbility(name = AirSpout.NAME, element = BendingElement.AIR, shift=false)
public class AirSpout extends BendingActiveAbility {
	public final static String NAME = "AirSpout";
	
	@ConfigurationParameter("Height")
	private static double HEIGHT = 20.0;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 250;

	private static final long interval = 100;

	private static final ParticleEffect VISUAL = ParticleEffect.ENCHANTMENT_TABLE;

	private int angle = 0;

	private long time;
	private FlyingPlayer flying;

	private double height;

	public AirSpout(RegisteredAbility register, Player player) {
		super(register, player);

		this.time = this.startedTime;

		height = HEIGHT;
		if (bender.hasPath(BendingPath.MOBILE)) {
			height *= 1.2;
		}
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			this.flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
			if (this.flying != null) {
				setState(BendingAbilityState.PROGRESSING);
			}
		} else if (getState() == BendingAbilityState.PROGRESSING) {
			long now = System.currentTimeMillis();
			if (now >= (this.startedTime + 200)) {
				remove();
			}
		}
		return false;
	}

	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);

		if ((instances == null) || instances.isEmpty()) {
			return players;
		}

		for (Object ob : instances.keySet()) {
			players.add((Player) ob);
		}
		return players;
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (this.player.getEyeLocation().getBlock().isLiquid() 
				|| BlockTools.isSolid(this.player.getEyeLocation().getBlock())) {
			return false;
		}
		return true;
	}

	@Override
	public void progress() {
		this.player.setFallDistance(0);
		this.player.setSprinting(false);
		Block block = getGround();
		if (block != null) {
			double dy = this.player.getLocation().getY() - block.getY();
			if (dy > height) {
				this.flying.resetState();
			} else {
				this.flying.fly();
			}
			rotateAirColumn(block);
		} else {
			remove();
		}
	}

	private Block getGround() {
		Block standingblock = this.player.getLocation().getBlock();
		for (int i = 0; i <= (height + 5); i++) {
			Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (BlockTools.isSolid(block) || block.isLiquid()) {
				return block;
			}
		}
		return null;
	}

	@Override
	public void stop() {
		FlyingPlayer.removeFlyingPlayer(this.player, this);
		this.bender.cooldown(NAME, COOLDOWN);
	}

	private void rotateAirColumn(Block block) {
		if (System.currentTimeMillis() >= (this.time + interval)) {
			this.time = System.currentTimeMillis();

			Location location = block.getLocation();
			Location playerloc = this.player.getLocation();
			location = new Location(location.getWorld(), playerloc.getX(), location.getY(), playerloc.getZ());

			double dy = playerloc.getY() - block.getY();
			if (dy > height) {
				dy = height;
			}
			Integer[] directions = { 0, 1, 2, 3, 5, 6, 7, 8 };
			int index = this.angle;

			this.angle++;
			if (this.angle >= directions.length) {
				this.angle = 0;
			}
			for (int i = 1; i <= dy; i++) {
				index += 1;
				if (index >= directions.length) {
					index = 0;
				}
				VISUAL.display(0, 0, 0, 1, 1, new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ()), 20);
			}
		}
	}

	public static void removeSpouts(Location loc, double radius, Player sourceplayer) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return;
		}

		Location loc0 = loc;
		for (Object o : instances.keySet()) {
			Player player = (Player) o;
			if (!player.equals(sourceplayer)) {
				Location loc1 = player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				double dx = loc1.getX() - loc0.getX();
				double dy = loc1.getY() - loc0.getY();
				double dz = loc1.getZ() - loc0.getZ();

				double distance = Math.sqrt((dx * dx) + (dz * dz));

				if ((distance <= radius) && (dy > 0) && (dy < ((AirSpout) (instances.get(o))).getHeight())) {
					instances.get(o).remove();
				}
			}
		}
	}

	public double getHeight() {
		return height;
	}

	public static boolean isSpouting(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);

		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		return instances.containsKey(player);
	}

	@Override
	protected long getMaxMillis() {
		return 1000L * 60 * 20;
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (isSpouting(this.player)) {
			return false;
		}

		return true;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

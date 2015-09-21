package net.avatar.realms.spigot.bending.abilities.air;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;

@BendingAbility(name = "Air Spout", bind = BendingAbilities.AirSpout, element = BendingElement.Air)
public class AirSpout extends BendingActiveAbility {

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

	public AirSpout(Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		this.time = this.startedTime;

		height = HEIGHT;
		if (bender.hasPath(BendingPath.Mobile)) {
			height *= 1.2;
		}
	}

	@Override
	public boolean swing() {

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return true;
		}

		if (this.state == BendingAbilityState.CanStart) {
			this.flying = FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
			if (this.flying != null) {
				setState(BendingAbilityState.Progressing);
				AbilityManager.getManager().addInstance(this);
			}
			return false;
		}

		if (this.state == BendingAbilityState.Progressing) {
			long now = System.currentTimeMillis();
			if (now >= (this.startedTime + 200)) {
				setState(BendingAbilityState.Ended);
				return false;
			}
		}

		return false;
	}

	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AirSpout);

		if ((instances == null) || instances.isEmpty()) {
			return players;
		}

		for (Object ob : instances.keySet()) {
			players.add((Player) ob);
		}
		return players;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		if (this.player.getEyeLocation().getBlock().isLiquid() || BlockTools.isSolid(this.player.getEyeLocation().getBlock())) {
			return false;
		}
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
			return false;
		}

		return true;
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
	}

	@Override
	public void remove() {
		this.bender.cooldown(BendingAbilities.AirSpout, COOLDOWN);
		super.remove();
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

				Location effectloc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());

				// location.getWorld().playEffect(effectloc2, Effect.SMOKE,
				// (int) directions[index], (int) height + 5);
				VISUAL.display(0, 0, 0, 1, 1, effectloc2, 20);
			}
		}
	}

	public static void removeSpouts(Location loc0, double radius, Player sourceplayer) {
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AirSpout);

		if ((instances == null) || instances.isEmpty()) {
			return;
		}

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
					instances.get(o).consume();
				}
			}
		}
	}

	public double getHeight() {
		return height;
	}

	public static boolean isSpouting(Player player) {
		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AirSpout);

		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		return instances.containsKey(player);
	}

	@Override
	protected long getMaxMillis() {
		return 1000 * 60 * 20;
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

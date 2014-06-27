package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FireBurst implements IAbility {
	private static Map<Player, FireBurst> instances = new HashMap<Player, FireBurst>();

	private Player player;
	private long starttime;
	private int damage = 3;
	private long chargetime = 2500;
	private double deltheta = 10;
	private double delphi = 10;
	private boolean charged = false;
	private IAbility parent;

	public FireBurst(Player player, IAbility parent) {
		this.parent = parent;
		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
				Abilities.FireBurst))
			return;
		if (instances.containsKey(player))
			return;
		starttime = System.currentTimeMillis();
		if (Tools.isDay(player.getWorld())) {
			chargetime /= ConfigManager.dayFactor;
		}
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		instances.put(player, this);
	}

	public static void coneBurst(Player player) {
		if (instances.containsKey(player))
			instances.get(player).coneBurst();
	}

	private void coneBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = BlockTools.getBlocksAroundPoint(
					player.getLocation(), 2);
			Vector vector = location.getDirection();
			double angle = Math.toRadians(30);
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					if (direction.angle(vector) <= angle) {
						// Tools.verbose(direction.angle(vector));
						// Tools.verbose(direction);
						new FireBlast(location, direction.normalize(), player,
								damage, safeblocks, this);
					}
				}
			}
			remove();
		}
	}
	
	private void remove() {
		instances.remove(player);
	}

	private void sphereBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = BlockTools.getBlocksAroundPoint(
					player.getLocation(), 2);
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					new FireBlast(location, direction.normalize(), player,
							damage, safeblocks, this);
				}
			}
		}
	}

	private boolean progress() {
		if (!EntityTools.canBend(player, Abilities.FireBurst)
				|| EntityTools.getBendingAbility(player) != Abilities.FireBurst) {
			return false;
		}
		if (System.currentTimeMillis() > starttime + chargetime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				sphereBurst();
			}
			return false;
		} else if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES,
					4, 3);
		}
		return true;
	}

	public static void progressAll() {
		List<FireBurst> toRemove = new LinkedList<FireBurst>();
		for (FireBurst burst : instances.values()) {
			boolean keep = burst.progress();
			if(!keep) {
				toRemove.add(burst);
			}
		}
		
		for (FireBurst burst : toRemove) {
			burst.remove();
		}
	}

	public static String getDescription() {
		return "FireBurst is a very powerful firebending ability. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of flames in front of you, or click to release the burst in a sphere around you. ";
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public int getBaseExperience() {
		return 11;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

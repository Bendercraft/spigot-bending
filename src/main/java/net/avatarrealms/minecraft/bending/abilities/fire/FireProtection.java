package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FireProtection implements IAbility {
	private static Map<Player, FireProtection> instances = new HashMap<Player, FireProtection>();

	private static long interval = 100;
	private static double radius = 3;
	private static double discradius = 1.5;
	private static long duration = 1000;
	private static boolean ignite = true;

	private Player player;
	private long time;
	private long starttime;
	private IAbility parent;

	public FireProtection(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		if (instances.containsKey(player))
			return;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.FireShield))
			return;

		if (!player.getEyeLocation().getBlock().isLiquid()) {
			time = System.currentTimeMillis();
			starttime = time;
			instances.put(player, this);
			bPlayer.cooldown(Abilities.FireShield);
		}
	}

	private void remove() {
		instances.remove(player);
	}

	private boolean progress() {
		if (!player.isOnline() || player.isDead()) {
			return false;
		}

		if (System.currentTimeMillis() > starttime + duration) {
			return false;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();
			
			List<Block> blocks = new LinkedList<Block>();
			Location location = player.getEyeLocation().clone();
			Vector direction = location.getDirection();
			location = location.clone().add(direction.multiply(radius));

			if (PluginTools.isRegionProtectedFromBuild(player,
					Abilities.FireShield, location)) {
				return false;
			}

			for (double theta = 0; theta < 360; theta += 20) {
				Vector vector = Tools.getOrthogonalVector(direction, theta,
						discradius);
				Block block = location.clone().add(vector).getBlock();
				if (!blocks.contains(block) && !BlockTools.isSolid(block)
						&& !block.isLiquid())
					blocks.add(block);
			}

			for (Block block : blocks) {
				if (!PluginTools.isRegionProtectedFromBuild(player,
						Abilities.FireShield, block.getLocation()))
					block.getWorld().playEffect(block.getLocation(),
							Effect.MOBSPAWNER_FLAMES, 0, 20);
			}

			for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
					discradius)) {
				if (PluginTools.isRegionProtectedFromBuild(player,
						Abilities.FireShield, entity.getLocation())) {
					continue;
				}
					
				if (player.getEntityId() != entity.getEntityId() && ignite) {
					entity.setFireTicks(120);
					if (!(entity instanceof LivingEntity)) {
						entity.remove();
					}
				}
			}

			FireBlast.removeFireBlastsAroundPoint(location, discradius);
			WaterManipulation.removeAroundPoint(location, discradius);
			EarthBlast.removeAroundPoint(location, discradius);
			FireStream.removeAroundPoint(location, discradius);
			
		}
		return true;
	}

	public static void progressAll() {
		List<FireProtection> toRemove = new LinkedList<FireProtection>();
		for (FireProtection shield : instances.values()) {
			boolean keep = shield.progress();
			if(!keep) {
				toRemove.add(shield);
			}
		}
		
		for(FireProtection shield : toRemove) {
			shield.remove();
		}
	}

	public static String getDescription() {
		return "FireShield is a basic defensive ability. "
				+ "Clicking with this ability selected will create a "
				+ "small disc of fire in front of you, which will block most "
				+ "attacks and bending. Alternatively, pressing and holding "
				+ "sneak creates a very small shield of fire, blocking most attacks. "
				+ "Creatures that contact this fire are ignited.";
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

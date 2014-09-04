package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class FireShield implements IAbility {
	private static Map<Player, FireShield> instances = new HashMap<Player, FireShield>();

	private static long interval = 100;
	private static double radius = 3;
	private static boolean ignite = true;

	private Player player;
	private long time;
	private IAbility parent;

	public FireShield(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		if (instances.containsKey(player))
			return;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.FireShield))
			return;

		if (!player.getEyeLocation().getBlock().isLiquid()) {
			time = System.currentTimeMillis();
			instances.put(player, this);
		}
	}

	private void remove() {
		instances.remove(player);
	}

	private boolean progress() {
		if ((!player.isSneaking())
				|| !EntityTools.canBend(player, Abilities.FireShield)
				|| !EntityTools.hasAbility(player, Abilities.FireShield)) {
			return false;
		}

		if (!player.isOnline() || player.isDead()) {
			return false;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();
			
			List<Block> blocks = new LinkedList<Block>();
			Location location = player.getEyeLocation().clone();

			for (double theta = 0; theta < 180; theta += 20) {
				for (double phi = 0; phi < 360; phi += 20) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					Block block = location
							.clone()
							.add(radius * Math.cos(rphi) * Math.sin(rtheta),
									radius * Math.cos(rtheta),
									radius * Math.sin(rphi)
											* Math.sin(rtheta)).getBlock();
					if (!blocks.contains(block) && !BlockTools.isSolid(block)
							&& !block.isLiquid())
						blocks.add(block);
				}
			}

			for (Block block : blocks) {
				if (!Tools.isRegionProtectedFromBuild(player,
						Abilities.FireShield, block.getLocation()))
					block.getWorld().playEffect(block.getLocation(),
							Effect.MOBSPAWNER_FLAMES, 0, 20);
			}

			for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
					radius)) {
				if (Tools.isRegionProtectedFromBuild(player,
						Abilities.FireShield, entity.getLocation())) {
					continue;
				}	
				if (player.getEntityId() != entity.getEntityId() && ignite) {
					entity.setFireTicks(120);
					new Enflamed(entity, player, this);
				}
			}

			FireBlast.removeFireBlastsAroundPoint(location, radius);
			
		}
		return true;
	}

	public static void progressAll() {
		List<FireShield> toRemove = new LinkedList<FireShield>();
		for (FireShield shield : instances.values()) {
			boolean keep = shield.progress();
			if(!keep) {
				toRemove.add(shield);
			}
		}
		
		for(FireShield shield : toRemove) {
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

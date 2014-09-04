package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SpikeField implements IAbility {

	private static int radius = ConfigManager.icespikeAreaRadius;
	public static int numofspikes = ((radius * 2) * (radius * 2)) / 16;
	private static long cooldown = ConfigManager.icespikeAreaCooldown;
	public static Map<Player, Long> cooldowns = new HashMap<Player, Long>();

	private Random ran = new Random();
	private int damage = ConfigManager.icespikeAreaDamage;
	private Vector thrown = new Vector(0,
			ConfigManager.icespikeAreaThrowingMult, 0);
	private IAbility parent;

	public SpikeField(Player p, IAbility parent) {
		this.parent = parent;
		if (cooldowns.containsKey(p))
			if (cooldowns.get(p) + cooldown >= System.currentTimeMillis())
				return;
		// Tools.verbose("Trying to create IceField" + numofspikes);
		int locX = p.getLocation().getBlockX();
		int locY = p.getLocation().getBlockY();
		int locZ = p.getLocation().getBlockZ();
		List<Block> iceblocks = new ArrayList<Block>();
		for (int x = -(radius - 1); x <= (radius - 1); x++) {
			for (int z = -(radius - 1); z <= (radius - 1); z++) {
				for (int y = -1; y <= 1; y++) {
					Block testblock = p.getWorld().getBlockAt(locX + x,
							locY + y, locZ + z);
					if (testblock.getType() == Material.ICE
							&& testblock.getRelative(BlockFace.UP).getType() == Material.AIR
							&& !(testblock.getX() == p.getEyeLocation()
									.getBlock().getX() && testblock.getZ() == p
									.getEyeLocation().getBlock().getZ())) {
						iceblocks.add(testblock);
					}
				}
			}
		}

		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(p.getLocation(),
				radius);

		for (int i = 0; i < numofspikes; i++) {
			if (iceblocks.isEmpty())
				return;

			Entity target = null;
			Block targetblock = null;
			for (Entity entity : entities) {
				if (entity.getEntityId() != p.getEntityId()) {
					for (Block block : iceblocks) {
						if (block.getX() == entity.getLocation().getBlockX()
								&& block.getZ() == entity.getLocation()
										.getBlockZ()) {
							target = entity;
							targetblock = block;
							break;
						}
					}
				} else {
					continue;
				}
			}

			if (target != null) {
				entities.remove(target);
			} else {
				targetblock = iceblocks.get(ran.nextInt(iceblocks.size()));
			}

			if (targetblock.getRelative(BlockFace.UP).getType() != Material.ICE) {
				new IceSpike(p, targetblock.getLocation(), damage, thrown,
						cooldown, this);
				cooldowns.put(p, System.currentTimeMillis());
				iceblocks.remove(targetblock);
			}
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}

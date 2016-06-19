package net.bendercraft.spigot.bending.abilities.water;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

public class SpikeField {
	@ConfigurationParameter("Radius")
	private static int RADIUS = 6;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 2;

	@ConfigurationParameter("Throw-Mult")
	private static double THROW_MULT = 1.0;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 3000;

	public int numofspikes;

	private Random ran = new Random();

	private int damage = DAMAGE;
	private Vector thrown = new Vector(0, THROW_MULT, 0);

	private List<SpikeFieldColumn> spikes = new LinkedList<SpikeFieldColumn>();

	public SpikeField(Player p, BendingAbility parent) {
		this.numofspikes = (RADIUS * RADIUS) / 4;
		// Tools.verbose("Trying to create IceField" + numofspikes);
		int locX = p.getLocation().getBlockX();
		int locY = p.getLocation().getBlockY();
		int locZ = p.getLocation().getBlockZ();
		List<Block> iceblocks = new ArrayList<Block>();
		for (int x = -(RADIUS - 1); x <= (RADIUS - 1); x++) {
			for (int z = -(RADIUS - 1); z <= (RADIUS - 1); z++) {
				for (int y = -1; y <= 1; y++) {
					Block testblock = p.getWorld().getBlockAt(locX + x, locY + y, locZ + z);
					if (testblock.getType() == Material.ICE && testblock.getRelative(BlockFace.UP).getType() == Material.AIR && !(testblock.getX() == p.getEyeLocation().getBlock().getX() && testblock.getZ() == p.getEyeLocation().getBlock().getZ())) {
						iceblocks.add(testblock);
					}
				}
			}
		}
		if (iceblocks.isEmpty()) {
			return;
		}
		
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(p.getLocation(), RADIUS);
		for (int i = 0; i < this.numofspikes; i++) {
			Entity target = null;
			Block targetblock = null;
			for (Entity entity : entities) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (entity.getEntityId() != p.getEntityId()) {
					for (Block block : iceblocks) {
						if (block.getX() == entity.getLocation().getBlockX() && block.getZ() == entity.getLocation().getBlockZ()) {
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
				if (!iceblocks.isEmpty()) {
					targetblock = iceblocks.get(this.ran.nextInt(iceblocks.size()));
					this.spikes.add(new SpikeFieldColumn(p, targetblock.getLocation(), this.damage, this.thrown));
					iceblocks.remove(targetblock);
				}
			}
		}
	}

	public boolean progress() {
		boolean result = false;
		for (SpikeFieldColumn column : this.spikes) {
			if(column.progress()) {
				result = true;
			}
		}
		return result;
	}
	
	public void remove() {
		for (SpikeFieldColumn column : this.spikes) {
			column.remove();
		}
		this.spikes.clear();
	}
}

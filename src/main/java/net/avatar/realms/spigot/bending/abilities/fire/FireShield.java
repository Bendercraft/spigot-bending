package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Fire Shield", element=BendingType.Fire)
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
		if (instances.containsKey(player)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.FireShield)) {
			return;
		}

		if (!player.getEyeLocation().getBlock().isLiquid()) {
			this.time = System.currentTimeMillis();
			BendingPlayer.getBendingPlayer(this.player).cooldown(Abilities.FireShield, FireProtection.COOLDOWN);
			instances.put(player, this);
		}
	}

	private void remove() {
		instances.remove(this.player);
	}

	private boolean progress() {
		if ((!this.player.isSneaking())
 || !EntityTools.canBend(this.player, Abilities.FireShield)) {
			return false;
		}

		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}

		if (System.currentTimeMillis() > (this.time + interval)) {
			this.time = System.currentTimeMillis();

			List<Block> blocks = new LinkedList<Block>();
			Location location = this.player.getEyeLocation().clone();

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
							&& !block.isLiquid()) {
						blocks.add(block);
					}
				}
			}

			for (Block block : blocks) {
				if (!ProtectionManager.isRegionProtectedFromBending(this.player,
						Abilities.FireShield, block.getLocation())) {
					block.getWorld().playEffect(block.getLocation(),
							Effect.MOBSPAWNER_FLAMES, 0, 20);
				}
			}

			for (Entity entity : EntityTools.getEntitiesAroundPoint(location,
					radius)) {
				if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(this.player,
						Abilities.FireShield, entity.getLocation())) {
					continue;
				}	
				if ((this.player.getEntityId() != entity.getEntityId()) && ignite) {
					entity.setFireTicks(120);
					new Enflamed(entity, this.player, this);
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

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return this.parent;
	}
}

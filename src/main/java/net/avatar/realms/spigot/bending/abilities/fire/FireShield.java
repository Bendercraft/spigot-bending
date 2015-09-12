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
import net.avatar.realms.spigot.bending.abilities.BendingPathType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Fire Shield", element=BendingType.Fire)
public class FireShield extends ActiveAbility {
	private static Map<Player, FireShield> instances = new HashMap<Player, FireShield>();

	private static long interval = 100;
	private static double radius = 3;
	private static boolean ignite = true;

	private long time;

	public FireShield(Player player) {
		super (player, null);
		if (instances.containsKey(player)) {
			return;
		}

		if (this.bender.isOnCooldown(Abilities.FireShield)) {
			return;
		}

		if (!player.getEyeLocation().getBlock().isLiquid()) {
			this.time = System.currentTimeMillis();

			instances.put(player, this);
		}
	}
	
	@Override
	public boolean sneak() {
		return false;
	}
	
	@Override
	public boolean swing() {
		FireProtection protect = new FireProtection(this.player);
		return protect.swing();
	}

	@Override
	public void remove() {
		this.bender.cooldown(Abilities.FireShield, FireProtection.COOLDOWN);
		super.remove();
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		if (!this.player.isSneaking()){
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
					if(this.bender.hasPath(BendingPathType.Lifeless)) {
						EntityTools.damageEntity(this.player, entity, 2);
					}
					new Enflamed(this.player, entity, 3, this);
				}
			}

			FireBlast.removeFireBlastsAroundPoint(location, radius);

		}
		return true;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.FireShield;
	}

}

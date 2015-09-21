package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@BendingAbility(name = "Fire Shield", bind = BendingAbilities.FireShield, element = BendingElement.Fire)
public class FireShield extends BendingActiveAbility {

	private static long interval = 100;
	private static double radius = 3;
	private static boolean ignite = true;

	private long time;

	public FireShield(Player player) {
		super(player, null);
	}

	@Override
	public boolean sneak() {
		if (state == BendingAbilityState.CanStart) {
			this.time = System.currentTimeMillis();
			AbilityManager.getManager().addInstance(this);
		}
		return false;
	}

	@Override
	public boolean swing() {
		FireProtection protect = new FireProtection(this.player);
		return protect.swing();
	}

	@Override
	public void remove() {
		this.bender.cooldown(BendingAbilities.FireShield, FireProtection.COOLDOWN);
		super.remove();
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		if (!this.player.isSneaking()) {
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
					Block block = location.clone().add(radius * Math.cos(rphi) * Math.sin(rtheta), radius * Math.cos(rtheta), radius * Math.sin(rphi) * Math.sin(rtheta)).getBlock();
					if (!blocks.contains(block) && !BlockTools.isSolid(block) && !block.isLiquid()) {
						blocks.add(block);
					}
				}
			}

			for (Block block : blocks) {
				if (!ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.FireShield, block.getLocation())) {
					block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
				}
			}

			for (Entity entity : EntityTools.getEntitiesAroundPoint(location, radius)) {
				if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.FireShield, entity.getLocation())) {
					continue;
				}
				if ((this.player.getEntityId() != entity.getEntityId()) && ignite) {
					if (this.bender.hasPath(BendingPath.Lifeless)) {
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
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.FireShield);
		return !instances.containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

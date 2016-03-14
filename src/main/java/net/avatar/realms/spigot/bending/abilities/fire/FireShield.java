package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@ABendingAbility(name = FireShield.NAME, element = BendingElement.FIRE)
public class FireShield extends BendingActiveAbility {
	public final static String NAME = "FireShield";

	private static long interval = 100;
	private static double radius = 3;
	private static boolean ignite = true;

	private long time;
	private FireProtection protect;

	public FireShield(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if (getState() == BendingAbilityState.START) {
			this.time = System.currentTimeMillis();
			setState(BendingAbilityState.PROGRESSING);
			
		}
		return false;
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.START) {
			protect = new FireProtection(this.player);
			setState(BendingAbilityState.PROGRESSING);
			
		}
		return false;
	}

	@Override
	public void stop() {
		if(protect != null) {
			protect.remove();
		}
		this.bender.cooldown(NAME, FireProtection.COOLDOWN);
	}

	@Override
	public void progress() {
		if(protect != null) {
			if(!protect.progress()) {
				remove();
			}
			return;
		}
		if (!this.player.isSneaking()) {
			remove();
			return;
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
				if (!ProtectionManager.isLocationProtectedFromBending(this.player, NAME, block.getLocation())) {
					block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
				}
			}

			for (Entity entity : EntityTools.getEntitiesAroundPoint(location, radius)) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (ProtectionManager.isLocationProtectedFromBending(this.player, NAME, entity.getLocation())) {
					continue;
				}
				if ((this.player.getEntityId() != entity.getEntityId()) && ignite) {
					if (this.bender.hasPath(BendingPath.LIFELESS)) {
						EntityTools.damageEntity(bender, entity, 2);
					}
					Enflamed.enflame(this.player, entity, 3);
				}
			}
			FireBlast.removeFireBlastsAroundPoint(location, radius);
		}
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (player.getEyeLocation().getBlock().isLiquid()) {
			return false;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		return !instances.containsKey(player);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}

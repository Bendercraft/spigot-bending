package net.bendercraft.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.Tools;

public class FireProtection {

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1000;

	@ConfigurationParameter("Duration")
	private static long DURATION = 1200;

	private static long interval = 100;
	private static double radius = 3;
	private static double discradius = 1.5;

	private long time;
	private long startedTime;

	private Player player;

	private BendingPlayer bender;

	public FireProtection(Player player) {
		this.player = player;
		this.time = System.currentTimeMillis();
		this.startedTime = this.time;
		this.bender = BendingPlayer.getBendingPlayer(player);
	}

	public void remove() {
		bender.cooldown(FireShield.NAME, COOLDOWN);
	}

	public boolean progress() {
		if (System.currentTimeMillis() > (this.time + interval)) {
			this.time = System.currentTimeMillis();
			
			if(this.startedTime + DURATION < this.time) {
				return false;
			}
			
			if (this.player.getEyeLocation().getBlock().isLiquid()) {
				return false;
			}

			List<Block> blocks = new LinkedList<Block>();
			Location location = this.player.getEyeLocation().clone();
			Vector direction = location.getDirection();
			location = location.clone().add(direction.multiply(radius));

			RegisteredAbility ability = AbilityManager.getManager().getRegisteredAbility(FireShield.NAME);
			if (ProtectionManager.isLocationProtectedFromBending(this.player, ability, location)) {
				return false;
			}

			for (double theta = 0; theta < 360; theta += 20) {
				Vector vector = Tools.getOrthogonalVector(direction, theta, discradius);
				Block block = location.clone().add(vector).getBlock();
				if (!blocks.contains(block) && !BlockTools.isSolid(block) && !block.isLiquid()) {
					blocks.add(block);
				}
			}

			for (Block block : blocks) {
				if (!ProtectionManager.isLocationProtectedFromBending(this.player, ability, block.getLocation())) {
					block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
				}
			}

			for (Entity entity : EntityTools.getEntitiesAroundPoint(location, discradius)) {
				if (ProtectionManager.isEntityProtected(entity)) {
					continue;
				}
				if (ProtectionManager.isLocationProtectedFromBending(this.player, ability, entity.getLocation())) {
					continue;
				}

				if (this.player.getEntityId() != entity.getEntityId()) {
					if (!(entity instanceof LivingEntity)) {
						entity.remove();
					} else {
						Enflamed.enflame(player, entity, 5);
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
}

package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
		bender.cooldown(BendingAbilities.FireShield, COOLDOWN);
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

			if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.FireShield, location)) {
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
				if (!ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.FireShield, block.getLocation())) {
					block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
				}
			}

			for (Entity entity : EntityTools.getEntitiesAroundPoint(location, discradius)) {
				if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
					continue;
				}
				if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.FireShield, entity.getLocation())) {
					continue;
				}

				if (this.player.getEntityId() != entity.getEntityId()) {
					if (!(entity instanceof LivingEntity)) {
						entity.remove();
					} else {
						new Enflamed(player, entity, 5, null);
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

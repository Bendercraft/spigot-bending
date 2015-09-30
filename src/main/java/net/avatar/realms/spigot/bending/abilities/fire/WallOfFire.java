package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

@BendingAbility(name = "Wall of Fire", bind = BendingAbilities.WallOfFire, element = BendingElement.Fire)
public class WallOfFire extends BendingActiveAbility {

	private static double maxangle = 50;
	private static long interval = 250;

	@ConfigurationParameter("Range")
	private static int RANGE = 4;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 7500;

	@ConfigurationParameter("Interval")
	private static long DAMAGE_INTERVAL = 500;

	@ConfigurationParameter("Height")
	private static int HEIGHT = 4;

	@ConfigurationParameter("Width")
	private static int WIDTH = 4;

	@ConfigurationParameter("Duration")
	private static long DURATION = 5000;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 9;

	private Location origin;
	private long time;
	private int damagetick = 0, intervaltick = 0;
	private List<Block> blocks = new LinkedList<Block>();

	private int damage;
	private int width;
	private int height;
	private long duration;

	public WallOfFire(Player player) {
		super(player, null);

		if (this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		this.origin = EntityTools.getTargetedLocation(player, RANGE);

		Block block = this.origin.getBlock();

		if (block.isLiquid() || BlockTools.isSolid(block)) {
			setState(BendingAbilityState.CannotStart);
			return;
		}
	}

	@Override
	public boolean swing() {
		switch (this.state) {
		case None:
		case CannotStart:
			return false;
		case CanStart:
			World world = this.player.getWorld();

			this.width = (int) PluginTools.firebendingDayAugment(WIDTH, world);
			this.height = (int) PluginTools.firebendingDayAugment(HEIGHT, world);
			this.duration = (long) PluginTools.firebendingDayAugment(DURATION, world);
			this.damage = (int) PluginTools.firebendingDayAugment(DAMAGE, world);

			if (this.bender.hasPath(BendingPath.Nurture)) {
				this.damage *= 0.8;
			}
			if (this.bender.hasPath(BendingPath.Lifeless)) {
				this.damage *= 1.1;
			}

			this.time = this.startedTime;

			Vector direction = this.player.getEyeLocation().getDirection();
			Vector compare = direction.clone();
			compare.setY(0);

			if (Math.abs(direction.angle(compare)) > Math.toRadians(maxangle)) {
				return false;
			}

			initializeBlocks();
			setState(BendingAbilityState.Progressing);
			AbilityManager.getManager().addInstance(this);

			return false;
		case Preparing:
		case Prepared:
		case Progressing:
		case Ending:
		case Ended:
		case Removed:
		default:
			return false;
		}
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		this.time = System.currentTimeMillis();

		if ((this.time - this.startedTime) > this.duration) {
			return false;
		}

		if ((this.time - this.startedTime) > (this.intervaltick * interval)) {
			this.intervaltick++;
			display();
		}

		if ((this.time - this.startedTime) > (this.damagetick * DAMAGE_INTERVAL)) {
			this.damagetick++;
			damage();
		}
		return true;
	}

	private void initializeBlocks() {
		Vector direction = this.player.getEyeLocation().getDirection();
		direction = direction.normalize();

		Vector ortholr = Tools.getOrthogonalVector(direction, 0, 1);
		ortholr = ortholr.normalize();

		Vector orthoud = Tools.getOrthogonalVector(direction, 90, 1);
		orthoud = orthoud.normalize();

		double w = this.width;
		double h = this.height;
		// TODO : Make it no longer pass through the walls
		for (double i = -w; i <= w; i++) {
			for (double j = -h; j <= h; j++) {
				Location location = this.origin.clone().add(orthoud.clone().multiply(j));
				location = location.add(ortholr.clone().multiply(i));
				if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.WallOfFire, location)) {
					continue;
				}
				Block block = location.getBlock();
				if (!this.blocks.contains(block)) {
					this.blocks.add(block);
				}
			}
		}
	}

	private void display() {
		for (Block block : this.blocks) {
			block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 15);
		}
	}

	private void damage() {
		double radius = this.height;
		if (radius < this.width) {
			radius = this.width;
		}
		radius = radius + 1;
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(this.origin, radius);
		if (entities.contains(this.player)) {
			entities.remove(this.player);
		}

		for (LivingEntity entity : entities) {
			if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.WallOfFire, entity.getLocation())) {
				continue;
			}
			for (Block block : this.blocks) {
				if (entity.getLocation().distance(block.getLocation()) <= 1.5) {
					affect(entity);
					break;
				}
			}
		}
	}

	private void affect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtectedByCitizens(entity)) {
			return;
		}
		entity.setVelocity(new Vector(0, 0, 0));
		new Enflamed(this.player, entity, 1, this);
		EntityTools.damageEntity(this.player, entity, this.damage);	
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}
package net.avatar.realms.spigot.bending.abilities.water;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.TempBlock;

public class SpikeFieldColumn {
	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 2000;
	
	@ConfigurationParameter("Live")
	private static long LIVE = 5000;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 4;

	@ConfigurationParameter("Throw-Mult")
	private static double THROW_MULT = 0.7;

	private static double speed = 25;

	private long interval;

	private Location location;
	private Block block;
	private int progress = 0;
	private int damage = DAMAGE;
	private long time;
	int height = 2;
	private Vector thrown = new Vector(0, THROW_MULT, 0);
	private List<TempBlock> affectedblocks = new LinkedList<TempBlock>();
	private List<LivingEntity> damaged = new LinkedList<LivingEntity>();
	private Player player;

	public SpikeFieldColumn(Player player, Location origin, int damage, Vector throwing, long aoecooldown, SpikeField spikeField) {
		interval = (long) (1000 / speed);
		this.player = player;
		this.location = origin.clone();
		this.block = this.location.getBlock();
		this.damage = damage;
		this.thrown = throwing;
		this.time = System.currentTimeMillis() - interval;
	}

	public boolean progress() {
		if (this.block.getType() != Material.ICE) {
			return false;
		}
		if (System.currentTimeMillis() - this.time >= interval) {
			if (this.progress < this.height) {
				this.time = System.currentTimeMillis();
				this.progress++;
				Block affectedblock = this.location.getBlock().getRelative(BlockFace.UP);
				if(affectedblock.getType() != Material.AIR) {
					progress = height;
					return true;
				}
				this.location = affectedblock.getLocation();
				if (ProtectionManager.isRegionProtectedFromBending(this.player, BendingAbilities.IceSpike, this.location)) {
					return false;
				}
				for (LivingEntity en : EntityTools.getLivingEntitiesAroundPoint(this.location, 1.4)) {
					if (en != this.player && !this.damaged.contains((en))) {
						LivingEntity le = en;
						affect(le);
					}
				}
				affectedblocks.add(new TempBlock(affectedblock, Material.ICE, (byte) 0x0));

				return true;
			}
			if(System.currentTimeMillis() - time > LIVE) {
				return false;
			}
		}
		return true;
	}

	private void affect(LivingEntity entity) {
		if (ProtectionManager.isEntityProtected(entity)) {
			return;
		}
		entity.setVelocity(this.thrown);
		entity.damage(this.damage);
		this.damaged.add(entity);
		long slowCooldown = IceSpike.slowCooldown;
		int mod = 2;
		if (entity instanceof Player) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) entity);
			if (bPlayer.canBeSlowed()) {
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
				entity.addPotionEffect(effect);
				bPlayer.slow(slowCooldown);
			}
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 70, mod);
			entity.addPotionEffect(effect);
		}
	}
	
	public void remove() {
		for(TempBlock b : affectedblocks) {
			b.revertBlock();
		}
		affectedblocks.clear();
	}
}

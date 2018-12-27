package net.bendercraft.spigot.bending.abilities.arts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingAbilityState;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

@SuppressWarnings("deprecation")
@ABendingAbility(name = C4.NAME, affinity = BendingAffinity.CHI)
public class C4 extends BendingActiveAbility {
	public final static String NAME = "C4";

	@ConfigurationParameter("Radius")
	private static double RADIUS = 3;

	@ConfigurationParameter("Damage")
	private static int MAX_DAMAGE = 4;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 20000;

	@ConfigurationParameter("Fuse-Interval")
	private static int INTERVAL = 1500;

	@ConfigurationParameter("Max-Bombs-Amount")
	private static int MAX_BOMBS = 2;

	@ConfigurationParameter("Max-Range")
	private static int MAX_RANGE = 3;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 1000 * 60 * 10;
	
	@ConfigurationParameter("Parastick-Enhance-Factor")
	private static double PARASTICK_ENHANCE_FACTOR = 3.0;

	private static final Particle EXPLODE = Particle.EXPLOSION_HUGE;

	private UUID id = UUID.randomUUID();
	private TempBlock bomb = null;
	private boolean hidden = false;
	private Location location;
	private Block hitBlock = null;
	private BlockFace hitFace = null;
	
	private Arrow arrow;

	private long cooldown;
	private double radius;

	public C4(RegisteredAbility register, Player player) {
		super(register, player);
		
		this.cooldown = COOLDOWN;
		if(bender.hasPerk(BendingPerk.MASTER_AIMCD_C4CD_SLICE_CD)) {
			this.cooldown -= 500;
		}
		
		this.radius = RADIUS;
		if(bender.hasPerk(BendingPerk.MASTER_STRAIGHTSHOTCD_C4RADIUS_NEBULARCD)) {
			this.radius += 1;
		}
		
		loadBlockByDir(player.getEyeLocation(), player.getEyeLocation().getDirection());
	}
	
	public void setArrow(Arrow arrow) {
		this.arrow = arrow;
		loadBlockByDir(arrow.getLocation(), arrow.getVelocity().normalize());
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(arrow != null) {
			if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
				return false;
			}
			if (!BlockTools.isFluid(location.getBlock()) && !BlockTools.isPlant(location.getBlock())) {
				return false;
			}
		} else {
			if (!hasDetonator(player)) {
				return false;
			}
			if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
				return false;
			}
			if (!BlockTools.isFluid(location.getBlock()) && !BlockTools.isPlant(location.getBlock())) {
				return false;
			}
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}
		int cpt = 0;
		for (BendingAbility ab : instances.values()) {
			if (ab.getPlayer().equals(player)) {
				cpt++;
				if (cpt >= MAX_BOMBS) {
					return false;
				}
			}
		}
		return true;
	}

	private void loadBlockByDir(Location source, Vector direction) {
		BlockIterator bi = null;
		hitBlock = player.getEyeLocation().getBlock();
		Block previousBlock = player.getEyeLocation().getBlock();

		bi = new BlockIterator(source.getWorld(), source.toVector(), direction.normalize(), 0, MAX_RANGE);
		while (bi.hasNext() && BlockTools.isFluid(hitBlock)) {
			previousBlock = hitBlock;
			hitBlock = bi.next();
		}

		if ((hitBlock != null) && (previousBlock != null)) {
			hitFace = hitBlock.getFace(previousBlock);
			location = previousBlock.getLocation();
		}
	}

	@Override
	public boolean swing() {
		if (getState().equals(BendingAbilityState.PROGRESSING)) {
			// The block has already been posed

			long now = System.currentTimeMillis();
			if ((now - startedTime) < 200) {
				// This would mean that it is the same event as its creation
				return false;
			}
			return true;
		}
		
		if(location.getBlock().getType() != Material.AIR) {
			return false;
		}

		generateC4(location.getBlock(), hitFace);

		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public boolean sneak() {
		if (!isState(BendingAbilityState.PROGRESSING)) {
			return false;
		}

		if (System.currentTimeMillis() <= (startedTime + INTERVAL)) {
			return false;
		}

		if (!hasDetonator(player)) {
			return false;
		}

		activate();

		return false;
	}

	private boolean hasDetonator(Player player) {
		ItemStack held = player.getInventory().getItemInMainHand();
		if ((held.getType() == Material.LEVER) || (held.getType() == Material.BOW)) {
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if(!isState(BendingAbilityState.PROGRESSING)) {
			return;
		}

		if(bomb == null && !hidden) {
			remove();
			return;
		}

		if(!hidden && bomb != null && bomb.getBlock().getType() != Material.SKELETON_SKULL) {
			remove();
			return;
		}

		if(bomb != null && bomb.getBlock().getDrops() != null) {
			bomb.getBlock().getDrops().clear();
		}
	}

	private void activate() {
		location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 10, 1);
		location.getWorld().spawnParticle(EXPLODE, location, 1, 0, 0, 0);

		if(bomb != null) {
			bomb.revertBlock();
		}

		explode();

		bender.cooldown(NAME, cooldown);
		remove();
	}

	private void generateC4(Block block, BlockFace face) {
		Rotatable data = (Rotatable) Material.SKELETON_SKULL.createBlockData();
		data.setRotation(face.getOppositeFace());
		if(bender.hasPerk(BendingPerk.MASTER_SMOKE_HIDE_SHIELD)) {
			hidden = true;
			player.sendBlockChange(block.getLocation(), Material.TNT, (byte) 0x0);
		} else {
			hidden = false;
			bomb = TempBlock.makeTemporary(this, block, Material.SKELETON_SKULL, data, false);
			Skull skull = (Skull) bomb.getBlock().getState();
			skull.setSkullType(SkullType.PLAYER);
			skull.setOwner("MHF_TNT");
			skull.update();
			bomb.getBlock().getDrops().clear();
		}
		
		location.getWorld().playSound(location, Sound.BLOCK_GRAVEL_STEP, 10, 1);
	}

	private void explode() {
		boolean obsidian = false;

		List<Block> affecteds = new LinkedList<Block>();
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (block.getType() == Material.OBSIDIAN) {
				obsidian = true;
			}
			if (!obsidian || (obsidian && (location.distance(block.getLocation()) < (radius / 2.0)))) {
				if (!ProtectionManager.isLocationProtectedFromBending(player, register, block.getLocation()) && !ProtectionManager.isLocationProtectedFromExplosion(player, NAME, block.getLocation())) {
					affecteds.add(block);
				}
			}
		}
		for (Block block : affecteds) {
			if (!block.getType().equals(Material.BEDROCK)) {
				if (!obsidian || (location.distance(block.getLocation()) < 2.0)) {
					if (isCFour(block) != null) {
						continue;
					}
					if (block.getType() == Material.TNT) {
						block.setType(Material.AIR);
						block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), TNTPrimed.class);
					} else {
						List<Block> adjacent = new LinkedList<Block>();
						adjacent.add(block.getRelative(BlockFace.NORTH));
						adjacent.add(block.getRelative(BlockFace.SOUTH));
						adjacent.add(block.getRelative(BlockFace.EAST));
						adjacent.add(block.getRelative(BlockFace.WEST));
						adjacent.add(block.getRelative(BlockFace.UP));
						adjacent.add(block.getRelative(BlockFace.DOWN));
						if (affecteds.containsAll(adjacent)) {
							// Explosion ok
							removeBlock(block);
						} else {
							double rand = Math.random();
							if (rand < 0.8) {
								removeBlock(block);
							}
						}
					}
				}
			}
		}

		double factor = 1.5;
		if (ParaStick.hasParaStick(player)) {
			ParaStick stick = ParaStick.getParaStick(player);
			stick.consume();
			
			factor *= PARASTICK_ENHANCE_FACTOR;
			if(stick.isEnhanced()) {
				factor *= 1.5;
			}
		}
		
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, radius * factor);
		for (LivingEntity entity : entities) {
			affect(entity);
		}
	}

	private void removeBlock(Block block) {
		if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
			CoreProtectAPI cp = CoreProtect.getInstance().getAPI();
			cp.logRemoval(player.getName(), block.getLocation(), block.getType(), block.getData());
		}
		double rand = Math.random();
		if (rand < 0.5) {
			block.getDrops().clear();
		}
		block.breakNaturally();
	}

	@Override
	public void stop() {
		if(bomb != null) {
			bomb.revertBlock();
		}
		if(arrow != null) {
			arrow.remove();
		}
	}

	public static Object isCFour(Block block) {
		if (block.getType() != Material.SKELETON_SKULL) {
			return null;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		for (Object obj : instances.keySet()) {
			if (((C4) instances.get(obj)).bomb.equals(block)) {
				return obj;
			}
		}
		return null;
	}

	@Override
	public Object getIdentifier() {
		return id;
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}
	
	private void affect(Entity entity) {
		BendingHitEvent event = new BendingHitEvent(this, entity);
		Bending.callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		
		double distance = entity.getLocation().distance(location);
		if (distance > radius) {
			return;
		}
		DamageTools.damageEntity(bender, entity, this, MAX_DAMAGE);
		double dx = entity.getLocation().getX() - location.getX();
		double dy = entity.getLocation().getY() - location.getY();
		double dz = entity.getLocation().getZ() - location.getZ();
		Vector v = new Vector(dx, dy, dz);
		v = v.normalize();
		v.multiply(distance);
		entity.setVelocity(v);
	}

	public static C4 getCFour(Object id) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances != null) && !instances.isEmpty()) {
			return (C4) instances.get(id);
		}
		return null;
	}

}

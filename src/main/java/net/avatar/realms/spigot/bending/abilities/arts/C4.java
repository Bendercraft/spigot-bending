package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.coreprotect.CoreProtectAPI;

@ABendingAbility(name = C4.NAME, affinity = BendingAffinity.CHI)
public class C4 extends BendingActiveAbility {
	public final static String NAME = "C4";

	private static int ID = Integer.MIN_VALUE;

	@ConfigurationParameter("Radius")
	private static double RADIUS = 2.35;

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

	private static final Particle EXPLODE = Particle.EXPLOSION_HUGE;

	private int id;
	private Block bomb = null;
	private Location location;
	private Material previousType;
	private Block hitBlock = null;
	private BlockFace hitFace = null;
	
	private Arrow arrow;

	public C4(RegisteredAbility register, Player player) {
		super(register, player);
		loadBlockByDir(player.getEyeLocation(), player.getEyeLocation().getDirection());
		previousType = location.getBlock().getType();
	}
	
	public void setArrow(Arrow arrow) {
		this.arrow = arrow;
		previousType = null;
		loadBlockByDir(arrow.getLocation(), arrow.getVelocity().normalize());
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if(arrow != null) {
			if (ProtectionManager.isLocationProtectedFromBending(player, NAME, location)) {
				return false;
			}
			if (!BlockTools.isFluid(location.getBlock()) && !BlockTools.isPlant(location.getBlock())) {
				return false;
			}
		} else {
			if (!hasDetonator(player)) {
				return false;
			}
			if (ProtectionManager.isLocationProtectedFromBending(player, NAME, location)) {
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

		generateCFour(location.getBlock(), hitFace);
		id = ID++;

		setState(BendingAbilityState.PROGRESSING);
		return false;
	}

	@Override
	public boolean sneak() {

		if (!getState().equals(BendingAbilityState.PROGRESSING)) {
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
		if (!getState().equals(BendingAbilityState.PROGRESSING)) {
			return;
		}

		if (bomb == null) {
			remove();
			return;
		}

		if ((bomb != null) && (bomb.getType() != Material.SKULL)) {
			remove();
			return;
		}

		if (bomb.getDrops() != null) {
			bomb.getDrops().clear();
		}
	}

	private void activate() {
		location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 10, 1);
		location.getWorld().spawnParticle(EXPLODE, location, 1, 0, 0, 0);

		if ((bomb != null) && (previousType != null)) {
			bomb.setType(previousType);
		}

		explode();

		bender.cooldown(NAME, COOLDOWN);
		remove();
	}

	@SuppressWarnings("deprecation")
	private void generateCFour(Block block, BlockFace face) {
		bomb = block;
		byte facing = 0x1;
		switch (face) {
			case SOUTH:
				facing = 0x3;
				break;
			case NORTH:
				facing = 0x2;
				break;
			case WEST:
				facing = 0x4;
				break;
			case EAST:
				facing = 0x5;
				break;
			default:
				facing = 0x1;
				break;
		}
		bomb.setTypeIdAndData(Material.SKULL.getId(), facing, true);
		Skull skull = (Skull) bomb.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner("MHF_TNT");
		skull.update();
		bomb.getDrops().clear();
		location.getWorld().playSound(location, Sound.BLOCK_GRAVEL_STEP, 10, 1);
	}

	private void explode() {
		boolean obsidian = false;

		List<Block> affecteds = new LinkedList<Block>();
		for (Block block : BlockTools.getBlocksAroundPoint(location, RADIUS)) {
			if (block.getType() == Material.OBSIDIAN) {
				obsidian = true;
			}
			if (!obsidian || (obsidian && (location.distance(block.getLocation()) < (RADIUS / 2.0)))) {
				if (!ProtectionManager.isLocationProtectedFromBending(player, NAME, block.getLocation()) && !ProtectionManager.isLocationProtectedFromExplosion(player, NAME, block.getLocation())) {
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

		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, RADIUS + 1);
		for (LivingEntity entity : entities) {
			if (ProtectionManager.isEntityProtected(entity)) {
				continue;
			}
			dealDamage(entity);
			knockBack(entity);
		}
	}

	public void dealDamage(Entity entity) {
		double distance = entity.getLocation().distance(location);
		if (distance > RADIUS) {
			return;
		}

		EntityTools.damageEntity(bender, entity, MAX_DAMAGE);
	}

	private void knockBack(Entity entity) {
		double distance = entity.getLocation().distance(location);
		if (distance > RADIUS) {
			return;
		}
		double dx = entity.getLocation().getX() - location.getX();
		double dy = entity.getLocation().getY() - location.getY();
		double dz = entity.getLocation().getZ() - location.getZ();
		Vector v = new Vector(dx, dy, dz);
		v = v.normalize();

		v.multiply(distance);

		entity.setVelocity(v);
	}

	@SuppressWarnings("deprecation")
	private void removeBlock(Block block) {
		if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
			CoreProtectAPI cp = CoreProtectAPI.plugin.getAPI();
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
		if (bomb != null && previousType != null) {
			bomb.setType(previousType);
		}
	}

	public static Object isCFour(Block block) {
		if (block.getType() != Material.SKULL) {
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

	public void cancel() {
		bomb.setType(previousType);
		remove();
	}

	@Override
	public Object getIdentifier() {
		return id;
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

	public static C4 getCFour(Object id) {

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if ((instances != null) && !instances.isEmpty()) {
			return (C4) instances.get(id);
		}
		return null;
	}

}

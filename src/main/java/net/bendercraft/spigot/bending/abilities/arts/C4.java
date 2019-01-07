package net.bendercraft.spigot.bending.abilities.arts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.bendercraft.spigot.bending.abilities.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.event.BendingHitEvent;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.DamageTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;


@ABendingAbility(name = C4.NAME, affinity = BendingAffinity.CHI)
public class C4 extends BendingActiveAbility {
	public static final String NAME = "C4";

	private static final OfflinePlayer SKULL_OWNER;
	static {
		final String SKIN_PLAYER_ID = "55e73380-a973-4a52-9bb5-1efa5256125c"; //MHF_TNT2
		final UUID SKIN_PLAYER_UUID = UUID.fromString(SKIN_PLAYER_ID);
		//final GameProfile profile = new GameProfile(SKIN_PLAYER_UUID, "MHF_TNT2");

		//profile.getProperties().put("textures", new Property("textures","dc75cd6f9c713e9bf43fea963990d142fc0d252974ebe04b2d882166cbb6d294"));
		SKULL_OWNER = Bukkit.getServer().getOfflinePlayer(SKIN_PLAYER_UUID);
	}

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
	private static int MAX_RANGE = 4;

	@ConfigurationParameter("Max-Detonation-Range")
	private static double MAX_DETONATION_RANGE = 128.0f;

	@ConfigurationParameter("Sound-Interval")
	private static int SOUND_INTERVAL = 1500;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 1000 * 60 * 10;

	@ConfigurationParameter("Max-Duration-Offrange")
	private static long MAX_DURATION_OFFRANGE = (30+60) * 1000;
	
	@ConfigurationParameter("Parastick-Enhance-Factor")
	private static double PARASTICK_ENHANCE_FACTOR = 3.0;

	private static final Particle EXPLODE = Particle.EXPLOSION_HUGE;

	private UUID id = UUID.randomUUID();
	private TempBlock bomb = null;
	private boolean hidden = false;
	private Location location;
	private Block hitBlock = null;
	private BlockFace hitFace = null;
	private Material headType = null;

	private long lastTimeBiped;
	
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

		lastTimeBiped = startedTime;

		loadPlayerTargetedBlock();
	}
	
	public void setArrow(Arrow arrow) {
		this.arrow = arrow;
		loadBlockByDirection(arrow.getLocation(), arrow.getVelocity().normalize());
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		if (location == null) {
			return false;
		}
		Block block = location.getBlock();
		if(arrow != null) {
			if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
				return false;
			}
			if (!BlockTools.isFluid(block) && !BlockTools.isPlant(block)) {
				return false;
			}
		}
		else {
			if (!hasDetonator(player)) {
				return false;
			}
			if (ProtectionManager.isLocationProtectedFromBending(player, register, location)) {
				return false;
			}
			if (!BlockTools.isFluid(block) && !BlockTools.isPlant(block)) {
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

	private void loadPlayerTargetedBlock() {
		RayTraceResult rayTraceResult = player.rayTraceBlocks(MAX_RANGE);
		if (rayTraceResult != null) {
			hitBlock = rayTraceResult.getHitBlock();
			if (hitBlock != null) {
				hitFace = rayTraceResult.getHitBlockFace();
				location = hitBlock.getRelative(hitFace).getLocation();
			}
		}
	}

	private void loadBlockByDirection(Location source, Vector direction) {
		BlockIterator bi;
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

		double distance = player.getLocation().distance(location);
		if (distance > MAX_DETONATION_RANGE) {
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
		held = player.getInventory().getItemInOffHand();
		if (Material.LEVER == held.getType()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canTick() {
		if (!super.canTick()) {
			return false;
		}

		if (bomb == null && !hidden) {
			return false;
		}

		if (!hidden && bomb.getBlock().getType() != headType) {
			return false;
		}

		return true;
	}

	@Override
	public void progress() {
		if(!isState(BendingAbilityState.PROGRESSING)) {
			return;
		}

		Location playerLocation = player.getLocation();
		if (!playerLocation.getWorld().equals(location.getWorld())) {
			remove();
			return;
		}

		long now = System.currentTimeMillis();
		double distance = playerLocation.distance(location);
		if (distance < MAX_DETONATION_RANGE) {
			if (now - lastTimeBiped >= SOUND_INTERVAL) {
				player.playSound(playerLocation, Sound.BLOCK_LEVER_CLICK, (float)(1-(distance/MAX_DETONATION_RANGE)), 1.0f);
				lastTimeBiped = now;
			}
		}
		else {
			if (now - lastTimeBiped > MAX_DURATION_OFFRANGE) {
				bender.cooldown(NAME, cooldown/2);
				remove();
				return;
			}
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

		if (!bender.isOnCooldown(NAME)) {
			bender.cooldown(NAME, cooldown);
		}

		remove();
	}

	private void generateC4(Block block, BlockFace face) {

		if (bender.hasPerk(BendingPerk.MASTER_SMOKE_HIDE_SHIELD)) {
			hidden = true;
			player.sendBlockChange(block.getLocation(), Material.TNT.createBlockData());
		}
		else {
			hidden = false;

			chooseHeadType(face);

			final BlockData data = headType.createBlockData();
			if (Material.PLAYER_WALL_HEAD == headType) {
				Directional directionable = (Directional) data;
				directionable.setFacing(face);
			}

			bomb = TempBlock.makeTemporary(this, block, headType, data, false);
			Block b = bomb.getBlock();
			Skull skull = (Skull) b.getState();
			//skull.setOwningPlayer(SKULL_OWNER);
			skull.setOwner("MHF_TNT2");
			skull.update();
			b.getDrops().clear();
		}
		
		location.getWorld().playSound(location, Sound.BLOCK_GRAVEL_STEP, 10, 1);
	}

	private void chooseHeadType(BlockFace face) {
		if (!(BlockFace.UP == face || BlockFace.DOWN == face)) {
			headType = Material.PLAYER_WALL_HEAD;
		}
		else {
			headType = Material.PLAYER_HEAD;
		}
	}

	private void explode() {
		boolean obsidian = false;

		List<Block> affecteds = new LinkedList<>();
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (block.getType() == Material.OBSIDIAN) {
				obsidian = true;
			}
			if (!obsidian || (location.distance(block.getLocation()) < (radius / 2.0))) {
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
						List<Block> adjacent = new LinkedList<>();
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
			cp.logRemoval(player.getName(), block.getLocation(), block.getType(), block.getBlockData());
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
		Material type = block.getType();
		if (type != Material.PLAYER_HEAD && type != Material.PLAYER_WALL_HEAD) {
			return null;
		}

		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		for (Object obj : instances.keySet()) {
			if (((C4) instances.get(obj)).bomb.getBlock().equals(block)) {
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

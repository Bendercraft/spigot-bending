package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

@BendingAbility(name="Plastic Bomb", element=BendingType.ChiBlocker, specialization = BendingSpecializationType.Inventor)
public class C4 {
	private static Map<Player,C4> instances = new HashMap<Player, C4>();
	
	@ConfigurationParameter("Radius")
	private static double RADIUS = 2.35;
	
	@ConfigurationParameter("Damage")
	private static int MAX_DAMAGE = 4;
	
	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 20000;
	
	private static final ParticleEffect EXPLODE = ParticleEffect.EXPLOSION_HUGE;
	
	@ConfigurationParameter("Fuse-Interval")
	private static int INTERVAL = 1500;
	
	private Player player;
	private Player target = null;
	private Block bomb = null;;
	private ItemStack headBomb = null;
	private ItemStack saveHead = null;
	private Location location;
	private Material previousType;
	private long fuseTime = 0;
	
	public C4 (Player player, Block block, BlockFace face){
		if (player == null || block == null || face == null){
			return;
		}
		if (instances.containsKey(player)) {
			return;
		}
		if (!hasDetonator(player)){
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		if (bPlayer.isOnCooldown(Abilities.PlasticBomb)) {
			return;
		}
		Block temp = block.getRelative(face);
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PlasticBomb, temp.getLocation())){
			return;
		}		
		if (!BlockTools.isFluid(temp) && !BlockTools.isPlant(temp)) {
			return;
		}
		this.player = player;
		this.location = temp.getLocation();
		this.previousType = temp.getType();
		this.fuseTime = System.currentTimeMillis();
		this.generateCFour(temp, face);
		instances.put(player, this);
	}
	
	public C4 (Player player, LivingEntity target) {
		if (player == null || target == null) {
			return;
		}	
		if (!(target instanceof Player)) {
			return;
		}	
		if (!hasDetonator(player)) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		if (bPlayer.isOnCooldown(Abilities.PlasticBomb)) {
			return;
		}
		
		this.player = player;
		this.target = (Player) target;
		this.fuseTime = System.currentTimeMillis();
		this.generateHeadBomb();
		instances.put(player,this);
	}
	
	private boolean hasDetonator(Player player) {
		ItemStack held = player.getItemInHand();
		if (held.getType() == Material.LEVER ||
				held.getType() == Material.BOW) {
			return true;
		}
		return false;
	}
	
	public static void activate(Player player) {
		if (instances.containsKey(player)) {
			C4 bomb = instances.get(player);
			bomb.activate();
		}
	}
	
	public static void progressAll() {
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : instances.keySet()){
			boolean keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
			}
		}
		
		for (Player p : toRemove){
			instances.get(p).remove();
			instances.remove(p);
		}
	}
	
	public boolean progress() {
		if (!player.isOnline() || player.isDead()) {
			return false;
		}
		
		if (bomb == null && headBomb == null) {
			return false;
		}
		
		if ((bomb!=null) && (bomb.getType() != Material.SKULL)) {
			return false;
		}
		
		if (((headBomb!=null) && (headBomb.getType() != Material.SKULL_ITEM))) {
			return false;
		}
		
		if(bomb.getDrops() != null) {
			bomb.getDrops().clear();
		}
		return true;
	}
	
	public void activate() {
		if (System.currentTimeMillis() <= fuseTime + INTERVAL) {
			return;
		}
		if (!hasDetonator(player)) {
			return;
		}
		if (headBomb != null && target != null) {
			location = target.getEyeLocation();
		}
		
		location.getWorld().playSound(location, Sound.EXPLODE, 10, 1);
		EXPLODE.display(0, 0, 0, 1, 1, location, 20);
		
		if (bomb != null && previousType != null) {
			bomb.setType(previousType);
		}
		else if (headBomb != null && target != null){
			target.getInventory().setHelmet(saveHead);
		}
		
		explode();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null) {
			bPlayer.cooldown(Abilities.PlasticBomb, COOLDOWN);
		}
		instances.remove(player);
	}
	
	@SuppressWarnings("deprecation")
	private void generateCFour(Block block, BlockFace face) {
		bomb = block;
		byte facing = 0x1;
		switch (face) {
			case SOUTH : facing = 0x3; break;
			case NORTH : facing = 0x2; break;
			case WEST : facing = 0x4; break;
			case EAST : facing = 0x5; break;
			default : facing = 0x1; break;
		}
		bomb.setTypeIdAndData(Material.SKULL.getId(), facing, true);
		Skull skull = (Skull) bomb.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner("MHF_TNT");
		skull.update();
		bomb.getDrops().clear();
		location.getWorld().playSound(location, Sound.STEP_GRAVEL, 10, 1);
	}
	
	@SuppressWarnings("deprecation")
	private void generateHeadBomb() {
		headBomb = new ItemStack(397, 1, (short) 3);
		SkullMeta meta = (SkullMeta) headBomb.getItemMeta();
		meta.setOwner("MHF_TNT");
		headBomb.setItemMeta(meta);
		
		this.saveHead = target.getInventory().getHelmet();
		target.getInventory().setHelmet(headBomb);
	}
	
	private void explode() {
		boolean obsidian = false;
		
		List<Block> affecteds = new LinkedList<Block>();
		for (Block block : BlockTools.getBlocksAroundPoint(location, RADIUS)) {
			if (block.getType() == Material.OBSIDIAN) {
				obsidian = true;
			}
			if (!obsidian || (obsidian && location.distance(block.getLocation()) < RADIUS/2.0)) {
				if (!ProtectionManager.isRegionProtectedFromBending(player, Abilities.PlasticBomb,
						block.getLocation()) 
						&& !ProtectionManager.isRegionProtectedFromExplosion(player, Abilities.PlasticBomb, block.getLocation())) {
					affecteds.add(block);
				}
			}
		}
		for (Block block : affecteds) {
			if(!block.getType().equals(Material.BEDROCK)) {
				if (!obsidian || location.distance(block.getLocation())<2.0) {
					if (block.getType() == Material.TNT) {
						block.setType(Material.AIR);
						block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), TNTPrimed.class);
					}else {
						List<Block> adjacent = new LinkedList<Block>();
						adjacent.add(block.getRelative(BlockFace.NORTH));
						adjacent.add(block.getRelative(BlockFace.SOUTH));
						adjacent.add(block.getRelative(BlockFace.EAST));
						adjacent.add(block.getRelative(BlockFace.WEST));
						adjacent.add(block.getRelative(BlockFace.UP));
						adjacent.add(block.getRelative(BlockFace.DOWN));
						if(affecteds.containsAll(adjacent)) {
							//Explosion ok
							this.removeBlock(block);
						} else {
							double rand = Math.random();
							if(rand < 0.8) {
								this.removeBlock(block);
							}
						}
						
					}
				}	
			}
		}
		
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, RADIUS+1);
		for(LivingEntity entity : entities) {
			if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			this.dealDamage(entity);
			this.knockBack(entity);
		}
	}
	
	public void dealDamage(Entity entity) {
		double distance = entity.getLocation().distance(location);
		if (distance > RADIUS){
			return;
		}	

		EntityTools.damageEntity(player, entity, MAX_DAMAGE);
	}
	
	private void knockBack(Entity entity) {
		double distance = entity.getLocation().distance(location);
		if (distance > RADIUS){
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
		if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
			CoreProtectAPI cp = CoreProtectAPI.plugin.getAPI();
			cp.logRemoval(player.getName(), block.getLocation(), block.getType(), block.getData());
		}
		double rand = Math.random();
		if(rand < 0.5) {
			block.getDrops().clear();
		}
		block.breakNaturally();
	}
	
	public static void removeAll() {
		for (C4 plastic : instances.values()) {
			plastic.remove();
		}
		instances.clear();
	}
	
	public static C4 isTarget(Player targ) {
		if(targ == null) {
			return null;
		}
		for (C4 c4 : instances.values()) {
			if(c4.target != null && c4.target.equals(targ)) {
				return c4;
			}
		}
		return null;
	}
	
	public ItemStack getHeadBomb(){
		return headBomb;
	}
	
	public void remove() {
		if (bomb != null) {
			bomb.setType(previousType);
		}
		else if (headBomb != null) {
			if (target != null) {
				target.getInventory().setHelmet(saveHead);
			}
		}
		
	}
	
	public static Player isCFour(Block block) {
		if (block.getType() != Material.SKULL) {
			return null;
		}
		
		for (Player p : instances.keySet()) {
			if (instances.get(p).bomb.equals(block)) {
				return p;
			}
		}
		return null;
	}
	
	public static C4 getCFour (Player p) {
		if (instances.containsKey(p)) {
			return instances.get(p);
		}
		return null;
	}
	
	public void cancel() {
		bomb.setType(previousType);
		instances.remove(player);
	}

}

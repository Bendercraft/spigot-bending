package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ParticleEffect;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;
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
import org.bukkit.util.Vector;

public class CFour {
	private static Map<Player,CFour> instances = new HashMap<Player, CFour>();
	
	private static double radius = ConfigManager.plasticRadius;
	private static int maxDamage = ConfigManager.plasticDamage;
	private static final ParticleEffect EXPLODE = ParticleEffect.HUGE_EXPLOSION;
	
	private static final int fuseInterval = 1500;
	
	private Player player;
	private Block bomb;
	private Location location;
	private Material previousType;
	private long fuseTime = 0;
	
	public CFour (Player player, Block block, BlockFace face){
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
			CFour bomb = instances.get(player);
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
		return true;
	}
	
	public void activate() {
		if (System.currentTimeMillis() <= fuseTime + fuseInterval) {
			return;
		}
		if (!hasDetonator(player)) {
			return;
		}
		location.getWorld().playSound(location, Sound.EXPLODE, 10, 1);
		EXPLODE.display(location, 0, 0, 0, 1, 1);
		explode();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null) {
			bPlayer.cooldown(Abilities.PlasticBomb);
		}
		instances.remove(player);
	}
	
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
	}
	
	private void explode() {
		boolean obsidian = false;
		bomb.setType(previousType);
		List<Block> affecteds = new LinkedList<Block>();
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (block.getType() == Material.OBSIDIAN) {
				obsidian = true;
			}
			if (!obsidian || (obsidian && location.distance(block.getLocation()) < radius/2.0)) {
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
		
		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(location, radius);
		for(LivingEntity entity : entities) {
			this.dealDamage(entity);
			this.knockBack(entity);
		}
	}
	
	public void dealDamage(Entity entity) {
		double distance = entity.getLocation()
				.distance(location);
		if (distance > radius){
			return;
		}	

		EntityTools.damageEntity(player, entity, maxDamage);
	}
	
	private void knockBack(Entity entity) {
		double distance = entity.getLocation()
				.distance(bomb.getLocation());
		if (distance > radius){
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
	
	private void removeBlock(Block block) {
		if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
			CoreProtectAPI cp = CoreProtectAPI.plugin.getAPI();
			cp.logRemoval(player.getName(), block.getLocation(), block.getType().getId(), block.getData());
		}
		double rand = Math.random();
		if(rand < 0.5) {
			block.getDrops().clear();
		}
		block.breakNaturally();
	}
	
	public static void removeAll() {
		for (CFour plastic : instances.values()) {
			plastic.remove();
		}
		instances.clear();
	}
	
	public void remove() {
		bomb.setType(previousType);
	}

}

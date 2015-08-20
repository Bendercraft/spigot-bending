package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.coreprotect.CoreProtectAPI;

@BendingAbility(name="Plastic Bomb", element=BendingType.ChiBlocker, specialization = BendingSpecializationType.Inventor)
public class C4 extends Ability{

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

	private static final ParticleEffect EXPLODE = ParticleEffect.EXPLOSION_HUGE;

	private int id;
	private Block bomb = null;;
	private Location location;
	private Material previousType;
	private Block hitBlock = null;
	private BlockFace hitFace = null;

	public C4 (Player player){
		super (player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		if (!hasDetonator(this.player)) {
			setState(AbilityState.CannotStart);
		}

		loadBlockByDir(player.getEyeLocation(),player.getEyeLocation().getDirection());

		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PlasticBomb, this.location)){
			setState(AbilityState.CannotStart);
			return;
		}		
		if (!BlockTools.isFluid(this.location.getBlock()) && !BlockTools.isPlant(this.location.getBlock())) {
			setState(AbilityState.CannotStart);
			return;
		}
		this.previousType = this.location.getBlock().getType();
	}

	public C4 (Player player, Arrow arrow) {
		super (player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		loadBlockByDir(arrow.getLocation(),arrow.getVelocity().normalize());

		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PlasticBomb, this.location)){
			setState(AbilityState.CannotStart);
			return;
		}		
		if (!BlockTools.isFluid(this.location.getBlock()) && !BlockTools.isPlant(this.location.getBlock())) {
			setState(AbilityState.CannotStart);
			return;
		}
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.PlasticBomb);
		if ((instances == null) || instances.isEmpty()) {
			return true;
		}
		int cpt = 0;
		for (Ability ab : instances.values()) {
			if (ab.getPlayer().equals(this.player)) {
				cpt++;
				if (cpt >= MAX_BOMBS) {
					return false;
				}
			}
		}
		return true;
	}

	private void loadBlockByDir(Location source,Vector direction) {
		BlockIterator bi = null;
		this.hitBlock = this.player.getEyeLocation().getBlock();
		Block previousBlock = this.player.getEyeLocation().getBlock();

		bi = new BlockIterator(source.getWorld(), source.toVector(), direction.normalize(), 0, MAX_RANGE);
		while (bi.hasNext() && BlockTools.isFluid(this.hitBlock)) {
			previousBlock = this.hitBlock;
			this.hitBlock = bi.next();
		}

		if ((this.hitBlock != null) && (previousBlock != null)) {
			this.hitFace = this.hitBlock.getFace(previousBlock);
			this.location = previousBlock.getLocation();
		}
	}

	@Override
	public boolean swing() {

		if (this.state.isBefore(AbilityState.CanStart)) {
			return true;
		}

		if (this.state.equals(AbilityState.Progressing)) {
			// The block has already been posed

			long now = System.currentTimeMillis();
			if ((now - this.startedTime) < 200) {
				// This would mean that it is the same event as its creation
				return false;
			}
			return true;
		}

		this.generateCFour(this.location.getBlock(), this.hitFace);
		this.id = ID++;
		AbilityManager.getManager().addInstance(this);

		setState(AbilityState.Progressing);
		return false;
	}

	@Override
	public boolean sneak() {

		if (!this.state.equals(AbilityState.Progressing)) {
			return false;
		}

		if (System.currentTimeMillis() <= (this.startedTime + INTERVAL)) {
			return false;
		}

		if (!hasDetonator(this.player)) {
			return false;
		}

		activate();

		return false;
	}

	private boolean hasDetonator(Player player) {
		ItemStack held = player.getItemInHand();
		if ((held.getType() == Material.LEVER) ||
				(held.getType() == Material.BOW)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (!this.state.equals(AbilityState.Progressing)) {
			return true;
		}

		if (this.bomb == null) {
			return false;
		}

		if ((this.bomb!=null) && (this.bomb.getType() != Material.SKULL)) {
			return false;
		}

		if(this.bomb.getDrops() != null) {
			this.bomb.getDrops().clear();
		}
		return true;
	}

	private void activate() {

		this.location.getWorld().playSound(this.location, Sound.EXPLODE, 10, 1);
		EXPLODE.display(0, 0, 0, 1, 1, this.location, 20);

		if ((this.bomb != null) && (this.previousType != null)) {
			this.bomb.setType(this.previousType);
		}

		explode();

		this.bender.cooldown(Abilities.PlasticBomb, COOLDOWN);

		setState(AbilityState.Ended);
	}

	@SuppressWarnings("deprecation")
	private void generateCFour(Block block, BlockFace face) {
		this.bomb = block;
		byte facing = 0x1;
		switch (face) {
			case SOUTH : facing = 0x3; break;
			case NORTH : facing = 0x2; break;
			case WEST : facing = 0x4; break;
			case EAST : facing = 0x5; break;
			default : facing = 0x1; break;
		}
		this.bomb.setTypeIdAndData(Material.SKULL.getId(), facing, true);
		Skull skull = (Skull) this.bomb.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner("MHF_TNT");
		skull.update();
		this.bomb.getDrops().clear();
		this.location.getWorld().playSound(this.location, Sound.STEP_GRAVEL, 10, 1);
	}

	private void explode() {
		boolean obsidian = false;

		List<Block> affecteds = new LinkedList<Block>();
		for (Block block : BlockTools.getBlocksAroundPoint(this.location, RADIUS)) {
			if (block.getType() == Material.OBSIDIAN) {
				obsidian = true;
			}
			if (!obsidian || (obsidian && (this.location.distance(block.getLocation()) < (RADIUS/2.0)))) {
				if (!ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.PlasticBomb,
						block.getLocation()) 
						&& !ProtectionManager.isRegionProtectedFromExplosion(this.player, Abilities.PlasticBomb, block.getLocation())) {
					affecteds.add(block);
				}
			}
		}
		for (Block block : affecteds) {
			if(!block.getType().equals(Material.BEDROCK)) {
				if (!obsidian || (this.location.distance(block.getLocation())<2.0)) {
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

		List<LivingEntity> entities = EntityTools.getLivingEntitiesAroundPoint(this.location, RADIUS+1);
		for(LivingEntity entity : entities) {
			if(ProtectionManager.isEntityProtectedByCitizens(entity)) {
				continue;
			}
			this.dealDamage(entity);
			this.knockBack(entity);
		}
	}

	public void dealDamage(Entity entity) {
		double distance = entity.getLocation().distance(this.location);
		if (distance > RADIUS){
			return;
		}	

		EntityTools.damageEntity(this.player, entity, MAX_DAMAGE);
	}

	private void knockBack(Entity entity) {
		double distance = entity.getLocation().distance(this.location);
		if (distance > RADIUS){
			return;
		}	
		double dx = entity.getLocation().getX() - this.location.getX();
		double dy = entity.getLocation().getY() - this.location.getY();
		double dz = entity.getLocation().getZ() - this.location.getZ();
		Vector v = new Vector(dx, dy, dz);
		v = v.normalize();

		v.multiply(distance);

		entity.setVelocity(v);
	}

	@SuppressWarnings("deprecation")
	private void removeBlock(Block block) {
		if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
			CoreProtectAPI cp = CoreProtectAPI.plugin.getAPI();
			cp.logRemoval(this.player.getName(), block.getLocation(), block.getType(), block.getData());
		}
		double rand = Math.random();
		if(rand < 0.5) {
			block.getDrops().clear();
		}
		block.breakNaturally();
	}

	@Override
	public void stop() {
		if (this.bomb != null) {
			this.bomb.setType(this.previousType);
		}
	}

	@Override
	public void remove() {
		AbilityManager.getManager().getInstances(Abilities.PlasticBomb).remove(this.id);
	}

	public static Object isCFour(Block block) {
		if (block.getType() != Material.SKULL) {
			return null;
		}

		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.PlasticBomb);
		for (Object obj : instances.keySet()) {
			if (((C4)instances.get(obj)).bomb.equals(block)) {
				return obj;
			}
		}
		return null;
	}

	public void cancel() {
		this.bomb.setType(this.previousType);
		remove();
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.PlasticBomb;
	}

	@Override
	public Object getIdentifier() {
		return this.id;
	}

	public static C4 getCFour(Object id) {

		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.PlasticBomb);
		if ((instances != null)  && !instances.isEmpty()) {
			return (C4) instances.get(id);
		}
		return null;
	}

}

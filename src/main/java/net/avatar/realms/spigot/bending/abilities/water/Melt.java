package net.avatar.realms.spigot.bending.abilities.water;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.abilities.energy.AvatarState;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Phase Change", element=BendingType.Water)
public class Melt implements IAbility {


	private int range = FreezeMelt.RANGE;
	private int radius = FreezeMelt.RADIUS;
	
	private static final byte full = 0x0;
	private IAbility parent;
	
	public Melt(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer.isOnCooldown(Abilities.PhaseChange)) {
			return;
		}
		
		int range = (int) PluginTools.waterbendingNightAugment(this.range,
				player.getWorld());
		int radius = (int) PluginTools.waterbendingNightAugment(this.radius,
				player.getWorld());
		
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			radius = AvatarState.getValue(radius);
		}
		Location location = EntityTools.getTargetedLocation(player, range);
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			melt(player, block);
		}
		
		bPlayer.cooldown(Abilities.PhaseChange, FreezeMelt.COOLDOWN);
	}
	
	@SuppressWarnings("deprecation")
	public static void melt(Player player, Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PhaseChange, block.getLocation())) {
			return;
		}
		
		if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			return;
		}
		if (!Torrent.canThaw(block)) {
			Torrent.thaw(block);
			return;
		}
		if (BlockTools.isMeltable(block) && !TempBlock.isTempBlock(block)
				&& WaterManipulation.canPhysicsChange(block)) {
			if (block.getType() == Material.SNOW) {
				block.setType(Material.AIR);
				return;
			}
			if (FreezeMelt.isFrozen(block)) {
				FreezeMelt.thawThenRemove(block);
			} else {
				block.setType(Material.WATER);
				block.setData(full);
			}
		}
	}
	
	public static void evaporate(Player player, Block block) {
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PhaseChange,
				block.getLocation())) {
			return;
		}
		if (BlockTools.isWater(block) && !TempBlock.isTempBlock(block)
				&& WaterManipulation.canPhysicsChange(block)) {
			block.setType(Material.AIR);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
		}
	}
	
	@Override
	public IAbility getParent() {
		return this.parent;
	}
	
}

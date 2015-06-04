package net.avatarrealms.minecraft.bending.listeners;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.abilities.air.AirBubble;
import net.avatarrealms.minecraft.bending.abilities.chi.C4;
import net.avatarrealms.minecraft.bending.abilities.chi.Paralyze;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthGrab;
import net.avatarrealms.minecraft.bending.abilities.earth.LavaTrain;
import net.avatarrealms.minecraft.bending.abilities.fire.FireStream;
import net.avatarrealms.minecraft.bending.abilities.fire.Illumination;
import net.avatarrealms.minecraft.bending.abilities.fire.Lightning;
import net.avatarrealms.minecraft.bending.abilities.water.Bloodbending;
import net.avatarrealms.minecraft.bending.abilities.water.FreezeMelt;
import net.avatarrealms.minecraft.bending.abilities.water.OctopusForm;
import net.avatarrealms.minecraft.bending.abilities.water.Torrent;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.abilities.water.WaterWall;
import net.avatarrealms.minecraft.bending.abilities.water.Wave;
import net.avatarrealms.minecraft.bending.utils.BlockTools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class BendingBlockListener implements Listener{
	
	public Bending plugin;

	public BendingBlockListener(Bending bending) {
		this.plugin = bending;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		BendingPlayer.getBendingPlayer(player).cooldown();
		if (Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.getCause() == IgniteCause.LIGHTNING) {
			if (Lightning.isNearbyChannel(event.getBlock().getLocation())) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(BlockFromToEvent event) {
		Block toblock = event.getToBlock();
		Block fromblock = event.getBlock();
		if (BlockTools.isWater(fromblock)) {
			event.setCancelled(!AirBubble.canFlowTo(toblock));
			if (!event.isCancelled()) {
				event.setCancelled(!WaterManipulation.canFlowFromTo(fromblock,
						toblock));
			}
			if (!event.isCancelled()) {
				if (Illumination.isIlluminated(toblock))
					toblock.setType(Material.AIR);
			}
		}
		if(BlockTools.isLava(fromblock)) {
			if(LavaTrain.isLavaPart(fromblock)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		event.setCancelled(Illumination.isIlluminated(block));
		if (!event.isCancelled()) {
			event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(FreezeMelt.isFrozen(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Wave.canThaw(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Torrent.canThaw(block));
		}
		if (FireStream.isIgnited(block)) {
			FireStream.remove(block);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		if (!event.isCancelled())
			event.setCancelled(Illumination.isIlluminated(block));
		if (!event.isCancelled())
			event.setCancelled(BlockTools.tempnophysics.contains(block));
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (WaterWall.wasBrokenFor(player, block)
				|| OctopusForm.wasBrokenFor(player, block)
				|| Torrent.wasBrokenFor(player, block)) {
			event.setCancelled(true);
			return;
		}
		EarthBlast blast = EarthBlast.getBlastFromSource(block);
		if (blast != null) {
			blast.cancel();
		}

		Player bomber = C4.isCFour(block);
		if (bomber != null) {
			block.getDrops().clear();
			C4.getCFour(bomber).cancel();
		}
		

		EarthGrab grab = EarthGrab.blockInEarthGrab(block);
		if (grab != null) {
			grab.setToKeep(false);
			event.setCancelled(true);
		}

		if (FreezeMelt.isFrozen(block)) {
			FreezeMelt.thawThenRemove(block);
			event.setCancelled(true);
		} else if (WaterWall.isWaterWallPart(block)) {
			WaterWall.thaw(block);
			event.setCancelled(true);
		} else if (Illumination.isIlluminated(block)) {
			event.setCancelled(true);
		} else if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			event.setCancelled(true);
		} else if (BlockTools.bendedBlocks.containsKey(block)) {
			BlockTools.removeRevertIndex(block);
		}
		TempBlock.revertBlock(block);
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (TempBlock.isTempBlock(event.getBlock()))
			event.setCancelled(true);
		if (!WaterManipulation.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
	}
}

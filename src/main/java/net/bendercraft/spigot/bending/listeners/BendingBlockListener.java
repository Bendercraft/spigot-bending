package net.bendercraft.spigot.bending.listeners;

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
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.air.AirBubble;
import net.bendercraft.spigot.bending.abilities.arts.C4;
import net.bendercraft.spigot.bending.abilities.earth.EarthGrab;
import net.bendercraft.spigot.bending.abilities.fire.FireStream;
import net.bendercraft.spigot.bending.abilities.fire.Lightning;
import net.bendercraft.spigot.bending.abilities.water.Bloodbending;
import net.bendercraft.spigot.bending.abilities.water.OctopusForm;
import net.bendercraft.spigot.bending.abilities.water.PhaseChange;
import net.bendercraft.spigot.bending.abilities.water.Torrent;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.abilities.water.WaterWall;
import net.bendercraft.spigot.bending.abilities.water.Wave;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BendingBlockListener implements Listener {

	public Bending plugin;

	public BendingBlockListener(Bending bending) {
		this.plugin = bending;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		BendingPlayer.getBendingPlayer(player).cooldown();
		if (Bloodbending.isBloodbended(player)) {
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
				event.setCancelled(!WaterManipulation.canFlowFromTo(fromblock, toblock));
			}
		}
		if (BlockTools.isLava(fromblock) && TempBlock.isTempBlock(fromblock)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		if (!event.isCancelled()) {
			event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(PhaseChange.isFrozen(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Wave.canThaw(block));
		}
		if (FireStream.isIgnited(block)) {
			FireStream.remove(block);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		if (!event.isCancelled()) {
			event.setCancelled(BlockTools.isTempNoPhysics(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (WaterWall.wasBrokenFor(player, block) || OctopusForm.wasBrokenFor(player, block) || Torrent.wasBrokenFor(player, block)) {
			event.setCancelled(true);
			return;
		}

		Object bomber = C4.isCFour(block);
		if (bomber != null) {
			block.getDrops().clear();
			C4.getCFour(bomber).cancel();
		}

		if (PhaseChange.isFrozen(block)) {
			PhaseChange.thawThenRemove(block);
			event.setCancelled(true);
		} else if (WaterWall.isWaterWallPart(block)) {
			WaterWall.thaw(block);
			event.setCancelled(true);
		} else if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			event.setCancelled(true);
		}
		TempBlock.revertBlock(block);
	}

	//Counter Factions protection
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onEarthGrabBreak(BlockBreakEvent event) {

		EarthGrab grab = EarthGrab.blockInEarthGrab(event.getBlock());
		if (grab != null) {
			grab.setToKeep(false);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (TempBlock.isTempBlock(event.getBlock())) {
			event.setCancelled(true);
		}
		if (!WaterManipulation.canPhysicsChange(event.getBlock())) {
			event.setCancelled(true);
		}
	}
}

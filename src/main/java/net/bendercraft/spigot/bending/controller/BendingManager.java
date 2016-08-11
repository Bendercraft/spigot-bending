package net.bendercraft.spigot.bending.controller;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingAbilityCooldown;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.fire.Enflamed;
import net.bendercraft.spigot.bending.abilities.fire.FireStream;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.TempBlock;

public class BendingManager implements Runnable {
	private Bending plugin;
	private long time;
	private long timestep = 1; // in ms
	
	private List<Queue> revertQueue = new LinkedList<Queue>();

	public BendingManager(Bending bending) {
		this.plugin = bending;
		this.time = System.currentTimeMillis();
	}

	@Override
	public void run() {
		try {
			long now = System.currentTimeMillis();
			timestep = now - this.time;
			this.time = now;

			AbilityManager.getManager().progressAllAbilities();
			FlyingPlayer.handleAll();
			
			manageFirebending();

			manageGlobalTempBlock();
			
			if(Settings.USE_SCOREBOARD) {
				//One scoreboard per player
				//One team per scoreboard (team name = player name)
				//Team's entries = ability's name (case sensitive) (not player !) (using this to be able to list scores (not possible otherwise))
				//Entry's score = cooldown left
				for(Player player : plugin.getServer().getOnlinePlayers()) {
					BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
					if(bender != null && bender.isUsingScoreboard()) {
						Scoreboard scoreboard = player.getScoreboard();
						if(scoreboard != bender.getScoreboard()) {
							bender.conflictScoreboard();
							continue;
						}
						Team team = scoreboard.getTeam(player.getName());
						if(team == null) {
							bender.conflictScoreboard();
							continue;
						}
						
						Objective objective = scoreboard.getObjective(BendingPlayer.OBJECTIVE_STATUS);
						if(objective == null) {
							bender.conflictScoreboard();
							continue;
						}
						
						if(EntityTools.isChiBlocked(player)) {
							if(!team.hasEntry("chi")) {
								team.addEntry("chi");
							}
							Score score = objective.getScore(ChatColor.GOLD+"Chiblocked");
							int value = (int) (EntityTools.blockedChiTimeLeft(player, now)/1000);
							if(value != score.getScore()) {
								score.setScore(value);
							}
						} else {
							Score score = objective.getScore(ChatColor.GOLD+"Chiblocked");
							if(score.getScore() == 0) {
								scoreboard.resetScores(ChatColor.GOLD+"Chiblocked");
								team.removeEntry("chi");
							}
						}
						
						if(bender.isBender(BendingElement.FIRE)) {
							if(!team.hasEntry("fire")) {
								team.addEntry("fire");
							}
							Score score = objective.getScore(ChatColor.DARK_RED+"Energy");
							int value = bender.fire.getPower();
							if(value != score.getScore()) {
								score.setScore(value);
							}
						} else {
							if(team.hasEntry("fire")) {
								team.removeEntry("fire");
								scoreboard.resetScores(ChatColor.DARK_RED+"Energy");
							}
						}
						
						
						Map<String, BendingAbilityCooldown> cooldowns = bender.getCooldowns();
						for(Entry<String, BendingAbilityCooldown> entry : cooldowns.entrySet()) {
							if(!team.hasEntry("cd-"+entry.getKey())) {
								team.addEntry("cd-"+entry.getKey());
							}
							
							Score score = objective.getScore(entry.getKey());
							int value = (int) (entry.getValue().timeLeft(now)/1000);
							if(value != score.getScore()) {
								score.setScore(value);
							}
						}
						
						List<String> toRemove = new LinkedList<String>();
						for(String entry : team.getEntries()) {
							if(!cooldowns.containsKey(entry.substring("cd-".length()))) {
								toRemove.add(entry);
							}
						}
						for(String entry : toRemove) {
							scoreboard.resetScores(entry.substring("cd-".length()));
							team.removeEntry(entry);
						}
					}
				}
			}
		} catch (Exception e) {
			try {
				AbilityManager.getManager().stopAllAbilities();
				PluginTools.stopAllBending();
				plugin.getLogger().log(Level.SEVERE, "Exception in bending loop", e);
			} catch (Exception e1) {
				plugin.getLogger().log(Level.SEVERE, "Exception in exception bending loop : this is really bad", e1);
			}
		}

	}

	private void manageGlobalTempBlock() {
		long now = System.currentTimeMillis();
		List<Queue> toRemove = new LinkedList<Queue>();
		for(Queue queue : revertQueue) {
			if(queue.started+queue.life < now) {
				for(TempBlock block : queue.blocks) {
					block.revertBlock();
				}
				toRemove.add(queue);
			}
		}
		revertQueue.removeAll(toRemove);
	}

	private void manageFirebending() {
		for(Player player : plugin.getServer().getOnlinePlayers()) {
			BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
			if(bender != null && bender.isBender(BendingElement.FIRE)) {
				bender.fire.progress();
			}
		}

		FireStream.progressAll();
		Enflamed.progressAll();
	}

	public void addGlobalTempBlock(long life, TempBlock... blocks) {
		List<TempBlock> temp = new LinkedList<TempBlock>();
		Collections.addAll(temp, blocks);
		addGlobalTempBlock(life, temp);
	}
	
	public void addGlobalTempBlock(long life, List<TempBlock> blocks) {
		Queue queue = new Queue();
		queue.started = System.currentTimeMillis();
		queue.life = life;
		queue.blocks = blocks;
		
		revertQueue.add(queue);
	}
	
	public boolean isGlobalTemBlock(TempBlock block) {
		return revertQueue.stream().filter(x -> x.blocks.contains(block)).findAny().isPresent();
	}

	public long getTimestep() {
		return timestep;
	}

	private class Queue {
		private long started;
		private long life;
		private List<TempBlock> blocks;
	}
}

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
import net.bendercraft.spigot.bending.abilities.BendingPerk;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.fire.Enflamed;
import net.bendercraft.spigot.bending.abilities.fire.FireStream;
import net.bendercraft.spigot.bending.abilities.water.Frozen;
import net.bendercraft.spigot.bending.abilities.water.HealingWaters;
import net.bendercraft.spigot.bending.abilities.water.WaterBalance.Damage;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.PluginTools;

public class BendingManager implements Runnable {
	private Bending plugin;
	private long time;
	private long timestep = 1; // in ms

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
			Frozen.handle();
			
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
						
						if(HealingWaters.hasBuff(player) != null) {
							if(!team.hasEntry("healing")) {
								team.addEntry("healing");
							}
							Score score = objective.getScore(ChatColor.LIGHT_PURPLE+"Damage boost");
							score.setScore(1);
						} else {
							if(team.hasEntry("healing")) {
								scoreboard.resetScores(ChatColor.LIGHT_PURPLE+"Damage boost");
								team.removeEntry("healing");
							}
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
							String ability = team.getEntries().stream().filter(e -> e.startsWith("fire-")).findAny().orElse("fire-");
							if(!ability.substring("fire-".length()).equals(bender.fire.getLastAbility())) {
								scoreboard.resetScores(ChatColor.RED+ability.substring("fire-".length()));
								team.removeEntry(ability);
								
								if(bender.fire.getLastAbility() != null) {
									team.addEntry("fire-"+bender.fire.getLastAbility());
									objective.getScore(ChatColor.RED+bender.fire.getLastAbility()).setScore(1);
								}
							}
						} else {
							if(team.hasEntry("fire")) {
								scoreboard.resetScores(ChatColor.DARK_RED+"Energy");
								team.removeEntry("fire");
							}
							String ability = team.getEntries().stream().filter(e -> e.startsWith("fire-")).findAny().orElse(null);
							if(ability != null) {
								scoreboard.resetScores(ChatColor.RED+ability.substring("fire-".length()));
								team.removeEntry(ability);
							}
						}
						
						if(bender.isBender(BendingElement.WATER)) {
							ChatColor color = null;
							if(bender.water.toward(Damage.ICE)) {
								if(!team.hasEntry("water-ice")) {
									team.addEntry("water-ice");
								}
								if(team.hasEntry("water-liquid")) {
									scoreboard.resetScores(ChatColor.BLUE+"Balance");
									team.removeEntry("water-liquid");
								}
								color = ChatColor.AQUA;
							} else {
								if(!team.hasEntry("water-liquid")) {
									team.addEntry("water-liquid");
								}
								if(team.hasEntry("water-ice")) {
									scoreboard.resetScores(ChatColor.AQUA+"Balance");
									team.removeEntry("water-ice");
								}
								color = ChatColor.BLUE;
							}
							
							Score score = objective.getScore(color+"Balance");
							int value = bender.water.getBalance();
							if(value != score.getScore()) {
								score.setScore(value);
							}
						} else {
							if(team.hasEntry("water-liquid")) {
								scoreboard.resetScores(ChatColor.BLUE+"Balance");
								team.removeEntry("water-liquid");
							}
							if(team.hasEntry("water-ice")) {
								scoreboard.resetScores(ChatColor.AQUA+"Balance");
								team.removeEntry("water-ice");
							}
						}
						
						if(bender.hasPerk(BendingPerk.EARTH_PATIENCE) && bender.earth.hasBonus()) {
							if(!team.hasEntry("earth-patience")) {
								team.addEntry("earth-patience");
							}
							Score score = objective.getScore(ChatColor.GREEN+"Patience");
							score.setScore(1);
						} else {
							if(team.hasEntry("earth-patience")) {
								scoreboard.resetScores(ChatColor.GREEN+"Patience");
								team.removeEntry("earth-patience");
							}
						}
						
						if(bender.hasPerk(BendingPerk.EARTH_RESISTANCE) && bender.earth.hasPreventDeath()) {
							if(!team.hasEntry("earth-resistance")) {
								team.addEntry("earth-resistance");
							}
							Score score = objective.getScore(ChatColor.DARK_GREEN+"Resistance");
							score.setScore(1);
						} else {
							if(team.hasEntry("earth-resistance")) {
								scoreboard.resetScores(ChatColor.DARK_GREEN+"Resistance");
								team.removeEntry("earth-resistance");
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
							if(entry.startsWith("cd-")) {
								if(!cooldowns.containsKey(entry.substring("cd-".length()))) {
									toRemove.add(entry);
								}
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

	public long getTimestep() {
		return timestep;
	}
}

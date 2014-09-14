package net.avatarrealms.minecraft.bending.controller;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BendingPlayers {
	public static String FILE_NAME = "benders.json";
	
	private File bendingPlayersFile = null;
	private Map<UUID, BendingPlayerData> bendingPlayers;
	
	private File dataFolder;

	public BendingPlayers(File file) {
		dataFolder = file;
		load();
	}
	
	public BendingPlayerData get(UUID id) {
		if (bendingPlayers != null) {
			return bendingPlayers.get(id);
		}
		return null;
	}
	
	public void removePlayer(UUID playerID) {
		if (bendingPlayers != null) {
			bendingPlayers.remove(playerID);
			this.save();
		}
	}

	public void setPlayer(UUID playerID, BendingPlayer player) {
		if (bendingPlayers != null) {
			bendingPlayers.put(playerID, player.serialize());
			this.save();
		}
	}

	public BendingPlayer getBendingPlayer(Player player) {
		if(bendingPlayers != null) {
			if(bendingPlayers.containsKey(player.getUniqueId())) {
				//OK nice, this player has been already converted
				return BendingPlayer.deserialize(bendingPlayers.get(player.getUniqueId()));
			} 
		}
		return null;
	}
	
	public void reload() {
bendingPlayers = new HashMap<UUID, BendingPlayerData>();
		
		if (bendingPlayersFile == null) {
			bendingPlayersFile = new File(dataFolder, FILE_NAME);
		}
		try {
			if(!bendingPlayersFile.exists()) {
				bendingPlayersFile.createNewFile();
				FileWriter content = new FileWriter(bendingPlayersFile);
				content.write("{}");
				content.close();
			}
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(bendingPlayersFile);
			Iterator<Entry<String, JsonNode>> it = root.fields();
			while(it.hasNext()) {
				Entry<String, JsonNode> entry = it.next();
				BendingPlayerData data = mapper.readValue(entry.getValue().traverse(), BendingPlayerData.class);
				UUID uuid = UUID.fromString(entry.getKey());
				bendingPlayers.put(uuid, data);
			}
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + bendingPlayersFile, e);
		}
	}
	
	private void load() {
		if (bendingPlayers == null) {
			reload();
		}
	}

	public void save() {
		if (bendingPlayers == null || bendingPlayersFile == null) {
			return;
		}
		try {
			if(!bendingPlayersFile.exists()) {
				bendingPlayersFile.createNewFile();
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(bendingPlayersFile, bendingPlayers);
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save config to " + bendingPlayersFile, e);
		}
	}

	public void close() {
		save();
		bendingPlayers = null;
		bendingPlayersFile = null;
	}

}
